/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.sdk.internal;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.app.SAPWizardApplication;
import demo.sap.safetyandroid.app.TraceReportActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.dpppt.android.sdk.DP3T;

import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.crypto.ContactsFactory;
import org.dpppt.android.sdk.internal.crypto.CryptoModule;
import org.dpppt.android.sdk.internal.crypto.EphId;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Contact;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.gatt.BleClient;
import org.dpppt.android.sdk.internal.gatt.BleServer;
import org.dpppt.android.sdk.internal.gatt.BluetoothServiceStatus;
import org.dpppt.android.sdk.internal.gatt.BluetoothState;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.DayDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.dpppt.android.sdk.internal.AppConfigManager.DEFAULT_SCAN_DURATION;
import static org.dpppt.android.sdk.internal.AppConfigManager.DEFAULT_SCAN_INTERVAL;

public class TracingService extends Service {

	private static final String TAG = "TracingService";

	public static final String ACTION_START = TracingService.class.getCanonicalName() + ".ACTION_START";
	public static final String ACTION_RESTART_CLIENT = TracingService.class.getCanonicalName() + ".ACTION_RESTART_CLIENT";
	public static final String ACTION_RESTART_SERVER = TracingService.class.getCanonicalName() + ".ACTION_RESTART_SERVER";
	public static final String ACTION_STOP = TracingService.class.getCanonicalName() + ".ACTION_STOP";

	public static final String EXTRA_ADVERTISE = TracingService.class.getCanonicalName() + ".EXTRA_ADVERTISE";
	public static final String EXTRA_RECEIVE = TracingService.class.getCanonicalName() + ".EXTRA_RECEIVE";
	public static final String EXTRA_SCAN_INTERVAL = TracingService.class.getCanonicalName() + ".EXTRA_SCAN_INTERVAL";
	public static final String EXTRA_SCAN_DURATION = TracingService.class.getCanonicalName() + ".EXTRA_SCAN_DURATION";

	private static final String NOTIFICATION_CHANNEL_ID = "dp3t_tracing_service";
	private static final int NOTIFICATION_ID = 1827;

	private Handler handler;
	private PowerManager.WakeLock wl;

	private BleServer bleServer;
	private BleClient bleClient;

	private NotificationManagerCompat notificationManager;


	private final BroadcastReceiver bluetoothStateChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
				if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_ON) {
					Logger.w(TAG, BluetoothAdapter.ACTION_STATE_CHANGED);
					BluetoothServiceStatus.resetInstance();
					BroadcastHelper.sendErrorUpdateBroadcast(context);


					//TEST to turn on bluetooth
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (!mBluetoothAdapter.isEnabled()) {
						Log.i(TAG,"BLUETOOTH OFF");
						mBluetoothAdapter.enable();
					}
				}
			}
		}
	};

	private final BroadcastReceiver locationServiceStateChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (LocationManager.MODE_CHANGED_ACTION.equals(intent.getAction())) {
				Logger.w(TAG, LocationManager.MODE_CHANGED_ACTION);
				BroadcastHelper.sendErrorUpdateBroadcast(context);
			}
		}
	};

	private final BroadcastReceiver errorsUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BroadcastHelper.ACTION_UPDATE_ERRORS.equals(intent.getAction())) {
				invalidateForegroundNotification();
			}
		}
	};

	private boolean startAdvertising;
	private boolean startReceiving;
	private long scanInterval;
	private long scanDuration;

	private boolean isFinishing;

	public TracingService() { }

	@Override
	public void onCreate() {
		super.onCreate();

		isFinishing = false;

		IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(bluetoothStateChangeReceiver, bluetoothFilter);

		IntentFilter locationServiceFilter = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
		registerReceiver(locationServiceStateChangeReceiver, locationServiceFilter);

		IntentFilter errorsUpdateFilter = new IntentFilter(BroadcastHelper.ACTION_UPDATE_ERRORS);
		registerReceiver(errorsUpdateReceiver, errorsUpdateFilter);

		notificationManager = NotificationManagerCompat.from(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null || intent.getAction() == null) {
			stopSelf();
			return START_NOT_STICKY;
		}

		if (wl == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					getPackageName() + ":TracingServiceWakeLock");
			wl.acquire();
		}

		Logger.i(TAG, "onStartCommand() with " + intent.getAction());

		scanInterval = intent.getLongExtra(EXTRA_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL);
		scanDuration = intent.getLongExtra(EXTRA_SCAN_DURATION, DEFAULT_SCAN_DURATION);

		startAdvertising = intent.getBooleanExtra(EXTRA_ADVERTISE, true);
		startReceiving = intent.getBooleanExtra(EXTRA_RECEIVE, true);

		if (ACTION_START.equals(intent.getAction())) {
			startForeground(NOTIFICATION_ID, createForegroundNotification());
			start();
		} else if (ACTION_RESTART_CLIENT.equals(intent.getAction())) {
			startForeground(NOTIFICATION_ID, createForegroundNotification());
			ensureStarted();
			restartClient();
		} else if (ACTION_RESTART_SERVER.equals(intent.getAction())) {
			startForeground(NOTIFICATION_ID, createForegroundNotification());
			ensureStarted();
			restartServer();
		} else if (ACTION_STOP.equals(intent.getAction())) {
			stopForegroundService();
		}

		//TEST
		sendephemeral();

		sendcontacts();

		checkInfected();

		return START_REDELIVER_INTENT;
	}



	private Notification createForegroundNotification() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel();
		}

		Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		PendingIntent contentIntent = null;
		if (launchIntent != null) {
			contentIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		TracingStatus status = DP3T.getStatus(this);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setOngoing(true)
				.setSmallIcon(R.mipmap.ic_launcher_round)
				.setContentIntent(contentIntent);

		if (status.getErrors().size() > 0) {
			String errorText = getNotificationErrorText(status.getErrors());
			builder.setContentTitle(getString(R.string.dp3t_sdk_service_notification_title))
					.setContentText(errorText)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(errorText))
					.setPriority(NotificationCompat.PRIORITY_DEFAULT);

			Log.i("ERROR",errorText);
			if(errorText.startsWith("BLE")){
				//segnalo su activity che il bluetooth è spento
				Intent in = new Intent();
				in.putExtra("TYPE", errorText);
				in.setAction("noBLE");
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);

			}


		} else {
			// every minute is annoying

			String text = getString(R.string.dp3t_sdk_service_notification_text);
			builder.setContentTitle(getString(R.string.dp3t_sdk_service_notification_title))
					.setContentText(text)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
					.setPriority(NotificationCompat.PRIORITY_LOW)
					.build();
		}

		return builder.build();
	}

	private String getNotificationErrorText(Collection<TracingStatus.ErrorState> errors) {
		StringBuilder sb = new StringBuilder(getString(R.string.dp3t_sdk_service_notification_errors)).append("\n");
		String sep = "";
		for (TracingStatus.ErrorState error : errors) {
			sb.append(sep).append(getString(error.getErrorString()));
			sep = ", ";
		}
		return sb.toString();
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createNotificationChannel() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelName = getString(R.string.dp3t_sdk_service_notification_channel);
		NotificationChannel channel =
				new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
	}

	private void invalidateForegroundNotification() {
		if (isFinishing) {
			return;
		}

		Notification notification = createForegroundNotification();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void start() {
		Log.e("TracingService","START0");
		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
		handler = new Handler();
	Log.e("TracingService","START");
		invalidateForegroundNotification();
		restartClient();
		restartServer();
	}

	private void ensureStarted() {
		if (handler == null) {
			handler = new Handler();
		}
		invalidateForegroundNotification();
	}

	private void restartClient() {
		//also restart server here to generate a new mac-address so we get rediscovered by apple devices
		startServer();

		BluetoothState bluetoothState = startClient();
		if (bluetoothState == BluetoothState.NOT_SUPPORTED) {
			Logger.e(TAG, "bluetooth not supported");
			return;
		}

		handler.postDelayed(() -> {
			stopScanning();
			scheduleNextClientRestart(this, scanInterval);
		}, scanDuration);
	}

	private void restartServer() {
		BluetoothState bluetoothState = startServer();
		if (bluetoothState == BluetoothState.NOT_SUPPORTED) {
			Log.e(TAG, "bluetooth not supported");
			return;
		}

		scheduleNextServerRestart(this);
	}

	public static void scheduleNextClientRestart(Context context, long scanInterval) {
		long now = System.currentTimeMillis();
		long delay = scanInterval - (now % scanInterval);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, TracingServiceBroadcastReceiver.class);
		intent.setAction(ACTION_RESTART_CLIENT);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + delay, pendingIntent);
	}

	public static void scheduleNextServerRestart(Context context) {
		long nextEpochStart = CryptoModule.getInstance(context).getCurrentEpochStart() + CryptoModule.MILLISECONDS_PER_EPOCH;
		long nextAdvertiseChange = nextEpochStart;
		String calibrationTestDeviceName = AppConfigManager.getInstance(context).getCalibrationTestDeviceName();
		if (calibrationTestDeviceName != null) {
			long now = System.currentTimeMillis();
			nextAdvertiseChange = now - (now % (60 * 1000)) + 60 * 1000;
		}
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, TracingServiceBroadcastReceiver.class);
		intent.setAction(ACTION_RESTART_SERVER);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAdvertiseChange, pendingIntent);
	}

	private void stopForegroundService() {
		isFinishing = true;
		stopClient();
		stopServer();
		BluetoothServiceStatus.resetInstance();
		stopForeground(true);
		wl.release();
		stopSelf();
	}

	private BluetoothState startServer() {
		stopServer();
		if (startAdvertising) {
			bleServer = new BleServer(this);

			Logger.d(TAG, "startAdvertising");
			BluetoothState advertiserState = bleServer.startAdvertising();
			return advertiserState;
		}
		return null;
	}

	private void stopServer() {
		if (bleServer != null) {
			bleServer.stop();
			bleServer = null;
		}
	}

	private BluetoothState startClient() {
		stopClient();
		if (startReceiving) {
			bleClient = new BleClient(this);
			BluetoothState clientState = bleClient.start();
			return clientState;
		}
		return null;
	}

	private void stopScanning() {
		if (bleClient != null) {
			bleClient.stopScan();
		}
	}

	private void stopClient() {
		if (bleClient != null) {
			bleClient.stop();
			bleClient = null;
		}
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Logger.i(TAG, "onDestroy()");

		unregisterReceiver(errorsUpdateReceiver);
		unregisterReceiver(bluetoothStateChangeReceiver);
		unregisterReceiver(locationServiceStateChangeReceiver);

		if (handler != null) {
			handler.removeCallbacksAndMessages(null);
		}
	}

	public void sendcontacts()  {
		SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);

		long ephidsBatch_timestamp = sharedPref.getLong("lastContactsBatch",0);
		Database db= new Database(getApplicationContext());
		List<Handshake> hshakes = db.getHandshakesAfter(ephidsBatch_timestamp);

		List <Contact> contacts = ContactsFactory.mergeHandshakesToContacts(getApplicationContext(),hshakes);
			try {
				postContacts(contacts);
			}
			catch (IOException e){
				e.printStackTrace();
				Log.e("ERROR",e.getLocalizedMessage());
			}
	}


	public void sendephemeral()  {
		Log.i("TRACING_SERVICE","send ephemeral");
		if(((SAPWizardApplication) getApplication()).getAppConfig()!=null) {
			String serviceRoot = ((SAPWizardApplication) getApplication()).getAppConfig().getHost();
			Log.i("TRACING_SERVICE",serviceRoot);
			try{
				SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);

				long ephidsBatch_timestamp = sharedPref.getLong("lastEPHIDBatch",0);
				if(ephidsBatch_timestamp != 0){

					java.util.Date date = new java.util.Date();
					Timestamp timestamp1 = new Timestamp(date.getTime()); //now
					Timestamp timestamp2 = new Timestamp(ephidsBatch_timestamp);

					long diff =	timestamp1.getTime()-timestamp2.getTime();

					if (diff > 24*60*60*1000) {
						//more than 1 day
						postEPHIDS();
					}
				}
				else{
					//first time
					postEPHIDS();
				}



			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	public void postEPHIDS() throws IOException {

		AppConfig appconfig = ((SAPWizardApplication) getApplication()).getAppConfig();
		String clientID = ((OAuthClient)((OAuth)appconfig.getAuth().get(0)).getConfig().getOAuthClients().get(0)).getClientID();
		String authUrl = ((OAuth)appconfig.getAuth().get(0)).getConfig().getAuthorizationEndpoint();
		String tokenUrl = ((OAuth)appconfig.getAuth().get(0)).getConfig().getTokenEndpoint();
		String serviceRoot = ((SAPWizardApplication) getApplication()).getAppConfig().getHost();
		String serviceUrl = ((SAPWizardApplication) getApplication()).getAppConfig().getServiceUrl();


		MediaType MEDIA_TYPE = MediaType.parse("application/json");
		String url = serviceUrl+"xsjsFunctions";

		OkHttpClient client = new OkHttpClient();

		// Server side OAuth2 configuration
		OAuth2Configuration oauthConfiguration = new OAuth2Configuration.Builder(getApplicationContext())
				.clientId(clientID)
				.responseType("code")
				.authUrl(authUrl)
				.tokenUrl(tokenUrl)
				.redirectUrl(serviceUrl)
				.build();

// Storage for OAuth2 tokens
		OAuth2TokenStore tokenStore = new OAuth2TokenInMemoryStore();

// Create an OKHttp intercepter to listen for challenges
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new OAuth2Interceptor(new OAuth2BrowserProcessor(oauthConfiguration), tokenStore))
				.cookieJar(new WebkitCookieJar())
				.build();

// Set global OKHttpClient. The OAuth flow will use this for requesting the token.
		ClientProvider.set(okHttpClient);

/*
         {
         "function": "insertEIDs",
         "payload" : {
         	"EIDs": [
         			{"CreatedAt":"2020-06-11T16:20:50Z", "EID":"80 e8 2b 04 80 c4 d9 ae 1f 3e c5 28 76 bd 42 8b ","DeviceI":"roberto.urban@sap.com"},
         			{"CreatedAt":"2020-06-11T16:20:50Z", "EID":null,"DeviceID":"roberto.urban@sap.com"}
        			 ]
         }
         }
         */



		CryptoModule crypto = CryptoModule.getInstance(this);
		//                                    let data = try? crypto.getCurrentEphID()
		long now = System.currentTimeMillis();
		DayDate currentDay = new DayDate(now);
		List<EphId> todayephsIDs =  crypto.getEphIdsForToday(currentDay); //get today ephsid
		SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
		String username = sharedPref.getString("username","SATWUSER");



		SimpleDateFormat changeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String temp = changeFormat.format(new Date());


		JSONArray EIDS = new JSONArray();

		for(EphId eph : todayephsIDs) {
			JSONObject EID = new JSONObject();
			try {
				EID.put("CreatedAt", temp);
				String ephemeral = bytesToHex(eph.getData());
				EID.put("EID", ephemeral);
				EID.put("DeviceID", username);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			EIDS.put(EID);
		}
		/*JSONObject EID2 = new JSONObject();
		try {
			EID2.put("CreatedAt","2020-09-11T16:20:51Z");
			EID2.put("EID","00 00 81 18 80 c4 d9 ae 1f 3e c5 28 76 bd 42 8b");
			EID2.put("DeviceID","massimo.perego@sap.com");
		} catch(JSONException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		EIDS.put(EID2);*/


		JSONObject EIDSJson = new JSONObject();
		try {
			EIDSJson.put("EIDs", EIDS);

		} catch(JSONException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		JSONObject postdata = new JSONObject();
		try {
			postdata.put("function", "insertEIDs");
			postdata.put("payload", EIDSJson);

		} catch(JSONException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.build();

		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				String mMessage = e.getMessage().toString();
				Log.w("failure Response", mMessage);
				//call.cancel();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {

				String mMessage = response.body().string();
				sharedPref.edit().putLong("lastEPHIDBatch",System.currentTimeMillis()).apply();
				Log.e(TAG, mMessage);
			}
		});
	}



	public void postContacts(List<Contact> contacts) throws IOException {


		Log.i("TRACING_SERVICE","send contacts");
		if(((SAPWizardApplication) getApplication()).getAppConfig()!=null) {
			String serviceRoot = ((SAPWizardApplication) getApplication()).getAppConfig().getHost();
			Log.i("TRACING_SERVICE", serviceRoot);
			try {


				AppConfig appconfig = ((SAPWizardApplication) getApplication()).getAppConfig();
				String clientID = ((OAuthClient) ((OAuth) appconfig.getAuth().get(0)).getConfig().getOAuthClients().get(0)).getClientID();
				String authUrl = ((OAuth) appconfig.getAuth().get(0)).getConfig().getAuthorizationEndpoint();
				String tokenUrl = ((OAuth) appconfig.getAuth().get(0)).getConfig().getTokenEndpoint();

				String serviceUrl = ((SAPWizardApplication) getApplication()).getAppConfig().getServiceUrl();


				MediaType MEDIA_TYPE = MediaType.parse("application/json");
				String url = serviceUrl + "xsjsFunctions";

				OkHttpClient client = new OkHttpClient();

				// Server side OAuth2 configuration
				OAuth2Configuration oauthConfiguration = new OAuth2Configuration.Builder(getApplicationContext())
						.clientId(clientID)
						.responseType("code")
						.authUrl(authUrl)
						.tokenUrl(tokenUrl)
						.redirectUrl(serviceUrl)
						.build();

// Storage for OAuth2 tokens
				OAuth2TokenStore tokenStore = new OAuth2TokenInMemoryStore();

// Create an OKHttp intercepter to listen for challenges
				OkHttpClient okHttpClient = new OkHttpClient.Builder()
						.addInterceptor(new OAuth2Interceptor(new OAuth2BrowserProcessor(oauthConfiguration), tokenStore))
						.cookieJar(new WebkitCookieJar())
						.build();

// Set global OKHttpClient. The OAuth flow will use this for requesting the token.
				ClientProvider.set(okHttpClient);

				CryptoModule crypto = CryptoModule.getInstance(this);
				//                                    let data = try? crypto.getCurrentEphID()
				long now = System.currentTimeMillis();
				DayDate currentDay = new DayDate(now);

				SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);
				SimpleDateFormat changeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


				JSONArray events = new JSONArray();
				EphId myeph = crypto.getCurrentEphId();
				for (Contact c : contacts) {
					JSONObject event = new JSONObject();
					try {
						String date = changeFormat.format(c.getDate());
						event.put("CreatedAt", date);

						String ephemeral = bytesToHex(myeph.getData());
						event.put("SourceEID", ephemeral);

						event.put("TargetEID", bytesToHex(c.getEphId().getData()));
						event.put("Distance", c.getWindowCount());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					events.put(event);
				}


				JSONObject eventsJson = new JSONObject();
				try {
					eventsJson.put("events", events);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				JSONObject postdata = new JSONObject();
				try {
					postdata.put("function", "importEvents");
					postdata.put("payload", eventsJson);

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

				Request request = new Request.Builder()
						.url(url)
						.post(body)
						.header("Accept", "application/json")
						.header("Content-Type", "application/json")
						.build();

				okHttpClient.newCall(request).enqueue(new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						String mMessage = e.getMessage().toString();
						Log.w("failure Response", mMessage);
						//call.cancel();
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {

						String mMessage = response.body().string();
						sharedPref.edit().putLong("lastContactsBatch", System.currentTimeMillis()).apply();
						Log.e(TAG, mMessage);
					}
				});
			} catch (Exception e) {
			}
		}
	}

	private void checkInfected() {

 		Log.i("TRACING_SERVICE","checkInfected");
		if(((SAPWizardApplication) getApplication()).getAppConfig()!=null) {
			String serviceRoot = ((SAPWizardApplication) getApplication()).getAppConfig().getHost();
			Log.i("TRACING_SERVICE", serviceRoot);
			try {

				AppConfig appconfig = ((SAPWizardApplication) getApplication()).getAppConfig();
				String clientID = ((OAuthClient) ((OAuth) appconfig.getAuth().get(0)).getConfig().getOAuthClients().get(0)).getClientID();
				String authUrl = ((OAuth) appconfig.getAuth().get(0)).getConfig().getAuthorizationEndpoint();
				String tokenUrl = ((OAuth) appconfig.getAuth().get(0)).getConfig().getTokenEndpoint();

				String serviceUrl = ((SAPWizardApplication) getApplication()).getAppConfig().getServiceUrl();

				SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);

				long ephidsBatch_timestamp = sharedPref.getLong("lastCheckInf",0);
				boolean callBackend = false;
				if(ephidsBatch_timestamp != 0){

					java.util.Date date = new java.util.Date();
					Timestamp timestamp1 = new Timestamp(date.getTime()); //now
					Timestamp timestamp2 = new Timestamp(ephidsBatch_timestamp);

					long diff =	timestamp1.getTime()-timestamp2.getTime();

					if (diff > 2*60*60*1000) {
						//more than 2 hours
						callBackend = true;
					}
				}
				else {
					callBackend = true;
				}


				if(callBackend) {
					String username = sharedPref.getString("username", "SATWUSER");


					MediaType MEDIA_TYPE = MediaType.parse("application/json");
					String url = serviceUrl + "xsjsFunctions";

					OkHttpClient client = new OkHttpClient();

					// Server side OAuth2 configuration
					OAuth2Configuration oauthConfiguration = new OAuth2Configuration.Builder(getApplicationContext())
							.clientId(clientID)
							.responseType("code")
							.authUrl(authUrl)
							.tokenUrl(tokenUrl)
							.redirectUrl(serviceUrl)
							.build();

// Storage for OAuth2 tokens
					OAuth2TokenStore tokenStore = new OAuth2TokenInMemoryStore();

// Create an OKHttp intercepter to listen for challenges
					OkHttpClient okHttpClient = new OkHttpClient.Builder()
							.addInterceptor(new OAuth2Interceptor(new OAuth2BrowserProcessor(oauthConfiguration), tokenStore))
							.cookieJar(new WebkitCookieJar())
							.build();

// Set global OKHttpClient. The OAuth flow will use this for requesting the token.
					ClientProvider.set(okHttpClient);

					//		let json: [String: Any] = [
//		"function": "checkInfection",
//				"payload": [
//		"deviceId":username
//                ]
//            ]


					JSONObject eventsJson = new JSONObject();
					try {
						eventsJson.put("deviceId", username);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					JSONObject postdata = new JSONObject();
					try {
						postdata.put("function", "checkInfection");
						postdata.put("payload", eventsJson);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

					Request request = new Request.Builder()
							.url(url)
							.post(body)
							.header("Accept", "application/json")
							.header("Content-Type", "application/json")
							.build();

					okHttpClient.newCall(request).enqueue(new Callback() {
						@Override
						public void onFailure(Call call, IOException e) {
							String mMessage = e.getMessage().toString();
							Log.w("failure Response", mMessage);
							//call.cancel();
						}

						@Override
						public void onResponse(Call call, Response response) throws IOException {

							String mMessage = response.body().string();

							try {
								JSONObject obj = new JSONObject(mMessage);
								JSONObject mess = (JSONObject) obj.get("value");
								boolean infected = mess.getBoolean("meetInfected");

								if (infected) {
									//send to UI

									Intent in = new Intent();
									in.putExtra("TYPE", "INFECTED");
									in.setAction("NOW");
									LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);


									Intent notificationIntent = new Intent(getApplicationContext(), TraceReportActivity.class);

									notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
											| Intent.FLAG_ACTIVITY_SINGLE_TOP);

									PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
											notificationIntent, 0);


									NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
											.setSmallIcon(R.mipmap.ic_launcher_round)
											.setContentTitle(String.valueOf(R.string.Attenzione))
											.setContentText(getResources().getString(R.string.Possibile_contagio))
											.setContentIntent(intent)
											.setStyle(new NotificationCompat.BigTextStyle()
													.bigText(getResources().getString(R.string.Possibile_contagio)))
											.setPriority(NotificationCompat.PRIORITY_MAX)
											.setDefaults(NotificationCompat.DEFAULT_ALL);



									// notificationId is a unique int for each notification that you must define
									notificationManager.notify(1001, builder.build());



								} else {
									//TEST
									Intent in = new Intent();
									in.putExtra("TYPE", "NOTINFECTED");
									in.setAction("NOW");
									LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(in);
								}
							} catch (Exception e) {

							}
							sharedPref.edit().putLong("lastCheckInf", System.currentTimeMillis()).apply();
							Log.e(TAG, mMessage);
						}
					});
				}

			}
		catch (Exception e){
				e.printStackTrace();
		}
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
	}
}
