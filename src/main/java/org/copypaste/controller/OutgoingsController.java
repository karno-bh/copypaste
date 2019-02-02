package org.copypaste.controller;

import org.copypaste.consts.Global;
import org.copypaste.data.FileChunk;
import org.copypaste.data.FileSummary;
import org.copypaste.data.Response;
import org.copypaste.service.FileChunkReader;
import org.copypaste.service.FileMetadataHashService;
import org.copypaste.service.FileMetadataNoHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.List;

/**
 * The main server end point set. Due to simplicity of application, all endpoints are concentrated in one file.
 *
 * @author Sergey
 */
@RestController
public class OutgoingsController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private FileMetadataHashService fileMetadataHashService;

    private FileChunkReader fileChunkReader;

    @Autowired
    public void setFileMetadataHashService(FileMetadataHashService fileMetadataHashService) {
        this.fileMetadataHashService = fileMetadataHashService;
    }

    @Autowired
    public void setFileChunkReader(FileChunkReader fileChunkReader) {
        this.fileChunkReader = fileChunkReader;
    }

    @GetMapping("/files")
    public Response<List<FileSummary>> getAllFiles() {
        log.info("All files requested");
        try {
            List<FileSummary> fileSummaries = fileMetadataHashService.directorySummaries(Global.OUTGOING_DIRECTORY);
            return Response.good(fileSummaries).build();
        } catch (Exception e) {
            log.error("Error while getting all files", e);
            return Response.bad().exception(e.getMessage()).build();
        }
    }

    @GetMapping("/chunk")
    public Response<FileChunk> fileChunk(
            @RequestParam String file,
            @RequestParam long chunkNum) {
        log.info("File chunk requested");
        try {
            return Response.good(fileChunkReader.readChunk(Paths.get(Global.OUTGOING_DIRECTORY, file), chunkNum)).build();
        } catch (Exception e) {
            log.error("Error while getting chunk", e);
            return Response.bad().exception(e.getMessage()).build();
        }
    }



}
