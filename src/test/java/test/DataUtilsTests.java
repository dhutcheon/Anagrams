package test;

import com.ibotta.dao.DataUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Exercises the methods in the DataUtils class
 */
public class DataUtilsTests {

    @Test
    public void validCharacters() {
        assertTrue("dog", DataUtils.validCharacters("dog"));
        assertTrue("CAT", DataUtils.validCharacters("CAT"));
        assertTrue("Jean-Pierre", DataUtils.validCharacters("Jean-Pierre"));
        assertFalse("bird's", DataUtils.validCharacters("bird's"));
        assertFalse("", DataUtils.validCharacters(""));
        assertFalse(" ", DataUtils.validCharacters(" "));
        assertFalse("     ", DataUtils.validCharacters("     "));
        assertFalse("a1phanum3r1c", DataUtils.validCharacters("a1phanum3r1c"));
    }

    @Test
    public void getKey() {
        String key = DataUtils.createKeyFromWord("dog");
        assertEquals("dgo", "dgo", key);

        key = DataUtils.createKeyFromWord("CAT");
        assertEquals("act", "act", key);

        key = DataUtils.createKeyFromWord("Bird");
        assertEquals("bdir", "bdir", key);
    }

    @Test
    public void areAnagrams() {
        assertTrue(DataUtils.validateAnagrams(TestUtils.getReadAnagramList()));
        assertFalse(DataUtils.validateAnagrams(TestUtils.getRandomWordList()));
    }

    @Test
    public void checkKeyErrors() {
        assertException(null);
        assertException("");
    }

    private void assertException(String s) {
        try {
            DataUtils.createKeyFromWord(s);
            Assert.fail("No exception thrown: " + s);
        } catch (Exception ex) { }
    }
}
