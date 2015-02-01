package uk.co.massimocarli.friendfence.util;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

import uk.co.massimocarli.friendfence.R;

/**
 * Utility class for managing Activity Recognition values
 * <p/>
 * Created by Massimo Carli on 01/09/14.
 */
public final class ActivityUtil {

    /**
     * The (Application)Context
     */
    private final Context mContext;

    /**
     * The Singleton Instance
     */
    private static ActivityUtil sInstance;

    /**
     * The names for the Activity types
     */
    private String[] mActivityNames;

    /**
     * Private constructor
     */
    private ActivityUtil(final Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Static Factory Method to get the ActivityUtil Singleton
     *
     * @param context The Context
     * @return The ActivityUtil Singleton instance
     */
    public synchronized static ActivityUtil get(final Context context) {
        if (sInstance == null) {
            sInstance = new ActivityUtil(context);
        }
        return sInstance;
    }

    /**
     * Returns the name for the given activity type
     *
     * @param activityType The constants for the ActivityType
     * @return The ActivityType as a code
     */
    public synchronized String getActivityLabel(final int activityType) {
        if (mActivityNames == null) {
            mActivityNames = mContext.getResources().getStringArray(R.array.activity_type_names);
        }
        if (activityType >= DetectedActivity.IN_VEHICLE && activityType <= DetectedActivity.RUNNING) {
            return mActivityNames[activityType];
        } else {
            return mActivityNames[DetectedActivity.UNKNOWN];
        }
    }


}
