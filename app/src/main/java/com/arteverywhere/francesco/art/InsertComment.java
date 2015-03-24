package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDefaultResponseMessage;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainInsertCommentMessage;

/**
 * Created by Francesco on 11/02/2015.
 */
public class InsertComment extends AsyncTask<Void, Void, MainInsertCommentMessage> {
                Context mContext;
                String email;
                String commento;
                String url;
                TaskCallbackInsertComment mCallback;

                public InsertComment(Context context) {
                    mContext = context;
                }

                public InsertComment(Context context, String email, String commento, String url, TaskCallbackInsertComment mCallback) {
                    mContext = context;
                    this.email = email;
                    this.commento = commento;
                    this.url = url;
                    this.mCallback = mCallback;
                }

                protected MainInsertCommentMessage doInBackground(Void... unused) {
                    // Retrieve service handle.
                    ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                    Log.d("DB","doInBack");
                    
                    try {
                        Log.d("LOG", "Sono qui");

                        //MainArtistInfoMessage greeting = new MainArtistInfoMessage();
                        MainInsertCommentMessage greeting = new MainInsertCommentMessage();
                        greeting.setEmail(email);
                        greeting.setComment(commento);
                        greeting.setUrl(url);

                        System.out.println("*"+email);
                        System.out.println("*"+commento);
                        System.out.println("*"+url);

                        ArtEverywhere.Comment.Insertcomment get = apiServiceHandle.comment().insertcomment(greeting);

                        MainDefaultResponseMessage response = get.execute();
                        System.out.println(response.getMessage());
                        return greeting;


                    } catch (IOException e) {
                        Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
                    }
                    return null;
                }


                protected void onPostExecute(MainInsertCommentMessage greeting) {
                    if (greeting!=null) {
                        Toast.makeText(mContext, "Commento inserito con successo!", Toast.LENGTH_LONG).show();
                        mCallback.done("z");


                    } else {
                        Toast.makeText(mContext, "Commento non inserito", Toast.LENGTH_LONG).show();
                        mCallback.done("z");
                        //Toast.makeText(mContext, "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                    }
                }

}
