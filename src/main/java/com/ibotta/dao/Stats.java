package com.ibotta.dao;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.*;

/**
 * Container for dictionary stats and performs calculations,
 * implements Observer interface so that dictionary object can notify Stats of changes
 */
public class Stats implements Observer {

    final Dictionary dictionary;

    boolean calculated = false;

    int anagramCnt = 0;
    int wordCnt = 0;

    Integer largestGroupsSize = 0;
    List<List<String>> largestGroups = new Vector<>();

    int min = 0;
    int max = 0;
    double median = 0;
    double average = 0;

    /**
     * New instance of Stats class based on the dictionary specified
     * @param dictionary dictionary to calculate
     */
    public Stats(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.dictionary.addObserver(this);
    }

    /**
     * calculates a count of words in the dictionary and min/max/median/average word length
     */
    public void calculate() {
        if (calculated)
            return;

        wordCnt = 0;
        anagramCnt = 0;

        largestGroupsSize = 0;
        largestGroups.clear();

        List<Double> lengths = new Vector<Double>();
        min = -1;
        max = -1;
        median = 0;
        average = 0;

        if (dictionary.isEmpty())
            return;

        int totWordLength = 0;
        for (Iterator<Anagrams> it = dictionary.iterator(); it.hasNext(); ) {
            Anagrams anagrams = it.next();
            anagramCnt++;
            List<String> words = anagrams.getWords();
            if (words == null || words.size() == 0)
                throw new IllegalStateException("Empty word list found");
            wordCnt = wordCnt + words.size();
            if (words.size() > largestGroupsSize) {
                largestGroups.clear();
                largestGroups.add(words);
                largestGroupsSize = words.size();
            } else if (words.size() == largestGroupsSize) {
                largestGroups.add(words);
            }

            for (int i = 0; i < words.size(); i++) {
                String w = words.get(i);
                int length = w.length();
                lengths.add((double) length);
                min = (min == -1) ? length : Math.min(length, min);
                max = (max == -1) ? length : Math.max(length, max);
                totWordLength = totWordLength + length;
            }
        }

        Collections.sort(lengths);
        int mIdx = lengths.size()/2;
        if (lengths.size() % 2 == 0)
            median = (lengths.get(mIdx) + lengths.get(mIdx - 1))/2;
        else
            median = lengths.get(mIdx);
        average = totWordLength / wordCnt;
//        average = lengths.parallelStream()
//                        .mapToDouble(l -> l)
//                        .average()
//                        .getAsDouble();
        calculated = true;
    }

    /**
     * Gets the size of the largest groups
     * @return Gets the size of the largest group(s)
     */
    @JsonGetter
    public Integer getLargestGroupsSize() {
        calculate();
        return largestGroupsSize;
    }

    /**
     * Gets the list of words for the largest group(s) in the dictionary
     * @return Gets the list of words for the largest group(s) in the dictionary
     */
    @JsonGetter
    public List<List<String>> getLargestGroups() {
        calculate();
        return largestGroups;
    }

    /**
     * Count of all words in the dictionary
     * @return Count of all words in the dictionary
     */
    @JsonGetter
    public int getWordCount() {
        calculate();
        return wordCnt;
    }

    /**
     * Count of all anagram groups in the dictionary
     * @return Count of all anagram groups in the dictionary
     */
    @JsonGetter
    public int getAnagramCount() {
        calculate();
        return anagramCnt;
    }

    /**
     * Min word size
     * @return Min word size in the dictionary
     */
    @JsonGetter
    public int getMin() {
        calculate();
        return min;
    }

    /**
     * Max word size
     * @return Max word size in the dictionary
     */
    @JsonGetter
    public int getMax() {
        calculate();
        return max;
    }

    /**
     * Median word size
     * @return Median word size in the dictionary
     */
    @JsonGetter
    public double getMedian() {
        calculate();
        return median;
    }

    /**
     * Avg word size
     * @return Avg word size in the dictionary
     */
    @JsonGetter
    public double getAverage() {
        calculate();
        return average;
    }

    /**
     * Called by the subject (the dictionary) to notify of an update, sets the calculated flag to false so stats are recalculated on next access
     */
    @Override
    public void update(Observable o, Object arg) {
        calculated = false;
    }

    /**
     * Manual garbage collection, remove this object as an observer if it is deleted
     * @throws Throwable
     */
    @Override
    public void finalize() throws Throwable {
        this.dictionary.deleteObserver(this);
        super.finalize();
    }
}
