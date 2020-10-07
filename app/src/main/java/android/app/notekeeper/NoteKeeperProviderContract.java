package android.app.notekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {

    //prevent accidental creation of the class
    private void NoteKeeperDatabaseContract() {
    }

    //android.app.notekeeper.provider
    public static final String AUTHORITY = "android.app.notekeeper.provider";

    //content://android.notekeeper.provider"
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    //Exposes column that describes the course title
    protected interface CourseIdColumn {
        public static String COLUMN_COURSE_ID = "course_id";
    }

    //Exposes column  that describes a course
    protected interface CoursesColumns {
        public static final String COURSE_TITLE = "course_title";
    }

    //Exposes columns that describe a note
    protected interface NotesColumns {
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
    }

    //Exposes information that describes a course
    public static final class Courses implements BaseColumns, CoursesColumns, CourseIdColumn {

        //Uri content://android.app.notekeeper.provider/courses
        public static final String CONTENT_PATH_COURSES = "courses";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, CONTENT_PATH_COURSES);
    }

    //Exposes information that describes a note
    public static final class Notes implements BaseColumns, NotesColumns, CourseIdColumn, CoursesColumns {

        //Uri content://android.app.notekeeper.provider/notes
        public static final String CONTENT_PATH_NOTES = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, CONTENT_PATH_NOTES);

        // Uri  content://android.app.notekeeper.provider/notes_expanded
        public static final String CONTENT_PATH_NOTES_EXPANDED = "notes_expanded";
        public static final Uri CONTENT_URI_EXPANDED = Uri.withAppendedPath(BASE_URI, CONTENT_PATH_NOTES_EXPANDED);
    }
}
