package uk.co.massimocarli.friendfence.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * This is the BroadcastReceiver that manages the incoming push notification messages
 * <p/>
 * Created by Massimo Carli on 11/10/14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = GcmBroadcastReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // We have to do like this because we have to use the same extras and send them
        // to the service
        GcmService.convertIntent(context, intent);
        startWakefulService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }
}
