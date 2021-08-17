package com.example.instagramclone.model;

import java.util.Date;

public class Post extends PostId{

    private String user , text , image;
    private Date time ;

    public String getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public String getImage() {
        return image;
    }

    public Date getTime() {
        return time;
    }
}
