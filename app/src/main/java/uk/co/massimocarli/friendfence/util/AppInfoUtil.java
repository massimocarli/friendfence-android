package uk.co.massimocarli.friendfence.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

/**
 * This is an  utility class that contains some method for getting information
 * about the app
 * <p/>
 * Created by Massimo Carli on 11/10/14.
 */
public final class AppInfoUtil {

    /**
     * The Tag for the Log
     */
    private static final String TAG_LOG = AppInfoUtil.class.getName();

    /**
     * Private constructor
     */
    private AppInfoUtil() {
        throw new AssertionError("Never invoke me! I'm an utility class!");
    }


    /**
     * This method returns the version of this app
     *
     * @param context The Context
     * @return The version of the app
     */
    public static int getAppVersion(final Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // If our app is executing this should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    /**
     * Utility method that returns the Device Id
     *
     * @param context The Context
     * @return The id of the device
     */
    public static String getDeviceId(final Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }


}
