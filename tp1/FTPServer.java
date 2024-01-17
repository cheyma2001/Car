package tp1;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Scanner;



public class FTPServer{
	 private static final HashMap<String,String> userCredentials = new HashMap<>();
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
	 //chaque connexion ftp une nouvelle sokets est cree
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
				 
				 switch (command) {
				 case "USER":
					 handleUserCommand(scanner, outputStream);
				 break;
				 case "PASS":
					 handlePassCommand(scanner, outputStream);
				 break;
				 case "QUIT":
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
	 if (testConnectionUserName(username)){
	 sendResponse(outputStream, "331 User " + username + " OK. Password required.\r\n");
	 }
	 else{
	 sendResponse(outputStream, "userName incorrect.\r\n");
	 }

	 }
	 
	 private static void handlePassCommand(Scanner scanner, OutputStream outputStream) throws IOException {
	 String password = scanner.next();
	 
	 if (testConnectionUserPass(password)) {
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
	 
	 private static boolean testConnectionUserName(String username){
	 return userCredentials.containsKey(username);
	 }
	 private static boolean testConnectionUserPass(String password){
	 return userCredentials.containsValue(password);
	 }
	 //envoyer des message lors de la connexion ftp
	 private static void sendResponse(OutputStream outputStream, String response) throws IOException {
	 outputStream.write(response.getBytes());
	 }
	}