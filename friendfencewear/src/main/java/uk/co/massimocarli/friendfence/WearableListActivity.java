package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;

import uk.co.massimocarli.friendfence.datalayer.SyncImageActivity;


public class WearableListActivity extends Activity {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = WearableListActivity.class.getName();

    /**
     * The Model
     */
    private static final String[] MODEL = {"CardFragment", "CardScroll", "Grid", "Confirm", "Dismiss", "SyncImage"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wearable_list);
        // We get the reference to the WearableListView
        final WearableListView listView = (WearableListView) findViewById(R.id.wearable_list);
        // We create the Adapter
        final WearListAdapter adapter = new WearListAdapter(this, MODEL);
        // We set the Adapter to the List
        listView.setAdapter(adapter);
        // We set the listener for the ListView
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                // We get the selected position
                final int selectedPosition = (Integer) viewHolder.itemView.getTag();
                switch (selectedPosition) {
                    case 0:
                        launchCardFragmentTest();
                        break;
                    case 1:
                        launchCardScrollTest();
                        break;
                    case 2:
                        launchGridTest();
                        break;
                    case 3:
                        launchConfirmTest();
                        break;
                    case 4:
                        launchDismissTest();
                        break;
                    case 5:
                        launchSyncImage();
                        break;
                    default:
                        // Nothing to do
                }
            }

            @Override
            public void onTopEmptyRegionClick() {
                // If we click outside the ist
            }
        });
    }

    /**
     * Utility method that starts the CardFragment example
     */
    private void launchCardFragmentTest() {
        final Intent intent = new Intent(this, CardFragmentTest.class);
        startActivity(intent);
        // We use finish() in case we want to remove this Activity from the stack
        //finish();
    }

    /**
     * Utility method that starts the CardScrollView example
     */
    private void launchCardScrollTest() {
        final Intent intent = new Intent(this, CardScrollActivity.class);
        startActivity(intent);
        // We use finish() in case we want to remove this Activity from the stack
        //finish();
    }

    /**
     * Utility method that starts the Grid example
     */
    private void launchGridTest() {
        final Intent intent = new Intent(this, GridActivity.class);
        startActivity(intent);
        // We use finish() in case we want to remove this Activity from the stack
        //finish();
    }

    /**
     * Utility method that starts the Auto Confirm example
     */
    private void launchConfirmTest() {
        final Intent intent = new Intent(this, AutoConfirmActivity.class);
        startActivity(intent);
        // We use finish() in case we want to remove this Activity from the stack
        //finish();
    }

    /**
     * Utility method that starts the Dismiss example
     */
    private void launchDismissTest() {
        final Intent intent = new Intent(this, DismissActivity.class);
        startActivity(intent);
        // We use finish() in case we want to remove this Activity from the stack
        //finish();
    }

    /**
     * Utility method that starts the SyncImage example
     */
    private void launchSyncImage() {
        final Intent intent = new Intent(this, SyncImageActivity.class);
        startActivity(intent);
        // We use finish() in case we want to remove this Activity from the stack
        //finish();
    }


}
