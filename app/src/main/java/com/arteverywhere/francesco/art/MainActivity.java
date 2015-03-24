package com.arteverywhere.francesco.art;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import cod.com.appspot.art_everywhere.artEverywhere.ArtEverywhere;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainArtistBriefCollection;
import cod.com.appspot.art_everywhere.artEverywhere.model.MainTechniqueResponseCollection;



public class MainActivity extends ActionBarActivity implements TaskCallbackDownloadArtworks, TaskCallbackRefreshArtworks, TaskCallbackDownloadFilteredArtworks, TaskCallbackDownloadArtistForGallery {

    String[] urlPhoto = new String[AppConstants.numFoto];
    SharedPreferences pref;
    String email;
    boolean visitatore = false;

    public static SwipeRefreshLayout swipeView;
    DatabaseArtwork db;
    GridView gridview;

    AlertDialog dialog;
    String[] tecniche;
    String tecnicaScelta;

    public DownloadArtworksByDate d;
    TaskCallbackDownloadArtworks callback;
    int pagina;

    boolean isFiltri = false;
    boolean isSelected; //serve nel dialog per filtro città
    String selectedCity; // serve nel dialog per filtro città
    String[] artists;
    String[] artistsPic;
    String[] artistsMail;
    String selectedArtist; //email dell'artista selezionato
    String picArtista; //url della picture profile dell'artista selezionato

    View fabUpload; //Floating action bar



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        email = pref.getString("email_artista",null);

        /*if(getIntent().hasExtra("visitatore")){
            Bundle extras = getIntent().getExtras();
            visitatore = extras.getBoolean("visitatore",false);
        }*/
        if(email==null){
            visitatore=true;
        }
        if(!visitatore) { //visualizzo il floating button per l'upload solo se è artista
            fabUpload = findViewById(R.id.fab);
            fabUpload.setVisibility(View.VISIBLE);
            fabUpload.bringToFront();
            fabUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(MainActivity.this, UploadActivity.class);
                    myIntent.putExtra("email", email);
                    startActivity(myIntent);
                }
            });
        }


        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_trasparente);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        swipeView = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        swipeView.setEnabled(false);

        gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));

        db = new DatabaseArtwork(getApplicationContext());
        urlPhoto = db.getPhotos();

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);

                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeView.setRefreshing(false);

                        /* Devo chiamare l'API */
                        chiamaApi();
                    }
                }, 2000);

            }
        });

        callback=this; /* Serve per poter chiamare la funz di libr (con callback) all'interno dell'OnScrollListener */
        gridview.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list
                pagina=page;
                //ho già le foto nel database. non devo ricaricarle

                //Se filtro la ricerca "disabilito" l'endless scroll
                if(!isFiltri) {
                    if(db.getAllArtworks().size()>=urlPhoto.length+4){

                        String [] b=db.getPhotosAfterScroll(((pagina/2)+1)*4+20);
                        urlPhoto=new String[b.length];
                        urlPhoto=b;
                        gridview.setSelection(gridview.getFirstVisiblePosition());
                        gridview.setAdapter(new ImageAdapter(getApplicationContext()));

                    }
                    else{

                        String data=db.getArtworkFromUrl(urlPhoto[urlPhoto.length-1]).getData();
                        new DownloadArtworksByDate(getApplicationContext(),db, callback, data).execute();

                    }
                }
            }

        });



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

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void chiamaApi(){
        new DownloadArtworksForRefresh(getApplicationContext(),db,this).execute();
        // Dopo aver completato l'asyntask viene chiamato done(boolean,boolean)
    }

    public void done(boolean x, boolean a){
        /* I parametri x e a non servono a nulla */

        /* Faccio una ricerca per data sul DB */
        urlPhoto = db.getArtworksOrderByDate();

        /* Chiamo gridview.setAdapter(new ImageAdapter(this)); */
        gridview.setAdapter(new ImageAdapter(this));
    }

    /* Serve per l'endless scroll */
    private String[] toArray(List<Artwork> l){
        int c=0;
        Iterator i =l.iterator();
        while(i.hasNext())
            c++;
        String [] f=new String[c];
        Iterator t=l.iterator();
        int g=0;
        while(t.hasNext()) {
            f[g] = l.get(g).getPhoto();
            g++;
        }
        return f;
    }

    /* Serve per l'endless scroll - TaskCallback Download artworks */
    @Override
    public void done() {
        String [] a=db.getPhotosAfterScroll(((pagina/2)+1)*4+20);
        urlPhoto=new String[a.length];
        urlPhoto=a;

        for(int i=0; i<urlPhoto.length; i++){
            if(urlPhoto[i]!=null){
                System.out.println(urlPhoto[i]);

            }
        }
        gridview.setSelection(gridview.getFirstVisiblePosition());
        gridview.setAdapter(new ImageAdapter(this));
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
            getMenuInflater().inflate(R.menu.menu_main_per_visitatori, menu);
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

            if(id == R.id.logout){
                //Login.signOutFromGplus();
                pref.edit().remove("email_artista").commit();
                pref.edit().putBoolean("token", true).commit();

                Intent i = new Intent(MainActivity.this, Login.class);
                this.startActivity(i);
                Toast.makeText(getApplicationContext(), "Logout eseguito!", Toast.LENGTH_LONG).show();
                this.finish();
                return true;
            }else if(id == R.id.menu_filtro){
                /* Faccio visualizzare l'alert dialog con la lista dei filtri possibli */
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                final LayoutInflater inflater = getLayoutInflater();
                final View convertView = (View) inflater.inflate(R.layout.custom, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Filtra ricerca");
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);

                final String[] filtri = {"Più recenti","Tecnica","Luogo", "Artista"};

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String item = filtri[position];
                        System.out.println("Ho scelto: " + item);
                        dialog.dismiss();
                        //isTecnica = true;
                        //effettuaDownload();

                        if(item.equalsIgnoreCase("Più recenti")){
                            seeMoreRecent();
                        }else if(item.equalsIgnoreCase("Tecnica")){ //è stato selezionato -> TECNICA
                            getTecniche();
                        }else if(item.equalsIgnoreCase("Luogo")){ //è stato selezionato -> LUOGO
                            seePlace();
                        }else if(item.equalsIgnoreCase("Artista")){ //è stato selezionato -> ARTISTA
                            seeArtists();
                        }
                    }
                });
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_black, filtri);
                lv.setAdapter(adapter);
                dialog = alertDialog.show();
                return true;
            }else if(id == R.id.menu_profile){
                new DownloadArtistForGallery(getApplicationContext(),email,this).execute();
            }else if(id == R.id.menu_feedback){
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"arteverywhere00@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Feedback - Art Everywhere");
                i.putExtra(Intent.EXTRA_TEXT   , "");
                try {
                    startActivity(Intent.createChooser(i, "Invia feedback..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            else if(id==R.id.artista){
                pref.edit().putBoolean("token",true).commit();
                Intent myIntent = new Intent(MainActivity.this, Login.class);
                this.startActivity(myIntent);
                this.finish();
            }

        return super.onOptionsItemSelected(item);
    }

    public void seeMoreRecent(){
        urlPhoto = db.getPhotos();
        gridview.setAdapter(new ImageAdapter(this));
    }

    /* Scarico le tecniche da visualizzare nel dialog */
    private void getTecniche(){
        AsyncTask<Void, Void, MainTechniqueResponseCollection> getTec = new AsyncTask<Void, Void, MainTechniqueResponseCollection>() {

            @Override
            protected MainTechniqueResponseCollection doInBackground(Void... unused) {
                // Retrieve service handle.
                ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                    MainTechniqueResponseCollection greeting = new MainTechniqueResponseCollection();
                    ArtEverywhere.Techniques.Gettechniques get = apiServiceHandle.techniques().gettechniques();
                    greeting = get.execute();
                    return greeting;
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Exception during API call - tecniche!", Toast.LENGTH_LONG).show();
                    //Log.d("ERRORE",e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(MainTechniqueResponseCollection greeting) {
                if (greeting != null) {
                    System.out.println("SONO QUI");
                    System.out.println("tecniche: " + greeting.size());
                    System.out.println("tecniche: " + greeting.getTechniques().size());
                    tecniche = new String[greeting.getTechniques().size()];
                    for(int i = 0; i < greeting.getTechniques().size(); i++){
                        tecniche[i] = greeting.getTechniques().get(i).getTechnique();
                        //Log.d("TECNICA",greeting.getTechniques().get(i).getTechnique());
                    }

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    final LayoutInflater inflater = getLayoutInflater();
                    final View convertView = (View) inflater.inflate(R.layout.custom, null);
                    alertDialog.setView(convertView);
                    alertDialog.setTitle("Tecniche");
                    ListView lv = (ListView) convertView.findViewById(R.id.listView1);

                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //System.out.println(tecniche[position]);
                            tecnicaScelta = tecniche[position];
                            System.out.println("Ho scelto: " + tecnicaScelta);
                            dialog.dismiss();
                            //isTecnica = true;
                            System.out.println("avvio il download per tecnica");
                            effettuaDownloadPerTecnica(tecnicaScelta);
                        }
                    });
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_black, tecniche);
                    lv.setAdapter(adapter);
                    dialog = alertDialog.show();


                    //Toast.makeText(getApplicationContext(), "Upload successfull!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                }
            }
        };
        getTec.execute();

    }

    public void effettuaDownloadPerTecnica(String tecnicaScelta){
        new DownloadArtworksByTechinique(getApplicationContext(),db,this,tecnicaScelta).execute();
    }

    public void seePlace(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        final LayoutInflater inflater = getLayoutInflater();
        final View convertView = (View) inflater.inflate(R.layout.custom_filter_place, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Luogo");

        isSelected = false;
        AutoCompleteTextView place = (AutoCompleteTextView) convertView.findViewById(R.id.editText3);
        place.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        place.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isSelected = true;
                String str = (String) parent.getItemAtPosition(position);
                selectedCity = str;
                Log.d("PLACE", str);
            }
        });

        Button cerca = (Button) convertView.findViewById(R.id.buttonOk);
        cerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSelected){
                    Toast.makeText(getApplicationContext(), "Attenzione! Devi selezionare uno dei suggerimenti proposti", Toast.LENGTH_LONG).show();
                }else{
                    //avvio la chiamata alla funzione di libreria
                    dialog.dismiss();
                    effettuaDownloadPerLuogo(selectedCity);
                }
            }
        });

        dialog = alertDialog.show();
    }

    public void effettuaDownloadPerLuogo(String city){
        new DownloadArtworksByPlace(getApplicationContext(),db,this,city).execute();
    }

    public void seeArtists(){
        AsyncTask<Void, Void, MainArtistBriefCollection> getArtists = new AsyncTask<Void, Void, MainArtistBriefCollection>() {

            @Override
            protected MainArtistBriefCollection doInBackground(Void... unused) {
                // Retrieve service handle.
                ArtEverywhere apiServiceHandle = AppConstants.getApiServiceHandle(null);

                try {
                    MainArtistBriefCollection greeting = new MainArtistBriefCollection();
                    ArtEverywhere.Artistlist.Getartists get = apiServiceHandle.artistlist().getartists();
                    greeting = get.execute();
                    return greeting;
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Exception during API call - get Artists!", Toast.LENGTH_LONG).show();
                    //Log.d("ERRORE",e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(MainArtistBriefCollection greeting) {
                if (greeting != null) {
                    artists = new String[greeting.getArtists().size()];
                    artistsPic = new String[greeting.getArtists().size()];
                    artistsMail = new String[greeting.getArtists().size()];

                    for(int i= 0;i < greeting.getArtists().size(); i++){
                        artists[i] = greeting.getArtists().get(i).getCognome() + " " + greeting.getArtists().get(i).getNome();
                        artistsPic[i] = greeting.getArtists().get(i).getPic();
                        artistsMail[i] = greeting.getArtists().get(i).getEmail();
                    }

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    final LayoutInflater inflater = getLayoutInflater();
                    final View convertView = (View) inflater.inflate(R.layout.custom, null);
                    alertDialog.setView(convertView);
                    alertDialog.setTitle("Artisti");

                    CustomList adapter = new CustomList(MainActivity.this, artists, artistsPic);
                    ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            selectedArtist = artistsMail[position];
                            picArtista = artistsPic[position];
                            dialog.dismiss();
                            getArtistInfo();
                        }
                    });

                    dialog = alertDialog.show();


                    //Toast.makeText(getApplicationContext(), "Upload successfull!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                }
            }
        };
        getArtists.execute();
    }

    public void getArtistInfo(){
        new DownloadArtistForGallery(getApplicationContext(),selectedArtist,this).execute();
    }

    // metodo di ritorno dalla chiamata getArtistInfo
    @Override
    public void done(String email, String nc, String pic, String nick, String bio, String sito) {
        Intent intent = new Intent(MainActivity.this, ArtistProfile.class);
        intent.putExtra("nomecognome",nc);
        intent.putExtra("bio",bio);
        intent.putExtra("sito",sito);
        intent.putExtra("nickname",nick);
        intent.putExtra("email",email);
        intent.putExtra("pic",pic);

        startActivity(intent);
    }

    //metodo di ritorno dalle funzioni di libreria dei filtri
    public void done(int x){
        isFiltri = true;

        if(x==0){ //callback from DownloadArtworksByTechnique
            urlPhoto = db.getArtworksFromTechinique(tecnicaScelta);
        }else if(x==1){ //callback from DownloadArtworksByPlace
            urlPhoto = db.getArtworksFromPlace(selectedCity);
        }

        //gridview.setSelection(gridview.getAdapter().getCount()-1);
        gridview.setSelection(gridview.getFirstVisiblePosition());
        gridview.setAdapter(new ImageAdapter(this));

    }
}
