package com.example.connect;

public class Messages
{
    private String message,type;
    private Boolean seen;
    private long time;
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Messages(String message, String type, Boolean seen, long time) {
        this.message = message;
        this.type = type;
        this.seen = seen;
        this.time = time;
    }

    private Messages()
    {

    }




}
