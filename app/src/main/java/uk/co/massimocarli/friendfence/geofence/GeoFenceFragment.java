package uk.co.massimocarli.friendfence.geofence;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.content.cursor.CursorResolver;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;
import uk.co.massimocarli.friendfence.service.GeofenceService;
import uk.co.massimocarli.friendfence.settings.SettingsActivity;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is the Fragment to manage Geofences
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class GeoFenceFragment extends Fragment {

    /**
     * The Tag for this log
     */
    private static final String TAG_LOG = GeoFenceFragment.class.getName();

    /**
     * The starting fields
     */
    private static final String[] FROM = {FenceDB.Geofence.FENCE_ID, FenceDB.Geofence.LATITUDE,
            FenceDB.Geofence.LONGITUDE};

    /**
     * The ids for the view related to the fields
     */
    private static final int[] TO = {R.id.geofence_id, R.id.geofence_latitude,
            R.id.geofence_longitude};

    /**
     * This is the request code we use for the onActivityResult management
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * The Identifier for the loader
     */
    private final static int GEOFENCE_LOADER_ID = 48;

    /**
     * The key for the map type in the SharedPreferences
     */
    private final static String MAP_TYPE_PREFS_KEY = "map_type";

    /**
     * The Default map type as String
     */
    private final static String DEFAULT_MAP_TYPE = "1";

    /**
     * The Default zoom
     */
    private final static int DEFAULT_ZOOM = 13;

    /**
     * The GoogleApiClient we use to interact with Location Services
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The Fragment for the Map
     */
    private SupportMapFragment mMapFragment;

    /**
     * The handle to manage Google Map
     */
    private GoogleMap mGoogleMap;

    /**
     * The CurrentLocation
     */
    private Location mCurrentLocation;

    /**
     * The ListView for the Geofence
     */
    private ListView mGeofenceListView;

    /**
     * The Adapter for the Geofence data
     */
    private SimpleCursorAdapter mCursorAdapter;

    /**
     * The Set of circles
     */
    private Set<Circle> mCircles = new HashSet<Circle>();

    /**
     * The implementation of the interface to manage CallBacks from Google Play Services
     */
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG_LOG, "Connected");
            // We update the data into the View with the first location
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
            showLocationInMap(mCurrentLocation);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG_LOG, "Disconnected. Please re-connect.");
        }


    };

    /**
     * The implementation of the interface we use to manage errors from Google Play Services
     */
    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
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


    /**
     * The Loader we use to intercept variations into the ContentProvider
     */
    private final LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), FenceDB.Geofence.CONTENT_URI, null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            // We empty all the circles
            for (Circle circle : mCircles) {
                circle.remove();
            }
            mCircles.clear();
            mCursorAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            mCursorAdapter.swapCursor(null);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Here we have to connection the client
        mGoogleApiClient.connect();
        // We update the type of the map
        updateMapType();
    }

    @Override
    public void onResume() {
        super.onResume();
        // We start the Loader
        getLoaderManager().restartLoader(GEOFENCE_LOADER_ID, new Bundle(), mLoaderCallback);
    }

    @Override
    public void onStop() {
        // Here we have to disconnect the client
        mGoogleApiClient.disconnect();
        // And then call super.onStop()
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterForContextMenu(mGeofenceListView);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_geofence, container, false);
        // The Fragment for the Map
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setMyLocationEnabled(true);
                // We update the type of the map
                updateMapType();
                // Show the current location
                showLocationInMap(mCurrentLocation);
                // We register the listener for the long click
                mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        startNewGeofence(latLng);
                    }
                });
            }
        });
        getChildFragmentManager().beginTransaction().replace(R.id.geofence_map_anchor, mMapFragment).commit();
        // The UI items
        mGeofenceListView = UI.findViewById(view, R.id.geofence_list);
        mGeofenceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // In this case we center the map on the selected circle
                final FenceCursorFactory.GeofenceCursorData mCursor =
                        CursorResolver.CURSOR_RESOLVER.extractGeofenceCursor(mCursorAdapter.getCursor());
                mCursor.moveToPosition(position);
                final GeofenceData geofenceData = mCursor.getGeofence();
                final LatLng geofencePosition = new LatLng(geofenceData.getLatitude(), geofenceData.getLongitude());
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(geofencePosition, DEFAULT_ZOOM);
                // Apply the CameraUpdate to the Map
                mGoogleMap.animateCamera(cameraUpdate);
            }
        });
        // The Adapter
        mCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_geofence_item, null, FROM, TO, 0);
        mCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                final FenceCursorFactory.GeofenceCursorData mCursor = CursorResolver.CURSOR_RESOLVER.extractGeofenceCursor(cursor);
                final GeofenceData geoFenceData = mCursor.getGeofence();
                if (view.getId() == R.id.geofence_id) {
                    // We add the item to the Map
                    final LatLng position = new LatLng(geoFenceData.getLatitude(), geoFenceData.getLongitude());
                    final Circle circle = drawCircle(position, geoFenceData.getRadius());
                    mCircles.add(circle);
                }
                return false;
            }
        });
        mGeofenceListView.setAdapter(mCursorAdapter);
        registerForContextMenu(mGeofenceListView);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fence, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final MenuInflater menuInflater = new MenuInflater(getActivity());
        menuInflater.inflate(R.menu.geofence_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.geofence_context_delete_menu_item) {
            // We delete the related geofence
            requestForDelete(info.id);
        } else if (item.getItemId() == R.id.mock_location_action) {
            startMockLocations(info.id);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (R.id.action_settings == itemId) {
            // Here we show the Settings Activity
            final Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Here we update the type of the map reading the information from the Settings
     */
    private void updateMapType() {
        if (mGoogleMap == null) {
            // if not present we do nothing
            return;
        }
        // We read the value from the SharedPreferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String mapTypeStr = sharedPref.getString(MAP_TYPE_PREFS_KEY, DEFAULT_MAP_TYPE);
        final int mapType = Integer.parseInt(mapTypeStr);
        final int oldMapType = mGoogleMap.getMapType();
        if (oldMapType != mapType) {
            mGoogleMap.setMapType(mapType);
        }
    }

    /**
     * This is the method we use to center the map to a specific location
     *
     * @param location The location to show in the Map
     */
    private void showLocationInMap(final Location location) {
        if (location == null || mGoogleMap == null) {
            // Location not available
            return;
        }
        // Create an object LatLng that encapsulate location
        final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Create a CameraUpdate for the location centering on the Map
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM);
        // Apply the CameraUpdate to the Map
        mGoogleMap.moveCamera(cameraUpdate);
    }


    /**
     * This method prepare the creazione of the Geofence
     *
     * @param position The position of the possible Geofence
     */
    private void startNewGeofence(final LatLng position) {
        // We create the GeofenceData
        final String geofenceId = "Geofence - " + System.currentTimeMillis();
        final GeofenceData geoFenceData = GeofenceData.create(geofenceId, position.latitude, position.longitude);
        // We create a GeofenceRequest
        final GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(geoFenceData.buildGeofence()).build();
        final Intent intent = new Intent(getActivity(), GeofenceService.class);
        final PendingIntent geoPendingIntent = PendingIntent.getService(getActivity(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        final PendingResult<Status> result =
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, geoPendingIntent);
        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                // We check the status to know if the geofence was successfully added or not
                if (status.isSuccess()) {
                    // In this case we save the data into the DB
                    FenceDB.Geofence.save(getActivity(), geoFenceData);
                } else {
                    // In this case it failed so we do nothing
                    Toast.makeText(getActivity(), R.string.geofence_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * This method delete the Geofence with the given _id into the ContentProvider
     *
     * @param itemId The id of the Geofence to delete
     */
    private void requestForDelete(final long itemId) {
        // We get the data related to the given Geofence
        final Uri geofenceUri = Uri.withAppendedPath(FenceDB.Geofence.CONTENT_URI, String.valueOf(itemId));
        final Cursor cursor = getActivity().getContentResolver().query(geofenceUri, null, null, null, null);
        final FenceCursorFactory.GeofenceCursorData geoFenceCursor =
                CursorResolver.CURSOR_RESOLVER.extractGeofenceCursor(cursor);
        if (geoFenceCursor.moveToNext()) {
            final GeofenceData geofenceData = geoFenceCursor.getGeofence();
            final String idToDelete = geofenceData.getRequestId();
            final ArrayList<String> idsToDelete = new ArrayList<String>(1);
            idsToDelete.add(idToDelete);
            final PendingResult<Status> result =
                    LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, idsToDelete);
            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    // We check the status to know if the geofence was successfully added or not
                    if (status.isSuccess()) {
                        // In this case we delete the data into the DB
                        getActivity().getContentResolver().delete(geofenceUri, null, null);
                        Toast.makeText(getActivity(), R.string.geofence_deleted, Toast.LENGTH_SHORT).show();
                    } else {
                        // In this case it failed so we do nothing
                        Toast.makeText(getActivity(), R.string.geofence_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        cursor.close();
    }

    /**
     * Utility method that shows a rectangle
     *
     * @param center The center of the rectangle
     * @param radius The radius in meter
     */
    private Circle drawCircle(final LatLng center, final double radius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius(radius);
        return mGoogleMap.addCircle(circleOptions);
    }

    /**
     * The Name for the Mock Provider
     */
    private static final String MOCK_PROVIDER = "flp";

    /**
     * The distance from the destination as lat/lon delta
     */
    private static final double DISTANCE = 0.006;

    /**
     * The Number of step
     */
    private static final double STEP_NUMBER = 100;

    /**
     * The accuracy for the Location information
     */
    private static final float ACCURACY = 2.0f;

    /**
     * The interval between mock locations
     */
    private static final long DELAY = 200L;

    /**
     * This method sends a set of Mock Location events every 10 seconds until the given destination
     *
     * @param geofenceId The id of the Geofence to test
     */
    private void startMockLocations(final long geofenceId) {
        // We get the data related to the given Geofence
        final Uri geofenceUri = Uri.withAppendedPath(FenceDB.Geofence.CONTENT_URI, String.valueOf(geofenceId));
        final Cursor cursor = getActivity().getContentResolver().query(geofenceUri, null, null, null, null);
        final FenceCursorFactory.GeofenceCursorData geoFenceCursor =
                CursorResolver.CURSOR_RESOLVER.extractGeofenceCursor(cursor);
        GeofenceData geofenceData = null;
        if (geoFenceCursor.moveToNext()) {
            geofenceData = geoFenceCursor.getGeofence();
        }
        cursor.close();
        if (geofenceData != null) {
            // We start to simulate the item
            final GeofenceData finalData = geofenceData;
            // We calculate the starting point
            final double step = DISTANCE / STEP_NUMBER;
            new Thread() {

                double currentLatitude = finalData.getLatitude() - DISTANCE;
                double currentLongitude = finalData.getLongitude() - DISTANCE;

                {
                    // Create an object LatLng that encapsulate location
                    final LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);
                    // Create a CameraUpdate for the location centering on the Map
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM);
                    // Apply the CameraUpdate to the Map
                    mGoogleMap.moveCamera(cameraUpdate);

                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void run() {
                    LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
                    for (int i = 0; i < STEP_NUMBER; i++) {
                        currentLatitude += step;
                        currentLongitude += step;
                        final Location testLocation = new Location(MOCK_PROVIDER);
                        testLocation.setLatitude(currentLatitude);
                        testLocation.setLongitude(currentLongitude);
                        testLocation.setAccuracy(ACCURACY);
                        testLocation.setTime(System.currentTimeMillis());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            testLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                        }
                        final PendingResult<Status> mockLocResponse =
                                LocationServices.FusedLocationApi
                                        .setMockLocation(mGoogleApiClient, testLocation);
                        mockLocResponse.setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LatLng newPos = new LatLng(currentLatitude, currentLongitude);
                                            mGoogleMap.animateCamera(CameraUpdateFactory
                                                    .newLatLngZoom(newPos, DEFAULT_ZOOM));
                                            Log.w(TAG_LOG, "Mock Location success");
                                        }
                                    });
                                } else {
                                    Log.w(TAG_LOG, "Error setting Mock Location " + status.getStatusMessage());
                                }
                            }
                        });
                        try {
                            Thread.sleep(DELAY);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, false);
                }
            }.start();
        }
    }

}
