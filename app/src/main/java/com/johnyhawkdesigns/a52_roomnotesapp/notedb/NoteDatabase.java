package com.johnyhawkdesigns.a52_roomnotesapp.notedb;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.johnyhawkdesigns.a52_roomnotesapp.notedb.dao.NoteDao;
import com.johnyhawkdesigns.a52_roomnotesapp.notedb.model.Note;
import com.johnyhawkdesigns.a52_roomnotesapp.util.Constants;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDatabase extends RoomDatabase{

    public abstract NoteDao getNoteDao();

    private static NoteDatabase noteDB;

    public static NoteDatabase getInstance (Context context){
        if (null == noteDB){
            noteDB = buildDatabaseInstance(context);
        }
        return noteDB;
    }

    private static NoteDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(
                context,
                NoteDatabase.class,
                Constants.DB_NAME
        ).allowMainThreadQueries().build();
    }

    public void cleanUp(){
        noteDB = null;
    }

}
