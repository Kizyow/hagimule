import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileRequest implements Serializable {
    String fileName;
    List<FileRequestChunk> fileChunks = new ArrayList<>();

    public void addChunk(FileRequestChunk chunk) {
        fileChunks.add(chunk);
    }

    public List<FileRequestChunk> getChunks() {
        return fileChunks;
    }    
}

