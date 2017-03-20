package com.ibotta;

import com.ibotta.config.AnagramConfig;
import com.ibotta.dao.Dictionary;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;

import java.io.IOException;

@Configuration
@PropertySource("classpath:/application.yml")
@EnableAutoConfiguration
public class BeanLoader extends WsConfigurerAdapter { //implements WebApplicationInitializer {

    public BeanLoader() { }


    /**
     * Initializes Dictionary bean to inject in Autowired classes (Endpoint in this case)
     * @return The loaded dictionary
     * @throws IOException If the dictionary could not be loaded from the dictionary file
     */
    @Bean
    public Dictionary getDictionary() throws IOException {
        Logger logger = Logger.getLogger(BeanLoader.class);
        logger.info("BeanLoader - Loading dictionary");
        AnagramConfig config = AnagramConfig.instance();
        return new Dictionary(config);
    }

}
