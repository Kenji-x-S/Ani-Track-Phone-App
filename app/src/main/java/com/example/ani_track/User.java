package com.example.ani_track;

import java.util.List;

public class User {
    private String username;
    private String profileImageUrl;
    private String email;
    private List<String> watchlist;

    public User() {}

    public User(String username, String profileImageUrl, List<String> watchlist) {
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.watchlist = watchlist;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(List<String> watchlist) {
        this.watchlist = watchlist;
    }
}

