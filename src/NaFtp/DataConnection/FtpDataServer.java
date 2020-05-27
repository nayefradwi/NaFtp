package NaFtp.DataConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpDataServer extends Thread {
    private int port;
    private Socket ftpDataClientSocket;

    public FtpDataServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        super.run();
        try {
            ServerSocket dataServerSocket = new ServerSocket(port);
            ftpDataClientSocket = dataServerSocket.accept();
            System.out.println("data socket accepted: " + ftpDataClientSocket.getInetAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getFtpDataClientSocket() {
        return ftpDataClientSocket;
    }


}
