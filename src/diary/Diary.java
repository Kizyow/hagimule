import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Diary implements ServiceDiary {

    private List<ServiceClient> clients;

    public Diary() {
        this.clients = new ArrayList<>();
    }

    @Override
    public void enregisterClient(ServiceClient client) throws RemoteException {
        this.clients.add(client);
        System.out.println("Un client a été ajouté à l'annuaire");
    }

    @Override
    public void retirerClient(ServiceClient client) throws RemoteException {
        if(this.clients.remove(client)){
            System.out.println("Un client a été retiré de l'annuaire");
        }
    }

    @Override
    public List<ServiceClient> telechargerFichier(FileData fileData) throws RemoteException {
        List<ServiceClient> clientFichier = new ArrayList<>();
        this.clients.forEach(client -> {
            try {
                if(client.listeFichiers().contains(fileData)){
                    clientFichier.add(client);
                }
            } catch (RemoteException e) {
                System.err.println("Liste de fichier d'un client inaccessible");
                throw new RuntimeException(e);
            }
        });
        return clientFichier;
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

}
