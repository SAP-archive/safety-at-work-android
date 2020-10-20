package demo.sap.safetyandroid.fcm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.sap.cloud.mobile.flowv2.model.FlowConstants;
import com.sap.cloud.mobile.flowv2.model.FlowType;
import com.sap.cloud.mobile.foundation.remotenotification.PushRemoteMessage;

import demo.sap.safetyandroid.app.MainBusinessActivity;
import demo.sap.safetyandroid.app.SAPWizardApplication;
import demo.sap.safetyandroid.app.WelcomeActivity;

import static demo.sap.safetyandroid.fcm.NotificationUtilities.NOTIFICATION_DATA;
import static demo.sap.safetyandroid.fcm.NotificationUtilities.NOTIFICATION_ID_EXTRA;

/**
 * Activity which serves as a transparent background for push message dialogs. Using an activity helps
 * to show the dialog on top of the current activity stack.
 */
public class PushNotificationActivity extends FragmentActivity {

    private String notificationId;
    private PushRemoteMessage notificationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationId = getIntent().getStringExtra(NOTIFICATION_ID_EXTRA);
        notificationMessage = getIntent().getParcelableExtra(NOTIFICATION_DATA);

        SAPWizardApplication application = (SAPWizardApplication) getApplication();
        if(isTaskRoot()) {
            // if the stack were empty, then open the WelcomeActivity, which is the entry point
            // for the whole application
            application.notificationMessage = notificationMessage;
            WelcomeActivity.startFlow(this, FlowType.RESTORE);
        } else {
            if(!application.isApplicationUnlocked) {
                application.notificationMessage = notificationMessage;
                NotificationUtilities.showMessageDialog(this, notificationMessage, () -> {
                    finish();
                });

                return;
            }
            NotificationUtilities.showMessageDialog(this, notificationMessage, () -> {
                finish();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FlowConstants.FLOW_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, MainBusinessActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    /**
     * Utility method for presenting the messages on an alert dialog.
     *
     * THIS CAN BE A PLACE TO CUSTOMIZE PUSH MESSAGE HANDLING.
     *
     * @param activity activity (Activity) for the alert dialog
     * @param notificationId push notification id, unique id of the push message
     */
    public static void presentPushMessage(Activity activity, String notificationId) {
        PushRemoteMessage notificationMessage = activity.getIntent().getParcelableExtra(NOTIFICATION_DATA);
        Intent intent = new Intent(activity.getApplicationContext(), PushNotificationActivity.class);
        intent.putExtra(NOTIFICATION_ID_EXTRA, notificationId);
        intent.putExtra(NOTIFICATION_DATA, notificationMessage);
        activity.startActivity(intent);
    }
}
