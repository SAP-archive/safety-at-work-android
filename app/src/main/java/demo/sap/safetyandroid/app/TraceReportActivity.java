package demo.sap.safetyandroid.app;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.TracingService;
import org.dpppt.android.sdk.internal.crypto.ContactsFactory;
import org.dpppt.android.sdk.internal.crypto.CryptoModule;
import org.dpppt.android.sdk.internal.crypto.EphId;
import org.dpppt.android.sdk.internal.database.Database;
import org.dpppt.android.sdk.internal.database.models.Contact;
import org.dpppt.android.sdk.internal.database.models.Handshake;
import org.dpppt.android.sdk.internal.util.DayDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import demo.sap.safetyandroid.BuildConfig;
import demo.sap.safetyandroid.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TraceReportActivity extends AppCompatActivity {


    private static final String TAG = "TraceReportActivity";
    private static final String NOTIFICATION_CHANNEL_ID = "dp3t_tracing_service";

    private NotificationManagerCompat notificationManager;

    private boolean sync1 = false;
    private boolean sync2 = false;
    private boolean sync3 = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_report);




        Switch onOffSwitch = (Switch)  findViewById(R.id.switch1);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", ""+isChecked);
                if(isChecked){
                    //immagine e colore del testo blu
                    ImageView shieldImg = (ImageView)  findViewById(R.id.shield);
                    shieldImg.setImageResource(R.drawable.icons8_security_checked);

                    TextView traceStatusTextview = (TextView)  findViewById(R.id.traceStatus);
                    traceStatusTextview.setTextColor(getResources().getColor(R.color.status_blue));
                    traceStatusTextview.setText( getResources().getString(R.string.tracciamentoattivo).toUpperCase());


                    DP3T.start(getApplication());
                }
                else{
                    //immagine e colore del testo rosso
                    ImageView shieldImg = (ImageView)  findViewById(R.id.shield);
                    shieldImg.setImageResource(R.drawable.icons8_warning_shield);

                    TextView traceStatusTextview = (TextView)  findViewById(R.id.traceStatus);
                    traceStatusTextview.setTextColor(getResources().getColor(R.color.red));
                    traceStatusTextview.setText(getResources().getString(R.string.tracciamentodisattivo).toUpperCase());

                    DP3T.stop(getApplication());
                }
            }

        });


        ImageButton sendButton = (ImageButton)  findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("tracereport","clicked");




// To dismiss the dialog
                //progress.dismiss();

               RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

               relProgr.setVisibility(View.VISIBLE);


                try {
                    sync1 = true;
                    postEPHIDS();
                }
                catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG,"error on postEPHIDS");
                }

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);

                long ephidsBatch_timestamp = sharedPref.getLong("lastContactsBatch",0);
                Database db= new Database(getApplicationContext());
                List<Handshake> hshakes = db.getHandshakesAfter(ephidsBatch_timestamp);

                List <Contact> contacts = ContactsFactory.mergeHandshakesToContacts(getApplicationContext(),hshakes);

                try {
                    sync2 = true;
                    postContacts(contacts);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,"error on postcontacts");
                }

                sync3 = true;
                checkInfected();




            }
        });


        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

       TextView versionLabel = findViewById(R.id.versioneLabel);

       versionLabel.setText(versionName + " Build ("+versionCode +")  ");

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(TraceReportActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("NOW"));
        LocalBroadcastManager.getInstance(TraceReportActivity.this).registerReceiver(BTbroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        //everytime open app
        checkInfected();
    }




    private BroadcastReceiver BTbroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    // Bluetooth was disconnected
                    Switch onOffSwitch = (Switch)  findViewById(R.id.switch1);
                    onOffSwitch.setChecked(false);
                }
                else{
                    Switch onOffSwitch = (Switch)  findViewById(R.id.switch1);
                    onOffSwitch.setChecked(true);
                }
            }


        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("TYPE");  //get the type of message from MyGcmListenerService 1 - lock or 0 -Unlock

            Log.d("trace", "Status: " + type);
            if (type.equalsIgnoreCase("INFECTED")) // 1 == lock
            {

                TextView title = (TextView)  findViewById(R.id.textView7);
                title.setText(getResources().getString(R.string.Possibile_contagio));
                title.setTextColor(getResources().getColor(R.color.white));


                TextView subtitle = (TextView)  findViewById(R.id.textView8);
                subtitle.setText(getResources().getString(R.string.Sospetto));
                subtitle.setTextColor(getResources().getColor(R.color.white));

                Button callButton = (Button)  findViewById(R.id.CallHRbutton);
                callButton.setVisibility(View.VISIBLE);
                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_DIAL);

                        String tel = getMetaData(getApplicationContext(),"telephone");
                        String p = tel;
                        i.setData(Uri.parse(p));
                        startActivity(i);
                    }
                });

                CardView layout = (CardView)  findViewById(R.id.cardMiddle);
                layout.setBackgroundColor(getResources().getColor(R.color.orangeCard));


            } else {

                TextView title = (TextView)  findViewById(R.id.textView7);
                title.setText(getResources().getString(R.string.NessunaSegnalazione));
                title.setTextColor(getResources().getColor(R.color.black));


                TextView subtitle = (TextView)  findViewById(R.id.textView8);
                subtitle.setText(getResources().getString(R.string.NoSegnalazioni));
                subtitle.setTextColor(getResources().getColor(R.color.black));

                Button callButton = (Button)  findViewById(R.id.CallHRbutton);
                callButton.setVisibility(View.GONE);

                CardView layout = (CardView)  findViewById(R.id.cardMiddle);
                layout.setBackgroundColor(getResources().getColor(R.color.white));
            }



        }
    };


    public void postEPHIDS() throws IOException {

        AppConfig appconfig = ((SAPWizardApplication) getApplication()).getAppConfig();
        String clientID = ((OAuthClient)((OAuth)appconfig.getAuth().get(0)).getConfig().getOAuthClients().get(0)).getClientID();
        String authUrl = ((OAuth)appconfig.getAuth().get(0)).getConfig().getAuthorizationEndpoint();
        String tokenUrl = ((OAuth)appconfig.getAuth().get(0)).getConfig().getTokenEndpoint();
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




        CryptoModule crypto = CryptoModule.getInstance(this);

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

                sync1 = false;

                if(!sync1 && !sync2 && !sync3)
                {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

                            relProgr.setVisibility(View.GONE);

                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();
                sharedPref.edit().putLong("lastEPHIDBatch",System.currentTimeMillis()).apply();
                Log.e(TAG, mMessage);
                sync1 = false;
                if(!sync1 && !sync2 && !sync3)
                {


                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

                            relProgr.setVisibility(View.GONE);

                        }
                    });


                }
            }
        });
    }



    public void postContacts(List<Contact> contacts) throws IOException {


        Log.i(TAG,"send contacts");
        if(((SAPWizardApplication) getApplication()).getAppConfig()!=null) {
            String serviceRoot = ((SAPWizardApplication) getApplication()).getAppConfig().getHost();
            Log.i(TAG, serviceRoot);
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
                        sync2=false;
                        if(!sync1 && !sync2 && !sync3)
                        {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

                                    relProgr.setVisibility(View.GONE);

                                }
                            });
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        String mMessage = response.body().string();
                        sharedPref.edit().putLong("lastContactsBatch", System.currentTimeMillis()).apply();
                        Log.e(TAG, mMessage);
                        sync2=false;
                        if(!sync1 && !sync2 && !sync3)
                        {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

                                    relProgr.setVisibility(View.GONE);

                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    private void checkInfected() {

        Log.i(TAG,"checkInfected");
        if(((SAPWizardApplication) getApplication()).getAppConfig()!=null) {
            String serviceRoot = ((SAPWizardApplication) getApplication()).getAppConfig().getHost();
            Log.i(TAG, serviceRoot);
            try {

                AppConfig appconfig = ((SAPWizardApplication) getApplication()).getAppConfig();
                String clientID = ((OAuthClient) ((OAuth) appconfig.getAuth().get(0)).getConfig().getOAuthClients().get(0)).getClientID();
                String authUrl = ((OAuth) appconfig.getAuth().get(0)).getConfig().getAuthorizationEndpoint();
                String tokenUrl = ((OAuth) appconfig.getAuth().get(0)).getConfig().getTokenEndpoint();

                String serviceUrl = ((SAPWizardApplication) getApplication()).getAppConfig().getServiceUrl();

                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(String.valueOf(R.string.preference_file_key), Context.MODE_PRIVATE);


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
                            Log.i("failure Response", mMessage);
                            //call.cancel();
                            sync3=false;
                            if(!sync1 && !sync2 && !sync3)
                            {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

                                        relProgr.setVisibility(View.GONE);

                                    }
                                });
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            String mMessage = response.body().string();
                            Log.i("ok Response", mMessage);
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


                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),
                                            NOTIFICATION_CHANNEL_ID)
                                            .setSmallIcon(R.drawable.logo2)
                                            .setContentTitle("Attenzione")
                                            .setContentText("Probabile contagio")
                                            .setContentIntent(intent)
                                            .setStyle(new NotificationCompat.BigTextStyle()
                                                    .bigText("Probabile contagio"))
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
                            sync3=false;
                            if(!sync1 && !sync2 && !sync3)
                            {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        RelativeLayout relProgr = findViewById(R.id.relativelayout_progress);

                                        relProgr.setVisibility(View.GONE);

                                    }
                                });
                            }
                        }
                    });


            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public static String getMetaData(Context context, String name) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to load meta-data: " + e.getMessage());
        }
        return null;
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
