package com.ibotta.dao;

import com.ibotta.config.AnagramConfig;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data container for the anagram dictionary, extends Observable and can notify observer objects of changes
 */
public class Dictionary extends Observable implements Iterable<Anagrams> {

    private final static Logger LOG = Logger.getLogger(Dictionary.class);
    private final ConcurrentHashMap<String, Anagrams> corpus = new ConcurrentHashMap<>();

    /**
     * Reads the dictionary file passed in by the config and parses the anagrams from the file
     * @param Config containing the path to the dictionary file to load
     * @throws IOException if the file cannot be read
     */
    public Dictionary(@NotNull AnagramConfig config) throws IOException {
        if (config.dictionaryFile == null)
            throw new IllegalArgumentException("dictionaryFile path not specified in config");

        ClassPathResource res = new ClassPathResource(config.dictionaryFile);
        File file = res.getFile();
        LOG.info("Loading dictionary file " + file.getAbsolutePath());
        DateTime start = DateTime.now();
        List<String> dictionary = FileUtils.readLines(file, Charset.defaultCharset());
        for (String word : dictionary)
            addWord(word);
        DateTime end = DateTime.now();
        long span = end.getMillis() - start.getMillis();
        LOG.debug("Dictionary file loaded in " + span + "ms");
        DataUtils.logDictionaryStats(LOG, this);
    }

    /**
     * Add a list of words to the dictionary if they do not already exist
     * @param words the list to add
     */
    public void addWords(@NotNull Words words) { addWords(words.words); }
    public void addWords(@NotNull String[] words) {
        for (String w : words)
            addWord(w);
        setChangedAndNotifyObservers();
    }

    /**
     * Add a single word to the dictionary if it does not already exist
     * @param word the word to add
     * @return true if the word was added, false if the word already exists
     */
    public boolean addWord(String word) {
        if (!DataUtils.validCharacters(word))
            throw new IllegalArgumentException(word + " is not a valid word");
        word = word.toLowerCase();
        String key = DataUtils.createKeyFromWord(word);
        Anagrams anagrams = corpus.get(key);
        if (anagrams == null)
            anagrams = new Anagrams();
        else if (anagrams.getWords().contains(word))
            return false;
        anagrams.getWords().add(word);
        corpus.put(key, anagrams);
        setChangedAndNotifyObservers();
        return true;
    }

    /**
     * Checks whether a word exists in the dictionary
     * @param word the word to check
     * @return true if it exists
     */
    public boolean containsWord(String word) {
        String key = DataUtils.createKeyFromWord(word);
        Anagrams anagrams = corpus.get(key);
        if (anagrams == null)
            return false;
        return anagrams.getWords().contains(word.toLowerCase());
    }

    /**
     * Gets a list of anagrams for a given word
     * @param word the word to search the anagrams dictionary for
     * @param limit limits the number of results returned
     * @return Anagrams wrapper with the list of words, empty list if no matches
     */
    public Anagrams getAnagrams(String word) { return getAnagrams(word, null); }
    public Anagrams getAnagrams(String word, Integer limit) {
        word = word.toLowerCase();
        String key = DataUtils.createKeyFromWord(word);
        List<String> words = corpus.getOrDefault(key, new Anagrams()).getWords();
        //there may be a key for an anagram, but if it wasn't added to the word list return empty anagram wrapper
        if (!words.contains(word))
            return new Anagrams();
        words.remove(word); //remove the word that was searched
        words = (limit != null) ? words.stream().limit(limit).collect(Collectors.toList()) : words;
        Anagrams anagrams = new Anagrams();
        anagrams.setWords(words);
        return anagrams;
    }

    /**
     * Deletes a given word from the dictionary
     * @param word what to delete
     * @return true if the word was found in the dictionary and deleted
     */
    public boolean deleteWord(String word) {
        String key = DataUtils.createKeyFromWord(word);
        Anagrams anagrams = corpus.get(key);
        if (anagrams == null)
            return false;
        word = word.toLowerCase();
        boolean removed = anagrams.getWords().remove(word);
        if (removed)
            setChangedAndNotifyObservers();
        if (anagrams.getWords().isEmpty())
            corpus.remove(key);

        return removed;
    }

    /**
     * Deletes the entire list of anagrams for a given word
     * @param anagram the anagram to delete
     * @return the list of words deleted corresponding to the anagram
     */
    public Anagrams deleteAnagrams(String anagram) {
        String key = DataUtils.createKeyFromWord(anagram);
        Anagrams anagrams = corpus.remove(key);
        if (anagrams == null)
            return new Anagrams();
        setChangedAndNotifyObservers();
        return anagrams;
    }

    /**
     * Gets groupings of anagram lists with size >= the size specified
     * @param size the minimum size of the groups to return
     * @return Anagram word groups with size >= size specified
     */
    public List<Anagrams> getAnagramGroupsBySize(int size) {
        List<Anagrams> anagrams = corpus.values()
                                .parallelStream()
                                .filter(a -> a.getWords().size() >= size)
                                //.flatMap(a -> a.getWords().stream())
                                .collect(Collectors.toList());
        return anagrams;
    }

    /**
     * Don't do it!
     */
    public void deleteAll() {
        corpus.clear();
        setChangedAndNotifyObservers();
    }

    /**
     * Check whether the dictionary has stuff
     * @return true if it doesn't
     */
    public boolean isEmpty() {
        return corpus.isEmpty();
    }

    /**
     * Returns the entire list of anagram groups in the dictionary in Iterator form
     * @return Returns the entire list of anagram groups in the dictionary in Iterator form
     */
    @Override
    public Iterator<Anagrams> iterator() {
        return corpus.values().iterator();
    }

    /**
     * Calls necessary super class methods when the dictionary changes
     */
    private void setChangedAndNotifyObservers() {
        super.setChanged();
        super.notifyObservers();
    }

}
