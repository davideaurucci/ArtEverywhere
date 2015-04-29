package com.arteverywhere.francesco.art;

/**
 * Created by Francesco on 11/02/2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

//import cod.com.appspot.endpoints_final.testGCS.TestGCS;
//import cod.com.appspot.endpoints_final.testGCS.model.MainDownloadResponseCollection;

public class DatabaseArtwork extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ArtEverywhereDB";
    //Table Name
    private static final String TABLE_ARTWORKS = "artworks";

    public DatabaseArtwork(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        Log.d("DB","creo tabella");
        String CREATE_ARTWORK_TABLE = "CREATE TABLE IF NOT EXISTS artworks ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "filename TEXT, "+
                "photo TEXT, " +
                "artista TEXT, "+
                "descrizione TEXT, "+
                "dimensioni TEXT, "+
                "luogo TEXT, "+
                "tecnica TEXT, "+
                "likes INTEGER, "+
                "data TEXT "+
                ")";


        //prima di creare una tabella, cancello quella precedente
        db.execSQL(CREATE_ARTWORK_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DB","upgrade");
        db.execSQL("DROP TABLE IF EXISTS artworks");
        this.onCreate(db);
    }

    public void clear(SQLiteDatabase db){
        db.execSQL("DELETE FROM artworks");
    }

    public void removeArtwork(String url, SQLiteDatabase db){
        db.delete(TABLE_ARTWORKS, "photo" + " = ?", new String[]{url});

    }

    protected void insert(Artwork aw, SQLiteDatabase db){
        try {
            SQLiteStatement insStmt = db.compileStatement(
                    "INSERT INTO artworks ('filename','photo','artista','descrizione','dimensioni','luogo','tecnica','likes','data') VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            db.beginTransaction();

            insStmt.bindString(1, "" + aw.getFilename());
            insStmt.bindString(2, "" + aw.getPhoto());
            insStmt.bindString(3, "" + aw.getArtista());
            insStmt.bindString(4, "" + aw.getDescrizione());
            insStmt.bindString(5, "" + aw.getDimensioni());
            insStmt.bindString(6, "" + aw.getLuogo());
            insStmt.bindString(7, "" + aw.getTecnique());
            insStmt.bindLong(8, aw.getLikes());
            insStmt.bindString(9, "" + aw.getData());

            insStmt.executeInsert();

        }catch(Exception e){
            e.printStackTrace();
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    // Get All Books
    public List<Artwork> getAllArtworks() {
        List<Artwork> artworks = new LinkedList<Artwork>();

        // 1. build the query
        String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Artwork artwork = null;
        if (cursor.moveToFirst()) {
            do {
                artwork = new Artwork();
                artwork.setId(Integer.parseInt(cursor.getString(0)));
                artwork.setFilename((cursor.getString(1)));
                artwork.setPhoto(cursor.getString(2));
                artwork.setArtista(cursor.getString(3));
                artwork.setDescrizione(cursor.getString(4));
                artwork.setDimensioni(cursor.getString(5));
                artwork.setLuogo(cursor.getString(6));
                artwork.setTecnique(cursor.getString(7));
                artwork.setLikes(Integer.parseInt(cursor.getString(8)));
                artwork.setData(cursor.getString(9));

                // Add book to books
                artworks.add(artwork);
            } while (cursor.moveToNext());
        }

        // return books
        return artworks;
    }

    public String[] getPhotos() {
        //List<Artwork> artworks = new LinkedList<Artwork>();
        String[] photos = new String[AppConstants.numFoto];

        // 1. build the query
        String query = "SELECT photo FROM " + TABLE_ARTWORKS;
        //String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String ph;
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                ph = cursor.getString(0);

                // Add photo to photos
                photos[i] = ph;
                if(i!=(AppConstants.numFoto-1)) i++;
            } while (cursor.moveToNext());
        }

        // return books
        return photos;
    }

    public String[] getPhotosAfterScroll(int num) {
        String[] photos = new String[num];

        // 1. build the query
        String query = "SELECT photo FROM " + TABLE_ARTWORKS;
        //String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String ph;
        int i = 0;
        int c=0;
        if (cursor.moveToFirst()) {
            do {
                ph = cursor.getString(0);
                // Add photo to photos
                if(ph!=null) c++;
                photos[i]=ph;

                if(i!=(num-1)) i++;
            } while (cursor.moveToNext());
        }

        // return books
        String [] foto;
        if (c>=i+1){
            foto=new String[i+1];
            for(int t=0; t<i+1; t++){
                foto[t]=photos[t];
            }
        }
        else{
            foto=new String[c];
            for(int t=0; t<c; t++){
                foto[t]=photos[t];
            }
        }
        return foto;
    }

    public Artwork getArtworkFromUrl(String url) {

        // 1. build the query
        String query = "SELECT * FROM " + TABLE_ARTWORKS + " WHERE photo=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { url });

        // 3. go over each row, build book and add it to list
        Artwork artwork = null;
        if (cursor.moveToFirst()) {
            do {
                artwork = new Artwork();
                artwork.setId(Integer.parseInt(cursor.getString(0)));
                artwork.setFilename((cursor.getString(1)));
                artwork.setPhoto(cursor.getString(2));
                artwork.setArtista(cursor.getString(3));
                artwork.setDescrizione(cursor.getString(4));
                artwork.setDimensioni(cursor.getString(5));
                artwork.setLuogo(cursor.getString(6));
                artwork.setTecnique(cursor.getString(7));
                artwork.setLikes(Integer.parseInt(cursor.getString(8)));
                artwork.setData(cursor.getString(9));

                // Add book to books
            } while (cursor.moveToNext());
        }

        return artwork;
    }


    public String[] getArtworksOrderByDate(){
        String[] photos = new String[AppConstants.numFoto];

        // 1. build the query
        String query = "SELECT photo FROM " + TABLE_ARTWORKS + " ORDER BY data DESC";
        //String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String ph;
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                ph = cursor.getString(0);

                // Add photo to photos
                photos[i] = ph;
                if(i!=(AppConstants.numFoto-1)) i++;
            } while (cursor.moveToNext());
        }

        // return books
        return photos;
    }


    public String[] getArtworksOrderByDate(int c){
        String[] photos = new String[c];

        // 1. build the query
        String query = "SELECT photo FROM " + TABLE_ARTWORKS + " ORDER BY data DESC";
        //String query = "SELECT * FROM " + TABLE_ARTWORKS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String ph;
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                ph = cursor.getString(0);

                // Add photo to photos
                photos[i] = ph;
                if(i!=(c-1)) i++;
            } while (cursor.moveToNext());
        }

        return photos;
    }

    public String getMostRecentDate(){

        String data = "";
        // 1. build the query
        String query = "SELECT data FROM " + TABLE_ARTWORKS + " ORDER BY data DESC LIMIT 1";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String d;
        if (cursor.moveToFirst()) {
            do {
                d = cursor.getString(0);
                data = d;
            } while (cursor.moveToNext());
        }
        return data;
    }


    public String[] getArtworksFromTechinique(String tecnica) {

        // 1. build the query
        String query = "SELECT count(*) FROM " + TABLE_ARTWORKS + " WHERE tecnica=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { tecnica });
        // 3. go over each row, build book and add it to list
        int quanteFoto = 0;
        if (cursor.moveToFirst()) {
            do {
                quanteFoto = Integer.parseInt(cursor.getString(0));
            } while (cursor.moveToNext());

        }

        // 1. build the query
        String query2 = "SELECT photo FROM " + TABLE_ARTWORKS + " WHERE tecnica=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db2 = this.getWritableDatabase();
        Cursor cursor2 = db2.rawQuery(query2, new String[] { tecnica });

        // 3. go over each row, build book and add it to list
        String[] photos;
        if(quanteFoto < AppConstants.numFotoFiltri){
            photos = new String[quanteFoto];
        }else{
            photos = new String[AppConstants.numFotoFiltri];
        }

        int i = 0;
        if (cursor2.moveToFirst()) {
            do {
                String d = cursor2.getString(0);
                photos[i] = d;
                i++;
            } while (cursor2.moveToNext());
        }

        return photos;
    }


    public String[] getArtworksFromPlace(String luogo) {

        // 1. build the query
        String query = "SELECT count(*) FROM " + TABLE_ARTWORKS + " WHERE luogo=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { luogo });

        // 3. go over each row, build book and add it to list
        int quanteFoto = 0;
        if (cursor.moveToFirst()) {
            do {
                quanteFoto = Integer.parseInt(cursor.getString(0));
                //System.out.println("**"+quanteFoto);
            } while (cursor.moveToNext());
        }

        // 1. build the query
        String query2 = "SELECT photo FROM " + TABLE_ARTWORKS + " WHERE luogo=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db2 = this.getWritableDatabase();
        Cursor cursor2 = db2.rawQuery(query2, new String[] { luogo });

        // 3. go over each row, build book and add it to list
        String[] photos;
        if(quanteFoto < AppConstants.numFotoFiltri){
            photos = new String[quanteFoto];
        }else{
            photos = new String[AppConstants.numFotoFiltri];
        }

        int i = 0;
        if (cursor2.moveToFirst()) {
            do {
                String d = cursor2.getString(0);
                photos[i] = d;
                i++;
            } while (cursor2.moveToNext());
        }
        return photos;
    }

    public String[] getArtworksOfArtist(String arti) {
        // 1. build the query
        String query = "SELECT distinct count(*) FROM " + TABLE_ARTWORKS + " WHERE artista=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { arti });

        // 3. go over each row, build book and add it to list
        int quanteFoto = 0;
        if (cursor.moveToFirst()) {
            do {
                quanteFoto = Integer.parseInt(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        // 1. build the query
        String query2 = "SELECT distinct photo FROM " + TABLE_ARTWORKS + " WHERE artista=? ";

        // 2. get reference to writable DB
        SQLiteDatabase db2 = this.getWritableDatabase();
        Cursor cursor2 = db2.rawQuery(query2, new String[] { arti });

        // 3. go over each row, build book and add it to list
        /*String[] photos;
        if(quanteFoto < AppConstants.numFoto){
            photos = new String[quanteFoto];
        }else{
            photos = new String[AppConstants.numFoto];
        }*/
        String[] photos = new String[quanteFoto];

        int i = 0;
        if (cursor2.moveToFirst()) {
            do {
                String d = cursor2.getString(0);
                photos[i] = d;
                i++;
            } while (cursor2.moveToNext());
        }
        return photos;
    }
    public void setArtworkFromUrl(String url, Artwork artwork){
        SQLiteDatabase db=this.getWritableDatabase();
        // define the new value you want
        ContentValues newValues = new ContentValues();
        newValues.put("filename", artwork.getFilename());
        newValues.put("descrizione", artwork.getDescrizione());
        newValues.put("likes", artwork.getLikes());
        newValues.put("tecnica", artwork.getTecnique());
        newValues.put("luogo", artwork.getLuogo());
        newValues.put("dimensioni", artwork.getDimensioni());
        // you can .put() even more here if you want to update more than 1 row

        // define the WHERE clause w/o the WHERE and replace variables by ?
        // Note: there are no ' ' around ? - they are added automatically
        String whereClause = "photo == ?";

        // now define what those ? should be
        String[] whereArgs = new String[] {
                // in order the ? appear
                url
        };

        db.update(TABLE_ARTWORKS, newValues, whereClause, whereArgs);


    }

}