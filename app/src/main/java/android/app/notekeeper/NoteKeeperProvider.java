package android.app.notekeeper;

import android.app.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import android.app.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.app.notekeeper.NoteKeeperProviderContract.*;

public class NoteKeeperProvider extends ContentProvider {
    public static final String MIME_TYPE_VENDOR = "vnd." + AUTHORITY + ".";
    //Define the Uri matcher
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;
    public static final int NOTES_ROW = 3;

    //Define accepted Uri schemes
    static {
        //matches content://android.app.notekeeper.provider/courses
        sUriMatcher.addURI(AUTHORITY, Courses.CONTENT_PATH_COURSES, COURSES);
        //matches content://android.app.notekeeper.provider/notes
        sUriMatcher.addURI(AUTHORITY, Notes.CONTENT_PATH_NOTES, NOTES);
        //matches content://android.app.notekeeper.provider/notes_expanded
        sUriMatcher.addURI(AUTHORITY, Notes.CONTENT_PATH_NOTES_EXPANDED, NOTES_EXPANDED);
        //matches content://android.app.notekeeper.provider/notes/#
        sUriMatcher.addURI(AUTHORITY, Notes.CONTENT_PATH_NOTES + "/#", NOTES_ROW);
    }

    private NoteKeeperOpenHelper mdbOpenHelper;

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mdbOpenHelper.getWritableDatabase();
        int rowsAffected = 0;
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case NOTES_ROW:
                Long rowId = ContentUris.parseId(uri);

                String rowSelection = Notes._ID + "=?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};

                rowsAffected = db.delete(NoteInfoEntry.TABLE_NOTE_INFO, rowSelection, rowSelectionArgs);
                break;
        }
        return rowsAffected;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);

        //for a given Uri
        switch (uriMatch) {
            case NOTES:
                //vnd.android.cursor.dir/vnd.android.app.notekeeper.provider.notes
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_TYPE_VENDOR + Notes.CONTENT_PATH_NOTES;
                break;

            case COURSES:
                //vnd.android.cursor.dir/vnd.android.app.notekeeper.provider.courses
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_TYPE_VENDOR + Courses.CONTENT_PATH_COURSES;
                break;

            case NOTES_EXPANDED:
                //vnd.android.cursor.item/vnd.android.app.notekeeper.provider.notes_expanded
                mimeType = ContentResolver.ANY_CURSOR_ITEM_TYPE + "/" +
                        MIME_TYPE_VENDOR + Notes.CONTENT_PATH_NOTES_EXPANDED;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mdbOpenHelper.getWritableDatabase();

        //inserting a row returns a row id
        long rowId = -1;
        //operation returns uri of new row that identifies a newly inserted row
        Uri rowUri = null;

        //translate the uri
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NOTE_INFO, null, values);
                // content://android.app.notekeeper.provider/notes/1
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId); //constructing the row uri
                break;

            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_COURSE_INFO, null, values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;

            case NOTES_EXPANDED:
                //Throw an exception
                break;
        }
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        //initialize mOpenHelper instance
        mdbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        int uriMatch = sUriMatcher.match(uri);

        SQLiteDatabase db = mdbOpenHelper.getReadableDatabase();

        switch (uriMatch) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_COURSE_INFO, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NOTE_INFO, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case NOTES_EXPANDED:
                cursor = queryExpandedNotes(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                //Extract the row id from uri
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + "  = ? ";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};

                //perform query for that row
                cursor = db.query(NoteInfoEntry.TABLE_NOTE_INFO, projection, rowSelection,
                        rowSelectionArgs, null, null, null);
                break;
        }
        return cursor;
    }

    //Returns a cursor that contains combined results of note_info and course_info table
    private Cursor queryExpandedNotes(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs,
                                      String sortOrder) {

        //This array is same in length as projection  argument's array above
        String[] columns = new String[projection.length];

        //creates an array that contains qualified names of table columns equal to _id or course_id
        for (int id = 0; id < projection.length; id++) {
            columns[id] = projection[id].equals(BaseColumns._ID) || projection[id].equals(CourseIdColumn.COURSE_ID) ?
                    NoteInfoEntry.getQualifiedColumnName(projection[id]) :
                    projection[id];
        }

        //SQL Statement -> note_info JOIN course_info ON note_info.course_id = course_info.course_id
        String tablesWithJoin = NoteInfoEntry.TABLE_NOTE_INFO + " JOIN " + CourseInfoEntry.TABLE_COURSE_INFO + " ON " +
                NoteInfoEntry.getQualifiedColumnName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQualifiedColumnName(CourseInfoEntry.COLUMN_COURSE_ID);

        return db.query(tablesWithJoin, columns, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriMatch = sUriMatcher.match(uri);
        int rowsAffected = 0;
        SQLiteDatabase db = mdbOpenHelper.getWritableDatabase();

        switch (uriMatch) {
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);

                String rowSelection = Notes._ID + " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};

                rowsAffected = db.update(NoteInfoEntry.TABLE_NOTE_INFO, values, rowSelection, rowSelectionArgs);
                break;
        }
        return rowsAffected;
    }
}
