package com.example.connect;

public class Friends
{
    public String date;
    public String name;
    public String thumb_image;
    public String online_status;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getOnline_status() {
        return online_status;
    }

    public void setOnline_status(String online_status) {
        this.online_status = online_status;
    }

    public Friends()
    {

    }

    public Friends(String date, String name, String thumb_image, String online_status) {
        this.date = date;
        this.name = name;
        this.thumb_image = thumb_image;
        this.online_status = online_status;
    }
}
