import java.io.Serializable;

public class FileRequestChunk implements Serializable {
    String fileName;
    int startingByte;
    int endingByte;
    byte[] chunk;

    public FileRequestChunk(String fileName, int startingByte, int endingByte) {
        this.fileName = fileName;
        this.startingByte = startingByte;
        this.endingByte = endingByte;
        chunk = new byte[endingByte-startingByte];
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

    public byte[] getChunk() {
        return chunk;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }
}