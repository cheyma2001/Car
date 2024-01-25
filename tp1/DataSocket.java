package tp1;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class DataSocket {
       
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

