package com.arteverywhere.francesco.art;

/**
 * Created by Francesco on 11/02/2015.
 */
public class Artwork {
    private int id;
    private String filename;
    private String photo;
    private String artista;
    private String descrizione;
    private String dimensioni;
    private String luogo;
    private String tecnique;
    private long likes;
    private String data;

    public Artwork(){}

    public Artwork(String filename, String photo, String artista, String descr, String dim, String luogo, String tec, long likes, String data) {
        super();
        this.filename = filename;
        this.photo = photo;
        this.artista = artista;
        this.descrizione = descr;
        this.dimensioni = dim;
        this.luogo = luogo;
        this.tecnique = tec;
        this.likes = likes;
        this.data = data;
    }

    //getters & setters
    public void setId(int x){
        id = x;
    }

    public void setFilename(String x){
        this.filename = x;
    }

    public void setPhoto(String x){
        this.photo = x;
    }

    public void setArtista(String x){
        this.artista = x;
    }

    public void setDescrizione(String x){
        this.descrizione = x;
    }

    public void setDimensioni(String x){
        this.dimensioni = x;
    }

    public void setLuogo(String x){
        this.luogo = x;
    }

    public void setTecnique(String x){
        this.tecnique = x;
    }

    public void setLikes(long x){
        this.likes = x;
    }

    public void setData(String x){
        this.data = x;
    }

    public String getFilename(){
        return filename;
    }

    public String getPhoto(){
        return photo;
    }

    public String getArtista(){
        return artista;
    }

    public String getDescrizione(){
        return descrizione;
    }

    public String getDimensioni(){
        return dimensioni;
    }

    public String getLuogo(){
        return luogo;
    }

    public String getTecnique(){
        return  tecnique;
    }

    public long getLikes(){
        return likes;
    }

    public String getData(){
        return data;
    }



    @Override
    public String toString() {
        return "Artwork [id=" + id + ", filename=" + filename + ", artista=" + artista
                + ", descrizione="+ descrizione + ", dimensioni="+dimensioni
                + ", luogo="+luogo + ", tecnica="+tecnique + ",likes="+likes
                + "]";
    }
}
