package smap.gr15.appproject.tendr.models;

import java.util.List;

public class Conversation {
    private List<ChatMessage> chatMessages;
    private String firstUserId;
    private String secondUserId;
    private String combinedUserUid;

    public String getCombinedUserUid() {
        return combinedUserUid;
    }

    public void setCombinedUserUid(String combinedUserUid) {
        this.combinedUserUid = combinedUserUid;
    }
    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public String getFirstUserId() {
        return firstUserId;
    }

    public void setFirstUserId(String firstUserId) {
        this.firstUserId = firstUserId;
    }

    public String getSecondUserId() {
        return secondUserId;
    }

    public void setSecondUserId(String secondUserId) {
        this.secondUserId = secondUserId;
    }
}
