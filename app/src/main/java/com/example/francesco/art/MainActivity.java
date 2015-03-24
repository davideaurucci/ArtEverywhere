package com.example.francesco.art;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity{

    String[] urlPhoto = new String[AppConstants.numFoto];
    SharedPreferences pref;
    String email;
    boolean visitatore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));

        DatabaseArtwork db = new DatabaseArtwork(getApplicationContext());
        urlPhoto = db.getPhotos();

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        email = pref.getString("email_artista",null);

        if(getIntent().hasExtra("visitatore")){
            Bundle extras = getIntent().getExtras();
            visitatore = extras.getBoolean("visitatore",false);
        }

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ArtworkDetails.class);
                String url = urlPhoto[position];
                intent.putExtra("photo",url);
                startActivity(intent);
            }
        });

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

        if(visitatore){
            //non deve visualizzare nulla nella action bar
        }else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
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
                myIntent.putExtra("email",email);
                this.startActivity(myIntent);
                return true;
            }else if(id == R.id.logout){
                //Login.signOutFromGplus();
                pref.edit().remove("email_artista").commit();
                Intent i = new Intent(MainActivity.this, Login.class);
                this.startActivity(i);
                Toast.makeText(getApplicationContext(), "Logout eseguito!", Toast.LENGTH_LONG).show();
                this.finish();
                return true;
            }

        return super.onOptionsItemSelected(item);
    }
}
