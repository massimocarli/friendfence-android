package uk.co.massimocarli.friendfence.content;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Date;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.geofence.GeofenceData;

/**
 * This is the interface we use to manage constants related to the FriendFence DB for our
 * ContentProvider
 * Created by Massimo Carli on 12/06/14.
 */
public final class FenceDB {

    /**
     * This is the name of the DB for our ContentProvider
     */
    public static final String DB_NAME = "FriendFence";

    /**
     * This is the version of the DB
     */
    public static final int DB_VERSION = 2;

    /**
     * This is the name of the Authority for the ContentProvider. We'll use this to manage
     * the related Uri
     */
    public static final String AUTHORITY = Conf.PKG + ".authority.friendfence";


    /**
     * The string for the mime types specific of our application
     */
    private static final String MIME_PART = Conf.PKG + "/vnd.friendfence.";


    /**
     * Private constructor
     */
    private FenceDB() {
        throw new AssertionError("You should never instantiate this class!!");
    }

    /**
     * This describes the entity we use for every session that is the trace between a start
     * and a stop
     */
    public static class FenceSession implements BaseColumns {

        /**
         * This is the name of the Table we use for this entity
         */
        public static final String TABLE_NAME = "FenceSession";

        /**
         * The Path we'll use to manage sessions
         */
        public static final String PATH = "session";

        /**
         * The Uri for the Content in Content Provider.
         */
        public static final Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
                + AUTHORITY + "/" + PATH);

        /**
         * The MimeType for the single Item.
         */
        public static final String CURSOR_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_PART + PATH;

        /**
         * The MimeType for the list of Items.
         */
        public static final String CURSOR_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_PART + PATH;

        /**
         * The user for this session
         */
        public static final String SESSION_OWNER = "session_owner";

        /**
         * The start date for the session (timestamp)
         */
        public static final String START_DATE = "start_date";

        /**
         * The end date for the session (timestamp)
         */
        public static final String END_DATE = "end_date";

        /**
         * The total distance in meters (long)
         */
        public static final String TOTAL_DISTANCE = "totalDistance";

        /**
         * This is the utility method that creates a new FenceSession
         *
         * @param context The Context
         * @return The Uri of the new FenceSession
         */
        public static Uri createNewSession(final Context context, final String owner) {
            final ContentValues values = new ContentValues();
            // The owner information
            values.put(FenceSession.SESSION_OWNER, owner);
            // We insert the data for the startDate
            final Date now = new Date();
            values.put(FenceSession.START_DATE, now.getTime());
            // We insert the data
            final Uri newSessionUri = context.getContentResolver().insert(FenceSession.CONTENT_URI, values);
            // We return the Uri
            return newSessionUri;
        }

        /**
         * This is the utility method that set a session as closed updating the closing date
         *
         * @param context   The Context
         * @param sessionId The id of the session to close
         */
        public static void setSessionAsClosed(final Context context, final long sessionId) {
            final ContentValues values = new ContentValues();
            // We insert the data for the startDate
            final Date now = new Date();
            values.put(FenceSession.END_DATE, now.getTime());
            // We insert the data
            final Uri sessionUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(sessionId));
            // We update the session
            AsyncQueryHandler asyncQueryHandler = new AsyncQueryHandler(context.getContentResolver()) {
                @Override
                protected void onUpdateComplete(int token, Object cookie, int result) {
                    super.onUpdateComplete(token, cookie, result);
                    // Nothing to do
                }
            };
            asyncQueryHandler.startUpdate(0, null, sessionUri, values, null, null);
        }

        /**
         * Utility method that extract the sessionId from the related Uri
         *
         * @param sessionUri The sessionUri
         * @return The id of the session
         */
        public static long getSessionId(final Uri sessionUri) {
            // We extract the sessionId from the uri of the type AUTHORITY/session/<sessionId>
            final String sessionIdAsString = sessionUri.getPathSegments().get(1);
            return Long.parseLong(sessionIdAsString);
        }

    }

    /**
     * This describes the entity for position of a given session
     */
    public static class FencePosition implements BaseColumns {

        /**
         * This is the name of the Table we use for this entity
         */
        public static final String TABLE_NAME = "FencePosition";

        /**
         * The Path we'll use to manage position for a given session
         */
        public static final String PATH = "position";

        /**
         * The MimeType for the single Item.
         */
        public static final String CURSOR_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_PART + PATH;

        /**
         * The MimeType for the list of Items.
         */
        public static final String CURSOR_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_PART + PATH;

        /**
         * The reference to the session (numeric)
         */
        public static final String SESSION_ID = "session_id";

        /**
         * The time of the measure (timestamp)
         */
        public static final String POSITION_TIME = "position_time";

        /**
         * The latitude (real)
         */
        public static final String LATITUDE = "latitude";

        /**
         * The longitude (real)
         */
        public static final String LONGITUDE = "longitude";

        /**
         * The altitude (real)
         */
        public static final String ALTITUDE = "altitude";

        /**
         * The distance until here (real)
         */
        public static final String DISTANCE = "distance";

        /**
         * The activity recognition (integer)
         */
        public static final String ACTIVITY = "activity";

        /**
         * This is the static Factory Method for the Uri related to all the position of a given session
         *
         * @param sessionId The id of the session to consider
         * @return The Uri of all the
         */
        public static Uri getPositionUriForSession(final long sessionId) {
            final String uriPath = new StringBuilder().append(sessionId).append("/")
                    .append(FencePosition.PATH).toString();
            return Uri.withAppendedPath(FenceSession.CONTENT_URI, uriPath);
        }

        /**
         * This is the static Factory Method for the Uri related to all the position of a given session
         *
         * @param sessionId  The id of the session to consider
         * @param positionId The id of the given position
         * @return The Uri of all the
         */
        public static Uri getPositionUri(final long sessionId, final int positionId) {
            final String uriPath = new StringBuilder().append(sessionId).append("/")
                    .append(FencePosition.PATH).append("/")
                    .append(positionId).toString();
            return Uri.withAppendedPath(FenceSession.CONTENT_URI, uriPath);
        }

    }


    /**
     * This describes the entity for the Geofence
     */
    public static class Geofence implements BaseColumns {

        /**
         * This is the name of the Table we use for this entity
         */
        public static final String TABLE_NAME = "Geofence";

        /**
         * The Path we'll use to manage Geofence
         */
        public static final String PATH = "geofence";

        /**
         * The Uri for the Content in Content Provider.
         */
        public static final Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://"
                + AUTHORITY + "/" + PATH);

        /**
         * The MimeType for the single Item.
         */
        public static final String CURSOR_ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_PART + PATH;

        /**
         * The MimeType for the list of Items.
         */
        public static final String CURSOR_DIR_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_PART + PATH;

        /**
         * The identifier of the Geofence
         */
        public static final String FENCE_ID = "fence_id";

        /**
         * The latitude (real)
         */
        public static final String LATITUDE = "latitude";

        /**
         * The longitude (real)
         */
        public static final String LONGITUDE = "longitude";

        /**
         * The radius (real)
         */
        public static final String RADIUS = "radius";

        /**
         * The duration of the geofence (integer)
         */
        public static final String DURATION = "duration";

        /**
         * The transition_type of the geofence (integer)
         */
        public static final String TRANSITION_TYPE = "transition_type";

        /**
         * This is an utility method that saves the content of a Geofence
         *
         * @param context      The Context
         * @param geofenceData The data of the Geofence to save into the ContentProvider
         * @return The Uri of the new created item in the ContentProvider
         */
        public static Uri save(final Context context, final GeofenceData geofenceData) {
            // We create the ContentValues from the geoFence
            final ContentValues values = new ContentValues();
            values.put(Geofence.FENCE_ID, geofenceData.getRequestId());
            values.put(Geofence.DURATION, geofenceData.getDuration());
            values.put(Geofence.LATITUDE, geofenceData.getLatitude());
            values.put(Geofence.LONGITUDE, geofenceData.getLongitude());
            values.put(Geofence.RADIUS, geofenceData.getRadius());
            values.put(Geofence.TRANSITION_TYPE, geofenceData.getTransitionType());
            // We save into the DB
            Uri newGeofenceUri = context.getContentResolver().insert(Geofence.CONTENT_URI, values);
            return newGeofenceUri;
        }

    }

}
