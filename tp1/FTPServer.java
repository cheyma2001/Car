package tp1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;

public class FTPServer {
	private static final HashMap<String, String> userCredentials = new HashMap<>();
	static {
		// creation de deux utilisateurs
		userCredentials.put("user1", "pass1");
		userCredentials.put("user2", "pass2");

	}

	public static void main(String[] args) {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(2020);
			System.out.println("FTP Server is running on port 2020");
			// chaque connexion ftp une nouvelle sokets est cree
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

			while (true) {
				String command = scanner.next().toUpperCase();
				System.out.println(command);
				switch (command) {
					case "USER":
						System.out.println(command);
						handleUserCommand(scanner, outputStream);
						break;
					case "PASS":
						System.out.println(command);
						handlePassCommand(scanner, outputStream);
						break;
					case "RETR":
						System.out.println(command);
						handleRetrCommand(scanner, outputStream, clientSocket);
						break;
					case "QUIT":
						System.out.println(command);
						handleQuitCommand(outputStream, clientSocket);
						return;

					default:
						sendResponse(outputStream, "500 Syntax error, command not recognized.\r\n");
				}
			}

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

	private static void handleQuitCommand(OutputStream outputStream, Socket clientSocket) throws IOException {
		try {
			sendResponse(outputStream, "221 Service closing control connection.\r\n");
		} finally {
			clientSocket.close();
		}
	}

	private static boolean testConnectionUserName(String username) {
		return userCredentials.containsKey(username);
	}

	private static boolean testConnectionUserPass(String password) {
		return userCredentials.containsValue(password);
	}

	// envoyer des message lors de la connexion ftp
	private static void sendResponse(OutputStream outputStream, String response) throws IOException {
		outputStream.write(response.getBytes());
	}

	private static void handleRetrCommand(Scanner scanner, OutputStream outputStream, Socket clientSocket)
			throws IOException {
		String fileName = scanner.next().trim();
		File file = new File(fileName);
		System.out.println(file.exists());
		if (!file.exists()) {
			sendResponse(outputStream, "550 File not found\r\n");
			return;
		}
		try {
			sendResponse(outputStream, "150 Opening data connection for RETR\r\n");

			// Accept the data connection for file transfer
			try (Socket dataSocket = new Socket(clientSocket.getInetAddress(), 2020);
					FileInputStream fileInputStream = new FileInputStream(file);
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
		}
	}

}