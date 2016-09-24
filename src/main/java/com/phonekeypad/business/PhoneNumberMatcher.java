package com.phonekeypad.business;

import java.io.*;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * The core business class. Load a dictionary and a keypad config. Thread safe class!
 * <p/>
 * Makes all possible matches for a given phone number.
 * <p/>
 * For every match, call the given consumer or lambda function.
 */
public class PhoneNumberMatcher {
    private static final Pattern PHONE_CLEAN_PATTERN = Pattern.compile("[^\\d.]");
    private static final Pattern WORD_CLEAN_PATTERN = Pattern.compile("[\\p{InCombiningDiacriticalMarks}|\'|\\s]");
    private Node root;
    private Map<Character, char[]> keyPad;

    /**
     * Constructor that uses a given dictionary config
     *
     * @param dictionary
     */
    public PhoneNumberMatcher(InputStream dictionary) {
        loadDefaultKeypad();
        loadDictionary(dictionary);
    }

    /**
     * Clean the initial word and start to explore word combinations
     *
     * @param phone
     * @param consumer
     */
    public void matches(String phone, Consumer<String> consumer) {
        String cleanPhone = cleanPhoneNumber(phone);

        if (cleanPhone.isEmpty())
            return;

        this.startWord(cleanPhone, 0, new char[cleanPhone.length() * 2], 0, consumer, true);

    }

    /**
     * Skip all first digits that are no mapped to letters (usually 0 and 1)
     * <p/>
     * At every word starting, control the allowed allowSkip
     * for characters (usually 1 per word).
     * <p/>
     * Control the dash for word separation
     * <p/>
     * Start two processing branches at every word: one allowing
     * to skip one char, and another not allowing to skip any char.
     *
     * @param phone
     * @param digitIndex
     * @param word
     * @param consumer
     * @param allowSkip
     * @return
     */
    protected Boolean startWord(String phone, int digitIndex, char[] word, int letterIndex, Consumer<String> consumer, boolean allowSkip) {

        //check digits to ignore, like 1 and 0
        char[] letters = this.keyPad.get(phone.charAt(digitIndex));
        if (letters == null) {
            letterIndex = putWordSeparation(word, letterIndex);
        }
        while (letters == null && digitIndex < phone.length()) {
            word[letterIndex] = phone.charAt(digitIndex); // give up and set the number itself (like 1 or 0)
            digitIndex++;
            letterIndex++;
            if (digitIndex < phone.length()) {
                letters = this.keyPad.get(phone.charAt(digitIndex)); //take the candidate letters
            } else {
                consumeWord(word, consumer);
                return true;
            }
        }

        //effectively start the word
        return startWordBranches(phone, digitIndex, word, letterIndex, consumer, allowSkip);
    }

    /**
     * Start two possible word branches:
     * - considering skip one letter,
     * - not considering to skip one letter
     *
     * @param phone
     * @param digitIndex
     * @param word
     * @param letterIndex
     * @param consumer
     * @param allowSkip
     * @return
     */
    protected Boolean startWordBranches(String phone, int digitIndex, char[] word, int letterIndex, Consumer<String> consumer, boolean allowSkip) {
        letterIndex = putWordSeparation(word, letterIndex);

        //the first branch without skip
        boolean regularAttempt = matchesLetters(this.root, phone, digitIndex, word, letterIndex, consumer, allowSkip);

        //the second branch, skipping the first char
        if (allowSkip) {
            //write the phone number digit as a letter directly into the work.
            word[letterIndex] = phone.charAt(digitIndex);

            //skip this digit
            digitIndex++;
            letterIndex++;

            if (digitIndex < phone.length()) {
                startWord(phone, digitIndex, word, letterIndex, consumer, false);
            } else {
                //skip the last letter and print the sequence of words
                consumeWord(word, consumer);
            }
        }

        return regularAttempt;
    }

    /**
     * Control different possible branches for every digit.
     * Usually 3 or 4 different branches at every phone digit.
     *
     * @param node
     * @param phone
     * @param digitIndex
     * @param word
     * @param consumer
     * @param skips
     * @return
     */
    protected Boolean matchesLetters(Node node, String phone, int digitIndex, char[] word, int letterIndex, Consumer<String> consumer, boolean skips) {
        char[] letters = this.keyPad.get(phone.charAt(digitIndex)); //take the candidate letters (3 or 4)
        boolean worked = false;

        //if some letters was found
        if (letters != null)
            //OK, some letters was found! Try the combinations
            for (int i = 0; i < letters.length; i++)
                if (matchInternal(node, phone, digitIndex, word, letterIndex, consumer, letters[i], skips))
                    worked = true;

        return worked;
    }

    /**
     * Traverse the trie structure. At the end of the branch, if matches with a possible good
     * word combination, consumes the word (println or something)
     *
     * @param node
     * @param phone
     * @param digitIndex
     * @param word
     * @param consumer
     * @param letter
     * @param skips
     * @return
     */
    protected boolean matchInternal(Node node, String phone, int digitIndex, char[] word, int letterIndex, Consumer<String> consumer, Character letter, boolean skips) {
        if (node == null)
            return false;

        boolean result;

        if (letter < node.c)
            result = matchInternal(node.left, phone, digitIndex, word, letterIndex, consumer, letter, skips);
        else if (letter > node.c)
            result = matchInternal(node.right, phone, digitIndex, word, letterIndex, consumer, letter, skips);
        else {
            word[letterIndex] = node.c;//write the char into the word

            skips=true;//when any char was finally included into the word, i'll allow some skip again

            if (node.finishesAWord) {
                if (digitIndex < phone.length() - 1) {
                    Boolean sameWord = matchesLetters(node.mid, phone, digitIndex + 1, word, letterIndex + 1, consumer, skips);
                    Boolean nextWord = startWord(phone, digitIndex + 1, word, letterIndex + 1, consumer, skips);

                    result = nextWord || sameWord;

                } else {
                    consumeWord(word, consumer);
                    result = true;
                }
            } else {
                if (digitIndex < phone.length() - 1)
                    result = matchesLetters(node.mid, phone, digitIndex + 1, word, letterIndex + 1, consumer, skips);
                else
                    result = false;
            }
        }

        cleanupWord(word, letterIndex);
        return result;
    }

    /**
     * Clean the word and call the consumer callback
     *
     * @param word
     * @param consumer
     */
    protected void consumeWord(char[] word, Consumer<String> consumer) {
        consumer.accept(new String(word).trim());
    }

    /**
     * Put a dash as a word separation
     *
     * @param word
     * @param letterIndex
     * @return
     */
    protected int putWordSeparation(char[] word, int letterIndex) {
        //if is not the first char, separate the next word using a dash char
        if (letterIndex > 0) {
            word[letterIndex] = '-';
            letterIndex++;
        }
        return letterIndex;
    }

    /**
     * @param phone
     * @return
     */
    protected String cleanPhoneNumber(String phone) {
        return PHONE_CLEAN_PATTERN.matcher(phone).replaceAll("").trim();
    }

    /**
     * Clean a word using a space char in the given element range
     *
     * @param word
     * @param f
     * @param t
     */
    protected void cleanupWord(char[] word, int f, int t) {
        for (int i = f; i < t; i++) {
            word[i] = ' ';
        }
    }

    /**
     * Clean a word using a space char in the given element range
     *
     * @param word
     * @param f
     */
    protected void cleanupWord(char[] word, int f) {
        cleanupWord(word, f, word.length);
    }

    /**
     * Put a word into this trie
     *
     * @param key
     */
    protected void put(String key) {
        root = put(root, key, 0);
    }

    /**
     * put a word into this trie
     *
     * @param x
     * @param key
     * @param d
     * @return
     */
    protected Node put(Node x, String key, int d) {
        char c = key.charAt(d);
        if (x == null) {
            x = new Node();
            x.c = c;
        }
        if (c < x.c) x.left = put(x.left, key, d);
        else if (c > x.c) x.right = put(x.right, key, d);
        else if (d < key.length() - 1) x.mid = put(x.mid, key, d + 1);
        else x.finishesAWord = true;
        return x;
    }


    /**
     * Load the default telephone Keymap config
     */
    protected void loadDefaultKeypad() {
        this.keyPad = new HashMap<>();
        this.keyPad.put('2', new char[]{'A', 'B', 'C'});
        this.keyPad.put('3', new char[]{'D', 'E', 'F'});
        this.keyPad.put('4', new char[]{'G', 'H', 'I'});
        this.keyPad.put('5', new char[]{'J', 'K', 'L'});
        this.keyPad.put('6', new char[]{'M', 'N', 'O'});
        this.keyPad.put('7', new char[]{'P', 'Q', 'R', 'S'});
        this.keyPad.put('8', new char[]{'T', 'U', 'V'});
        this.keyPad.put('9', new char[]{'W', 'X', 'Y', 'Z'});
    }

    /**
     * Load the inputStream as a dictionary config
     *
     * @param isDictionary archive containing the dictionary info
     */
    protected void loadDictionary(InputStream isDictionary) {
        try {
            InputStreamReader isr = new InputStreamReader(isDictionary);
            BufferedReader readerKeyPad = new BufferedReader(isr);

            for (String line = readerKeyPad.readLine(); line != null; line = readerKeyPad.readLine()) {
                String normalLine = normalize(line);
                if (!normalLine.isEmpty())
                    put(normalLine);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Error trying to read the dictionary file!", e);
        }
    }


    /**
     * Normalize the parameter string, removing any special char or accentuation, fix to upper case;
     *
     * @param str
     * @return
     */
    protected String normalize(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        return WORD_CLEAN_PATTERN.matcher(str.toUpperCase()).replaceAll("").trim();
    }

    /**
     * Represents the internal Nodes of a trie
     */
    protected class Node {
        char c;
        Node left, mid, right;
        boolean finishesAWord;
    }
}
