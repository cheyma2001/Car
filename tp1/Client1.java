package tp1;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {

    public static void main(String[] args) {
        try {
            // Se connecter au serveur FTP
            Socket socket = new Socket("localhost", 2121);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            try (// Attendre la réponse initiale du serveur
            Scanner scanner = new Scanner(inputStream)) {
                String response = scanner.nextLine();
                System.out.println(response);

                // Envoyer la commande USER
                sendCommand(outputStream, "USER miage");

                // Attendre la réponse du serveur pour la commande USER
                response = scanner.nextLine();
                System.out.println(response);

                // Envoyer la commande PASS
                sendCommand(outputStream, "PASS car");

                // Attendre la réponse du serveur pour la commande PASS
                response = scanner.nextLine();
                System.out.println(response);

                // Vous pouvez ajouter d'autres commandes FTP ici si nécessaire

                // Envoyer la commande QUIT
                sendCommand(outputStream, "QUIT");

                // Attendre la réponse du serveur pour la commande QUIT
                // response = scanner.nextLine();
               // System.out.println(response);
            }
            // Fermer la connexion
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendCommand(OutputStream outputStream, String command) throws IOException {
        outputStream.write((command + "\r\n").getBytes());
    }
}
