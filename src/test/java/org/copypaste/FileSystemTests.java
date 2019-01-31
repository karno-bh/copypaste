package org.copypaste;

import org.copypaste.consts.Global;
import org.copypaste.data.FileSummary;
import org.copypaste.service.FilesMetadataService;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FileSystemTests {


    @Test
    public void fileListTest() {

        List<String> dummyFiles = new ArrayList<>();

        for (int i = 9; i >= 0; i--) {
            String fileName = Commons.TEST_TEMP_DIR + "/dummy-file" + i + Global.VALID_FILE_EXTENSION;
            dummyFiles.add(fileName);
        }

        cleanFiles(dummyFiles);

        try {
            createDummyFiles(dummyFiles);
            FilesMetadataService filesMetadataService = new FilesMetadataService();
            List<FileSummary> summaries = filesMetadataService.directorySummaries(Commons.TEST_TEMP_DIR, true);
//            Assert.assertEquals(10, summaries.size());
            List<String> fsSummaries = new ArrayList<>();
            for (FileSummary fileSummary : summaries) {
                fsSummaries.add(Commons.TEST_TEMP_DIR + "/" + fileSummary.getName());
            }
//            System.out.println(summaries);
            Assert.assertEquals(dummyFiles, fsSummaries);
        } finally {
            cleanFiles(dummyFiles);
        }
    }

    private void cleanFiles(List<String> files) {
        files.forEach(fileName -> {
            File f = new File(fileName);
            if (f.isFile()) {
                boolean deleted = f.delete();
                if (!deleted) {
                    throw new RuntimeException("Cannot delete dummy file: " + fileName);
                }
            }
        });
    }

    private void createDummyFiles(List<String> files) {
        files.forEach(fileName -> {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {}
            try (PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
                bw.println("Bam bam bigellow!");
            } catch (IOException e) {
                throw new RuntimeException("Cannot write dummy files", e);
            }
        });
    }


}
