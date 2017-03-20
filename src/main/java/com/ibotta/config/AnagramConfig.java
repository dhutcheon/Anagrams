package com.ibotta.config;

import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Loads the yml config
 */
public class AnagramConfig {
    private static AnagramConfig anagramConfig;

    public String dictionaryFile;

    public static AnagramConfig instance() throws IOException {
        if (anagramConfig != null)
            return anagramConfig;

        Resource resource = new ClassPathResource("application.yml");
        StringWriter writer = new StringWriter();
        IOUtils.copy(resource.getInputStream(), writer, "UTF-8");
        String str = writer.toString();
        YamlReader yaml = new YamlReader(str);
        Map map = (Map)yaml.read();
        anagramConfig = new AnagramConfig();
        anagramConfig.dictionaryFile = map.get("dictionaryFile").toString().trim();
        return anagramConfig;
    }


}