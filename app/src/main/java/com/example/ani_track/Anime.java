package com.example.ani_track;

public class Anime {

    private String title;
    private String description;
    private String imageUrl;
    private int animeId;
    private String status;

    public Anime() {
    }

    public Anime(String title, String description, String imageUrl, int animeId, String status) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.animeId = animeId;
        this.status = status;
    }

    public int getAnimeId() {
        return animeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

