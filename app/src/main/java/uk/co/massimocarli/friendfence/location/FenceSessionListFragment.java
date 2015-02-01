package uk.co.massimocarli.friendfence.location;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.CreateFileActivityBuilder;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.maps.GoogleMap;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import uk.co.massimocarli.friendfence.Conf;
import uk.co.massimocarli.friendfence.R;
import uk.co.massimocarli.friendfence.activity.FenceStreetViewActivity;
import uk.co.massimocarli.friendfence.activity.ShowPathActivity;
import uk.co.massimocarli.friendfence.content.FenceDB;
import uk.co.massimocarli.friendfence.content.cursor.CursorResolver;
import uk.co.massimocarli.friendfence.content.cursor.FenceCursorFactory;
import uk.co.massimocarli.friendfence.drive.FenceDriveUtil;
import uk.co.massimocarli.friendfence.location.dialog.EditSessionDialog;
import uk.co.massimocarli.friendfence.util.DistanceUtil;
import uk.co.massimocarli.friendfence.util.IOUtils;

/**
 * This is a Fragment that shows the information related to the available FenceSession and permits
 * the CRUD of them.
 */
public class FenceSessionListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The Tag for the log
     */
    private static final String TAG_LOG = FenceSessionListFragment.class.getName();

    /**
     * The name of the extra we use to pass the session id
     */
    private final static String SESSION_ID_EXTRA = Conf.PKG + ".extra.SESSION_ID_EXTRA";

    /**
     * The Tag for the License Dialog
     */
    private final static String LICENSE_DIALOG_TAG = "LICENSE_DIALOG_TAG";

    /**
     * The default title we use for Google Drive files
     */
    private final static String DEFAULT_DRIVE_TITLE = "FenceSession";

    /**
     * The Mime Type we use for JSON
     */
    private final static String JSON_MIME_TYPE = "application/json";

    /**
     * The Testing driveId
     */
    private final String DRIVE_ID = "dajklal87ASDY9AHasjkaKSDHaks";


    /**
     * This is the request code we use for the onActivityResult management
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * The Code for the Google Drive FileActivityBuilder in saving
     */
    private final static int REQUEST_CODE_SAVE_SESSION = 188;

    /**
     * The Code for the Google Drive FileActivityBuilder in reading
     */
    private final static int REQUEST_CODE_LOAD_SESSION = 189;

    /**
     * The FROM field for the CursorAdapter of the DB fields
     */
    private final static String[] FROM = new String[]{FenceDB.FenceSession._ID,
            FenceDB.FenceSession.START_DATE, FenceDB.FenceSession.TOTAL_DISTANCE};

    /**
     * The TO field for the CursorAdapter of the Views id
     */
    private final static int[] TO = new int[]{R.id.fence_session_id, R.id.fence_session_start_date,
            R.id.fence_session_distance};

    /**
     * The identifier of the Loader for the session
     */
    private final static int FENCE_SESSION_LOADER_ID = 17;

    /**
     * The adapter we use for the list
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * The handle to manage Google Map
     */
    private GoogleMap mGoogleMap;

    /**
     * The GoogleApiClient we use to interact with Location Services
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Our custom cursor reference
     */
    private FenceCursorFactory.FenceSessionCursorData mSessionCursorData;

    /**
     * The id of the current session to be saved
     */
    private long mCurrentSavedSession;

    /**
     * This is the interface that contains the operation that is invoked when we select
     * a session item
     */
    public interface OnSessionSelectedListener {

        /**
         * This is the operation invoked when we select a Session in the list
         *
         * @param position The position of the selected session
         * @param id       The id of the session in the list
         */
        void onSessionSelected(int position, long id);

    }

    /**
     * The implementation of the interface to manage CallBacks from Google Play Services
     */
    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {

                @Override
                public void onConnected(Bundle bundle) {
                    Log.d(TAG_LOG, "Connected");
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.d(TAG_LOG, "Disconnected. Please re-connect.");
                }
            };

    /**
     * The implementation of the interface we use to manage errors from Google Play Services
     */
    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    if (connectionResult.hasResolution()) {
                        try {
                            connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            // Unable to resolve, message user appropriately
                        }
                    } else {
                        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
                    }
                }
            };

    /**
     * The WeakReference to the listener for the session selection
     */
    private WeakReference<OnSessionSelectedListener> mOnSessionSelectedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();
        mGoogleApiClient.connect();
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_session_item, null, FROM, TO, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (R.id.fence_session_start_date == view.getId()) {
                    // We have the date so we have to format it and show
                    final Date startDate = mSessionCursorData.getStartDate();
                    final TextView dateView = (TextView) view;
                    dateView.setText(Conf.SIMPLE_DATE_FORMAT.format(startDate));
                    return true;
                } else if (R.id.fence_session_distance == view.getId()) {
                    final float distanceInMeters = mSessionCursorData.getTotalDistance();
                    final TextView distanceView = (TextView) view;
                    distanceView.setText(DistanceUtil.formatDistance(getActivity(), distanceInMeters));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getActivity().getResources().getString(R.string.my_move_empty_session_message));
        getListView().setAdapter(mAdapter);
        getLoaderManager().initLoader(FENCE_SESSION_LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // We register the ListView for the Contextual menu
        registerForContextMenu(getListView());
    }

    @Override
    public void onStop() {
        // Here we have to disconnect the client
        //mGoogleApiClient.disconnect();
        super.onStop();
        // We unregister the context list view
        unregisterForContextMenu(getListView());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    /**
     * This is the method forwarded from the Activity in case we have some error from the
     * Google Play Services
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        if (CONNECTION_FAILURE_RESOLUTION_REQUEST == requestCode && Activity.RESULT_OK == resultCode) {
            // In this case we have to retry the connection
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                Log.d(TAG_LOG, "Error, we try to reconnect!");
                mGoogleApiClient.connect();
            }
        } else if (REQUEST_CODE_SAVE_SESSION == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                // We get the DriveId for the new File
                final DriveId createdFileId = data.getParcelableExtra(
                        CreateFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                if (mCurrentSavedSession >= 0) {
                    PutDataIntoDiveFileTask saveTask = new PutDataIntoDiveFileTask(mCurrentSavedSession);
                    saveTask.execute(createdFileId);
                } else {
                    Toast.makeText(getActivity(), R.string.action_drive_create_failed, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (REQUEST_CODE_LOAD_SESSION == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                // We get the DriveId for the new File
                final DriveId openedFileId = data.getParcelableExtra(
                        CreateFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                // We read the contents of the file and import the data
                Log.d(TAG_LOG, "read file " + openedFileId);
                readFenceSessionData(openedFileId);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.drive_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_drive_legal_notes) {
            // We show legal notes
            final SourceSoftwareLicenseDialog licenseDialog = new SourceSoftwareLicenseDialog();
            licenseDialog.show(getFragmentManager(), LICENSE_DIALOG_TAG);
        } else if (item.getItemId() == R.id.action_drive_import) {
            // We want to read information from Drive
            importFenceSession();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = new MenuInflater(getActivity());
        inflater.inflate(R.menu.fence_session_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // We get the object that contains the information for the selected item
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // We get the position and the id
        final int position = menuInfo.position;
        final long sessionId = menuInfo.id;
        // We execute the related operation
        final int selectedItemId = item.getItemId();
        // We use if and not switch because preventing usage into libraries
        if (R.id.session_context_delete_menu_item == selectedItemId) {
            // We delete the related session in async way
            deleteSessionData(sessionId, position);
        } else if (R.id.session_context_view_menu_item == selectedItemId) {
            // We want to view the item
            viewSessionData(sessionId, position);
        } else if (R.id.session_context_edit_menu_item == selectedItemId) {
            // We want to edit the item
            editSessionData(sessionId, position);
        } else if (R.id.session_context_edit_show_map == selectedItemId) {
            // Show the FenceSession in Map
            showInMap(sessionId);
        } else if (R.id.session_context_show_street_view == selectedItemId) {
            // Show the FenceSession in StreetView
            showInStreetView(sessionId);
        } else if (R.id.session_context_export == selectedItemId) {
            // We export the information of the session
            //exportFenceSession(sessionId);
            exportDirectFenceSession(sessionId);
            //searchFenceSessionFiles();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader loader = new CursorLoader(getActivity(), FenceDB.FenceSession.CONTENT_URI,
                null, null, null, FenceDB.FenceSession.START_DATE + " DESC ");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        setListShown(true);
        mSessionCursorData = CursorResolver.CURSOR_RESOLVER.extractSessionCursor(cursor);
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (mOnSessionSelectedListener != null) {
            final OnSessionSelectedListener listener = mOnSessionSelectedListener.get();
            if (listener != null) {
                listener.onSessionSelected(position, id);
            }
        }
    }

    /**
     * Set the OnSessionSelected for the selection of the session
     *
     * @param onSessionSelectedListener The OnSessionSelectedListener
     */
    public void setOnSessionSelectedListener(final @Nullable OnSessionSelectedListener onSessionSelectedListener) {
        this.mOnSessionSelectedListener = new WeakReference<OnSessionSelectedListener>(onSessionSelectedListener);
    }

    /**
     * This shows the fragment related to the given sessionId and position
     *
     * @param sessionId The id of the FenceSession to show
     * @param position  The position of the FenceSession into the list
     */
    private void viewSessionData(final long sessionId, final int position) {
        // We execute the same item of the selection
        if (mOnSessionSelectedListener != null) {
            final OnSessionSelectedListener listener = mOnSessionSelectedListener.get();
            if (listener != null) {
                listener.onSessionSelected(position, sessionId);
            }
        }
    }

    /**
     * This delete the FenceSession with the given sessionId and position
     *
     * @param sessionId The id of the FenceSession to delete
     * @param position  The position of the FenceSession into the list
     */
    private void deleteSessionData(final long sessionId, final int position) {
        // We have to delete the session which could have a lot of related position.
        // For this reason we want to execute them in async way
        final class AQH extends AsyncQueryHandler {

            AQH(ContentResolver cr) {
                super(cr);
            }

            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                super.onDeleteComplete(token, cookie, result);
                Log.d(TAG_LOG, "Delete operation completed!");
            }
        }
        final Uri sessionToDelete = Uri.withAppendedPath(FenceDB.FenceSession.CONTENT_URI, String.valueOf(sessionId));
        AQH queryHandler = new AQH(getActivity().getContentResolver());
        queryHandler.startDelete(1, null, sessionToDelete, null, null);
    }

    /**
     * This edit the FenceSession with the given sessionId and position
     *
     * @param sessionId The id of the FenceSession to edit
     * @param position  The position of the FenceSession into the list
     */
    private void editSessionData(final long sessionId, final int position) {
        // We want to create the Dialog to change the owner for the selected session
        EditSessionDialog editDialog = EditSessionDialog.create(sessionId);
        editDialog.show(getChildFragmentManager(), "edit");
    }


    /**
     * This utility method starts the Fragment to show the Map in the dialog
     *
     * @param sessionId The id of the session to show
     */
    private void showInMap(final long sessionId) {
        // We start the Activity
        ShowPathActivity.showSessionPathActivity(getActivity(), sessionId);
    }

    /**
     * This utility method starts the Fragment to show the StreetView
     *
     * @param sessionId The id of the session to show
     */
    private void showInStreetView(final long sessionId) {
        // We start the Activity
        FenceStreetViewActivity.showSessionStreetViewActivity(getActivity(), sessionId);
    }


    /**
     * The Dialog we use to show the Terms of Licens for Google Drive
     */
    public static class SourceSoftwareLicenseDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String legalNotes = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.ic_drawer)
                    .setTitle(R.string.action_drive_legal_notes)
                    .setMessage(legalNotes)
                    .create();
        }
    }

    /**
     * This is an utility method that we use to export the data for a given FenceSession
     *
     * @param sessionToExportId The id of the FenceSession to export
     */
    private void exportFenceSession(final long sessionToExportId) {
        // We create the Callback for the creation of the Content for the file
        ResultCallback<DriveApi.DriveContentsResult> contentCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult contentsResult) {
                if (contentsResult.getStatus().isSuccess()) {
                    // We update the last session
                    mCurrentSavedSession = sessionToExportId;
                    // We set the initial Metadata
                    final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setMimeType(JSON_MIME_TYPE)
                            .setTitle(DEFAULT_DRIVE_TITLE)
                            .build();
                    // We Start the CreateFileActivityBuilder
                    final IntentSender intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setActivityTitle(getString(R.string.session_context_export))
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(contentsResult.getDriveContents())
                            .build(mGoogleApiClient);
                    try {
                        getActivity().startIntentSenderForResult(intentSender, REQUEST_CODE_SAVE_SESSION, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG_LOG, "Error ", e);
                    }

                }
            }
        };
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(contentCallback);
    }

    /**
     * The AsyncTask we use to save data into the DriveFile
     */
    private class PutDataIntoDiveFileTask extends AsyncTask<DriveId, Void, DriveFile> {

        /**
         * The Id of the session we need to save
         */
        private final long mSessionId;

        /**
         * Initialize the Task we use to save data in Google Drive
         *
         * @param sessionId The sessionId of the data to save
         */
        public PutDataIntoDiveFileTask(final long sessionId) {
            this.mSessionId = sessionId;
        }

        @Override
        protected DriveFile doInBackground(DriveId... params) {
            // We get the reference to the file
            DriveFile newFile = Drive.DriveApi.getFile(mGoogleApiClient, params[0]);
            try {
                DriveApi.DriveContentsResult contentsResult = newFile.open(
                        mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).await();
                if (!contentsResult.getStatus().isSuccess()) {
                    return null;
                }
                OutputStream outputStream = contentsResult.getDriveContents().getOutputStream();
                final String sessionJson = FenceDriveUtil.fenceSessionAsJson(getActivity(), mSessionId);
                outputStream.write(sessionJson.getBytes());
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setLastViewedByMeDate(new Date()).build();
                com.google.android.gms.common.api.Status commitStatus = contentsResult
                        .getDriveContents().commit(mGoogleApiClient, changeSet).await();
                if (!commitStatus.isSuccess()) {
                    return null;
                }
                return newFile;
            } catch (IOException e) {
                Log.e(TAG_LOG, "IOException while appending to the output stream", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(DriveFile driveFile) {
            super.onPostExecute(driveFile);
            if (driveFile != null) {
                // The session has been saved
                mCurrentSavedSession = -1;
                // We show a toast
                Toast.makeText(getActivity(), R.string.action_drive_create_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.action_drive_create_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * This is another option to save a file without the usage of the provided UI
     *
     * @param sessionToExportId The id of the FenceSession to export
     */
    private void exportDirectFenceSession(final long sessionToExportId) {
        // We create the Callback for the creation of the Content for the file
        ResultCallback<DriveApi.DriveContentsResult> contentCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult contentsResult) {
                if (contentsResult.getStatus().isSuccess()) {
                    // We get the name from the Json
                    final String sessionName = FenceDriveUtil.getFenceSessionName(getActivity(), sessionToExportId);
                    // We set the initial Metadata
                    final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setMimeType(JSON_MIME_TYPE)
                            .setTitle(sessionName)
                            .setStarred(true)
                            .build();
                    // We save the Json into the content
                    OutputStream outputStream = contentsResult.getDriveContents().getOutputStream();
                    // We read the info about the session
                    final String sessionJson = FenceDriveUtil.fenceSessionAsJson(getActivity(), sessionToExportId);
                    try {
                        outputStream.write(sessionJson.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // We create the callback for the file
                    final ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
                            ResultCallback<DriveFolder.DriveFileResult>() {
                                @Override
                                public void onResult(DriveFolder.DriveFileResult result) {
                                    if (!result.getStatus().isSuccess()) {
                                        Log.e(TAG_LOG, "Error while trying to create the file");
                                        Toast.makeText(getActivity(), R.string.action_drive_create_failed, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    final String message = getString(R.string.action_drive_create_success_name, sessionName);
                                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                                }
                            };
                    // We save directly the file
                    Drive.DriveApi.getRootFolder(mGoogleApiClient)
                            .createFile(mGoogleApiClient, metadataChangeSet, contentsResult.getDriveContents())
                            .setResultCallback(fileCallback);
                }
            }
        };
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(contentCallback);
    }


    /**
     * This method opens the UI to select a file for reading
     */
    private void importFenceSession() {
        // We Start the CreateFileActivityBuilder
        final IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setActivityTitle(getString(R.string.action_drive_import))
                .build(mGoogleApiClient);
        try {
            getActivity().startIntentSenderForResult(intentSender, REQUEST_CODE_LOAD_SESSION, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG_LOG, "Error ", e);
        }
    }

    /**
     * This is the method that encapsulate the logic for reading the file and
     * parsing data into the ContentProvider
     *
     * @param driveId The identifier of the File we want to read
     */
    private void readFenceSessionData(final DriveId driveId) {
        // We get the File
        DriveFile fenceSessionFile = Drive.DriveApi.getFile(mGoogleApiClient, driveId);
        // We create the callback for the Contents
        final ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult contentsResult) {
                if (!contentsResult.getStatus().isSuccess()) {
                    Toast.makeText(getActivity(), R.string.action_drive_load_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                // We get the Contents
                DriveContents contents = contentsResult.getDriveContents();
                // We read the data from the Contents
                try {
                    String contentAsString = IOUtils.toString(contents.getInputStream());
                    FenceDriveUtil.importFenceSessionJson(getActivity(), contentAsString);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.action_drive_load_failed, Toast.LENGTH_SHORT).show();
                }
            }
        };
        fenceSessionFile.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long l, long l2) {
                Log.d(TAG_LOG, l + " -> " + l2);
            }
        }).setResultCallback(contentsOpenedCallback);
    }


    /**
     * This method doesn't use the UI but load directly the content of a file
     */
    private void importDirectFenceSession() {
        // We get the DriveId of a file
        final DriveId fileId = DriveId.decodeFromString(DRIVE_ID);
        // We download and save it if present
        readFenceSessionData(fileId);
    }


    /**
     * Utility method that search and lists all the file with name that starts with
     * FenceSession
     */
    private void searchFenceSessionFiles() {
        // Sort info
        final SortOrder sortOrder = new SortOrder.Builder()
                .addSortAscending(SortableField.TITLE)
                .addSortDescending(SortableField.CREATED_DATE)
                .build();
        // We create the proper Query object
        final Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, DEFAULT_DRIVE_TITLE))
                .addFilter(Filters.eq(SearchableField.TRASHED, false))
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, JSON_MIME_TYPE))
                .setSortOrder(sortOrder)
                .build();
        // We search the given File
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                    Log.i(TAG_LOG, metadata.getTitle() + " " + metadata.getCreatedDate());
                }
            }
        });
    }

}
