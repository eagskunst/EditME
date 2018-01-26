package com.eagsprojects.editordetexo.Model;

/**
 * Created by Emmanuel on 24/1/2018.
 */

public class TextModel {


    private String title;
    private String subtext;

    public TextModel(String title, String subtext){
        this.title = title;
        this.subtext = subtext;
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


}
