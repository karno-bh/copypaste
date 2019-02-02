package org.copypaste.data;

import java.util.Objects;

/**
 * The immutable data holder for the requested file chunk. The idea of this data holder is to implement similar to
 * Java's {@link java.util.Iterator} interface. Client requests the needed chunk till the system will reply that there
 * is no continuation.
 * <br/><br/>
 * Let's assume that I don't know about range file downloads and have never seen it.
 * The full java code is available here: https://stackoverflow.com/questions/28427339/how-to-implement-http-byte-range-requests-in-spring-mvc
 * However, I believe the some web service is better approach since it is much more controllable within Spring Boot
 * [At least it seems so for me...]. As well this solution is not somewhat too far from the range downloads....
 *
 * @author Sergey
 */
public class FileChunk {

    private final boolean hasNextChunk;

    private final String chunkData;

    private final String chunkHexMD5;

    /**
     *
     * @return the indicator whether there is a next chunk with the system chosen chunk granularity
     */
    public boolean isHasNextChunk() {
        return hasNextChunk;
    }

    /**
     * Although it is not obligated by this interface however in order to pass the data within JSON, the data should be
     * somehow coded into String, the most chances the content will be Base64 coded.
     * @return chunk data itself represented as string.
     */
    public String getChunkData() {
        return chunkData;
    }

    /**
     *
     * @return String HEX of MD5 of pure chunk content, i.e. before the content encoded to String
     */
    public String getChunkHexMD5() {
        return chunkHexMD5;
    }

    private FileChunk(String chunkData, String chunkHexMD5, boolean hasNextChunk) {
        this.hasNextChunk = hasNextChunk;
        this.chunkData = chunkData;
        this.chunkHexMD5 = chunkHexMD5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileChunk fileChunk = (FileChunk) o;
        return hasNextChunk == fileChunk.hasNextChunk &&
                Objects.equals(chunkData, fileChunk.chunkData) &&
                Objects.equals(chunkHexMD5, fileChunk.chunkHexMD5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasNextChunk, chunkData, chunkHexMD5);
    }

    public static Builder as() {
        return new Builder();
    }

    public static class Builder {

        private boolean hasNextChunk;

        private String chunkData;

        private String chunkHexMD5;

        public Builder hasNextChunk(boolean hasNextChunk) {
            this.hasNextChunk = hasNextChunk;
            return this;
        }

        public Builder chunkData(String chunkData) {
            this.chunkData = chunkData;
            return this;
        }

        public Builder chunkHexMD5(String chunkHexMD5) {
            this.chunkHexMD5 = chunkHexMD5;
            return this;
        }

        public FileChunk build() {
            return new FileChunk(chunkData, chunkHexMD5, hasNextChunk);
        }
    }
}
