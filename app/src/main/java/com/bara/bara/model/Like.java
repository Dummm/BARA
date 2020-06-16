package com.bara.bara.model;

public class Like {
    private String id;
    private String userId;
    private String postId;

    public Like() {
    }

    public Like(String id, String userId, String postId) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
