package android.app.notekeeper;

import android.app.IntentService;
import android.content.Intent;

public class NoteBackupService extends IntentService {

    public static final String EXTRA_ALL_COURSES = "android.app.notekeeper.extra.ALL_COURSES";

    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra(EXTRA_ALL_COURSES);
            //TODO: Back up notes in the background
            NoteBackup.backup(this, backupCourseId);
        }
    }
}
