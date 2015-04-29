package com.arteverywhere.francesco.art;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Sara on 19/03/2015.
 */
public class ModificaArtwork extends ActionBarActivity implements TaskCallbackModifica{
    ImageView img;
    ImageView imgArtista;
    EditText titolo;
    TextView nomeArtista;
    EditText descrizione;
    TextView tecnica;
    AutoCompleteTextView luogo;
    EditText size;
    Artwork artwork;
    String url;

    TaskCallbackModifica m;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica_artwork);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        m=this;

        url=getIntent().getExtras().getString("url");
        DatabaseArtwork db = new DatabaseArtwork(getApplicationContext());
        artwork = db.getArtworkFromUrl(url);
        img = (ImageView) findViewById(R.id.imageView);
        imgArtista = (ImageView) findViewById(R.id.imageView2);
        titolo = (EditText) findViewById(R.id.stextView3);

        nomeArtista = (TextView) findViewById(R.id.textView4);

        System.out.println("TECNICA" + artwork.getTecnique());
        System.out.println("MAIL" + artwork.getArtista());


        descrizione = (EditText) findViewById(R.id.sdesc);
        tecnica = (TextView) findViewById(R.id.tec);
        luogo = (AutoCompleteTextView) findViewById(R.id.sluogo);
        size = (EditText) findViewById(R.id.ssize);

        if(!(artwork.getDescrizione().equals("null")))
            descrizione.setText(artwork.getDescrizione());
        if(!(artwork.getLuogo().equals("null")))
            luogo.setText(artwork.getLuogo());
        if(!(artwork.getDimensioni().equals("null")))
            size.setText(artwork.getDimensioni());

        titolo.setText(artwork.getFilename());

        luogo.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        luogo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) parent.getItemAtPosition(position);
                Log.d("PLACE", str);
            }
        });


        tecnica.setText("Tecnica: " + artwork.getTecnique());
    }

    public void done(){

        Intent intent=new Intent(ModificaArtwork.this, ArtworkDetails.class);
        intent.putExtra("photo", url);
        intent.putExtra("descmod", descrizione.getText().toString() );
        System.out.println("TITOLO "+titolo.getText());
        intent.putExtra("titolomod", titolo.getText().toString());
        intent.putExtra("luogomod", luogo.getText().toString());
        intent.putExtra("dimmod", size.getText().toString());
        boolean visitatore=(boolean)getIntent().getExtras().get("visitatore");
        if(!visitatore)
            intent.putExtra("email", getIntent().getExtras().getString("email"));
        intent.putExtra("visitatore", visitatore);
        startActivity(intent);
        this.finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_modifica, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.modifica) {
            if(titolo.getText().toString().equals(""))
                Toast.makeText(getApplicationContext(), "Devi scegliere un titolo!", Toast.LENGTH_LONG).show();
            else {
                SalvaModifiche s = new SalvaModifiche(url, descrizione.getText().toString(), luogo.getText().toString(), size.getText().toString(), titolo.getText().toString(), getApplicationContext(), m);
                if(checkNetwork()) s.execute();
            }
        }
        return true;
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
                            startActivityForResult(callGPSSettingIntent,0);
                        }
                    }).show();
            return false;
        }
    }
}