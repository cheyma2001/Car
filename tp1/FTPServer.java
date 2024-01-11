package tp1;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Scanner;


class FTPServer{
     public static void main(String[] args) {
       
        ServerSocket serverSocket;

        try {
            
            serverSocket = new ServerSocket(2020); //ecouter les connexion du port 21
            System.out.println("connected to localhost");

            while (true) { //chaque connexion ftp une nouvelle sokets est cree
                Socket clientSocket = serverSocket.accept();
                OutputStream out =clientSocket.getOutputStream();
                String str="200 Service ready\r\n";
                out.write(str.getBytes());
              //  System.out.println("Nouvelle connexion acceptée.");
                InputStream in = clientSocket.getInputStream();
                
                Scanner scanner = new Scanner(in);


                String userName= scanner.nextLine();
                System.out.println(userName);
                out.write("331 User name ok \r\n".getBytes());
             

                String pwd= scanner.nextLine();
                out.write("230 User logged in \r\n".getBytes());
                System.out.println(pwd);

               scanner.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
}	
