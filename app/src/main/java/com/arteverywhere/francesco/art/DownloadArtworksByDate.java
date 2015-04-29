package com.arteverywhere.francesco.art;

/**
 * Created by Davide on 26/02/15.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDownloadResponseCollection;


public class DownloadArtworksByDate extends AsyncTask<Integer, Void, MainDownloadResponseCollection> {
    Context mContext;
    DatabaseArtwork db;
    TaskCallbackDownloadArtworks mCallback;
    String date;

    public DownloadArtworksByDate(Context context) {
        mContext = context;
    }

    public DownloadArtworksByDate(Context context, DatabaseArtwork db, TaskCallbackDownloadArtworks mCallback, String date) {
        mContext = context;
        this.db = db;
        this.mCallback = mCallback;
        this.date = date;

    }

    protected MainDownloadResponseCollection doInBackground(Integer... integers) {
        // Retrieve service handle.
        ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
        try {
            ArtEverywhere.Display.Getphotos get = apiServiceHandle.display().getphotos((long)4);
            get.setDateTime(date);
            MainDownloadResponseCollection greeting = get.execute();
            return greeting;
        } catch (IOException e) {
            Looper.prepare();
            Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainDownloadResponseCollection greeting) {
        if (greeting!=null) {
            int quanteFotoCaricate;
            if(greeting.getPhotos()==null) {
                quanteFotoCaricate = 0;
            }
            else {
                quanteFotoCaricate = greeting.getPhotos().size();
            }
            for(int i = 0; i < quanteFotoCaricate;i++){
                String filename = greeting.getPhotos().get(i).getTitle();
                String photo = greeting.getPhotos().get(i).getUrl();
                String artista = greeting.getPhotos().get(i).getArtist();
                String descrizione = greeting.getPhotos().get(i).getDescr();
                String dimensioni = greeting.getPhotos().get(i).getDim();
                String luogo = greeting.getPhotos().get(i).getLuogo();
                String tecnica = greeting.getPhotos().get(i).getTechnique();
                long likes = greeting.getPhotos().get(i).getLikes();
                String data = greeting.getPhotos().get(i).getDateTime();
                Artwork art = new Artwork(filename,photo,artista,descrizione,dimensioni,luogo,tecnica,likes,data);
                db.insert(art, db.getWritableDatabase());
            }
            mCallback.done();

        } else {
            Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
        }
    }

}