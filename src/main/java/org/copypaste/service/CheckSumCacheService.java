package org.copypaste.service;

import com.twmacinta.util.MD5;
import org.copypaste.consts.Global;
import org.copypaste.data.FileSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The daemon thread {@link Thread#setDaemon(boolean)} which caches MD5 check sums of available by system files in
 * upload directory. The design of the naked thread chosen because of its simplicity [daemon thread guaranties that it
 * will shutdown within process shutdown]. We want to have something that just works. Making it within more abstraction
 * layers makes it more complicated for support.
 *
 * @author Sergey
 */
@Service
public class CheckSumCacheService implements Runnable {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, String> configData;

    private FileMetadataNoHashService fileMetadataNoHashService;

    /**
     * fileName -> Hex MD5 Cache. Map is concurrent because it accessed from different thread. The synchronization is
     * performed by {@link ConcurrentHashMap#computeIfAbsent(java.lang.Object, java.util.function.Function)}.
     */
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @Autowired
    public void setConfigData(Map<String, String> configData) {
        this.configData = configData;
    }

    @Autowired
    public void setFileMetadataNoHashService(FileMetadataNoHashService fileMetadataNoHashService) {
        this.fileMetadataNoHashService = fileMetadataNoHashService;
    }

    @Override
    public void run() {
        // parse must be safe here
        long waitMillis = minutesToMs(Integer.parseInt(configData.get(Global.WAIT_TILL_NEXT_REHASH_MINUTES_KEY)));
        // this is a daemon => no need to stop explicitly
        while (true) {
            try {
                List<FileSummary> fileSummaries = fileMetadataNoHashService.directorySummaries(Global.OUTGOING_DIRECTORY);
                fileSummaries
                        .stream()
                        .map(FileSummary::getName)
                        .forEach(this::getMD5Hash);
            } catch (Throwable t) {
                // no matter what, daemon does not stop!
                log.error("Error while trying to hash files in daemon", t);
            } finally {
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException e) {
                    log.warn("Daemon cache service was interrupted", e);
                }
            }
        }
    }

    private long minutesToMs(int minutes) {
        return (long)minutes * 60L * 1000L;
    }

    /**
     * Look at the {@link ConcurrentHashMap#computeIfAbsent(java.lang.Object, java.util.function.Function)} explanation
     * why it does not need additional synchronizations.
     * @param fileName file name in outgoing directory
     * @return computed MD5 hash of the file
     */
    public String getMD5Hash(String fileName) {
        return cache.computeIfAbsent(fileName, this::onAbsentKey);
    }

    private String onAbsentKey(String fileName) {
        File file = Paths.get(Global.OUTGOING_DIRECTORY, fileName).toFile();
        try {
            log.info("Processing file: {}", file);
            return MD5.asHex(MD5.getHash(file));
        } catch (IOException e) {
            throw new RuntimeException("Cannot hash file " + file);
        }
    }

    public void startMeInBackGround() {
        Thread thread = new Thread(this, "Hasher Daemon");
        thread.setDaemon(true);
        thread.start();
    }
}
