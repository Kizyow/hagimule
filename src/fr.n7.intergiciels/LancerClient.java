import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class LancerClient {

    private volatile boolean running = true;
    private final static boolean COMPRESS = true;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java LancerClient <ip annuaire> <port annuaire>");
            System.exit(1);
        }

        Random random = new Random();

        try {

                String ipServeur = args[0];
                int portAnnuaire = Integer.parseInt(args[1]);
                int socketPort = 8080 + random.nextInt(1000);

                LancerClient cli = new LancerClient();

                // PARTIE PARTAGE DE FICHIERS (SERVERSOCKET)
                Thread serverSocketThread = cli.partagerFichiers(socketPort);

                // PARTIE ENREGISTREMENT DES INFOS DU CLIENT DANS L'ANNUAIRE
                InetSocketAddress clientIp = new InetSocketAddress(Inet4Address.getLocalHost().getHostAddress(),socketPort);
                Client client = new Client(clientIp);

                // On se connecte à l'annuaire 
                Registry reg = LocateRegistry.getRegistry(ipServeur, portAnnuaire);
                ServiceDiary diary = (ServiceDiary) reg.lookup("hagimule");

                // On rend notre client actuel accessible dans l'annuaire et donc aux autres clients.
                int Un_port = 0;
                ServiceClient service = (ServiceClient) UnicastRemoteObject.exportObject(client, Un_port);
                diary.enregisterClient(service);

                System.out.println("Connexion établie à l'annuaire");

                // PARTIE IHM en ligne de commande
                try {
                    cli.GUI(diary, client, serverSocketThread);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        } catch (NotBoundException | IOException e) {
            System.err.println("L'annuaire n'est plus disponible, fin du programme");
            System.exit(1);
        }

    }

    public void GUI(ServiceDiary diary, Client client, Thread serverSocketThread) throws RemoteException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while (running){
            System.out.println("\n\n");
            System.out.println("Bonjour et bienvenue dans HagiMule, voici la liste des actions disponibles");
            System.out.println("1 - Ajouter un fichier afin de le partager aux autres clients");
            System.out.println("2 - Supprimer un fichier");
            System.out.println("3 - Liste des fichiers disponible dans l'annuaire");
            System.out.println("4 - Télécharger un fichier");
            System.out.println("5 - Quitter le programme");
            System.out.println("");
            System.out.print("Quel action souhaitez-vous effectuer ? ");
            int numero = Integer.parseInt(scanner.nextLine());

            switch(numero){
                case 1:
                    System.out.print("Quel est le chemin relatif du fichier que vous souhaitez ajouter ? ");
                    String filePathAdd = scanner.nextLine();

                    Path pathAdd = Paths.get(filePathAdd);
                    if(Files.exists(pathAdd) && Files.isRegularFile(pathAdd)){
                        File file = new File(filePathAdd);
                        client.ajouterFichier(file, COMPRESS);
                        System.out.println("Le fichier " + file.getName() + " a bien été ajouté !");
                    } else {
                        System.out.println("Ce fichier n'existe pas, vous devez spécifier un chemin relatif par rapport à LancerClient et ca ne peut pas être un dossier !");
                    }
                    break;
                case 2:
                    System.out.print("Quel est le nom du fichier que vous souhaitez retirer ? ");
                    String fileDel = scanner.nextLine();

                    client.supprimerFichier(fileDel);
                    System.out.println("Le fichier " + fileDel + " a bien été retiré !");

                    break;
                case 3:
                    System.out.println("La liste des fichiers disponibles sont :");
                    diary.listeFichiers().forEach(t -> System.out.println(" - " + t.getFilename()));
                    break;
                case 4:
                    System.out.print("Quel est le nom du fichier que vous souhaitez télécharger ? ");
                    String fileName = scanner.nextLine();
                    
                    try {
                        this.telechargerFichier(fileName, diary, client);
                    } catch (IOException e) {
                        System.err.println("Le fichier demandé n'est pas disponible dans l'annuaire, verifiez la liste des fichiers disponible.");
                    }

                    break;
                case 5:
                    running = false;
                    diary.retirerClient(client);
                    serverSocketThread.interrupt();
                    System.exit(1);
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * Créer un serveur Socket afin de pouvoir accepter les demandes de téléchargements des autres clients
     * Quand un autre client veut télécharger un fichier et qu'on le possède, on lui envoie un fragment
     * @param socketPort Le port a écouter sur le Socket
     */
    public Thread partagerFichiers(int socketPort){
        Thread thread = new Thread(() -> {
            try {
                ServerSocket serverFile = new ServerSocket(socketPort);
                System.out.println("ServerSocket Lancé sur " + socketPort);
                while (running) {
                    // Randomly choose one of the target servers
                    Socket fileRequest = serverFile.accept();
                    // Create a new Slave to handle this request
                    new SlaveFileSender(fileRequest).start();
                }
            } catch (Exception e) {
                System.out.println("An error has occurred ...");
            }
        });
        thread.start();
        return thread;
    }

    /**
     * Permet de demander à télécharger un fichier en demandant aux autres clients
     * S'il y a plusieurs clients ayant le fichier, il sera divisé en fragments afin d'accélérer le download
     * @param fileName Le nom du fichier à télécharger
     * @param diary L'annuaire pour récupérer la liste des clients
     * @param currentClient Notre client actuel
     * @throws RemoteException
     * @throws IOException
     */
    public void telechargerFichier(String fileName, ServiceDiary diary, ServiceClient currentClient) throws RemoteException, IOException {

        System.out.println("Envoi d'une demande de téléchargement d'un fichier...");

        List<ServiceClient> clientList = new ArrayList<>(diary.telechargerFichier(fileName).stream().filter(cl -> !cl.equals(currentClient)).toList()); // Liste clients qui possèdent le fichier
        Optional<FileData> optFileData = diary.listeFichiers().stream().filter(fD -> fD.getFilename().equals(fileName)).findFirst();

        if(!optFileData.isPresent()){
            System.err.println("Le fichier n'est pas présent dans l'annuaire");
            return;
        }

        FileData fileData = optFileData.get();

        if (clientList.isEmpty()) {
            System.out.println("Aucun client ne possède le fichier demandé.");
            return; //il faut arrêter
        }

        System.out.println("Téléchargement du fichier : " + fileData.getFilename());
        System.out.println("Disponible chez " + clientList.size() + " clients");

        //Objectif : demander fichiers aux clients : découpage : définir début et fin de chaque morceau 
        //Commencer par trois ou quatre clients. Divise le fichier par le nombre de clients disponibles.

        String outputFileTempName = "downloaded_" + fileName;
        if (COMPRESS && !fileName.endsWith(".gz")) {
            outputFileTempName = outputFileTempName + ".gz";
        }

        File outputFileTemp = new File(outputFileTempName);

        try {

            int chunkSize = (int) fileData.getLength() / clientList.size();
            int totalSize = (int) fileData.getLength();

            List<Thread> threads = new ArrayList<>();
            Map<Integer, byte[]> chunkMap = new ConcurrentHashMap<>(); // Dictionnaire pour stocker les fragments, ConcurrentHashMap permet le threading

            List<Integer> errorChunk = new ArrayList<>();

            List<ServiceClient> clientListCopy = new ArrayList<>(clientList);
            Instant startTime = Instant.now();

            for(int i = 0; i < clientListCopy.size(); i++){

                final int startingByte = i * chunkSize;
                final int endingByte = startingByte + chunkSize;
                final ServiceClient clientI = clientListCopy.get(i);

                Thread thread = new Thread(() -> {
                    try {
                        this.telechargerFragment(clientI, startingByte, endingByte, fileData.getFilename(), chunkMap);
                    } catch (Exception e) {
                        System.err.println("Échec de connexion avec un client, essaye avec le client suivant...");
                        errorChunk.add(startingByte);
                        clientList.remove(clientI);
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
            Instant finishTime = Instant.now();
            long timeElapsed = Duration.between(startTime, finishTime).toMillis(); 
            System.out.println("Durée du téléchargement : " + timeElapsed + " millisecondes");
            System.out.println("Taille du fichier : " + totalSize + " octets");
            double timeElapsedInSeconds = timeElapsed / 1000.0;
            double totalSizeInBits = totalSize * 8.0;

            double mbps = totalSizeInBits / (timeElapsedInSeconds * 1_000_000);
            System.out.println("Vitesse moyenne de téléchargement : " + mbps + " Mbps");


            if(!errorChunk.isEmpty() && clientList.size() > 0){
                System.out.println("Téléchargements des fragments manquants.");
                // Recuperation des fragments manquant en itératif, on ressaie 1 fois, au dela on considère le download echoué
                // on redemande a l'annuaire qui possede de fichier
                int clientI = 0;
                for(Integer startingByte : new ArrayList<>(errorChunk)){
                    if(clientI >= clientList.size()) clientI = 0;
                    ServiceClient client = clientList.get(clientI++);
                    int endingByte = startingByte + chunkSize;
                    try {
                        telechargerFragment(client, startingByte, endingByte, fileData.getFilename(), chunkMap);
                        errorChunk.remove(startingByte);
                    } catch (Exception e) {
                        System.err.println("Échec de connexion avec un client, essaye avec le client suivant...");
                    }
                }
            }

            if(!errorChunk.isEmpty()){
                System.err.println("Échec de téléchargement du fichier échoué, deux tentatives effectuée sans succès.");
                return;
            }

            FileOutputStream fos = new FileOutputStream(outputFileTemp);

            // On écrit dans le fichier final dans l'ordre des fragments
            for (int i = 0; i < totalSize; i += chunkSize) {
                if (chunkMap.containsKey(i)) {
                    fos.write(chunkMap.get(i));
                }
            }

            fos.close();

            if(COMPRESS){

                //Enlève l'extension .gz dans le nom du fichier de sortie
                File outputFile = new File(outputFileTempName.substring(0, outputFileTempName.length() - 3));

                //On décompresse le fichier recomposé
                FileInputStream compressedFis = new FileInputStream(outputFileTemp);
                GZIPInputStream gzipInputStream = new GZIPInputStream(compressedFis);
                FileOutputStream decompressedFos = new FileOutputStream(outputFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                    decompressedFos.write(buffer, 0, bytesRead);
                }

                gzipInputStream.close();
                decompressedFos.close();
                outputFileTemp.delete();

                System.out.println("Écriture du fichier "+ outputFile.getName() +" completée");

            } else {
                System.out.println("Écriture du fichier "+ outputFileTemp.getName() +" completée");
            }


        } catch(IOException | InterruptedException e){
            e.printStackTrace();
        }

    }

    /**
     * Permet de télécharger un fragment spécifique
     * @param client Le client à qui on doit télécharger le fragment
     * @param startingByte Le début du fragment
     * @param endingByte La fin du fragment
     * @param fileName Le nom du fichier à télécharger
     * @param chunkMap Un dico permettant de sauvegarder le fragment
     * @throws Exception
     */
    public void telechargerFragment(ServiceClient client, int startingByte, int endingByte, String fileName, Map<Integer, byte[]> chunkMap) throws Exception {
        InetSocketAddress clientAddress = client.getSocketAddress();
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
    }

}
