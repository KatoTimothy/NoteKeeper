package android.app.notekeeper;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManagerTest {
	
	private static DataManager sDataManager;
	
	@BeforeClass
	static public void classSetup(){
		sDataManager = DataManager.getInstance();
	}
	@Before
	public void setup(){
		sDataManager.getNotes().clear();
		sDataManager.initializeExampleNotes();
	}
	@Test
	public void createNewNote() {
		final CourseInfo course = sDataManager.getCourse("android_async");
		final String noteTitle = "Test note title.";
		final String noteText = "This is the body of note one.";
		
		//creates new note and returns the position
		//of newly created note
		int noteIndex = sDataManager.createNewNote();
		//get note the new spot
		NoteInfo newNote = sDataManager.getNotes().get(noteIndex);
		//populate the new note with data
		newNote.setCourse(course);
		newNote.setTitle(noteTitle);
		newNote.setText(noteText);
		
		NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
		
		assertEquals(course, compareNote.getCourse());
		assertEquals(noteTitle, compareNote.getTitle());
		assertEquals(noteText, compareNote.getText());
	}
	
	@Test
	public void findSimilarNotes(){
		DataManager dm = DataManager.getInstance();
		final CourseInfo course = dm.getCourse("android_async");
		final String noteTitle = "Test note title.";
		final String noteText1 = "This is the body of note one.";
		final String noteText2 = "This is the body of note two.";
		
	//create the first note
		int noteIndex1 = dm.createNewNote();
		NoteInfo note1 = dm.getNotes().get(noteIndex1);
		note1.setCourse(course);
		note1.setTitle(noteTitle);
		note1.setText(noteText1);
	
	//create the second note
		int noteIndex2 = dm.createNewNote();
	//get the new note instance
		NoteInfo note2 = dm.getNotes().get(noteIndex2);
		//populate the note
		note2.setCourse(course);
		note2.setTitle(noteTitle);
		note2.setText(noteText2);
		
		int foundNote1 = dm.findNote(note1);
		assertEquals(noteIndex1, foundNote1);
		
		int foundNote2 = dm.findNote(note2);
		assertEquals(noteIndex2, foundNote2);
	}
	
	@Test
	public void createNewNoteOneStepCreation(){
		CourseInfo course = sDataManager.getCourse("android_async");
		String noteTitle = "Text note title";
		String noteText = "This is the body of note.";
		
		int noteIndex = sDataManager.createNewNote(course, noteTitle, noteText);
		
		NoteInfo compareNote = sDataManager.getNotes().get(noteIndex);
		assertEquals(course, compareNote.getCourse());
		assertEquals(noteTitle, compareNote.getTitle());
		assertEquals(noteText, compareNote.getText());
		
	}
	
}