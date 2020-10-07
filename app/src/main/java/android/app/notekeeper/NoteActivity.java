package android.app.notekeeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.app.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import android.app.notekeeper.NoteKeeperProviderContract.Courses;
import android.app.notekeeper.NoteKeeperProviderContract.Notes;
import android.content.ContentUris;
import android.content.ContentValues;

import androidx.loader.content.CursorLoader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.google.android.material.snackbar.Snackbar;

public class NoteActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public static final int NOTIFICATION_ID = 0;
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "android.app.notekeeper NOTE_POSITION";
    public static final int ID_NOT_FOUND = -1;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;

    private NoteActivityViewModel mViewModel;
    private NoteKeeperOpenHelper mOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private int mIdPos;
    private SimpleCursorAdapter mCourseAdapter;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;
    private ProgressBar mProgressBar;

    @Override
    protected void onDestroy() {
        mOpenHelper.close();
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar = findViewById(R.id.progress_bar);

        mOpenHelper = new NoteKeeperOpenHelper(this);

        //get reference to our view model provider
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        //This gives us a reference to View Model
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);


        //If activity is destroyed along with the viewModel,
        //the activity state will be restored from savedInstance state bundle
        if (savedInstanceState != null && mViewModel.mIsCreated) {
            //restore state from from savedInstanceSate bundle
            mViewModel.restoreState(savedInstanceState);
            Log.d(TAG, "View model was destroyed with along with NoteActivity.");
            if (mViewModel.mIsCreated) {
                Log.d(TAG, "View model is newly created");
            } else {
                Log.d(TAG, "ViewModel already existed.");
            }
        }
        //This is a flag to indicate that
        //the viewModel instance was already created by ViewModelProvider
        mViewModel.mIsCreated = false;

        //Get references to the TextViews
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        //Obtain reference to spinner widget identified by id value  'spinner_courses'
        mSpinnerCourses = findViewById(R.id.spinner_courses);

        //Setting up the Cursor Adapter View
        mCourseAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);

        //setting the layout to be used to display each view in dropdown menu of the spinner
        mCourseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Associate the Adapter View with the Spinner
        mSpinnerCourses.setAdapter(mCourseAdapter);

        //Load courses
        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        saveOriginalStateValues();

        //If user is NOT creating a new note,
        //display the note
        if (!mIsNewNote) {
            //use loader to load note from database
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);
        }
        Log.d(TAG, "onCreate");
    }

    @Override
    //This is called when the Activity has been  destroyed for instance
    //calling the finish() in the program
    //allows us to more persistently save activity state in a bundle
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //saves the state bundle if there's any
        if (outState != null) ;
        mViewModel.saveState(outState);
    }

    //Stores the original state values of the note before user interacts with it
    private void saveOriginalStateValues() {
        //if user is creating a new note then, simply exit
        if (mIsNewNote)
            return;
    }

    //Retrieves the note values from the cursor and
    //binds it to the corresponding views
    private void displayNote() {
        //Get  values from the cursor
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourseId(courseId);

        //set the selected course to to spinner
        mSpinnerCourses.setSelection(courseIndex);

        //set the title of the note title field
        mTextNoteTitle.setText(noteTitle);
        //set the text of note text field
        mTextNoteText.setText(noteText);

        //Handles details of sending a broadcast
        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing note");
    }

    //This returns the index of the row in the course cursor
    //We use courseId
    private int getIndexOfCourseId(String courseId) {
        //get the cursor that's associated with the course adapter
        //we want to use it to get the value of the course id
        Cursor cursor = mCourseAdapter.getCursor();

        //get the column index of the required courseId
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        //We don't know the where the cursor position is so
        //we explicitly move the cursor to first row
        boolean more = cursor.moveToFirst();

        //keeps track of the row index as we traverse the rows in the course_info table
        int courseRowIndex = 0; //start out with the first rows

        //checking the course_info table for the course that matches passed in courseId
        while (more) {
            //get the courseId in the cursor
            String cursorCourseId = cursor.getString(courseIdPos);
            //check if this courseId matches the one in the cursor
            if (courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            //move to the next row
            more = cursor.moveToNext();
        }
        //returns the row index containing the matching courseId
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        //get intent
        Intent intent = getIntent();

        //retrieve the information named NOTE_POSITION passed into the intent
        //the second parameter indicates a default value,
        //for the case when no extra was passed in the intent
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_FOUND);

        //We verify that  user is creating new note if
        //value in NOTE_POSITION key was not passed  in the intent extra
        //and use Default value ID_NOT_FOUND, -1
        mIsNewNote = mNoteId == ID_NOT_FOUND;

        if (mIsNewNote) {
            createNewNote();
        }

        //logcat info message to indicate the position of the note
        Log.i(TAG, "mNotePosition: " + mNoteId);

    }

    //creates a new note in the database
    private void createNewNote() {
        final ContentValues values = new ContentValues();

        //Start out with empty strings
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        @SuppressLint("StaticFieldLeak") AsyncTask<ContentValues, Integer, Uri> insertNoteTask =
                new AsyncTask<ContentValues, Integer, Uri>() {
                    @Override
                    protected void onPreExecute() {
                        //display the progress bar
                        mProgressBar.setVisibility(View.VISIBLE);
                        //set progress to one
                        mProgressBar.setProgress(1);
                    }

                    @Override
                    protected Uri doInBackground(ContentValues... contentValues) {
                        ContentValues insertValues = contentValues[0];

                        simulateLongRunningTask();
                        publishProgress(2);

                        simulateLongRunningTask();
                        publishProgress(3);

                        return getContentResolver().insert(Notes.CONTENT_URI, insertValues);
                    }

                    @Override
                    protected void onProgressUpdate(Integer... values) {
                        mProgressBar.setProgress(values[0]);
                    }

                    @Override
                    protected void onPostExecute(Uri uri) {
                        mNoteUri = uri;
                        //set visibility of progress bar to none
                        mProgressBar.setVisibility(View.INVISIBLE);
                        //display Snackbar
                        displaySnackBar(mNoteUri.toString());
                    }
                };
        insertNoteTask.execute(values);
    }

    private void displaySnackBar(String message) {
        Snackbar.make(mTextNoteText, message, Snackbar.LENGTH_LONG).show();
    }

    private void simulateLongRunningTask() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Checks if user is cancelling the note they're trying to create
        if (mIsCancelling) {
            //a simple information logcat msg to show
            //the position of the note we cancelling
            Log.i(TAG, "Cancelling note at position" + mNoteId);

            //Checks if  user is attempting to cancel new note creation process,
            //and delete that new note if true
            if (mIsNewNote)
                //deletes the backing store of that note
                deleteNoteFromDatabase();

        } else {
            //saves note when user switches away from an Activity
            saveNote();
        }
        //write a debug message
        Log.d(TAG, "onPause");
    }

    //Deletes a row from note_info table  that has an _ID value is equal to mNoteId
    private void deleteNoteFromDatabase() {

//        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        @SuppressLint("StaticFieldLeak") AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(mNoteUri, null, null);
                return null;
            }
        };
        task.execute();
    }

    private void saveNote() {
        //Get selected course in the spinner
        String courseId = selectedCourseId();

        //Obtain text field values
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();

        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    //returns courseId from the course_info table,
    // that matches selected course in the spinner
    private String selectedCourseId() {

        //get position of selected course in the spinner
        int selectedCoursePosition = mSpinnerCourses.getSelectedItemPosition();

        //get cursor associated with spinner Adapter
        Cursor cursor = mCourseAdapter.getCursor();

        cursor.moveToPosition(selectedCoursePosition);
        //read the value of courseId that corresponds to the selected course in spinner
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        return cursor.getString(courseIdPos);//returns the courseId value
    }

    //saves note to the database
    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText) {

        //Provide new values for column in the note_info table
        final ContentValues values = new ContentValues();

        values.put(Notes.COLUMN_COURSE_ID, courseId);
        values.put(Notes.COLUMN_NOTE_TITLE, noteTitle);
        values.put(Notes.COLUMN_NOTE_TEXT, noteText);

        @SuppressLint("StaticFieldLeak")
        AsyncTask task = new AsyncTask() {
            @Override
            @SuppressLint("StaticFieldLeak")
            protected Object doInBackground(Object[] objects) {
                getContentResolver().update(mNoteUri, values, null, null);
                return null;
            }
        };
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;

        } else if (id == R.id.action_cancel) {
            //set the cancelling flag
            mIsCancelling = true;
            //and return to previous activity
            finish();
        } else if (id == R.id.action_set_reminder) {
            sendNotification();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendNotification() {
        //get Notification manager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Intent notificationIntent = new Intent(this, NoteActivity.class);
        notificationIntent.putExtra(NoteActivity.NOTE_ID, mNoteId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Intent that starts up a NoteBackupService component
        Intent noteBackupServiceIntent = new Intent(this, NoteBackupService.class);
        noteBackupServiceIntent.putExtra(NoteBackupService.EXTRA_ALL_COURSES, NoteBackup.ALL_COURSES);
        //Pending Intent to start a service component
        PendingIntent getServicePendingIntent = PendingIntent.getService(
                this,
                0,
                noteBackupServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //create notification
        Notification notification = new NotificationCompat.Builder(this, NoteKeeperApp.CHANNEL_ID)
                //set small icon
                .setSmallIcon(R.drawable.ic_note)
                //set title
                .setContentTitle(getString(R.string.notification_content_title))
                //set text
                .setContentText(mTextNoteText.getText())
                //set priority
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                //set up expanded view notification
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(mTextNoteTitle.getText())
                        .bigText(mTextNoteText.getText())
                        .setSummaryText(getString(R.string.notification_summary_text))
                )
                .setContentIntent(pendingIntent)

                //starts the MainActivity component
                .addAction(0, "REVIEW ALL NOTES", PendingIntent.getActivity(this, 0, new Intent(this,
                        MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))

                //starts the NoteBackupService component to backup all notes
                .addAction(0, "BACKUP ALL NOTES", getServicePendingIntent)
                .build();
        //displays notification
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
    }

    //Starts an email program and
    //populates the subject field and body field of the email with
    //values from  the note title and note text  fields of NoteActivity respectively
    private void sendEmail() {
        //get the selected course  in the spinner
        String course = getSelectedCourseFromDatabase();
        //get the textNote text value
        String subject = mTextNoteTitle.getText().toString();
        //Prepare email body text
        String body = "Checkout what I learnt from plural sight \"" + course +
                "\n" + mTextNoteText.getText().toString();

        //Identify the target that handles ACTION_SEND for 'message/rfc2822' MIME type
        Intent intent = new Intent(Intent.ACTION_SEND);
        //set the MIME type for mails
        intent.setType("message/rfc2822");

        //Set up Extras
        //Specifies text for the email subject
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //Species text for the email body
        intent.putExtra(Intent.EXTRA_TEXT, body);

        //starts mailing app chooser
        startActivity(intent);
    }

    //Returns the selected courseTitle  in the spinner
    private String getSelectedCourseFromDatabase() {
        //get position of the selected course in the spinner
        //This helps to position the cursor in the right place
        int selectedCoursePosition = mSpinnerCourses.getSelectedItemPosition();

        //get the cursor associated with the spinner adapter
        Cursor cursor = mCourseAdapter.getCursor();

        cursor.moveToPosition(selectedCoursePosition);

        //get the column index of course_title of note_info table
        int courseTitlePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);

        return cursor.getString(courseTitlePos);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //Flags that query to note_info table is done
        mNotesQueryFinished = false;
        CursorLoader loader = null;
        if (id == LOADER_NOTES) {
            //instantiate loader of id LOADER_NOTES
            loader = createLoaderNotes();
        } else if (id == LOADER_COURSES) {
            //instantiate loader of id LOADER_COURSES
            loader = createLoaderCourses();
        }
        return loader;
    }

    //Returns a cursorLoader containing rows from course_info table arranged by course_title
    //In ASCENDING order
    private CursorLoader createLoaderCourses() {
        //Flags that  query to the course_info table  has finished or not
        mCoursesQueryFinished = false;

        Uri uri = Courses.CONTENT_URI;

        //specify the columns to be returned when we perform a query
        String[] courseColumns = {
                Courses.COURSE_TITLE,
                Courses.COLUMN_COURSE_ID,
                Courses._ID
        };
        return new CursorLoader(this, uri,
                courseColumns, null, null,
                Courses.COURSE_TITLE);
    }

    //Returns a cursorLoader containing rows from note_info table
    private CursorLoader createLoaderNotes() {
        //columns to return when we perform a query
        String[] noteColumns = {
                Notes._ID,
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        //Construct a note uri
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);

        return new CursorLoader(this, mNoteUri,
                noteColumns, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //check if we have the right Loader
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        else if (loader.getId() == LOADER_COURSES) {
            //associate the cursor with CursorAdapter
            mCourseAdapter.changeCursor(data);
            mCoursesQueryFinished = true; //indicates the query for courses is finished

            //displays note when both queries to note_info and course_info have finished
            displayNoteWhenQueryFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        //We need column positions to access the column values
        //in the cursor
        mIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry._ID);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        //move cursor position to the first row of  the cursor
        mNoteCursor.moveToNext();
        mNotesQueryFinished = true; //This flag indicates query for loading notes has finished
        displayNoteWhenQueryFinished();
    }

    //displays note when both queries to load notes and courses is finished
    private void displayNoteWhenQueryFinished() {
        if (mCoursesQueryFinished && mNotesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //check if loader is for notes
        if (loader.getId() == LOADER_NOTES) {
            //check if cursor is non null before closing it
            if (mNoteCursor != null)
                //resets the mNoteCursor
                mNoteCursor.close();
        }
        //check if loader is for courses
        else if (loader.getId() == LOADER_COURSES)
            //reset the mCourseAdapter
            mCourseAdapter.changeCursor(null);
    }
}
