package org.copypaste.service;

import org.copypaste.consts.Global;
import org.copypaste.data.FileSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

/**
 *
 * A service that retrieves suitable files for transfer. Used by either file hashing cache service or by server
 * indirectly
 *
 * @author Sergey
 */
@Service
public class FileMetadataNoHashService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final long DAY_IN_MS = 1000 * 60 * 24;

    public List<FileSummary> directorySummaries(String directory) {
        Path dir = Paths.get(directory);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, this::filterValidCandidate)) {

            return StreamSupport
                    .stream(stream.spliterator(), false)
                    .map(this::pathToFileSummary)
                    .sorted(this::compareFileSummaries)
                    .collect(Collectors.toList());
        } catch (IOException ioe) {
            throw new RuntimeException("Error while reading outgoing directory files", ioe);
        }
    }

    private boolean filterValidCandidate(Path path) throws IOException {
        return path.getFileName().toString().toLowerCase().endsWith(Global.VALID_FILE_EXTENSION);
        /*
        // more real example to not allow all files older than some date

        long now = System.currentTimeMillis();
        if (!path.getFileName().toString().toLowerCase().endsWith(Global.VALID_FILE_EXTENSION)) {
            return false;
        }
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime fileTime = attr.creationTime();
        return fileTime.toMillis() > (now - DAY_IN_MS * 180);*/
    }

    private FileSummary pathToFileSummary(Path path) {
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            long creationTime = fileTime.toMillis();
            String name = path.getFileName().toString();
            long size = attr.size();

            return FileSummary.as()
                    .creationTime(creationTime)
                    .name(name)
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
