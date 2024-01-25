import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

public class FTPServer {
    private static final HashMap<String, String> userCredentials = new HashMap<>();

    static {
        userCredentials.put("user1", "pass1");
        userCredentials.put("user2", "pass2");
    }

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(2020);
            System.out.println("FTP Server is running on port 2020");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream();
             Scanner scanner = new Scanner(inputStream)) {

            String response = "220 Service ready for new user.\r\n";
            outputStream.write(response.getBytes());

            DataSocket dataSocket = null;

            while (true) {
                String command = scanner.next().toUpperCase();

                switch (command) {
                    case "USER":
                        handleUserCommand(scanner, outputStream);
                        break;
                    case "PASS":
                        handlePassCommand(scanner, outputStream);
                        break;
                    case "RETR":
                        handleRetrCommand(scanner, outputStream, dataSocket);
                        break;
                    case "QUIT":
                        handleQuitCommand(outputStream, clientSocket, dataSocket);
                        return;
                    case "PASV":
                        dataSocket = handlePasvCommand(clientSocket, outputStream);
                        break;
                    default:
                        sendResponse(outputStream, "500 Syntax error, command not recognized.\r\n");
                }
            }
        }
    }

    private static DataSocket handlePasvCommand(Socket clientSocket, OutputStream outputStream) throws IOException {
        ServerSocket dataServerSocket = new ServerSocket(0); // Let the system choose an available port
        int port = dataServerSocket.getLocalPort();
        String ipAddress = clientSocket.getInetAddress().getHostAddress().replace('.', ',');
        int p1 = port / 256;
        int p2 = port % 256;

        String response = "227 Entering Passive Mode (" + ipAddress + "," + p1 + "," + p2 + ").\r\n";
        sendResponse(outputStream, response);

        Socket dataSocket = dataServerSocket.accept();
        dataServerSocket.close();

        return new DataSocket(dataSocket);
    }

    private static void handleUserCommand(Scanner scanner, OutputStream outputStream) throws IOException {
        String username = scanner.next();
        if (testConnectionUserName(username)) {
            System.out.println("USER " + username);
            sendResponse(outputStream, "331 User " + username + " OK. Password required.\r\n");
        } else {
            sendResponse(outputStream, "userName incorrect.\r\n");
        }
    }

    private static void handlePassCommand(Scanner scanner, OutputStream outputStream) throws IOException {
        String password = scanner.next();
        if (testConnectionUserPass(password)) {
            System.out.println("PASS " + password);
            sendResponse(outputStream, "230 User logged in.\r\n");
        } else {
            sendResponse(outputStream, "530 Login incorrect.\r\n");
        }
    }

    private static void handleQuitCommand(OutputStream outputStream, Socket clientSocket, DataSocket dataSocket) throws IOException {
        try {
            sendResponse(outputStream, "221 Service closing control connection.\r\n");
        } finally {
            clientSocket.close();
            if (dataSocket != null) {
                dataSocket.close();
            }
        }
    }

    private static boolean testConnectionUserName(String username) {
        return userCredentials.containsKey(username);
    }

    private static boolean testConnectionUserPass(String password) {
        return userCredentials.containsValue(password);
    }

    private static void sendResponse(OutputStream outputStream, String response) throws IOException {
        outputStream.write(response.getBytes());
    }

    private static void handleRetrCommand(Scanner scanner, OutputStream outputStream, DataSocket dataSocket)
            throws IOException {
        if (dataSocket == null) {
            sendResponse(outputStream, "425 Use PASV first.\r\n");
            return;
        }

        String fileName = scanner.next().trim();
        File file = new File(fileName);

        if (!file.exists()) {
            sendResponse(outputStream, "550 File not found\r\n");
            return;
        }

        try {
            sendResponse(outputStream, "150 Opening data connection for RETR\r\n");

            try (FileInputStream fileInputStream = new FileInputStream(file);
                 OutputStream dataOutputStream = dataSocket.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }

                sendResponse(outputStream, "226 Transfer complete\r\n");
            } catch (IOException e) {
                sendResponse(outputStream, "552 Requested file action aborted\r\n");
            }
        } catch (IOException e) {
            sendResponse(outputStream, "552 Requested file action aborted\r\n");
        } finally {
            dataSocket.close();
        }
    }

    private static class DataSocket {
        private final Socket socket;

        public DataSocket(Socket socket) {
            this.socket = socket;
        }

        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }

        public void close() throws IOException {
            socket.close();
        }
    }
}
