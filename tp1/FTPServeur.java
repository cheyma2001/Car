package tp1;


import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FTPServeur {

    private static ServerSocket serverSocket, dataServer;
    private static Socket clientSocket, dataClientSocket;
    private static String currentDirectory = "/home/passwd/devoir/Car";
    
private static final Map<String, String> userCredentials = new HashMap<>();

    static {
        // Ajoutez les utilisateurs et mots de passe autorisés ici
        userCredentials.put("user1", "pass1");
        userCredentials.put("user2", "pass2");
    }

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(2121);
            System.out.println("\nServer started on port 2121\n");

            while (true) {
                try {
                    clientSocket = serverSocket.accept();
                    processClientConnection(clientSocket);
                } catch (IOException e) {
                    System.out.println("Error handling client: " + e.getMessage());
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

    private static void processClientConnection(Socket clientSocket) {
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            InputStream inputStream = clientSocket.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            sendResponse(outputStream, "220 Service ready \r\n");
           

            while (true) {
                String cm = scanner.nextLine();
                System.out.println(cm);

                String[] commandArray = cm.split(" ");

                if (cm.startsWith("USER")) {
                    handleUserAuthentication(commandArray, outputStream, scanner);
                } else if (cm.equals("EPSV")) {
                    handleEpsvCommand(outputStream);
                } else if (cm.startsWith("RETR")) {
                    handleRetrCommand(commandArray, outputStream);
                } else if (cm.equals("LIST")) {
                    handleListCommand(outputStream);
                } else if (cm.startsWith("CWD")) {
                    handleCwdCommand(commandArray, outputStream);
                } else if (cm.toUpperCase().equals("QUIT")) {
                    outputStream.write("221 User logged out\r\n".getBytes());
                    if (clientSocket != null) clientSocket.close();
                } else {
                    sendResponse(outputStream,"502 Command not implemented\r\n");
                }
            }
        } catch (IOException e) {
            System.out.println("! Something wrong happened !");
            e.printStackTrace();
        }
    }

    private static void handleUserAuthentication(String[] commandTb, OutputStream out, Scanner scanner) throws IOException {
        String username = commandTb[1];
        if (userCredentials.containsKey(username)) {
            out.write("331 User name okay, need password\r\n".getBytes());
            String passCommand = scanner.nextLine();
            String[] passParts = passCommand.split(" ");
            if (passParts[0].equalsIgnoreCase("PASS") && userCredentials.get(username).equals(passParts[1])) {
                out.write("230 User logged in\r\n".getBytes());
            } else {
                out.write("530 Not logged in, incorrect password\r\n".getBytes());
            }
        } else {
            out.write("530 Not logged in, user not found\r\n".getBytes());
        }
    }

    private static void handleEpsvCommand(OutputStream outputStream) throws IOException {
        dataServer = new ServerSocket(0); 
        int hostPort = dataServer.getLocalPort();
        outputStream.write(("229 Entering Extended Passive Mode (|||" + hostPort + "|)\r\n").getBytes());
    }

  
    private static void handleRetrCommand(String[] commandTb, OutputStream outputStream) throws IOException {
        if (commandTb.length < 2) {
            sendResponse(outputStream, "501 Missing argument for RETR command\r\n");
            return;
        }
    
        String fileName = commandTb[1];
        File file = new File(currentDirectory, fileName); // Concaténer le chemin du répertoire courant
    
        if (!file.exists()) {
            sendResponse(outputStream, "550 File not found\r\n");
            return;
        }
    
        sendResponse(outputStream, "150 Opening BINARY mode data connection for file transfer\r\n");
    
        try (Socket dataClientSocket = dataServer.accept();
             FileInputStream fis = new FileInputStream(file);
             OutputStream dataOut = dataClientSocket.getOutputStream()) {
    
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                dataOut.write(buffer, 0, count);
            }
            sendResponse(outputStream, "226 Transfered with success\r\n");
    
        } finally {
            if (dataClientSocket != null && !dataClientSocket.isClosed()) {
                dataClientSocket.close();
            }
            if (dataServer != null && !dataServer.isClosed()) {
                dataServer.close();
            }
        }
    }
   

    private static void handleCwdCommand(String[] commandTb, OutputStream output) throws IOException {
        if (commandTb.length < 2) {
            sendResponse(output, "501 Missing argument for CWD command\r\n");
         
            return;
        }
    
        String targetDirectory = commandTb[1];
        File newDir = new File(currentDirectory, targetDirectory);
    
        if (newDir.isDirectory() && newDir.exists()) {
            currentDirectory = newDir.getAbsolutePath();
            sendResponse(output,"250 CWD command successful. Working directory is "+ currentDirectory + "\r\n");
          
        } else {
            sendResponse(output,"550 Failed to change directory\r\n");
          
        }
    }
    private static void handleListCommand(OutputStream outputStream) throws IOException {
        try (Socket dataClientSocket = dataServer.accept();
             OutputStream dataOut = dataClientSocket.getOutputStream()) {
    
            File directoryCurrent = new File(currentDirectory);
            File[] files = directoryCurrent.listFiles();
    
            if (files != null) {
                sendResponse(outputStream, "150 Displaying the directory contents\r\n");
               
    
                for (File file : files) {
                    String permissions = getPermissions(file);
                    if (file.isDirectory()) {
                        dataOut.write((permissions + " " + file.getName() + "\r\n").getBytes());
                    } else {
                        dataOut.write((permissions + " " + file.getName() + "\r\n").getBytes());
                    }
                }
                outputStream.write("226 Directory listing sent successfully.\r\n".getBytes());
            } else {
                outputStream.write("550 Directory not found\r\n".getBytes());
            }
    
        }
    }
    
    private static String getPermissions(File file) {
        StringBuilder permissions = new StringBuilder();
        if (file.isDirectory()) {
            permissions.append("d");
        } else {
            permissions.append("-");
        }
    
        if (file.canRead()) {
            permissions.append("r");
        } else {
            permissions.append("-");
        }
    
        if (file.canWrite()) {
            permissions.append("w");
        } else {
            permissions.append("-");
        }
    
        // Add more conditions for other permissions (execute, etc.) if needed.
    
        return permissions.toString();
    }


    private static void sendResponse(OutputStream outputStream, String response) throws IOException {
        outputStream.write(response.getBytes());
    }

    

}