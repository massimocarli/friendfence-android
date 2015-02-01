package uk.co.massimocarli.friendfence.geofence;

import com.google.android.gms.location.Geofence;

/**
 * Created by Massimo Carli on 25/09/14.
 */
public final class GeofenceData {

    /**
     * The Id of the fence
     */
    private final String mRequestId;

    /**
     * The type of the transition
     */
    private int mTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER;

    /**
     * The Latitude of the geofence region
     */
    private final double mLatitude;

    /**
     * The longitude of the Geofence region
     */
    private final double mLongitude;

    /**
     * The Radius of the geofence region (default 500 meters)
     */
    private int mRadius = 500;

    /**
     * The duration in milliseconds (default  1 day)
     */
    private long mDuration = 24 * 60 * 60 * 1000L;

    /**
     * The loitering delay (default 1 hour). This is the time that the user should
     * spend into a Geofence
     */
    private int mLoiteringDelay = 60 * 60 * 1000;

    /**
     * The time we prefer between the event and the related noticiation (1 min)
     */
    private int mNotificationResponsiveness = 60 * 1000;


    /**
     * Private constructor that creates a GeofenceData with the given id
     *
     * @param requestId The id for this Geofence
     * @param latitude  The latitude of the geofence center
     * @param longitude The longitude of the geofence center
     */
    private GeofenceData(final String requestId, final double latitude, final double longitude) {
        this.mRequestId = requestId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    /**
     * Static Factory method for the GeofenceData
     *
     * @param requestId The id for this Geofence
     * @param latitude  The latitude of the geofence center
     * @param longitude The longitude of the geofence center
     * @return The GeofenceData instance
     */
    public static GeofenceData create(final String requestId, final double latitude, final double longitude) {
        final GeofenceData geofenceData = new GeofenceData(requestId, latitude, longitude);
        return geofenceData;
    }

    /**
     * Se the specific transition type
     *
     * @param transitionType The Transition type
     * @return The instance itself for chaining
     */
    public GeofenceData withTransitionType(final int transitionType) {
        this.mTransitionType = transitionType;
        return this;
    }

    /**
     * Set the duration of the geofence
     *
     * @param duration The duration
     * @return The instance itself for chaining
     */
    public GeofenceData withExpirationDuration(final long duration) {
        this.mDuration = duration;
        return this;
    }

    /**
     * Set the loitering delay of the geofence. This is the time that the user can spend into
     * a geofence without a notification if the transition type is of type dwell
     *
     * @param loiteringDelay The delay for dwell
     * @return The instance itself for chaining
     */
    public GeofenceData withLoiteringDelay(final int loiteringDelay) {
        this.mLoiteringDelay = loiteringDelay;
        return this;
    }

    /**
     * Set the max time between the event and its notification
     *
     * @param notificationResponsiveness The responsiveness
     * @return The instance itself for chaining
     */
    public GeofenceData withNotificationResponsiveness(final int notificationResponsiveness) {
        this.mNotificationResponsiveness = notificationResponsiveness;
        return this;
    }

    /**
     * @return The latitude of the geofence center
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * @return The longitude of the geofence center
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * @return The radius of the Geofence
     */
    public int getRadius() {
        return mRadius;
    }

    /**
     * @return The request id of the Geofence
     */
    public String getRequestId() {
        return mRequestId;
    }

    /**
     * @return The transition type for the geofence
     */
    public int getTransitionType() {
        return mTransitionType;
    }

    /**
     * @return The duration for the Geofence
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * @return The loitering delay
     */
    public int getLoiteringDelay() {
        return mLoiteringDelay;
    }


    /**
     * @return The responsiveness for the notification
     */
    public int getNotificationResponsiveness() {
        return mNotificationResponsiveness;
    }

    /**
     * @return The Geofence object from the data into this item
     */
    public Geofence buildGeofence() {
        final Geofence.Builder builder = new Geofence.Builder();
        builder.setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setRequestId(getRequestId())
                .setExpirationDuration(getDuration())
                .setTransitionTypes(getTransitionType())
                .setNotificationResponsiveness(getNotificationResponsiveness());
        if (getTransitionType() == Geofence.GEOFENCE_TRANSITION_DWELL) {
            builder.setLoiteringDelay(getLoiteringDelay());
        }
        return builder.build();
    }
}
