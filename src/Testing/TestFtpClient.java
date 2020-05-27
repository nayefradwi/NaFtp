package Testing;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TestFtpClient {

    public TestFtpClient(){
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(),9997);
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            System.out.println("connected to server");
            File file = new File("D:\\TestFtpClientDir\\ce_prerequisite_flowchart_2017_v2.pdf");
            boolean fileExists = is.readBoolean();
            System.out.println("does file exist: "+fileExists);
            if(fileExists){
                double length = is.readDouble();
                System.out.println("file length is: "+length);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] data = new byte[1024];
                for(int i = 0; i<length/1024; i++){
                    int bytes =  is.read(data);
                    System.out.println("bytes read: "+bytes);
                    fileOutputStream.write(data);
                }
                fileOutputStream.close();
                System.out.println("transfer done");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TestFtpClient();
    }
}
