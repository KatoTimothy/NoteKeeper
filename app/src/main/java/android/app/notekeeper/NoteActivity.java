package android.app.notekeeper;

import androidx.lifecycle.ViewModelProvider;

import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.app.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_ID = "android.app.notekeeper NOTE_POSITION";
    public static final int ID_NOT_FOUND = -1;
    private NoteInfo mNote;
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

        mOpenHelper = new NoteKeeperOpenHelper(this);

        //get reference to our view model provider
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        //This gives us a reference to View Model
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if (mViewModel.mIsNewlyCreated) {
            Log.d(TAG, "View model is now created");
        } else {
            Log.d(TAG, "View model was destroyed and recreated.");
        }

        //If activity was destroyed along with the view model, then
        //restore activity state from bundle
        if (savedInstanceState != null && mViewModel.mIsNewlyCreated)
            mViewModel.restoreState(savedInstanceState);
        //View Model is no longer a new creation
        //Implying it is already existing
        mViewModel.mIsNewlyCreated = false;

//        //get a list of courses from the DataManager Class
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();

        //Get references to the Views
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        //Obtain reference to spinner widget named 'spinner_courses'
        mSpinnerCourses = findViewById(R.id.spinner_courses);
        //Setting up the Cursor Adapter View
        mCourseAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);
        //setting the view of the dropdown list
        mCourseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Associate the Adapter View with the Spinner
        mSpinnerCourses.setAdapter(mCourseAdapter);

        loadCoursesData();

        readDisplayStateValues();
        saveOriginalStateValues();

        //If not a new note,
        //display the note
        if (!mIsNewNote) {
            loadNoteData();
        }
        Log.d(TAG, "onCreate");
    }

    private void loadCoursesData() {
        //open database in read mode
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        //specify the columns to be return
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };

        //query the database
        Cursor courseCursor = db.query(CourseInfoEntry.TABLE_COURSE_INFO, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

        //associate the cursor with CursorAdapter
        mCourseAdapter.changeCursor(courseCursor);
    }

    private void loadNoteData() {
        //Get reference to SQLite database
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        //specify the query criteria

        //selection clause
        String selection = NoteInfoEntry._ID + " = ?";

        //selection values
        String[] selectionArgs = {Integer.toString(mNoteId)};

        //columns to return when we perform a query
        String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        //perform query
        mNoteCursor = db.query(
                NoteInfoEntry.TABLE_NOTE_INFO,
                noteColumns,
                selection,
                selectionArgs,
                null, null, null);

        //We need column positions to access the column values
        //in the cursor
        mIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry._ID);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        //move cursor position to the first row in the result
        mNoteCursor.moveToNext();
        displayNote();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //store state if it exists
        //to the view model via the view model's saveState() method
        if (outState != null) ;
        mViewModel.saveState(outState);
    }

    private void saveOriginalStateValues() {
        //if is new note exit
        if (mIsNewNote)
            return;
        //save original note info
        //i.e. courseId, noteTitle and noteText
        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
    }


    //Reads the existing Notes
    //displays the in the fields
    private void displayNote() {
        //Get  values from the cursor
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);


//        //get list of courses from the data manager
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        //get course whose course id matches on  from database
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        //search for position of that course instance from courses list

        int courseIndex = getIndexOfCourseId(courseId);

        //set the selected course to to spinner
        mSpinnerCourses.setSelection(courseIndex);

        //set the title of the note title field
        mTextNoteTitle.setText(noteTitle);
        //set the text of note text field
        mTextNoteText.setText(noteText);
    }

    //This returns the index of the row in the course cursor that
    //matches the selected course in the current note
    private int getIndexOfCourseId(String courseId) {
        //get the cursor that's associated with the course adapter
        //we want to use it to get the value of the course id
        Cursor cursor = mCourseAdapter.getCursor();

        //get the column index of the courseID
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        //We don't know the where the cursor is position
        //we explicitly move the cursor to first row
        boolean more = cursor.moveToFirst();
        int courseRowIndex = 0;

        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            //check if the first row has the course we looking for
            //if true break out of the loop
            //otherwise we go do the same in the next row
            if (courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        //get intent
        Intent intent = getIntent();

        //retrieve the information named NOTE_POSITION passed into the intent
        //the second parameter indicates a default value,
        //for the case when no extra was passed in the intent
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_FOUND);

        //User is creating new note if
        //value in NOTE_POSITION key was not passed  in the intent extra
        mIsNewNote = mNoteId == ID_NOT_FOUND;

        if (mIsNewNote) {
            createNewNote();
        }

        //logcat info message to indicate the position of the note
        Log.i(TAG, "mNotePosition: " + mNoteId);

        //Read the note at the position supplied in the intent extra
        mNote = DataManager.getInstance().getNotes().get(mNoteId);
    }

    //creates a new note
    private void createNewNote() {
        DataManager dm = DataManager.getInstance();

        //creates new note and return its position
        mNoteId = dm.createNewNote();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if user cancels while
        // Creating a new note then,
        //Remove the new note
        //otherwise save note
        if (mIsCancelling) {
            //a simple info logcat msg to show
            //the position of the note we cancelling
            Log.i(TAG, "Cancelling note at position" + mNoteId);

            //If user is creating new note then,
            //Remove the new note
            //else explicitly store the old values back
            if (mIsNewNote)
                DataManager.getInstance().removeNote(mNoteId);
            else {
                //put the old values back
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
        //write a debug message
        Log.d(TAG, "onPause");
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        //set note to previous state values
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);
    }

    private void saveNote() {
        //set note course to currently selected course in the spinner
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        //set note Title and text values to current text values
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            //if cancelling
            //set the cancelling flag
            mIsCancelling = true;
            //if true then exit
            //and return to previous activity
            finish();
        } else if (id == R.id.action_next) {
            moveNext();
        }
        return super.onOptionsItemSelected(item);
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);
        saveOriginalStateValues();
        displayNote();

        //calls for recreation of the options menu
        invalidateOptionsMenu();
    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     *
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        //get the last index of the note
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        //Enable the next button until
        //the last note is reached
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void sendEmail() {
        //Get spinner and text input field values
        //get the selected item in the spinner
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        //get the textNote text value
        String subject = mTextNoteTitle.getText().toString();
        //Prepare email body text
        String body = "Checkout what I learnt from plural sight \"" + course.getTitle() + "\n" + mTextNoteText.getText().toString();

        //Identify the target that handles ACTION_SEND for 'message/rfc2822' MIME type
        Intent intent = new Intent(Intent.ACTION_SEND);
        //set the MIME type for mails
        intent.setType("message/rfc2822");

        //Set up Extras
        //Specifies email's subject text field text
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        //Specify email's body text field text
        intent.putExtra(Intent.EXTRA_TEXT, body);

        //start mail activity
        startActivity(intent);
    }
}
