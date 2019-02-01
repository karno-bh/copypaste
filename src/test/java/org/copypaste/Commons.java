package org.copypaste;

import java.io.*;
import java.util.List;

public class Commons {

    public static final String TEST_TEMP_DIR = "test-data";

    public static void cleanFiles(List<String> files) {
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

    public static void createDummyFiles(List<String> files) {
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
