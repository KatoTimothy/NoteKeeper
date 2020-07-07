package android.app.notekeeper;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {
    // Makes non creatable
    private NoteKeeperDatabaseContract() {
    }

    //The CourseInfo table
    public static final class CourseInfoEntry implements BaseColumns{
        //table name
        public static final String TABLE_COURSE_INFO = "course_info";

        //Column names
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        //SQL statements
        //CREATE TABLE course_info(_id, course_id,course_title)
        public static final String SQL_CREATE_TABLE=
                "CREATE TABLE "+ TABLE_COURSE_INFO + "(" +
                        _ID + " INTEGER PRIMARY KEY,"+
                        COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL,"+
                        COLUMN_COURSE_TITLE + " TEXT NOT NULL)";
    }

    //The NoteInfoEntry table
    public static final class NoteInfoEntry implements BaseColumns {
        //table
        public static final String TABLE_NOTE_INFO = "note_info";

        //Columns
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        //SQL Statements

        //CREATE TABLE note_info (_id, note_title, note_text, course_id)
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE "+ TABLE_NOTE_INFO + "("+
                        _ID + " INTEGER PRIMARY KEY, "+
                        COLUMN_NOTE_TITLE + " TEXT NOT NULL, "+
                        COLUMN_NOTE_TEXT + " TEXT,"+
                        COLUMN_COURSE_ID +" TEXT NOT NULL)";
    }
}
