package org.copypaste.precondition;

import org.copypaste.consts.Global;
import org.copypaste.service.CheckSumCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

@Service
@Configuration
public class PreconditionChecker implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PreconditionChecker.class);

    private CheckSumCacheService checkSumCacheService;

    @Autowired
    public void setCheckSumCacheService(CheckSumCacheService checkSumCacheService) {
        this.checkSumCacheService = checkSumCacheService;
    }

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

}
