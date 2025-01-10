import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Random;

public class LancerClient {


    public static void main(String[] args) {
        Random random = new Random();

        try {

            if (args.length != 2) {
                System.out.println("Usage: java LancerClient <ip du serveur> <port de l'annuaire>");
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
                }}).start();


                Registry reg = LocateRegistry.getRegistry(ipServeur, portAnnuaire);
                ServiceDiary diary = (ServiceDiary) reg.lookup("hagimule");

                Client client = new Client(new InetSocketAddress("localhost", clientePort));
                File file = new File("../../resources/test1.txt");
                client.ajouterFichier(file);
                client.ajouterFichier(new File("../../resources/video.mkv"));

                int Un_port = 0;
                ServiceClient service = (ServiceClient) UnicastRemoteObject.exportObject(client, Un_port);

                diary.enregisterClient(service);

                System.out.println("Connexion etablie à l'annuaire");

                FileData fileData = new FileData(file.getName(), Files.size(file.toPath()));
                List<ServiceClient> clientList = diary.telechargerFichier(fileData); //Liste clients qui possèdent le fichier

                if (clientList.stream().filter(t -> !t.equals(service)).toList().isEmpty()) {
                    System.out.println("Aucun client ne possède le fichier demandé.");
                    return; //il faut arrêter
                }

                System.out.println("Téléchargement du fichier : " + fileData.getFilename());
                System.out.println("Disponible chez " + clientList.size() + " clients");

                //Objectif : demander fichiers aux clients : découpage : définir début et fin de chaque morceau 
                //Commencer par trois ou quatre clients. Divise le fichier par le nombre de clients disponibles. --> Ou plutôt nombre fixe de chunks peu importe le client

                //Création des fragments de fichier
                String fileName = fileData.getFilename();
                File outputFile = new File("downloaded_" + fileName); 
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {

                    int chunkSize = 1024 * 1024 * 50; //50 mo (taille d'un fragment) Compter en octet ou en byte ??
                    int startingByte = 0; //position du byte de départ (0 pour le premier fragment)
                
                    /////////////////
                    //Partie socket//
                    /////////////////


                    for (ServiceClient clientI : clientList){ //Pacours la liste des clients qui possèdent le fichier

                        try {

                            InetSocketAddress clientAddress = clientI.getSocketAddress();

                            System.out.println("Connexion au client " + clientAddress.getHostName() + ":" + clientAddress.getPort());

                            //Connexion au client par une socket
                            try (Socket socket = new Socket(clientAddress.getHostName(), clientAddress.getPort())) {

                                System.out.println("connecté ??" + socket.isConnected());
                                

                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                System.out.println("OUTPUT STREAM ??" + oos);


                                //Créé et envoit un FileRequestChunk
                                FileRequestChunk requestChunk = new FileRequestChunk(fileName, startingByte, startingByte + chunkSize);
                                oos.writeObject(requestChunk);
                            
                                System.out.println("CHUNK ECRIT" + requestChunk);

                                InputStream is = socket.getInputStream();
                                System.out.println("INPUUUUT STREAM:" + is);
                                ObjectInputStream ois = new ObjectInputStream(is);
                                System.out.println("OBJECT INPUT STRAM" + ois);

                                //Récupère nouveau chunk
                                FileRequestChunk responseChunk = (FileRequestChunk) ois.readObject();

                                //Ecrit le fragment dans le fichier
                                fos.write(responseChunk.getChunk());
                                System.out.println("Reçu et écrit : bytes [" + startingByte + " - " + responseChunk.getEndingByte() + "]");

                                startingByte = responseChunk.getEndingByte(); //Met à jour le starting byte -> prendra le fragments suivant

                                //Problème : qu'est ce qu'il se passe pour le dernier fragment qui fait peut être moins que la taille de chunk fixée ?
                                
                            }


                        } catch (Exception e) {
                            //Prevu de gérer ce qui se passe si n'arrive pas a récupérer un fragment chez un client : doit passer à un autre
                            System.err.println("Échec de connexion avec un client, essaye avec le client suivant...");
                        }
                    }
                    
                    System.out.println("DOWNLOAD FINISH, JAVA TOURNE TJRS CAR SERVEUR QUI ECOUTE");


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
