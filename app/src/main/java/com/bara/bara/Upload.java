package com.bara.bara;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Upload {
    private String mMessage;
    private String mEmail;
    private String mImageUrl;



    public Upload(){/*empty constructor necessary*/}

    public Upload(String message, String imageUrl)
    {
        mMessage = message;
        mImageUrl = imageUrl;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mEmail = currentUser.getEmail();
    }

    public String getMessage()
    {
        return mMessage;
    }

    public void setMessage(String message)
    {
        mMessage = message;
    }

    public String getImageUrl()
    {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        mImageUrl = imageUrl;
    }

    public String getEmail()
    {
        return mEmail;
    }

}