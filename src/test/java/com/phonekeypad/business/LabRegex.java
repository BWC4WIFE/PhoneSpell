package com.phonekeypad.business;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by darcio on 9/15/16.
 */
public class LabRegex {


    public static void main(String[] args) {

        Pattern pattern = Pattern.compile("^.*[2-9]{2,}.*$");
        Pattern cleanPhrase = Pattern.compile("[^\\d.|^A-Z.]");

        String m = cleanPhrase.matcher("DATA-6-6-8-9-META-7-ALFA-00").replaceAll("").trim();

        System.out.println(m.equals("DATA6689META7ALFA00"));

        System.out.println(pattern.matcher("DATA-META-ALFA").matches());
        System.out.println(pattern.matcher("DATA-6-META-7-ALFA").matches());
        System.out.println(pattern.matcher("DATA-6-META-7000-ALFA-00").matches());
        System.out.println(pattern.matcher("DATA-667-META-7-ALFA").matches());
        System.out.println(pattern.matcher("DATA-6-6-8-9-META-7-ALFA").matches());
        System.out.println(pattern.matcher("DATA-66345-META-7-ALFA").matches());

    }

}
