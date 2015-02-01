package uk.co.massimocarli.friendfence.content.cursor;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Build;

import java.lang.reflect.Field;

/**
 * This is an interface that describes the operation of a CursorResolver that extract a Cursor
 * from a CursorWrapper. In versions previous HONEYCOMB the Cursor is not accessible in a CursorWrapper
 * so we have to use introspection
 * <p/>
 * Created by Massimo Carli on 05/08/14.
 */
public abstract class CursorResolver {

    /**
     * This is the operation that we have to implement to extract the Cursor as FenceSessionCursorData
     * from the given Cursor
     *
     * @param wrapper The CursorWrapper to use
     * @return THe Cursor as FenceSessionCursorData
     */
    public abstract FenceCursorFactory.FenceSessionCursorData extractSessionCursor(Cursor wrapper);

    /**
     * This is the operation that we have to implement to extract the Cursor as FencePositionCursorData
     * from the given Cursor
     *
     * @param wrapper The CursorWrapper to use
     * @return THe Cursor as FencePositionCursorData
     */
    public abstract FenceCursorFactory.FencePositionCursorData extractPositionCursor(Cursor wrapper);

    /**
     * This is the operation that we have to implement to extract the Cursor as GeofenceCursorData
     * from the given Cursor
     *
     * @param wrapper The CursorWrapper to use
     * @return THe Cursor as GeofenceCursorData
     */
    public abstract FenceCursorFactory.GeofenceCursorData extractGeofenceCursor(Cursor wrapper);


    /**
     * The normal CursorResolver for version after HONEYCOMB
     */
    private static CursorResolver DEFAULT_CURSOR_RESOLVER = new CursorResolver() {


        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public FenceCursorFactory.FenceSessionCursorData extractSessionCursor(Cursor wrapper) {
            if (wrapper == null) {
                return null;
            }
            if (wrapper instanceof FenceCursorFactory.FenceSessionCursorData) {
                return (FenceCursorFactory.FenceSessionCursorData) wrapper;
            }
            if (wrapper instanceof CursorWrapper) {
                final CursorWrapper cursorWrapper = (CursorWrapper) wrapper;
                // In this case we simply invoke the getWrappedCursor() method
                final Cursor wrappedCursor = cursorWrapper.getWrappedCursor();
                if (wrappedCursor instanceof FenceCursorFactory.FenceSessionCursorData) {
                    return (FenceCursorFactory.FenceSessionCursorData) wrappedCursor;
                } else {
                    throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FenceSessionCursorData!!");
                }
            } else {
                throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FenceSessionCursorData!!");
            }
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public FenceCursorFactory.FencePositionCursorData extractPositionCursor(Cursor wrapper) {
            if (wrapper == null) {
                return null;
            }
            if (wrapper instanceof FenceCursorFactory.FencePositionCursorData) {
                return (FenceCursorFactory.FencePositionCursorData) wrapper;
            }
            if (wrapper instanceof CursorWrapper) {
                final CursorWrapper cursorWrapper = (CursorWrapper) wrapper;
                // In this case we simply invoke the getWrappedCursor() method
                final Cursor wrappedCursor = cursorWrapper.getWrappedCursor();
                if (wrappedCursor instanceof FenceCursorFactory.FencePositionCursorData) {
                    return (FenceCursorFactory.FencePositionCursorData) wrappedCursor;
                } else {
                    throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FencePositionCursorData!!");
                }
            } else {
                throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FencePositionCursorData!!");
            }
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public FenceCursorFactory.GeofenceCursorData extractGeofenceCursor(Cursor wrapper) {
            if (wrapper == null) {
                return null;
            }
            if (wrapper instanceof FenceCursorFactory.GeofenceCursorData) {
                return (FenceCursorFactory.GeofenceCursorData) wrapper;
            }
            if (wrapper instanceof CursorWrapper) {
                final CursorWrapper cursorWrapper = (CursorWrapper) wrapper;
                // In this case we simply invoke the getWrappedCursor() method
                final Cursor wrappedCursor = cursorWrapper.getWrappedCursor();
                if (wrappedCursor instanceof FenceCursorFactory.GeofenceCursorData) {
                    return (FenceCursorFactory.GeofenceCursorData) wrappedCursor;
                } else {
                    throw new IllegalArgumentException("The given CursorWrapper doesn't contain a GeofenceCursorData!!");
                }
            } else {
                throw new IllegalArgumentException("The given CursorWrapper doesn't contain a GeofenceCursorData!!");
            }
        }
    };


    /**
     * The normal CursorResolver for version after HONEYCOMB
     */
    private static CursorResolver PRE_HONEY_CURSOR_RESOLVER = new CursorResolver() {

        /**
         * The Cursor field
         */
        private Field mCursorField;

        {
            // We access the inner cursor using introspection
            Class<CursorWrapper> clazz = CursorWrapper.class;
            try {
                this.mCursorField = clazz.getDeclaredField("mCursor");
            } catch (Exception e) {
                e.printStackTrace();
                this.mCursorField = null;
            }
        }

        @Override
        public FenceCursorFactory.FenceSessionCursorData extractSessionCursor(Cursor wrapper) {
            if (wrapper == null) {
                return null;
            }
            if (wrapper instanceof FenceCursorFactory.FenceSessionCursorData) {
                return (FenceCursorFactory.FenceSessionCursorData) wrapper;
            }
            if (!(wrapper instanceof CursorWrapper)) {
                throw new IllegalArgumentException("The given Cursor is not a CursorWrapper!!");
            }
            FenceCursorFactory.FenceSessionCursorData returnCursor = null;
            if (mCursorField != null) {
                try {
                    mCursorField.setAccessible(true);
                    final Cursor localCursor = (Cursor) mCursorField.get(wrapper);
                    if (localCursor instanceof FenceCursorFactory.FenceSessionCursorData) {
                        returnCursor = (FenceCursorFactory.FenceSessionCursorData) localCursor;
                    } else {
                        throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FenceSessionCursorData!!");
                    }
                    mCursorField.setAccessible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return returnCursor;
        }

        @Override
        public FenceCursorFactory.FencePositionCursorData extractPositionCursor(Cursor wrapper) {
            if (wrapper == null) {
                return null;
            }
            if (wrapper instanceof FenceCursorFactory.FencePositionCursorData) {
                return (FenceCursorFactory.FencePositionCursorData) wrapper;
            }
            if (!(wrapper instanceof CursorWrapper)) {
                throw new IllegalArgumentException("The given Cursor is not a CursorWrapper!!");
            }
            FenceCursorFactory.FencePositionCursorData returnCursor = null;
            if (mCursorField != null) {
                try {
                    mCursorField.setAccessible(true);
                    final Cursor localCursor = (Cursor) mCursorField.get(wrapper);
                    if (localCursor instanceof FenceCursorFactory.FencePositionCursorData) {
                        returnCursor = (FenceCursorFactory.FencePositionCursorData) localCursor;
                    } else {
                        throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FencePositionCursorData!!");
                    }
                    mCursorField.setAccessible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return returnCursor;
        }

        @Override
        public FenceCursorFactory.GeofenceCursorData extractGeofenceCursor(Cursor wrapper) {
            if (wrapper == null) {
                return null;
            }
            if (wrapper instanceof FenceCursorFactory.GeofenceCursorData) {
                return (FenceCursorFactory.GeofenceCursorData) wrapper;
            }
            if (!(wrapper instanceof CursorWrapper)) {
                throw new IllegalArgumentException("The given Cursor is not a CursorWrapper!!");
            }
            FenceCursorFactory.GeofenceCursorData returnCursor = null;
            if (mCursorField != null) {
                try {
                    mCursorField.setAccessible(true);
                    final Cursor localCursor = (Cursor) mCursorField.get(wrapper);
                    if (localCursor instanceof FenceCursorFactory.GeofenceCursorData) {
                        returnCursor = (FenceCursorFactory.GeofenceCursorData) localCursor;
                    } else {
                        throw new IllegalArgumentException("The given CursorWrapper doesn't contain a FenceSessionCursorData!!");
                    }
                    mCursorField.setAccessible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return returnCursor;
        }
    };

    /**
     * The CURSOR_RESOLVER to use for resolving the cursor
     */
    public static final CursorResolver CURSOR_RESOLVER;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            CURSOR_RESOLVER = DEFAULT_CURSOR_RESOLVER;
        } else {
            CURSOR_RESOLVER = PRE_HONEY_CURSOR_RESOLVER;
        }
    }

}
