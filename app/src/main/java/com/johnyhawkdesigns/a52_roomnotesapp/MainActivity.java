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
                //startActivity(intent);
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
                return activityReference.get().noteDatabase.getNoteDao().getNotes();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            if (notes != null && notes.size() > 0) {
                activityReference.get().notes.clear();
                activityReference.get().notes.addAll(notes);
                // hides empty text view
                activityReference.get().textViewMsg.setVisibility(View.GONE);
                activityReference.get().notesAdapter.notifyDataSetChanged();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);

        if (requestCode == NEW_NOTE_ACTIVITY_REQUEST_CODE && resultCode > 0) {
            if (resultCode == 1) {
                Log.d(TAG, "onActivityResult: resultCode ==1, new note added");
                notes.add((Note) data.getSerializableExtra("note"));
            } else if (resultCode == 2) {
                Note receivedNote = (Note) data.getSerializableExtra("note");
                Log.d(TAG, "onActivityResult: resultCode ==2 for updated note = " + receivedNote.getTitle());
                notes.set(pos, receivedNote);
            }
            listVisibility();
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
                                Log.i(TAG, "onClick: delete item at position = " + pos);
                                notes.remove(pos);
                                listVisibility();
                                break;
                            case 1:
                                MainActivity.this.pos = pos;
                                Log.i(TAG, "onClick: update item at position = " + pos);
                                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class).putExtra("note", notes.get(pos));
                                startActivityForResult(intent, NEW_NOTE_ACTIVITY_REQUEST_CODE);
                                break;
                        }
                    }
                }).show();

    }

    // If RecyclerView is empty (notes.size() == 0) , show textView. If it's not empty hide textViewMsg
    private void listVisibility() {
        int emptyMsgVisibility = View.GONE;

        if (notes.size() == 0) { // no item to display
            if (textViewMsg.getVisibility() == View.GONE) // just additional check to see if it's already GONE
                emptyMsgVisibility = View.VISIBLE;
        }

        textViewMsg.setVisibility(emptyMsgVisibility);
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        noteDatabase.cleanUp();
        super.onDestroy();
    }
}
