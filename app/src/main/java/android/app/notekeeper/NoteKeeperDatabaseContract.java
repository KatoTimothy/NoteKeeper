package android.app.notekeeper;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {
    // Makes non creatable
    private NoteKeeperDatabaseContract() {
    }

    //The CourseInfo table
    public static final class CourseInfoEntry implements BaseColumns {
        //table name
        public static final String TABLE_COURSE_INFO = "course_info";

        //Column names
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        //returns a qualified column name of the course_info table
        public static final String getQualifiedColumnName(String columnName) {
            return TABLE_COURSE_INFO + "." + columnName;
        }

        //SQL statements
        //CREATE TABLE course_info(_id, course_id,course_title)
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_COURSE_INFO + "(" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL," +
                        COLUMN_COURSE_TITLE + " TEXT NOT NULL)";

        //CREATE INDEX course_info_index1 ON note_info (course_title)
        public static final String index1 = TABLE_COURSE_INFO + "_index1";
        //Creates an index on course_title column of course_info table
        public static final String SQL_CREATE_INDEX1 =
                "CREATE INDEX " + index1 + " ON " + TABLE_COURSE_INFO +
                " ("+ COLUMN_COURSE_TITLE +")";
    }

    //The NoteInfoEntry table
    public static final class NoteInfoEntry implements BaseColumns {

        //table name
        public static final String TABLE_NOTE_INFO = "note_info";

        //Column names
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        //returns a qualified columnName of the note_info table
        public static final String getQualifiedColumnName(String columnName) {
            return TABLE_NOTE_INFO + "." + columnName;
        }

        //SQL Statements

        //CREATE TABLE note_info (_id, note_title, note_text, course_id)
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NOTE_INFO + "(" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NOTE_TITLE + " TEXT NOT NULL, " +
                        COLUMN_NOTE_TEXT + " TEXT," +
                        COLUMN_COURSE_ID + " TEXT NOT NULL)";

        //CREATE INDEX note_info_index1 ON note_info (note_title)
        public static final String index1 = TABLE_NOTE_INFO + "_index1";
        public static final String SQL_CREATE_INDEX1 =
                "CREATE INDEX "+ index1 + " ON "+ TABLE_NOTE_INFO +
                " (" + COLUMN_NOTE_TITLE  + ")";
    }
}
