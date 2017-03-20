package test;

import com.ibotta.config.AnagramConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * tests whether config fields loaded from yml file
 */
public class AnagramConfigTests {

    private AnagramConfig config;

    @Before
    public void before() throws IOException {
        config = AnagramConfig.instance();
    }

    @After
    public void after() {
        config = null;
    }

    @Test
    public void dictionary() {
        assertEquals("dictionary.txt", config.dictionaryFile);
    }
}
