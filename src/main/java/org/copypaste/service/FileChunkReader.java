package org.copypaste.service;

import com.twmacinta.util.MD5;
import org.copypaste.consts.Global;
import org.copypaste.data.FileChunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Map;

/**
 *
 * The main logic of reading the file chunk. The granularity of chunk is chosen either by user or system default.
 * The design is such in order for client not having a control over the chunk sizes as it is in range downloads.
 * Despite of its simplicity range downloads are more fragile from the point of view DDoS attacks.
 *
 * @author Sergey
 */
@Service
public class FileChunkReader {

    private Map<String, String> configData;

    @Autowired
    public void setConfigData(Map<String, String> configData) {
        this.configData = configData;
    }

    /**
     * Read the file chunk based on some chunk size. Chunk size granularity is injected by config map.
     * Without entering to the deeper details most modern file system has bounded by O(log(n)) in fseek operation.
     * @param path - file to process
     * @param chunkNum
     * @return requested file chunk
     */
    public FileChunk readChunk(Path path, long chunkNum) {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")){
            /*
            NIO2 Introduced some API which maybe is better for machines, but definitely it is overkill for people to
            to understand ByteBuffer flipping...
             */
            long length = raf.length();
            long lastAvailableByteNum = length - 1;
            int chunkSizeInBytes = chunkSizeInBytes();

            long offset = chunkSizeInBytes * chunkNum;
            if (chunkNum < 0 || offset > lastAvailableByteNum) {
                throw new IllegalArgumentException("Invalid chunk requested");
            }
            raf.seek(offset);
            long nextChunkStart = chunkSizeInBytes * (chunkNum + 1);
            boolean hasNextChunk = !(nextChunkStart > lastAvailableByteNum);
            // convert to integer should be safe!
            int buffSize = hasNextChunk ? chunkSizeInBytes : (int)(length - offset);
            byte[] buffer = new byte[buffSize];
            raf.readFully(buffer);
            String b64Encoded = Base64.getEncoder().encodeToString(buffer);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5Hash = digest.digest(buffer);
            String md5Hex = MD5.asHex(md5Hash);
            return FileChunk.as()
                    .chunkData(b64Encoded)
                    .chunkHexMD5(md5Hex)
                    .hasNextChunk(hasNextChunk)
                    .build();
        } catch (IOException ioe) {
            throw new RuntimeException(MessageFormat.format("Cannot read file {0} on chunk {1}", path.toString(), chunkNum), ioe);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot find MD5 digest. Check your JDK");
        }
    }

    private int chunkSizeInBytes() {
        return Integer.parseInt(configData.get(Global.CHUNK_SIZE_KILOBYTES_KEY)) * 1024;
    }
}
