package NaFtp.CommandConnection;

import Database.DatabaseHandler;

import java.io.IOException;
import java.net.ServerSocket;

public class FtpServer {
    public static final String FILE_DIRECTORY = "D:\\NaFtpDir\\";
    public static final String DRIVE_LABEL = "D:";
    public static final int PORT = 21;
    public static final DatabaseHandler DBH = new DatabaseHandler();

    public FtpServer() {
        ServerSocket ftpServerSocket = null;
        try {
            ftpServerSocket = new ServerSocket(PORT);
            System.out.println("server started");
            while (!ftpServerSocket.isClosed()) {
                new FtpService(ftpServerSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ftpServerSocket != null)
                    ftpServerSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        new FtpServer();
    }
}
