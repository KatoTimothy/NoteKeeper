package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperProviderContract.Notes;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NoteUploader {
    private final Context mContext;
    private boolean mIsCanceled;
    private final String TAG = getClass().getSimpleName();

    NoteUploader(Context context) {
        mContext = context;
    }

    public void cancel() {
        mIsCanceled = true;
    }

    public boolean isCanceled() {
        return mIsCanceled;
    }

    //simulates a note upload process over a network
    public void upLoadNote(Uri uri) {
        //columns to return
        String[] columns = new String[]{
                Notes.COLUMN_COURSE_ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_NOTE_TEXT
        };

        //query the database
        Cursor cursor = mContext.getContentResolver().query(uri, columns, null, null, null);

        assert cursor != null;
        int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, ">>>>>>>>UPLOAD START<<<<<<<: Thread:" + Thread.currentThread().getId());
        while (!mIsCanceled && cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            if (!noteTitle.equals("")) {
                Log.i(TAG, ">>>>>>>UPLOADING<<<<<<< " + courseId + "|" + noteTitle + "|" + noteText + "|");
                simulateLongRunningJob();
            }
        }
        Log.i(TAG, ">>>>>>UPLOAD ENDED<<<<<<");
    }

    //simulates a long running job for 3 seconds
    private void simulateLongRunningJob() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
    }
}
