package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperProviderContract.Notes;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class NoteBackup {
    public static final String ALL_COURSES = "ALL_COURSES";
    public static final String TAG = NoteBackup.class.getSimpleName();

    public static void backup(Context context, String backupCourseId) {
        //Specify columns to be returned
        String[] columns = new String[]{
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        //specify the selection criteria
        String selection = null;
        String[] selectionArgs = null;

        if (!backupCourseId.equals(ALL_COURSES)) {
            selection = Notes.COLUMN_COURSE_ID + "= ?";
            selectionArgs = new String[]{backupCourseId};
        }

        //query the db
        Cursor cursor = context.getContentResolver().query(Notes.CONTENT_URI, columns, selection, selectionArgs, null);

        //get column indexes
        int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, "*******BACKUP STARTED******* -- Thread -> " + Thread.currentThread().getId());
        while (cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            if (!noteTitle.equals("")) {
                //Log query results out to the log cat -- This action simulates a long process of writing the disk
                Log.i(TAG, ">>>>>>>BACKING UP<<<<<< " + courseId + "|" + noteTitle + "|" + noteText);
                simulateLongRunningWork();
            }
        }
        Log.i(TAG, ">>>>>>BACKUP COMPLETED<<<<<<");
        cursor.close();
    }

    private static void simulateLongRunningWork() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
    }
}
