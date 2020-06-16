package com.bara.bara.model;

import java.util.HashMap;
import java.util.Map;

public class Post {
    private String id;
    private String message;
    private String email;
    private String imageUrl;
    private User user;
    private Map<String, String> likes;


    public Post() {
    }

    public Post(String message, String imageUrl, User user, String email) {
        this.likes = new HashMap<>();
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, String> likes) {
        this.likes = likes;
    }
}
