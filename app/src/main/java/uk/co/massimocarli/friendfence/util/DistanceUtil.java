package uk.co.massimocarli.friendfence.util;

import android.content.Context;

/**
 * Utility class for distance output formatting
 * Created by Massimo Carli on 01/09/14.
 */
public final class DistanceUtil {

    /**
     * Number fo meters in a Km
     */
    private static final int METERS_IN_A_KM = 1000;

    /**
     * Private constructor
     */
    private DistanceUtil() {
        throw new AssertionError("Never call this!!! I'm an Utility class!");
    }

    /**
     * Utility method to format the distance in meters
     *
     * @param context  The Context
     * @param distance The distance in meters
     * @return The distance as a String
     */
    public static String formatDistance(final Context context, final float distance) {
        if (distance < METERS_IN_A_KM) {
            return String.format("%.0f m", distance);
        } else {
            return String.format("%.2f km", (distance / METERS_IN_A_KM));
        }
    }
}
