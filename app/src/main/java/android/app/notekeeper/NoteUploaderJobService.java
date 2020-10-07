package android.app.notekeeper;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "android.app.notekeeper.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        @SuppressLint("StaticFieldLeak") AsyncTask<JobParameters, Void, Void> task =
                new AsyncTask<JobParameters, Void, Void>() {
                    @Override
                    protected Void doInBackground(JobParameters... parameters) {
                        JobParameters jobParams = parameters[0];

                        String dataUriString = jobParams.getExtras().getString(EXTRA_DATA_URI);
                        Uri dataUri = Uri.parse(dataUriString);

                        //do the upload in background
                        mNoteUploader.upLoadNote(dataUri);

                        //Finish job
                        if (!mNoteUploader.isCanceled())
                            jobFinished(jobParams, false);

                        return null;
                    }
                };

        mNoteUploader = new NoteUploader(this);
        task.execute(jobParameters);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        //cancel note upload
        mNoteUploader.cancel();

        //Reschedule the Job
        return true;
    }
}
