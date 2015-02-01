package uk.co.massimocarli.friendfence.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.map.GeocoderInfoWindowAdapter;
import uk.co.massimocarli.friendfence.map.ShowSessionInMapFragment;

/**
 * Created by Massimo Carli on 21/09/14.
 */
public class ShowPathActivity extends ActionBarActivity {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = ShowPathActivity.class.getName();

    /**
     * The Key for the Session Id
     */
    private static final String SESSION_ID_KEY = Conf.PKG + ".extra.SESSION_ID_KEY";


    /**
     * The Reference to the MapFragment
     */
    private SupportMapFragment mMapFragment;

    /**
     * The handle to manage Google Map
     */
    private GoogleMap mGoogleMap;

    /**
     * Starts the Activity for the given Session
     *
     * @param context   The Context
     * @param sessionId The id of the session to show
     */
    public static void showSessionPathActivity(final Context context, final long sessionId) {
        final Intent intent = new Intent(context, ShowPathActivity.class);
        intent.putExtra(SESSION_ID_KEY, sessionId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        final long sessionId = getIntent().getLongExtra(SESSION_ID_KEY, -1);
        if (sessionId == -1) {
            Log.w(TAG_LOG, "SessionId missing!");
            finish();
            return;
        }
        mMapFragment = ShowSessionInMapFragment.newInstance(sessionId);
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setMyLocationEnabled(true);
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.single_fragment_anchor, mMapFragment).commit();
    }
}
