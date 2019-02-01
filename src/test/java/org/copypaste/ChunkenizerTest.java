package org.copypaste;

import org.copypaste.consts.Global;
import org.copypaste.data.FileChunk;
import org.copypaste.service.FileChunkReader;
import org.junit.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import static org.copypaste.Commons.*;

public class ChunkenizerTest {

    public final static String TEST_FILE = "chunkenizer-test";

    List<String> file;
    char[] chunk1;
    char[] chunk2;

    FileChunkReader fileChunkReader;

    @Before
    public void prerequisites() {
        file = Collections.singletonList(Paths.get(TEST_TEMP_DIR, TEST_FILE).toString());
        chunk1 = new char[4096];
        chunk2 = new char[4096];

        for (int i = 0; i < chunk1.length; i++) {
            chunk1[i] = 'A';
        }
        for (int i = 0; i < chunk2.length; i++) {
            chunk2[i] = 'B';
        }

        try (PrintWriter bw = new PrintWriter(new BufferedWriter(new FileWriter(file.get(0))))) {
            for (char c : chunk1) {
                bw.print(c);
            }
            for (char c : chunk2) {
                bw.print(c);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Error while working with file", ioe);
        }

        fileChunkReader = new FileChunkReader();
        Map<String, String> config = new HashMap<>();
        config.put(Global.CHUNK_SIZE_KILOBYTES_KEY, "4");
        fileChunkReader.setConfigData(config);
    }

    @After
    public void postProcessing() {
        cleanFiles(file);
    }

    @Test
    public void firstChunkTest() throws Exception{
        FileChunk fileChunk = fileChunkReader.readChunk(Paths.get(TEST_TEMP_DIR, TEST_FILE), 0);
        byte[] decode = Base64.getDecoder().decode(fileChunk.getChunkData());
        ArrayList<Byte> decodeArr = new ArrayList<>(decode.length);
        for (byte b : decode) {
            decodeArr.add(b);
        }
        String orig = new String(chunk1);
        byte[] bytes = orig.getBytes("UTF-8");
        ArrayList<Byte> origBytes = new ArrayList<>(bytes.length);
        for (byte b : bytes) {
            origBytes.add(b);
        }

        Assert.assertEquals(origBytes, decodeArr);
    }

    @Test
    public void secondChunkTest() throws Exception{
        FileChunk fileChunk = fileChunkReader.readChunk(Paths.get(TEST_TEMP_DIR, TEST_FILE), 1);
        byte[] decode = Base64.getDecoder().decode(fileChunk.getChunkData());
        ArrayList<Byte> decodeArr = new ArrayList<>(decode.length);
        for (byte b : decode) {
            decodeArr.add(b);
        }
        String orig = new String(chunk2);
        byte[] bytes = orig.getBytes("UTF-8");
        ArrayList<Byte> origBytes = new ArrayList<>(bytes.length);
        for (byte b : bytes) {
            origBytes.add(b);
        }

        Assert.assertEquals(origBytes, decodeArr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void minusChunkTest() {
        FileChunk fileChunk = fileChunkReader.readChunk(Paths.get(TEST_TEMP_DIR, TEST_FILE), -42);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overChunkTest() {
        FileChunk fileChunk = fileChunkReader.readChunk(Paths.get(TEST_TEMP_DIR, TEST_FILE), 42);
    }
}
