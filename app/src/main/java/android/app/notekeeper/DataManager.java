package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.app.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    //Creating a data manager instance
    private static DataManager ourInstance = null;
    //an array of courses
    private List<CourseInfo> mCourses = new ArrayList<>();
    //an array of notes
    private List<NoteInfo> mNotes = new ArrayList<>();

    //method to get instance of dataManager
    public static DataManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new DataManager();
            //initialize the course instance
//            ourInstance.initializeCourses();
//            ourInstance.initializeExampleNotes();
        }
        return ourInstance;
    }

    public static void loadFromDatabase(NoteKeeperOpenHelper dbHelper) {
        //open connection to the database for reading
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //We're specifying  the columns to return from courses table when
        //the query is performed
        final String[] coursesColumns = {
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry.COLUMN_COURSE_TITLE
        };

        //querying the course_info table
        //returns rows with fields course_id and course_title
        //data returned is sorted by title in ASCENDING order
        final Cursor coursesCursor = db.query(
                CourseInfoEntry.TABLE_COURSE_INFO,
                coursesColumns,
                null, null, null, null,
                CourseInfoEntry.COLUMN_COURSE_TITLE);

        //Populates the mCourses list with info from course_info table
        loadCoursesFromDatabase(coursesCursor);

        final String[] columnNotes = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        //String noteOrderBy below specifies that query results returned
        //should first be arranged in ASCENDING order by course_id and then
        //arranged in the same order by note_title
        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + ", " + NoteInfoEntry.COLUMN_NOTE_TITLE;

        //querying the note_info table
        //returns data set containing
        // course_id, note_title and note_text fields
        final Cursor notesCursor = db.query(
                NoteInfoEntry.TABLE_NOTE_INFO,
                columnNotes,
                null, null, null, null, noteOrderBy);

        //Populates the mNotes list with data from note_info table
        loadNotesFromDatabase(notesCursor);
    }

    private static void loadNotesFromDatabase(Cursor cursor) {
        //stores the column indexes of the note_info table
        int idPos = cursor.getColumnIndex(NoteInfoEntry._ID);
        int noteTitlePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        int noteCourseIdPos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);

        DataManager dm = getInstance();
        //clears up the existing notes in the mNotes list before
        //loading it up with data from database
        dm.mNotes.clear();

        //While moving through each row in the notes_info table we're
        //retrieving and storing corresponding column values
        //Each column value is retrieved using the column index value
        //The retrieved column values for each row are used to
        //to create a new note which is then
        //added to the mNotes list
        while (cursor.moveToNext()) {
            //store these retrieved values
            int noteId = cursor.getInt(idPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);
            String noteCourseId = cursor.getString(noteCourseIdPos);

            //This retrieves a course that matches course_id value of the note_info table
            CourseInfo noteCourse = dm.getCourse(noteCourseId);

            //Creates a note from the retrieved cursor values and
            //then adds the note to the mNotes list
            NoteInfo note = new NoteInfo(noteId, noteCourse, noteTitle, noteText);
            dm.mNotes.add(note);
        }
        cursor.close();
    }

    //Loads up courses from database into mCourses List
    public static void loadCoursesFromDatabase(Cursor cursor) {
        //We need to obtain the index of each columns
        //Using hard coded the values of the column indexes makes our code fragile
        //Instead we obtain these indexes from cursor's getColumnIndex() method
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseTitlePos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);

        DataManager dm = getInstance();
        //clear any existing data about courses  on loading up
        dm.mCourses.clear();

        //Loop through results of each row till the end
        //adding each row info to the courseInfo list
        while (cursor.moveToNext()) {
            //This extracts the values of course_id and course_title columns
            //for each row/entry
            String courseId = cursor.getString(courseIdPos);
            String courseTitle = cursor.getString(courseTitlePos);

            //These values are used to populate the course instance
            //and then added to the courses list
            CourseInfo course = new CourseInfo(courseId, courseTitle, null);
            dm.mCourses.add(course);
        }
        //closes the cursor after use to
        //avoid leaking of system resources
        cursor.close();
    }

    public String getCurrentUserName() {
        return "Jim Wilson";
    }

    public String getCurrentUserEmail() {
        return "jimw@jwhh.com";
    }

    public List<NoteInfo> getNotes() {
        return mNotes;
    }

    public int createNewNote() {
        NoteInfo note = new NoteInfo(null, null, null);
        mNotes.add(note);
        int lastNotePosition = mNotes.size() - 1;
        return lastNotePosition;
    }

    public int findNote(NoteInfo note) {
        for (int index = 0; index < mNotes.size(); index++) {
            if (note.equals(mNotes.get(index)))
                return index;
        }
        return -1;
    }

    public void removeNote(int index) {
        mNotes.remove(index);
    }

    public List<NoteInfo> getNotes(CourseInfo course) {
        ArrayList<NoteInfo> notes = new ArrayList<>();
        for (NoteInfo note : mNotes) {
            if (course.equals(note.getCourse()))
                notes.add(note);
        }
        return notes;
    }

    public List<CourseInfo> getCourses() {
        return mCourses;
    }

    public CourseInfo getCourse(String id) {
        for (CourseInfo course : mCourses) {
            if (id.equals(course.getCourseId()))
                return course;
        }
        return null;
    }

    public int getNoteCount(CourseInfo course) {
        int count = 0;
        for (NoteInfo note : mNotes) {
            if (course.equals(note.getCourse()))
                count++;
        }
        return count;
    }

    private DataManager() {
    }

    public int createNewNote(CourseInfo course, String noteTitle, String noteText) {
        //creates a spot for the new note
        int index = createNewNote();

        NoteInfo newNote = DataManager.getInstance().getNotes().get(index);

        newNote.setCourse(course);
        newNote.setTitle(noteTitle);
        newNote.setText(noteText);

        return index;
    }
}
