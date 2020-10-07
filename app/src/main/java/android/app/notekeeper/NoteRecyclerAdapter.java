package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.app.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import android.app.notekeeper.NoteKeeperProviderContract.Notes;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void changeCursor(Cursor cursor) {
        //Close old cursor if it exists
        if (mCursor != null) {
            cursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
        //notify RecyclerAdapter when data set changes
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.list_item_note, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // position the cursor to current item position
        if (mCursor != null) {
            mCursor.moveToPosition(position);

            //get column indexes
            int courseTitlePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
            int noteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);

            //retrieve row values
            String courseId = mCursor.getString(courseTitlePos);
            String noteTitle = mCursor.getString(noteTitlePos);

            //Binds cursor values to views in the viewHolder
            holder.mTextCourse.setText(courseId);
            holder.mTextTitle.setText(noteTitle);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //made public so that outer class can refer to them directly
        public final TextView mTextCourse;
        public final TextView mTextTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //store references to the views in
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);
            mTextTitle = (TextView) itemView.findViewById(R.id.text_title);

            //add click listener to top level view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //get position of currently selected item in the recycler view
                    int currentNotePosition = getAdapterPosition();

                    //retrieve the corresponding  id value  of the selected item  from the database
                    int currentNoteId = getNoteIdAt(currentNotePosition);

                    //include id value in the intent extra and start the note activity
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, currentNoteId);
                    mContext.startActivity(intent);
                }
            });
        }

        //retrieves the id value of a note at a given position from the database
        private int getNoteIdAt(int position) {
            //move cursor to the desired row position
            mCursor.moveToPosition(position);
            int noteIdPos = mCursor.getColumnIndex(Notes._ID);
            return mCursor.getInt(noteIdPos);
        }
    }
}
