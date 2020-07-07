package android.app.notekeeper;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.pressBack;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
	static DataManager sDataManager;
	//Before all tests
	//get the dataManager instance
	@BeforeClass
	//methods annotated with @BeforeClass
	//must be static
	public static void classSetUp() {
		sDataManager = DataManager.getInstance();
	}
	
	//JUNIT will take care of
	//managing the Activity Lifetime of the NoteListActivity
	//ie creation and destruction
	@Rule
	public ActivityTestRule<MainActivity> mNoteListActivityTestRule =
			new ActivityTestRule<>(MainActivity.class);
	
	
	@Test
	public void createNewNote() {
		//get course
		CourseInfo testCourse = sDataManager.getCourse("android_async");
		String testTitle = "My Test Title.";
		String testText = "This is a test body text.";
		
		 //a view interaction object that is returned
		//from the onView() method
		//allows us to perform actions on
		//fab button
		//with a perform() method
		
		//ViewInteraction fabButton = onView(withId(R.id.fab));
		//fabButton.perform(click());
		
		//The above two steps can be done in a single line
		//by chaining perform() on the
		//interaction object returned by the
		//onView method
		
		//perform a click action on a fab
		onView(withId(R.id.fab)).perform(click());
		
		//Performing interactions on the Spinner
		
		//The proceeding code implies
		//Find all AdapterViews on this activity and
		//find the one that holds a CourseInfo instance
		//whose is equal to our testCourse variable
		//rather than going to the spinner and
		// finding the View that
		//corresponds to that selection

		//find spinner and click on it
		onView(withId(R.id.spinner_courses)).perform(click());

		//find spinner item and click on it
		onData(allOf(instanceOf(CourseInfo.class), equalTo(testCourse))).perform(click())
				//find the text view and check that spinner
				//is part of the string
				.check(matches(withText(containsString(testCourse.getTitle()))));
		
		//perform a typeText action on the noteTitle text field
		onView(withId(R.id.text_note_title)).perform(typeText(testTitle));
		//perform a typeText action in the noteText text field
		onView(withId(R.id.text_note_text)).perform(typeText(testText), closeSoftKeyboard());
		
		//push back button at the end of the test
		pressBack();
		
	}
}