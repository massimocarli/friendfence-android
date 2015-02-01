package uk.co.massimocarli.friendfence.location;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.activity.MainActivity;
import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.service.LocationService;
import uk.co.massimocarli.friendfence.service.ServiceState;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is the Fragment that shows the information related to a specific Path.
 * Created by Massimo Carli on 12/06/14.
 */
public class MyMoveFragment extends Fragment implements FenceSessionListFragment.OnSessionSelectedListener {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = MyMoveFragment.class.getName();

    /**
     * This is the request code we use for the onActivityResult management
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * The smaller interval to get location information
     */
    private final static long FASTEST_INTERVAL = 1000L;

    /**
     * The interval we want to receive location update
     */
    private final static long UPDATE_INTERVAL = 1000L;

    /**
     * The interval we want to receive activity information
     */
    private final static long ACTIVITY_INTERVAL = 5000L;

    /**
     * We don't want update in location if the distance is less that the value in meter
     * of this constants
     */
    private final static int MIN_DISPLACEMENT = 20;

    /**
     * The request code for location
     */
    private final static int UPDATE_LOCATION_REQUEST_CODE = 8888;

    /**
     * The request code for activity recognition
     */
    private final static int UPDATE_ACTIVITY_REQUEST_CODE = 8889;

    /**
     * The Tag we use for the list of sessions
     */
    private final static String SESSION_LIST_TAG = Conf.PKG + ".tag.SESSION_LIST_TAG";

    /**
     * The Tag we use for the list of position
     */
    private final static String POSITION_LIST_TAG = Conf.PKG + ".tag.POSITION_LIST_TAG";

    /**
     * The GoogleApiClient we use to interact with Location Services
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The state of the service
     */
    private ServiceState mServiceState;

    /**
     * The ToggleButton for the service
     */
    private ToggleButton mStartedButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Here we have to initialize the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();
        // We read the state of the service
        mServiceState = ServiceState.get(getActivity());
    }

    /**
     * The implementation of the interface to manage CallBacks from Google Play Services
     */
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle bundle) {
                    Log.d(TAG_LOG, "Connected");
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.d(TAG_LOG, "Disconnected. Please re-connect.");
                }
            };

    /**
     * The implementation of the interface we use to manage errors from Google Play Services
     */
    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    // This is invoked when we have an error in Google Play Services management.
                    // We have to check if there is a standard resolution for this
                    if (connectionResult.hasResolution()) {
                        // In this case we launch the Intent to manage the problem
                        try {
                            connectionResult.startResolutionForResult(getActivity(),
                                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            // In case Play Services cancels the Intent
                            e.printStackTrace();
                        }
                    } else {
                        // In this case there's no standard resolution for the error so we can
                        // only show a Dialog with the error
                        DialogFragment dialogFragment = new DialogFragment();
                        dialogFragment.show(getFragmentManager(), "Error:" + connectionResult.getErrorCode());
                    }
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View myMoveView = inflater.inflate(R.layout.fragment_my_move, null);
        mStartedButton = UI.findViewById(myMoveView, R.id.my_move_start_button);
        mStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here we have to check the state of the button and start or stop the
                // Location tracking
                final boolean serviceState = mStartedButton.isChecked();
                if (serviceState) {
                    // We start the tracking
                    startTracking();
                } else {
                    // We stop the tracking
                    stopTracking();
                }
            }
        });
        return myMoveView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Here we attach the Fragment to the container
        final FenceSessionListFragment fenceSessionListFragment = new FenceSessionListFragment();
        fenceSessionListFragment.setOnSessionSelectedListener(this);
        getChildFragmentManager().beginTransaction().
                replace(R.id.info_container, fenceSessionListFragment, SESSION_LIST_TAG)
                .commit();
        // We change the current Fragment reference
        ((MainActivity) getActivity()).setCurrentFragment(fenceSessionListFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Here we have to connection the client
        mGoogleApiClient.connect();
        // We check the state of the button
        mStartedButton.setChecked(mServiceState.isRunning());
    }

    @Override
    public void onStop() {
        // Here we have to disconnect the client
        mGoogleApiClient.disconnect();
        // And then call super.onStop()
        super.onStop();
    }

    /**
     * This is the method forwarded from the Activity in case we have some error from the
     * Google Play Services
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        if (CONNECTION_FAILURE_RESOLUTION_REQUEST == requestCode && Activity.RESULT_OK == resultCode) {
            // In this case we have to retry the connection
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                Log.d(TAG_LOG, "Error, we try to reconnect!");
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Start the tracking
     */
    private final void startTracking() {
        if (mServiceState.isRunning()) {
            Log.d(TAG_LOG, "Tracking already running!");
            return;
        }
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(FASTEST_INTERVAL)
                .setSmallestDisplacement(MIN_DISPLACEMENT)
                .setInterval(UPDATE_INTERVAL);
        // Here we want to create the new session into the DB and get the related id
        final Uri newSessionUri = FenceDB.FenceSession.createNewSession(getActivity(), Conf.DEFAULT_USER);
        if (newSessionUri != null) {
            // We get the id from the newSessionUri
            final long newSessionId = FenceDB.FenceSession.getSessionId(newSessionUri);
            final Intent locationIntent = LocationService.getLocationIntent(getActivity(), newSessionId);
            final PendingIntent callbackIntent = PendingIntent.getService(getActivity(), UPDATE_LOCATION_REQUEST_CODE,
                    locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, callbackIntent);
            // We manage the ActivityRecognition service
            final Intent activityRecognitionIntent = LocationService.getActivityRecognitionIntent(getActivity());
            final PendingIntent activityIntent = PendingIntent.getService(getActivity(), UPDATE_ACTIVITY_REQUEST_CODE,
                    activityRecognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                    ACTIVITY_INTERVAL, activityIntent);
            // We start the service
            mServiceState.start(newSessionId);
            Log.d(TAG_LOG, "Tracking started!");
        } else {
            // We show an error message
        }
    }


    /**
     * Stop the tracking
     */
    private final void stopTracking() {
        if (!mServiceState.isRunning()) {
            Log.d(TAG_LOG, "Tracking already stopped!");
            return;
        }
        Log.d(TAG_LOG, "Tracking stop!");
        final Intent locationIntent = LocationService.getLocationIntent(getActivity(), 0L);
        final PendingIntent callbackIntent = PendingIntent.getService(getActivity(), UPDATE_LOCATION_REQUEST_CODE,
                locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, callbackIntent);
        // We stop Activity Recognition service
        final Intent activityRecognitionIntent = LocationService.getActivityRecognitionIntent(getActivity());
        final PendingIntent activityIntent = PendingIntent.getService(getActivity(), UPDATE_ACTIVITY_REQUEST_CODE,
                activityRecognitionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, activityIntent);
        // Save data
        long currentSessionId = mServiceState.getSessionId();
        mServiceState.stop();
        FenceDB.FenceSession.setSessionAsClosed(getActivity(), currentSessionId);
    }

    @Override
    public void onSessionSelected(int position, long id) {
        // Here we want to show the information related to the position for  given session
        final FencePositionListFragment positionFragment = FencePositionListFragment.create(id);
        getChildFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.info_container, positionFragment, POSITION_LIST_TAG)
                .commit();
        // We change the current Fragment reference
        ((MainActivity) getActivity()).setCurrentFragment(positionFragment);
    }


}
