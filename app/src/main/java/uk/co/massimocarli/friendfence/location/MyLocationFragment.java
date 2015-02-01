package uk.co.massimocarli.friendfence.location;


import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.map.GeocoderInfoWindowAdapter;
import uk.co.massimocarli.friendfence.settings.SettingsActivity;
import uk.co.massimocarli.friendfence.util.ProgressDialogFragment;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyLocationFragment extends Fragment {

    /**
     * The Tag for this log
     */
    private static final String TAG_LOG = MyLocationFragment.class.getName();

    /**
     * This is the request code we use for the onActivityResult management
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * The duration of the request. After this time the request is cancelled
     */
    private final static long LOCATION_DURATION_TIME = 5000L;

    /**
     * The Max number of addresses we want from the Geocoder
     */
    private final static int MAX_GEOCODE_RESULTS = 1;

    /**
     * The default value for the Zoom
     */
    private final static float DEFAULT_ZOOM = 6.0f;

    /**
     * The default value for animation duration (in millisec)
     */
    private final static int MAP_ANIMATION_DURATION = 400;

    /**
     * The key for the map type in the SharedPreferences
     */
    private final static String MAP_TYPE_PREFS_KEY = "map_type";

    /**
     * The Default map type as String
     */
    private final static String DEFAULT_MAP_TYPE = "1";

    /**
     * The default padding
     */
    private final static int DEFAULT_PADDING = 50;

    // value taken from google-play-services.jar
    private static final String MAP_OPTIONS = "MapOptions";

    /**
     * The GoogleApiClient we use to interact with Location Services
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The last location
     */
    private volatile Location mLastLocation;

    /**
     * The TextView for the location time
     */
    private TextView mLocationTimeView;

    /**
     * The TextView for Latitude
     */
    private TextView mLatitudeView;

    /**
     * The TextView for Longitude
     */
    private TextView mLongitudeView;

    /**
     * The TextView for Altitude
     */
    private TextView mAltitudeView;

    /**
     * The Fragment for the Map
     */
    private SupportMapFragment mMapFragment;

    /**
     * The handle to manage Google Map
     */
    private GoogleMap mGoogleMap;

    /**
     * The reference to the InfoWindowAdapter
     */
    private GeocoderInfoWindowAdapter mInfoWindowAdapter;

    /**
     * The implementation of the interface to manage CallBacks from Google Play Services
     */
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG_LOG, "Connected");
            // We update the data into the View with the first location
            final Location firstLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            displayLocation(firstLocation);
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


    public MyLocationFragment() {
        // Required empty public constructor
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_my_location, container, false);
        // We get the reference to the TextViews
        mLatitudeView = UI.findViewById(view, R.id.my_location_latitude);
        mLongitudeView = UI.findViewById(view, R.id.my_location_longitude);
        mAltitudeView = UI.findViewById(view, R.id.my_location_altitude);
        mLocationTimeView = UI.findViewById(view, R.id.my_location_time);
        // We get the reference of the Map
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                // Enable my location
                mGoogleMap.setMyLocationEnabled(true);
                // We update the type of the map
                updateMapType();
                // We create the specific InfoWindowAdapter
                mInfoWindowAdapter = new GeocoderInfoWindowAdapter(getActivity());
                mGoogleMap.setInfoWindowAdapter(mInfoWindowAdapter);
            }
        });
        getChildFragmentManager().beginTransaction().replace(R.id.my_location_map_anchor, mMapFragment).commit();
        return view;
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
    public void onStop() {
        // Here we have to disconnect the client
        mGoogleApiClient.disconnect();
        // And then call super.onStop()
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (R.id.menu_my_location_update == itemId) {
            // Here we have to update the location
            updateLocation();
        } else if (R.id.menu_my_location_geocode == itemId) {
            // Here we want to geocode the last given location
            //geoCodeLocation(mLastLocation);
            // We use this action to show the location in the map
            showLocationInMap(mLastLocation);
            // The code to show the centered cities
            //showCitiesInMap();
            // Tilt and bearing information
            //showLocationInMapWithTiltAndBearing(mLastLocation);
        } else if (R.id.action_settings == itemId) {
            // Here we show the Settings Activity
            final Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * We update the location and show the data into the UI items
     */
    private void updateLocation() {
        // Here we create the LocationRequest to send to our client to get
        // an updated position
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setExpirationDuration(LOCATION_DURATION_TIME);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // We update the location with the
                displayLocation(location);
            }
        });
    }

    /**
     * This is the method we use to center the map to a specific location
     *
     * @param location The location to show in the Map
     */
    private void showLocationInMap_1(final Location location) {
        if (location == null || mGoogleMap == null) {
            // Location not available
            return;
        }
        // Create an object LatLng that encapsulate location
        final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Create a CameraUpdate for the location centering on the Map
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        // Apply the CameraUpdate to the Map
        mGoogleMap.moveCamera(cameraUpdate);
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
        mGoogleMap.animateCamera(cameraUpdate, MAP_ANIMATION_DURATION, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Log.d(TAG_LOG, "Animation completed successfully!");
            }

            @Override
            public void onCancel() {
                Log.d(TAG_LOG, "Animation Cancelled!");
            }
        });
        // We show a simple Marker with title and snippet
        //showSimplePin(currentLatLng);
        // Usage of Pin with anchor point
        //showPinZeroAnchorPoint(currentLatLng);
        // Usage of a colored pin
        showBluePin(currentLatLng);
    }

    /**
     * Test method that show the Map to contain a set of Cities
     */
    private void showCitiesInMap() {
        if (mGoogleMap == null) {
            // Location not available
            return;
        }
        // We define the cities we want to represent
        final LatLng rome = new LatLng(41.872389, 12.48018);
        final LatLng naples = new LatLng(40.851775, 14.268124);
        final LatLng milan = new LatLng(45.465422, 9.185924);
        final LatLng trieste = new LatLng(45.649526, 13.776818);
        final LatLng palermo = new LatLng(38.115688, 13.361267);
        // We create the LatLngBounds object using chaining
        final LatLngBounds bounds = new LatLngBounds(rome, rome)
                .including(naples)
                .including(milan)
                .including(trieste)
                .including(palermo);
        // Create a CameraUpdate
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, DEFAULT_PADDING);
        // Apply the CameraUpdate to the Map
        mGoogleMap.moveCamera(cameraUpdate);
    }

    /**
     * This method shows the Location using specific values of bearing and tilt
     *
     * @param location The location to show in the Map
     */
    private void showLocationInMapWithTiltAndBearing(final Location location) {
        if (location == null || mGoogleMap == null) {
            // Location not available
            return;
        }
        // Create an object LatLng that encapsulate location
        final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        // Create an object of type CameraPosition
        final CameraPosition cameraPosition = CameraPosition.builder().target(currentLatLng)
                .zoom(DEFAULT_ZOOM)
                .bearing(90)
                .tilt(30)
                .build();
        // Create a CameraUpdate for the location centering on the Map
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        // Apply the CameraUpdate to the Map
        mGoogleMap.moveCamera(cameraUpdate);
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
     * We use this to show a simple draggable Marker with title and snippet
     *
     * @param pinPosition The position of the pin
     */
    private void showSimplePin(final LatLng pinPosition) {
        // We show a simple Marker with title and snippet
        final MarkerOptions markerOptions = new MarkerOptions().position(pinPosition)
                .title("I'm here!")
                .draggable(true)
                .snippet("Here we'll show the address after Geocoding");
        final Marker simpleMarker = mGoogleMap.addMarker(markerOptions);
    }

    /**
     * We use this to show a simple draggable Marker with title and snippet and
     * 0.0f, 0.0f anchor point
     *
     * @param pinPosition The position of the pin
     */
    private void showPinZeroAnchorPoint(final LatLng pinPosition) {
        // We show a simple Marker with title and snippet
        final MarkerOptions markerOptions = new MarkerOptions().position(pinPosition)
                .title("I'm here!")
                .draggable(true)
                        //.anchor(0.0f, 0.0f)
                .anchor(1.0f, 1.0f)
                .snippet("Here we'll show the address after Geocoding");
        final Marker simpleMarker = mGoogleMap.addMarker(markerOptions);
    }


    /**
     * We use this to show a simple blue draggable Marker with title and snippet
     *
     * @param pinPosition The position of the pin
     */
    private void showBluePin(final LatLng pinPosition) {
        // We show a simple Marker with title and snippet
        final MarkerOptions markerOptions = new MarkerOptions().position(pinPosition)
                .title("I'm here!")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .snippet("Here we'll show the address after Geocoding");
        final Marker simpleMarker = mGoogleMap.addMarker(markerOptions);
    }

    /**
     * Method that encapsulate the logic to show Location info
     *
     * @param currentLocation The Location to show if any
     */
    private void displayLocation(final Location currentLocation) {
        // We update the currentLocation
        mLastLocation = currentLocation;
        // We get the info to print if any
        long locationTime = System.currentTimeMillis();
        // We show coordinates
        if (currentLocation != null) {
            // The time of the request
            locationTime = currentLocation.getTime();
            // Latitude in degree
            final String latAsString = Location.convert(currentLocation.getLatitude(), Location.FORMAT_DEGREES);
            mLatitudeView.setText(latAsString);
            // Longitude in degree
            final String lonAsString = Location.convert(currentLocation.getLongitude(), Location.FORMAT_DEGREES);
            mLongitudeView.setText(lonAsString);
            // Altitude
            if (currentLocation.hasAltitude()) {
                final String altInMeter = getActivity().getResources().getString(R.string.my_location_altitude_format,
                        currentLocation.getAltitude());
                mAltitudeView.setText(altInMeter);
            } else {
                mAltitudeView.setText(R.string.no_info);
            }
        } else {
            Toast.makeText(getActivity(), R.string.my_location_not_available, Toast.LENGTH_SHORT).show();
            mLatitudeView.setText(R.string.no_info);
            mLongitudeView.setText(R.string.no_info);
            mAltitudeView.setText(R.string.no_info);
        }
        // In any case we show the time that is the location one if available or the current
        // time if not
        final SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.my_location_time_format));
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(locationTime);
        final String timeAsString = sdf.format(calendar.getTime());
        mLocationTimeView.setText(timeAsString);
    }

    /**
     * Utility method that geoCode a given Location and show the info into the screen
     *
     * @param location The Location to GeoCode
     */
    private void geoCodeLocation(final Location location) {
        if (null != location) {
            // We check if the Geocoder is present
            if (Geocoder.isPresent()) {
                // Here we have to execute the geocode in asynchronously
                final GeoCoderAsyncTask geoCoderAsyncTask = new GeoCoderAsyncTask(this, MAX_GEOCODE_RESULTS);
                geoCoderAsyncTask.execute(location);
            } else {
                // In this case the Geocoder service is not available
                Toast.makeText(getActivity(), R.string.my_location_geocoder_not_available,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // In this case there's no location to geocode
            Log.w(TAG_LOG, "No Location to geocode!");
            Toast.makeText(getActivity(), R.string.my_location_not_available,
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This is the AsyncTask that uses Geocoder to geocode an Address in Background
     */
    private static class GeoCoderAsyncTask extends AsyncTask<Location, Void, List<Address>> {

        /**
         * The Tag for the ProgressDialog
         */
        private static final String PROGRESS_TAG = "PROGRESS_TAG";

        /**
         * The Reference to the MyLocationFragment
         */
        private final WeakReference<MyLocationFragment> mFragmentRef;

        /**
         * The Max number of result in this AsyncTask
         */
        private final int mMaxResult;

        /**
         * The ProgressDialog for the GeoCoding
         */
        private ProgressDialogFragment mProgressDialog;

        /**
         * Constructor for the GeoCoderAsyncTask
         *
         * @param fragment The MyLocationFragment to use later
         */
        private GeoCoderAsyncTask(final MyLocationFragment fragment, final int maxResult) {
            // We create a WeakReference to the Context
            this.mFragmentRef = new WeakReference<MyLocationFragment>(fragment);
            // The max result for the geocoding
            this.mMaxResult = maxResult;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            final MyLocationFragment fragment = mFragmentRef.get();
            if (fragment != null) {
                mProgressDialog = ProgressDialogFragment.get(R.string.loading_message);
                mProgressDialog.show(fragment.getActivity().getSupportFragmentManager(), PROGRESS_TAG);
            }

        }

        @Override
        protected List<Address> doInBackground(Location... params) {
            // If the context is not available we skip
            final MyLocationFragment fragment = mFragmentRef.get();
            if (fragment == null) {
                Log.w(TAG_LOG, "Context is null!");
                return null;
            }
            // We have to create the Geocoder instance
            final Geocoder geocoder = new Geocoder(fragment.getActivity(), Locale.getDefault());
            // We get the Location to geocode
            final Location location = params[0];
            // We get the Addresses from the Location
            List<Address> geoAddresses = null;
            try {
                geoAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), mMaxResult);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG_LOG, "Error getting Addressed from Location: " + location);
            }
            return geoAddresses;
        }


        @Override
        protected void onPostExecute(List<Address> addresses) {
            super.onPostExecute(addresses);
            if (mProgressDialog != null) {
                mProgressDialog.dismissAllowingStateLoss();
            }
            // If the context is not available we skip
            final MyLocationFragment fragment = mFragmentRef.get();
            if (fragment != null && addresses != null && addresses.size() > 0) {
                final Address address = addresses.get(0);
                // We compose the String for the GeoCoder
                final StringBuilder geoCoding = new StringBuilder();
                final int maxIndex = address.getMaxAddressLineIndex();
                for (int i = 0; i <= maxIndex; i++) {
                    geoCoding.append(address.getAddressLine(i));
                    if (i < maxIndex) {
                        geoCoding.append(", ");
                    }
                }
            } else {
                Log.w(TAG_LOG, "Geocode data not available");
                if (fragment != null) {
                    Toast.makeText(fragment.getActivity(), R.string.my_location_geo_coding_not_available, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
