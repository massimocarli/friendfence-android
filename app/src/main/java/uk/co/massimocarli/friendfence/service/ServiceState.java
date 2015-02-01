package uk.co.massimocarli.friendfence.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.location.DetectedActivity;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.notification.FenceNotificationHelper;

/**
 * Created by Massimo Carli on 01/09/14.
 */
public final class ServiceState {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = LocationService.class.getName();

    /**
     * The value for a no session id
     */
    private static final long NO_SESSION_ID = -1L;

    /**
     * The name for the SharedPrefs
     */
    private static final String SHARED_NAME = Conf.PKG + ".prefs.LOCATION_SERVICE_STATE";

    /**
     * The key for the state
     */
    private static final String CURRENT_SESSION_ID = Conf.PKG + ".key.CURRENT_SESSION_ID";

    /**
     * The Last latitude info
     */
    private static final String LAST_LATITUDE = Conf.PKG + ".key.LAST_LATITUDE";

    /**
     * The last longitude info
     */
    private static final String LAST_LONGITUDE = Conf.PKG + ".key.LAST_LONGITUDE";

    /**
     * The current distance
     */
    private static final String CURRENT_DISTANCE = Conf.PKG + ".key.CURRENT_DISTANCE";

    /**
     * The current activity recognition state
     */
    private static final String ACTIVITY_STATE = Conf.PKG + ".key.ACTIVITY_STATE";

    /**
     * The SingletonInstance
     */
    private static ServiceState sInstance;

    /**
     * The total distance
     */
    private float mCurrentDistance;

    /**
     * The current Activity type
     */
    private int mCurrentActivityType = DetectedActivity.UNKNOWN;

    /**
     * The Preference reference
     */
    private final SharedPreferences mPrefs;

    /**
     * The Helper to manage notification
     */
    private FenceNotificationHelper mNotificationHelper;

    /**
     * Creates and initialize the ServiceState
     *
     * @param context The Context
     */
    private ServiceState(final Context context) {
        mPrefs = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        // NotificationHelper initialisation
        mNotificationHelper = FenceNotificationHelper.get(context);
    }

    /**
     * Static Factory method for the ServiceState
     *
     * @param context The Context
     * @return The ServiceState Singleton Instance
     */
    public synchronized static final ServiceState get(final Context context) {
        if (sInstance == null) {
            sInstance = new ServiceState(context);
        }
        return sInstance;
    }

    /**
     * Update the service state to started saving the related SessionId
     *
     * @param currentSessionId The sessionId of the current service
     */
    public synchronized void start(final long currentSessionId) {
        // we start the session and reset all the information for the
        // location and distance
        mPrefs.edit()
                .putLong(CURRENT_SESSION_ID, currentSessionId)
                .putInt(ACTIVITY_STATE, DetectedActivity.UNKNOWN)
                .commit();
        Log.d(TAG_LOG, "Start session " + currentSessionId);
    }

    /**
     * Update the current service to stopped
     */
    public synchronized void stop() {
        Log.d(TAG_LOG, "Stop session " + getSessionId());
        mPrefs.edit()
                .remove(CURRENT_SESSION_ID)
                .remove(LAST_LATITUDE)
                .remove(LAST_LONGITUDE)
                .remove(ACTIVITY_STATE)
                .putFloat(CURRENT_DISTANCE, 0.0f)
                .commit();
        // We remove the notification
        mNotificationHelper.dismissDistanceNotification();
    }

    /**
     * @return True if the service is running ald false otherwise
     */
    public synchronized boolean isRunning() {
        return getSessionId() != NO_SESSION_ID;
    }

    /**
     * @return The current running serviceId if any or NO_SESSION_ID (-1) if not
     */
    public synchronized long getSessionId() {
        return mPrefs.getLong(CURRENT_SESSION_ID, NO_SESSION_ID);
    }

    /**
     * We use this to update the Activity type
     *
     * @param activityState The current Activity type
     */
    public synchronized void updateActivityType(final int activityState) {
        mCurrentActivityType = activityState;
        mPrefs.edit()
                .putInt(ACTIVITY_STATE, mCurrentActivityType)
                .commit();
    }


    /**
     * @return The current ActivityType
     */
    public synchronized int getActivityType() {
        if (mCurrentActivityType == DetectedActivity.UNKNOWN) {
            mCurrentActivityType = mPrefs.getInt(ACTIVITY_STATE, DetectedActivity.UNKNOWN);
        }
        return mCurrentActivityType;
    }

    /**
     * Add the current distance
     *
     * @param newLocation The new Location
     * @return The Pair with the last distance and the total
     */
    public synchronized Pair<Float, Float> addLocation(final Location newLocation) {
        final float distance;
        final double newLatitude = newLocation.getLatitude();
        final double newLongitude = newLocation.getLongitude();
        if (mPrefs.contains(LAST_LATITUDE)) {
            // In this case we have a previous Location
            final float previousLatitude = mPrefs.getFloat(LAST_LATITUDE, 0.0f);
            final float previousLongitude = mPrefs.getFloat(LAST_LONGITUDE, 0.0f);
            // We calculate the distance
            final float[] distances = new float[1];
            Location.distanceBetween(previousLatitude, previousLongitude,
                    newLatitude, newLongitude, distances);
            distance = distances[0];
        } else {
            // We don't have a previous location
            distance = 0.0f;
        }
        mCurrentDistance = mPrefs.getFloat(CURRENT_DISTANCE, 0.0f);
        mCurrentDistance = mCurrentDistance + distance;
        mPrefs.edit()
                .putFloat(LAST_LATITUDE, (float) newLatitude)
                .putFloat(LAST_LONGITUDE, (float) newLongitude)
                .putFloat(CURRENT_DISTANCE, mCurrentDistance)
                .commit();
        Log.d(TAG_LOG, "Distance to position [" + newLatitude + "," + newLongitude + "] is "
                + distance + " total:" + mCurrentDistance);
        // We show the notification
        mNotificationHelper.showDistanceNotification(getSessionId(), mCurrentDistance);
        // We return the distances
        return new Pair<Float, Float>(distance, mCurrentDistance);
    }


}
