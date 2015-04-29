package com.arteverywhere.francesco.art;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
    DatabaseArtwork db;
    String[] photoArtista = new String[4];
    SharedPreferences pref;
    String artistLogged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        picArtista = (ImageView)findViewById(R.id.imageView);
        NomeCognome = (TextView)findViewById(R.id.textView3);
        nickname = (TextView)findViewById(R.id.textView5);
        emailArtista = (TextView)findViewById(R.id.textView6);
        bioArtista = (TextView)findViewById(R.id.textView7);

        //RECUPERO INFO DA ACTIVITY PRECEDENTE
        Bundle extras = getIntent().getExtras();
        nomecognome = extras.getString("nomecognome");
        bio = extras.getString("bio");
        sito = extras.getString("sito");
        nick = extras.getString("nickname");
        email = extras.getString("email");
        pic = extras.getString("pic");

        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_trasparente);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(nomecognome);

        Picasso.with(ArtistProfile.this).load(pic).transform(new CircleTransform()).into(picArtista);
        NomeCognome.setText(nomecognome);
        nickname.setText(nick);
        emailArtista.setText(email);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        artistLogged = pref.getString("email_artista",null);


        LinearLayout lin = (LinearLayout) findViewById(R.id.linlay2);
            if(bio != null){
              bioArtista.setText(bio);
            }else{
                lin.setVisibility(View.GONE);
            }

            db = new DatabaseArtwork(getApplicationContext());

            if(checkNetwork()) new GetArtworksOfArtist(getApplicationContext(),email,db,this).execute();

    }

    public void done(String[] url){
        GridView gridview = (GridView) findViewById(R.id.gridView2);
        gridview.setAdapter(new ImageAdapter(this));
        photoArtista = url;

        if(photoArtista != null){
            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ArtistProfile.this, ArtworkDetails.class);
                    String photo = photoArtista[position];
                    System.out.println(photo);
                    intent.putExtra("photo",photo);
                    startActivity(intent);
                }
            });
        }


    }

    //Custom adapter
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            if(photoArtista != null){
                return photoArtista.length;
            }
            return 0;
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
        if(!email.equalsIgnoreCase(artistLogged)) {
            if (sito != null) {
                getMenuInflater().inflate(R.menu.menu_artist_profile, menu);
            } else {
                getMenuInflater().inflate(R.menu.menu_artist_profile_senza_web, menu);
            }
        }else{ //se l'artista va nel può profilo può modificarlo
            getMenuInflater().inflate(R.menu.menu_artist_suo_profilo, menu);
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
        }else if(id == R.id.modifica){
            Intent i = new Intent(ArtistProfile.this,ModifyArtistProfile.class);
            i.putExtra("email",email);
            this.startActivity(i);
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isOnline = (netInfo != null && netInfo.isConnectedOrConnecting());
        if(isOnline) {
            return true;
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("Ops..qualcosa è andato storto!")
                    .setMessage("Sembra che tu non sia collegato ad internet! ")
                    .setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            Intent callGPSSettingIntent = new Intent(Settings.ACTION_SETTINGS);
                            startActivityForResult(callGPSSettingIntent, 0);
                        }
                    }).show();
            return false;
        }
    }

}
