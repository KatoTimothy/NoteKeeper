package android.app.notekeeper;

import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();
    public static final String NOTE_POSITION = "android.app.notekeeper NOTE_POSITION";
    public static final int POSITION_NOT_FOUND = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    
    private NoteActivityViewModel mViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        //get reference to our view model provider
        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        //This gives us a reference to View Model
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.mIsNewlyCreated) {
            Log.d(TAG, "View model has just been created");
        }else {
            Log.d(TAG, "View model is already existing");
        }
        //If activity is being recreated and that
        //view model is existing  then
        //restore activity state using the view model's restoreState() method
        if(savedInstanceState != null && mViewModel.mIsNewlyCreated )
            mViewModel.restoreState(savedInstanceState);
        //View Model is no longer a new creation
        //Implying it is already existing
        mViewModel.mIsNewlyCreated = false;
        
        //get a list of courses from the DataManager Class
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
    
        //Get references to the Views
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);
        //Obtain reference to spinner widget named 'spinner_courses'
        mSpinnerCourses = findViewById(R.id.spinner_courses);
        //Setting up the Adapter View
        ArrayAdapter<CourseInfo> courseAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Associate the Adapter View with the Spinner
        mSpinnerCourses.setAdapter(courseAdapter);

        readDisplayStateValues();
        saveOriginalStateValues();

        //If not a new note,
        //display the note
        if(!mIsNewNote){
            displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        }
        Log.d(TAG, "onCreate");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //store state if it exists
        //to the view model via the view model's saveState() method
        if(outState !=null);
        mViewModel.saveState(outState);
    }
    private void saveOriginalStateValues() {
        //if is new note exit
        if(mIsNewNote)
            return;
        //save original note info
        //i.e. courseId, noteTitle and noteText
        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
    }
    
  
    //Reads the existing Notes
    //displays the in the fields
    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        //get list of courses from the data manager
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //Get the position of the selected course from the list of courses
        int courseIndex = courses.indexOf(mNote.getCourse());
        //set the selected course in the spinner
        spinnerCourses.setSelection(courseIndex);
        
        //set the title of the note title field
        textNoteTitle.setText(mNote.getTitle());
        //set the text of note text field
        textNoteText.setText(mNote.getText());
    }
    
    
    private void readDisplayStateValues() {
        //get intent
        Intent intent = getIntent();
        
        //retrieve the information named NOTE_POSITION passed into the intent
        //the second parameter indicates a default value
        //for the case when no extra was passed in the intent
        mNotePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_FOUND);
        
        //User is creating new note if
        //value in NOTE_POSITION was not found in the intent extra
        //instead we use the POSITION_NOT_FOUND value
        mIsNewNote = mNotePosition == POSITION_NOT_FOUND;
        
        if(mIsNewNote){
            createNewNote();
        }
    
        //logcat info message to indicate the position of the note
        Log.i(TAG, "mNotePosition: "+ mNotePosition);
        
        //Read the note at the position supplied in the intent extra
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
    }
    
    //creates a new note
    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        //createNewNote() of the DataManager class,
        //creates new note and return its position
        mNotePosition = dm.createNewNote();
        //mNote = dm.getNotes().get(mNotePosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if user cancels while
        // Creating a new note then,
        //Remove the new note
        //otherwise save note
        if(mIsCancelling){
            //a simple info logcat msg to show
            //the position of the note we cancelling
            Log.i(TAG, "Cancelling note at position" + mNotePosition);
            
            //If user is creating new note then,
            //Remove the new note
            //else explicitly store the old values back
            if(mIsNewNote)
                DataManager.getInstance().removeNote(mNotePosition);
            else{
                //put the old values back
                storePreviousNoteValues();
            }
        }else {
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
        
    }@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        }else if(id==R.id.action_cancel){
        //if cancelling
            //set the cancelling flag
            mIsCancelling = true;
            //if true then exit
            //and return to previous activity
            finish();
        }else if(id == R.id.action_next){
            moveNext();
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void moveNext() {
        saveNote();
        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);
        saveOriginalStateValues();
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);
        
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
        int lastNoteIndex = DataManager.getInstance().getNotes().size()-1;
        //Enable the next button until
        //the last note is reached
        item.setEnabled(mNotePosition < lastNoteIndex);
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
        String body = "Checkout what I learnt from plural sight \""+ course.getTitle()+ "\n" + mTextNoteText.getText().toString();
        
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
