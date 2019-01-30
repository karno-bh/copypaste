package org.copypaste.precondition;

import org.copypaste.consts.Global;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PreconditionChecker implements CommandLineRunner {

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
    }
}
