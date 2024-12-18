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
                List<ServiceClient> clientList = diary.telechargerFichier(fileData); //Liste clients qui possèdent le fichier

                if (clientList.isEmpty()) {
                    System.out.println("Aucun client ne possède le fichier demandé.");
                    return; //il faut arrêter
                }

                System.out.println("Téléchargement du fichier : " + fileData.getFilename());
                System.out.println("Disponible chez " + clientList.size() + " clients");

                //Objectif : demander fichiers aux clients : découpage : définir début et fin de chaque morceau 
                //Commencer par trois ou quatre clients. Divise le fichier par le nombre de clients disponibles. --> Ou plutôt nombre fixe de chunks peu importe le client

                //Création des fragments de fichier
                File outputFile = new File("downloaded_" + fileName); 
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {

                    int chunkSize = 1024 * 1024 * 50; //50 mo (taille d'un fragment) Compter en octet ou en byte ??
                    int startingByte = 0; //position du byte de départ (0 pour le premier fragment)
                
                    /////////////////
                    //Partie socket//
                    /////////////////

                    while(true){

                        for (ServiceClient client : clientList){ //Pacours la liste des clients qui possèdent le fichier

                            try {

                                String clientAddress = client.getAddress();
                                int clientPort = client.getPort();

                                System.out.println("Connexion au client " + clientAddress + ":" + clientPort);

                                //Connexion au client par une socket
                                try (Socket socket = new Socket(clientAddress, clientPort);
                                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                                    //Créé et envoit un FileRequestChunk
                                    FileRequestChunk requestChunk = new FileRequestChunk(fileName, startingByte, startingByte + chunkSize);
                                    oos.writeObject(requestChunk);
                               

                                    //Récupère nouveau chunk
                                    FileRequestChunk responseChunk = (FileRequestChunk) ois.readObject();

                                    //Ecrit le fragment dans le fichier
                                    fos.write(responseChunk.getChunk());
                                    System.out.println("Reçu et écrit : bytes [" + startingByte + " - " + responseChunk.getEndingByte() + "]");

                                    startingByte = responseChunk.getEndingByte(); //Met à jour le starting byte -> prendra le fragments suivant

                                    //Problème : qu'est ce qu'il se passe pour le dernier fragment qui fait peut être moins que la taille de chunk fixée ?
                                    
                                    break; //On sort pour passer au prochain fragment
                                }

                            } catch (Exception e) {
                                //Prevu de gérer ce qui se passe si n'arrive pas a récupérer un fragment chez un client : doit passer à un autre
                                System.err.println("Échec de connexion avec un client, essaye avec le client suivant...");
                            }
                        }
                    
                    }

                }

                

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
