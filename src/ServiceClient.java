import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServiceClient extends Remote {

    List<FileData> listeFichiers() throws RemoteException;

    InetSocketAddress getSocketAddress() throws RemoteException;

}
