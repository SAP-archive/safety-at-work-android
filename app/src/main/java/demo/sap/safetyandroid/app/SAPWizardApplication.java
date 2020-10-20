package demo.sap.safetyandroid.app;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import demo.sap.safetyandroid.app.storage.SecureStorage;
import demo.sap.safetyandroid.fcm.FCMPushCallbackListenner;
import demo.sap.safetyandroid.service.SAPServiceManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;
import com.sap.cloud.mobile.flowv2.model.AppConfig;
import com.sap.cloud.mobile.foundation.networking.HttpException;
import com.sap.cloud.mobile.foundation.remotenotification.PushRemoteMessage;
import com.sap.cloud.mobile.foundation.remotenotification.PushService;
import com.sap.cloud.mobile.foundation.remotenotification.RemoteNotificationClient;
import com.sap.cloud.mobile.foundation.remotenotification.RemoteNotificationParameters;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.database.models.ExposureDay;
import org.dpppt.android.sdk.internal.util.ProcessUtil;
import org.dpppt.android.sdk.util.SignatureUtil;

import java.net.MalformedURLException;
import java.security.PublicKey;

import demo.sap.safetyandroid.repository.RepositoryFactory;
import demo.sap.safetyandroid.util.NotificationUtil;
import okhttp3.CertificatePinner;

import demo.sap.safetyandroid.R;


public class SAPWizardApplication extends Application {

    /**
     * The application configuration information.
     */
    private AppConfig appConfig;
    public boolean isApplicationUnlocked = false;

    public static final String KEY_LOG_SETTING_PREFERENCE = "key.log.settings.preference";

    /**
     * Manages and provides access to OData stores providing data for the app.
     */
    private SAPServiceManager sapServiceManager;


    /**
     * Application-wide RepositoryFactory
     */
    private RepositoryFactory repositoryFactory;

    public PushRemoteMessage notificationMessage;

    /**
     * Returns the application-wide service manager.
     *
     * @return the service manager
     */
    public SAPServiceManager getSAPServiceManager() {
        return sapServiceManager;
    }


    /**
     * Returns the application-wide repository factory
     *
     * @return the repository factory
     */
    public RepositoryFactory getRepositoryFactory() {
        return repositoryFactory;
    }

    /**
     * Clears all user-specific data from the application, essentially resetting
     * it to its initial state.
     */
    public void resetApp() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().clear().apply();
        appConfig = null;
        isApplicationUnlocked = false;
        repositoryFactory.reset();


        resetPushNotifications();
        notificationMessage = null;

    }

    /**
     * Resets the push registrations on both Firebase (Google) and SCP servers.
     */
    private void resetPushNotifications() {

        // reset push token on Firebase and remove push token from CP server
        PushService.unregisterPushSync(new RemoteNotificationClient.CallbackListener() {
            @Override
            public void onSuccess() {


            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(AppLifecycleCallbackHandler.getInstance());

        if (ProcessUtil.isMainProcess(this)) {
            registerReceiver(contactUpdateReceiver, DP3T.getUpdateIntentFilter());

            PublicKey publicKey = SignatureUtil.getPublicKeyFromBase64OrThrow(
                    "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFdkxXZHVFWThqcnA4aWNSNEpVSlJaU0JkOFh2UgphR2FLeUg2VlFnTXV2Zk1JcmxrNk92QmtKeHdhbUdNRnFWYW9zOW11di9rWGhZdjF1a1p1R2RjREJBPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg");
            DP3T.init(this, "org.dpppt.demo", false, publicKey);
            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add("demo.dpppt.org", "sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=")
                    .build();
            DP3T.setCertificatePinner(certificatePinner);
        }
        PushService.setPushCallbackListenner(new FCMPushCallbackListenner());







    }

    private BroadcastReceiver contactUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SecureStorage secureStorage = SecureStorage.getInstance(context);
            TracingStatus status = DP3T.getStatus(context);
            if (status.getInfectionStatus() == InfectionStatus.EXPOSED) {
                ExposureDay exposureDay = null;
                long dateNewest = 0;
                for (ExposureDay day : status.getExposureDays()) {
                    if (day.getExposedDate().getStartOfDayTimestamp() > dateNewest) {
                        exposureDay = day;
                        dateNewest = day.getExposedDate().getStartOfDayTimestamp();
                    }
                }
                if (exposureDay != null && secureStorage.getLastShownContactId() != exposureDay.getId()) {
                    createNewContactNotifaction(context, exposureDay.getId());
                }
            }
        }
    };

    private void createNewContactNotifaction(Context context, int contactId) {
        SecureStorage secureStorage = SecureStorage.getInstance(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createNotificationChannel(context);
        }

        Intent resultIntent = new Intent(context, MainBusinessActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.setAction(MainBusinessActivity.ACTION_GOTO_REPORTS);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification =
                new NotificationCompat.Builder(context, NotificationUtil.NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.push_exposed_title))
                        .setContentText(context.getString(R.string.push_exposed_text))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSmallIcon(R.drawable.ic_begegnungen)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationUtil.NOTIFICATION_ID_CONTACT, notification);

        secureStorage.setHotlineCallPending(true);
        secureStorage.setReportsHeaderAnimationPending(true);
        secureStorage.setLastShownContactId(contactId);
    }

    /**
     * Initialize service manager with application configuration
     *
     * @param appConfig the application configuration
     */
    public void initializeServiceManager(AppConfig appConfig) {
        sapServiceManager = new SAPServiceManager(appConfig);

        repositoryFactory = new RepositoryFactory(sapServiceManager);
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
}
