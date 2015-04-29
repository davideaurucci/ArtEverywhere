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
public class DownloadArtworksForRefresh extends AsyncTask<Integer, Void, MainDownloadResponseCollection> {
                Context mContext;
                DatabaseArtwork db;
                TaskCallbackRefreshArtworks mCallback;

                public DownloadArtworksForRefresh(Context context) {
                    mContext = context;
                }

                public DownloadArtworksForRefresh(Context context, DatabaseArtwork db, TaskCallbackRefreshArtworks mCallback) {
                    mContext = context;
                    this.db = db;
                    this.mCallback = mCallback;
                }

                protected MainDownloadResponseCollection doInBackground(Integer... integers) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
                    try {
                        String data = db.getMostRecentDate();
                        int sec = Character.getNumericValue(data.charAt(17));
                        int new_sec = sec + 1;
                        String final_data = data.substring(0,17) + "" + new_sec + data.charAt(18);
                        ArtEverywhere.Refresh.Refreshphotos get = apiServiceHandle.refresh().refreshphotos((long)AppConstants.numFoto, final_data);
                        MainDownloadResponseCollection greeting = get.execute();
                        return greeting;
                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainDownloadResponseCollection greeting) {
                    if (greeting!=null) {
                      if(greeting.getPhotos() == null){
                          Toast.makeText(mContext, "Nessun nuovo artwork!", Toast.LENGTH_LONG).show();
                          return;
                        }
                        int quanteFotoCaricate = greeting.getPhotos().size();
                            for(int i = 0; i < quanteFotoCaricate;i++){
                                String filename = greeting.getPhotos().get(i).getTitle();
                                String photo = greeting.getPhotos().get(i).getUrl();
                                System.out.println(photo);
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
                            mCallback.done(false,false);

                    } else {
                        Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

}
