/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal.gatt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sap.cloud.android.odata.v2.DeviceSetType;
import com.sap.cloud.android.odata.v2.EventSetType;
import com.sap.cloud.mobile.flowv2.model.AppConfig;
import com.sap.cloud.mobile.flowv2.model.OAuth;
import com.sap.cloud.mobile.flowv2.model.OAuthClient;
import com.sap.cloud.mobile.foundation.authentication.OAuth2BrowserProcessor;
import com.sap.cloud.mobile.foundation.authentication.OAuth2Configuration;
import com.sap.cloud.mobile.foundation.authentication.OAuth2Interceptor;
import com.sap.cloud.mobile.foundation.authentication.OAuth2TokenInMemoryStore;
import com.sap.cloud.mobile.foundation.authentication.OAuth2TokenStore;
import com.sap.cloud.mobile.foundation.common.ClientProvider;
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar;
import com.sap.cloud.mobile.odata.LocalDateTime;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.BroadcastHelper;
import org.dpppt.android.sdk.internal.crypto.CryptoModule;
import org.dpppt.android.sdk.internal.crypto.EphId;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.DayDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.app.SAPWizardApplication;
import demo.sap.safetyandroid.app.TraceReportActivity;
import demo.sap.safetyandroid.service.SAPServiceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import static org.dpppt.android.sdk.internal.crypto.CryptoModule.EPHID_LENGTH;
import static org.dpppt.android.sdk.internal.gatt.BleServer.SERVICE_UUID;

public class BleClient {

	private static final String TAG = "BleClient";

	private final Context context;
	private BluetoothLeScanner bleScanner;
	private ScanCallback bleScanCallback;
	private GattConnectionThread gattConnectionThread;

	private HashMap<String, List<Handshake>> scanResultMap = new HashMap<>();
	private HashMap<String, EphId> connectedEphIdMap = new HashMap<>();

	private NotificationManagerCompat notificationManager;
	private static final String NOTIFICATION_CHANNEL_ID = "dp3t_tracing_service";

	public BleClient(Context context) {
		this.context = context;
		gattConnectionThread = new GattConnectionThread();
		gattConnectionThread.start();
	}

	public BluetoothState start() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			BroadcastHelper.sendErrorUpdateBroadcast(context);

			//TEST to turn on bluetooth

			if (!bluetoothAdapter.isEnabled()) {
				Log.i(TAG,"BLUETOOTH OFF A");
				bluetoothAdapter.enable();
			}


			return bluetoothAdapter == null ? BluetoothState.NOT_SUPPORTED : BluetoothState.DISABLED;
		}
		bleScanner = bluetoothAdapter.getBluetoothLeScanner();
		if (bleScanner == null) {
			return BluetoothState.NOT_SUPPORTED;
		}

		List<ScanFilter> scanFilters = new ArrayList<>();
		scanFilters.add(new ScanFilter.Builder()
				.setServiceUuid(new ParcelUuid(SERVICE_UUID))
				.build());

		// Scan for Apple devices as iOS does not advertise service uuid when in background,
		// but instead pushes it to the "overflow" area (manufacturer data). For now let's
		// connect to all Apple devices until we find the algorithm used to put the service uuid
		// into the manufacturer data
		scanFilters.add(new ScanFilter.Builder()
				.setManufacturerData(0x004c, new byte[0])
				.build());



		//scan for specific BeaconUUID, major and minor are used for identify room and floor as you prefer
		ScanFilter.Builder mBuilder = new ScanFilter.Builder();
		ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
		ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);

		String uuidset="A2FA7357-C8CD-4B95-98FD-9D091CE43337"; //example
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			uuidset= bundle.getString("beaconUUID");
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Unable to load meta-data: " + e.getMessage());
		}


		byte[] uuid = asBytes(java.util.UUID.fromString(uuidset));
		mManufacturerData.put(0, (byte) 0xBE);
		mManufacturerData.put(1, (byte) 0xAC);
		for (int i = 2; i <= 17; i++) {
			mManufacturerData.put(i, uuid[i - 2]);
		}
		for (int i = 0; i <= 17; i++) {
			mManufacturerDataMask.put((byte) 0x01);
		}
		mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
		ScanFilter mScanFilter = mBuilder.build();

		scanFilters.add(mScanFilter);

		ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
				.setScanMode(AppConfigManager.getInstance(context).getBluetoothScanMode().getSystemValue())
				.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
				.setReportDelay(0)
				.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
				.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			settingsBuilder
					.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
					.setLegacy(true);
		}


		ScanSettings scanSettings = settingsBuilder.build();

		BluetoothServiceStatus bluetoothServiceStatus = BluetoothServiceStatus.getInstance(context);

		bleScanCallback = new ScanCallback() {
			private static final String TAG = "ScanCallback";

			public void onScanResult(int callbackType, ScanResult result) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				if (result.getScanRecord() != null) {
					onDeviceFound(result);
				}
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				Logger.d(TAG, "Batch size " + results.size());
				for (ScanResult result : results) {
					onScanResult(0, result);
				}
			}

			public void onScanFailed(int errorCode) {
				bluetoothServiceStatus.updateScanStatus(errorCode);
				Logger.e(TAG, "error: " + errorCode);
			}
		};

		bleScanner.startScan(scanFilters, scanSettings, bleScanCallback);
		Logger.i(TAG, "started BLE scanner, scanMode: " + scanSettings.getScanMode() + " scanFilters: " + scanFilters.size());


		notificationManager = NotificationManagerCompat.from(context);

		return BluetoothState.ENABLED;
	}


	public static byte[] asBytes(java.util.UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

//	private void createNotificationChannel() {
//		// Create the NotificationChannel, but only on API 26+ because
//		// the NotificationChannel class is new and not in the support library
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//			CharSequence name = "repubblica";
//			String description = "italia";
//			int importance = NotificationManager.IMPORTANCE_HIGH;
//			NotificationChannel channel = new NotificationChannel("${context.packageName}-$name", name, importance);
//			channel.setDescription(description);
//			// Register the channel with the system; you can't change the importance
//			// or other notification behaviors after this
//			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//			notificationManager.createNotificationChannel(channel);
//		}
//
//
//	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createNotificationChannel() {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		String channelName = context.getString(R.string.dp3t_sdk_service_notification_channel);
		NotificationChannel channel =
				new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
	}


	private void onDeviceFound(ScanResult scanResult) {
		try {
			BluetoothDevice bluetoothDevice = scanResult.getDevice();
			final String deviceAddr = bluetoothDevice.getAddress();


//			Logger.i(TAG,bluetoothDevice.getName());
//			Logger.i(TAG,bluetoothDevice.getAddress());
//
			Logger.i(TAG, String.valueOf(scanResult.getRssi()));

			int power = scanResult.getScanRecord().getTxPowerLevel();
			if (power == Integer.MIN_VALUE) {
				Logger.d(TAG, "No power levels found for " + deviceAddr + ", use default of 12dbm");
				power = 12;
			}


			List<Handshake> handshakesForDevice = scanResultMap.get(deviceAddr);
			if (handshakesForDevice == null) {
				handshakesForDevice = new ArrayList<>();
				scanResultMap.put(deviceAddr, handshakesForDevice);
			}

			byte[] payload = scanResult.getScanRecord().getServiceData(new ParcelUuid(SERVICE_UUID));

			boolean correctPayload = payload != null && payload.length == CryptoModule.EPHID_LENGTH;
			Log.i(TAG, "found " + deviceAddr + "; power: " + power + "; rssi: " + scanResult.getRssi() +
					"; haspayload: " + correctPayload);

			if (correctPayload) {
				// if Android, optimize (meaning: send/read payload directly in the advertisement
				Logger.i(TAG, "handshake with " + deviceAddr + " (servicedata payload)");
				handshakesForDevice.add(createHandshake(new EphId(payload), scanResult, power));
			} else {
				if (handshakesForDevice.isEmpty()) {
					Log.i(TAG, "thread  with " + deviceAddr + " ");
					gattConnectionThread.addTask(new GattConnectionTask(context, bluetoothDevice, scanResult,
							(ephId, device) -> {
								Log.i(TAG, "gatt handshake 3 with " + device.getAddress() + " ");
								connectedEphIdMap.put(device.getAddress(), ephId);
								Log.i(TAG, "handshake with " + device.getAddress() + " (gatt connection)");
							}));




//					final Handler handler = new Handler();
//					handler.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							// Do something after 5s = 5000ms
//							bluetoothDevice.connectGatt(context,false,gattCallback,BluetoothDevice.TRANSPORT_LE);
//						}
//					}, 500);



				}

				//Log.i(TAG, "handshake 2 with " + deviceAddr + " (no servicedata payload)");
				handshakesForDevice.add(createHandshake(null, scanResult, power));
			}


			//for ibeacon
			byte[] scanRecord = scanResult.getScanRecord().getBytes();
			findBeaconPattern(scanRecord,scanResult.getRssi());

			Log.i("DISTANCE", calculateDistance(power, scanResult.getRssi()));
			;


		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}





	final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

			Log.i(TAG, "conConnectionStateChange 2 " + status + " " + newState);

			super.onConnectionStateChange(gatt, status, newState);



			if (newState == BluetoothProfile.STATE_CONNECTING) {
				Log.i(TAG, "connecting... " + status);
			} else if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "connected " + status);
				Log.i(TAG, "requesting mtu...");
				gatt.requestMtu(512);

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED || newState == BluetoothProfile.STATE_DISCONNECTING) {
				Log.i(TAG, "Gatt Connection disconnected " + status);
				//			finish();
				gatt.disconnect();
				gatt.close();
				gatt = null;
			}
		}

		@Override
		public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
			Log.i(TAG, "discovering services...");
			gatt.discoverServices();
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			BluetoothGattService service = gatt.getService(BleServer.SERVICE_UUID);

			if (service == null) {
				Log.i(TAG, "No GATT service for " + BleServer.SERVICE_UUID + " found, status=" + status);
				//			finish();
				return;
			}

			Logger.i(TAG, "Service " + service.getUuid() + " found");

			BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleServer.TOTP_CHARACTERISTIC_UUID);

			boolean initiatedRead = gatt.readCharacteristic(characteristic);
			if (!initiatedRead) {
				Log.i(TAG, "Failed to initiate characteristic read");
			} else {
				Log.i(TAG, "Read initiated");
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicRead [status:" + status + "] " + characteristic.getUuid() + ": " +
					Arrays.toString(characteristic.getValue()));

			if (characteristic.getUuid().equals(BleServer.TOTP_CHARACTERISTIC_UUID)) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					if (characteristic.getValue().length == EPHID_LENGTH) {
//						callback.onEphIdRead(new EphId(characteristic.getValue()), gatt.getDevice());
//						scanResult.getRssi();
					} else {
						Log.i(TAG, "got wrong sized ephid " + characteristic.getValue().length);
					}
				} else {
					Log.i(TAG, "Failed to read characteristic. Status: " + status);
					// TODO error
				}
			}
			//		finish();
			Log.i(TAG, "Closed Gatt Connection");
		}
	};




	private void createAlertnotification(int rssi){

		//new code
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel();
		}
		SharedPreferences sharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);


		long notification_timestamp =  sharedPref.getLong(String.valueOf(R.string.notification_timestamp),0);


//			boolean isFD68 = false;
//			if(scanResult.getScanRecord().getServiceUuids()!=null && scanResult.getScanRecord().getServiceUuids().get(0) != null)
//			 	isFD68 = scanResult.getScanRecord().getServiceUuids().get(0).getUuid() == new ParcelUuid(SERVICE_UUID).getUuid();

		if(rssi > -73){
			//show notification
			Log.i("FD68","isin");

			if(notification_timestamp != 0){

				java.util.Date date = new java.util.Date();
				Timestamp timestamp1 = new Timestamp(date.getTime()); //now

				Timestamp timestamp2 = new Timestamp(notification_timestamp);

				long diff =	timestamp1.getTime()-timestamp2.getTime();

				if (diff > 60000){
					//if 1 minute passed, launch alert

					Intent notificationIntent = new Intent(context, TraceReportActivity.class);

					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);

					PendingIntent intent = PendingIntent.getActivity(context, 0,
							notificationIntent, 0);




					NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
							.setSmallIcon(R.drawable.logo2)
							.setContentTitle(context.getResources().getString(R.string.Attenzione))
							.setContentText(context.getResources().getString(R.string.Mantieni_le_distanze))
							.setContentIntent(intent)
							.setStyle(new NotificationCompat.BigTextStyle()
									.bigText(context.getResources().getString(R.string.Mantieni_le_distanze)))
							.setPriority(NotificationCompat.PRIORITY_MAX)
							.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
							.setDefaults(NotificationCompat.DEFAULT_ALL);



					// notificationId is a unique int for each notification that you must define
					notificationManager.notify(1001, builder.build());


					SharedPreferences.Editor editor = sharedPref.edit();

					Long timestamp = System.currentTimeMillis();
					editor.putLong(String.valueOf(R.string.notification_timestamp), timestamp);
					editor.commit();
				}
			}
			else{
				Intent notificationIntent = new Intent(context, TraceReportActivity.class);

				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);

				PendingIntent intent = PendingIntent.getActivity(context, 0,
						notificationIntent, 0);


				NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
						.setSmallIcon(R.drawable.logo2)
						.setContentTitle(context.getResources().getString(R.string.Attenzione))
						.setContentText(context.getResources().getString(R.string.Mantieni_le_distanze))
						.setContentIntent(intent)
						.setStyle(new NotificationCompat.BigTextStyle()
								.bigText(context.getResources().getString(R.string.Mantieni_le_distanze)))
						.setPriority(NotificationCompat.PRIORITY_MAX)
						.setDefaults(NotificationCompat.DEFAULT_ALL);



				// notificationId is a unique int for each notification that you must define
				notificationManager.notify(1001, builder.build());


				SharedPreferences.Editor editor = sharedPref.edit();

				Long timestamp = System.currentTimeMillis();
				editor.putLong(String.valueOf(R.string.notification_timestamp), timestamp);
				editor.commit();
			}




		}

	}

	public String calculateDistance(int txPower, double rssi) {
		if (rssi == 0) {
			return "Unknown"; // if we cannot determine accuracy, return -1.
		}
		double ratio = rssi*1.0/txPower;
		if (ratio < 1.0) {
			return getDistance(Math.pow(ratio,10));
		}
		else {
			double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
			return getDistance(accuracy);
		}
	}

	private String getDistance(double accuracy) {
		if (accuracy == -1.0) {
			return "Unknown";
		} else if (accuracy < 1) {
			return "Immediate";
		} else if (accuracy < 3) {
			return "Near";
		} else {
			return "Far";
		}
	}

	private void findBeaconPattern(byte[] scanRecord,int rssi) {
		int startByte = 2;
		boolean patternFound = false;
		while (startByte <= 5) {
			if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
					((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
				patternFound = true;
				break;
			}
			startByte++;
		}

		if (patternFound) {
			//Convert to hex String
			byte[] uuidBytes = new byte[16];
			System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
			String hexString = bytesToHex(uuidBytes);

			//UUID detection
			String uuid = hexString.substring(0, 8) + "-" +
					hexString.substring(8, 12) + "-" +
					hexString.substring(12, 16) + "-" +
					hexString.substring(16, 20) + "-" +
					hexString.substring(20, 32);

			// major
			final int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);

			// minor
			final int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

			Log.i(TAG, "UUID: " + uuid + "\\nmajor: " + major + "\\nminor" + minor);

			//call iminroom if UUID is equal to the UUID set on device hardware and on androidmanifest.xml

			//TEST

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
					.setSmallIcon(R.drawable.logo2)
					.setContentTitle("INROOM")
					.setContentText("UUID: " + uuid )

					.setStyle(new NotificationCompat.BigTextStyle()
							.bigText("UUID: " + uuid ))
					.setPriority(NotificationCompat.PRIORITY_MAX)
					.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
					.setDefaults(NotificationCompat.DEFAULT_ALL);



			// notificationId is a unique int for each notification that you must define
			notificationManager.notify(1001, builder.build());

			//END TEST

			String uuidset="A2FA7357-C8CD-4B95-98FD-9D091CE43337"; //example
			try {
				ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				uuidset= bundle.getString("beaconUUID");
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(TAG, "Unable to load meta-data: " + e.getMessage());
			}

			if(uuid.equalsIgnoreCase(uuidset)) {
				try {
					Iminroom(uuid, rssi);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}



	public void Iminroom(String uuid,int distance) throws IOException {

		SAPWizardApplication app= (SAPWizardApplication)context.getApplicationContext();
		AppConfig appconfig = app.getAppConfig();
		String clientID = ((OAuthClient)((OAuth)appconfig.getAuth().get(0)).getConfig().getOAuthClients().get(0)).getClientID();
		String authUrl = ((OAuth)appconfig.getAuth().get(0)).getConfig().getAuthorizationEndpoint();
		String tokenUrl = ((OAuth)appconfig.getAuth().get(0)).getConfig().getTokenEndpoint();



		CryptoModule crypto = CryptoModule.getInstance(context);
		// Proxy Class
		EventSetType event = new EventSetType();
// Using type specific setters to set the property value
		event.setCreatedAt( LocalDateTime.now());
		event.setSourceEID(bytesToHex(crypto.getCurrentEphId().getData()));
		event.setTargetIED(uuid);
		event.setDistance(BigInteger.valueOf(distance));


		if (android.os.Build.VERSION.SDK_INT > 9)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		SAPServiceManager sapServiceManager = app.getSAPServiceManager();
		try {
			sapServiceManager.getv2().createEntity(event);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("SEND EVENT",e.getLocalizedMessage());
		}



	}
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}

		String temp= new String(hexChars);

		String val = "2";   // fox to match iOS app Ephid Format
		String result = temp.replaceAll("(.{" + val + "})", "$1 ").trim();
		System.out.println(result.toLowerCase());

		return result.toLowerCase();
		//return new String(hexChars);
	}


	private Handshake createHandshake(EphId ephId, ScanResult scanResult, int power) {
		return new Handshake(-1, System.currentTimeMillis(), ephId, power, scanResult.getRssi(),
				BleCompat.getPrimaryPhy(scanResult), BleCompat.getSecondaryPhy(scanResult),
				scanResult.getTimestampNanos());
	}

	public synchronized void stopScan() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			bleScanner = null;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return;
		}
		if (bleScanner != null) {
			Log.i(TAG, "stopping BLE scanner");
			bleScanner.stopScan(bleScanCallback);

			bleScanner = null;

		}
	}

	public synchronized void stop() {
		gattConnectionThread.terminate();
		stopScan();

		Database database = new Database(context);
		for (Map.Entry<String, List<Handshake>> entry : scanResultMap.entrySet()) {
			String device = entry.getKey();
			List<Handshake> handshakes = scanResultMap.get(device);
			if (connectedEphIdMap.containsKey(device)) {
				EphId ephId = connectedEphIdMap.get(device);
				for (Handshake handshake : handshakes) {
					handshake.setEphId(ephId);
					Log.i("Add handshake",bytesToHex(ephId.getData()));
					database.addHandshake(context, handshake);
					createAlertnotification(handshake.getRssi());
				}
			} else {
				for (Handshake handshake : handshakes) {
					if (handshake.getEphId() != null) {
						database.addHandshake(context, handshake);
						createAlertnotification(handshake.getRssi());
					}
				}
			}
		}
	}


}
