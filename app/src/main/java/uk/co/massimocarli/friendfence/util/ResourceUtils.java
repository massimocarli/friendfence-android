package uk.co.massimocarli.friendfence.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Massimo Carli on 13/06/14.
 */
public final class ResourceUtils {

    /**
     * The default encoding for the files
     */
    private static final String DEFAULT_ENCODING = "UTF8";

    /*
     * Private constructor
     */
    private ResourceUtils() {
        throw new AssertionError("Never call this!!!");
    }

    /**
     * Return the content of a raw resource as a String given the encoding
     *
     * @param context  The Context reference
     * @param encoding The encoding to use for reading
     * @param rawId    The id for the raw resource
     * @return The raw Resource as a String
     * @throws java.io.IOException                  In case of error reading from the Stream
     * @throws java.io.UnsupportedEncodingException In case of wrong encoding
     */
    public static String getRawAsString(Context context, String encoding, int rawId) throws UnsupportedEncodingException, IOException {
        InputStream is = context.getResources().openRawResource(rawId);
        String result = IOUtils.toString(is, encoding);
        return result;
    }

    /**
     * Return the content of an assets file as a String given the encoding
     *
     * @param context    The Context reference
     * @param encoding   The encoding to use for reading
     * @param assetsFile The file of the assets to read
     * @return The raw Resource as a String
     * @throws java.io.IOException                  In case of error reading from the Stream
     * @throws java.io.UnsupportedEncodingException In case of wrong encoding
     */
    public static String getAssetsAsString(Context context, String encoding, String assetsFile) throws UnsupportedEncodingException, IOException {
        InputStream is = context.getAssets().open(assetsFile);
        String result = IOUtils.toString(is, encoding);
        return result;
    }

    /**
     * Return the content of a raw resource as a String using UTF-8 encoding
     *
     * @param context The Context reference
     * @param rawId   The id for the raw resource
     * @return The raw Resource as a String
     * @throws java.io.IOException                  In case of error reading from the Stream
     * @throws java.io.UnsupportedEncodingException In case of wrong encoding
     */
    public static String getRawAsString(Context context, int rawId) throws UnsupportedEncodingException, IOException {
        return getRawAsString(context, DEFAULT_ENCODING, rawId);
    }

    /**
     * Return the content of a raw resource as a String using UTF-8 encoding
     *
     * @param context    The Context reference
     * @param assetsFile The file of the assets to read
     * @return The raw Resource as a String
     * @throws java.io.IOException                  In case of error reading from the Stream
     * @throws java.io.UnsupportedEncodingException In case of wrong encoding
     */
    public static String getAssetsAsString(Context context, String assetsFile) throws UnsupportedEncodingException, IOException {
        return getAssetsAsString(context, DEFAULT_ENCODING, assetsFile);
    }

    /**
     * Return the content of a raw resource as a String given the encoding. The String can contains
     * placeholders as a printf
     *
     * @param context  The Context reference
     * @param encoding The encoding to use for reading
     * @param rawId    The id for the raw resource
     * @param args     The args if present
     * @return The raw Resource as a String
     * @throws java.io.IOException                  In case of error reading from the Stream
     * @throws java.io.UnsupportedEncodingException In case of wrong encoding
     */
    public static String getRawAsEvaluatedFormat(Context context, String encoding, int rawId, Object... args) throws UnsupportedEncodingException, IOException {
        String strFormat = getRawAsString(context, encoding, rawId);
        if (strFormat != null) {
            return String.format(strFormat, args);
        } else {
            return null;
        }
    }

    /**
     * Return the content of a raw resource as a String as URF-8 encoding. The String can contains
     * placeholders as a printf
     *
     * @param context The Context reference
     * @param rawId   The id for the raw resource
     * @param args    The args if present
     * @return The raw Resource as a String
     * @throws java.io.IOException                  In case of error reading from the Stream
     * @throws java.io.UnsupportedEncodingException In case of wrong encoding
     */
    public static String getRawAsEvaluatedFormat(Context context, int rawId, Object... args) throws UnsupportedEncodingException, IOException {
        return getRawAsEvaluatedFormat(context, DEFAULT_ENCODING, rawId, args);
    }

}
