package org.copypaste.service;

import org.copypaste.data.FileSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class FileMetadataHashService {

    private FileMetadataNoHashService fileMetadataNoHashService;

    private CheckSumCacheService checkSumCacheService;

    @Autowired
    public void setFileMetadataNoHashService(FileMetadataNoHashService fileMetadataNoHashService) {
        this.fileMetadataNoHashService = fileMetadataNoHashService;
    }

    @Autowired
    public void setCheckSumCacheService(CheckSumCacheService checkSumCacheService) {
        this.checkSumCacheService = checkSumCacheService;
    }

    public List<FileSummary> directorySummaries(String directory) {
        List<FileSummary> fileSummariesNoHash = fileMetadataNoHashService.directorySummaries(directory);
        return fileSummariesNoHash
                .stream()
                .map(this::updateFileSummaryWithHash)
                .collect(Collectors.toList());
    }

    private FileSummary updateFileSummaryWithHash(FileSummary fileSummary) {
        return FileSummary.as()
                .fileSummary(fileSummary)
                .checkSum(checkSumCacheService.getMD5Hash(fileSummary.getName()))
                .build();
    }
}
