package test;


import com.ibotta.dao.Anagrams;
import com.ibotta.dao.Dictionary;
import com.ibotta.dao.Stats;
import com.ibotta.dao.Words;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;


/**
 * Checks the various methods of the dictionary class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class DictionaryTests {

    Dictionary dictionary;
    Stats stats;

    @Before
    public void before() throws IOException {
        //mock the file load in the Dictionary constructor
       dictionary = TestUtils.mockDictionary();
       stats = new Stats(dictionary);
    }

    @Test
    public void addWord() {
        int aCnt = stats.getAnagramCount();
        int wCnt = stats.getWordCount();

        assertFalse(dictionary.addWord("Dog")); //dog exists in dictionary
        assertTrue(stats.getAnagramCount() == aCnt);
        assertTrue(stats.getWordCount() == wCnt);

        assertTrue(dictionary.addWord("read"));
        assertTrue(dictionary.addWord("dare"));
        assertTrue(dictionary.addWord("dear"));
        aCnt++; //one new anagram
        wCnt = wCnt+3; //3 new words
        assertTrue(stats.getAnagramCount() == aCnt);
        assertTrue(stats.getWordCount() == wCnt);
    }

    @Test
    public void addWords() {
        int aCnt = stats.getAnagramCount();
        String[] newWords = new String[] {"cat", "somerandomword", "someotherrandomword" };
        dictionary.addWords(newWords);
        aCnt = aCnt + 2; //cat already exists should be two new words
        assertTrue(aCnt == stats.getAnagramCount());

        for (String w : newWords) {
            assertTrue(w, dictionary.containsWord(w));
        }
    }

    @Test
    public void addWordsByWordsWrapper() {
        int aCnt = stats.getAnagramCount();
        String[] newWords = new String[] {"cat", "somerandomword", "someotherrandomword" };
        Words words = new Words();
        words.words = newWords;
        dictionary.addWords(words);
        aCnt = aCnt + 2; //cat already exists should be two new words
        assertTrue(aCnt == stats.getAnagramCount());

        for (String w : newWords) {
            assertTrue(w, dictionary.containsWord(w));
        }
    }

    @Test
    public void getAnagrams() {
        Anagrams anagrams = dictionary.getAnagrams("Act");
        List<String> words = anagrams.getWords();
        //assertTrue("act", words.contains("act"));
        assertTrue("cat", words.contains("cat"));
    }

    /*
    void deleteWord(string getWords) - support ws method, deleteWord single dictionary entry
        UT - mock load dictionary with three words
            - deleteWord getWords
            - assert size is 2
            - assert getWords is not in dictionary
    */

    @Test
    public void removeWord() {
        int aCnt = stats.getAnagramCount();
        int wCnt = stats.getWordCount();

        dictionary.deleteWord("wordnotindictionary");
        assertTrue(aCnt == stats.getAnagramCount());
        assertTrue(wCnt == stats.getWordCount());

        dictionary.deleteWord("cat");
        wCnt--;
        assertTrue(aCnt == stats.getAnagramCount());
        assertTrue(wCnt == stats.getWordCount());
        assertFalse(dictionary.containsWord("cat"));
        assertTrue(dictionary.containsWord("act"));
    }

    @Test
    public void removeAnagram() {
        int aCnt = stats.getAnagramCount();
        int wCnt = stats.getWordCount();

        dictionary.deleteAnagrams("wordnotindictionary");
        assertTrue(aCnt == stats.getAnagramCount());
        assertTrue(wCnt == stats.getWordCount());

        dictionary.deleteAnagrams("cat");
        wCnt = wCnt-2;
        aCnt--;
        assertTrue(aCnt == stats.getAnagramCount());
        assertTrue(wCnt == stats.getWordCount());
        assertFalse(dictionary.containsWord("cat"));
        assertFalse(dictionary.containsWord("act"));
    }

    @Test
    public void deleteAll() {
        assertFalse(dictionary.isEmpty());
        dictionary.deleteAll();
        assertTrue(dictionary.isEmpty());
    }

    @Test
    public void anagramGroupsBySize() {
        dictionary.addWords(TestUtils.getReadAnagramList());
        dictionary.addWord("ride");
        dictionary.addWord("dire");
        List<Anagrams> results = dictionary.getAnagramGroupsBySize(3);
        assertEquals(1, results.size());
        for (String w : TestUtils.getReadAnagramList())
            assertTrue(results.get(0).getWords().contains(w));
        results = dictionary.getAnagramGroupsBySize(2);
        assertEquals(3, results.size());
    }

}
