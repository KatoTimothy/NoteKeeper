package android.app.notekeeper;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastHelper {
    public static final String ACTION_COURSE_EVENT = "android.app.notekeeper.action.COURSE_EVENT";

    public static final String EXTRA_COURSE_ID = "android.app.notekeeper.extra.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE = "android.app.notekeeper.extra.COURSE_MESSAGE";

    //handles details of sending a broadcast
    public static void sendEventBroadcast(Context context, String courseId, String message) {
        Intent intent = new Intent(ACTION_COURSE_EVENT);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_COURSE_MESSAGE, message);

        //sends a broadcast
        context.sendBroadcast(intent);
    }
}
