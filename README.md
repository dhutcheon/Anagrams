Anagrams Documentation - Ibotta Dev Project
=========

This is a Java web API built using the Spring framework that allows fast searching and manipulation of an anagrams data store (dictionary).  


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.


## Prerequisites/Caveats

The Anagrams project was created using IntelliJ 2017.1 Public Preview for Mac. Since this was a beta version of IntelliJ there were some bugs discovered in the IDE. Hopefully this should not affect users of other versions of IntelliJ.

All Java dependencies are managed via a maven pom file and should be downloaded automatically.

The Anagrams Project has not been tested in Eclipse or other IDEs. I suspect it should import fine but this documentation is written based off of running the project in IntelliJ 


## Installing

1. Download the Anagrams project from GitHub: https://github.com/dhutcheon/Anagrams
2. Import the project into your IDE of choice
3. Open a terminal window and cd to the project directory root
4. To compile the project type:
```{bash}
$ mvn clean install
```
5. To run the project type:
```{bash}
$ mvn spring-boot:run
```
6. The Anagrams API will run on port 9000, to verify it is listening type:
```{bash}
$ curl -i http://localhost:9000/anagrams/read.json
```

## Running the tests

There are several methods for testing the API:

1. A suite of JUnit tests have been provided in src/main/test/java. No special test config should be necessary, right-click the test class in your IDE and click run. Integration tests are also included as part of the test suite, the integration tests will spin up a local version of the API on a random port and call each of the API methods in the Endpoint class.
2. Ruby tests (included in src/test/ruby) can be run when the API is started from the command line
3. curl, just remember to hit port 9000
4. [Postman](https://www.getpostman.com/), the best API testing tool ever  


## Additional Commentary/Retrospective

### Data Store
I used an in memory thread safe hash map to store the data (ConcurrentHashMap) where the key for the map is the anagram with each character in the anagram sorted in alphabetical order. The value for each key in the map is the list of anagrams. Thread safety is an important consideration as multiple read and writer operations could happen on the dictionary simultaneously through the API. Obviously some form of data persistence rather than an in memory data store would be an important consideration if this were a real world application. Also if the dictionary were to hold millions of words rather than a few thousand, "flattening" the data could be another consideration.       

### Spring Boot
I chose Spring Boot for this project as I believe this provides the best method for running the Anagrams API as a standalone application. I also believe this is the easiest way to get the application working "out of the box" with the least amount of hassle for API developers and testers. 
While the TestRestTemplate class can sometimes be temperamental, I have found it incredibly useful for creating integration tests and properly being able to test and debug endpoint methods.  

### Error Handling
Proper exception checking and error handling is probably a necessary feature to add to this API. Sending the client a descriptive error message in JSON format rather than a 500 http response code is helpful for those developing against the API and any client applications. As of right now the API mainly handles "happy path" types of transactions. Invalid data or a bad call to the endpoint will emit a 500 response without much detail as to the nature of the error.

### YamlBeans
I added support for YAML configuration files when starting the development on Anagrams as it is part of the template I use for creating new API projects. Given that there is only one real configuration field (dictionaryFile), it's safe to say this added some unnecessary complexity for such a small project.

### Edge Cases
A couple of edge cases that I ran into had to do with case sensitivity and special characters. 
 - To hash the key for each anagram list in the dictionary I converted the key to lower case and made searching the anagrams dictionary case-insensitive. All words in the anagrams dictionary are also lower case. This made it difficult to handle proper-nouns (if we can assume that all proper nouns have at least one capital as their first letter). This made it harder to support the optional requirement for excluding proper nouns without some refactoring.
 - I limited the use of special (non-alpha) characters to hyphens only as that is the only punctuation mark that I found in the dictionary file. This got me wondering about what additional special characters (apostrophes for instance) needed to be supported. Additionally, if the anagram API supported multiple languages it might need to include other characters (ñ, umlaut?).    


## Built With

* [Spring Boot](https://projects.spring.io/spring-boot/)
* [PowerMock](http://powermock.github.io/)
* [YamlBeans](https://github.com/EsotericSoftware/yamlbeans)


## Author

[David Hutcheon](https://github.com/dhutcheon/)


