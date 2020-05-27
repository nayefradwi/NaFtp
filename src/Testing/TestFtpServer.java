package Testing;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestFtpServer {
    public static final int PORT = 9997;

    public TestFtpServer() {
        ServerSocket ftpServerSocket = null;
        try {
            ftpServerSocket = new ServerSocket(PORT);
            System.out.println("server started");
            Socket accepted = ftpServerSocket.accept();
            DataInputStream is = new DataInputStream(accepted.getInputStream());
            DataOutputStream os = new DataOutputStream(accepted.getOutputStream());
            System.out.println("starting file transfer...");
            File file = new File("D:\\TestFtpDir\\ce_prerequisite_flowchart_2017_v2.pdf");
            System.out.println("does file exist: "+file.exists());
            os.writeBoolean(file.exists()); //send if the file exists
            os.flush();
            if (file.exists()) {
                System.out.println("file lenght: "+ file.length());
                os.writeDouble(file.length()); // send the length of the file
                os.flush();
                convertTheFileToBytesAndSendToClient(file, os);
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
        new TestFtpServer();
    }

    private void convertTheFileToBytesAndSendToClient(File fileRequested, DataOutputStream bufferOut) {
        FileInputStream fileInputStream = null;
        try {
            byte[] dataBuffer = new byte[1024]; //byte array of data that will be sent
            fileInputStream = new FileInputStream(fileRequested); //reading from the file
            //reading the bytes of the file
            int bytes = -2;
            while (bytes != -1) {
                bytes = fileInputStream.read(dataBuffer);
                System.out.println("bytes sent: "+bytes);
                bufferOut.write(dataBuffer);
                bufferOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } //closing the file stream
        }
    }
}
