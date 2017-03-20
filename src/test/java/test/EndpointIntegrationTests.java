package test;

import com.ibotta.Application;
import com.ibotta.api.Endpoint;
import com.ibotta.dao.Anagrams;
import com.ibotta.dao.Words;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.*;

/**
 * Spins up a web service on localhost and runs integration tests against the endpoint methods
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Application.class, Endpoint.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.port=0"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EndpointIntegrationTests {

    @LocalServerPort
    private int port;

    @Value("${local.management.port}")
    private int mgt;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String host;

    @Before
    public void before() {
        host = "http://localhost:" + this.port + "/";
    }

    @Test
    public void _1endpointReturnsAnagrams() {
        Anagrams anagrams = callWord("read", null);
        List<String> words = anagrams.getWords();
        assertTrue(words.contains("dear"));
        assertTrue(words.contains("dare"));
        assertFalse(words.contains("read"));
    }

    @Test
    public void _2endpointReturnsAnagramsWithLimit1() {
        Anagrams anagrams = callWord("dog", 1);
        assertTrue(anagrams.getWords().size() == 1);
        assertFalse(anagrams.getWords().contains("read"));
    }

    @Test
    public void _3endpointAddsList() {
        String[] words = new String[] {"abc", "cba", "bca"};
        Words body = new Words();
        body.words = words;

        String url = host + "words.json";
        System.out.println(url);
        ResponseEntity<Void> entity = this.testRestTemplate.postForEntity(url, body, Void.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));

        for (String w : words) {
            Anagrams anagrams = callWord(w, null);
            assertTrue(w,anagrams.getWords().size() > 0);
        }
    }

    @Test
    public void _4endpointReturnsGroupsBySize() {
        String url = host + "anagrams/size/5";
        System.out.println("Calling: " + url);
        ResponseEntity<List> entity = this.testRestTemplate.getForEntity(url, List.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(entity.getBody());
        assertTrue(entity.hasBody());
        List<LinkedHashMap> response = entity.getBody();
        assertFalse(response.isEmpty());
        int size = response.size();
        Object[] groups = response.stream()
                                    .flatMap(hm -> hm.values().stream())
                                    .map(a -> (ArrayList)a)
                                    .toArray();

        for (Object a : groups)
            assertTrue(((ArrayList)a).size() >= 5);

        //check that there are more groups of 4+ then 5+
        url = host + "anagrams/size/4";
        entity = this.testRestTemplate.getForEntity(url, List.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(entity.getBody());
        assertTrue(entity.getBody().size() > size);
        response = entity.getBody();
        assertFalse(response.isEmpty());
        groups = response.stream()
                        .flatMap(hm -> hm.values().stream())
                        .map(a -> (ArrayList)a)
                        .toArray();

        for (Object a : groups)
            assertTrue(((ArrayList)a).size() >= 4);
    }

    @Test
    public void _5endpointReturnsStats() {
        String url = host + "stats.json";
        System.out.println("Calling: " + url);
        ResponseEntity<String> entity = this.testRestTemplate.getForEntity(url, String.class);
        then(entity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        System.out.println(entity.getBody());
        String stats = entity.getBody();
        assertNotNull(stats);
        assertTrue(stats.contains("largestGroupsSize"));
        assertTrue(stats.contains("largestGroups"));
        assertTrue(stats.contains("anagramCount"));
        assertTrue(stats.contains("wordCount"));
        assertTrue(stats.contains("min"));
        assertTrue(stats.contains("max"));
        assertTrue(stats.contains("average"));
        assertTrue(stats.contains("median"));
    }


    @Test
    public void _6endpointDeletesWord() throws MalformedURLException {
        Anagrams oldList = callWord("read", null);
        String url = host + "words/read.json";
        System.out.println("Calling: " + url);
        this.testRestTemplate.delete(url);
        //can't assert 200 status code since delete does not return a response
        //then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Anagrams newList = callWord("dear", null);

        for (Object o : oldList.getWords()) {
            String w = o.toString();
            if ("read".equals(w))
                assertFalse(w, newList.getWords().contains(w));
            else
                assertTrue(w, newList.getWords().contains(w));
        }

        newList = callWord("read", null);
        assertTrue(newList.getWords().size() == 0);
    }

    @Test
    public void _7endpointDeletesAnagram() {
        String url = host + "delete/cat.json";
        System.out.println("Calling: " + url);
        ResponseEntity<Anagrams> entity = this.testRestTemplate.getForEntity(url, Anagrams.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.hasBody()).isEqualTo(true);
        assertTrue(entity.getBody().getWords().size() > 0);
        Anagrams newList = callWord("cat", null);
        assertTrue(newList.getWords().size() == 0);
    }


    @Test
    public void _8endpointDeletesAll() {

        Anagrams list1 = callWord("ram", null);
        Anagrams list2 = callWord("ride", null);
        if (list1.getWords().size() == 0 || list2.getWords().size() == 0)
            fail("Empty list(s) found before delete called");

        String url = host + "words.json";
        System.out.println(url);
        this.testRestTemplate.delete(url);
        //can't assert 204 status code since delete does not return a response
        //then(entity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        Anagrams newList = callWord("ram", null);
        assertTrue("ram", newList.getWords().size() == 0);
        newList = callWord("dire", null);
        assertTrue("dire", newList.getWords().size() == 0);
    }

    @Test
    public void _9endpointValidateForAnagrams() {
        String[] words = TestUtils.getReadAnagramList();
        String url = host + "validate.json";
        System.out.println(url);
        ResponseEntity<Boolean> entity = this.testRestTemplate.postForEntity(url, words, Boolean.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.hasBody()).isEqualTo(true);
        assertTrue(entity.getBody());

        words = TestUtils.getRandomWordList();
        entity = this.testRestTemplate.postForEntity(url, words, Boolean.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(entity.hasBody()).isEqualTo(true);
        assertFalse(entity.getBody());
    }


    private Anagrams callWord(@NotNull String word, Integer limit) {
        //String params = "getWords?word=" + word + ((limit != null) ? "&limit=" + limit : "");
        String url = host + "anagrams/" + word + ".json" + ((limit != null) ? "?limit=" + limit : "");
        System.out.println("Calling: " + url);
        ResponseEntity<Anagrams> entity = this.testRestTemplate.getForEntity(url, Anagrams.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(entity.getBody());
        assertTrue(entity.hasBody());
        Anagrams anagrams = entity.getBody();
        return anagrams;
    }

}
