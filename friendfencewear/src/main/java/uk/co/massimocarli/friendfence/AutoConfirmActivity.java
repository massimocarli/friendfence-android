package uk.co.massimocarli.friendfence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;

/**
 * Created by Massimo Carli on 24/10/14.
 */
public class AutoConfirmActivity extends Activity {

    /**
     * The delay to wait before automatic confirmation
     */
    private static final long DELAY = 2000L;

    /**
     * The Reference to the Confirmation item
     */
    private DelayedConfirmationView mDelayedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_confirm);
        // We get the reference to the DelayedConfirmationView
        mDelayedView = (DelayedConfirmationView) findViewById(R.id.delayed_confirm);
        // We register the Listener
        mDelayedView.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {
            @Override
            public void onTimerFinished(View view) {
                // Here the time is finished
                showStandardConfirmation();
                /*
                final Intent boomIntent = new Intent(AutoConfirmActivity.this, BoomActivity.class);
                startActivity(boomIntent);
                */
                finish();
            }

            @Override
            public void onTimerSelected(View view) {
                mDelayedView.setListener(null);
                // The timer was closed
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // We set the timer
        mDelayedView.setTotalTimeMs(DELAY);
        // We start the timer
        mDelayedView.start();
    }


    /**
     * Utility method that shows standard confirmation messages
     */
    private void showStandardConfirmation() {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.boom_text));
        startActivity(intent);
    }
}
