package uk.co.massimocarli.friendfence.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import uk.co.massimocarli.friendfence.R;

/**
 * The Activity we use as Splash
 */
public class SplashActivity extends Activity {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = SplashActivity.class.getName();

    /**
     * The delay to wait before going to the MainActivity
     */
    private static final long SPLASH_DELAY = 2000;

    /**
     * The Request code to use for the Dialog management
     */
    private static final int GPS_REQUEST_CODE = 1;

    /**
     * The Handler to manage the delay message
     */
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!isFinishing()) {
                final Intent mainIntent =
                        new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We check if the GooglePlayServices are available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS != resultCode) {
            // In this case the Google Play Services are not installed or
            // in the wrong version so we have to launch the Play Store
            // for the installation
            GooglePlayServicesUtil.showErrorDialogFragment(resultCode,
                                                           this, GPS_REQUEST_CODE,
                                                           new DialogInterface.OnCancelListener() {
                                                               @Override
                                                               public void onCancel(DialogInterface dialog) {
                                                                   // In this case we close the app
                                                                   Toast.makeText(SplashActivity.this,
                                                                                  R.string.gps_mandatory,
                                                                                  Toast.LENGTH_LONG).show();
                                                                   finish();
                                                               }
                                                           });
        } else {
            // Here we implement the logic for the MainActivity
            mHandler.sendEmptyMessageDelayed(0, SPLASH_DELAY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GPS_REQUEST_CODE == requestCode &&
                Activity.RESULT_CANCELED == resultCode) {
            Log.d(TAG_LOG, "Dialog closed from the Play Market");
            Toast.makeText(SplashActivity.this, R.string.gps_mandatory,
                           Toast.LENGTH_LONG).show();
        }
    }

}
