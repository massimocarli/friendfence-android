package uk.co.massimocarli.friendfence;

import java.text.SimpleDateFormat;

/**
 * This is a class that contains some useful constants for the application
 * Created by Massimo Carli on 12/06/14.
 */
public final class Conf {

    /**
     * Private constructor
     */
    private Conf() {
        throw new AssertionError("You should never instantiate this class!!");
    }

    /**
     * The Package for the application
     */
    public static final String PKG = "uk.co.friendfence";

    /**
     * The default user
     */
    public static final String DEFAULT_USER = "defaultUser";

    /**
     * The SimpleDateFormat for the date
     */
    public static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm");

    /**
     * The SenderId for GCM
     */
    public static final String GCM_SENDER_ID = "625172533037";
}
