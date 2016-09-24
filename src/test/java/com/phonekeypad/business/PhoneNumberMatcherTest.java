package com.phonekeypad.business;

import com.phonekeypad.console.ConsoleClient;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

/**
 * Tests for PhoneNumberMatcher
 */
public class PhoneNumberMatcherTest {
    private PhoneNumberMatcher phoneMatcher;

    /**
     * Dummy Consumer implementation to hold the results
     */
    class ResultHolder implements Consumer<String> {
        List<String> results = new LinkedList<>();

        @Override
        public void accept(String s) {
            this.results.add(s);
        }

        public List<String> getResults() {
            return this.results;
        }
    }

    /**
     * Setup the phone number matcher
     */
    @Before
    public void setUp() {
        this.phoneMatcher = new PhoneNumberMatcher(ConsoleClient.getDefaultDictionary());
    }

    /**
     * Test CALL-ME
     */
    @Test
    public void testCallMe() {
        PhoneNumberMatcher trie = new PhoneNumberMatcher(this.getClass().getResourceAsStream("/ubuntu_english_dict"));
        ResultHolder rh1 = new ResultHolder();
        trie.matches("225563", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("CALL-ME"));
    }

    /**
     * Ensure that there is no two skiped numbers, that is not 0 or 1
     */
    @Test
    public void testNoSequentialSkip() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("080032820063823420", rh1);

        Pattern sequentialDigitsPattern = Pattern.compile("^.*[2-9]{2,}.*$");
        Pattern cleanPhrasePattern = Pattern.compile("[^\\d.|^A-Z.]");

        Assert.assertTrue(rh1.getResults().size() > 2000);

        for (String phrase : rh1.getResults()) {
            String cleanPhrase = cleanPhrasePattern.matcher(phrase).replaceAll("");
            Matcher m = sequentialDigitsPattern.matcher(cleanPhrase);
            Assert.assertFalse(m.matches());
        }
    }

    /**
     * Test for correct approach for word separation
     */
    @Test
    public void testDATA6META7ALFA() {
        ResultHolder rh1 = new ResultHolder();

        phoneMatcher.matches("63-6-63-7-25", rh1);

        Assert.assertThat(rh1.getResults(), hasItem("MD-6-MD-7-CL"));
        Assert.assertFalse(rh1.getResults().contains("ODOM-3-7-BK"));
        Assert.assertFalse(rh1.getResults().contains("MEN-6-ES-2-5"));
        Assert.assertFalse(rh1.getResults().contains("6-3-ON-DS-AL"));
    }

    /**
     * Test for correct approach for word separation 2
     */
    @Test
    public void testDATAMETAALFA() {
        byte[] strDict = (
                "meta\n" +
                        "alfa\n" +
                        "DATA\n" +
                        "NEVA\n" +
                        "meda\n" +
                        "fala \n" +
                        "").getBytes();

        ByteArrayInputStream dictionary = new ByteArrayInputStream(strDict);
        PhoneNumberMatcher phoneMatcher = new PhoneNumberMatcher(dictionary);

        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("328263822532", rh1);

        Assert.assertThat(rh1.getResults(), hasItem("DATA-META-ALFA"));
    }

    /**
     * Simple phone number results
     */
    @Test
    public void testNoZeroDigit() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("32826382", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("DATA-NEVA"));
    }

    /**
     * Phone number with free call 0800 prefix
     */
    @Test
    public void test0800() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("080032826382", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("0-8-00-DATA-NEVA"));
    }

    /**
     * Simple phone number with 0 in the middle of digits
     */
    @Test
    public void testZeroDigit() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("328206382", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("DATA-0-NEVA"));
    }

    /**
     * Simple phone number with double zeros in the middle of digits
     */
    @Test
    public void testDoubleZeros() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("3282006382", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("DATA-00-NEVA"));
    }

    /**
     * Only zeros and ones digits. Should skip lll
     */
    @Test
    public void testOnlyZerosAndOnes() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("0010001010111010101", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("0010001010111010101"));
    }

    /**
     * Zero digits in the middle and at the end
     */
    @Test
    public void testDoubleZerosAtTheEnd() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("328200638200", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("DATA-00-NEVA-00"));
    }

    /**
     * Zero digits in the middle and at the end
     */
    @Test
    public void testOneZeroAtTheEnd() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("32820063820", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("DATA-00-NEVA-0"));
    }

    /**
     * 0800 prefix and zero digits in the middle and at the end
     */
    @Test
    public void testUSFreeCall1() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("080032820063820", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("0-8-00-DATA-00-NEVA-0"));
    }

    /**
     * 1800 predix and zero digits in the middle and at the end
     */
    @Test
    public void testUSFreeCall2() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("180032820063820", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("1-8-00-DATA-00-NEVA-0"));
    }

    /**
     * Numeric mixed to non numeric chars
     */
    @Test
    public void testDirtyInput() {
        ResultHolder rh1 = new ResultHolder();
        phoneMatcher.matches("ad    f(3g   28)26d  fsjh  ´'  dfdf   38#as2$ +++k", rh1);
        Assert.assertThat(rh1.getResults(), hasItem("DATA-NEVA"));
    }
}