package uk.co.massimocarli.friendfence.datalayer;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is the service we use to manage Data synchronization with the main application
 * Created by Massimo Carli on 25/10/14.
 */
public class SyncDataService extends WearableListenerService {


    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = SyncDataService.class.getName();

    /**
     * The Path of the message we want to send
     */
    public static final String MESSAGE_PATH = "/friendfence/sync";

    /**
     * The timeout for the GoogleApiClient connection in seconds
     */
    private static final long CONNECTION_TIMEOUT = 10;

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.d(TAG_LOG, "onDataChanged received on Wearable device");
        // We get the information form the DataEvents as Freezable object
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        // We initialize the GoogleApiClient object
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        // We connect the GoogleApiClient
        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        if (connectionResult.isSuccess()) {
            // Now we have the list of received events so we manage all the data in it
            for (DataEvent event : events) {
                // We use this if the sync is done using PutDataRequest
                //manageEvent(event);
                // We use this if the sync is done using PutDataMapRequest
                manageEventMap(event);
            }
        } else {
            Log.e(TAG_LOG, "Error connecting to GoogleApiClient");
        }
    }


    /**
     * Utility method that manages the received event
     *
     * @param event Encapsulate the information for the event
     */
    private void manageEvent(final DataEvent event) {
        // We get the DataItem
        final DataItem dataItem = event.getDataItem();
        // We get the Uri for the received event
        final Uri receivedUri = dataItem.getUri();
        // We check if the uri is related to the data sent form the application on
        // the main device
        if (MESSAGE_PATH.equals(receivedUri.getPath())) {
            // We get the nodeId we need for the reply
            String nodeId = receivedUri.getHost();
            // We read the payload for the received data
            final byte[] payload = dataItem.getData();
            final String receivedMessage = new String(payload);
            // We launch the Activity to manage this
            Intent intent = new Intent(this, SyncDataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(SyncDataActivity.MESSAGE_EXTRA, receivedMessage);
            intent.putExtra(SyncDataActivity.NODE_EXTRA, nodeId);
            startActivity(intent);
        }
    }

    /**
     * Utility method that manages the received event when sent with Map
     *
     * @param event Encapsulate the information for the event
     */
    private void manageEventMap(final DataEvent event) {
        // We get the DataItem
        final DataItem dataItem = event.getDataItem();
        // We get the Uri for the received event
        final Uri receivedUri = dataItem.getUri();
        // We check if the uri is related to the data sent form the application on
        // the main device
        if (MESSAGE_PATH.equals(receivedUri.getPath())) {
            // We get the nodeId we need for the reply
            String nodeId = receivedUri.getHost();
            // We get the DataMapItem
            final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            // We read the message
            final String receivedMessage = dataMapItem.getDataMap()
                    .getString(SyncDataActivity.MESSAGE_EXTRA);
            // We launch the Activity to manage this
            Intent intent = new Intent(this, SyncDataActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(SyncDataActivity.MESSAGE_EXTRA, receivedMessage);
            intent.putExtra(SyncDataActivity.NODE_EXTRA, nodeId);
            startActivity(intent);
        }
    }
}
