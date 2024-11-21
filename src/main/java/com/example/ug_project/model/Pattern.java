package com.example.ug_project.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Pattern {
    @Id
    @GeneratedValue
    private int id;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<String> pattern;

    public Pattern(List<String> pattern) {
        this.pattern = pattern;
    }

    public Pattern() {

    }

    public int getId() {
        return id;
    }

    public List<String> getPattern() {
        return pattern;
    }

    public void setPattern(List<String> pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        String patterns = "";
        for (String thePattern : pattern) {
            patterns += thePattern + ", ";
        }
        return patterns;
    }
}
