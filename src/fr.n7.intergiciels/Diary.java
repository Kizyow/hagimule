import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public List<ServiceClient> telechargerFichier(String fileName) throws RemoteException {
        List<ServiceClient> clientFichier = new ArrayList<>();
        new ArrayList<>(clients).forEach(client -> {
            try {
                if(client.listeFichiers().stream().map(fd -> fd.getFilename()).anyMatch(fn -> fn.equals(fileName))){
                    clientFichier.add(client);
                }
            } catch (RemoteException e) {
                System.err.println("Liste de fichier d'un client inaccessible");
                System.err.println("    |-> Ce client a été retiré de l'annuaire, il s'est deconnecté");
                this.clients.remove(client);
            }
        });
        return clientFichier;
    }

    @Override
    public Set<FileData> listeFichiers() throws RemoteException {
        Set<FileData> listFiles = new HashSet<>();
        new ArrayList<>(clients).forEach(client -> {
            try {
                client.listeFichiers().forEach(fD ->listFiles.add(fD));
            } catch (RemoteException e) {
                System.err.println("Liste de fichier d'un client inaccessible");
                System.err.println("    |-> Ce client a été retiré de l'annuaire, il s'est deconnecté");
                this.clients.remove(client);
            }
        });
        return listFiles;
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

}
