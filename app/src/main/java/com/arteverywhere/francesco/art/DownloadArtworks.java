package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDownloadResponseCollection;

/**
 * Created by Francesco on 11/02/2015.
 */
public class DownloadArtworks extends AsyncTask<Integer, Void, MainDownloadResponseCollection> {
                Context mContext;
                DatabaseArtwork db;
                TaskCallbackDownloadArtworks mCallback;

                public DownloadArtworks(Context context) {
                    mContext = context;
                }

                public DownloadArtworks(Context context, DatabaseArtwork db, TaskCallbackDownloadArtworks mCallback) {
                    mContext = context;
                    this.db = db;
                    this.mCallback = mCallback;
                }

                protected MainDownloadResponseCollection doInBackground(Integer... integers) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
                    try {
                        ArtEverywhere.Display.Getphotos get = apiServiceHandle.display().getphotos((long)AppConstants.numFoto);
                        MainDownloadResponseCollection greeting = get.execute();
                        return greeting;
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainDownloadResponseCollection greeting) {
                    if (greeting!=null) {
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
                               db.insert(art, db.getWritableDatabase());
                            }
                            mCallback.done();

                    } else {
                        Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

}
