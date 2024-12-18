import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;;;

public class Client implements ServiceClient {

    private List<FileData> fileDataList;

    public Client(){
        this.fileDataList = new ArrayList<>();
    }

    public void ajouterFichier(File file){
        try {
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
        return null;
    }

}
