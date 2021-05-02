package com.fayazmohamed.telegraph;

import java.util.ArrayList;
import java.util.Arrays;

public class MorseCodeHelper {


    static final ArrayList<Character> letter = new ArrayList<>(Arrays.asList(' ','a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '0'));
    static final ArrayList<String> code = new ArrayList<>(Arrays.asList("/ ", ".- ", "-... ", "-.-. ", "-.. ",  ". ",
            "..-. ", "--. ",  ".... ", ".. ",   ".--- ",
            "-.- ",  ".-.. ", "-- ",   "-. ",   "--- ",
            ".--. ", "--.- ", ".-. ",  "... ",  "- ",
            "..- ",  "...- ", ".-- ",  "-..- ", "-.-- ",
            "--.. ",
            /* Numbers */
            ".---- ","..--- ","...-- ","....- ","..... ","-.... ","--... ","---.. ","----. ","----- ")
            );


    public static ArrayList<String> convertToMorse(String inp){
        ArrayList<String> output = new ArrayList<>();
        if (inp != null){
            char[] charArray = inp.toLowerCase().toCharArray();
            for (char c : charArray){
                int i = letter.indexOf(c);
                if (i != -1 && i < code.size()){
                    output.add(code.get(i));
                }
            }
        }
        return output;
    }

    public static String convertToLetter(ArrayList<String> inp){
        String output = "";
        if (inp != null){
            for (String s : inp){
                int i = code.indexOf(s.toLowerCase());
                if (i != -1 && i < letter.size()){
                    output = output + letter.get(i);
                }
            }
        }
        return output;
    }
}
