package uk.co.massimocarli.friendfence.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.location.Location;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderApi;

import java.util.ArrayList;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.content.FenceDB;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class LocationService extends IntentService {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = LocationService.class.getName();

    /**
     * The number of ContentProviderOperation we use to update the data in the ContentProvider
     */
    private static final int CONTENT_PROVIDER_OP_SIZE = 2;

    /**
     * The value for SessionId in the case the related extra is not present
     */
    private static final long NO_SESSION_ID = -1L;

    /**
     * This is the Action we use to send a Location information to this service
     */
    private static final String SAVE_LOCATION_ACTION = Conf.PKG + ".action.SAVE_LOCATION_ACTION";

    /**
     * This is the extra we use to send the information related to the session id
     */
    private static final String EXTRA_SESSION_ID = Conf.PKG + ".extra.EXTRA_SESSION_ID";

    /**
     * The object that contains the service for the state
     */
    private ServiceState mServiceState;

    /**
     * This is the static factory method that creates the Intent to launch for this Service
     *
     * @param context   The Context
     * @param sessionId The sessionId
     * @return The Intent to start for sending information to the LocationService
     */
    public static Intent getLocationIntent(final Context context, final long sessionId) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(SAVE_LOCATION_ACTION);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        return intent;
    }

    /**
     * This is the static factory method that creates the Intent to launch the service for
     * ActivityRecognition
     *
     * @param context The Context
     * @return The Intent to start for sending information to the LocationService about ActivityRecognition
     */
    public static Intent getActivityRecognitionIntent(final Context context) {
        Intent intent = new Intent(context, LocationService.class);
        return intent;
    }

    public LocationService() {
        super("LocationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // We initialize the ServiceState
        mServiceState = ServiceState.get(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (SAVE_LOCATION_ACTION.equals(action)) {
                final long sessionId = intent.getLongExtra(EXTRA_SESSION_ID, NO_SESSION_ID);
                if (sessionId == NO_SESSION_ID) {
                    Log.w(TAG_LOG, "No SessionId in extra!");
                    return;
                }
                // We manage the Location info
                final Location location = intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
                saveLocationData(sessionId, location);
            } else if (ActivityRecognitionResult.hasResult(intent)) {
                // In this case we're receiving an information for Activity recognition
                final ActivityRecognitionResult activityResult = ActivityRecognitionResult.extractResult(intent);
                final DetectedActivity mostProbableActivity = activityResult.getMostProbableActivity();
                final int activityType = mostProbableActivity.getType();
                Log.w(TAG_LOG, "Activity detected! " + activityType);
                // We save the type into our SessionState object
                mServiceState.updateActivityType(activityType);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void saveLocationData(final long sessionId, final Location location) {
        Log.d(TAG_LOG, "Session: " + sessionId + " location: " + location);
        // If we have an old location we calculate the distance between the two
        final Pair<Float, Float> distances = mServiceState.addLocation(location);
        // We get the location information and create the ContentValue to insert
        // into the DB
        final ContentValues locationValues = new ContentValues();
        locationValues.put(FenceDB.FencePosition.POSITION_TIME, location.getTime());
        locationValues.put(FenceDB.FencePosition.ALTITUDE, location.getAltitude());
        locationValues.put(FenceDB.FencePosition.LATITUDE, location.getLatitude());
        locationValues.put(FenceDB.FencePosition.LONGITUDE, location.getLongitude());
        locationValues.put(FenceDB.FencePosition.DISTANCE, distances.second);
        locationValues.put(FenceDB.FencePosition.ACTIVITY, mServiceState.getActivityType());
        final Uri positionUri = FenceDB.FencePosition.getPositionUriForSession(sessionId);
        final ArrayList<ContentProviderOperation> updateOps =
                new ArrayList<ContentProviderOperation>(CONTENT_PROVIDER_OP_SIZE);
        updateOps.add(ContentProviderOperation.newInsert(positionUri).withValues(locationValues).build());
        // The update for the session data
        final Uri sessionUri = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(sessionId));
        final ContentValues sessionValues = new ContentValues();
        sessionValues.put(FenceDB.FenceSession.TOTAL_DISTANCE, distances.second);
        updateOps.add(ContentProviderOperation.newUpdate(sessionUri).withValues(sessionValues).build());
        // We insert this data into the ContentProvider
        try {
            getContentResolver().applyBatch(FenceDB.AUTHORITY, updateOps);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG_LOG, "RemoteException inserting data into ContentProvider", e);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            Log.e(TAG_LOG, "OperationApplicationException inserting data into ContentProvider", e);
        }
    }

}
