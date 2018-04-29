package com.eagsprojects.editordetexo.Adapter;

import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.eagsprojects.editordetexo.Model.TextModel;
import com.eagsprojects.editordetexo.NewTextActivity;
import com.eagsprojects.editordetexo.R;

import java.util.List;

/**
 * Created by Emmanuel on 24/1/2018.
 * Sometimes I get confused about the adapter, so I would left a resume I got on stackoverflow!
 * 1. Creating a ViewHolder object for each RecylerView item.
 * 2. Returning the number of item in the data source.
 * 3. Binding data from the data source to each item.
 * 4. Inflating each item view that will be display.
 */

public class TextsAdapter extends RecyclerView.Adapter<TextsAdapter.TextViewHolder>{

    private List<TextModel> textList;
    private final OnItemClickListener clickListener;
    private final OnLongItemClickListener longClickListener;
    private static int cvColor;

    public TextsAdapter (List<TextModel> textList,int cvColor,OnItemClickListener clickListener, OnLongItemClickListener longClickListener){
        this.textList = textList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.cvColor = cvColor;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_texts,parent,false); //Inflating the cardview!
        TextViewHolder viewHolder = new TextViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.title.setText(textList.get(position).getTitle());
        holder.subtext.setText(textList.get(position).getSubtext());
        holder.bind(textList.get(position),clickListener,longClickListener,position);
    }

    @Override
    public int getItemCount() {
        return textList.size();
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder{

        private TextView title,subtext;
        private EditText text;
        private CardView cardView;

        public TextViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardview);
            cardView.setCardBackgroundColor(cvColor);
            title = itemView.findViewById(R.id.texttitle);
            subtext = itemView.findViewById(R.id.subtext);
            text = itemView.findViewById(R.id.edittext);
        }

        /*
        *I google for ways to handling the listeners and I liked this way
        * We call the bind model in the main class for setting the listener to each CardView
         */

        private void bind(final TextModel textModel, final OnItemClickListener clickListener, final OnLongItemClickListener longClickListener,final int position) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(textModel);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longClickListener.onItemLongClick(textModel,position);
                    return true;
                }
            });

        }
    }

    public void setTextList(List<TextModel> textList) {
        this.textList = textList;
    }

    public interface OnItemClickListener{
        void onItemClick(TextModel item);
    }

    public interface OnLongItemClickListener{
        void onItemLongClick(TextModel item,int position);
    }

}



