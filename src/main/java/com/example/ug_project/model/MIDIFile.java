package com.example.ug_project.model;

import java.util.List;


public class MIDIFile {

    private int id;

    private String name;

    private Header header;

    private List<Track> body;

    public MIDIFile(int id, String name, Header header, List<Track> body) {
        this.id = id;
        this.name = name;
        this.header = header;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Track> getBody() {
        return body;
    }

    public void setBody(List<Track> body) {
        this.body = body;
    }
}
