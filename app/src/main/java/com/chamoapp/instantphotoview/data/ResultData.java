package com.chamoapp.instantphotoview.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Koo on 2016. 11. 1..
 */

public class ResultData {

    private Photos photos;
    public Photos getPhotos() {
        return photos;
    }

    public static class Photos{
        private int page;
        private int pages;
        private int perpage;
        private int total;
        private List<Photo> photo;

        public int getPage() {
            return page;
        }

        public int getPages() {
            return pages;
        }

        public int getPerpage() {
            return perpage;
        }

        public int getTotal() {
            return total;
        }

        public List<Photo> getPhoto() {
            return photo;
        }
    }

    private Person person;
    public Person getPerson(){
        return person;
    }

    public static class Person{
        @SerializedName("username")
        Content username;

        @SerializedName("realname")
        Content realname;

        @SerializedName("location")
        Content location;

        @SerializedName("profileurl")
        Content profileurl;

        int iconfarm;
        int iconserver;
        String nsid;

        public String getUsername() {
            return username.get_content();
        }

        public String getRealname() {
            return realname != null ? realname.get_content() : null;
        }

        public String getLocation() {
            return location.get_content();
        }

        public String getProfileurl() {
            return profileurl.get_content();
        }

        public int getIconfarm() {
            return iconfarm;
        }

        public int getIconserver() {
            return iconserver;
        }

        public String getNsid() {
            return nsid;
        }
    }
}
