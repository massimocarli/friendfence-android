package uk.co.massimocarli.friendfence.gcm;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.model.UserModel;
import uk.co.massimocarli.friendfence.notification.FenceNotificationHelper;
import uk.co.massimocarli.friendfence.util.AppInfoUtil;

/**
 * An {@link android.app.IntentService} subclass to manage operations using the Google Cloud Messaging
 * <p/>
 * helper methods.
 */
public class GcmService extends IntentService {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = GcmService.class.getName();

    /**
     * This is the action we use to manage RegistrationId
     */
    private static final String GET_REGISTRATION_ID_ACTION = Conf.PKG + ".gcm.action.GET_REGISTRATION_ID_ACTION";

    /**
     * This is the action we use to parse the incoming message
     */
    private static final String MANAGE_MESSAGE_ACTION = Conf.PKG + ".gcm.action.MANAGE_MESSAGE_ACTION";

    /**
     * The extra for the username
     */
    private static final String USERNAME_EXTRA = Conf.PKG + ".gcm.extra.USERNAME";

    /**
     * The extra for the sender of the message
     */
    private static final String SENDER_EXTRA = "senderUser";

    /**
     * The extra for the message
     */
    private static final String MESSAGE_EXTRA = "msgBody";

    /**
     * The MediaType fro JSON data
     */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * The object we use to manage GoogleCloudMessaging
     */
    private GoogleCloudMessaging mGcm;

    /**
     * The client we use to access rest Services
     */
    private OkHttpClient httpClient;

    /**
     * Starts this service to implement the management of the RegistrationId
     *
     * @param context  The Context
     * @param username The username of the user to register
     */
    public static void manageRegistrationId(final Context context, final String username) {
        Intent intent = new Intent(context, GcmService.class);
        intent.putExtra(USERNAME_EXTRA, username);
        intent.setAction(GET_REGISTRATION_ID_ACTION);
        context.startService(intent);
    }

    /**
     * Utility method that creates the Intent we use to launch this service to manage push
     * message data
     *
     * @param context The Context
     * @param intent  The Intent to manage for the
     * @return The Intent to launch
     */
    public static void convertIntent(final Context context, final Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmService.class.getName());
        intent.setComponent(comp);
        //intent.setAction(MANAGE_MESSAGE_ACTION);
    }

    public GcmService() {
        super("GcmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        httpClient = new OkHttpClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // We initialize the  GoogleCloudMessaging object
        if (mGcm == null) {
            mGcm = GoogleCloudMessaging.getInstance(this);
        }
        if (intent != null) {
            final String action = intent.getAction();
            if (GET_REGISTRATION_ID_ACTION.equals(action)) {
                final String username = intent.getStringExtra(USERNAME_EXTRA);
                handleRegistrationId(username);
            } else {
                handleReceivedMessage(intent);
            }
        }
    }


    /**
     * This is the operation that manages the RegistrationId
     *
     * @param username The username of the user to register
     */
    private void handleRegistrationId(final String username) {
        // We get the UserModel object
        final UserModel userModel = UserModel.get(this);
        // We check if the registrationId is already present
        final int currentAppVersion = AppInfoUtil.getAppVersion(this);
        String registrationId = userModel.getRegistrationId(this, currentAppVersion);
        if (TextUtils.isEmpty(registrationId)) {
            // In this case the registration Id is not present so we request it to the
            // GCM server
            try {
                // We get the registration Id from server
                registrationId = mGcm.register(Conf.GCM_SENDER_ID);
                // We send it to the server
                sendRegistrationIdToServer(registrationId, username);
                // If everything is ok we save the reg Id into the profile
                userModel.setRegistrationId(registrationId, currentAppVersion);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG_LOG, "Registration Id already present!");
        }
    }

    private void sendRegistrationIdToServer(final String registrationId, final String username) throws IOException {
        // The url for the service
        final String registerUrl = getString(R.string.registration_id_url);
        // We create the Json object to send
        final String jsonInput = GcmRequest.RegistrationBuilder.create()
                .withRegistrationId(registrationId)
                .withUsername(username)
                .withDeviceId(AppInfoUtil.getDeviceId(this))
                .getJsonAsString();
        // We create the output
        RequestBody body = RequestBody.create(JSON, jsonInput);
        Request request = new Request.Builder()
                .url(registerUrl)
                .post(body)
                .build();
        Response response = httpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            IOException ioe = new IOException("Registration Error");
            Log.e(TAG_LOG, "Registration Error", ioe);
            throw ioe;
        }
    }

    /**
     * Utility method that manage the received message.
     *
     * @param intent The received Intent
     */
    private void handleReceivedMessage(final Intent intent) {
        // We get the extra from the received message
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {
            // We read the type of the received message
            String messageType = mGcm.getMessageType(intent);
            // We only manage messages with information
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // The message has some information
                final String sender = intent.getStringExtra(SENDER_EXTRA);
                final String message = intent.getStringExtra(MESSAGE_EXTRA);
                // We use these information to show a notification
                FenceNotificationHelper.get(this).showChatNotification(this, sender, message);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                // Here we have an error
                Log.e(TAG_LOG, "Send message error");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                // The message has been deleted
                Log.e(TAG_LOG, "GCM message deleted");
            }
        }
        // In Any case we have to release the lock
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}
