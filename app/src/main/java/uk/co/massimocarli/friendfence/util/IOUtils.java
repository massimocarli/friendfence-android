package uk.co.massimocarli.friendfence.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Massimo Carli on 13/06/14.
 */
public final class IOUtils {

    /*
     * The default for the buffer size.
     */
    private static final int DEFAULT_BUFFE_SIZE = 2048;

    /*
     * The default encoding we use when not specified
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Private constructor.
     */
    private IOUtils() {
    }

    /**
     * Copies data from an input stream to an output stream, using a standard
     * buffer. Note that the streams are not closed at the end of the copy!.
     *
     * @param in  The input stream.
     * @param out The output stream.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final InputStream in, final OutputStream out)
            throws IOException {
        copy(in, out, DEFAULT_BUFFE_SIZE);
    }

    /**
     * Copies data from an input stream to an output stream, using a buffer of
     * given size Note that the streams are not closed at the end of the copy!.
     *
     * @param in         The input stream.
     * @param out        The output stream.
     * @param bufferSize The buffer size (in bytes).
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final InputStream in, final OutputStream out, final int bufferSize)
            throws IOException {
        byte[] buffer = new byte[bufferSize];
        int read = 0;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Copies data from a reader to a writer, using a standard buffer. Note that
     * the reader nad the writer are not closed at the end of the copy!.
     *
     * @param in  The reader.
     * @param out The writer.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final Reader in, final Writer out) throws IOException {
        copy(in, out, DEFAULT_BUFFE_SIZE);
    }

    /**
     * Copies data from a reader to a writer, using a custom buffer size. Note
     * that the reader nad the writer are not closed at the end of the copy!.
     *
     * @param in         The reader.
     * @param out        The writer.
     * @param bufferSize The buffer size (in bytes).
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final Reader in, final Writer out, final int bufferSize)
            throws IOException {
        char[] buffer = new char[bufferSize];
        int read = 0;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Copies a file input stream to a file output stream using a default buffer
     * (the method is implemented using the NIO api). Note that the streams are
     * not closed at the end of the copy!.
     *
     * @param in  The input stream.
     * @param out The output stream.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final FileInputStream in, final FileOutputStream out)
            throws IOException {
        copy(in, out, DEFAULT_BUFFE_SIZE);
    }

    /**
     * Copies a file input stream to a file output stream using a buffer (the
     * method is implemented using the NIO api). Note that the streams are not
     * closed at the end of the copy!.
     *
     * @param in         The input stream.
     * @param out        The output stream.
     * @param bufferSize The buffer size (in bytes).
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final FileInputStream in, final FileOutputStream out,
                            final int bufferSize) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = in.getChannel();
            outChannel = out.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            while (true) {
                buffer.clear();
                int read = inChannel.read(buffer);
                if (read == -1) {
                    break;
                }
                buffer.flip();
                outChannel.write(buffer);
            }
        } finally {
            closeQuietly(inChannel);
            closeQuietly(outChannel);
        }
    }

    /**
     * Copies data from a file to another, using a default buffer.
     *
     * @param source The source file.
     * @param target The target file.
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final File source, final File target) throws IOException {
        copy(source, target, DEFAULT_BUFFE_SIZE);
    }

    /**
     * Copies data from a file to another, using a buffer.
     *
     * @param source     The source file.
     * @param target     The target file.
     * @param bufferSize The buffer size (in bytes).
     * @throws java.io.IOException If an I/O error occurs.
     */
    public static void copy(final File source, final File target, final int bufferSize)
            throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(target);
            copy(in, out, bufferSize);
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    /**
     * Closes the input/output stream quietly, no matter if it is null or if
     * errors occur closing it.
     *
     * @param stream the input/output stream to close.
     */
    public static void closeQuietly(final Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read characters from an input stream to a string. Note that the input
     * stream is not closed at the end of method!.
     *
     * @param in       The input stream.
     * @param encoding The characters encoding.
     * @return The string.
     * @throws java.io.IOException If an IO error occurs.
     */
    public static String toString(final InputStream in, final String encoding)
            throws IOException {
        InputStreamReader reader = new InputStreamReader(in, encoding);
        StringWriter writer = new StringWriter();
        copy(reader, writer, DEFAULT_BUFFE_SIZE);
        return writer.getBuffer().toString();
    }

    /**
     * Read characters from an input stream to a string. Note that the input
     * stream is not closed at the end of method!.
     *
     * @param in The input stream.
     * @return The string.
     * @throws java.io.IOException If an IO error occurs.
     */
    public static String toString(final InputStream in) throws IOException {
        return toString(in, DEFAULT_ENCODING);
    }

    /**
     * Read data from an InputStream and return them as byte[] using default
     * buffer.
     *
     * @param input The InputStream to read from
     * @return The content of the InputStream as byte[]
     * @throws java.io.IOException In case of error reading from the InputStream
     */
    public static byte[] toByteArray(final InputStream input) throws IOException {
        return toByteArray(input, DEFAULT_BUFFE_SIZE);
    }

    /**
     * Read data from an InputStream and return them as byte[].
     *
     * @param input        The InputStream to read from
     * @param bufferLength The length of the buffer
     * @return The content of the InputStream as byte[]
     * @throws java.io.IOException In case of error reading from the InputStream
     */
    public static byte[] toByteArray(final InputStream input, final int bufferLength)
            throws IOException {
        byte[] buffer = new byte[bufferLength];
        int numRead = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((numRead = input.read(buffer)) != -1) {
            baos.write(buffer, 0, numRead);
        }
        byte[] dataRead = baos.toByteArray();
        baos.close();
        return dataRead;
    }
}
