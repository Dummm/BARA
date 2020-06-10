package com.bara.bara.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String uuid;
    private String name;
    private String email;
    private List<User> followers;
    private List<User> followings;

    public User() {
    }

    public User(String uuid, String name, String email) {
        this.uuid = uuid;
        this.name = name;
        this.email = email;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public void setFollowers(List<User> followers) {
        this.followers = followers;
    }

    public List<User> getFollowings() {
        return followings;
    }

    public void setFollowings(List<User> followings) {
        this.followings = followings;
    }
}
