package com.zhuzhaproject.socium.Utils;

public class Friends {
    private String username, profileImageUrl, status, profession;

    public Friends() {
    }


    public Friends(String username, String profileImageUrl, String status,String profession) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.profession = profession;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }
}
