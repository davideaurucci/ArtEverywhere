package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDefaultResponseMessage;

/**
 * Created by Sara on 18/03/2015.
 */
public class DeleteArtwork extends AsyncTask<Integer, Void, MainDefaultResponseMessage> {
    String email;
    String url;
    Context mContext;
    TaskCallbackDelete l;

    public DeleteArtwork(String url, Context context, TaskCallbackDelete l) {
        this.url = url;
        this.mContext=context;
        this.l=l;

    }
    protected MainDefaultResponseMessage doInBackground(Integer... integers){
        // Retrieve service handle.
        ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
        try {

            ArtEverywhere.Delete.Deletephoto d=apiServiceHandle.delete().deletephoto(url);
            MainDefaultResponseMessage risposta=d.execute();

            return risposta;

        } catch (IOException e) {
            Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
        }
        return null;
    }
    protected void onPostExecute(MainDefaultResponseMessage risposta) {
        if (risposta!=null) {
            l.done(0);
        } else {
            Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
        }
    }
}
