package com.viby.playit.models;

public class Song {
    public static Long count = 0L;
    private Long id;
    private String path;
    private String title;
    private String artist;
    private String album;
    private Long length;
    private String year;
    private String albumArt;

    public Song(String path) {
        this.path = path;
        this.id = count;
        count++;
    }

    public Long getId() { return this.id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getPath() {
        return path;
    }

    public void setPath(String path) { this.path = path; }

    public String getArtist() { return artist; }

    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }

    public void setAlbum(String album) { this.album = album; }

    public Long getLength() { return length; }

    public void setLength(Long length) { this.length = length; }

    public String getYear() { return year; }

    public void setYear(String year) { this.year = year; }

    public String getAlbumArt() { return albumArt; }

    public void setAlbumArt(String albumArt) { this.albumArt = albumArt; }
}
