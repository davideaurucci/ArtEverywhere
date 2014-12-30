package com.example.francesco.art;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import cod.com.appspot.omega_terrain_803.testGCS.TestGCS;
import cod.com.appspot.omega_terrain_803.testGCS.TestGCS.Display.Getphotos;
import cod.com.appspot.omega_terrain_803.testGCS.model.MainDownloadResponseCollection;
import cod.com.appspot.omega_terrain_803.testGCS.model.MainDownloadResponseMessage;


import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity{

    //quante foto da scaricare
    final int quanteFoto = 10;
    String[] urlPhoto = new String[quanteFoto];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));

        //ricavo l'array di stringhe (url) che ho generato nello SplashScreen
        Bundle extras = getIntent().getExtras();
        urlPhoto = extras.getStringArray("urls");


        /*
        CODE FOR GRID ITEM DETAIL
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ActivityTwo.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        */
    }

    //Custom adapter
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return urlPhoto.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {


            ImageView imageView;
            //check to see if we have a view
            if (convertView == null) {
            //no view - so create a new one
                imageView = new ImageView(mContext);
            } else {
            //use the recycled view object
                imageView = (ImageView) convertView;
            }

            //Picasso.with(MainActivity.this).setDebugging(true);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int size = metrics.widthPixels;

            Picasso.with(MainActivity.this)
                    .load(""+urlPhoto[position]+"")
                    .placeholder(R.raw.place_holder)
                    .error(R.raw.place_holder)
                    .noFade().resize(size/2, size/2)
                    .centerCrop()
                    .into(imageView);
            return imageView;

       }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_upload){
            Intent myIntent = new Intent(MainActivity.this, UploadActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
