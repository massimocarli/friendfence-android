package uk.co.massimocarli.friendfence.content;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * The ContentProvider for the FriendFence application
 * Created by Massimo Carli on 12/06/14.
 */
public class FenceContentProvider extends ContentProvider {

    /**
     * The Tag for the Log
     */
    private final static String TAG_LOG = FenceContentProvider.class.getName();

    /**
     * The UriMatcher to match Uri for the given data
     */
    private final static UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private final static int SESSION_DIR_INDICATOR = 1;
    private final static int SESSION_ITEM_INDICATOR = 2;
    private final static int POSITION_DIR_INDICATOR = 3;
    private final static int POSITION_ITEM_INDICATOR = 4;
    private final static int GEOFENCE_DIR_INDICATOR = 5;
    private final static int GEOFENCE_ITEM_INDICATOR = 6;

    static {
        // The Uri for all the FenceSession is of the type AUTHORITY/session
        URI_MATCHER.addURI(FenceDB.AUTHORITY, FenceDB.FenceSession.PATH, SESSION_DIR_INDICATOR);
        // The Uri for a given session is of the type AUTHORITY/session/<sessionId>
        URI_MATCHER.addURI(FenceDB.AUTHORITY, FenceDB.FenceSession.PATH + "/#", SESSION_ITEM_INDICATOR);
        // The Uri for all the position of a given session is of the type
        // AUTHORITY/session/<sessionId>/position
        URI_MATCHER.addURI(FenceDB.AUTHORITY, FenceDB.FenceSession.PATH + "/#/" +
                FenceDB.FencePosition.PATH, POSITION_DIR_INDICATOR);
        // The Uri for a given position of a given session is of the type
        // AUTHORITY/session/<sessionId>/position/<positionId>
        URI_MATCHER.addURI(FenceDB.AUTHORITY, FenceDB.FenceSession.PATH + "/#/" +
                FenceDB.FencePosition.PATH + "/#", POSITION_ITEM_INDICATOR);
        // We manage the Geofence Uri
        URI_MATCHER.addURI(FenceDB.AUTHORITY, FenceDB.Geofence.PATH, GEOFENCE_DIR_INDICATOR);
        URI_MATCHER.addURI(FenceDB.AUTHORITY, FenceDB.Geofence.PATH + "/#", GEOFENCE_ITEM_INDICATOR);

    }

    /**
     * The DbHelper for this ContentProvider
     */
    private FenceDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // We create the DbHelper
        mDbHelper = new FenceDbHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // We detect the uri to manage
        final int uriMatch = URI_MATCHER.match(uri);
        int deletedCount = -1;
        switch (uriMatch) {
            case SESSION_DIR_INDICATOR: {
                // In this case we have to delete all the session with the selection. Before
                // invoke delete we have to get all the information related to the sessionIds
                // because we also have to delete all the related items. We execute everything
                // into a transaction
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    db.beginTransaction();
                    // We get all the session with the given selection
                    final Cursor sessionsToDeleteCursor = db.query(FenceDB.FenceSession.TABLE_NAME,
                            new String[]{FenceDB.FenceSession._ID}, selection, selectionArgs, null, null, null);
                    // We delete all the related FencePosition
                    final int idIndex = sessionsToDeleteCursor.getColumnIndex(FenceDB.FenceSession._ID);
                    final String whereClause = new StringBuilder(FenceDB.FencePosition.SESSION_ID).append("= ?").toString();
                    while (sessionsToDeleteCursor.moveToNext()) {
                        final long sessionId = sessionsToDeleteCursor.getLong(idIndex);
                        final String[] whereArgs = {String.valueOf(sessionId)};
                        db.delete(FenceDB.FencePosition.TABLE_NAME, whereClause, whereArgs);
                    }
                    sessionsToDeleteCursor.close();
                    // Now we can delete the sessions
                    deletedCount = db.delete(FenceDB.FenceSession.TABLE_NAME, selection, selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case SESSION_ITEM_INDICATOR: {
                // In this case the sessionId is into the Uri as path segment
                final String sessionId = uri.getPathSegments().get(1);
                // We have to add this constraint to the one that we receive
                // with the params
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FenceSession._ID)
                        .append(" = ").append(sessionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // In this case we can delete al least 1 session so we try to delete it and if
                // everything is fine we delete the related positions
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    db.beginTransaction();
                    deletedCount = db.delete(FenceDB.FenceSession.TABLE_NAME,
                            where.toString(), selectionArgs);
                    if (deletedCount > 0) {
                        // In this case we have to delete the related position as well
                        final String whereClause = new StringBuilder(FenceDB.FencePosition.SESSION_ID).append("= ?").toString();
                        final String[] whereArgs = {sessionId};
                        db.delete(FenceDB.FencePosition.TABLE_NAME, whereClause, whereArgs);
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            case POSITION_DIR_INDICATOR: {
                // In this case we have to delete all the items in FencePosition for the given session
                // which is contained into the Uri as path element
                final String sessionId = uri.getPathSegments().get(1);
                // As before we have to create the where clause based, this time, on the
                // sessionId field
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FencePosition.SESSION_ID)
                        .append(" = ").append(sessionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We delete using the created where
                deletedCount = mDbHelper.getWritableDatabase().delete(FenceDB.FencePosition.TABLE_NAME,
                        where.toString(), selectionArgs);
                break;
            }
            case POSITION_ITEM_INDICATOR: {
                // In this case we delete a given position for a given session. The id of the position
                // can appear useless but to delete an item we want both the position identifier and the
                // session identifier. We remind that the Uri for a given position is of this form
                // AUTHORITY/session/<sessionId>/position/<positionId>
                // so we need the session as the pahElements in 1 and the positionId as the element in 3
                final String sessionId = uri.getPathSegments().get(1);
                final String positionId = uri.getPathSegments().get(3);
                // We build the query as before using these two informations
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FencePosition.SESSION_ID)
                        .append(" = ").append(sessionId).append(" AND ")
                        .append(FenceDB.FencePosition._ID).append(" = ")
                        .append(positionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We delete using the created where
                deletedCount = mDbHelper.getWritableDatabase().delete(FenceDB.FencePosition.TABLE_NAME,
                        where.toString(), selectionArgs);
                break;
            }
            case GEOFENCE_DIR_INDICATOR: {
                // In this case we have to delete all the items in Geofence
                deletedCount = mDbHelper.getWritableDatabase().delete(FenceDB.Geofence.TABLE_NAME,
                        selection, selectionArgs);
                break;
            }
            case GEOFENCE_ITEM_INDICATOR: {
                final String fenceId = uri.getPathSegments().get(1);
                // We have to add this constraint to the one that we receive
                // with the params
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.Geofence._ID)
                        .append(" = ").append(fenceId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // In this case we can delete al least 1 session so we try to delete it and if
                // everything is fine we delete the related positions
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                try {
                    db.beginTransaction();
                    deletedCount = db.delete(FenceDB.Geofence.TABLE_NAME,
                            where.toString(), selectionArgs);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            }
            default:
                break;
        }
        if (deletedCount >= 0) {
            // We notify the deletion
            getContext().getContentResolver().notifyChange(uri, null);
            // We return the number of deleted items
            return deletedCount;
        } else {
            // It means that the Uri didn't match
            throw new UnsupportedOperationException("The given Uri " + uri + " is not supported");
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SESSION_DIR_INDICATOR:
                return FenceDB.FenceSession.CURSOR_DIR_MIME_TYPE;
            case SESSION_ITEM_INDICATOR:
                return FenceDB.FenceSession.CURSOR_ITEM_MIME_TYPE;
            case POSITION_DIR_INDICATOR:
                return FenceDB.FencePosition.CURSOR_DIR_MIME_TYPE;
            case POSITION_ITEM_INDICATOR:
                return FenceDB.FencePosition.CURSOR_ITEM_MIME_TYPE;
            case GEOFENCE_DIR_INDICATOR:
                return FenceDB.Geofence.CURSOR_DIR_MIME_TYPE;
            case GEOFENCE_ITEM_INDICATOR:
                return FenceDB.Geofence.CURSOR_ITEM_MIME_TYPE;
            default:
                break;
        }
        throw new UnsupportedOperationException("The given Uri " + uri + " is not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // We can only insert into the DIR types
        final int uriMatch = URI_MATCHER.match(uri);
        Uri newItemUri = null;
        switch (uriMatch) {
            case SESSION_DIR_INDICATOR: {
                // In this case we have to simply insert into the Session
                final long newId = mDbHelper.getWritableDatabase().insert(FenceDB.FenceSession.TABLE_NAME,
                        FenceDB.FenceSession.SESSION_OWNER, values);
                newItemUri = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(newId));
                break;
            }
            case POSITION_DIR_INDICATOR: {
                // Here we have to get the sessionId from the Uri
                final int sessionId = Integer.parseInt(uri.getPathSegments().get(1));
                // We add this information to the values
                values.put(FenceDB.FencePosition.SESSION_ID, sessionId);
                // We insert the data into the DB
                final long newId = mDbHelper.getWritableDatabase().insert(FenceDB.FencePosition.TABLE_NAME,
                        FenceDB.FenceSession.SESSION_OWNER, values);
                newItemUri = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(newId));
                break;
            }
            case GEOFENCE_DIR_INDICATOR: {
                // In this case we have to simply insert into the Session
                final long newId = mDbHelper.getWritableDatabase().insert(FenceDB.Geofence.TABLE_NAME,
                        FenceDB.Geofence.FENCE_ID, values);
                newItemUri = Uri.withAppendedPath(FenceDB.Geofence.CONTENT_URI, String.valueOf(newId));
                break;
            }
            default:
                throw new UnsupportedOperationException("The given Uri " + uri + "is not supported");
        }
        // We notify the creation of the entity
        if (newItemUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newItemUri;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // We detect the uri to manage
        final int uriMatch = URI_MATCHER.match(uri);
        Cursor cursor = null;
        switch (uriMatch) {
            case SESSION_DIR_INDICATOR: {
                // In this case we simply execute the given query on the FenceSession Table
                cursor = mDbHelper.getReadableDatabase().query(FenceDB.FenceSession.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case SESSION_ITEM_INDICATOR: {
                // In this case the sessionId is into the Uri as path segment
                final String sessionId = uri.getPathSegments().get(1);
                // We have to add this constraint to the one that we receive
                // with the params
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FenceSession._ID)
                        .append(" = ").append(sessionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We use our constrains for the query
                cursor = mDbHelper.getReadableDatabase().query(FenceDB.FenceSession.TABLE_NAME, projection,
                        where.toString(), selectionArgs, null, null, sortOrder);
                break;
            }
            case POSITION_DIR_INDICATOR: {
                // In this case we have to delete all the items in FencePosition for the given session
                // which is contained into the Uri as path element
                final String sessionId = uri.getPathSegments().get(1);
                // As before we have to create the where clause based, this time, on the
                // sessionId field
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FencePosition.SESSION_ID)
                        .append(" = ").append(sessionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We use our constrains for the query
                cursor = mDbHelper.getReadableDatabase().query(FenceDB.FencePosition.TABLE_NAME, projection,
                        where.toString(), selectionArgs, null, null, sortOrder);
                break;
            }
            case POSITION_ITEM_INDICATOR: {
                // In this case we delete a given position for a given session. The id of the position
                // can appear useless but to delete an item we want both the position identifier and the
                // session identifier. We remind that the Uri for a given position is of this form
                // AUTHORITY/session/<sessionId>/position/<positionId>
                // so we need the session as the pahElements in 1 and the positionId as the element in 3
                final String sessionId = uri.getPathSegments().get(1);
                final String positionId = uri.getPathSegments().get(3);
                // We build the query as before using these two information
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FencePosition.SESSION_ID)
                        .append(" = ").append(sessionId).append(" AND ")
                        .append(FenceDB.FencePosition._ID).append(" = ")
                        .append(positionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We use our constrains for the query
                cursor = mDbHelper.getReadableDatabase().query(FenceDB.FencePosition.TABLE_NAME, projection,
                        where.toString(), selectionArgs, null, null, sortOrder);
                break;
            }
            case GEOFENCE_DIR_INDICATOR: {
                // In this case we simply execute the given query on the Geofence Table
                cursor = mDbHelper.getReadableDatabase().query(FenceDB.Geofence.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case GEOFENCE_ITEM_INDICATOR: {
                // In this case the fenceId is into the Uri as path segment
                final String fenceId = uri.getPathSegments().get(1);
                // We have to add this constraint to the one that we receive
                // with the params
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.Geofence._ID)
                        .append(" = ").append(fenceId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We use our constrains for the query
                cursor = mDbHelper.getReadableDatabase().query(FenceDB.Geofence.TABLE_NAME, projection,
                        where.toString(), selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                break;
        }
        if (cursor != null) {
            // We notify the query on the cursor for the requested Uri
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            // We return the cursor itself
            return cursor;
        } else {
            // It means that the Uri didn't match
            throw new UnsupportedOperationException("The given Uri " + uri + " is not supported");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // We detect the uri to manage
        final int uriMatch = URI_MATCHER.match(uri);
        int updatedCount = -1;
        switch (uriMatch) {
            case SESSION_DIR_INDICATOR: {
                // In this case we have to update using the all session Uri
                updatedCount = mDbHelper.getWritableDatabase().update(FenceDB.FenceSession.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            }
            case SESSION_ITEM_INDICATOR: {
                // In this case the sessionId is into the Uri as path segment
                final String sessionId = uri.getPathSegments().get(1);
                // We have to add this constraint to the one that we receive
                // with the params
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FenceSession._ID)
                        .append(" = ").append(sessionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We update using the created where
                updatedCount = mDbHelper.getWritableDatabase().update(FenceDB.FenceSession.TABLE_NAME,
                        values, where.toString(), selectionArgs);
                break;
            }
            case POSITION_DIR_INDICATOR: {
                // In this case we have to delete all the items in FencePosition for the given session
                // which is contained into the Uri as path element
                final String sessionId = uri.getPathSegments().get(1);
                // As before we have to create the where clause based, this time, on the
                // sessionId field
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FencePosition.SESSION_ID)
                        .append(" = ").append(sessionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We update using the created where
                updatedCount = mDbHelper.getWritableDatabase().update(FenceDB.FencePosition.TABLE_NAME,
                        values, where.toString(), selectionArgs);
                break;
            }
            case POSITION_ITEM_INDICATOR: {
                // In this case we delete a given position for a given session. The id of the position
                // can appear useless but to delete an item we want both the position identifier and the
                // session identifier. We remind that the Uri for a given position is of this form
                // AUTHORITY/session/<sessionId>/position/<positionId>
                // so we need the session as the pahElements in 1 and the positionId as the element in 3
                final String sessionId = uri.getPathSegments().get(1);
                final String positionId = uri.getPathSegments().get(3);
                // We build the query as before using these two information
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.FencePosition.SESSION_ID)
                        .append(" = ").append(sessionId).append(" AND ")
                        .append(FenceDB.FencePosition._ID).append(" = ")
                        .append(positionId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We update using the created where
                updatedCount = mDbHelper.getWritableDatabase().update(FenceDB.FencePosition.TABLE_NAME,
                        values, where.toString(), selectionArgs);
                break;
            }
            case GEOFENCE_DIR_INDICATOR: {
                // In this case we have to update using the all session Uri
                updatedCount = mDbHelper.getWritableDatabase().update(FenceDB.Geofence.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            }
            case GEOFENCE_ITEM_INDICATOR: {
                // In this case the fenceId is into the Uri as path segment
                final String fenceId = uri.getPathSegments().get(1);
                // We have to add this constraint to the one that we receive
                // with the params
                final StringBuilder where = new StringBuilder("( ").append(FenceDB.Geofence._ID)
                        .append(" = ").append(fenceId).append(" ) ");
                if (!TextUtils.isEmpty(selection)) {
                    // If we have to append the filter based on the _ID
                    where.append(" AND ").append(selection);
                }
                // We update using the created where
                updatedCount = mDbHelper.getWritableDatabase().update(FenceDB.Geofence.TABLE_NAME,
                        values, where.toString(), selectionArgs);
                break;
            }
            default:
                break;
        }
        if (updatedCount >= 0) {
            // We notify the deletion
            getContext().getContentResolver().notifyChange(uri, null);
            // We return the number of updated items
            return updatedCount;
        } else {
            // It means that the Uri didn't match
            throw new UnsupportedOperationException("The given Uri " + uri + " is not supported");
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        // We have overridden the applyBatch to improve performances. In this was we can
        // execute them into a transaction
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }


}
