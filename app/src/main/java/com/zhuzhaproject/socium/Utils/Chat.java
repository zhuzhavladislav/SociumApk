package com.zhuzhaproject.socium.Utils;

public class Chat {
    private String message, status, userID;

    public Chat() {
    }

    public Chat(String message, String status, String userID) {
        this.message = message;
        this.status = status;
        this.userID = userID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
