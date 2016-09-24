package com.phonekeypad.console;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for batch processing
 */
public class ConsoleFileProcessingTests {

    /**
     * Test for the size of results and unicity
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNotValidDictionaryFlag() {
        String dictionaryFile = "-d=notExistentDictFile";
        String fileToProcess = "/phonesToProcess_unique";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);
        Boolean isUnique = isUniqueResults(list);
        Assert.assertTrue("The results should be unique!", isUnique);
    }

    /**
     * Test for the size of results and unicity
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNotValidDictionaryFlag2() {
        String dictionaryFile = "-d notExistentDictFile";
        String fileToProcess = "/phonesToProcess_unique";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);
        Boolean isUnique = isUniqueResults(list);
        Assert.assertTrue("The results should be unique!", isUnique);
    }

    /**
     * Test for batch processsing
     */
    @Test
    public void testConsoleFileProcessing() {
        String dictionaryFile = "-d=/darcio_dict";
        String fileToProcess = "/phonesToProcess";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);

        Assert.assertThat(list, Matchers.hasItems(
                "DATA-NEVA",
                "DATA-0-NEVA",
                "DATA-00-NEVA",
                "DATA-00-NEVA-00",
                "DATA-00-NEVA-0",
                "0-8-00-DATA-00-NEVA-0",
                "0-8-00-DATA-NEVA",
                "1-8-00-DATA-00-NEVA-0",
                "0010001010111010101"
        ));
    }

    /**
     * Test for the size of results and unicity
     */
    @Test
    public void testUniqueResults() {
        String dictionaryFile = "-d=/darcio_dict";
        String fileToProcess = "/phonesToProcess_unique";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);
        Boolean isUnique = isUniqueResults(list);
        Assert.assertTrue("The results should be unique!", isUnique);
    }

    /**
     * Test for the size of results and unicity
     */
    @Test
    public void testNotUniqueResults() {
        String dictionaryFile = "-d=/darcio_dict";
        String fileToProcess = "/phonesToProcess_notUnique";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);
        Boolean isUnique = isUniqueResults(list);
        Assert.assertFalse("The results should not be unique!", isUnique);
    }

    /**
     * Validate the internal cleaning of old results and trim of results
     */
    @Test
    public void testSameSize() {
        String dictionaryFile = "-d=/ubuntu_english_dict";
        String fileToProcess = "/phonesToProcess_sameSize";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);
        Assert.assertTrue("No enough results to check!", list.size() > 1);

        final String model = list.get(0);
        final String modelClean = model.replaceAll("-", "");

        for (String res : list) {
            final String resClean = res.replaceAll("-", "");
            if (resClean.length() != modelClean.length()) {
                Assert.fail(MessageFormat.format("Strings should have the same size! {0} and {1}", modelClean, resClean));
            }
        }
    }

    /**
     * Validate the internal cleaning of old results and trim of results
     */
    @Test
    public void testNotSameSize() {
        String dictionaryFile = "-d=/ubuntu_english_dict";
        String fileToProcess = "/phonesToProcess_notSameSize";

        List<String> list = callAsBatchConsoleApp(dictionaryFile, fileToProcess);
        Assert.assertTrue("No enough results to check!", list.size() > 10);

        final String model = list.get(0);
        final String modelClean = model.replaceAll("-", "");

        for (String res : list) {
            String resModel = res.replaceAll("-", "");
            if (resModel.length() != modelClean.length()) {
                //avoid false positives
                if (resModel.length() > 4 && modelClean.length() > 4)
                    return;
            }
        }
        Assert.fail("Theses results should not have the same size!");
    }

    /**
     * Test for unicity of results
     * @param list
     * @return
     */
    private Boolean isUniqueResults(List<String> list) {
        Collections.sort(list);
        String last = null;
        Boolean isUnique = true;

        for (String item : list) {
            if (last != null && last.equals(item)) {
                isUnique = false;
                break;
            }
            last = item;
        }
        return isUnique;
    }

    /**
     * Call the batch console application
     *
     * @param dictionaryFile
     * @param fileToProcess
     * @return
     */
    private List<String> callAsBatchConsoleApp(String dictionaryFile, String fileToProcess) {
        String[] args = new String[]{dictionaryFile, fileToProcess};

        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{});
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ConsoleClient.startConsoleClient(args, in, out);

        String strOutputLines = new String(out.toByteArray());
        String[] outputLines = strOutputLines.split("\n");
        return Arrays.asList(outputLines);
    }
}