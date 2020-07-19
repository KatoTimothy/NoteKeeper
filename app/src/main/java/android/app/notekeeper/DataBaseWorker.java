package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Initialises the database
public class DataBaseWorker {
    private SQLiteDatabase mDb;

    public DataBaseWorker(SQLiteDatabase db) {
        mDb = db;
    }

    //Inserts  a single course into course_info table
    private void insertCourse(String course_id, String course_title) {

        ContentValues course = new ContentValues();

        course.put(CourseInfoEntry.COLUMN_COURSE_ID, course_id);
        course.put(CourseInfoEntry.COLUMN_COURSE_TITLE, course_title);

        mDb.insert(CourseInfoEntry.TABLE_COURSE_INFO, null, course);
    }

    //Populates all rows in the course_info table
    public void insertCourses() {
        insertCourse("android_intents", "Android Programming with Intents");
        insertCourse("android_async", "Android Async Programming and Services");
        insertCourse("java_lang", "Java Fundamentals: The Java Language");
        insertCourse("java_core", "Java Fundamentals: The Java Core");
    }

    //Populates a single row of note_info table
    private void insertNote(String note_title, String note_text, String course_id) {
        ContentValues note = new ContentValues();

        note.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TITLE, note_title);
        note.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_NOTE_TEXT, note_text);
        note.put(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_COURSE_ID, course_id);

        mDb.insert(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NOTE_INFO, null, note);
    }

    //Populates multiple rows of note_info table
    public void insertSampleNotes() {
        insertNote("Dynamic Intent Resolution",
                "Wow, intents allow components to be resolved at runtime.",
                "android_intents");
        insertNote("Delegating intents",
                "Pending Intents are powerful;they delegate much more than just a component invocation.",
                "android_intents");

        insertNote("Service default threads",
                "Did you know that by default an Android service will tie up the UI thread?",
                "android_async");
        insertNote("Long running operations",
                "Foreground Services can be tied to a notification icon.",
                "android_async");

        insertNote("Parameters",
                "Leverage variable-length parameter lists",
                "java_lang");
        insertNote("Anonymous classes",
                "Anonymous classes simplify implementing one-use types.",
                "java_lang");

        insertNote("Compiler options",
                "The -jar option isn't compatible with the -cp option",
                "java_core");
        insertNote("Serialization",
                "Remember to include SerialVersionUID to assure version compatibility",
                "java_core");
    }

}
