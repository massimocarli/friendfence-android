package uk.co.massimocarli.friendfence.datalayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.Set;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * Activity we use to send a message
 * Created by Massimo Carli on 12/10/14.
 */
public class SendMessageActivity extends ActionBarActivity {

    /**
     * The Tag for the name
     */
    private final static String TAG_LOG = SendMessageActivity.class.getName();

    /**
     * The Path of the message we want to send
     */
    private static final String MESSAGE_PATH = "/friendfence/timestamp";

    /**
     * The key to persist the resolution state of this Activity
     */
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

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
     * The set of the connected nodes ids
     */
    private Set<String> mNodeIds = new HashSet<String>();

    /**
     * The Listener for the Message
     */
    private MessageApi.MessageListener mMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {

        }
    };

    /**
     * The Listener for the Node
     */
    private NodeApi.NodeListener mNodeListener = new NodeApi.NodeListener() {
        @Override
        public void onPeerConnected(Node node) {
            // we search the Node identifiers
            Toast.makeText(getApplicationContext(), "Node " + node.getDisplayName() + " connected",
                    Toast.LENGTH_SHORT).show();
            mNodeIds.add(node.getId());
        }

        @Override
        public void onPeerDisconnected(Node node) {
            Toast.makeText(getApplicationContext(), "Node " + node.getDisplayName() + " disconnected",
                    Toast.LENGTH_SHORT).show();
            // We remove the nodes
            mNodeIds.remove(node.getId());
        }
    };

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
                    // We register the listener for the NodeApi
                    Wearable.MessageApi.addListener(mGoogleApiClient, mMessageListener);
                    Wearable.NodeApi.addListener(mGoogleApiClient, mNodeListener);
                    // We query the connected nodes
                    insertConnectedNodes();
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.i(TAG_LOG, "GoogleApiClient connection suspended");
                    Toast.makeText(getApplication(), "CONNECTION ERROR! ", Toast.LENGTH_SHORT).show();
                    Wearable.MessageApi.removeListener(mGoogleApiClient, mMessageListener);
                    Wearable.NodeApi.removeListener(mGoogleApiClient, mNodeListener);
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
                                connectionResult.getErrorCode(), SendMessageActivity.this, 0, new DialogInterface.OnCancelListener() {
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
                        connectionResult.startResolutionForResult(SendMessageActivity.this, REQUEST_CODE_RESOLUTION);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG_LOG, "Exception while starting resolution activity", e);
                        retryConnecting();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message_layout);
        // Google APi Client initialization
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedLister)
                .build();
        // The event on the Button
        UI.findViewById(this, R.id.data_layer_send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We Send the message
                sendMessage();
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
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Utility method that describe how to send a message with timestamp information
     */
    private void sendMessage() {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG_LOG, "mGoogleApiClient not connected ");
            return;
        }
        // We check if we have connected nodes
        if (mNodeIds.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.action_data_layer_no_connected_nodes,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // We send the message to the Wearable device with the first id
        final String nodeId = mNodeIds.toArray(new String[mNodeIds.size()])[0];
        // The message to send
        final byte[] dataToSend = String.valueOf(System.currentTimeMillis()).getBytes();
        // We send the data
        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, MESSAGE_PATH, dataToSend)
                .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (sendMessageResult.getStatus().isSuccess()) {
                            // Message sent successfully
                            Toast.makeText(getApplicationContext(), R.string.data_layer_send_success,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Error sending message
                            Toast.makeText(getApplicationContext(), R.string.data_layer_send_failure,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * We add the already connected nodes
     */
    private void insertConnectedNodes() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    mNodeIds.add(node.getId());
                }
            }
        });
    }

}
