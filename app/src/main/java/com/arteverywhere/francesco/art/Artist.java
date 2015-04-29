package com.arteverywhere.francesco.art;

/**
 * Created by Francesco on 11/02/2015.
 */
public class Artist {
    private String name;
    private String photo;
    private String email;

    public Artist(){}

    public Artist(String n, String p, String e) {
        super();
        this.name = n;
        this.photo = p;
        this.email = e;
    }

    //getters & setters
    public void setName(String x){
        this.name = x;
    }

    public void setPhoto(String x){
        this.photo = x;
    }

    public void setEmail(String x){
        this.email = x;
    }

    public String getName(){
        return name;
    }

    public String getPhoto(){
        return photo;
    }

    public String getEmail(){
        return email;
    }



    @Override
    public String toString() {
        return "Artist [name=" + name + ", photo=" + photo
                + ", email="+ email
                + "]";
    }
}
