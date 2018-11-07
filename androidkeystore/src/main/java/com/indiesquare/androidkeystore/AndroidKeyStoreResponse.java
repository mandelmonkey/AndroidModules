package com.indiesquare.androidkeystore;

/**
 * Created by a on 2018/10/30.
 */

public class AndroidKeyStoreResponse
{
    public Object response;
    public String error;

    public AndroidKeyStoreResponse (Object response,String error)
    {

        this.response = response;
        this.error = error;

    }
}