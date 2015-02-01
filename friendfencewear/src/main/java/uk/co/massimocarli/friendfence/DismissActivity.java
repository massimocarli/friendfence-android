package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Massimo Carli on 24/10/14.
 */
public class DismissActivity extends Activity {

    /**
     * The reference to the object responsible for the long press dismiss
     */
    private DismissOverlayView mDismissOverlay;

    /**
     * We use this to detect the gestures
     */
    private GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dismiss_layout);

        // We manage the DismissOverlayView object
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.long_press_dismiss);
        mDismissOverlay.showIntroIfNecessary();
        // We use the GestureDetector to understand if the user did long press
        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                // We show the Dismiss Overlay
                mDismissOverlay.show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // This is the Method where we actually capture the long pressed
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }

}
