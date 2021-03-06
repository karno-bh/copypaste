package org.copypaste.data;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Minimal representation for the client to make her understanding what file she can download. In general she should
 * take one with tha nearest time modification. However, in order to the system to support re-downloading the file she
 * can also request one from the list. This is because downloading can fail and since the files are big it may be
 * requested to download the file again, but the newest one will be already updated.
 * @author Sergey
 */
public class FileSummary {

    private final long creationTime;

    private final String name;

    private final long size;

    private final String checkSum;

    public long getCreationTime() {
        return creationTime;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public static Builder as() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSummary that = (FileSummary) o;
        return creationTime == that.creationTime &&
                size == that.size &&
                Objects.equals(name, that.name) &&
                Objects.equals(checkSum, that.checkSum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(creationTime, name, size, checkSum);
    }

    private FileSummary(long creationTime, String name, long size, String checkSum) {
        this.creationTime = creationTime;
        this.name = name;
        this.size = size;
        this.checkSum = checkSum;
    }

    public static class Builder {

        private long creationTime;

        private String name;

        private long size;

        private String checkSum;

        public Builder creationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder name(String name) {
            if (StringUtils.isEmpty(name)) {
                throw new IllegalArgumentException("File name cannot be empty");
            }
            this.name = name;
            return this;
        }

        public Builder size(long size) {
            if (size < 0) {
                throw new IllegalArgumentException("File size should be greater than zero");
            }
            this.size = size;
            return this;
        }

        public Builder checkSum(@Nullable String checkSum) {
            this.checkSum = checkSum;
            return this;
        }

        public Builder fileSummary(FileSummary fileSummary) {
            creationTime(fileSummary.creationTime);
            name(fileSummary.name);
            size(fileSummary.size);
            checkSum(fileSummary.checkSum);
            return this;
        }

        public FileSummary build() {
            return new FileSummary(creationTime, name, size, checkSum);
        }
    }
}
