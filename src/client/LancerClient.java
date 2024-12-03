import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class LancerClient {

    public static void main(String[] args) {
        try {

            if (args.length != 2) {
                System.out.println("Usage: java LancerClient <ip du serveur> <port de l'annuaire>");
                System.exit(1);
            } else {
                String ipServeur = args[0];
                int portAnnuaire = Integer.parseInt(args[1]);


                Registry reg = LocateRegistry.getRegistry(ipServeur, portAnnuaire);
                ServiceDiary diary = (ServiceDiary) reg.lookup("hagimule");

                Client client = new Client();
                File file = new File("../../resources/test1.txt");
                client.ajouterFichier(file);
                client.ajouterFichier(new File("../../resources/video.mkv"));

                int Un_port = 0;
                ServiceClient service = (ServiceClient) UnicastRemoteObject.exportObject(client, Un_port);

                diary.enregisterClient(client);

                System.out.println("Connexion etablie à l'annuaire");

                FileData fileData = new FileData(file.getName(), Files.size(file.toPath()));
                List<ServiceClient> clientList = diary.telechargerFichier(fileData);

                System.out.println("Téléchargement du fichier : " + fileData.getFilename());
                System.out.println("Disponible chez " + clientList.size() + " clients");


            }

        } catch (RemoteException e) {
            System.out.println(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
