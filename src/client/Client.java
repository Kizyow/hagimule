import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Client implements ServiceClient {

    private List<FileData> fileDataList;
    private InetSocketAddress ip;

    public Client(InetSocketAddress ip){
        this.fileDataList = new ArrayList<>();
        this.ip = ip;
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
