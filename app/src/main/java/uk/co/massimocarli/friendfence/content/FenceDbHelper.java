package uk.co.massimocarli.friendfence.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;
import uk.co.massimocarli.friendfence.util.ResourceUtils;


/**
 * Created by Massimo Carli on 13/06/14.
 */
public class FenceDbHelper extends SQLiteOpenHelper {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = FenceDbHelper.class.getName();

    /**
     * The Reference to the Context
     */
    private final Context mContext;

    /**
     * Create a SQLiteOpenHelper for the FenceDB to manage its lifecycle
     *
     * @param context The Context
     */
    public FenceDbHelper(Context context) {
        super(context, FenceDB.DB_NAME, new FenceCursorFactory(), FenceDB.DB_VERSION);
        this.mContext = context;
    }

    @Override
    public final void onCreate(final SQLiteDatabase db) {
        try {
            db.beginTransaction();
            // We create the tables
            final String createSessionSql = ResourceUtils.getRawAsString(mContext, R.raw.create_session_table);
            db.execSQL(createSessionSql);
            final String createPositionSql = ResourceUtils.getRawAsString(mContext, R.raw.create_position_table);
            db.execSQL(createPositionSql);
            final String createGeofenceSql = ResourceUtils.getRawAsString(mContext, R.raw.create_geofence_table);
            db.execSQL(createGeofenceSql);
            db.setTransactionSuccessful();
            Log.i(TAG_LOG, FenceDB.DB_NAME + " Successfully created for version " + FenceDB.DB_VERSION);
        } catch (Exception e) {
            Log.e(TAG_LOG, "Error creating DB " + FenceDB.DB_NAME, e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        try {
            db.beginTransaction();
            // We drop the tables
            final String dropPositionSql = ResourceUtils.getRawAsString(mContext, R.raw.drop_position_table);
            db.execSQL(dropPositionSql);
            final String dropSessionSql = ResourceUtils.getRawAsString(mContext, R.raw.drop_session_table);
            db.execSQL(dropSessionSql);
            final String dropGeofenceSql = ResourceUtils.getRawAsString(mContext, R.raw.drop_geofence_table);
            db.execSQL(dropGeofenceSql);
            db.setTransactionSuccessful();
            Log.i(TAG_LOG, FenceDB.DB_NAME + " Successfully created for version " + FenceDB.DB_VERSION);
        } catch (Exception e) {
            Log.e(TAG_LOG, "Error creating DB " + FenceDB.DB_NAME, e);
        } finally {
            db.endTransaction();
        }
        onCreate(db);
    }
}
