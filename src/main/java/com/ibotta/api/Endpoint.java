package com.ibotta.api;

import com.ibotta.dao.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ValidationException;
import java.util.List;

import static com.ibotta.dao.DataUtils.logDictionaryStats;


/**
 * Rest Controller Class, exposes JSON endpoints
 */
@RestController
@RequestMapping("/")
public class Endpoint {

    private final static Logger LOG = Logger.getLogger(Endpoint.class);


    private final Dictionary dictionary;
    private final Stats stats;


    @Autowired
    public Endpoint(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.stats = new Stats(dictionary);
    }

    /**
    * POST /words.json: Takes a JSON array of English-language words and adds them to the corpus (data store).
    *
    * http://localhost:64466/words.json
    */
    @RequestMapping(value = "/words.json", method = RequestMethod.POST)
    public ResponseEntity addWords(@RequestBody Words body)  {
        if (body.words == null)
            throw new ValidationException("Null word list posted in JSON body");
        LOG.info("words.json POST");
        LOG.debug(" - Array size " + body.words.length);
        dictionary.addWords(body);
        logDictionaryStats(LOG, dictionary);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
    * GET /anagrams/:word.json
    * Returns a JSON array of English-language words that are anagrams of the word passed in the URL.
    * This endpoint should support an optional query param that indicates the maximum number of results to return.
    *
    * http://localhost:64367/anagrams/act.json?limit=1
    */
    @RequestMapping(value = "/anagrams/{word}.json", method = RequestMethod.GET)
    public @ResponseBody Anagrams getAnagrams(@PathVariable(required = true) String word, @RequestParam(value = "limit", required = false) Integer limit) {
        LOG.info("/anagrams/" + word + ".json called");
        Anagrams anagrams = dictionary.getAnagrams(word, limit);
        List<String> words = anagrams.getWords();
        LOG.info(" - " + words.size() + " anagrams returned");
        if (LOG.isDebugEnabled())
            words.forEach(w -> LOG.debug(" - " + w));
        return anagrams;
    }

    /**
     * DELETE /words/:word.json: Deletes a single word from the data store.
     *
     * http://localhost:64595/words/read.json
     */
    @RequestMapping(value = "/words/{word}.json", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteWord(@PathVariable String word) {
        LOG.info("/words/" + word + ".json called");
        boolean success = dictionary.deleteWord(word);
        LOG.debug(" - " + word + " deleted: " + success);
        logDictionaryStats(LOG, dictionary);
    }

    /**
     * DELETE /words.json: Deletes all contents of the data store.
     *
     * http://localhost:64595/words.json
     */
    @RequestMapping(value = "/words.json", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity deleteAll() {
        LOG.info("words.json - DELETE");
        dictionary.deleteAll();
        logDictionaryStats(LOG, dictionary);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * DELETE /delete/:word.json: Endpoint to delete a word and all of its anagrams (optional feature)
     *
     * http://localhost:64595/delete/read.json
     */
    @RequestMapping(value = "/delete/{word}.json", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody Anagrams deleteAnagram(@PathVariable String word) {
        LOG.info("/words/" + word + ".json called");
        Anagrams removed = dictionary.deleteAnagrams(word);
        LOG.debug(" - " + word + " deleted: " + removed.getWords().size() + " anagrams");
        logDictionaryStats(LOG, dictionary);
        return removed;
    }

    /**
     * Endpoint to return all anagram groups of size >= x
     *
     * http://localhost:64595/size/5
     */
    @RequestMapping(value = "/anagrams/size/{size}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody List<Anagrams> getAnagramGroupsBySize(@PathVariable int size) {
        LOG.info("/size/" + size + " called");
        List<Anagrams> anagrams = dictionary.getAnagramGroupsBySize(size);
        LOG.debug(anagrams.size() + " groups returned");
        return anagrams;
    }

    /**
     * Endpoint that takes a set of words and returns whether or not they are all anagrams of each other
     *
     * http://localhost:64595/validate.json
     */
    @RequestMapping(value = "/validate.json", method = RequestMethod.POST)
    public @ResponseBody boolean validateAnagrams(@RequestBody String[] words) {
        LOG.info("validate.json POST");
        LOG.debug(" - Array size " + words.length);
        boolean result = DataUtils.validateAnagrams(words);
        LOG.debug("Result: " + result);
        return result;
    }

    /**
     * Endpoint that returns a count of words in the dictionary and min/max/median/average word length
     * Endpoint that identifies words with the most anagrams
     *
     * http://localhost:64595/stats.json
     */
    @RequestMapping(value = "/stats.json", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Stats> getStats() {
        stats.calculate();
        return ResponseEntity.ok(stats);
    }
}
