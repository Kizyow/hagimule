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

        // On créer le tableau de byte qui contiendra le morceau de fichier
        byte[] chunkContent = new byte[requesterIS.getEndingByte() - requesterIS.getStartingByte()]; // Pas +1 ?
        try {
            // On utiliser RandomAccessFile pour lire la portion spécifique du fichier
            System.out.println("TEST 1");
            RandomAccessFile file = new RandomAccessFile("../../resources/"+requesterIS.getFileName(),"r");
            System.out.println("LECTURE RANDOMACCESSFILE");
            file.seek(requesterIS.getStartingByte()); // On se place au début à lire
            System.out.println("SEEK");

            if (requesterIS.getEndingByte() == (int) file.length()-1){
                chunkContent = new byte[requesterIS.getEndingByte() - requesterIS.getStartingByte() + 1];
            }

            // int taille = (int) file.length();
            // chunkContent = new byte[taille];
            //System.out.println("TAILLLE CHUNKK " + taille);

            file.readFully(chunkContent); // On lit jusqu'à ce qu'on ait remplis le tableau (ou moins si fichier fini d'être lu)
            System.out.println("READ FULLY");
            file.close();
            System.out.println("CLOSE");
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier: "+requesterIS.getFileName());
        }
        requesterIS.setChunk(chunkContent); // On ajoute le morceau à l'objet de la demande qu'on va retourner
        System.out.println("SET CHUNK");
        OutputStream os = fileRequest.getOutputStream();
        System.out.println("GET OUTPUTST");
        ObjectOutputStream oos = new ObjectOutputStream(os);
        System.out.println("SET OOB");
        oos.writeObject(requesterIS); // On renvoie l'objet de la demande avec le morceau dedans
        System.out.println("WRITE CONTENT FILE");
    } catch (Exception e) {
        System.out.println("Error lors de la génération du morceau de fichier");
        e.printStackTrace();
    }
    }
}