package com.example.ug_project.service;

import com.example.ug_project.model.*;
import com.example.ug_project.repos.ClusterRepo;
import com.example.ug_project.repos.PatternRepo;
import com.example.ug_project.repos.SongRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Search {

    public static List<SongData> similarSongs(ClusterRepo clusterRepo, String filename) {
        File searchFile = new File(filename);
        SongData search = oneFile(searchFile, "clustering");
        List<SongData> similarSongs = new ArrayList<>(Search.searchSongs(clusterRepo, search));
        System.out.println(search);
        System.out.println("\n\nResults: \n");
        System.out.println(similarSongs);
        return similarSongs;
    }

    public static SongData oneFile(File file, String method) {
        //File directory = new File("C:\\Users\\joshu\\Documents\\University\\lmd\\lmd_full\\0\\");
        //File directory = new File("C:\\Users\\joshu\\Documents\\University\\UG_Project\\src\\main\\resources\\midi");

        try {

            MIDIFile MIDIData = MIDIReadService.mainMethod(String.valueOf(file));

            List<Pattern> savedPatterns = PatternExtractionService.mainMethod(MIDIData, method);

            //for (Pattern pattern : patterns) {
            //    pattern = patternRepo.save(pattern);
            //    savedPatterns.add(pattern);
            //}

            String fileName = MIDIData.getName();
            String name = "Not found.";
            String lyrics = "";
            List<String> instruments = new ArrayList<>();
            int tempo = 120;
            String timeSignature = "4/4";
            String keySignature = "Cmajor";
            String text = "";

            for (Track track : MIDIData.getBody()) {
                for (TrackEvent event : track.getTrackEvents()) {
                    if (Objects.equals(event.getMessage(), "Name") && Objects.equals(name, "Not found.")) {
                        name = event.getData1();
                    }
                    if (Objects.equals(event.getMessage(), "Lyric")) {
                        lyrics += event.getData1() + ", ";
                    }
                    if (Objects.equals(event.getMessage(), "Instrument-Name")) {
                        instruments.add(event.getData1());
                    }
                    if (Objects.equals(event.getMessage(), "Tempo")) {
                        tempo = 60000000 / Integer.parseInt(event.getData1());
                    }
                    if (Objects.equals(event.getMessage(), "Time-Signature")) {
                        timeSignature = event.getData1();
                    }
                    if (Objects.equals(event.getMessage(), "Key-Signature")) {
                        keySignature = event.getData1() + event.getData2();
                    }
                    if (Objects.equals(event.getMessage(), "Text")) {
                        text += event.getData1() + ", ";
                    }
                }
            }
            SongData song = new SongData(fileName, name, lyrics, instruments, savedPatterns, tempo, timeSignature, keySignature, text);
            System.out.println("Done!");
            return song;
        } catch (Exception e) {
            System.out.println("Unsuccessful reading of MIDI file.");
            System.out.println(e);
            return null;
        }
    }

    public static List<SongData> searchSongs(ClusterRepo clusterRepo, SongData song) {
        List<Cluster> clusters = (List<Cluster>) clusterRepo.findAll();
        Map<Cluster,Double> clusterScores = new HashMap<>();
        for (Cluster cluster : clusters) {
            clusterScores.put(cluster, KMeansClustering.EuclideanDistance(cluster, song));
        }

        List<Map.Entry<Cluster, Double>> orderedScores = new ArrayList<>(clusterScores.entrySet());
        Cluster theCluster = orderedScores.getFirst().getKey();
        Double currentScore = 0.0;
        for (Map.Entry<Cluster, Double> entry : orderedScores) {
            if(entry.getValue() > currentScore) {
                currentScore = entry.getValue();
                theCluster = entry.getKey();
            }
        }

        Map<SongData, Double> patternScores = KMP.mainMethod(theCluster.getSongsInCluster(), song.getPatterns());
        List<Map.Entry<SongData, Double>> orderedPatterns = new ArrayList<>(patternScores.entrySet());
        orderedPatterns.sort(Map.Entry.comparingByValue());
        List<SongData> songMatches = new ArrayList<>();
        for (Map.Entry<SongData, Double> entry : orderedPatterns) {
            songMatches.add(entry.getKey());
        }

        return songMatches;
    }


}
