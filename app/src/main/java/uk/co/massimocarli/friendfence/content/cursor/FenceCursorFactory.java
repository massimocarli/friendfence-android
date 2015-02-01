package uk.co.massimocarli.friendfence.content.cursor;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.os.Build;

import java.util.Date;

import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.geofence.GeofenceData;

/**
 * Created by Massimo Carli on 13/06/14.
 */
public class FenceCursorFactory implements SQLiteDatabase.CursorFactory {

    /**
     * The Interface with the operation that a FenceSessionCursor should have
     */
    public static interface FenceSessionCursorData extends Cursor {

        /**
         * @return The session StartDate
         */
        Date getStartDate();

        /**
         * @return The session EndDate
         */
        Date getEndDate();

        /**
         * @return The id of this session
         */
        long getId();

        /**
         * @return The Owner
         */
        String getOwner();

        /**
         * @return The total distance in meters
         */
        float getTotalDistance();

    }

    /**
     * The Interface with the operation that a FencePositionCursorData should have
     */
    public static interface FencePositionCursorData extends Cursor {

        /**
         * @return The start date
         */
        Date getPositionTime();

        /**
         * @return The id of this position
         */
        long getId();

        /**
         * @return The latitude
         */
        float getLatitude();

        /**
         * @return The longitude
         */
        float getLongitude();

        /**
         * @return The distance in meters
         */
        float getDistance();

        /**
         * @return The value for the Activity type
         */
        int getActivityType();

    }

    /**
     * Implementation of the SQLiteCursor for the FenceSession table for Api Level after Honeycomb
     */
    private static class FenceSessionCursor extends SQLiteCursor implements FenceSessionCursorData {

        /**
         * The SessionCursorDelegate we use for all the fields we need
         */
        private final SessionCursorDelegate mCursorDelegate;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public FenceSessionCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(driver, editTable, query);
            mCursorDelegate = new SessionCursorDelegate(this);
        }

        @Override
        public Date getStartDate() {
            return mCursorDelegate.getStartDate();
        }

        @Override
        public Date getEndDate() {
            return mCursorDelegate.getEndDate();
        }

        @Override
        public long getId() {
            return mCursorDelegate.getId();
        }

        @Override
        public String getOwner() {
            return mCursorDelegate.getOwner();
        }

        @Override
        public float getTotalDistance() {
            return mCursorDelegate.getTotalDistance();
        }
    }

    /**
     * Implementation of the FenceSession for Honeycomb
     */
    private static class HoneyFenceSessionCursor extends SQLiteCursor implements FenceSessionCursorData {

        private final SessionCursorDelegate mCursorDelegate;

        @SuppressWarnings("deprecation")
        public HoneyFenceSessionCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(db, driver, editTable, query);
            mCursorDelegate = new SessionCursorDelegate(this);
        }

        @Override
        public Date getStartDate() {
            return mCursorDelegate.getStartDate();
        }

        @Override
        public Date getEndDate() {
            return mCursorDelegate.getEndDate();
        }

        @Override
        public long getId() {
            return mCursorDelegate.getId();
        }

        @Override
        public String getOwner() {
            return mCursorDelegate.getOwner();
        }

        @Override
        public float getTotalDistance() {
            return mCursorDelegate.getTotalDistance();
        }
    }

    /**
     * An utility class that contains the common things between the different implementations
     * of our Cursor for FenceSession
     */
    private static class SessionCursorDelegate {

        /**
         * The cursor reference
         */
        private final Cursor mCursor;

        /**
         * The index of the _Id column
         */
        private final int mIdIndex;

        /**
         * The index of the startDate column
         */
        private final int mStartDateIndex;

        /**
         * The index of the endDate column
         */
        private final int mEndDateIndex;

        /**
         * The index of the owner column
         */
        private final int mOwnerIndex;

        /**
         * The index of the total distance
         */
        private final int mTotalDistanceIndex;

        /**
         * Create a SessionCursorDelegate for cursor
         *
         * @param cursor The cursor
         */
        private SessionCursorDelegate(final Cursor cursor) {
            mCursor = cursor;
            mIdIndex = cursor.getColumnIndex(FenceDB.FenceSession._ID);
            mStartDateIndex = cursor.getColumnIndex(FenceDB.FenceSession.START_DATE);
            mEndDateIndex = cursor.getColumnIndex(FenceDB.FenceSession.END_DATE);
            mOwnerIndex = cursor.getColumnIndex(FenceDB.FenceSession.SESSION_OWNER);
            mTotalDistanceIndex = cursor.getColumnIndex(FenceDB.FenceSession.TOTAL_DISTANCE);
        }

        /**
         * @return The sessionId
         */
        public long getId() {
            return mCursor.getLong(mIdIndex);
        }

        /**
         * @return The start date
         */
        private Date getStartDate() {
            final long startDate = mCursor.getLong(mStartDateIndex);
            final Date date = new Date();
            date.setTime(startDate);
            return date;
        }

        /**
         * @return The start date
         */
        private Date getEndDate() {
            final long startDate = mCursor.getLong(mEndDateIndex);
            if (startDate > 0) {
                final Date date = new Date();
                date.setTime(startDate);
                return date;
            } else {
                return null;
            }
        }

        /**
         * @return The Owner of the session
         */
        public String getOwner() {
            return mCursor.getString(mOwnerIndex);
        }

        /**
         * @return The total distance
         */
        public float getTotalDistance() {
            return mCursor.getFloat(mTotalDistanceIndex);
        }
    }

    /**
     * Implementation of the SQLiteCursor for the FencePosition table for Api Level after Honeycomb
     */
    private static class FencePositionCursor extends SQLiteCursor implements FencePositionCursorData {

        /**
         * The SessionCursorDelegate we use for all the fields we need
         */
        private final PositionCursorDelegate mCursorDelegate;

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public FencePositionCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(driver, editTable, query);
            mCursorDelegate = new PositionCursorDelegate(this);
        }

        @Override
        public Date getPositionTime() {
            return mCursorDelegate.getPositionTime();
        }

        @Override
        public long getId() {
            return mCursorDelegate.getId();
        }

        @Override
        public float getLatitude() {
            return mCursorDelegate.getLatitude();
        }

        @Override
        public float getLongitude() {
            return mCursorDelegate.getLongitude();
        }

        @Override
        public float getDistance() {
            return mCursorDelegate.getDistance();
        }

        @Override
        public int getActivityType() {
            return mCursorDelegate.getActivityType();
        }
    }

    private static class HoneyFencePositionCursor extends SQLiteCursor implements FencePositionCursorData {

        /**
         * The SessionCursorDelegate we use for all the fields we need
         */
        private final PositionCursorDelegate mCursorDelegate;

        @SuppressWarnings("deprecation")
        public HoneyFencePositionCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(db, driver, editTable, query);
            mCursorDelegate = new PositionCursorDelegate(this);
        }

        @Override
        public Date getPositionTime() {
            return mCursorDelegate.getPositionTime();
        }

        @Override
        public long getId() {
            return mCursorDelegate.getId();
        }

        @Override
        public float getLatitude() {
            return mCursorDelegate.getLatitude();
        }

        @Override
        public float getLongitude() {
            return mCursorDelegate.getLongitude();
        }

        @Override
        public float getDistance() {
            return mCursorDelegate.getDistance();
        }

        @Override
        public int getActivityType() {
            return mCursorDelegate.getActivityType();
        }
    }

    @Override
    public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
        // We have to check the current Android version (I hate this!!)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (FenceDB.FenceSession.TABLE_NAME.equals(editTable)) {
                return new FenceSessionCursor(masterQuery, editTable, query);
            } else if (FenceDB.FencePosition.TABLE_NAME.equals(editTable)) {
                return new FencePositionCursor(masterQuery, editTable, query);
            } else if (FenceDB.Geofence.TABLE_NAME.equals(editTable)) {
                return new GeofenceCursor(masterQuery, editTable, query);
            }
        } else {
            if (FenceDB.FenceSession.TABLE_NAME.equals(editTable)) {
                return new HoneyFencePositionCursor(db, masterQuery, editTable, query);
            } else if (FenceDB.FencePosition.TABLE_NAME.equals(editTable)) {
                return new HoneyFencePositionCursor(db, masterQuery, editTable, query);
            } else if (FenceDB.Geofence.TABLE_NAME.equals(editTable)) {
                return new HoneyGeofenceCursor(db, masterQuery, editTable, query);
            }
        }
        return null;
    }

    /**
     * An utility class that contains the common things between the different implementations
     * of our Cursor for FencePosition
     */
    private static class PositionCursorDelegate {

        /**
         * The cursor reference
         */
        private final Cursor mCursor;

        /**
         * The index of the _Id column
         */
        private final int mIdIndex;

        /**
         * The index of the Position time column
         */
        private final int mPositionTimeIndex;

        /**
         * The index of the latitude column
         */
        private final int mLatitudeIndex;

        /**
         * The index of the longitude column
         */
        private final int mLongitudeIndex;

        /**
         * The index of the distance column
         */
        private final int mDistanceIndex;

        /**
         * The index of the activity type
         */
        private final int mActivityIndex;

        /**
         * Create a SessionCursorDelegate for cursor
         *
         * @param cursor The cursor
         */
        private PositionCursorDelegate(final Cursor cursor) {
            mCursor = cursor;
            mPositionTimeIndex = cursor.getColumnIndex(FenceDB.FencePosition.POSITION_TIME);
            mIdIndex = cursor.getColumnIndex(FenceDB.FencePosition._ID);
            mLatitudeIndex = cursor.getColumnIndex(FenceDB.FencePosition.LATITUDE);
            mLongitudeIndex = cursor.getColumnIndex(FenceDB.FencePosition.LONGITUDE);
            mDistanceIndex = cursor.getColumnIndex(FenceDB.FencePosition.DISTANCE);
            mActivityIndex = cursor.getColumnIndex(FenceDB.FencePosition.ACTIVITY);
        }

        /**
         * @return The sessionId
         */
        public long getId() {
            return mCursor.getLong(mIdIndex);
        }

        /**
         * @return The start date
         */
        private Date getPositionTime() {
            final long startDate = mCursor.getLong(mPositionTimeIndex);
            final Date date = new Date();
            date.setTime(startDate);
            return date;
        }

        /**
         * @return The latitude
         */
        private float getLatitude() {
            return mCursor.getFloat(mLatitudeIndex);
        }

        /**
         * @return The longitude
         */
        private float getLongitude() {
            return mCursor.getFloat(mLongitudeIndex);
        }

        /**
         * @return The current distance
         */
        private float getDistance() {
            return mCursor.getFloat(mDistanceIndex);
        }

        /**
         * @return The activity type
         */
        private int getActivityType() {
            return mCursor.getInt(mActivityIndex);
        }
    }

    /**
     * The Interface with the operation that a GeofenceCursorData should have
     */
    public static interface GeofenceCursorData extends Cursor {

        /**
         * @return The Geofence object
         */
        GeofenceData getGeofence();

    }

    /**
     * Implementation of the SQLiteCursor for the Geofence table for Api Level after Honeycomb
     */
    private static class GeofenceCursor extends SQLiteCursor implements GeofenceCursorData {

        /**
         * The SessionCursorDelegate we use for all the fields we need
         */
        private final GeofenceCursorDelegate mCursorDelegate;


        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public GeofenceCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(driver, editTable, query);
            mCursorDelegate = new GeofenceCursorDelegate(this);
        }


        @Override
        public GeofenceData getGeofence() {
            return mCursorDelegate.getGeofence();
        }
    }

    /**
     * Implementation of the SQLiteCursor for the Geofence table for Api Level for Honeycomb
     */
    private static class HoneyGeofenceCursor extends SQLiteCursor implements GeofenceCursorData {

        /**
         * The GeofenceCursorDelegate we use for all the fields we need
         */
        private final GeofenceCursorDelegate mCursorDelegate;

        @SuppressWarnings("deprecation")
        public HoneyGeofenceCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
            super(db, driver, editTable, query);
            mCursorDelegate = new GeofenceCursorDelegate(this);
        }

        @Override
        public GeofenceData getGeofence() {
            return mCursorDelegate.getGeofence();
        }

    }

    /**
     * An utility class that contains the common things between the different implementations
     * of our Cursor for Geofence
     */
    private static class GeofenceCursorDelegate {

        /**
         * The cursor reference
         */
        private final Cursor mCursor;

        /**
         * Index for the fence_id
         */
        private final int mIdIndex;

        /**
         * Index for the latitude
         */
        private final int mLatitudeIndex;

        /**
         * Index for the longitude
         */
        private final int mLongitudeIndex;

        /**
         * Index for the radius
         */
        private final int mRadiusIndex;

        /**
         * Index for the duration
         */
        private final int mDurationIndex;

        /**
         * Index for the transition type
         */
        private final int mTransitionTypeIndex;

        /**
         * Create a SessionCursorDelegate for cursor
         *
         * @param cursor The cursor
         */
        private GeofenceCursorDelegate(final Cursor cursor) {
            mCursor = cursor;
            mIdIndex = cursor.getColumnIndex(FenceDB.Geofence.FENCE_ID);
            mLatitudeIndex = cursor.getColumnIndex(FenceDB.Geofence.LATITUDE);
            mLongitudeIndex = cursor.getColumnIndex(FenceDB.Geofence.LONGITUDE);
            mRadiusIndex = cursor.getColumnIndex(FenceDB.Geofence.RADIUS);
            mDurationIndex = cursor.getColumnIndex(FenceDB.Geofence.DURATION);
            mTransitionTypeIndex = cursor.getColumnIndex(FenceDB.Geofence.TRANSITION_TYPE);
        }

        public GeofenceData getGeofence() {
            // We create the GeofenceData
            return GeofenceData.create(mCursor.getString(mIdIndex),
                    mCursor.getFloat(mLatitudeIndex),
                    mCursor.getFloat(mLongitudeIndex))
                    .withExpirationDuration(mCursor.getLong(mDurationIndex))
                    .withTransitionType(mCursor.getInt(mTransitionTypeIndex));
        }


    }


}