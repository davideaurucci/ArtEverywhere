package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainPictureDetailsCollection;

/**
 * Created by Francesco on 11/02/2015.
 */
public class GetArtworksOfArtist extends AsyncTask<String, Void, MainPictureDetailsCollection> {
    Context mContext;
    String mail;
    DatabaseArtwork db;
    TaskCallbackArtworksOfArtist mCallback;

    public GetArtworksOfArtist(Context context) {
        mContext = context;
    }

    public GetArtworksOfArtist(Context context, String mail,DatabaseArtwork db, TaskCallbackArtworksOfArtist mCallback) {
        mContext = context;
        this.mail = mail;
        this.db = db;
        this.mCallback = mCallback;
    }

    protected MainPictureDetailsCollection doInBackground(String... strings) {
        // Retrieve service handle.
        ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
        try {
            ArtEverywhere.Artworks.Getartworks get = apiServiceHandle.artworks().getartworks(mail);
            MainPictureDetailsCollection greeting = get.execute();
            return greeting;
        } catch (IOException e) {
            Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
        }
        return null;
    }


    protected void onPostExecute(MainPictureDetailsCollection greeting) {
        if (greeting!=null) {

            if(greeting.getPhotos() == null){
                mCallback.done(null);
                return;
            }

            int quanteFotoCaricate = greeting.getPhotos().size();
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
                if(controllo(art)) {
                    db.insert(art, db.getWritableDatabase());
                }

            }

            String[] urlPhoto = db.getArtworksOfArtist(mail);
            mCallback.done(urlPhoto);

        } else {
            Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
        }
    }

    public boolean controllo(Artwork art){
        if(db.getArtworkFromUrl(art.getPhoto()) == null){
            return true;
        }
        return false;
    }

}
