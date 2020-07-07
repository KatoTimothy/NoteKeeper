package android.app.notekeeper;

import androidx.lifecycle.ViewModel;
import android.os.Bundle;

public class NoteActivityViewModel extends ViewModel {
	public String mOriginalNoteCourseId;
	public String mOriginalNoteTitle;
	public String mOriginalNoteText;
	public boolean mIsNewlyCreated = true;

	public static final String ORIGINAL_COURSE_ID = "android.app.notekeeper ORIGINAL_COURSE_ID";
	public static final String ORIGINAL_NOTE_TITLE = "android.app.notekeeper ORIGINAL_NOTE_TITLE";
	public static final String ORIGINAL_NOTE_TEXT = "android.app.notekeeper ORIGINAL_NOTE_TEXT";
	
	//saves the state into bundle
	public void saveState(Bundle outState) {
		//To save state values into the bundle,
		//They should have names
		//follow the same idea and convention,
		//as we passed values to the intent
		//By creating string constants whose names are app defined
		//Each of those names are qualified by the applications package name
		//This is done so to avoid collisions of other names used to insert values in the bundle
		
		outState.putString(ORIGINAL_COURSE_ID, mOriginalNoteCourseId);
		outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
		outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
	}
	
	//restore state
	public void restoreState(Bundle inState) {
		mOriginalNoteCourseId = inState.getString(ORIGINAL_COURSE_ID, mOriginalNoteCourseId);
		mOriginalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
		mOriginalNoteText = inState.getString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
	}
}
