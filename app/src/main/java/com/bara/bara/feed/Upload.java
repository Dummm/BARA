package com.bara.bara.feed;

import com.bara.bara.model.User;

public class Upload {
    private String message;
    private String email;
    private String imageUrl;
    private User user;


    public Upload() {
    }

    public Upload(String message, String imageUrl, User user, String email) {
        this.user = user;
        this.message = message;
        this.imageUrl = imageUrl;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
