package android.app.notekeeper;

import androidx.navigation.Navigation;
import androidx.test.espresso.contrib.*;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NextThroughNotesTest {
	//adding a rule for the activity to test
	//automates the
	//lifetime of the Activity under Test
	//In other words,
	//it starts an Activity before each test in the class and
	//terminates the activity after each test in the class
	@Rule
	public ActivityTestRule<MainActivity> mActivityTestRule
			= new ActivityTestRule<>(MainActivity.class);
	
	@Test
	public void nextThroughNotes() {
		//Verifying that we can select a note from the MainActivity involves

		//opening the drawer
		onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
		//and choosing selected the notes from the menu
		onView(withId(R.id.nav_view)).perform(NavigationViewActions
				.navigateTo(R.id.nav_notes));
		
		//then performing a click on an first item in the RecyclerView
		//this opens up the NoteActivity
		onView(withId(R.id.list_items)).perform(
	RecyclerViewActions.actionOnItemAtPosition(0, click()));
		
		//Now lets verify information contained in the note
		List<NoteInfo> notes = DataManager.getInstance().getNotes();

		//loop through a full range of notes
		//verifying that the right info is contained in the respective notes

		for(int index = 0; index < notes.size(); index++){
			//get the current note
			NoteInfo note = notes.get(index);

			//verify text in the spinner is contained in the note
			onView(withId(R.id.spinner_courses)).check(
					matches(withSpinnerText(note.getCourse().getTitle())
			));

			//verify text in the course title text field is the one in the
			//in the note
			onView(withId(R.id.text_note_title)).check(
					matches(withText(note.getTitle()))
			);

			//verify that text in the text field is the one
			//contained in the current note
			onView((withId(R.id.text_note_text))).check(
					matches(withText(note.getText()))
			);

			//perform a click to go next note in the range
			//and also check keep checking if the next button is enabled
			if(index < notes.size() -1)
//				onView(allOf(withId(R.id.action_next), isEnabled())).perform(click());
				onView(withId(R.id.action_next)).check(matches(isEnabled()));
				onView(withId(R.id.action_next)).perform(click());
		}
		//verify that the 'next' button in the menu is disabled
		onView(withId(R.id.action_next)).check(
				matches(not(isEnabled()))
		);
	}
}