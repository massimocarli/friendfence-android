package uk.co.massimocarli.friendfence.drive;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.content.cursor.CursorResolver;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;

/**
 * Created by Massimo Carli on 28/09/14.
 */
public final class FenceDriveUtil {

    /**
     * The Pattern we use for the date and time
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);

    /**
     * We define the constants for the Json fields that are not equals to the related
     * columns on the DB
     */
    private static interface JsonProperties {
        String POSITIONS = "positions";
    }

    /**
     * The private constructor
     */
    private FenceDriveUtil() {
        throw new AssertionError("Never instantiate me! I'm an utility class!");
    }

    /**
     * This utility method access the DB for the given session and create a Json file for the
     * related information
     *
     * @param context   The Context
     * @param sessionId The id of the FenceSession
     * @return The Json version of a session
     */
    public static String fenceSessionAsJson(final Context context, final long sessionId) {
        // The return Json
        final JSONObject returnJson = new JSONObject();
        // We read the data for the given session
        final Uri sessionUri = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(sessionId));
        final Cursor cursor = context.getContentResolver().query(sessionUri, null, null, null, null);
        final FenceCursorFactory.FenceSessionCursorData sessionCursor =
                CursorResolver.CURSOR_RESOLVER.extractSessionCursor(cursor);
        if (sessionCursor.moveToNext()) {
            // We read the information related to the current session
            try {
                returnJson.put(FenceDB.FenceSession.SESSION_OWNER, sessionCursor.getOwner());
                returnJson.put(FenceDB.FenceSession.START_DATE, DATE_FORMAT.format(sessionCursor.getStartDate()));
                final Date endDate = sessionCursor.getEndDate();
                if (endDate != null) {
                    returnJson.put(FenceDB.FenceSession.END_DATE, DATE_FORMAT.format(endDate));
                }
                returnJson.put(FenceDB.FenceSession.TOTAL_DISTANCE, sessionCursor.getTotalDistance());
                // Now we have to save the data for the positions
                final JSONArray positions = new JSONArray();
                returnJson.put(JsonProperties.POSITIONS, positions);
                final Uri positionUri = FenceDB.FencePosition.getPositionUriForSession(sessionId);
                final Cursor posCursor = context.getContentResolver().query(positionUri, null, null, null, null);
                final FenceCursorFactory.FencePositionCursorData positionCursor =
                        CursorResolver.CURSOR_RESOLVER.extractPositionCursor(posCursor);
                while (positionCursor.moveToNext()) {
                    // We create the object for the position
                    final JSONObject posObj = new JSONObject();
                    // We fill the data
                    posObj.put(FenceDB.FencePosition.ACTIVITY, positionCursor.getActivityType());
                    posObj.put(FenceDB.FencePosition.LATITUDE, positionCursor.getLatitude());
                    posObj.put(FenceDB.FencePosition.LONGITUDE, positionCursor.getLongitude());
                    posObj.put(FenceDB.FencePosition.DISTANCE, positionCursor.getDistance());
                    posObj.put(FenceDB.FencePosition.POSITION_TIME, DATE_FORMAT.format(positionCursor.getPositionTime()));
                    // We add the object to the array
                    positions.put(posObj);
                }
                posCursor.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return returnJson.toString();
    }

    /**
     * This method returns the name of the session with the provided Id
     *
     * @param context   The Context
     * @param sessionId The id of the FenceSession
     * @return The name of the session
     */
    public static final String getFenceSessionName(final Context context, final long sessionId) {
        String sessionName = null;
        // The return Json
        final JSONObject returnJson = new JSONObject();
        // We read the data for the given session
        final Uri sessionUri = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(sessionId));
        final Cursor cursor = context.getContentResolver().query(sessionUri, null, null, null, null);
        final FenceCursorFactory.FenceSessionCursorData sessionCursor =
                CursorResolver.CURSOR_RESOLVER.extractSessionCursor(cursor);
        if (sessionCursor.moveToNext()) {
            sessionName = sessionCursor.getOwner() + " - " + DATE_FORMAT.format(sessionCursor.getStartDate());
        }
        cursor.close();
        return sessionName;
    }


    /**
     * This method reads the data and import that into the DB
     *
     * @param context  The Context
     * @param jsonData The Json data for the FenceSession
     */
    public static void importFenceSessionJson(final Context context, final String jsonData) throws Exception {
        final JSONObject jsonObject = new JSONObject(jsonData);
        // We create the Session Object
        final ContentValues sessionData = new ContentValues();
        sessionData.put(FenceDB.FenceSession.SESSION_OWNER, jsonObject.optString(FenceDB.FenceSession.SESSION_OWNER));
        sessionData.put(FenceDB.FenceSession.START_DATE, DATE_FORMAT
                .parse(jsonObject.optString(FenceDB.FenceSession.START_DATE)).getTime());
        sessionData.put(FenceDB.FenceSession.END_DATE, DATE_FORMAT
                .parse(jsonObject.optString(FenceDB.FenceSession.END_DATE)).getTime());
        sessionData.put(FenceDB.FenceSession.TOTAL_DISTANCE, jsonObject.optDouble(FenceDB.FenceSession.TOTAL_DISTANCE));
        // We insert the data into the DB
        final Uri insertedSessionUri = context.getContentResolver().insert(FenceDB.FenceSession.CONTENT_URI, sessionData);
        // We get the id
        long newSessionId = FenceDB.FenceSession.getSessionId(insertedSessionUri);
        // We read the positions
        final JSONArray positions = jsonObject.optJSONArray(JsonProperties.POSITIONS);
        if (positions != null) {
            final Uri uriForPositions = FenceDB.FencePosition.getPositionUriForSession(newSessionId);
            final int positionNumber = positions.length();
            final ArrayList<ContentProviderOperation> insertOps = new ArrayList<ContentProviderOperation>(positionNumber);
            for (int i = 0; i < positions.length(); i++) {
                final JSONObject position = positions.optJSONObject(i);
                if (position != null) {
                    // We create the ContentValues
                    final ContentValues positionValues = new ContentValues();
                    positionValues.put(FenceDB.FencePosition.SESSION_ID, newSessionId);
                    positionValues.put(FenceDB.FencePosition.ACTIVITY, position.optInt(FenceDB.FencePosition.ACTIVITY));
                    positionValues.put(FenceDB.FencePosition.LATITUDE, position.optDouble(FenceDB.FencePosition.LATITUDE));
                    positionValues.put(FenceDB.FencePosition.LONGITUDE, position.optDouble(FenceDB.FencePosition.LONGITUDE));
                    positionValues.put(FenceDB.FencePosition.DISTANCE, position.optDouble(FenceDB.FencePosition.DISTANCE));
                    positionValues.put(FenceDB.FencePosition.POSITION_TIME,
                            DATE_FORMAT.parse(position.optString(FenceDB.FencePosition.POSITION_TIME)).getTime());
                    // We create the Insert operations
                    final ContentProviderOperation insertOp = ContentProviderOperation.newInsert(uriForPositions)
                            .withValues(positionValues)
                            .build();
                    // We add the operations to the list
                    insertOps.add(insertOp);
                }
            }
            // We execute the batch
            context.getContentResolver().applyBatch(FenceDB.AUTHORITY, insertOps);
        }


    }

}

