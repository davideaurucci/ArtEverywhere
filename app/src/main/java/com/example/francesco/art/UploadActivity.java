package com.example.francesco.art;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cod.com.appspot.omega_terrain_803.testGCS.TestGCS;
import cod.com.appspot.omega_terrain_803.testGCS.TestGCS.Upload.Putphoto;
import cod.com.appspot.omega_terrain_803.testGCS.model.MainUploadRequestMessage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.util.Base64;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;



public class UploadActivity extends ActionBarActivity {

    Button btn_upload;
    Button btn_choose;
    EditText filename;
    String image;
    byte[] ba;

    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    int id = 1;

    private final int SELECT_PHOTO = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        btn_upload = (Button)findViewById(R.id.button);
        filename = (EditText)findViewById(R.id.editText);
        btn_choose = (Button)findViewById(R.id.button2);

        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<Void, Void, MainUploadRequestMessage> uploadPhoto = new AsyncTask<Void, Void, MainUploadRequestMessage>() {

                    @Override
                    protected MainUploadRequestMessage doInBackground(Void... unused) {
                        // Retrieve service handle.
                        TestGCS apiServiceHandle = AppConstants.getApiServiceHandle();

                        try {
                            MainUploadRequestMessage greeting = new MainUploadRequestMessage();
                            greeting.setFilename(filename.getText().toString());
                            greeting.encodePhoto(ba);
                            Putphoto put = apiServiceHandle.upload().putphoto(greeting);
                            put.execute();
                            return greeting;
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Exception during API call!", Toast.LENGTH_LONG).show();
                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        // Displays the progress bar for the first time.
                        mBuilder.setProgress(100, 0, false);
                        mBuilder.setProgress(0, 0, true);
                        mNotifyManager.notify(id, mBuilder.build());
                    }

                    @Override
                    protected void onPostExecute(MainUploadRequestMessage greeting) {
                        if (greeting!=null) {
                            mBuilder.setContentText("Upload complete");
                            // Removes the progress bar
                            mBuilder.setProgress(0, 0, false);
                            mNotifyManager.notify(id, mBuilder.build());
                            Toast.makeText(getApplicationContext(), "Upload successfull!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "No greetings were returned by the API.", Toast.LENGTH_LONG).show();
                        }
                    }
                };

                //setto le impostazioni per la notifica dell'upload progress
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(UploadActivity.this);
                mBuilder.setContentTitle("Art Everywhere")
                        .setContentText("Artwork upload in progress")
                        .setSmallIcon(R.drawable.ic_launcher);


                //eseguo l'upload della foto
                uploadPhoto.execute((Void)null);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap bitmapOrg = BitmapFactory.decodeStream(imageStream);
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 100, bao);
                        ba = bao.toByteArray();
                        image=Base64.encodeToString(ba,Base64.DEFAULT);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Error during choosing artwork!", Toast.LENGTH_LONG).show();
                }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
