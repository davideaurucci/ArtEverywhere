package com.example.francesco.art;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class ArtistProfile extends ActionBarActivity implements TaskCallbackArtworksOfArtist {
    ImageView picArtista;
    TextView NomeCognome;
    TextView nickname;
    TextView emailArtista;
    TextView bioArtista;

    String pic;
    String nomecognome;
    String nick;
    String bio;
    String sito;
    String email;

    String[] photoArtista = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);

        picArtista = (ImageView)findViewById(R.id.imageView);
        NomeCognome = (TextView)findViewById(R.id.textView3);
        nickname = (TextView)findViewById(R.id.textView5);
        emailArtista = (TextView)findViewById(R.id.textView6);
        bioArtista = (TextView)findViewById(R.id.textView7);

        Bundle extras = getIntent().getExtras();
        nomecognome = extras.getString("nomecognome");
        bio = extras.getString("bio");
        sito = extras.getString("sito");
        nick = extras.getString("nickname");
        email = extras.getString("email");
        pic = extras.getString("pic");


        Picasso.with(ArtistProfile.this).load(pic).transform(new CircleTransform()).into(picArtista);
        NomeCognome.setText(nomecognome);
        nickname.setText(nick);
        emailArtista.setText(email);

        LinearLayout lin = (LinearLayout) findViewById(R.id.linlay2);
        if(bio != null){
            bioArtista.setText(bio);
        }else{
            lin.setVisibility(View.GONE);
        }

        new GetArtworksOfArtist(getApplicationContext(),email,this).execute();

    }

    public void done(String[] url){

        GridView gridview = (GridView) findViewById(R.id.gridView2);
        gridview.setAdapter(new ImageAdapter(this));
        photoArtista = url;

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ArtistProfile.this, ArtworkDetails.class);
                String photo = photoArtista[position];
                intent.putExtra("photo",photo);
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
            return photoArtista.length;
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

            Picasso.with(ArtistProfile.this)
                    .load("" + photoArtista[position] + "")
                    //.placeholder(R.raw.place_holder)
                    //.error(R.raw.place_holder)
                    .noFade().resize(size/2, size/2)
                    .centerCrop()
                    .into(imageView);
            return imageView;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(sito != null){
            getMenuInflater().inflate(R.menu.menu_artist_profile, menu);
        }else{
            getMenuInflater().inflate(R.menu.menu_artist_profile_senza_web, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.mail) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
            i.putExtra(Intent.EXTRA_SUBJECT, "Contatto da Art Everywhere");
            i.putExtra(Intent.EXTRA_TEXT   , "");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ArtistProfile.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
            return true;

        }else if(id == R.id.sito){
            String url =  sito;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
