package org.copypaste.controller;

import org.copypaste.consts.Global;
import org.copypaste.data.FileSummary;
import org.copypaste.service.FilesMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OutgoingsController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FilesMetadataService filesMetadataService;

    @GetMapping("/files")
    public List<FileSummary> getAllFiles() {
        log.info("All files requested");
        List<FileSummary> fileSummaries = filesMetadataService.directorySummaries(Global.OUTGOING_DIRECTORY);
        return fileSummaries;
    }

}
