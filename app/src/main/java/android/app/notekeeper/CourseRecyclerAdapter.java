package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperProviderContract.Courses;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder> {
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;

    public CourseRecyclerAdapter(Context context, Cursor cursor) {
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.list_item_course, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentCourseTitle = getCourseTitleAt(position);

        //display data in the text view
        holder.mTextCourse.setText(currentCourseTitle);
    }

    public String getCourseTitleAt(int position) {
        //move to current item position
        mCursor.moveToPosition(position);
        //get column indexes
        int courseTitlePos = mCursor.getColumnIndex(Courses.COURSE_TITLE);
        //get course title
        return mCursor.getString(courseTitlePos);
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        //reference to the text view
        public final TextView mTextCourse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //store references to the views in
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);

            //add click listener to top level view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentPosition = getAdapterPosition();
                    String courseTitle = getCourseTitleAt(currentPosition);

                    Snackbar.make(v, courseTitle, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    public void changeCursor(Cursor cursor) {
        //close old cursor if it exists
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
