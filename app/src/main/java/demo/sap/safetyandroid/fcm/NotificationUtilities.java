package demo.sap.safetyandroid.fcm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.sap.cloud.mobile.flowv2.core.DialogHelper;
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler;
import com.sap.cloud.mobile.foundation.remotenotification.PushNotificationActionReceiver;
import com.sap.cloud.mobile.foundation.remotenotification.PushRemoteMessage;
import com.sap.cloud.mobile.foundation.remotenotification.PushService;
import com.sap.cloud.mobile.foundation.remotenotification.RemoteNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import demo.sap.safetyandroid.R;
import demo.sap.safetyandroid.app.SAPWizardApplication;
import kotlin.Unit;


/**
 * This class contains necessary functions required to show the remote notification
 * (with title, message, image and timestamp) in notification tray.
 */
public class NotificationUtilities {

    private static final String PUSH_CHANNEL = "my_push_channel";
    public static final String NOTIFICATION_ID_EXTRA = "NotificationID";
    public static final String NOTIFICATION_DATA = "NotificationData";
    private Context context;
    private String negativeButton;

    /**
     * id to handle the notifications in the notification tray.
     * This is declared static intentionally so that increment of this variable is shared with multiple background notifications.
     * Each time a background notification is received, this value will be incremented to keep a unique id for the intents
     */
    private static int notificationIdStart = 100;

    public NotificationUtilities(Context context) {
        this.context = context;
        negativeButton = context.getApplicationContext().getResources().getString(R.string.cancel);
    }

    public void showNotificationMessage(final String title, final String message, Intent intent) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;

        // status bar icon
        final int smallIcon = R.mipmap.ic_launcher_round;

        // notification bar icon
        final int largeIcon = R.mipmap.ic_launcher_round;
        intent.putExtra(RemoteNotificationConfig.CURRENT_NOTIFICATION_ID, notificationIdStart );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
        final PendingIntent resultPendingIntent =
           PendingIntent.getActivity(
                 context,
                 notificationIdStart,
                 intent,
                 PendingIntent.FLAG_UPDATE_CURRENT
            );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // versions >= 26 we need to have notification channel, as well
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // user-visible name of the channel.
            CharSequence name = "push-channel";
            // user-visible description of the channel.
            String description = "notification channel for push messages";
            // importance
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(PUSH_CHANNEL, name, importance);
            notificationChannel.setDescription(description);
            notificationChannel.enableLights(true);
            // sets the notification light color for notifications posted to this
            // channel, if the device supports this feature
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, PUSH_CHANNEL);

        showSmallNotification(notificationBuilder, smallIcon, largeIcon, title, message, resultPendingIntent);
    }


    private void showSmallNotification(NotificationCompat.Builder notificationBuilder, int smallIcon, int largeIcon, String title, String message, PendingIntent resultPendingIntent) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.addLine(message);

        Intent intentCancel = new Intent(context, PushNotificationActionReceiver.class);
        intentCancel.setAction(negativeButton);
        intentCancel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentCancel.putExtra(RemoteNotificationConfig.CURRENT_NOTIFICATION_ID,notificationIdStart );
        //This Intent will be called when Cancel button from notification will be clicked by user.
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, notificationIdStart, intentCancel, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = notificationBuilder
                .setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon))
                .setTicker(title)
                .setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(inboxStyle)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentText(message)
                .addAction(R.drawable.ic_close_black_24dp, negativeButton, pendingIntentCancel)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationIdStart++, notification);
    }

    // show notification message
    public static void showNotificationMessage(PushRemoteMessage notificationMessage) {
        if (notificationMessage != null) {
            PushRemoteMessage message = notificationMessage;
            Activity foregroundActivity = AppLifecycleCallbackHandler.getInstance().getActivity();
            Intent intent = new Intent(foregroundActivity, PushNotificationActivity.class);
            intent.putExtra(NOTIFICATION_DATA, message);
            intent.putExtra(NOTIFICATION_ID_EXTRA, message.getNotificationID());
            foregroundActivity.startActivity(intent);
        }
    }

public static void showMessageDialog(FragmentActivity activity, PushRemoteMessage message,
                                     MessageDialogDismissedListener listener) {
        String textMsg = activity.getResources().getString(R.string.push_text);
        if (message.getAlert() != null) {
            textMsg = message.getAlert();
        }
        String notificationTitle = activity.getResources().getString(R.string.push_message);
        JSONObject jsonPayload = null;
        if (message != null) {
            jsonPayload = new JSONObject(message.getData());
            try {
                notificationTitle = jsonPayload.getString("title");
            } catch (JSONException e) {
            }
            try {
                textMsg = jsonPayload.getString("alert");
            } catch (JSONException e) {
            }
        }
        SAPWizardApplication application = (SAPWizardApplication) activity.getApplication();
        new DialogHelper(activity, R.style.OnboardingDefaultTheme_Dialog_Alert).
                showDialogWithCancelAction(
                        activity.getSupportFragmentManager(),
                        textMsg,
                        (() -> {
                            application.notificationMessage = null;
                            if (listener != null) {
                                listener.onDismiss();
                            }
                            return Unit.INSTANCE;
                        }),
                        null,
                        notificationTitle,
                        null,
                        (() -> {
                            if (message.getNotificationID() != null) {
                                PushService.setPushMessageStatus(message.getNotificationID(),
                                        PushService.NotificationStatus.CONSUMED);
                            }
                            application.notificationMessage = null;
                            if (listener != null) {
                                listener.onDismiss();
                            }
                            return Unit.INSTANCE;
                        })
                );
    }

    // Clears notification tray messages
    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public interface MessageDialogDismissedListener {
        void onDismiss();
    }
}
