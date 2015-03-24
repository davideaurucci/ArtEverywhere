package com.arteverywhere.francesco.art;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainDefaultResponseMessage;

/**
 * Created by Sara on 19/03/2015.
 */
public class SalvaModifiche extends AsyncTask<Integer, Void, MainDefaultResponseMessage> {
    String url;
    String desc;
    String luogo;
    String size;
    String titolo;
    Context mContext;
    TaskCallbackModifica m;

    public SalvaModifiche(String url, String desc, String luogo, String size, String t, Context c, TaskCallbackModifica m){
        this.url=url;
        this.desc=desc;
        this.luogo=luogo;
        this.size=size;
        titolo=t;
        this.mContext=c;
        this.m=m;
    }
    protected MainDefaultResponseMessage doInBackground(Integer... integers){
        // Retrieve service handle.
        ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);
        try {
            Looper.prepare();
            ArtEverywhere.Picinfo.Updatepicture change=apiServiceHandle.picinfo().updatepicture(url);

            change.setUrl(url);
            change.setNewDim(size);
            change.setNewLuogo(luogo);
            change.setNewTitle(titolo);
            change.setNewDescr(desc);

            MainDefaultResponseMessage risposta=change.execute();

            return risposta;

        } catch (IOException e) {
            Toast.makeText(mContext, "Exception during API call!", Toast.LENGTH_LONG).show();
        }
        return null;
    }
    protected void onPostExecute(MainDefaultResponseMessage risposta) {

        m.done();
    }
}
