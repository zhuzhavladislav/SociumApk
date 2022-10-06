package com.zhuzhaproject.socium.Utils;

public class Chats {
    private String username, profileImageUrl, lastMessage;

    public Chats() {
    }


    public Chats(String username, String profileImageUrl, String lastMessage) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.lastMessage = lastMessage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setStatus(String lastMessage) {
        this.lastMessage = lastMessage;
    }

}
