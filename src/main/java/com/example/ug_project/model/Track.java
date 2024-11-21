package com.example.ug_project.model;

import java.util.List;

public class Track {

    private String trackMarker;

    private int trackLength;

    private int noteNumber;

    private int totalPitch;

    private List<TrackEvent> trackEvents;

    public Track(String trackMarker, int trackLength, int noteNumber, int totalPitch, List<TrackEvent> trackEvents) {
        this.trackMarker = trackMarker;
        this.trackLength = trackLength;
        this.noteNumber = noteNumber;
        this.totalPitch = totalPitch;
        this.trackEvents = trackEvents;
    }

    public String getTrackMarker() {
        return trackMarker;
    }

    public void setTrackMarker(String trackMarker) {
        this.trackMarker = trackMarker;
    }

    public int getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(int trackLength) {
        this.trackLength = trackLength;
    }

    public int getNoteNumber() {
        return noteNumber;
    }

    public void setNoteNumber(int noteNumber) {
        this.noteNumber = noteNumber;
    }

    public int getTotalPitch() {
        return totalPitch;
    }

    public void setTotalPitch(int totalPitch) {
        this.totalPitch = totalPitch;
    }

    public List<TrackEvent> getTrackEvents() {
        return trackEvents;
    }

    public void setTrackEvents(List<TrackEvent> trackEvents) {
        this.trackEvents = trackEvents;
    }

}
