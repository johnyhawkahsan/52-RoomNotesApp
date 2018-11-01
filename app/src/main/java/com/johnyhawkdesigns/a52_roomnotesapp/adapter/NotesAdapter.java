package com.johnyhawkdesigns.a52_roomnotesapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.johnyhawkdesigns.a52_roomnotesapp.R;
import com.johnyhawkdesigns.a52_roomnotesapp.notedb.model.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    // Interface to pass clicked item position info
    public interface OnNoteItemClick{
        void onNoteClick(int pos);
    }

    private static final String TAG = NotesAdapter.class.getSimpleName();
    private List<Note> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private OnNoteItemClick onNoteItemClick; // Create an object of the interface

    // Constructor
    public NotesAdapter(List<Note> list, Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.list = list;
        this.context = context;
        this.onNoteItemClick = (OnNoteItemClick) context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.note_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: list.get(position) = "+ list.get(position));
        holder.textViewTitle.setText(list.get(position).getTitle());
        holder.textViewContent.setText(list.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder class which holds our note_list_item view and objects
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewContent;
        TextView textViewTitle;

        // Constructor
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textViewContent = itemView.findViewById(R.id.item_text);
            textViewTitle = itemView.findViewById(R.id.tv_title);
        }

        @Override
        public void onClick(View view) {
            onNoteItemClick.onNoteClick(getAdapterPosition());
            Log.d(TAG, "onClick: onNoteItemClick.onNoteClick(getAdapterPosition()) = " + getAdapterPosition());
        }
    }


}