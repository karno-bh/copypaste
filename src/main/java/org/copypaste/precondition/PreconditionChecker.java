package org.copypaste.precondition;

import org.copypaste.consts.Global;
import org.copypaste.service.CheckSumCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

@Service
@Configuration
public class PreconditionChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PreconditionChecker.class);

    @Autowired
    private CheckSumCacheService checkSumCacheService;

    private final Map<String, Predicate<String>> knownValuesValidator = new HashMap<String, Predicate<String>>() {{
        put(Global.WAIT_TILL_NEXT_REHASH_MINUTES_KEY, PreconditionChecker::validateWait);
    }};

    @Override
    public void run(String... args) throws Exception {
        File outGoingDirectory = new File(Global.OUTGOING_DIRECTORY);

        if (outGoingDirectory.exists() && !outGoingDirectory.isDirectory()) {
            throw new IllegalStateException("Outgoing directory should be a directory");
        }

        if (!outGoingDirectory.exists()) {
            boolean outgoingCreated = outGoingDirectory.mkdir();
            if (!outgoingCreated) {
                throw new IllegalStateException("Cannot create outgoing directory");
            }
        }

        checkSumCacheService.startMeInBackGround();
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
        }

        loadAndMergeProperties(config);

        return Collections.unmodifiableMap(config);
    }


    private Map<String, String> defaultConfiguration() {
        Map<String, String> config = new HashMap<>();
        config.put(Global.WAIT_TILL_NEXT_REHASH_MINUTES_KEY, "5");

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
}
