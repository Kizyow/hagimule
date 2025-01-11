import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

public class Client implements ServiceClient {

    private List<FileData> fileDataList;
    private InetSocketAddress ip;

    public Client(InetSocketAddress ip){
        this.fileDataList = new ArrayList<>();
        this.ip = ip;
    }

        public static File compressFile(String inputFileName, String outputFileName) throws IOException {
        File inputFile = new File(inputFileName);
        File outputFile = new File(outputFileName);

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

    public void ajouterFichier(File file, boolean compress){
        try {

            if(compress){
                File directory = file.getParentFile();
                String gzFileName = file.getPath() + ".gz";

                boolean gzExists = new File(directory, gzFileName).exists();

                if (!gzExists){
                    System.out.println("Compression du fichier en cours... Cela peut prendre un moment");
                    file = compressFile(file.getPath(), gzFileName);
                }
            }

            FileData fileData = new FileData(file.getName(), file.getPath(), Files.size(file.toPath()));
            this.fileDataList.add(fileData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void supprimerFichier(String fileName){
        Optional<FileData> optFD = fileDataList.stream().filter(fd -> fd.getFilename().equals(fileName)).findFirst();
        optFD.ifPresent(fd -> fileDataList.remove(fd));
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
