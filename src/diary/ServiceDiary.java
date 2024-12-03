import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServiceDiary extends Remote {

    void enregisterClient(ServiceClient client) throws RemoteException;

    void retirerClient(ServiceClient client) throws RemoteException;

    List<ServiceClient> telechargerFichier(FileData fileData) throws RemoteException;

    boolean isAlive() throws RemoteException;

}
