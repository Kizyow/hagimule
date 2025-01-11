import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class LancerClient2 {


    public static void main(String[] args) {
        Random random = new Random();

        try {

            if (args.length != 2) {
                System.out.println("Usage: java LancerClient2 <ip du serveur> <port de l'annuaire>");
                System.exit(1);
            } else {
                String ipServeur = args[0];
                int portAnnuaire = Integer.parseInt(args[1]);
                
                int clientePort = 8080 + random.nextInt(1000);

                new Thread(() -> {
                    try {
                        ServerSocket serverFile = new ServerSocket(clientePort);
                        System.out.println("ServerSocket Lancé sur " + clientePort);
                        while (true) {
                            // Randomly choose one of the target servers
                            Socket fileRequest = serverFile.accept();
                            // Create a new Slave to handle this request
                            new SlaveFileSender(fileRequest).start();
                        }
                    } catch (Exception e) {
                        System.out.println("An error has occurred ...");
                    }
                }).start();


                Registry reg = LocateRegistry.getRegistry(ipServeur, portAnnuaire);
                ServiceDiary diary = (ServiceDiary) reg.lookup("hagimule");

                Client client = new Client(new InetSocketAddress("localhost", clientePort));
                File file = new File("../../resources/1GB.bin");
                client.ajouterFichier(file);

                int Un_port = 0;
                ServiceClient service = (ServiceClient) UnicastRemoteObject.exportObject(client, Un_port);

                diary.enregisterClient(service);

                System.out.println("Connexion etablie à l'annuaire, et partage du fichier 1GB.bin...");

            }

        } catch (RemoteException e) {
            System.out.println(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }


    }

}
