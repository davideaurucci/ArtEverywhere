package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainGeneralLikeRequest;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainGeneralLikeResponse;

/**
 * Created by Sara on 15/03/2015.
 */
public class CheckLike extends AsyncTask<Integer, Void, MainGeneralLikeResponse> {
        String email;
        String url;
        Context mContext;
        TaskCallbackCheckLike l;

    public CheckLike(String url, String email, Context context, TaskCallbackCheckLike l) {
        this.email = email;
        this.url = url;
        this.mContext=context;
        this.l=l;
    }
    protected MainGeneralLikeResponse doInBackground(Integer... integers){
        // Retrieve service handle.
        ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
        try {

            MainGeneralLikeRequest m=new MainGeneralLikeRequest();
            m.setEmail(email);
            m.setUrl(url);
            ArtEverywhere.Like.Likestatus check=apiServiceHandle.like().likestatus(m);
            MainGeneralLikeResponse risposta=check.execute();

            return risposta;

        } catch (IOException e) {
            //Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
        }
        return null;
    }
    protected void onPostExecute(MainGeneralLikeResponse risposta) {
        if (risposta!=null) {
            l.done(risposta.getHasLiked(), true);

        } else {
            Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
        }
    }
}
