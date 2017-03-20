package com.ibotta.dao;

import org.apache.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * Utilities class that supports data objects and endpoint
 */
public class DataUtils {

    /**
     * For a given word, sorts the characters of the word in order
     * @param the word to be converted to a hash map key
     * @return the hashmap key
     */
    public static String createKeyFromWord(@NotNull String word) {
        if (word == "")
            throw new IllegalArgumentException("Word was empty");
        char[] chars = word.toLowerCase().toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * Checks the word for valid characters [A-Z and hypen]
     * @param the word to validate
     * @return true if the word contains valid characters
     */
    public static boolean validCharacters(String word) {
        if (word == null || word == "")
            return false;
        return word.matches("[A-Za-z\\-]+");
    }

    /**
     * checks whether a given array of words are anagrams of each other
     * @param the array of words to check
     * @return true if all words are anagrams
     */
    public static boolean validateAnagrams(@NotNull String[] words) {
        if (words.length < 2)
            return false; //got to have at least 2 words in the list
        String key = null;
        for (String w : words) {
            if (!validCharacters(w))
                throw new IllegalArgumentException(w + " not a valid word");
            if (key == null)
                key = createKeyFromWord(w);
            else if (!key.equals(createKeyFromWord(w)))
                return false;
        }
        return true;
    }

    /**
     * Gets stats from the dictionary object passed to the method and logs them to the specified log
     * @param log The log to use for logging
     * @param dictionary The dictionary to run stats on
     */
    public static void logDictionaryStats(Logger log, Dictionary dictionary) {
        if (!log.isDebugEnabled())
            return;

        Stats stats = new Stats(dictionary);
        log.debug(" - " + stats.getWordCount() + " words in dictionary");
        log.debug(" - " + stats.getAnagramCount() + " anagrams in dictionary");
        log.debug(" - " + stats.getMin() + " min word length");
        log.debug(" - " + stats.getMax() + " max word length");
        log.debug(" - " + stats.getAverage() + " avg word length");
        log.debug(" - " + stats.getMedian() + " median word length");

        dictionary.deleteObserver(stats);
    }
}
