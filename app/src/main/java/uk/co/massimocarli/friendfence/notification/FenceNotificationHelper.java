package uk.co.massimocarli.friendfence.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.activity.MainActivity;
import uk.co.massimocarli.friendfence.gcm.SimpleChatActivity;
import uk.co.massimocarli.friendfence.util.DistanceUtil;

/**
 * This is the class we can use to manage notifications
 * <p/>
 * Created by Massimo Carli on 01/09/14.
 */
public final class FenceNotificationHelper {

    /**
     * The tag for the Log
     */
    private static final String TAG_LOG = FenceNotificationHelper.class.getName();

    /**
     * The Notification Id for the distance
     */
    private static final int DISTANCE_NOTIFICATION_ID = 37;

    /**
     * The Request code we use to launch the app
     */
    private static final int LAUNCH_APP_REQUEST_CODE = 38;

    /**
     * The Notification Id for the chat message
     */
    private static final int CHAT_NOTIFICATION_ID = 39;

    /**
     * The Request code we use to launch the app
     */
    private static final int GO_TO_MAIN_REQUEST_CODE = 39;

    /**
     * The Request code we use to launch the app
     */
    private static final int GO_TO_GEOFENCE_REQUEST_CODE = 40;

    /**
     * The name for the extra for the reply
     */
    public static final String REPLY_TEXT_EXTRA_NAME = Conf.PKG + ".extra.REPLY_TEXT_EXTRA_NAME";

    /**
     * The name for the notification group
     */
    public static final String GROUP_NAME = "NOTIFICATION_GROUP";

    /**
     * The Singleton Instance
     */
    private static FenceNotificationHelper sInstance;

    /**
     * The Notification Manager
     */
    private NotificationManager mNotificationManager;


    /**
     * The ApplicationContext we'll retain
     */
    private Context mContext;

    /**
     * Private constructor with Context
     *
     * @param context The Context. Only ApplicationContext will be retained
     */
    private FenceNotificationHelper(final Context context) {
        mContext = context.getApplicationContext();
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * The Static Factory Method for the FenceNotificationHelper
     *
     * @param context The Context. Only ApplicationContext will be retained
     * @return The FenceNotificationHelper Singleton Instance
     */
    public synchronized static FenceNotificationHelper get(final Context context) {
        if (sInstance == null) {
            sInstance = new FenceNotificationHelper(context);
        }
        return sInstance;
    }

    /**
     * Show the notification with the distance
     *
     * @param distance The distance to show
     */
    public void showDistanceNotification(final long sessionId, final float distance) {
        // The PendingIntent to launch the application
        final Intent launchAppIntent = new Intent(mContext, MainActivity.class);
        launchAppIntent.putExtra(MainActivity.FIRST_FRAGMENT_INDEX, 1);
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // The distance text
        final String distanceText = DistanceUtil.formatDistance(mContext, distance);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentText(mContext.getString(R.string.notification_distance_format, sessionId, distanceText))
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .setContentIntent(launchPendingIntent)
                .setAutoCancel(false);
        mNotificationManager.notify(DISTANCE_NOTIFICATION_ID, builder.build());
    }

    /**
     * This is an utility method that receives an Intent with the information related to a
     * Geofence and send a notification for this
     *
     * @param context         The Context.
     * @param geofencingEvent Event about Geofencing
     */
    public void showGeofenceNotification(final Context context, final GeofencingEvent geofencingEvent) {
        final int transitionType = geofencingEvent.getGeofenceTransition();
        // We get the location that triggered the event
        final Location triggeredLocation = geofencingEvent.getTriggeringLocation();
        // We get the Geofence that generates the event
        final List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
        final String geoFenceId;
        if (triggeredGeofences != null && triggeredGeofences.size() > 0) {
            geoFenceId = triggeredGeofences.get(0).getRequestId();
        } else {
            geoFenceId = mContext.getString(R.string.geofence_unknown_geofence);
        }
        // We build the title
        final String title;
        if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            title = mContext.getString(R.string.geofence_exit_event, geoFenceId);
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            title = mContext.getString(R.string.geofence_enter_event, geoFenceId);
        } else {
            title = mContext.getString(R.string.geofence_unsupported_event);
        }
        final String message = mContext.getString(R.string.info_window_location,
                triggeredLocation.getLatitude(), triggeredLocation.getLongitude());
        // We build the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);
        mNotificationManager.notify(DISTANCE_NOTIFICATION_ID, builder.build());
    }


    /**
     * This method cancels the current notification
     */
    public void dismissDistanceNotification() {
        // We cancel the notification
        mNotificationManager.cancel(DISTANCE_NOTIFICATION_ID);
    }

    /**
     * This method manage the notification to launch when we receive a message from a user
     *
     * @param context The Context
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showChatNotification(final Context context, final String sender, final String message) {
        // Uncomment this and comment all the other code to use notification with actions
        //showChatNotificationWithActions(context, sender, message);
        // Uncomment this and comment all the other code to use Big Text notification
        //showBigTextChatNotification(context, sender, message);
        // Uncomment this and comment all the other code to use Big Image notification
        //showBigPictureChatNotification(context, sender, message);
        // Uncomment this and comment all the other code to use Wear specific notification with actions
        //showChatNotificationWithWearAction(context, sender, message);
        // Uncomment this and comment all the other code to use Wear specific with pages
        //showAdditionalPagesOnWear(context, sender, message);
        // Uncomment this and comment all the other code to use Wear specific without icon
        //showNoIconNotification(context, sender, message);
        // Uncomment this and comment all the other code to use reply with RemoteInput
        //showChatNotificationWithReply(context, sender, message);
        // Uncomment this and comment all the other code to use groups for notification
        showGroupedChatNotificationWithReply(context, sender, message);

        /*
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .setAutoCancel(true);
        mNotificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
        */
    }


    public void showChatNotificationWithActions(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create Action for the App
        final Intent actionIntent1 = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent1 = PendingIntent.getActivity(context, GO_TO_MAIN_REQUEST_CODE,
                actionIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the Action for the Geofence
        final Intent actionIntent2 = new Intent(context, MainActivity.class);
        actionIntent2.putExtra(MainActivity.FIRST_FRAGMENT_INDEX, 2);
        final PendingIntent pendingIntent2 = PendingIntent.getActivity(context, GO_TO_GEOFENCE_REQUEST_CODE,
                actionIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .addAction(R.drawable.ic_launcher, "Main", pendingIntent1)
                .addAction(R.drawable.ic_launcher, "Geofence", pendingIntent2)
                .setAutoCancel(true);
        mNotificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * Example of BigTextStyle notification
     *
     * @param context The Context. Only ApplicationContext will be retained
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showBigTextChatNotification(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the long text for the BitTextStype
        final String bigText = context.getString(R.string.notification_chat_big_text, sender, message);
        final String bigContentTitle = context.getString(R.string.notification_chat_big_title, sender);
        final String bigSummaryText = context.getString(R.string.notification_chat_big_summary, sender);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText)
                        .setBigContentTitle(bigContentTitle).setSummaryText(bigSummaryText))
                .setAutoCancel(true);
        mNotificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * Example of BigPictureStyle notification
     *
     * @param context The Context. Only ApplicationContext will be retained
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showBigPictureChatNotification(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the long text for the BitTextStype
        final Bitmap bigImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        final String bigContentTitle = context.getString(R.string.notification_chat_big_title, sender);
        final String bigSummaryText = context.getString(R.string.notification_chat_big_summary, sender);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(bigContentTitle)
                        .bigPicture(bigImage)
                        .setSummaryText(bigSummaryText))
                .setAutoCancel(true);
        mNotificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * This method shows how to show different Actions on Wear device and smartphone
     *
     * @param context The Context
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showChatNotificationWithWearAction(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create Action for the App
        final Intent actionIntent1 = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent1 = PendingIntent.getActivity(context, GO_TO_MAIN_REQUEST_CODE,
                actionIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the Action for the Geofence
        final Intent actionIntent2 = new Intent(context, MainActivity.class);
        actionIntent2.putExtra(MainActivity.FIRST_FRAGMENT_INDEX, 2);
        final PendingIntent pendingIntent2 = PendingIntent.getActivity(context, GO_TO_GEOFENCE_REQUEST_CODE,
                actionIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the wearAction object for the Wear actions
        NotificationCompat.Action wearAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                        "WearMain", pendingIntent1)
                        .build();
        final NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .addAction(wearAction);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .addAction(R.drawable.ic_launcher, "Main", pendingIntent1)
                .addAction(R.drawable.ic_launcher, "Geofence", pendingIntent2)
                .extend(extender)
                .setAutoCancel(true);
        // We define a new NotificationManager
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);
        notificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * This method shows additional pages on the Wear device
     *
     * @param context The Context
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showAdditionalPagesOnWear(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // Notification for Page1
        NotificationCompat.Builder page1Notification = new NotificationCompat.Builder(mContext)
                .setContentText("Page 1")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);
        // Notification for Page2
        NotificationCompat.Builder page2Notification = new NotificationCompat.Builder(mContext)
                .setContentText("Page 2")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);
        // Notification for Page3
        NotificationCompat.Builder page3Notification = new NotificationCompat.Builder(mContext)
                .setContentText("Page 3")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the Extender for the Wear device
        final NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .addPage(page1Notification.build())
                        .addPage(page2Notification.build())
                        .addPage(page3Notification.build());
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .extend(extender)
                .setAutoCancel(true);
        // We define a new NotificationManager
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);
        notificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * This method shows a simple notification with no icon
     *
     * @param context The Context
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showNoIconNotification(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the Extender for the Wear device
        final NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(true);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .extend(extender)
                .setAutoCancel(true);
        // We define a new NotificationManager
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);
        notificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * This is a method that send a notification to the Wear Device with an action that
     * permits the user to reply
     *
     * @param context The Context
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showChatNotificationWithReply(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the RemoteInput for reply
        RemoteInput remoteInput = new RemoteInput.Builder(REPLY_TEXT_EXTRA_NAME)
                .setLabel(context.getString(R.string.notification_reply_label, sender))
                .setChoices(context.getResources().getStringArray(R.array.possible_replies))
                .build();
        // We create the Intent for the reply
        Intent replyIntent = new Intent(context, SimpleChatActivity.class);
        replyIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(context, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the reply Action
        final String replyActionName = context.getString(R.string.notification_reply_action);
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                        replyActionName, replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        // We create the Extender for the Wear device
        final NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .addAction(replyAction);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .extend(extender)
                .setAutoCancel(true);
        // We define a new NotificationManager
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);
        notificationManager.notify(CHAT_NOTIFICATION_ID, builder.build());
    }

    /**
     * Example of notification for multiple message of the same type
     *
     * @param context The Context
     * @param sender  The sender of the message
     * @param message The message to show
     */
    public void showGroupedChatNotificationWithReply(final Context context, final String sender, final String message) {
        // The PendingIntent to launch the application
        final Intent chatIntent = new Intent(mContext, SimpleChatActivity.class);
        chatIntent.putExtra(SimpleChatActivity.SENDER_EXTRA, sender);
        chatIntent.putExtra(SimpleChatActivity.MESSAGE_EXTRA, message);
        // We create another test Action
        final PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, LAUNCH_APP_REQUEST_CODE,
                chatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the RemoteInput for reply
        RemoteInput remoteInput = new RemoteInput.Builder(REPLY_TEXT_EXTRA_NAME)
                .setLabel(context.getString(R.string.notification_reply_label, sender))
                .setChoices(context.getResources().getStringArray(R.array.possible_replies))
                .build();
        // We create the Intent for the reply
        Intent replyIntent = new Intent(context, SimpleChatActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(context, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        // We create the reply Action
        final String replyActionName = context.getString(R.string.notification_reply_action);
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                        replyActionName, replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        // We create the Extender for the Wear device
        final NotificationCompat.WearableExtender extender =
                new NotificationCompat.WearableExtender()
                        .addAction(replyAction);
        // We create the Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.notification_chat_message, sender))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(launchPendingIntent)
                .extend(extender)
                .setGroup(GROUP_NAME)
                .setAutoCancel(true);
        // We define a new NotificationManager
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(mContext);
        notificationManager.notify(CHAT_NOTIFICATION_ID + counter++, builder.build());
    }

    /**
     * Just to create different ids
     */
    private static int counter;

}
