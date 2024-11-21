package com.example.ug_project.service;

import com.example.ug_project.model.MIDIFile;
import com.example.ug_project.model.Pattern;
import com.example.ug_project.model.Track;
import com.example.ug_project.model.TrackEvent;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.ug_project.service.MIDIReadService.conversion;
import static java.lang.Math.min;

public class PatternExtractionService {

    public static List<Pattern> mainMethod(MIDIFile midi, String method) {
        List<String> melody = new ArrayList<>();

        if (Objects.equals(method, "clustering")) {
            melody = melodyExtractionClustering(midi);
        }

        if (Objects.equals(method, "first")) {
            melody = melodyExtractionFirstTrack(midi);
        }

        if (Objects.equals(method, "longest")) {
            melody = melodyExtractionLongestTrack(midi);
        }

        if (Objects.equals(method, "highest")) {
            melody = melodyExtractionHighestPitch(midi);
        }

        List<Pattern> patterns = correlationMatrix(melody);
        return patterns;
    }

    public static List<String> melodyExtractionClustering(MIDIFile midi) {
        //combines all note on events into a single monophonic melody using the clustering method
        Map<Track, Integer> averagePitches = new HashMap<>();
        List<TrackEvent> wholeSong = new ArrayList<>();
        for (Track track : midi.getBody()) {
            wholeSong.addAll(track.getTrackEvents());
        }
        //get average pitches for all tracks to be compared against whole song
        for (Track track : midi.getBody()) {
            if (track.getNoteNumber() == 0) {
                continue;
            }
            averagePitches.put(track, averagePitch(track.getTrackEvents()));
        }
        List<Map.Entry<Track, Integer>> averagePitchesList = new ArrayList<>(averagePitches.entrySet());
        averagePitchesList.sort(Map.Entry.comparingByValue());

        List<List<Map.Entry<Track, Integer>>> clusters = new ArrayList<>(List.of(List.of(averagePitchesList.getFirst())));
        int currentCluster = 0;
        //define threshold interval for how different the next track can be before a new cluster is created
        int threshold = (averagePitchesList.getLast().getValue() - averagePitchesList.getFirst().getValue()) / (averagePitches.size() / 4 + 1);

        int totalAverage = averagePitchesList.getFirst().getValue();
        //construct clusters
        for (Map.Entry<Track, Integer> track : averagePitchesList.subList(1, averagePitches.size())) {
            if (track.getValue() - totalAverage / clusters.get(currentCluster).size() > threshold) {
                clusters.add(new ArrayList<>(List.of(track)));
                currentCluster += 1;
                totalAverage = track.getValue();
            } else {
                List<Map.Entry<Track, Integer>> current = new ArrayList<>(clusters.get(currentCluster));
                current.add(track);
                clusters.set(currentCluster, current);
                totalAverage += track.getValue();
            }
        }

        //compute distances between each track and the whole song
        //the closest distance in each cluster is selected
        List<TrackEvent> selectedTracks = new ArrayList<>();
        for (List<Map.Entry<Track, Integer>> cluster : clusters) {
            Track theTrack = cluster.getFirst().getKey();
            double score = EuclideanDistance(wholeSong, theTrack.getTrackEvents());
            for (Map.Entry<Track, Integer> track : cluster) {
                double distance = EuclideanDistance(wholeSong, track.getKey().getTrackEvents());
                if (distance < score) {
                    score = distance;
                    theTrack = track.getKey();
                }
            }
            for (TrackEvent event : theTrack.getTrackEvents()) {
                if (event.getMessage().equals("Note-On")) {
                    selectedTracks.add(event);
                }
            }
        }

        return skyline(selectedTracks);
    }

    public static List<String> melodyExtractionFirstTrack(MIDIFile midi) {
        return skyline(midi.getBody().getFirst().getTrackEvents());
    }

    public static List<String> melodyExtractionLongestTrack(MIDIFile midi) {
        Track theTrack = midi.getBody().get(0);
        for (Track track : midi.getBody()) {
            if (track.getTrackLength() > theTrack.getTrackLength()) {
                theTrack = track;
            }
        }

        return skyline(theTrack.getTrackEvents());
    }

    public static List<String> melodyExtractionHighestPitch(MIDIFile midi) {
        Track theTrack = midi.getBody().getFirst();
        int thePitch = averagePitch(theTrack.getTrackEvents());
        for (Track track : midi.getBody()) {
            int pitch = averagePitch(track.getTrackEvents());
            if (pitch > thePitch) {
                theTrack = track;
                thePitch = pitch;
            }
        }
        return skyline(theTrack.getTrackEvents());
    }

    public static List<String> skyline(List<TrackEvent> track) {
        List<String> melody = new ArrayList<>();
        Map<Integer, TrackEvent> events = new HashMap<>();
        for (TrackEvent event : track) {
            //skip if not an event that has a note
            if (!Objects.equals(event.getMessage(), "Note-On")) {
                continue;
            }
            //if the delta time doesn't exist then make a new entry
            //if it does exist then check the pitch and compare to the current event
            events.putIfAbsent(event.getDeltaTime(), event);
            if (Integer.parseInt(event.getData1()) > Integer.parseInt(events.get(event.getDeltaTime()).getData1())) {
                events.put(event.getDeltaTime(), event);
            }
        }
        //convert all selected events into text format and return
        for (Map.Entry<Integer, TrackEvent> event : events.entrySet()) {
            melody.add(numToNote(Integer.parseInt(event.getValue().getData1())));
        }
        return melody;
    }

    public static Integer averagePitch(List<TrackEvent> events) {
        int totalPitch = 0;
        int noteNumber = 0;
        for (TrackEvent event : events) {
            if (event.getMessage().equals("Note-On")) {
                totalPitch += Integer.parseInt(event.getData1());
                noteNumber += 1;
            }
        }
        return totalPitch / noteNumber;
    }

    //note interval and frequency
    public static Double EuclideanDistance(List<TrackEvent> track1, List<TrackEvent> track2) {
        double score = 0;
        Map<Integer, Integer> freq1 = positionInSpace(track1);
        Map<Integer, Integer> freq2 = positionInSpace(track2);

        //euclidean distance = sqrt( (a1-b1)^2 + (a2-b2)^2 + ... )
        for (Integer i : freq1.keySet()) {
            score += Math.pow(freq1.get(i) - freq2.get(i), 2);
        }

        return Math.sqrt(score);
    }

    public static Map<Integer, Integer> positionInSpace(List<TrackEvent> track) {
        Map<Integer, Integer> frequencies = new HashMap<>() {{
            put(0, 0);
            put(1, 0);
            put(2, 0);
            put(3, 0);
            put(4, 0);
            put(5, 0);
            put(6, 0);
            put(7, 0);
            put(8, 0);
            put(9, 0);
            put(10, 0);
            put(11, 0);
            put(12, 0);
        }};
        //get frequencies for all ranges of notes
        for (TrackEvent event : track) {
            if (!Objects.equals(event.getMessage(), "Note-On")) {
                continue;
            }
            frequencies.put(Integer.parseInt(event.getData1()) / 12, frequencies.get(Integer.parseInt(event.getData1()) / 12) + 1);
        }

        return frequencies;
    }

    public static String numToNote(int num) {
        //gets the octave of the current note
        String octave = Integer.toString((num / 12) - 2);
        String letter = conversion.get(String.valueOf(num % 12));
        return letter + octave;
    }

    public static Integer noteToNum(String note) {
        int number = Integer.parseInt(conversion.get(note.substring(0, note.length() - 1)));
        int multiplier = (Integer.parseInt(note.substring(note.length() - 1)) + 2) * 12;
        return multiplier + number;
    }


    public static List<Pattern> correlationMatrix(List<String> songs) {
        List<String> theSong = parsonCode(songs);
        //System.out.print(theSong);
        List<List<Integer>> matrix = new ArrayList<>();
        //create matrix
        for (int i = 0; i < theSong.size(); i++) {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < theSong.size(); j++) {
                matrix.get(i).add(0);
            }
        }

        //populate matrix by adding one to a value if the notes on each axis match
        for (int i = 0; i < theSong.size(); i++) {
            for (int j = i + 1; j < theSong.size(); j++) {
                if (Objects.equals(theSong.get(i), theSong.get(j)) && i != 0) {
                    matrix.get(i).set(j, matrix.get(i - 1).get(j - 1) + 1);
                } else {
                    matrix.get(i).set(j, 1);
                }
            }
        }

        Map<Pattern, Integer> frequency = patternCounter(theSong, matrix);
        return topPatterns(frequency);
    }

    public static Map<Pattern, Integer> patternCounter(List<String> theSong, List<List<Integer>> matrix) {
        Map<Pattern, Integer> frequency = new HashMap<>(Map.of());

        //minimum and maximum lengths of pattern allowed
        int minimum = 5;
        int maximum = 80;
        //loop through matrix and search for the highest numbers which indicate the end of a pattern
        for (int i = 0; i < theSong.size(); i++) {
            for (int j = i + 1; j < theSong.size(); j++) {
                int patternLength = matrix.get(i).get(j);
                if (patternLength > minimum && patternLength < maximum) {
                    //if the current position being checked isn't the last in a column or row
                    if (i < matrix.get(0).size() - 1 && j < matrix.get(0).size() - 1) {
                        //if the current position being checked in a pattern is part of a larger pattern
                        if ((matrix.get(i + 1).get(j + 1) > patternLength || matrix.get(i).get(j + 1) > patternLength) && (matrix.get(i + 1).get(j + 1) < maximum || matrix.get(i).get(j + 1) < maximum)) {
                            continue;
                        }
                    }
                    //System.out.println("The length of the pattern is: " + String.valueOf(patternLength));
                    List<String> pattern = new ArrayList<>();
                    for (int k = patternLength; k > 0; k--) {
                        //System.out.println(String.valueOf(theSong.get(j - k)));
                        pattern.add(theSong.get(j - k));
                    }
                    Pattern thePattern = new Pattern(pattern);
                    if (frequency.containsKey(thePattern)) {
                        frequency.replace(thePattern, frequency.get(thePattern) + 1);
                        //System.out.println(String.valueOf(pattern));
                    } else {
                        frequency.put(thePattern, 2);
                    }
                    //System.out.println();
                }
            }
        }
        return frequency;
    }

    public static List<Pattern> topPatterns(Map<Pattern, Integer> frequency) {
        //maximum number of patterns to be used
        int num = 3;

        //sort patterns by frequency and return the most frequent
        List<Map.Entry<Pattern, Integer>> patterns = new ArrayList<>(frequency.entrySet());
        patterns.sort(Map.Entry.comparingByValue());
        while (true) {
            if (!patterns.isEmpty()) {
                if (Collections.frequency(patterns.get(0).getKey().getPattern(), "0") > (double) patterns.get(0).getKey().getPattern().size() / 1.5) {
                    patterns.remove(0);
                    continue;
                }
            }
            if (patterns.size() > 1) {
                if (Collections.frequency(patterns.get(1).getKey().getPattern(), "0") > patterns.get(1).getKey().getPattern().size() / 1.5) {
                    patterns.remove(1);
                    continue;
                }
            }
            if (patterns.size() > 2) {
                if (Collections.frequency(patterns.get(2).getKey().getPattern(), "0") > patterns.get(2).getKey().getPattern().size() / 1.5) {
                    patterns.remove(2);
                    continue;
                }
            }
            break;
        }
        int number = min(num, patterns.size());
        return patterns.subList(0, number).stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public static int noteDifference(String n1, String n2) {
        //find the integer difference value between two notes
        int letter1 = 0;
        int letter2 = 0;

        if (n1.charAt(1) == 'b') {
            letter1 += Integer.parseInt(conversion.get(String.valueOf(n1.charAt(0)) + String.valueOf(n1.charAt(1))));
            n1 = n1.substring(2);
        } else {
            letter1 += Integer.parseInt(conversion.get(String.valueOf(n1.charAt(0))));
            n1 = n1.substring(1);
        }
        if (n2.charAt(1) == 'b') {
            letter2 += Integer.parseInt(conversion.get(String.valueOf(n2.charAt(0)) + String.valueOf(n2.charAt(1))));
            n2 = n2.substring(2);
        } else {
            letter2 += Integer.parseInt(conversion.get(String.valueOf(n2.charAt(0))));
            n2 = n2.substring(1);
        }

        int octave1 = Integer.parseInt(n1);
        int octave2 = Integer.parseInt(n2);

        return (octave1 * 12 + letter1) - (octave2 * 12 + letter2);
    }

    public static List<String> parsonCode(List<String> midi) {
        //convert song to parson code so each note is relative to the note before it
        List<String> code = new ArrayList<>();
        String previous = midi.get(0);
        for (String note : midi) {
            code.add(String.valueOf(noteDifference(previous, note)));
            previous = note;
        }
        return code;
    }
}
