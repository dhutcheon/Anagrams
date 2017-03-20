package test;

import com.ibotta.config.AnagramConfig;
import com.ibotta.dao.Dictionary;
import org.apache.commons.io.FileUtils;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

/**
 * Static Utils class containing common testing methods for mocking
 */
public class TestUtils {


    public static String[] getRandomWordList() {
        return new String[] { "CAT", "Act", "dog", "bird" };
    }

    public static String[] getReadAnagramList() {
        return new String[] { "read", "dear", "dare" };
    }

    public static Dictionary mockDictionary() throws IOException {
        AnagramConfig config = PowerMockito.mock(AnagramConfig.class);
        config.dictionaryFile = "dictionary.txt";
        PowerMockito.mockStatic(FileUtils.class);
        List<String> dictionary = Arrays.asList(TestUtils.getRandomWordList());
        PowerMockito.when(FileUtils.readLines(any(File.class), eq(Charset.defaultCharset()))).thenReturn(dictionary);
        return new Dictionary(config);
    }
}
