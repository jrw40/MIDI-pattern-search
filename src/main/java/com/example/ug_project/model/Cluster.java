package com.example.ug_project.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.ug_project.service.KMeansClustering.noteToNum;

@Entity
public class Cluster {
    @Id
    @GeneratedValue
    private int id;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, Integer> instruments = new HashMap<>();

    private double tempo;

    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, Integer> timeSignature = new HashMap<>();

    private double keySignature;

    private int numberOfSongs;

    @OneToMany(fetch = FetchType.EAGER)
    private List<SongData> songsInCluster = new ArrayList<>();

    public Cluster() {
    }

    public Cluster(SongData songData) {
        for (String instrument : songData.getInstruments()) {
            this.instruments.put(instrument, 1);
        }
        this.tempo = songData.getTempo();
        this.timeSignature.put(songData.getTimeSignature(), 1);
        this.keySignature = noteToNum(songData.getKeySignature());
        this.numberOfSongs = 1;
        this.songsInCluster.add(songData);
    }

    public void addSong(SongData songData) {
        this.numberOfSongs += 1;
        this.songsInCluster.add(songData);
    }

    public Boolean reCluster() {
        Map<String, Integer> tempInstruments = this.instruments;
        double tempTempo = this.tempo;
        Map<String, Integer> tempTimeSignature = this.timeSignature;
        double tempKeySignature = this.keySignature;

        this.instruments = new HashMap<>();
        this.tempo = 0;
        this.timeSignature = new HashMap<>();
        this.keySignature = 0;

        for (SongData song : this.songsInCluster) {
            for (String instrument : song.getInstruments()) {
                this.instruments.putIfAbsent(instrument, 0);
                this.instruments.put(instrument, this.instruments.get(instrument) + 1);
            }
            this.tempo += song.getTempo();
            this.timeSignature.putIfAbsent(song.getTimeSignature(), 0);
            this.timeSignature.put(song.getTimeSignature(), this.timeSignature.get(song.getTimeSignature()) + 1);
            this.keySignature += noteToNum(song.getKeySignature());
        }
        this.tempo = this.tempo/this.numberOfSongs;
        this.keySignature = this.keySignature/this.numberOfSongs;

        this.numberOfSongs = 0;
        this.songsInCluster = new ArrayList<>();

        if (tempInstruments.equals(this.instruments) && tempTempo == this.tempo && tempTimeSignature.equals(this.timeSignature) && tempKeySignature == this.keySignature) {
            return true;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Integer> getInstruments() {
        return instruments;
    }

    public void setInstruments(Map<String, Integer> instruments) {
        this.instruments = instruments;
    }

    public double getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public Map<String, Integer> getTimeSignature() {
        return timeSignature;
    }

    public void setTimeSignature(Map<String, Integer> timeSignature) {
        this.timeSignature = timeSignature;
    }

    public double getKeySignature() {
        return keySignature;
    }

    public void setKeySignature(int keySignature) {
        this.keySignature = keySignature;
    }

    public int getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }

    public List<SongData> getSongsInCluster() {
        return songsInCluster;
    }

    public void setSongsInCluster(List<SongData> songsInCluster) {
        this.songsInCluster = songsInCluster;
    }
}
