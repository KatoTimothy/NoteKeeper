package android.app.notekeeper;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.List;

public class NoteListActivity extends AppCompatActivity {
    private NoteRecyclerAdapter mNoteRecyclerAdapter;

//    private ArrayAdapter<NoteInfo> mAdapterNotes;
    
    @Override
    protected void onResume() {
        super.onResume();
//        mAdapterNotes.notifyDataSetChanged();
        mNoteRecyclerAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Loads the activityNoteList layout resource
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //start a new note activity
                startActivity(new Intent(NoteListActivity.this, NoteActivity.class));
            }
        });
        //initializes the display of the Notes
        initializeDisplayContent();
    }

    private void initializeDisplayContent() {
        //get reference to the List View
//      ListView ListNotes = findViewById(R.id.list_notes);

        //set up the adapter view
        //Fetches and stores a list of notes from the data manager
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//
//        //Creates a Spinner View from a list notes
////        mAdapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes);
//
//        //associates SpinnerView created from the SpinnerAdapter with the ListView
////        ListNotes.setAdapter(mAdapterNotes);
//
//        //If a user clicks one of notes in the notes list,
//        //an intent to start the next NoteActivity is sent
//        ListNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterNotes, View view, int position, long id) {
//
//                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
//
//                //This returns the data associated with the selected note
//                //NoteInfo note = (NoteInfo) ListNotes.getItemAtPosition(position);
//
//                //Includes position of selected note
//                // as intent extra to the NoteActivity
//                intent.putExtra(NoteActivity.NOTE_POSITION, position);
//                //start up the NoteActivity
//                startActivity(intent);
//            }
//        });
        //list_notes stores a reference to our RecyclerView
        final RecyclerView recyclerNotes = (RecyclerView)findViewById(R.id.list_items);
        
        //Create layoutManager
        //We use a LinearLayOutManager here
        //We pass in the context. In this case it is the
        //NoteListActivity class
        final LinearLayoutManager notesLayoutManager = new LinearLayoutManager(this);
        
        //Next, is to
        //associate the recyclerView with the Layout Manager
        recyclerNotes.setLayoutManager(notesLayoutManager);
    
        final List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, notes);
        //associate the adapter with the recycler view
        recyclerNotes.setAdapter(mNoteRecyclerAdapter);
    }
}
