package org.copypaste;

import org.copypaste.consts.Global;
import org.copypaste.data.FileSummary;
import org.copypaste.service.FileMetadataNoHashService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.copypaste.Commons.*;

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
            FileMetadataNoHashService fileMetadataNoHashService = new FileMetadataNoHashService();
            List<FileSummary> summaries = fileMetadataNoHashService.directorySummaries(Commons.TEST_TEMP_DIR);
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




}
