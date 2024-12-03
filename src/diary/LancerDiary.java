import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LancerDiary {

    public static void main(String[] args) {

        try {

            if (args.length != 1) {
                System.out.println("Usage: java LancerDiary <port de l'annuaire>");
                System.exit(1);
            } else {
                int portAnnuaire = Integer.parseInt(args[0]);

                Diary diary = new Diary();

                int Un_port = 0;
                ServiceDiary service = (ServiceDiary) UnicastRemoteObject.exportObject(diary, Un_port);

                Registry reg = LocateRegistry.createRegistry(portAnnuaire);
                reg.rebind("hagimule", service);

                System.out.println("L'annuaire a bien démarré sur le port " + portAnnuaire);

            }


        } catch (RemoteException e) {
            System.out.println(e);
        }
    }
}
