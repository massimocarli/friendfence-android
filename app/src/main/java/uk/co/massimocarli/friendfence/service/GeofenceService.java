package uk.co.massimocarli.friendfence.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;

import uk.co.massimocarli.friendfence.notification.FenceNotificationHelper;

/**
 * This is the class that describes the Service that will receive data about GeoFence
 */
public class GeofenceService extends IntentService {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = GeofenceService.class.getName();

    /**
     * The Default Constructor
     */
    public GeofenceService() {
        super("GeofenceService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // We get the GeofencingEvent from the received intent
            final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            // We check if we have errors
            if (!geofencingEvent.hasError()) {
                // If everything is ok we can intercept which is the triggered event
                FenceNotificationHelper.get(this).showGeofenceNotification(this, geofencingEvent);
            } else {
                // In this case we got an error so the only thing we can do is to Log it
                int errorCode = geofencingEvent.getErrorCode();
                Log.e(TAG_LOG, "Error receving Geofence notification. Error code: " + errorCode);
            }
        }
    }

}
