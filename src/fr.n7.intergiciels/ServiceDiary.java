import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface ServiceDiary extends Remote {

    void enregisterClient(ServiceClient client) throws RemoteException;

    void retirerClient(ServiceClient client) throws RemoteException;

    List<ServiceClient> telechargerFichier(String fileName) throws RemoteException;

    Set<FileData> listeFichiers() throws RemoteException;

    boolean isAlive() throws RemoteException;

}
