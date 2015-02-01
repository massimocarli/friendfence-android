package uk.co.massimocarli.friendfence.datalayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

import java.util.List;

import uk.co.massimocarli.friendfence.R;

/**
 * This is the Activity we use as a demo for the Sync data example
 * Created by Massimo Carli on 25/10/14.
 */
public class WearSyncDataActivity extends Activity {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = WearSyncDataActivity.class.getName();

    /**
     * The name of the extra for the message to show
     */
    public static final String MESSAGE_EXTRA = "MESSAGE_EXTRA";

    /**
     * The name of the extra for the name of the node
     */
    public static final String NODE_EXTRA = "NODE_EXTRA";

    /**
     * The request id for the reply operation
     */
    private static final int REPLY_REQUEST_ID = 37;

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * The TextView for the message
     */
    private TextView mMessageTextView;

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
                                connectionResult.getErrorCode(), WearSyncDataActivity.this, 0, new DialogInterface.OnCancelListener() {
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
                        connectionResult.startResolutionForResult(WearSyncDataActivity.this, REQUEST_CODE_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG_LOG, "Exception while starting resolution activity", e);
                        retryConnecting();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_layer_sync_layout);
        // We initialize Wearable API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedLister)
                .build();
        // Get the reference to the message TextView
        mMessageTextView = (TextView) findViewById(R.id.data_layer_message_text);
        // We show the text if present
        final String message = getIntent().getStringExtra(MESSAGE_EXTRA);
        if (!TextUtils.isEmpty(message)) {
            mMessageTextView.setText(message);
        }
        // We manage the reply with the button
        findViewById(R.id.data_layer_reply_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We manage the Reply
                startReply();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REPLY_REQUEST_ID == requestCode) {
            if (RESULT_OK == resultCode) {
                // We get a list of possible result
                List<String> results = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                // We get the first if available
                if (results != null && results.size() > 0) {
                    String recognizedText = results.get(0);
                    mMessageTextView.setText(recognizedText);
                    // We start the reply
                    //syncReply(recognizedText);
                    syncReplyMap(recognizedText);
                } else {
                    mMessageTextView.setText(R.string.talk_not_understood);
                }
            }
        } else if (REQUEST_CODE_RESOLUTION == requestCode) {
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
     * We launch the speech recognizer
     */
    private void startReply() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, REPLY_REQUEST_ID);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // We show the text if present
        final String message = intent.getStringExtra(MESSAGE_EXTRA);
        if (!TextUtils.isEmpty(message)) {
            mMessageTextView.setText(message);
        }
    }

    /**
     * Here we have the reply message
     *
     * @param replyMessage The reply message to send
     */
    private void syncReply(final String replyMessage) {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We create the PutDataRequest object
        final PutDataRequest putDataRequest = PutDataRequest.create(SyncDataService.MESSAGE_PATH);
        // We set the payload
        putDataRequest.setData(replyMessage.getBytes());
        // We set the data as payload into the shared container
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    // We get the DataItem sent
                    final DataItem dataItem = dataItemResult.getDataItem();
                    Log.d(TAG_LOG, "DataItem " + dataItem + " successfully sent");
                    Toast.makeText(WearSyncDataActivity.this, R.string.data_layer_sync_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Error creating the message
                    Toast.makeText(WearSyncDataActivity.this, R.string.data_layer_sync_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Here we have the reply message
     *
     * @param replyMessage The reply message to send
     */
    private void syncReplyMap(final String replyMessage) {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We create the PutDataMapRequest object
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SyncDataService.SYNC_PATH);
        // We get the DataMap
        final DataMap dataMap = putDataMapRequest.getDataMap();
        // We set the message as value in the map
        dataMap.putString(MESSAGE_EXTRA, replyMessage);
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
                    Toast.makeText(WearSyncDataActivity.this, R.string.data_layer_sync_success,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Error creating the message
                    Toast.makeText(WearSyncDataActivity.this, R.string.data_layer_sync_failure,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
