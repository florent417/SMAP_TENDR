package smap.gr15.appproject.tendr.models;

import java.util.Date;

public class ChatMessage {
    private String message;
    private String sender;
    private Date timeStamp;

    public ChatMessage(){

    }

    public ChatMessage(String message, String sender, Date timeStamp) {
        this.message = message;
        this.sender = sender;
        this.timeStamp = timeStamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
    // Dont send gifs and pictures
}
