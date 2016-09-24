package com.phonekeypad.console;

import com.phonekeypad.business.PhoneNumberMatcher;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console interface client
 */
public class ConsoleClient {
    private PhoneNumberMatcher pm;
    private InputStream in;
    private OutputStream out;

    /**
     * Defaul console cliente constructor
     *
     * @param dictionaryFile
     * @param in
     * @param out
     */
    public ConsoleClient(InputStream dictionaryFile, InputStream in, OutputStream out) {
        this.pm = new PhoneNumberMatcher(dictionaryFile);
        this.in = in;
        this.out = out;
    }

    /**
     * Main console application method
     *
     * @param args
     */
    public static void main(String[] args) {
        InputStream inputStream = System.in;
        PrintStream printStream = System.out;
        startConsoleClient(args, inputStream, printStream);
    }

    /**
     * Start console client
     *
     * @param args
     * @param in
     * @param out
     */
    protected static void startConsoleClient(String[] args, InputStream in, OutputStream out) {
        InputStream dictionaryFile = getDictionaryFile(getDictFileParam(args));
        List<InputStream> filesPhoneNumbers = getFilesToProcess(args);

        if (filesPhoneNumbers.isEmpty()) {
            //open as an interactive console application
            ConsoleClient cc = new ConsoleClient(dictionaryFile, in, out);
            cc.startConsole();

        } else {
            //consumes all informed files immediately and finishes
            processFiles(dictionaryFile, filesPhoneNumbers, out);
        }
    }

    /**
     * Start the console application
     */
    protected void startConsole() {
        printChallengeSplash();

        Scanner sc = new Scanner(this.in);
        PrintStream ps = new PrintStream(this.out);

        for (String phone = sc.nextLine(); phone != null; phone = sc.nextLine())
            this.pm.matches(phone, ps::println);
    }

    /**
     * Consume the phone number files making suggestions accordingly to the dictionary file
     *
     * @param dictionaryFile
     * @param filesPhoneNumbers
     * @param out
     */
    private static void processFiles(InputStream dictionaryFile, List<InputStream> filesPhoneNumbers, OutputStream out) {
        PhoneNumberMatcher pm = new PhoneNumberMatcher(dictionaryFile);
        PrintStream ps = new PrintStream(new BufferedOutputStream(out));

        for (InputStream fis : filesPhoneNumbers) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                for (String phone = br.readLine(); phone != null; phone = br.readLine())
                    pm.matches(phone, ps::println);

                fis.close();

            } catch (FileNotFoundException e) {
                throw new ChallengeRuntimeException("Error trying to open the file of phones!", e);
            } catch (IOException e) {
                throw new ChallengeRuntimeException("Error trying to read file line!", e);
            }
        }

        ps.flush();
        ps.close();
    }

    /**
     * Get the phone file input stream
     *
     * @param filePath
     * @return
     * @throws FileNotFoundException
     */
    protected static InputStream getInputStream(String filePath) throws FileNotFoundException {
        InputStream nestedIS = null;

        File f = new File(filePath);
        if (f.exists()) {
            nestedIS = new FileInputStream(f);
        } else {
            nestedIS = ConsoleClient.class.getResourceAsStream(filePath);
            if (nestedIS == null)
                throw new FileNotFoundException(MessageFormat.format("File {0} not found!", filePath));
        }
        return new BufferedInputStream(nestedIS);
    }

    /**
     * @param strDictFile
     * @return
     */
    protected static InputStream getDictionaryFile(String strDictFile) {
        if (strDictFile == null || strDictFile.trim().isEmpty()) {
            return getDefaultDictionary();
        } else {
            try {
                return getInputStream(strDictFile);
            } catch (FileNotFoundException e) {
                System.out.println("Custom dictionary file not found!");
                printUsageSplash();

                throw new IllegalArgumentException("Error trying to load the dictionary file!", e);
            }
        }
    }

    /**
     * Collect all files to process
     *
     * @param args
     * @return
     */
    protected static List<InputStream> getFilesToProcess(String[] args) {
        List<InputStream> fileToProcess = new ArrayList<>();

        for (String arg : args) {

            try {
                InputStream fis = getInputStream(arg);
                fileToProcess.add(fis);
            } catch (Exception e) {
                //Do nothing!
            }
        }
        return fileToProcess;
    }

    /**
     * Get the dictionary file or null
     *
     * @param args
     * @return
     */
    protected static String getDictFileParam(String[] args) {
        String dictionaryFile = null;
        if (args.length > 0) {
            for (String arg : args) {
                arg = arg.trim();
                if ("-d".equals(arg.substring(0, 2))) {

                    String fullDictArg = arg.substring(2);

                    String[] argParts = fullDictArg.split("=");

                    if (argParts.length != 2 || argParts[1].trim().isEmpty()) {
                        printUsageSplash();

                        throw new IllegalArgumentException("Error trying to load the dictionary file!");
                    }

                    dictionaryFile = argParts[1].trim();
                    break;
                }
            }
        }
        return dictionaryFile;
    }

    /**
     * Print splash and usage pattern
     */
    private static void printUsageSplash() {
        printChallengeSplash();
        System.out.println("Usage: java -jar 1-800-CHALLENGE.jar [-d=DICTIONARY_FILE] [PHONE_LIST_1 PHONE_LIST_2 ...]");
    }

    /**
     * Print a defaul Splash file
     */
    private static void printChallengeSplash() {
        InputStream is = ConsoleClient.class.getResourceAsStream("/usageSplash.txt");
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader readerKeyPad = new BufferedReader(reader);

        try {
            for (String line = readerKeyPad.readLine(); line != null; line = readerKeyPad.readLine())
                System.out.println(line);
        } catch (IOException e) {
            System.err.println("Error trying to read splash file! " + e.getMessage());
        }


    }

    /**
     * Load the default SO dictionary file. If no default SO dictionary was found, raise exception!
     */
    public static InputStream getDefaultDictionary() {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream("/ubuntu_english_dict_no_single_letters_shuf");
        } catch (FileNotFoundException e) {
            throw new ChallengeRuntimeException("Error trying to open the default dictionary!", e);
        }

        if (inputStream != null)
            return inputStream;
        else
            throw new ChallengeRuntimeException("Error trying to open the default dictionary! null!");

    }
}
