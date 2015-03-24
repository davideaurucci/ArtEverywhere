package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainGeneralLikeRequest;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainGeneralLikeResponse;

/**
 * Created by Sara on 14/03/2015.
 */
public class PutLike extends AsyncTask<Integer, Void, MainGeneralLikeResponse> {
    String email;
    String url;
    Context mContext;
    TaskCallbackLike l;

    public PutLike(String url, String email, Context context, TaskCallbackLike l) {
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
            ArtEverywhere.Change.Changelikestatus change=apiServiceHandle.change().changelikestatus(m);
            MainGeneralLikeResponse risposta=change.execute();

            return risposta;

        } catch (IOException e) {
            Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
        }
        return null;
    }
    protected void onPostExecute(MainGeneralLikeResponse risposta) {
        if (risposta!=null) {
            l.done(risposta.getHasLiked());

        } else {
            Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
        }
    }
}
