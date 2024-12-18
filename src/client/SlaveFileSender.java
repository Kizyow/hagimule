import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class SlaveFileSender extends Thread {
    Socket fileRequest;

    public SlaveFileSender(Socket fileRequest) {
        this.fileRequest = fileRequest;
    }
    public void run() {
    try {
        // On récupère l'objet dans le socket qui contient les informations sur le morceau de fichier demandé
        ObjectInputStream ois = new ObjectInputStream(fileRequest.getInputStream());
        FileRequestChunk requesterIS = (FileRequestChunk) ois.readObject();
        ois.close();

        // On créer le tableau de byte qui contiendra le morceau de fichier
        byte[] chunkContent = new byte[requesterIS.getEndingByte()-requesterIS.getStartingByte()]; // Pas +1 ?

        try {
            // On utiliser RandomAccessFile pour lire la portion spécifique du fichier
            RandomAccessFile file = new RandomAccessFile("../resources/"+requesterIS.getFileName(),"r");
            file.seek(requesterIS.getStartingByte()); // On se place au début à lire
            file.readFully(chunkContent); // On lit jusqu'à ce qu'on ait remplis le tableau (ou moins si fichier fini d'être lu)
            file.close();
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier: "+requesterIS.getFileName());
        }
        requesterIS.setChunk(chunkContent); // On ajoute le morceau à l'objet de la demande qu'on va retourner
        Socket socket = new Socket("localhost", 8080);
        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(requesterIS); // On renvoie l'objet de la demande avec le morceau dedans
        socket.close();
    } catch (Exception e) {
        System.out.println("Error lors de la génération du morceau de fichier");
    }
    }
}