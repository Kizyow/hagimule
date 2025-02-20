import java.net.ServerSocket;
import java.net.Socket;

public class DeamonClient {

    public static void main (String[] str) {
        try {
            ServerSocket serverFile = new ServerSocket(8080);
            while (true) {
                // Randomly choose one of the target servers
                Socket fileRequest = serverFile.accept();
                // Create a new Slave to handle this request
                new SlaveFileSender(fileRequest).start();
            }
        } catch (Exception e) {
            System.out.println("An error has occurred ...");
        }
    }
}