package smap.gr15.appproject.tendr.models;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class Conversation {
    private List<ChatMessage> chatMessages;

    public String getFirstUserId() {
        return firstUserId;
    }

    public void setFirstUserId(String firstUserId) {
        this.firstUserId = firstUserId;
    }

    private String firstUserId;

    public String getSecondUserId() {
        return secondUserId;
    }

    public void setSecondUserId(String secondUserId) {
        this.secondUserId = secondUserId;
    }

    private String secondUserId;



    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public void addChatMessage(ChatMessage chatMessage)
    {
        this.chatMessages.add(chatMessage);
    }

    public String getCombinedUserUid() {
        return combinedUserUid;
    }

    public void setCombinedUserUid(String combinedUserUid) {
        this.combinedUserUid = combinedUserUid;
    }

    private String combinedUserUid;



}
