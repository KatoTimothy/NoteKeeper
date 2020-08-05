package android.app.notekeeper;
import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.app.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteKeeperOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NoteKeeper.db";
    public static final int DATABASE_VERSION = 2;

    public NoteKeeperOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creates course_info table
        db.execSQL(CourseInfoEntry.SQL_CREATE_TABLE);
        //creates note_info
        db.execSQL(NoteInfoEntry.SQL_CREATE_TABLE);
        //Create and index on note_info table
        db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);
        //Create an index on course_info table
        db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);

        //DataBaseWorker class Contains an initial content values for course_info and note_info table
        DataBaseWorker worker = new DataBaseWorker(db);

        //initializes the course_info table
        worker.insertCourses();
        //initializes the note_info table
        worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 2)
            db.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1);
            db.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1);
    }
}
