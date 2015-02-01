package uk.co.massimocarli.friendfence.datalayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.ProgressDialogFragment;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * Activity we use to synchronize an image
 * Created by Massimo Carli on 12/10/14.
 */
public class SyncImageActivity extends ActionBarActivity {

    /**
     * The Tag for the name
     */
    private final static String TAG_LOG = SyncImageActivity.class.getName();

    /**
     * The Path of the message we want to send
     */
    private static final String SYNC_IMAGE_PATH = "/friendfence/image";

    /**
     * The extra for the Image in case of Map usage
     */
    private static final String IMAGE_KEY = "IMAGE_KEY";

    /**
     * The value for the best image quality
     */
    private static final int BEST_QUALITY = 100;

    /**
     * The key to persist the resolution state of this Activity
     */
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Request code for select the image
     */
    protected static final int PICK_IMAGE_REQUEST_CODE = 37;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     * The ProgressDialog
     */
    private ProgressDialogFragment mProgressDialog;

    /**
     * The ImageView with the image to send
     */
    private ImageView mImageView;

    /**
     * The Bitmap to send
     */
    private Bitmap mBitmap;

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
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.i(TAG_LOG, "GoogleApiClient connection suspended");
                    Toast.makeText(getApplication(), "CONNECTION ERROR! ", Toast.LENGTH_SHORT).show();
                    retryConnecting();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_image_layout);
        // Google APi Client initialization
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedLister)
                .build();
        // Get the reference to the items in the layout
        mImageView = UI.findViewById(this, R.id.data_layer_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We select the Image
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST_CODE);
            }
        });
        // The event on the Button
        UI.findViewById(this, R.id.data_layer_sync_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap != null) {
                    // We sync the image with PutDataRequest
                    // syncImage(mBitmap);
                    // We sync the image with PutDataMapRequest
                    syncImageMap(mBitmap);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.data_layer_image_missing,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

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

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    retryConnecting();
                } else {
                    mGoogleApiClient.disconnect();
                    mIsInResolution = false;
                }
                break;
            case PICK_IMAGE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = data.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                        mBitmap = BitmapFactory.decodeStream(imageStream);
                        mImageView.setImageBitmap(mBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Utility method that describe how sync data using Wearable API
     *
     * @param bitmap The Image to send
     */
    private void syncImage(final Bitmap bitmap) {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We get the Asset from the Bitmap
        Asset asset = fromBitmap(bitmap);
        // We create the PutDataRequest object
        final PutDataRequest putDataRequest = PutDataRequest.create(SYNC_IMAGE_PATH);
        // We set the Asset into the request
        putDataRequest.putAsset(IMAGE_KEY, asset);
        // We set the data as payload into the shared container
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    // We get the DataItem sent
                    final DataItem dataItem = dataItemResult.getDataItem();
                    Log.d(TAG_LOG, "DataItem " + dataItem + " successfully sent");
                    Toast.makeText(SyncImageActivity.this, R.string.data_layer_sync_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Error creating the message
                    Toast.makeText(SyncImageActivity.this, R.string.data_layer_sync_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Utility method that describe how sync data using Wearable API using PutDataMapRequest
     *
     * @param bitmap The bitmap to send
     */
    private void syncImageMap(final Bitmap bitmap) {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We get the Asset from the Bitmap
        Asset asset = fromBitmap(bitmap);
        // We create the PutDataMapRequest object
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SYNC_IMAGE_PATH);
        // We get the DataMap
        final DataMap dataMap = putDataMapRequest.getDataMap();
        // We set the asset
        dataMap.putAsset(IMAGE_KEY, asset);
        // We get the PutDataRequest
        final PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        // We set the data as payload into the shared container
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    // We get the DataItem sent
                    final DataItem dataItem = dataItemResult.getDataItem();
                    Log.d(TAG_LOG, "DataItem " + dataItem + " successfully sent");
                    Toast.makeText(SyncImageActivity.this, R.string.data_layer_sync_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Error creating the message
                    Toast.makeText(SyncImageActivity.this, R.string.data_layer_sync_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * This is an utility method that translate a Bitmap object into an Asset object
     *
     * @param bitmap The bitmap to transform
     * @return The Asset that contain the information for the bitmap
     */
    private static Asset fromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, BEST_QUALITY, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

}
