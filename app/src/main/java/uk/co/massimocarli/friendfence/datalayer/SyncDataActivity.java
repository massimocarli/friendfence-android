package uk.co.massimocarli.friendfence.datalayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.ProgressDialogFragment;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * Created by Massimo Carli on 12/10/14.
 */
public class SyncDataActivity extends ActionBarActivity {

    /**
     * The Tag for the name
     */
    private final static String TAG_LOG = SyncDataActivity.class.getName();

    /**
     * The Path of the message we want to send
     */
    private static final String MESSAGE_PATH = "/friendfence/sync";

    /**
     * The key to persist the resolution state of this Activity
     */
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * The name of the extra for the message to show
     */
    public static final String MESSAGE_EXTRA = "MESSAGE_EXTRA";

    /**
     * The name of the extra for the name of the node
     */
    public static final String NODE_EXTRA = "NODE_EXTRA";

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
     * The EditText for the input message
     */
    private EditText mEditTextInput;

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
                                connectionResult.getErrorCode(), SyncDataActivity.this, 0, new DialogInterface.OnCancelListener() {
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
                        connectionResult.startResolutionForResult(SyncDataActivity.this, REQUEST_CODE_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG_LOG, "Exception while starting resolution activity", e);
                        retryConnecting();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_layer);
        // Google APi Client initialization
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedLister)
                .build();
        // Get the reference to the items in the layout
        mEditTextInput = UI.findViewById(this, R.id.data_layer_message_input);
        final String receivedMessage = getIntent().getStringExtra(MESSAGE_EXTRA);
        if (!TextUtils.isEmpty(receivedMessage)) {
            mEditTextInput.setText(receivedMessage);
        }
        UI.findViewById(this, R.id.data_layer_message_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We use the AsyncTask to send the message
                final Editable reply = mEditTextInput.getText();
                if (!TextUtils.isEmpty(reply)) {
                    // We sync the message
                    //syncMessage(reply.toString());
                    syncMessageMap(reply.toString());
                } else {
                    // We show a message
                    Log.w(TAG_LOG, "Empty message!");
                    Toast.makeText(SyncDataActivity.this, R.string.chat_empty_reply,
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String receivedMessage = intent.getStringExtra(MESSAGE_EXTRA);
        if (!TextUtils.isEmpty(receivedMessage)) {
            mEditTextInput.setText(receivedMessage);
        }
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
     * @param message The message to send
     */
    private void syncMessage(final String message) {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We create the PutDataRequest object
        final PutDataRequest putDataRequest = PutDataRequest.create(MESSAGE_PATH);
        // We set the payload
        putDataRequest.setData(message.getBytes());
        // We set the data as payload into the shared container
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    // We get the DataItem sent
                    final DataItem dataItem = dataItemResult.getDataItem();
                    Log.d(TAG_LOG, "DataItem " + dataItem + " successfully sent");
                    Toast.makeText(SyncDataActivity.this, R.string.data_layer_sync_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Error creating the message
                    Toast.makeText(SyncDataActivity.this, R.string.data_layer_sync_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Utility method that describe how sync data using Wearable API using PutDataMapRequest
     *
     * @param message The message to send
     */
    private void syncMessageMap(final String message) {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We create the PutDataMapRequest object
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(MESSAGE_PATH);
        // We get the DataMap
        final DataMap dataMap = putDataMapRequest.getDataMap();
        // We set the message as value in the map
        dataMap.putString(MESSAGE_EXTRA, message);
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
                    Toast.makeText(SyncDataActivity.this, R.string.data_layer_sync_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Error creating the message
                    Toast.makeText(SyncDataActivity.this, R.string.data_layer_sync_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
