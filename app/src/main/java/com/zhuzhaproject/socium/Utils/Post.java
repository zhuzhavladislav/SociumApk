package com.zhuzhaproject.socium.Utils;



public class Post {


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Post(long timestamp, String by, String liked) {
        this.timestamp = timestamp;
        this.by = by;
        this.liked = liked;
    }

    public Post(){

    }
    public long timestamp;


    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }

    public String liked;

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String by;
}

