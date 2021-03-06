package com.johnyhawkdesigns.a52_roomnotesapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.johnyhawkdesigns.a52_roomnotesapp.notedb.NoteDatabase;
import com.johnyhawkdesigns.a52_roomnotesapp.notedb.model.Note;

import java.lang.ref.WeakReference;

public class AddNoteActivity extends AppCompatActivity {

    private static final String TAG = AddNoteActivity.class.getSimpleName();

    private TextInputEditText et_title, et_content;
    private NoteDatabase noteDatabase;
    private Note note;
    private boolean update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        et_title = findViewById(R.id.et_title);
        et_content = findViewById(R.id.et_content);
        noteDatabase = NoteDatabase.getInstance(AddNoteActivity.this); // get single instance of NoteDatabase using singleton pattern
        Button button = findViewById(R.id.but_save);

        // If we are editing an existing note, we must receive note in the intent. Now we need
        if ( (note = (Note) getIntent().getSerializableExtra("note"))!=null )
        {
            Log.d(TAG, "onCreate: received note in getIntent from MainActivity = " + note.getTitle());

            getSupportActionBar().setTitle("Update Note");
            update = true;
            button.setText("Update"); // Change text from "Save" to "Update"
            et_title.setText(note.getTitle());
            et_content.setText(note.getContent());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    note.setTitle(et_title.getText().toString());
                    note.setContent(et_content.getText().toString());
                    noteDatabase.getNoteDao().updateNote(note);
                    setResult(note, 2);
                }
            });

        }
        // else if there is no intent data received, means we are adding a new Note
        else
        {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    note = new Note(et_content.getText().toString(), et_title.getText().toString() ); // Add new note when "save" button is clicked
                    Log.d(TAG, "onCreate: create a new note = " + note.getTitle());
                    new InsertTask(AddNoteActivity.this, note).execute(); //launch InsertTask
                }
            });
        }
    }

    private void setResult(Note note, int flag){
        Log.d(TAG, "setResult: setting result for note = " + note.getTitle() + ", flag = " + flag);
        setResult(flag, new Intent().putExtra("note", note));
        finish();
    }

    private static class InsertTask extends AsyncTask<Void, Void, Boolean>{

        private WeakReference<AddNoteActivity> activityReference;
        private Note note;

        // only retain a weak reference to the activity
        InsertTask(AddNoteActivity context, Note note) {
            activityReference = new WeakReference<>(context);
            this.note = note;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            // retrieve auto incremented note id
            long j = activityReference.get().noteDatabase.getNoteDao().insertNote(note);
            note.setNote_id(j);
            Log.d("ID ", "doInBackground: j = " + j );
            return true;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool){
                activityReference.get().setResult(note,1); // 1 is resultCode for new note added
                activityReference.get().finish(); // Finish this activity once result is set and new Note data sent to MainActivity
            }
        }

    }
}
