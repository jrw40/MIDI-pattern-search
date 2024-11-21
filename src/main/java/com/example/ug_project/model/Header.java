package com.example.ug_project.model;

public class Header {
    private enum pattern{
        SINGLE,
        MULTI,
        MULTIPLE
    }

    private String fileType;

    private int headerLength;

    private pattern trackPattern;

    private int numberOfTracks;

    private int timing;

    public Header(String fileType, int headerLength, int trackPattern, int numberOfTracks, int timing) {
        this.fileType = fileType;
        this.headerLength = headerLength;
        switch (trackPattern) {
            case 0:
                this.trackPattern = pattern.SINGLE;
                break;
            case 1:
                this.trackPattern = pattern.MULTI;
                break;
            case 2:
                this.trackPattern = pattern.MULTIPLE;
                break;
            default:
                this.trackPattern = pattern.SINGLE;
        }
        this.numberOfTracks = numberOfTracks;
        this.timing = timing;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    private pattern getTrackPattern() {
        return trackPattern;
    }

    private void setTrackPattern(pattern trackPattern) {
        this.trackPattern = trackPattern;
    }

    public int getNumberOfTracks() {
        return numberOfTracks;
    }

    public void setNumberOfTracks(int numberOfTracks) {
        this.numberOfTracks = numberOfTracks;
    }

    public int getTiming() {
        return timing;
    }

    public void setTiming(int timing) {
        this.timing = timing;
    }
}
