package uk.co.massimocarli.friendfence.map;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.content.cursor.CursorResolver;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;

/**
 * Created by Massimo Carli on 21/09/14.
 */
public class ShowSessionInMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The argument for the SessionId
     */
    private static final String SESSION_ID_ARG = Conf.PKG + ".arg.SESSION_ID_ARG";

    /**
     * The identifier of the Loader for the session
     */
    private static final int FENCE_SESSION_PATH_LOADER_ID = 27;

    /**
     * The Default padding
     */
    private static final int DEFAULT_PADDING = 10;

    /**
     * The width of the Path
     */
    private static final int PATH_WIDTH = 20;

    /**
     * The Polyline for the path
     */
    private Polyline mLastPolyline;

    /**
     * The Id of the session to show
     */
    private long mSessionId;

    /**
     * Creates a SupportMapFragment that we use to manage the creation of the GoogleMap object
     * r
     *
     * @return The SupportMapFragment created
     */
    public static ShowSessionInMapFragment newInstance(final long sessionId) {
        ShowSessionInMapFragment fragment = new ShowSessionInMapFragment();
        final Bundle arguments = new Bundle();
        arguments.putLong(SESSION_ID_ARG, sessionId);
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionId = getArguments().getLong(SESSION_ID_ARG, 0);
    }


    public static ShowSessionInMapFragment create(final long sessionId) {
        final ShowSessionInMapFragment fragment = new ShowSessionInMapFragment();
        return fragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(FENCE_SESSION_PATH_LOADER_ID, null, this);
        // Test for Polygon
        //final LatLng rome = new LatLng(41.872389, 12.48018);
        //showRectangle(rome);
        // Test for Circle
        //showCircle(rome, 10000);
    }


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = new CursorLoader(getActivity(), FenceDB.FencePosition.getPositionUriForSession(mSessionId),
                null, null, null, FenceDB.FencePosition.POSITION_TIME + " DESC ");
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        // We read the data from the DB and create the Polygon
        final FenceCursorFactory.FencePositionCursorData positionCursorData = CursorResolver.CURSOR_RESOLVER.extractPositionCursor(cursor);
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        PolylineOptions currentPolyline = null;
        int lastActivityType = -1;
        LatLng position = null;
        LatLng previous = null;
        while (cursor.moveToNext()) {
            final int currentActivityType = positionCursorData.getActivityType();
            if (lastActivityType != currentActivityType) {
                // In this case the activity type is different so we add the previous
                // Polyline and create a new one
                if (currentPolyline != null) {
                    getMap().addPolyline(currentPolyline);
                }
                lastActivityType = currentActivityType;
                currentPolyline = new PolylineOptions()
                        .color(getActivityColor(currentActivityType))
                        .width(PATH_WIDTH);
            }
            if (previous != null) {
                currentPolyline.add(previous);
            }
            position = new LatLng(positionCursorData.getLatitude(), positionCursorData.getLongitude());
            previous = position;
            currentPolyline.add(position);
            bounds.include(position);
        }
        // We add the last
        getMap().addPolyline(currentPolyline);
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds.build(), DEFAULT_PADDING);
        getMap().moveCamera(cameraUpdate);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
    }


    /**
     * Utility method that return a different color for every activity type
     *
     * @return The color for the given Activity
     */
    private int getActivityColor(final int activityType) {
        switch (activityType) {
            case DetectedActivity.UNKNOWN:
                return 0xff000000;
            case DetectedActivity.ON_FOOT:
                return 0xff0000ff;
            case DetectedActivity.RUNNING:
                return 0xffff0000;
            case DetectedActivity.IN_VEHICLE:
                return 0xffffff00;
            case DetectedActivity.ON_BICYCLE:
                return 0xffdddddd;
            case DetectedActivity.STILL:
                return 0xff00ff00;
            case DetectedActivity.TILTING:
                return 0xffff00ff;
            case DetectedActivity.WALKING:
                return 0xffabbaab;
            default:
                return 0xff000000;
        }
    }


    /**
     * Utility method that shows a rectangle
     *
     * @param center The center of the rectangle
     */
    private void showRectangle(final LatLng center) {
        final float WIDTH = 0.1f;
        final float HEIGHT = 0.1f;
        final LatLng p1 = new LatLng(center.latitude - WIDTH / 2, center.longitude + HEIGHT / 2);
        final LatLng p2 = new LatLng(center.latitude + WIDTH / 2, center.longitude + HEIGHT / 2);
        final LatLng p3 = new LatLng(center.latitude + WIDTH / 2, center.longitude - HEIGHT / 2);
        final LatLng p4 = new LatLng(center.latitude - WIDTH / 2, center.longitude - HEIGHT / 2);
        PolygonOptions polygonOptions = new PolygonOptions()
                .add(p1).add(p2).add(p3).add(p4);
        getMap().addPolygon(polygonOptions);
    }

    /**
     * Utility method that shows a rectangle
     *
     * @param center The center of the rectangle
     * @param radius The radius in meter
     */
    private void showCircle(final LatLng center, final double radius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius(radius);
        getMap().addCircle(circleOptions);
    }
}
