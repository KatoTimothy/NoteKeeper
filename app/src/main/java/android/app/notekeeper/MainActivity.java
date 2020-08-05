package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperProviderContract.Courses;
import android.app.notekeeper.NoteKeeperProviderContract.Notes;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "MainActivity";
    public static final int LOADER_NOTES = 0;
    private AppBarConfiguration mAppBarConfiguration;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private GridLayoutManager mCoursesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private NoteKeeperOpenHelper mDbOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start a new note activity
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        initializeDisplayContent();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_notes, R.id.nav_courses)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination,
                                             @Nullable Bundle arguments) {
                int id = destination.getId();

                //handler for notes
                if (id == R.id.nav_notes) {
                    displayNotes();
                }
                //handler for courses
                else if (id == R.id.nav_courses) {
                    displayCourses();
                }
            }
        });
        Log.i(TAG, "onCreate");
    }

    private void initializeDisplayContent() {
        //Load notes and courses list in database
        DataManager.loadFromDatabase(mDbOpenHelper);

        //Create a recyclerView to use to display
        //the list of courses or notes
        mRecyclerItems = (RecyclerView) findViewById(R.id.list_items);

        //create a linear layout manager to use to manage
        //the display of notes
        mNotesLayoutManager = new LinearLayoutManager(this);

        //Create a Grid LayoutManager to use manage the
        //display of courses
        mCoursesLayoutManager = new GridLayoutManager(this,
                getResources().getInteger(R.integer.grid_span));

        //set up the Notes Adapter
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        //set up the Courses Adapter
        final List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        displayNotes();

    }

    private void displayNotes() {
        //associate a LinearLayoutManager to the RecyclerView
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);

        //associate the notesRecyclerAdapter with the recycler view
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);
    }

    private void displayCourses() {
        //set the layout manager to grid
        mRecyclerItems.setLayoutManager(mCoursesLayoutManager);
        //associate the courses adapter with recycler view
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //re-query the database to get the latest notes from note_info table
        LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);
        upDateNavHeader();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void upDateNavHeader() {
        //get reference to navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //get reference to header view
        View headerView = navigationView.getHeaderView(0);
        //get reference to text views within the nav
        TextView textUserName = headerView.findViewById(R.id.text_user_name);
        TextView textEmailAddress = headerView.findViewById(R.id.text_email_address);

        //Reference to shared preferences file
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String userName = pref.getString(
                getString(R.string.key_user_display_name), "");
        String emailAddress = pref.getString(
                getString(R.string.key_user_email_address), "");

        //set the text views in the header
        textUserName.setText(userName);
        textEmailAddress.setText(emailAddress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = loadAllNotes();

        return loader;
    }

    private CursorLoader loadAllNotes() {
        //content://android.app.notekeeper.provider/notes_expanded
        Uri uri = Notes.CONTENT_URI_EXPANDED;

        //specify columns to be returned from database when query is performed
        final String[] projection = {
                Notes._ID,
                Notes.COURSE_TITLE,
                Notes.NOTE_TITLE
        };

        //specifies that data returned be arranged in ASCENDING order by courseId as primary sort
        // then by note title
        String noteOrderBy = Notes.NOTE_TITLE + ", " + Courses.COURSE_TITLE;

       return new CursorLoader(this, uri, projection, null, null, noteOrderBy);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            //associate the cursor with the adapter
            mNoteRecyclerAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES)
            //reset cursor
            mNoteRecyclerAdapter.changeCursor(null);
    }
}
