package com.example.ug_project.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class SongData {
    @Id
    @GeneratedValue
    private int id;

    private String fileName;

    private String name;

    @Column(columnDefinition="TEXT NOT NULL", length = 3000)
    @MapKeyColumn(columnDefinition="TEXT NOT NULL")
    private String lyrics;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> instruments;

    @OneToMany(fetch = FetchType.EAGER)
    @Column(columnDefinition="TEXT NOT NULL", length = 3000)
    @MapKeyColumn(columnDefinition="TEXT NOT NULL")
    private List<Pattern> patterns;

    private int tempo;

    private String timeSignature;

    private String keySignature;

    @Column(columnDefinition="TEXT NOT NULL")
    @MapKeyColumn(columnDefinition="TEXT NOT NULL")
    private String text;

    public SongData(String fileName, String name, String lyrics, List<String> instruments, List<Pattern> patterns, int tempo, String timeSignature, String keySignature, String text) {
        this.fileName = fileName;
        this.name = name;
        this.lyrics = lyrics;
        this.instruments = instruments;
        this.patterns = patterns;
        this.tempo = tempo;
        this.timeSignature = timeSignature;
        this.keySignature = keySignature;
        this.text = text;
    }

    public SongData() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public List<String> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<String> instruments) {
        this.instruments = instruments;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public String getTimeSignature() {
        return timeSignature;
    }

    public void setTimeSignature(String timeSignature) {
        this.timeSignature = timeSignature;
    }

    public String getKeySignature() {
        return keySignature;
    }

    public void setKeySignature(String keySignature) {
        this.keySignature = keySignature;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        String instrument = "";
        String pattern = "";

        for (Pattern p : patterns) {
            pattern += " [" + p + "], ";
        }
        for (String i : instruments) {
            instrument += i + ", ";
        }

        return "\n\nSongData{" +
                "\nid=" + id +
                ", \nfileName='" + fileName + '\'' +
                ", \nname='" + name + '\'' +
                ", \nlyrics='" + lyrics + '\'' +
                ", \ninstruments=" + instrument +
                ", \npatterns=" + pattern +
                ", \ntempo=" + tempo +
                ", \ntimeSignature='" + timeSignature + '\'' +
                ", \nkeySignature='" + keySignature + '\'' +
                ", \ntext='" + text + '\'' +
                '}';
    }
}
