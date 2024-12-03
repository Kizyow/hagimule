import java.io.BufferedReader;
import java.io.InputStream;
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
        InputStream requesterIS = fileRequest.getInputStream();
        OutputStream requesterOS = fileRequest.getOutputStream();


        byte[] buffer = new byte[1024];
        
        int bytesRead = requesterIS.read(buffer);
            if (bytesRead > 0) {
                serverOS.write(buffer, 0, bytesRead);
                serverOS.flush();
            }

        bytesRead = serverIS.read(buffer);
        if (bytesRead > 0) {
            clientOS.write(buffer, 0, bytesRead);
            clientOS.flush();
        };

        socketServer.close();
        clientSocket.close();
    } catch (Exception e) {
        System.out.println("An error has occurred ...");
    }
    }
}
