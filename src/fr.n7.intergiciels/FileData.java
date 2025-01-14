import java.io.Serializable;
import java.util.Objects;

public class FileData implements Serializable {

    private final String filename;
    private final String path;
    private final long length;

    public FileData(String filename, String path, long length) {
        this.filename = filename;
        this.path = path;
        this.length = length;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public long getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return length == fileData.length && Objects.equals(filename, fileData.filename) && Objects.equals(path, fileData.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, path, length);
    }
}
