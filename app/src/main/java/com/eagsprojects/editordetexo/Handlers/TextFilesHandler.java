package com.eagsprojects.editordetexo.Handlers;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Emmanuel on 30/3/2018.
 * Create this class in order to use less memory when creating new Fragments.
 * Instead of loading every time all the text files, I use the refreshList() method
 * Implements Parcelable in order to put this Object in a Bundle
 */

public final class TextFilesHandler implements Parcelable{

    private File root;
    private String s[];
    private ArrayList<String> titles = new ArrayList<>(),subtexts = new ArrayList<>(),completeText = new ArrayList<>();

    public TextFilesHandler(){

        root = new File(Environment.getExternalStorageDirectory(),"Texts");
        if(!root.exists()){//if the directory on the internal storage doesn't exist, create it.
            root.mkdir();
        }

        s = root.list();//Getting the titles of the files

        setTitlesAndSubtexts(s,titles,subtexts,completeText);

    }

    protected TextFilesHandler(Parcel in) {
        titles = in.createStringArrayList();
        subtexts = in.createStringArrayList();
        completeText = in.createStringArrayList();
    }

    public static final Creator<TextFilesHandler> CREATOR = new Creator<TextFilesHandler>() {
        @Override
        public TextFilesHandler createFromParcel(Parcel in) {
            return new TextFilesHandler(in);
        }

        @Override
        public TextFilesHandler[] newArray(int size) {
            return new TextFilesHandler[size];
        }
    };

    private void setTitlesAndSubtexts(String[] s, ArrayList<String> titles, ArrayList<String> subtexts, ArrayList<String> completeText) {
        /*
        *Loop for adding the titles, subtexts and complete text.
        * Is easy to add the titles, we already have it in our s variable.
         */

        for (int i = 0;i<s.length;i++){
            titles.add(s[i]);
            File file = new File(root,s[i]);
            StringBuilder texts = new StringBuilder();//Found about this method while doing this! Handles mutables character sequences
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line,subtext;
                line = br.readLine();
                if(line.length()>155){
                    subtext = line.substring(0,155);//If first line to large, split it with '...'
                    subtexts.add(subtext+"...");
                    texts.append(line);//Appends adds more text
                    texts.append('\n');
                }
                else{
                    subtexts.add(line);//If not, well just add the line.
                    texts.append(line);
                    texts.append('\n');
                }
                while ((line = br.readLine()) != null) {
                    texts.append(line);
                    texts.append('\n');
                }
                texts.setLength(texts.length() - 1);
                completeText.add(texts.toString());
                br.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getS() {
        return s;
    }

    public void setS(String[] s) {
        this.s = s;
    }

    public ArrayList<String> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<String> titles) {
        this.titles = titles;
    }

    public ArrayList<String> getSubtexts() {
        return subtexts;
    }

    public void setSubtexts(ArrayList<String> subtexts) {
        this.subtexts = subtexts;
    }

    public ArrayList<String> getCompleteText() {
        return completeText;
    }

    public void setCompleteText(ArrayList<String> completeText) {
        this.completeText = completeText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(titles);
        parcel.writeList(subtexts);
        parcel.writeList(completeText);
    }

    public void refreshList(){
        s = root.list();//Getting the titles of the files
        titles.clear();
        subtexts.clear();
        completeText.clear();

        setTitlesAndSubtexts(s,titles,subtexts,completeText);
    }
}
