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
        System.out.println("Dans Slave");
        ObjectInputStream ois = new ObjectInputStream(fileRequest.getInputStream());
        FileRequestChunk requesterIS = (FileRequestChunk) ois.readObject();
        ois.close();

        byte[] chunkContent = new byte[requesterIS.getEndingByte()-requesterIS.getStartingByte()]; // Pas +1 ?

        try {
            RandomAccessFile file = new RandomAccessFile("../resources/"+requesterIS.getFileName(),"r");
            file.seek(requesterIS.getStartingByte());
            file.readFully(chunkContent);
            file.close();
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier: "+requesterIS.getFileName());
        }
        Socket socket = new Socket("localhost", 8080);
        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(requesterIS);
        socket.close();
    } catch (Exception e) {
        System.out.println("An error has occurred ...");
    }
    }
}