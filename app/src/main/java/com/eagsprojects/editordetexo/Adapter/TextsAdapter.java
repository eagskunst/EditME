package com.eagsprojects.editordetexo.Adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eagsprojects.editordetexo.Model.TextModel;
import com.eagsprojects.editordetexo.R;

import java.util.List;

/**
 * Created by Emmanuel on 24/1/2018.
 */

public class TextsAdapter extends RecyclerView.Adapter<TextsAdapter.TextViewHolder>{

    private List<TextModel> textList;

    public TextsAdapter (List<TextModel> textList){
        this.textList = textList;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_texts,parent,false);
        TextViewHolder viewHolder = new TextViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.title.setText(textList.get(position).getTitle());
        holder.subtext.setText(textList.get(position).getSubtext());
    }

    @Override
    public int getItemCount() {
        return textList.size();
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder{

        private TextView title,subtext;
        private CardView cardView;

        public TextViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardview);
            title = itemView.findViewById(R.id.texttitle);
            subtext = itemView.findViewById(R.id.subtext);
        }
    }

}
