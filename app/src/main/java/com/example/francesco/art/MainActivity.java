package com.example.francesco.art;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import cod.com.appspot.omega_terrain_803.testGCS.TestGCS;
import cod.com.appspot.omega_terrain_803.testGCS.TestGCS.Display.Getphotos;
import cod.com.appspot.omega_terrain_803.testGCS.model.MainDownloadResponseCollection;


import com.squareup.picasso.Picasso;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));
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
            return mThumbIds.length;
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

            //metrics.heightPixels;
            int size = metrics.widthPixels;

            Picasso.with(MainActivity.this)
                    .load(mThumbIds[position])
                    .placeholder(R.raw.place_holder)
                    .error(R.raw.big_problem)
                    .noFade().resize(size/2, size/2)
                    .centerCrop()
                    .into(imageView);
            return imageView;

       }
    }

    static Integer[] mThumbIds = {R.raw.img3, R.raw.img2, R.raw.urban, R.raw.urban2, R.raw.urban3, R.raw.image3};



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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.menu_upload){
            Intent myIntent = new Intent(MainActivity.this, UploadActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
