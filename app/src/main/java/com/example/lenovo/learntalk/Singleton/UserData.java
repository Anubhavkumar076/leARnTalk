package com.example.lenovo.learntalk.Singleton;

public class UserData {

    public UserData()
    {

    }

    public UserData(String date, String description, String fullname, String images, String profileimage, String time, String userid) {
        this.date = date;
        this.description = description;
        this.fullname = fullname;
        this.images = images;
        this.profileimage = profileimage;
        this.time = time;
        this.userid = userid;
    }

    public String date,description,fullname,images,profileimage,time,userid;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getfullname() {
        return fullname;
    }

    public void setfullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
