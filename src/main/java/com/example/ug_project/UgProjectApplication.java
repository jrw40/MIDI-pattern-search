package com.example.ug_project;

import com.example.ug_project.model.*;
import com.example.ug_project.repos.ClusterRepo;
import com.example.ug_project.repos.PatternRepo;
import com.example.ug_project.repos.SongRepo;
import com.example.ug_project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.util.*;

@SpringBootApplication
public class UgProjectApplication implements ApplicationRunner {

    @Autowired
    SongRepo songRepo;

    @Autowired
    PatternRepo patternRepo;

    @Autowired
    ClusterRepo clusterRepo;

    public static void main(String[] args) {
        SpringApplication.run(UgProjectApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        List<SongData> songData = new ArrayList<>();
        songData = processFiles("clustering");

        try {
            System.out.println("Finished!");
            Map<SongData, Double> score = new HashMap<>(KMP.mainMethod(songData.subList(1, songData.size()), songData.getFirst().getPatterns()));
        } catch (Exception e) {
            System.out.println("Unsuccessful pattern extraction.");
        }
        try {
            System.out.println("Clustering...");
            Map<Integer, Cluster> theClusters = KMeansClustering.mainMethod(songData, clusterRepo, "euclidean");
            clusterRepo.saveAll(theClusters.values());
            System.out.println("Finished!");
        } catch (Exception e) {
            System.out.println("Unsuccessful clustering.");
        }

        File searchFile = new File("C:\\Users\\joshu\\Documents\\University\\UG_Project\\src\\main\\resources\\midi\\001aed0e250bcca69cd78bb30c77cf9e.mid");
        SongData search = oneFile(searchFile, "clustering");
        List<SongData> similarSongs = new ArrayList<>(Search.searchSongs(clusterRepo, search));
        System.out.println(search);
        System.out.println("\n\nResults: \n");
        System.out.println(similarSongs);

        System.out.println("Finished!");
    }

    public List<SongData> processFiles(String method) {
        File directory = new File("C:\\Users\\joshu\\Documents\\University\\lmd\\lmd_full\\0\\");
        //File directory = new File("C:\\Users\\joshu\\Documents\\University\\UG_Project\\src\\main\\resources\\midi");
        File[] files = directory.listFiles();
        assert files != null;

        List<SongData> songData = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();

        int count = 0;
        for (File file : files) {
            try {
                System.out.println(count);
                count++;
                if (count == 40) {
                    break;
                }
                MIDIFile MIDIData = MIDIReadService.mainMethod(String.valueOf(file));
                if (MIDIData == null) {
                    continue;
                }

                List<Pattern> savedPatterns = PatternExtractionService.mainMethod(MIDIData, method);
                patterns.addAll(savedPatterns);

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
                songData.add(song);
                System.out.println("Done!");
            } catch (Exception e) {
                System.out.println("Unsuccessful reading of MIDI file.");
                System.out.println(e);
            }
        }
        patternRepo.saveAll(patterns);
        songRepo.saveAll(songData);

        return songData;
    }

    public List<SongData> similarSongs() {
        File searchFile = new File("C:\\Users\\joshu\\Documents\\University\\UG_Project\\src\\main\\resources\\midi\\001aed0e250bcca69cd78bb30c77cf9e.mid");
        SongData search = oneFile(searchFile, "clustering");
        List<SongData> similarSongs = new ArrayList<>(Search.searchSongs(clusterRepo, search));
        System.out.println(search);
        System.out.println("\n\nResults: \n");
        System.out.println(similarSongs);
        return similarSongs;
    }

    public SongData oneFile(File file, String method) {
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
}
