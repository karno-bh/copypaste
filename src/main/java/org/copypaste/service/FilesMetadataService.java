package org.copypaste.service;

import org.copypaste.consts.Global;
import org.copypaste.data.FileSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FilesMetadataService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CheckSumCacheService checkSumCacheService;

    private final long DAY_IN_MS = 1000 * 60 * 24;

    public List<FileSummary> directorySummaries(String directory, boolean withFileHash) {
        Path dir = Paths.get(directory);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, this::filterValidCandidate)) {

            return StreamSupport
                    .stream(stream.spliterator(), false)
                    .map(withFileHash ? this::pathToFileSummaryWithHash : this::pathToFileSummaryWithNoHash)
                    .sorted(this::compareFileSummaries)
                    .collect(Collectors.toList());
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading outgoing directory files", ioe);
        }
    }

    private boolean filterValidCandidate(Path path) throws IOException {
        long now = System.currentTimeMillis();
        if (!path.getFileName().toString().toLowerCase().endsWith(Global.VALID_FILE_EXTENSION)) {
            return false;
        }
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime fileTime = attr.creationTime();
        return fileTime.toMillis() > (now - DAY_IN_MS * 180);
    }

    private FileSummary pathToFileSummaryWithHash(Path path) {
        return pathToFileSummary(path, true);
    }

    private FileSummary pathToFileSummaryWithNoHash(Path path) {
        return pathToFileSummary(path, false);
    }

    private FileSummary pathToFileSummary(Path path, boolean withFileHash) {
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            long creationTime = fileTime.toMillis();
            String name = path.getFileName().toString();
            long size = attr.size();

            // in hope it is really fast... should be in cache!
            String hash = withFileHash ?  checkSumCacheService.getMD5Hash(name) : null;

            return FileSummary.as()
                    .creationTime(creationTime)
                    .name(name)
                    .checkSum(hash)
                    .size(size)
                    .build();
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading outgoing directory files", ioe);
        }
    }



    private int compareFileSummaries(FileSummary left, FileSummary right) {
        return Long.compare(left.getCreationTime(), right.getCreationTime());
    }
}
