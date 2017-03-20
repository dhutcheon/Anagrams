package com.ibotta.dao;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.List;
import java.util.Vector;

/**
 * Wrapper for anagram word list returned in JSON response
 */
public class Anagrams {
    private List<String> words = new Vector<>();

    @JsonGetter(value = "anagrams")
    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }
}
