package uk.co.massimocarli.friendfence.location;

import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.content.cursor.CursorResolver;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;
import uk.co.massimocarli.friendfence.util.ActivityUtil;
import uk.co.massimocarli.friendfence.util.DistanceUtil;
import uk.co.massimocarli.friendfence.util.UI;

/**
 * This is a Fragment that shows the information related to the position for the given session
 */
public class FencePositionListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The Tag for the log
     */
    private static final String TAG_LOG = FencePositionListFragment.class.getName();

    /**
     * The key for the argument for the sessionId
     */
    private final static String SESSION_ID_ARG_NAME = Conf.PKG + ".arg.SESSION_ID_ARG_NAME";

    /**
     * The FROM field for the CursorAdapter of the DB fields
     */
    private final static String[] FROM = new String[]{FenceDB.FencePosition._ID, FenceDB.FencePosition.POSITION_TIME,
            FenceDB.FencePosition.LATITUDE, FenceDB.FencePosition.LONGITUDE, FenceDB.FencePosition.DISTANCE,
            FenceDB.FencePosition.ACTIVITY};

    /**
     * The TO field for the CursorAdapter of the Views id
     */
    private final static int[] TO = new int[]{R.id.fence_position_id, R.id.fence_position_time,
            R.id.fence_position_coordinates, R.id.fence_position_coordinates, R.id.fence_distance,
            R.id.fence_activity};

    /**
     * The identifier of the Loader for the position data
     */
    private final static int FENCE_POSITION_LOADER_ID = 37;

    /**
     * The identifier of the Loader for the session data
     */
    private final static int FENCE_SESSION_DATA_LOADER_ID = 38;


    /**
     * The adapter we use for the list
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * TextView for the owner of the session
     */
    private TextView mSessionOwnerTextView;

    /**
     * TextView for the start date of the session
     */
    private TextView mSessionStartDateTextView;

    /**
     * TextView for the end date of the session
     */
    private TextView mSessionEndDateTextView;

    /**
     * The id for the session we want to show
     */
    private long mSessionId;

    /**
     * Out custom cursor
     */
    private FenceCursorFactory.FencePositionCursorData mPositionCursorData;

    /**
     * We create a FencePositionListFragment for the given session
     *
     * @param sessionId The id of the session
     * @return The Fragment to show all the position for the given session
     */
    public static FencePositionListFragment create(final long sessionId) {
        FencePositionListFragment fragment = new FencePositionListFragment();
        final Bundle args = new Bundle();
        args.putLong(SESSION_ID_ARG_NAME, sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // we read the information related to sessionId
        mSessionId = getArguments().getLong(SESSION_ID_ARG_NAME, -1);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_position_item, null, FROM, TO, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {

                boolean coordinateManaged = false;

                // We get the reference to our cursor
                if (R.id.fence_position_time == view.getId()) {
                    // We have the date so we have to format it and show
                    final Date positionTime = mPositionCursorData.getPositionTime();
                    final TextView dateView = (TextView) view;
                    dateView.setText(Conf.SIMPLE_DATE_FORMAT.format(positionTime));
                    coordinateManaged = false;
                    return true;
                } else if (R.id.fence_position_coordinates == view.getId()) {
                    if (coordinateManaged) {
                        coordinateManaged = true;
                    } else {
                        coordinateManaged = false;
                        // We get the data
                        final float latitude = mPositionCursorData.getLatitude();
                        final float longitude = mPositionCursorData.getLongitude();
                        // We format into the pattern
                        final String positionStr = getActivity().getResources()
                                .getString(R.string.session_position_format,
                                        Location.convert(latitude, Location.FORMAT_DEGREES),
                                        Location.convert(longitude, Location.FORMAT_DEGREES));
                        final TextView locationView = (TextView) view;
                        locationView.setText(positionStr);
                    }
                    return true;
                } else if (R.id.fence_distance == view.getId()) {
                    final float distanceInMeters = mPositionCursorData.getDistance();
                    final TextView distanceView = (TextView) view;
                    distanceView.setText(DistanceUtil.formatDistance(getActivity(), distanceInMeters));
                    return true;
                } else if (R.id.fence_activity == view.getId()) {
                    final int activityType = mPositionCursorData.getActivityType();
                    final TextView activityView = (TextView) view;
                    activityView.setText(ActivityUtil.get(getActivity()).getActivityLabel(activityType));
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragLayout = inflater.inflate(R.layout.fragment_my_move_positions, null);
        // Get the reference to the UI items
        mSessionStartDateTextView = UI.findViewById(fragLayout, R.id.session_info_start_date);
        mSessionEndDateTextView = UI.findViewById(fragLayout, R.id.session_info_end_date);
        mSessionOwnerTextView = UI.findViewById(fragLayout, R.id.session_info_owner);
        // Return the layout
        return fragLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setAdapter(mAdapter);
        getLoaderManager().initLoader(FENCE_SESSION_DATA_LOADER_ID, null, this);
        getLoaderManager().initLoader(FENCE_POSITION_LOADER_ID, null, this);
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case FENCE_POSITION_LOADER_ID: {
                // We create the Uri for all the position of a given session
                final Uri sessionPositionUri = FenceDB.FencePosition.getPositionUriForSession(mSessionId);
                CursorLoader loader = new CursorLoader(getActivity(), sessionPositionUri,
                        null, null, null, FenceDB.FencePosition.POSITION_TIME + " DESC ");
                return loader;
            }
            case FENCE_SESSION_DATA_LOADER_ID: {
                // We create the Uri for all the position of a given session
                final Uri sessionUri = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(mSessionId));
                CursorLoader loader = new CursorLoader(getActivity(), sessionUri,
                        null, null, null, null);
                return loader;
            }
            default:
                throw new IllegalArgumentException("This loader doesn't exist!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        final int loaderId = cursorLoader.getId();
        switch (loaderId) {
            case FENCE_POSITION_LOADER_ID: {
                mPositionCursorData = CursorResolver.CURSOR_RESOLVER.extractPositionCursor(cursor);
                mAdapter.swapCursor(cursor);
                break;
            }
            case FENCE_SESSION_DATA_LOADER_ID: {
                // We show the data for the session
                final FenceCursorFactory.FenceSessionCursorData sessionDataCursor
                        = CursorResolver.CURSOR_RESOLVER.extractSessionCursor(cursor);
                if (sessionDataCursor != null && sessionDataCursor.moveToNext()) {
                    final Date startDate = sessionDataCursor.getStartDate();
                    if (startDate != null) {
                        mSessionStartDateTextView.setText(Conf.SIMPLE_DATE_FORMAT.format(startDate));
                    }
                    final Date endDate = sessionDataCursor.getEndDate();
                    if (endDate != null) {
                        mSessionEndDateTextView.setText(Conf.SIMPLE_DATE_FORMAT.format(endDate));
                    }
                    // The Owner
                    final String owner = sessionDataCursor.getOwner();
                    mSessionOwnerTextView.setText(owner);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("This loader doesn't exist!");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        final int loaderId = cursorLoader.getId();
        if (FENCE_POSITION_LOADER_ID == loaderId) {
            mAdapter.swapCursor(null);
        }
    }


}
