package test;

import com.ibotta.api.Endpoint;
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

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests that call WS endpoint methods directly
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FileUtils.class)
public class EndpointTests {

    Dictionary dictionary;
    Endpoint endpoint;
    Stats stats;

    @Before
    public void before() throws IOException {
        //mock the file load in the Dictionary constructor
        dictionary = TestUtils.mockDictionary();
        endpoint = new Endpoint(dictionary);
        stats = endpoint.getStats().getBody();
    }

    @Test
    public void addWords() throws ValidationException {
        String[] words = new String[] { "somerandomword" ,"someotherrandomword", "cat" };

        int wCnt = stats.getWordCount() + 2;
        int aCnt = stats.getAnagramCount() + 2;

        Words body = new Words();
        body.words = words;
        endpoint.addWords(body);
        assertTrue(stats.getWordCount() == wCnt);
        assertTrue(stats.getAnagramCount() == aCnt);
        for (String w : words)
            assertTrue(w, dictionary.containsWord(w));
    }

    @Test
    public void addNullWordsThrowsValidationException() {
        try {
            Words body = new Words();
            body.words = null; //should be initialized to null anyway but setting it here for science
            endpoint.addWords(body);
        } catch (ValidationException ex) {
            return;
        }
        fail("No exception thrown");
    }

    @Test
    public void getAnagrams() {
        Anagrams anagrams = endpoint.getAnagrams("act", null);
        List<String> words = anagrams.getWords();
        assertTrue(words.size() == 1);
        assertFalse(words.contains("act"));
        assertTrue(words.contains("cat"));
    }

    @Test
    public void getAnagramsWithLimit() {
        Anagrams anagrams = endpoint.getAnagrams("act", 1);
        assertTrue(anagrams.getWords().size() == 1);
    }

    @Test
    public void deleteWord() {
        endpoint.deleteWord("cat");
        assertFalse(dictionary.containsWord("cat"));
    }

    @Test
    public void deleteAll() {
        endpoint.deleteAll();
        assertTrue(dictionary.isEmpty());
    }

    @Test
    public void anagramGroupsBySize() {
        String[] words = new String[] { "ride", "dire" };
        dictionary.addWords(words);
        dictionary.addWords(TestUtils.getReadAnagramList());

        List<Anagrams> results = endpoint.getAnagramGroupsBySize(3);
        assertEquals(1, results.size());
        for (String w : TestUtils.getReadAnagramList())
            assertTrue(results.get(0).getWords().contains(w));
        results = endpoint.getAnagramGroupsBySize(2);
        assertEquals(3, results.size());
    }
}
