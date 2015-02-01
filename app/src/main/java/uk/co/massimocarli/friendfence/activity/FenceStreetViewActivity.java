package uk.co.massimocarli.friendfence.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.SeekBar;

import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.content.cursor.CursorResolver;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is the Activity that hosts the Fragment for the StreetView management
 * Created by Massimo Carli on 21/09/14.
 */
public class FenceStreetViewActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = FenceStreetViewActivity.class.getName();

    /**
     * The Key for the Session Id
     */
    private static final String SESSION_ID_KEY = Conf.PKG + ".extra.SESSION_ID_KEY";

    /**
     * The Identifier for the loader
     */
    private final static int STREET_VIEW_LOADER_ID = 49;

    /**
     * The Object to interact with the StreetView features
     */
    private StreetViewPanorama mStreetViewPanorama;

    /**
     * The id of the current session
     */
    private long mSessionId;

    /**
     * The SeekBar to move the positions
     */
    private SeekBar mSeekBar;

    /**
     * The List of all positions
     */
    private List<LatLng> mPositionList = new LinkedList<LatLng>();

    /**
     * Starts the Activity for the given Session
     *
     * @param context   The Context
     * @param sessionId The id of the session to show
     */
    public static void showSessionStreetViewActivity(final Context context, final long sessionId) {
        final Intent intent = new Intent(context, FenceStreetViewActivity.class);
        intent.putExtra(SESSION_ID_KEY, sessionId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);
        mSessionId = getIntent().getLongExtra(SESSION_ID_KEY, -1);
        if (mSessionId == -1) {
            Log.w(TAG_LOG, "SessionId missing!");
            finish();
            return;
        }
        mSeekBar = UI.findViewById(this, R.id.fence_stree_view_seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Here we just show the current position if any
                if (mPositionList.size() > progress) {
                    showPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mStreetViewPanorama == null) {
            mStreetViewPanorama = ((SupportStreetViewPanoramaFragment)
                    getSupportFragmentManager().findFragmentById(R.id.fence_street_view))
                    .getStreetViewPanorama();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(STREET_VIEW_LOADER_ID, new Bundle(), this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We create the Uri for all the position of a given session
        final Uri sessionPositionUri = FenceDB.FencePosition.getPositionUriForSession(mSessionId);
        CursorLoader loader = new CursorLoader(this, sessionPositionUri,
                null, null, null, FenceDB.FencePosition.POSITION_TIME + " DESC ");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mPositionList.clear();
        final FenceCursorFactory.FencePositionCursorData cursorData =
                CursorResolver.CURSOR_RESOLVER.extractPositionCursor(cursor);
        while (cursorData.moveToNext()) {
            final LatLng latLng = new LatLng(cursorData.getLatitude(), cursorData.getLongitude());
            mPositionList.add(latLng);
        }
        // We go to the first position
        if (mPositionList.size() > 0) {
            showPosition(0);
            mSeekBar.setMax(mPositionList.size());
            mSeekBar.setProgress(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }


    /**
     * Show the position at the given index if any
     *
     * @param positionIndex The index of the position to show
     */
    private void showPosition(final int positionIndex) {
        if (mPositionList.size() > positionIndex) {
            mStreetViewPanorama.setPosition(mPositionList.get(positionIndex));
        }
    }

}
