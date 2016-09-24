package com.phonekeypad.console;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Console simulation tests
 */
public class ConsoleManualInputTests {

    /**
     * Elementary test "CALL-ME "
     */
    @Test
    public void consoleCallMeTest() {
        InputStream localDictionary = this.getClass().getResourceAsStream("/ubuntu_english_dict");
        List<String> output = emulateConsole(localDictionary, "225563");
        Assert.assertThat(output, Matchers.hasItem("CALL-ME"));
    }

    /**
     * Limited dictionary not normalized. Only two branches
     */
    @Test
    public void consoleInputTest() {
        byte[] strDict = (
                "meta\n" +
                "alpha\n" +
                "DATA\n" +
                "NEVA\n" +
                "").getBytes();

        ByteArrayInputStream dictionary = new ByteArrayInputStream(strDict);
        List<String> output = emulateConsole(dictionary, "32826382");
        Assert.assertThat(output, Matchers.containsInAnyOrder("DATA-NEVA", "DATA-META"));
    }

    /**
     * All possible combinations for 32826382, five leafs
     */
    @Test
    public void consoleInputTestLocalDictionary() {
        InputStream localDictionary = getLocalTestDictionary();
        List<String> output = emulateConsole(localDictionary, "32826382");

        Assert.assertThat(output, Matchers.containsInAnyOrder("DATA-MET-2","DATA-NET-2","DATA-NEVA","DATA-NEV-2",
                "DATA-OFT-2","DATA-6-DUB","DATA-6-ETA","DATA-6-EVA","DAUB-MET-2","DAUB-NET-2","DAUB-NEVA","DAUB-NEV-2",
                "DAUB-OFT-2","DAUB-6-DUB","DAUB-6-ETA","DAUB-6-EVA","DAVAO-DUB","DAVAO-ETA","DAVAO-EVA","EAT-2-MET-2",
                "EAT-2-NET-2","EAT-2-NEVA","EAT-2-NEV-2","EAT-2-OFT-2","FAT-2-MET-2","FAT-2-NET-2","FAT-2-NEVA","FAT-2-NEV-2",
                "FAT-2-OFT-2","3-AVA-MET-2","3-AVA-NET-2","3-AVA-NEVA","3-AVA-NEV-2","3-AVA-OFT-2",
                "3-AVA-6-DUB","3-AVA-6-ETA","3-AVA-6-EVA","3-CUB-MET-2","3-CUB-NET-2","3-CUB-NEVA","3-CUB-NEV-2","3-CUB-OFT-2",
                "3-CUB-6-DUB","3-CUB-6-ETA","3-CUB-6-EVA"));
    }

    /**
     * Load the local dictionary
     * @return
     */
    private InputStream getLocalTestDictionary() {
        return this.getClass().getResourceAsStream("/darcio_dict");
    }

    /**
     * Mock the console inputstream and outputstrem
     * @param dictionary
     * @param inputs
     * @return
     */
    private List<String> emulateConsole(InputStream dictionary, String... inputs) {
        StringBuffer sb = new StringBuffer();

        for (String input : inputs)
            sb.append(input + "\n");

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ConsoleClient cc = new ConsoleClient(dictionary, bais, baos);
        try {
            cc.startConsole(); //consumes the inputstream and write results to outputstream
        } catch (NoSuchElementException e) {/*It's ok just finish the console*/}


        String strOutputLines = new String(baos.toByteArray());
        String[] outputLines = strOutputLines.split("\n");
        List<String> list = Arrays.asList(outputLines);

        return list;
    }
}
