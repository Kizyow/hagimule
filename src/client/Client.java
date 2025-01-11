import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class Client implements ServiceClient {

    private List<FileData> fileDataList;
    private InetSocketAddress ip;

    public Client(InetSocketAddress ip){
        this.fileDataList = new ArrayList<>();
        this.ip = ip;
    }

    //Methods for data compression (should it be in a different class ?)
    public static boolean isCompressed(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("../../resources/"+fileName);
        byte[] header = new byte[2];
        int bytesRead = fileInputStream.read(header);
        fileInputStream.close();

        if (bytesRead == 2) {
            return header[0] == (byte) 0x1F && header[1] == (byte) 0x8B;
        }
        return false;
    }

    public static File compressFile(String inputFileName, String outputFileName) throws IOException {
        File inputFile = new File("../../resources/" + inputFileName);
        File outputFile = new File("../../resources/" + outputFileName);

        FileInputStream fileInputStream = new FileInputStream(inputFile);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            gzipOutputStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        gzipOutputStream.close();

        return outputFile;

    }
    //


    public void ajouterFichier(File file){
        try {
            //if the file is not already compressed in gzip format, we compressed it before adding it to the diary
            if (!isCompressed(file.getName())){
                file = compressFile(file.getName(), file.getName()+".gz");
            }

            FileData fileData = new FileData(file.getName(), Files.size(file.toPath()));
            this.fileDataList.add(fileData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void supprimerFichier(File file){
        // TODO
    }

    public List<FileData> listeFichiers() throws RemoteException {
        return this.fileDataList;
    }

    public InetSocketAddress getSocketAddress() throws RemoteException {
        return ip;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileDataList == null) ? 0 : fileDataList.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Client other = (Client) obj;
        if (fileDataList == null) {
            if (other.fileDataList != null)
                return false;
        } else if (!fileDataList.equals(other.fileDataList))
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        return true;
    }

    


}
