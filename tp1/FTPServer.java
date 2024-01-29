package tp1;
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
                System.err.println(command);
                switch (command) {
                    case "USER":
                        handleUserCommand(scanner, outputStream);
                        break;
                    case "PASS":
                        handlePassCommand(scanner, outputStream);
                        break;
                    case "RETR":
                    System.err.println("get");
                        handleRetrCommand(scanner, outputStream, dataSocket);
                        break;
                    case "QUIT":
                        handleQuitCommand(outputStream, clientSocket, dataSocket);
                        return;
                    case "PASV":
                        dataSocket = handlePasvCommand(clientSocket, outputStream);
                        break;
                    case "EPSV":
                        handlerEPSVCommand(outputStream);
                        break;
                    case "LPSV":
                        handlerLPSVCommand(outputStream);
                        break;
                    case "CD":
						handleDirCommand(clientSocket);
						break;
                    // case "LIST":
                    //     System.err.println("list");

                    //     handleListCommand(outputStream,clientSocket);
					// 	break;
                    default:
                        sendResponse(outputStream, "500 Syntax error, command not recognized.\r\n");
                }
            }
        }
    }



    
    
    


    private static void handleListCommand(OutputStream outputStream, Socket clientSocket) throws IOException {
        if (clientSocket == null) {
            sendResponse(outputStream, "425 Use PASV first.\r\n");
            return;
        }
    
        File directoryCurrent = new File(".");
        File[] files = directoryCurrent.listFiles();
    
        if (files == null) {
            sendResponse(outputStream, "550 Directory not found\r\n");
            return;
        } else {
            sendResponse(outputStream, "150 Here comes the directory listing\r\n");
    
            try (OutputStream dataOutputStream = clientSocket.getOutputStream()) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        dataOutputStream.write(("D " + file.getName() + "\r\n").getBytes());
                    } else if (file.isFile()) {
                        dataOutputStream.write(("F " + file.getName() + "\r\n").getBytes());
                    }
                }
    
                sendResponse(outputStream, "226 Directory send OK.\r\n");
            } catch (IOException e) {
                sendResponse(outputStream, "552 Requested file action aborted\r\n");
            }
        }
    }
    
 
	private static void handleDirCommand(Socket socket) throws IOException {
		// Retrieve a list of files in the current directory
		File folder = new File(".");
		File[] listOfFiles = folder.listFiles();
		String filesList = "";
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				filesList += listOfFiles[i].getName() + " [FILE], ";
			} else if (listOfFiles[i].isDirectory()) {
				filesList += listOfFiles[i].getName() + " [DIR], ";
			}
		}
		sendCommand(filesList, socket);
	}
    private static void sendCommand(String command, Socket socket) throws IOException {
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.println(command);
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

    public static void handlerEPSVCommand(OutputStream outputStream) {
        try (ServerSocket dataServerSocket = new ServerSocket(0)) {
            int port = dataServerSocket.getLocalPort();
            String mes = "228 Entering Extended Passive Mode (|||" + port + "|)\r\n";
            outputStream.write(mes.getBytes());
        } catch (IOException e) {
            System.err.println("Error : " + e);
        }
    }
    
    public static void handlerLPSVCommand(OutputStream outputStream) {
        try (ServerSocket dataServerSocket = new ServerSocket(0)) {
            int port = dataServerSocket.getLocalPort();
            String mes = "228 Entering Local Passive Mode (" + port + ")\r\n";
            outputStream.write(mes.getBytes());
        } catch (IOException e) {
            System.err.println("Error : " + e);
        }
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
                dataSocket.close();
            } catch (IOException e) {
                sendResponse(outputStream, "552 Requested file action aborted\r\n");
            }
        } catch (IOException e) {
            sendResponse(outputStream, "552 Requested file action aborted\r\n");
        } finally {
           
        }
    }

 
}
