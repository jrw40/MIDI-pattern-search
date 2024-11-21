package com.example.ug_project.service;

import com.example.ug_project.model.Cluster;
import com.example.ug_project.model.SongData;
import com.example.ug_project.repos.ClusterRepo;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.example.ug_project.service.MIDIReadService.conversion;

public class KMeansClustering {

    public static Map<Integer, Cluster> mainMethod(List<SongData> songData, ClusterRepo clusterRepo, String method) {
        List<SongData> songsRemaining = songData;
        int numberOfClusters = 4;
        Map<Integer, Cluster> clusters = initialiseClusters(songData.subList(0, numberOfClusters), clusterRepo);
        songsRemaining = songsRemaining.subList(numberOfClusters, songsRemaining.size());
        System.out.println("Clusters created...");

        int counter = 0;
        boolean changed = false;
        while (!changed) {
            System.out.println(counter);
            while (!songsRemaining.isEmpty()) {
                Integer clusterID = clusterSong(clusters, songsRemaining.getFirst(), clusterRepo);
                clusters.get(clusterID).addSong(songsRemaining.getFirst());
                songsRemaining = songsRemaining.subList(1, songsRemaining.size());
                System.out.println(songData.size() - songsRemaining.size() + " songs processed out of " + songData.size());
            }

            clusterRepo.saveAll(clusters.values());

            changed = true;
            counter++;
            System.out.println(counter);

            for (int i = 1; i <= clusters.size(); i++) {
                System.out.println("reClustering...");
                boolean save = clusters.get(i).reCluster();
                if (!save) {
                    changed = false;
                }
                clusterRepo.save(clusters.get(i));
            }
            if (counter == 50) {
                changed = true;
            }
            songsRemaining = songData;
            System.out.println(counter);
        }
        while (!songsRemaining.isEmpty()) {
            Integer clusterID = clusterSong(clusters, songsRemaining.getFirst(), clusterRepo);
            clusters.get(clusterID).addSong(songsRemaining.getFirst());
            songsRemaining = songsRemaining.subList(1, songsRemaining.size());
            System.out.println(songData.size() - songsRemaining.size() + " songs processed out of " + songData.size());
        }
        clusterRepo.saveAll(clusters.values());
        return clusters;
    }

    public static Map<Integer, Cluster> initialiseClusters(List<SongData> songData, ClusterRepo clusterRepo) {
        Map<Integer, Cluster> clusters = new HashMap<>();
        for (SongData song : songData) {
            Cluster cluster = new Cluster(song);
            clusterRepo.save(cluster);
            clusters.put(cluster.getId(), cluster);
        }
        return clusters;
    }

    public static Integer clusterSong(Map<Integer, Cluster> clusters, SongData song, ClusterRepo clusterRepo) {
        Map<Integer, Double> distances = new HashMap<>();
        String method = "euclidean";
        for (int i=1; i<=clusters.size();i++) {
            distances.put(i, EuclideanDistance(clusterRepo.findById(i), song));
        }

        if (Objects.equals(method, "manhattan")) {
            for (Cluster cluster : clusters.values()) {
                distances.put(cluster.getId(), ManhattanDistance(cluster, song));
            }
        }

        if (Objects.equals(method, "cosine")) {
            for (Cluster cluster : clusters.values()) {
                distances.put(cluster.getId(), CosineSimilarity(cluster, song));
            }
        }

        return Collections.min(distances.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static Double EuclideanDistance(Cluster cluster, SongData song) {
        List<Double> theSong = new ArrayList<>();
        List<Double> theCluster = new ArrayList<>();

        //value for instruments
        double instNum = 0;
        for (String instrument : song.getInstruments()) {
            cluster.getInstruments().putIfAbsent(instrument, 0);
            instNum += cluster.getInstruments().get(instrument);
        }
        double mapNum = 0;
        for (String instrument : cluster.getInstruments().keySet()) {
            mapNum += cluster.getInstruments().get(instrument);
        }
        theSong.add(instNum);
        theCluster.add(mapNum);

        //value for tempo
        theSong.add((double) song.getTempo());
        theCluster.add((double) cluster.getTempo());

        //value for time signature
        cluster.getTimeSignature().putIfAbsent(song.getTimeSignature(), 0);
        double songTime = cluster.getTimeSignature().get(song.getTimeSignature());
        double mapTime = 0;
        for (String timeSignature : cluster.getTimeSignature().keySet()) {
            mapTime += cluster.getTimeSignature().get(timeSignature);
        }
        theSong.add(songTime);
        theCluster.add(mapTime);

        //value for key signature
        double keySignature = noteToNum(song.getKeySignature());
        if (Math.abs(keySignature - cluster.getKeySignature()) > 6) {
            keySignature += 12;
        }
        theSong.add(keySignature);
        theCluster.add(cluster.getKeySignature());

        double score = 0;
        for (int i = 0; i < theSong.size(); i++) {
            score += Math.pow(theSong.get(i) - theCluster.get(i), 2);
        }
        return Math.sqrt(score);
    }

    public static Double ManhattanDistance(Cluster cluster, SongData song) {
        List<Double> theSong = new ArrayList<>();
        List<Double> theCluster = new ArrayList<>();

        //value for instruments
        double instNum = 0;
        for (String instrument : song.getInstruments()) {
            cluster.getInstruments().putIfAbsent(instrument, 0);
            instNum += cluster.getInstruments().get(instrument);
        }
        double mapNum = 0;
        for (String instrument : cluster.getInstruments().keySet()) {
            mapNum += cluster.getInstruments().get(instrument);
        }
        theSong.add(instNum);
        theCluster.add(mapNum);

        //value for tempo
        theSong.add((double) song.getTempo());
        theCluster.add((double) cluster.getTempo());

        //value for time signature
        cluster.getTimeSignature().putIfAbsent(song.getTimeSignature(), 0);
        double songTime = cluster.getTimeSignature().get(song.getTimeSignature());
        double mapTime = 0;
        for (String timeSignature : cluster.getTimeSignature().keySet()) {
            mapTime += cluster.getTimeSignature().get(timeSignature);
        }
        theSong.add(songTime);
        theCluster.add(mapTime);

        //value for key signature
        double keySignature = noteToNum(song.getKeySignature());
        if (Math.abs(keySignature - cluster.getKeySignature()) > 6) {
            keySignature += 12;
        }
        theSong.add(keySignature);
        theCluster.add(cluster.getKeySignature());

        double score = 0;
        for (int i = 0; i < theSong.size(); i++) {
            score += Math.abs(theSong.get(i) - theCluster.get(i));
        }
        return score;
    }

    public static Double CosineSimilarity(Cluster cluster, SongData song) {
        List<Double> theSong = new ArrayList<>();
        List<Double> theCluster = new ArrayList<>();

        //value for instruments
        double instNum = 0;
        for (String instrument : song.getInstruments()) {
            cluster.getInstruments().putIfAbsent(instrument, 0);
            instNum += cluster.getInstruments().get(instrument);
        }
        double mapNum = 0;
        for (String instrument : cluster.getInstruments().keySet()) {
            mapNum += cluster.getInstruments().get(instrument);
        }
        theSong.add(instNum);
        theCluster.add(mapNum);

        //value for tempo
        theSong.add((double) song.getTempo());
        theCluster.add((double) cluster.getTempo());

        //value for time signature
        cluster.getTimeSignature().putIfAbsent(song.getTimeSignature(), 0);
        double songTime = cluster.getTimeSignature().get(song.getTimeSignature());
        double mapTime = 0;
        for (String timeSignature : cluster.getTimeSignature().keySet()) {
            mapTime += cluster.getTimeSignature().get(timeSignature);
        }
        theSong.add(songTime);
        theCluster.add(mapTime);

        //value for key signature
        double keySignature = noteToNum(song.getKeySignature());
        if (Math.abs(keySignature - cluster.getKeySignature()) > 6) {
            keySignature += 12;
        }
        theSong.add(keySignature);
        theCluster.add(cluster.getKeySignature());

        double top = 0;
        double bottomLeft = 0;
        double bottomRight = 0;
        for (int i = 0; i < theSong.size(); i++) {
            top += theSong.get(i) * theCluster.get(i);
            bottomRight += Math.pow(theCluster.get(i), 2);
            bottomLeft += Math.pow(theSong.get(i), 2);
        }
        return top / (Math.sqrt(bottomRight) * Math.sqrt(bottomLeft));
    }

    public static Double noteToNum(String note) {
        double number = Double.parseDouble(conversion.get(note.substring(0, 1)));
        if (note.length() > 1) {
            if (note.substring(1).equals("major")) {
                number += 1;
            } else {
                number -= 1;
            }
        }
        return number;
    }
}
