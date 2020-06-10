package com.bara.bara.feed;

public class Upload {
    private String mMessage;
    private String mEmail;
    private String mImageUrl;


    public Upload() {}

    public Upload(String message, String imageUrl, String email) {
        mMessage = message;
        mImageUrl = imageUrl;
        mEmail = email;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

}
