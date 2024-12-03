import java.io.BufferedReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SlaveFileSender extends Thread {
    Socket fileRequest;

    public SlaveFileSender(Socket fileRequest) {
        this.fileRequest = fileRequest;
    }
    public void run() {
    try {
        System.out.println("Dans Slave");
        FileRequestChunk requesterIS = (FileRequestChunk) (new ObjectInputStream(fileRequest.getInputStream())).readObject();
        OutputStream requesterOS = fileRequest.getOutputStream();


        byte[] buffer = new byte[1024];
        
        //TODO Continue here.
        /*
        bytesRead = serverIS.read(buffer);
        if (bytesRead > 0) {
            clientOS.write(buffer, 0, bytesRead);
            clientOS.flush();
        };

        socketServer.close();
        clientSocket.close();
        */
    } catch (Exception e) {
        System.out.println("An error has occurred ...");
    }
    }
}
