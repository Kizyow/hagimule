import java.io.Serializable;
import java.util.Objects;

public class FileData implements Serializable {

    private final String filename;
    private final long length;

    public FileData(String filename, long length) {
        this.filename = filename;
        this.length = length;
    }

    public String getFilename() {
        return filename;
    }

    public long getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return length == fileData.length && Objects.equals(filename, fileData.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, length);
    }
}
