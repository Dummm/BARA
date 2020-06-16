package com.bara.bara.model;

public class Follower {
    String Follower;
    String Following;


public  Follower(){}
public  Follower(String Follower,String Following){
    this.Follower=Follower;
    this.Following=Following;
}

public void setFollower(String Follower){
    this.Follower=Follower;
}
public  void setFollowing(String Following){
    this.Following=Following;

}

    public String getFollowing(){
      return  this.Following;

    }
    public String getFollower(){
        return  this.Follower;

    }

}
