package uk.co.massimocarli.friendfence.datalayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import uk.co.massimocarli.friendfence.R;

/**
 * This is the Activity we use as a demo for the Sync data example
 * Created by Massimo Carli on 25/10/14.
 */
public class SyncImageActivity extends Activity {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = SyncImageActivity.class.getName();

    /**
     * The Path of the message we want to send
     */
    private static final String SYNC_IMAGE_PATH = "/friendfence/image";

    /**
     * The extra for the Image in case of Map usage
     */
    private static final String IMAGE_KEY = "IMAGE_KEY";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * The ImageView for the Image
     */
    private ImageView mImageView;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;


    /**
     * This is the implementation of the ConnectionCallbacks interface we use to manage
     * connection callback methods
     */
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle bundle) {
                    Log.i(TAG_LOG, "GoogleApiClient connected");
                    Toast.makeText(getApplication(), "Connected ", Toast.LENGTH_SHORT).show();
                    Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.i(TAG_LOG, "GoogleApiClient connection suspended");
                    Toast.makeText(getApplication(), "CONNECTION ERROR! ", Toast.LENGTH_SHORT).show();
                    retryConnecting();
                    Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
                }
            };
    /**
     * Implementation of the interface that manages connection errors
     */
    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedLister =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    Log.i(TAG_LOG, "GoogleApiClient connection failed: " + connectionResult.toString());
                    if (!connectionResult.hasResolution()) {
                        // Show a localized error dialog.
                        GooglePlayServicesUtil.getErrorDialog(
                                connectionResult.getErrorCode(), SyncImageActivity.this, 0, new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        retryConnecting();
                                    }
                                }).show();
                        return;
                    }
                    // If there is an existing resolution error being displayed or a resolution
                    // activity has started before, do nothing and wait for resolution
                    // progress to be completed.
                    if (mIsInResolution) {
                        return;
                    }
                    mIsInResolution = true;
                    try {
                        connectionResult.startResolutionForResult(SyncImageActivity.this, REQUEST_CODE_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG_LOG, "Exception while starting resolution activity", e);
                        retryConnecting();
                    }
                }
            };

    /**
     * The DataApi.DataListener we register to manage images
     */
    private final DataApi.DataListener mDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent event : dataEvents) {
                // In case of usage of PutDataRequest
                //manageEvent(event);
                // In case of usage of PutDataMapRequest
                manageEventMap(event);
            }
        }
    };


    /**
     * Utility method that manage event
     *
     * @param event The event to manage
     */
    private void manageEvent(DataEvent event) {
        // We get the DataItem
        final DataItem dataItem = event.getDataItem();
        // We get the Uri for the DataItem
        final Uri dataUri = dataItem.getUri();
        if (DataEvent.TYPE_CHANGED == event.getType() && SYNC_IMAGE_PATH.equals(dataUri.getPath())) {
            // We get the information
            final DataItemAsset dataItemAsset = dataItem.getAssets().get(IMAGE_KEY);
            // We get the File Descriptor
            Wearable.DataApi.getFdForAsset(mGoogleApiClient, dataItemAsset).setResultCallback(new ResultCallback<DataApi.GetFdForAssetResult>() {
                @Override
                public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                    final Bitmap bitmap = BitmapFactory.decodeStream(getFdForAssetResult.getInputStream());
                    mImageView.setImageBitmap(bitmap);
                }
            });
        }
    }

    /**
     * Utility method that manage event with Map
     *
     * @param event The event to manage
     */
    private void manageEventMap(DataEvent event) {
        // We get the DataItem
        final DataItem dataItem = event.getDataItem();
        // We get the Uri for the DataItem
        final Uri dataUri = dataItem.getUri();
        if (DataEvent.TYPE_CHANGED == event.getType() && SYNC_IMAGE_PATH.equals(dataUri.getPath())) {
            // We get the DataMapItem
            DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
            // We get the asset
            Asset profileAsset = dataMapItem.getDataMap().getAsset(IMAGE_KEY);
            // We get the File Descriptor
            Wearable.DataApi.getFdForAsset(mGoogleApiClient, profileAsset).setResultCallback(new ResultCallback<DataApi.GetFdForAssetResult>() {
                @Override
                public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                    final Bitmap bitmap = BitmapFactory.decodeStream(getFdForAssetResult.getInputStream());
                    mImageView.setImageBitmap(bitmap);
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_sync_layout);
        // We initialize Wearable API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedLister)
                .build();
        // Get the reference to the message TextView
        mImageView = (ImageView) findViewById(R.id.data_layer_image_view);
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_RESOLUTION == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                retryConnecting();
            } else {
                mGoogleApiClient.disconnect();
                mIsInResolution = false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This method implements the logic for retry
     */
    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }


    /**
     * Utility method to get the Bitmap form the Asset
     *
     * @param asset The Asset with the image inside
     * @return The Bitmap
     */
    private Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG_LOG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

}
