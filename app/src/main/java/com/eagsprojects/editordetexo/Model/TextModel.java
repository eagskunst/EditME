package com.eagsprojects.editordetexo.Model;

/**
 * Created by Emmanuel on 24/1/2018.
 * Simple POJO. Not much more to add...
 */

public class TextModel {


    private String title;
    private String subtext;
    private String text;

    public TextModel(String title, String subtext, String text){
        this.title = title;
        this.subtext = subtext;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
