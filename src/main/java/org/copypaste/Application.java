package org.copypaste;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import org.copypaste.consts.Global;
import org.copypaste.precondition.PreconditionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final Map<String, Predicate<String>> knownValuesValidator = new HashMap<String, Predicate<String>>() {{
        put(Global.WAIT_TILL_NEXT_REHASH_MINUTES_KEY, Application::validateWait);
        put(Global.CHUNK_SIZE_KILOBYTES_KEY, Application::validateChunkSize);
    }};

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Map<String, String> configData() {

        Map<String, String> config = defaultConfiguration();
        File configDir = new File(Global.CONFIG_DIRECTORY);
        if (!configDir.exists()) {
            boolean configCreated = configDir.mkdir();
            if (!configCreated) {
                log.warn("Cannot create config directory.");
                return Collections.unmodifiableMap(config);
            }
            saveConfigAsProps(config);
            return Collections.unmodifiableMap(config);
        } else if (!Paths.get(Global.CONFIG_DIRECTORY, Global.CONFIG_FILE).toFile().exists()) {
            saveConfigAsProps(config);
            return Collections.unmodifiableMap(config);
        }

        loadAndMergeProperties(config);

        return Collections.unmodifiableMap(config);
    }


    private Map<String, String> defaultConfiguration() {
        Map<String, String> config = new HashMap<>();
        config.put(Global.WAIT_TILL_NEXT_REHASH_MINUTES_KEY, "5");
        config.put(Global.CHUNK_SIZE_KILOBYTES_KEY, "64");
        return config;
    }

    private void saveConfigAsProps(Map<String, String> config) {
        Properties props = new Properties();
        props.putAll(config);
        try {
            props.store(new FileOutputStream(Paths.get(Global.CONFIG_DIRECTORY, Global.CONFIG_FILE).toFile()), null);
        } catch (IOException e) {
            log.warn("Cannot save config file", e);
        }
    }

    /**
     * This method will mutate the passed map.
     * @param config
     */
    private void loadAndMergeProperties(Map<String, String> config) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(Paths.get(Global.CONFIG_DIRECTORY, Global.CONFIG_FILE).toFile()));
        } catch (IOException e) {
            log.warn("Cannot load config file", e);
        }
        props.forEach((property, value) -> {
            String propStr = ("" + property).trim();
            String valStr = ("" + value).trim();
            if (validatePropery(propStr, valStr)) {
                config.put(propStr, valStr);
            }
        });
    }

    private boolean validatePropery(String key, String value) {
        Predicate<String> validator = knownValuesValidator.get(key);
        // unknown values should be added to map
        return validator == null || validator.test(value);
    }

    private static boolean validateWait(String value) {
        int minutes = -1;
        try {
            minutes = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            log.warn("Wait in minutes is not correct in configuration");
        }
        // 3 [invalid!] cases: minutes defined as 0, as negative value, cannot parse
        return minutes > 0;
    }

    private static boolean validateChunkSize(String value) {
        int kilobytes;
        try {
            kilobytes = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            log.warn("Chunk size is not correct");
            return false;
        }
        int min = 4, max = 1024;
        if (kilobytes < min || kilobytes > max) {
            log.warn("Chunk size should be defined in range from {} to {} but defined as {}", min, max, kilobytes);
            return false;
        }
        return true;
    }
}
