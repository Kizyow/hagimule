import java.io.Serializable;

public class FileRequestChunk implements Serializable {
    String fileName;
    int startingByte;
    int endingByte;

    public FileRequestChunk(String fileName, int startingByte, int endingByte) {
        this.fileName = fileName;
        this.startingByte = startingByte;
        this.endingByte = endingByte;
    }

    public String getFileName() {
        return fileName;
    }

    public int getStartingByte() {
        return startingByte;
    }

    public int getEndingByte() {
        return endingByte;
    }

    
}