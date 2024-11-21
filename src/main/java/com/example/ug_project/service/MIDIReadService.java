package com.example.ug_project.service;

import com.example.ug_project.model.Header;
import com.example.ug_project.model.MIDIFile;
import com.example.ug_project.model.Track;
import com.example.ug_project.model.TrackEvent;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class MIDIReadService {

    public static Map<String, String> conversion = new HashMap<>() {{
        put("11", "B");
        put("10", "Bb");
        put("9", "A");
        put("8", "Ab");
        put("7", "G");
        put("6", "Gb");
        put("5", "F");
        put("4", "E");
        put("3", "Eb");
        put("2", "D");
        put("1", "Db");
        put("0", "C");

        put("7k", "C");
        put("6k", "G");
        put("5k", "D");
        put("4k", "A");
        put("3k", "E");
        put("2k", "B");
        put("1k", "F");
        put("0k", "C");

        put("B", "11");
        put("Bb", "10");
        put("A", "9");
        put("Ab", "8");
        put("G", "7");
        put("Gb", "6");
        put("F", "5");
        put("E", "4");
        put("Eb", "3");
        put("D", "2");
        put("Db", "1");
        put("C", "0");
    }};

    public static MIDIFile mainMethod(String filePath) throws IOException {
        //checks that the file exists
        MIDIFile midi = readMIDIBinary(filePath);
        assert midi != null;
        //List<String> melody = melodyExtraction(midi);
        return midi;
    }

    public static MIDIFile readMIDIBinary(String filePath) throws IOException {
        File MIDIFile = new File(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(MIDIFile)) {
            //reads the bytes of the MIDI file and converts it to binary
            int byteRead;
            List<String> bytes = new ArrayList<>();
            while ((byteRead = fileInputStream.read()) != -1) {
                //String binByte = String.format("%02X", byteRead);
                bytes.add(String.format("%8s", Integer.toBinaryString(byteRead)).replace(' ', '0'));
                //System.out.println(String.format("%2s", Integer.toHexString(byteRead)).replace(' ', '0'));
            }

            //Check if the file is of type MIDI by looking at the first 4 bytes
            //If the values convert to the ASCII text of "MThd", it means it is a MIDI file
            String fileTypeCheck = Character.toString(Integer.parseInt(bytes.get(0), 2)) + Character.toString(Integer.parseInt(bytes.get(1), 2)) + Character.toString(Integer.parseInt(bytes.get(2), 2)) + Character.toString(Integer.parseInt(bytes.get(3), 2));
            if (fileTypeCheck.equals("MThd")) {
                int headerLength = Integer.parseInt(bytes.get(4) + bytes.get(5) + bytes.get(6) + bytes.get(7), 2);
                int trackType = Integer.parseInt(bytes.get(8) + bytes.get(9), 2);
                int numberOfTracks = Integer.parseInt(bytes.get(10) + bytes.get(11), 2);
                int timing = Integer.parseInt(bytes.get(12) + bytes.get(13), 2);
                Header header = new Header(fileTypeCheck, headerLength, trackType, numberOfTracks, timing);

                //Remove already processed header chunks
                bytes = bytes.subList(14, bytes.size());

                //reads in each track
                List<Track> body = new ArrayList<>();
                for (int i = 0; i < numberOfTracks; i++) {
                    List<Object> tracksBytes = trackRead(bytes);
                    body.add((Track) tracksBytes.get(0));
                    bytes = (List<String>) tracksBytes.get(1);
                }

                MIDIFile midi = new MIDIFile(1, Paths.get(filePath).getFileName().toString(), header, body);
                System.out.println("MIDI file read successfully.");
                return midi;
            } else {
                System.out.println("This is not a MIDI file, please try again.");
            }
            //System.out.println();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static List<Object> trackRead(List<String> bytes) {
        //checks that the current section of the song is a new track
        String trackMarker = Character.toString(Integer.parseInt(bytes.get(0), 2)) + Character.toString(Integer.parseInt(bytes.get(1), 2)) + Character.toString(Integer.parseInt(bytes.get(2), 2)) + Character.toString(Integer.parseInt(bytes.get(3), 2));
        int trackLength = Integer.parseInt(bytes.get(4) + bytes.get(5) + bytes.get(6) + bytes.get(7), 2);
        bytes = bytes.subList(8, bytes.size());

        //reads the events within the track
        List<Object> trackEventsBytes = eventRead(trackMarker, bytes, trackLength);
        Track track = (Track) trackEventsBytes.get(0);
        bytes = (List<String>) trackEventsBytes.get(1);

        return List.of(track, bytes);
    }

    public static List<Object> eventRead(String trackMarker, List<String> bytes, int trackLength) {
        Map<String, String> MIDIEventContentConversion = new HashMap<>() {{
            put("8", "Note-Off");
            put("9", "Note-On");
            put("a", "After-Touch");
            put("b", "Continuous-Controller");
            put("c", "Patch-Change");
            put("d", "Channel-Pressure");
            put("e", "Pitch-Bend");
        }};

        Map<String, String> METAEventContentConversion = new HashMap<>() {{
            put("00", "Sequence-Number");
            put("01", "Text");
            put("02", "Copyright");
            put("03", "Name");
            put("04", "Instrument-Name");
            put("05", "Lyric");
            put("06", "Marker");
            put("07", "Cue-Point");
            put("08", "Program-Name");
            put("09", "Device-Name");
            put("20", "Channel-Prefix");
            put("21", "Port");
            put("2f", "End-Of-Track");
            put("51", "Tempo");
            put("54", "Offset");
            put("58", "Time-Signature");
            put("59", "Key-Signature");
            put("7f", "Sequencer-Specific");
        }};

        List<TrackEvent> events = new ArrayList<>();

        String runningStatus;
        int y = 0;
        while (true) {
            if (bytes.get(y).charAt(0) == '0') {
                runningStatus = Integer.toHexString(Integer.parseInt(bytes.get(y + 1).substring(0, 4), 2)) + Integer.toHexString(Integer.parseInt(bytes.get(y + 1).substring(4, 8), 2));
                break;
            }
            y++;
        }

        int pitchTotal = 0;
        int noteCount = 0;
        int previousDeltaTime =0;
        for (int i = 0; i < trackLength; ) {
            int x = 0;
            StringBuilder deltaTimeString = new StringBuilder();
            //gathers delta time. Continuous process until the first bit of a given byte is 0
            while (true) {
                deltaTimeString.append(bytes.get(x), 1, 8);
                if (bytes.get(x).charAt(0) == '0') {
                    x++;
                    break;
                }
                x++;
            }
            int deltaTime = Integer.parseInt(String.valueOf(deltaTimeString), 2);

            bytes = bytes.subList(x, bytes.size());
            i += x;
            String statusByte = Integer.toHexString(Integer.parseInt(bytes.get(0).substring(0, 4), 2)) + Integer.toHexString(Integer.parseInt(bytes.get(0).substring(4, 8), 2));

            TrackEvent newEvent = new TrackEvent(deltaTime+previousDeltaTime);
            previousDeltaTime += deltaTime;

            String type;
            String message;
            switch (statusByte) {
                case "ff":
                    type = "META";
                    newEvent.setType(type);
                    message = Integer.toHexString(Integer.parseInt(bytes.get(1).substring(0, 4), 2)) + Integer.toHexString(Integer.parseInt(bytes.get(1).substring(4, 8), 2));
                    newEvent.setMessage(METAEventContentConversion.get(message));
                    int length = Integer.parseInt(bytes.get(2), 2);
                    switch (message) {
                        case "00":
                        case "20":
                        case "21":
                        case "51":
                            //number: data1 is a number
                            String number = "";
                            for (int z = 0; z < length; z++) {
                                number += bytes.get(z + 3);
                            }
                            newEvent.setData1(String.valueOf(Integer.parseInt(number, 2)));
                            break;
                        case "54":
                            //time: data1 is hours, minutes, seconds. data2 is frames and fractions of frames
                            String time = String.valueOf(Integer.parseInt(bytes.get(3), 2)) + "/" + String.valueOf(Integer.parseInt(bytes.get(4), 2)) + "/" + String.valueOf(Integer.parseInt(bytes.get(5), 2));
                            String frames = String.valueOf(Integer.parseInt(bytes.get(6), 2)) + "/" + String.valueOf(Integer.parseInt(bytes.get(7), 2));
                            newEvent.setData1(time);
                            newEvent.setData2(frames);
                            break;
                        case "58":
                            //time signature: data1 is numerator and denominator. data2 is clocks between metronome clicks and fractions of notes in a quarter notes
                            String numden = String.valueOf(Integer.parseInt(bytes.get(3), 2)) + "/" + String.valueOf(Math.round(Math.pow(2, Integer.parseInt(bytes.get(4), 2))));
                            String timing = String.valueOf(Integer.parseInt(bytes.get(5), 2)) + "/" + String.valueOf(Integer.parseInt(bytes.get(6), 2));
                            newEvent.setData1(numden);
                            newEvent.setData2(timing);
                            break;
                        case "59":
                            //key signature: data1 is number of flats or sharps. data2 is major or minor
                            String flatsharp = String.valueOf(Integer.parseInt(bytes.get(3), 2));
                            String majmin = String.valueOf(Integer.parseInt(bytes.get(4), 2));
                            if (Integer.parseInt(flatsharp) > 7) {
                                newEvent.setData1(conversion.get(String.valueOf(7 - (256 - Integer.parseInt(flatsharp)) % 12) + "k"));
                            } else {
                                newEvent.setData1(conversion.get(String.valueOf(Integer.parseInt(flatsharp) % 12) + "k"));
                            }
                            if (majmin.equals("0")) {
                                newEvent.setData2("major");
                            } else {
                                newEvent.setData2("minor");
                            }

                            break;
                        case "01":
                        case "02":
                        case "03":
                        case "04":
                        case "05":
                        case "06":
                        case "07":
                        case "08":
                        case "09":
                        case "7f":
                            //text: data1 is text
                            String data1 = "";
                            for (int z = 0; z < length; z++) {
                                data1 += Character.toString(Integer.parseInt(bytes.get(z + 3), 2));
                            }
                            newEvent.setData1(data1);
                            break;
                        case "2f":
                            //end track
                            break;
                    }
                    bytes = bytes.subList(length + 3, bytes.size());
                    i += length + 3;
                    break;

                case "f0":
                case "f7":
                    type = "SYSEX";
                    newEvent.setType(type);
                    String instruction = "";
                    while (!(Integer.toHexString(Integer.parseInt(bytes.get(0).substring(0, 4), 2)) + Integer.toHexString(Integer.parseInt(bytes.get(0).substring(4, 8), 2))).equals("f7")) {
                        instruction += Integer.toHexString(Integer.parseInt(bytes.get(0).substring(0, 4), 2)) + Integer.toHexString(Integer.parseInt(bytes.get(0).substring(4, 8), 2));
                        bytes = bytes.subList(1, bytes.size());
                        i += 1;
                    }
                    newEvent.setMessage(instruction);
                    bytes = bytes.subList(1, bytes.size());
                    i += 1;
                    break;
                default:
                    //checks status byte to see if running status is being used for this track event
                    if (MIDIEventContentConversion.containsKey(String.valueOf(statusByte.charAt(0)))) {
                        bytes = bytes.subList(1, bytes.size());
                        i += 1;
                        runningStatus = statusByte;
                    } else {
                        statusByte = runningStatus;
                    }
                    //gets all info on the MIDI event such as channel number, event type, pitch, etc.
                    type = "MIDI";
                    message = MIDIEventContentConversion.get(String.valueOf(statusByte.charAt(0)));
                    int channelNumber = Integer.parseInt(String.valueOf(statusByte.charAt(1)), 16);
                    if (channelNumber == 10) {
                        continue;
                    }
                    String data1 = String.valueOf(Integer.parseInt(bytes.get(0), 2));
                    bytes = bytes.subList(1, bytes.size());
                    i += 1;

                    switch (String.valueOf(statusByte.charAt(0))) {
                        case "9":
                        case "8":
                        case "a":
                        case "b":
                        case "e":
                            String data2 = Integer.toHexString(Integer.parseInt(bytes.get(0).substring(0, 4), 2)) + Integer.toHexString(Integer.parseInt(bytes.get(0).substring(4, 8), 2));
                            newEvent.setData2(data2);
                            bytes = bytes.subList(1, bytes.size());
                            i += 1;
                        case "c":
                        case "d":
                            newEvent.setType(type);
                            newEvent.setMessage(message);
                            newEvent.setChannelNumber(channelNumber);
                            newEvent.setData1(data1);
                            if (message.equals("Note-On")) {
                                pitchTotal += Integer.parseInt(data1);
                                noteCount += 1;
                            }
                            break;
                    }
                    runningStatus = statusByte;
            }
            events.add(newEvent);
        }
        Track track = new Track(trackMarker, trackLength, noteCount, pitchTotal, events);
        return List.of(track, bytes);
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

}
