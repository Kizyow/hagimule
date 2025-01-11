import java.io.File;
import java.io.FileInputStream;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

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
                    }
                }).start();


                Registry reg = LocateRegistry.getRegistry(ipServeur, portAnnuaire);
                ServiceDiary diary = (ServiceDiary) reg.lookup("hagimule");

                Client client = new Client(new InetSocketAddress("localhost", clientePort));
                client.ajouterFichier(new File("../../resources/test1.txt"));
                client.ajouterFichier(new File("../../resources/video.mkv"));

                int Un_port = 0;
                ServiceClient service = (ServiceClient) UnicastRemoteObject.exportObject(client, Un_port);

                diary.enregisterClient(service);

                System.out.println("Connexion etablie à l'annuaire");

                // PARTIE TELECHARGEMENT

                File dlFile = new File("../../resources/1GB.bin");

                FileData fileData = new FileData(dlFile.getName(), Files.size(dlFile.toPath()));
                List<ServiceClient> clientList = diary.telechargerFichier(fileData); //Liste clients qui possèdent le fichier

                if (clientList.stream().filter(t -> !t.equals(service)).toList().isEmpty()) {
                    System.out.println("Aucun client ne possède le fichier demandé.");
                    return; //il faut arrêter
                }

                System.out.println("Téléchargement du fichier : " + fileData.getFilename());
                System.out.println("Disponible chez " + clientList.size() + " clients");

                //Divise le fichier par le nombre de clients disponibles.

                //Création des fragments de fichier
                String fileName = fileData.getFilename();

                String outputFileGzipName = "downloaded_" + fileName;
                if (!fileName.endsWith(".gz")) {
                    outputFileGzipName = outputFileGzipName + "gz";
                }

                File outputFileGzip = new File(outputFileGzipName);


                try {

                    int chunkSize = (int) fileData.getLength() / clientList.size();
                    int totalSize = (int) fileData.getLength();
    
                    List<Thread> threads = new ArrayList<>();
                    Map<Integer, byte[]> chunkMap = new ConcurrentHashMap<>(); // Dictionnaire pour stocker les fragments, ConcurrentHashMap permet le threading

                    for(int i = 0; i < clientList.size(); i++){

                        final int startingByte = i * chunkSize;
                        final int endingByte = startingByte + chunkSize;
                        final ServiceClient clientI = clientList.get(i);

                        Thread thread = new Thread(() -> {
                            try {

                                InetSocketAddress clientAddress = clientI.getSocketAddress();
                                System.out.println("Connexion au client " + clientAddress.getHostName() + ":" + clientAddress.getPort());

                                //Connexion au client par une socket
                                try (Socket socket = new Socket(clientAddress.getHostName(), clientAddress.getPort())) {
                                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                                    //Créé et envoit un FileRequestChunk
                                    FileRequestChunk requestChunk = new FileRequestChunk(fileName, startingByte, endingByte);
                                    oos.writeObject(requestChunk);
                                
                                    InputStream is = socket.getInputStream();
                                    ObjectInputStream ois = new ObjectInputStream(is);

                                    //Récupère nouveau chunk
                                    FileRequestChunk responseChunk = (FileRequestChunk) ois.readObject();
                                    chunkMap.put(startingByte, responseChunk.getChunk()); // Stocker dans un dicitonnaire

                                    System.out.println("Reçu et enregistré : bytes [" + startingByte + " - " + responseChunk.getEndingByte() + "]");

                                }

                            } catch (Exception e) {
                                System.err.println("Échec de connexion avec un client, essaye avec le client suivant...");
                            }
                        });

                        threads.add(thread);
                        thread.start();

                    }

                    // On attends que tout les fragments soient download
                    for (Thread thread : threads) {
                        thread.join();
                    }
                    System.out.println("Téléchargement des fragments complété.");

                    FileOutputStream fos = new FileOutputStream(outputFileGzip);

                    // On écrit dans le fichier final dans l'ordre des fragments
                    for (int i = 0; i < totalSize; i += chunkSize) {
                        if (chunkMap.containsKey(i)) {
                            fos.write(chunkMap.get(i));
                        }
                    }

                    fos.close();

                    //Enlève l'extension .gz dans le nom du fichier de sortie
                    File outputFile = new File(outputFileGzipName.substring(0, outputFileGzipName.length() - 3));

                    //On décompresse le fichier recomposé
                    FileInputStream compressedFis = new FileInputStream(outputFileGzip);
                    GZIPInputStream gzipInputStream = new GZIPInputStream(compressedFis);
                    FileOutputStream decompressedFos = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                        decompressedFos.write(buffer, 0, bytesRead);
                    }

                    gzipInputStream.close();
                    decompressedFos.close();

                    System.out.println("Écriture du fichier "+ outputFile.getName() +" completée");

                } catch(IOException | InterruptedException e){
                    e.printStackTrace();
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
