package demo.sap.safetyandroid.fcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;
import com.sap.cloud.mobile.foundation.remotenotification.PushCallbackListener;
import com.sap.cloud.mobile.foundation.remotenotification.PushRemoteMessage;

import static demo.sap.safetyandroid.fcm.NotificationUtilities.NOTIFICATION_DATA;
import static demo.sap.safetyandroid.fcm.NotificationUtilities.NOTIFICATION_ID_EXTRA;

public class FCMPushCallbackListenner implements PushCallbackListener {
    @Override
    public void onRecieve(Context context, PushRemoteMessage message) {
            // The following section is a sample for handling push messages. This could be customized
            // according to the needs of the concrete application.
            // ---------------------start of suggested customization--------------------------
        String textMsg = context.getResources().getString(com.sap.cloud.mobile.foundation.R.string.push_text);
        String notificationTitle = context.getResources().getString(com.sap.cloud.mobile.foundation.R.string.push_message);
        String notificationID = message.getNotificationID();

            if (message != null) {
                if(message.getTitle() != null && !message.getTitle().isEmpty()){
                    notificationTitle = message.getTitle();
                }else {
                }
                if(message.getAlert() != null && !message.getAlert().isEmpty()){
                    textMsg = message.getAlert();
                }else {
                }

                if(message.getData().get("alert") != null && !message.getData().isEmpty()){
                    textMsg = message.getData().get("alert");
                }else {
                }
            }
            if (isAppInBackground()) {
                // background
                Intent pushActivityStarter = new Intent(context.getApplicationContext(), PushNotificationActivity.class);
                pushActivityStarter.putExtra(NOTIFICATION_DATA, message);
                pushActivityStarter.putExtra(NOTIFICATION_ID_EXTRA,notificationID);
                NotificationUtilities notUtils = new NotificationUtilities(context.getApplicationContext());
                notUtils.showNotificationMessage(notificationTitle, textMsg, pushActivityStarter);
            } else {
                // foreground
                Activity foregroundActivity = AppLifecycleCallbackHandler.getInstance().getActivity();
                foregroundActivity.getIntent().putExtra(NOTIFICATION_ID_EXTRA, notificationID);
                foregroundActivity.getIntent().putExtra(NOTIFICATION_DATA, message);
                PushNotificationActivity.presentPushMessage(foregroundActivity, notificationID);
            }

            // ------------------end of suggested customization------------------------------
    }
    /**
     * Method checks if the app is in background or not
     */
    public boolean isAppInBackground() {
        Lifecycle.State currentState = ProcessLifecycleOwner.get().getLifecycle().getCurrentState();
        return !currentState.isAtLeast(Lifecycle.State.RESUMED);
    }
}
