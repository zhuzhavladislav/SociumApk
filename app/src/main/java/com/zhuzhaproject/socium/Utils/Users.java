package com.zhuzhaproject.socium.Utils;

public class Users {
    private String username, profession, profileImage;

    public Users() {
    }

    public Users(String username, String profession, String profileImage) {
        this.username = username;
        this.profession = profession;
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
