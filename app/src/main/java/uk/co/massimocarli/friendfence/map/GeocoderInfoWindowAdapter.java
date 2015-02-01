package uk.co.massimocarli.friendfence.map;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is an implementation of the InfoWindowAdapter interface to show the information related
 * to an Address obtained using the Geocoder.
 * Created by Massimo Carli on 20/09/14.
 */
public class GeocoderInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = GeocoderInfoWindowAdapter.class.getName();

    /**
     * The timeout for the CountDownLatch (1 secondo)
     */
    private static final long TIMEOUT = 1000L;

    /**
     * The Context
     */
    private final Context mContext;

    /**
     * The Info for the Info Window
     */
    private final View mInfoWindowView;

    /**
     * The Holder to optimization
     */
    private final Holder mHolder;

    /**
     * The Address reference
     */
    private volatile Address mAddress;

    /**
     * The Holder for the elements into the Info View. This is useful for recycling the
     * reference to the Views when this object is reused
     */
    private class Holder {

        TextView mLocationTextView;
        TextView mCountryTextView;
        TextView mAddressTextView;

    }

    /**
     * Create a GeocoderInfoWindowAdapter passing the Context and the Address
     *
     * @param context The Context
     */
    public GeocoderInfoWindowAdapter(final Context context) {
        this.mContext = context;
        // Here we create the View to show for Info Window
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        mInfoWindowView = layoutInflater.inflate(R.layout.info_window_address, null);
        mHolder = new Holder();
        mHolder.mLocationTextView = UI.findViewById(mInfoWindowView, R.id.info_window_address_location);
        mHolder.mCountryTextView = UI.findViewById(mInfoWindowView, R.id.info_window_address_country);
        mHolder.mAddressTextView = UI.findViewById(mInfoWindowView, R.id.info_window_address_detail);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // We invoke an utility method so we can test the usage of it into this method
        // or into the getInfoContents method
        //return createInfoView(marker);
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        return createInfoView(marker);
    }


    /**
     * This method encapsulate all the logic of the View creation for the InfoWindow
     */
    private View createInfoView(final Marker marker) {
        // We get the info from the Marker
        final double latitude = marker.getPosition().latitude;
        final double longitude = marker.getPosition().longitude;
        final String locationString = mContext.getString(R.string.info_window_location,
                Location.convert(latitude, Location.FORMAT_DEGREES),
                Location.convert(longitude, Location.FORMAT_DEGREES));
        // We set the address
        mHolder.mLocationTextView.setText(locationString);
        // We manage Geocoder
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        // We launch the Tread to get Geocoding
        mAddress = null;
        GeoCodingRunnable geoCodingRunnable = new GeoCodingRunnable(countDownLatch, marker.getPosition());
        new Thread(geoCodingRunnable).start();
        try {
            countDownLatch.await(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAddress != null) {
            // The Country info
            final String countryInfo = mContext.getString(R.string.info_window_country, mAddress.getCountryCode(), mAddress.getCountryName());
            mHolder.mCountryTextView.setText(countryInfo);
            // The address info
            final StringBuilder addressBuilder = new StringBuilder();
            for (int i = 0; i < mAddress.getMaxAddressLineIndex(); i++) {
                addressBuilder.append(mAddress.getAddressLine(i)).append(" ");
            }
            mHolder.mAddressTextView.setText(addressBuilder.toString());
            // This fields are visible
            mHolder.mCountryTextView.setVisibility(View.VISIBLE);
            mHolder.mAddressTextView.setVisibility(View.VISIBLE);
        } else {
            // The information is not present
            mHolder.mCountryTextView.setVisibility(View.GONE);
            mHolder.mAddressTextView.setVisibility(View.GONE);
        }
        return mInfoWindowView;
    }


    private class GeoCodingRunnable implements Runnable {

        /**
         * The Max number of result
         */
        private final static int MAX_RESULT = 1;

        /**
         * The CountDownLatch we use to notify the end
         */
        private final CountDownLatch mCountDownLatch;

        /**
         * The Geocoder
         */
        private final Geocoder mGeocoder;

        /**
         * The Location for the Geocoder
         */
        private final LatLng mLocation;

        /**
         * Constructor for the GeoCoderAsyncTask
         */
        private GeoCodingRunnable(final CountDownLatch countDownLatch, final LatLng location) {
            this.mCountDownLatch = countDownLatch;
            this.mGeocoder = new Geocoder(mContext, Locale.getDefault());
            this.mLocation = location;
        }

        @Override
        public void run() {
            // We get the Addresses from the Location
            List<Address> geoAddresses = null;
            try {
                geoAddresses = mGeocoder.getFromLocation(mLocation.latitude, mLocation.longitude, MAX_RESULT);
                if (geoAddresses != null && geoAddresses.size() > 0) {
                    mAddress = geoAddresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG_LOG, "Error getting Addressed from Location: " + mLocation);
            } finally {
                mCountDownLatch.countDown();
            }
        }
    }

}
