package com.example.ug_project.service;

import com.example.ug_project.model.Pattern;
import com.example.ug_project.model.SongData;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.util.*;

public class KMP {
    public static Map<SongData, Double> mainMethod(List<SongData> searchPatterns, List<Pattern> patterns) {
        Map<SongData, Double> finalScores = new HashMap<>();
        //loop through patterns in query
        for (Pattern pattern : patterns) {
            List<Integer> prefix = prefixTable(pattern.getPattern());
            //loop through songs in search space
            for (SongData songPatterns : searchPatterns) {
                //loop through patterns in search song
                for (Pattern individualPattern : songPatterns.getPatterns()) {
                    double score = intMatch(pattern, individualPattern, prefix);
                    score += finalScores.getOrDefault(songPatterns, 0.0);
                    finalScores.put(songPatterns, score);
                }
            }
        }
        return finalScores;
    }

    public static List<Integer> prefixTable(List<String> pattern) {
        List<Integer> prefix = new ArrayList<>();
        //populate prefix table with 0s
        while (prefix.size() < pattern.size()) prefix.add(0);
        int i = 0;
        int j = 1;
        while (j < prefix.size()) {
            //if notes are equal then add one in the prefix table
            if (Objects.equals(pattern.get(i), pattern.get(j))) {
                prefix.set(j, i + 1);
                i++;
                j++;
            } else {
                //if notes aren't equal then check if i is 0 to decide whether to advance i or j
                if (i == 0) {
                    j++;
                } else {
                    i = prefix.get(i);
                }
            }
        }
        return prefix;
    }

    public static Double stringMatch(Pattern query, Pattern search, List<Integer> prefix) {
        int i = 0;
        int j = 0;
        int longestString = 0;
        int totalMatches = 0;
        int currentString = 0;
        int found = 0;
        while (j < search.getPattern().size()) {
            //if notes match
            if (query.getPattern().get(i).equals(search.getPattern().get(j))) {
                totalMatches += 1;
                currentString += 1;
                //if the whole string has been found
                if (i == query.getPattern().size() - 1) {
                    found += 1;
                    longestString = Math.max(longestString, currentString);
                    currentString = 0;
                    i = prefix.get(i - 1);
                } else {
                    //if only part of the string has been found
                    i++;
                    j++;
                }
            } else {
                longestString = Math.max(longestString, currentString);
                if (i != 0) {
                    i = prefix.get(i - 1);
                    currentString = i;
                } else {
                    j++;
                    currentString = 0;
                }
            }
        }
        return (double) totalMatches / (double) search.getPattern().size() + (double) longestString / (double) query.getPattern().size() + (double) found;
    }

    public static Double intMatch(Pattern query, Pattern search, List<Integer> prefix) {
        int i = 0;
        int j = 0;
        double longestString = 0.0;
        int totalMatches = 0;
        int currentString = 0;
        int found = 0;
        int threshold = 10;
        int kMismatches = 0;
        while (j < search.getPattern().size()) {
            kMismatches = kMismatches + Math.abs(Integer.parseInt(query.getPattern().get(i)) - Integer.parseInt(search.getPattern().get(j)));
            //if notes match
            if (kMismatches < threshold) {
                totalMatches += 1;
                currentString += 1;
                //if the whole string has been found
                if (i == query.getPattern().size() - 1) {
                    found += 1;
                    longestString = Math.max(longestString, currentString);
                    currentString = 0;
                    i = prefix.get(i - 1);
                } else {
                    //if only part of the string has been found
                    i++;
                    j++;
                }
            } else {
                longestString = Math.max(longestString, currentString) / ((double) kMismatches/10.0);
                kMismatches = 0;
                if (i != 0) {
                    i = prefix.get(i - 1);
                    currentString = i;
                } else {
                    j++;
                    currentString = 0;
                }
            }
        }
        return (double) totalMatches/10.0 / (double) search.getPattern().size() + (double) longestString / (double) query.getPattern().size() + (double) found;
    }
}
