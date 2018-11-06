package com.johnyhawkdesigns.a52_roomnotesapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.johnyhawkdesigns.a52_roomnotesapp.adapter.NotesAdapter;
import com.johnyhawkdesigns.a52_roomnotesapp.notedb.NoteDatabase;
import com.johnyhawkdesigns.a52_roomnotesapp.notedb.model.Note;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteItemClick {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int NEW_NOTE_ACTIVITY_REQUEST_CODE = 100;

    private TextView textViewMsg;
    private RecyclerView recyclerView;
    private NoteDatabase noteDatabase;
    private List<Note> notes;
    private NotesAdapter notesAdapter;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewMsg =  findViewById(R.id.tv__empty);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Launch new AddNoteActivity");
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                // Start new activity for result means when this activity is finished, data will be returned in onActivityResult
                startActivityForResult(intent, NEW_NOTE_ACTIVITY_REQUEST_CODE);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        notes = new ArrayList<>();
        notesAdapter = new NotesAdapter(notes, MainActivity.this);
        recyclerView.setAdapter(notesAdapter);

        displayList();
    }


    // Method to get instance of Database and retrieve data using AsyncTask (RetrieveTask)
    private void displayList() {
        noteDatabase = NoteDatabase.getInstance(MainActivity.this);
        new RetrieveTask(this).execute();
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<Note>> {

        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        RetrieveTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Note> doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground: retrieveTask. getNotes() from Dao");
            if (activityReference.get() != null)
                return activityReference.get().noteDatabase.getNoteDao().getNotes(); // This line will return List<Note> to onPostExecute
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            if (notes != null && notes.size() > 0) {
                activityReference.get().notes.clear(); // clear old notes
                activityReference.get().notes.addAll(notes); // add retrieved notes to list
                // hides empty text view
                activityReference.get().textViewMsg.setVisibility(View.GONE);
                activityReference.get().notesAdapter.notifyDataSetChanged();
            }
        }
    }


    // This result is redirected from AddNoteActivity's method setResult. Below method checks for associated resultCode which we sent like this
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);

        // We only have one request code which is NEW_NOTE_ACTIVITY_REQUEST_CODE = 100
        if (requestCode == NEW_NOTE_ACTIVITY_REQUEST_CODE && resultCode > 0) {
            if (resultCode == 1) {
                Note newNote = (Note) data.getSerializableExtra("note");
                notes.add(newNote);
                Log.d(TAG, "onActivityResult: resultCode = 1, new note added. Note title = " + newNote.getTitle());
            } else if (resultCode == 2) {
                Note updateNote = (Note) data.getSerializableExtra("note");
                Log.d(TAG, "onActivityResult: resultCode = 2 for updated note = " + updateNote.getTitle());
                notes.set(pos, updateNote);
            }
            listVisibility(); // Confirm our list visibility
        }

        else if (resultCode == 0){
            Log.d(TAG, "onActivityResult: resultCode = 0 for cancelled activity means back button is pressed ");
        }
    }

    @Override
    public void onNoteClick(final int pos) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Select Options")
                .setItems(new String[]{"Delete", "Update"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                noteDatabase.getNoteDao().deleteNote(notes.get(pos));
                                Log.i(TAG, "onClick: delete item at position = " + pos + ", Note Title = " + notes.get(pos).getTitle());
                                notes.remove(pos);
                                listVisibility(); // Recheck if the list is now empty so we can update textView with message
                                break;
                            case 1:
                                MainActivity.this.pos = pos;
                                Note updateNote = notes.get(pos);
                                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class).putExtra("note", updateNote);
                                Log.i(TAG, "onClick: update item title = " + updateNote.getTitle());
                                startActivityForResult(intent, NEW_NOTE_ACTIVITY_REQUEST_CODE);
                                break;
                        }
                    }
                }).show();

    }

    // If RecyclerView is empty (notes.size() == 0) , show textView. If it's not empty hide textViewMsg
    private void listVisibility() {
        int hideEmptyTextMessage = View.GONE;

        if (notes.size() == 0) { // no item to display
            if (textViewMsg.getVisibility() == View.GONE) // just additional check to see if it's already GONE
                hideEmptyTextMessage = View.VISIBLE;
        }

        textViewMsg.setVisibility(hideEmptyTextMessage);
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        noteDatabase.cleanUp();
        noteDatabase.getNoteDao().deleteAll(); // delete all previously stored notes. This is just for testing.
        super.onDestroy();
    }
}
