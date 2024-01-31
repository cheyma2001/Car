package tp1;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Test {
    private static ServerSocket serverSocket, dataServer;
    private static Socket clientSocket, dataSocket;
    public static void main(String[] args) {        
        try {
            serverSocket = new ServerSocket(2020);
            System.out.println("FTP Server is running on port 2020");

            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not listen on port 2121: " + e.getMessage());
        }
    }


    private static void handleClient(Socket clientSocket) throws IOException {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream();
             Scanner scanner = new Scanner(inputStream)) {

            String response = "220 Service ready for new user.\r\n";
            outputStream.write(response.getBytes());

            while (true) {
                String command = scanner.next();
                System.out.println("Recieved : " + command);

                String[] commandParts = command.split(" ");

                if (command.startsWith("USER")) {
                    handleUserCommand(scanner, outputStream);
                } else if (command.startsWith("PASS")) {
                    handlePassCommand(scanner, outputStream);
                } else if (command.equals("EPSV")) {
                    handlerEPSVCommand(outputStream);
                } else if (command.startsWith("RETR")) {
                    handleRetrCommand(commandParts, outputStream);
                } else if (command.toUpperCase().equals("QUIT")) {
                    handleQuitCommand(outputStream, clientSocket);
                } else {
                    outputStream.write("502 Command not implemented\r\n".getBytes());
                }
            }
        }
    }


    public static void handlerEPSVCommand(OutputStream outputStream) throws IOException  {
        dataServer = new ServerSocket(0); 
        int hostPort = dataServer.getLocalPort();
        outputStream.write(("229 Entering Extended Passive Mode (|||" + hostPort + "|)\r\n").getBytes());

    }
    
    private static void handleUserCommand(Scanner scanner, OutputStream outputStream) throws IOException {
        String username = scanner.next();
        if ("user".equals(username)) {
            System.out.println("USER " + username);
            sendResponse(outputStream, "331 User " + username + " OK. Password required.\r\n");
        } else {
            sendResponse(outputStream, "userName incorrect.\r\n");
        }
    }

    
    private static void handlePassCommand(Scanner scanner, OutputStream outputStream) throws IOException {
        String password = scanner.next();
        if ("pass".equals(password)) {
            System.out.println("PASS " + password);
            sendResponse(outputStream, "230 User logged in.\r\n");
        } else {
            sendResponse(outputStream, "530 Login incorrect.\r\n");
        }
    }

    private static void handleQuitCommand(OutputStream outputStream, Socket clientSocket) throws IOException {
        try {
            sendResponse(outputStream, "221 Service closing control connection.\r\n");
        } finally {
            if (clientSocket != null) clientSocket.close();
        }
    }

    private static void sendResponse(OutputStream outputStream, String response) throws IOException {
        outputStream.write(response.getBytes());
    }

    private static void handleRetrCommand(String[] commandParts,OutputStream outputStream) throws IOException {
        String fileName = commandParts[1];
        File file = new File(fileName);

        if (!file.exists()) {
            outputStream.write("550 File not found\r\n".getBytes());
            return;
        }

        outputStream.write("150 Opening BINARY mode data connection for file transfer\r\n".getBytes());

        try (Socket dataSocket = dataServer.accept();
        FileInputStream fis = new FileInputStream(file);
        OutputStream dataOut = dataSocket.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                dataOut.write(buffer, 0, count);
            }
            outputStream.write("226 Transfered with success\r\n".getBytes());
        } finally {
            if (dataSocket != null && !dataSocket.isClosed()) {
                dataSocket.close();
            }
            if (dataServer != null && !dataServer.isClosed()) {
                dataServer.close();
            }
        }
    }

 
}
    
