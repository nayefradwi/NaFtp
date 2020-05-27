package NaFtp.CommandConnection;

import NaFtp.Codes.ClientCodes;
import NaFtp.Codes.DataOperationCodes;
import NaFtp.Codes.ServerResponses;
import NaFtp.DataConnection.DataService;
import NaFtp.DataConnection.FtpDataServer;
import models.FtpUser;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Random;


public class FtpService extends Thread implements ClientCodes, ServerResponses {
    private Socket clientSocket;
    private String ftpClientDir = FtpServer.FILE_DIRECTORY;
    public static final int STOP_BIT = -1;
    public static final int PORTS_CONSTANT = 256;
    public static final String IP_FIELD = "(192,168,100,12,";

    private DataInputStream bufferIn; // to transmit the bytes to the client
    private DataOutputStream bufferOut; // to receive the bytes from the client
    private FtpUser ftpUser;
    private FtpDataServer dataServer;

    public FtpService(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            System.out.println("client connected");
            bufferIn = new DataInputStream(clientSocket.getInputStream());
            bufferOut = new DataOutputStream(clientSocket.getOutputStream());
            bufferOut.write(WELCOME_MSG.getBytes()); // sending welcome message
            bufferOut.flush();
            this.start();
        } catch (IOException e) {
            try {
                if (bufferIn != null)
                    bufferIn.close();
                if (bufferOut != null)
                    bufferOut.close();
                if (this.clientSocket != null)
                    this.clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            String command;
            while (!clientSocket.isClosed()) {
                byte[] payload = new byte[1024];
                int bytesRead = bufferIn.read(payload);
                if (bytesRead != STOP_BIT) {
                    command = new String(payload);
                    handleCommand(command.trim());
                }
            }
        } catch (IOException e) {
            try {
                if (bufferOut != null) {
                    bufferOut.close();
                }
                if (clientSocket != null)
                    clientSocket.close();
                if (bufferIn != null)
                    bufferIn.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            //todo if the dataserver thread or dataservice thread where interrupted the buffer should send in complete task
        }
    }

    private void handleCommand(String command) throws IOException, InterruptedException {
        System.out.println(command);
        String[] args = command.split(" ");
        if (args[0].equalsIgnoreCase(AUTH)) {
            if (args[1].equalsIgnoreCase(TLS)) {
                bufferOut.write(SIZE_NEGATIVE_RESPONSE.getBytes());
                bufferOut.flush();
            }
        } else if (args[0].equalsIgnoreCase(USER)) {

            if (FtpServer.DBH.checkIfUserExists(args[1])) {
                ftpUser = new FtpUser(args[1]);
                bufferOut.write(NEED_PASSWORD_RESPONSE.getBytes());
            } else {
                bufferOut.write(INVALID_USERNAME_PASSWORD_RESPONSE.getBytes());
            }
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(PASS)) {
            if (FtpServer.DBH.checkIfPasswordIsCorrectAndCreateUser(ftpUser, args[1])) {

                bufferOut.write(POSITIVE_USER_RESPONSE.getBytes());
            } else {
                bufferOut.write(INVALID_USERNAME_PASSWORD_RESPONSE.getBytes());
            }
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(SYST)) {
            bufferOut.write(SYSTEM_NAME_RESPONSE.getBytes());
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(PWD) || args[0].equalsIgnoreCase(XPWD)) {
            String path = PATH_NAME_CREATED_RESPONSE + " \"" + ftpClientDir + "\"\r\n";
            bufferOut.write(path.getBytes());
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(PASV)) {
            Random random = new Random();
            int p1 = (random.nextInt(15)) + 6; // first parameter of port equation
            int p2 = random.nextInt(PORTS_CONSTANT) + 1; // second parameter of port equation
            int port = p1 * PORTS_CONSTANT + p2;
            String passiveDataPortMsg = ENTERING_PASSIVE_MODE_RESPONSE + IP_FIELD + p1 + "," + p2 + ")\r\n";
            bufferOut.write(passiveDataPortMsg.getBytes());
            bufferOut.flush();
            System.out.println("passive mode activated");
            dataServer = new FtpDataServer(port);
            dataServer.start();
            dataServer.join();
        } else if (args[0].equalsIgnoreCase(TYPE)) {
            bufferOut.write(REQUEST_ACCEPTED_RESPONSE.getBytes());
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(LIST)) {
            bufferOut.write(OPENING_BINARY_MODE.getBytes());
            bufferOut.flush();
            DataService service = new DataService(DataOperationCodes.LIST, dataServer.getFtpDataClientSocket(), ftpClientDir);
            service.start();
            service.join();
            bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
            bufferOut.flush();
            System.out.println("List complete");

        } else if (args[0].equalsIgnoreCase(RETR)) {
            if (ftpUser.isCanDownload()) {
                String fileToDownload = getCorrectArgument(args);
                bufferOut.write(OPENING_BINARY_MODE.getBytes());
                bufferOut.flush();
                DataService service = new DataService(DataOperationCodes.DOWNLOAD_FILE_FROM_SERVER, dataServer.getFtpDataClientSocket(), ftpClientDir);
                service.setArgument(fileToDownload.trim());
                service.start();
                service.join();
                bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
                bufferOut.flush();
                System.out.println("file downloaded from server");
            } else {
                bufferOut.write(NO_PERMISSION_RESPONSE.getBytes());
                bufferOut.flush();
            }
        } else if (args[0].equalsIgnoreCase(STOR)) {
            if (ftpUser.isCanUpload()) {
                String fileToStore = getCorrectArgument(args);
                bufferOut.write(OPENING_BINARY_MODE.getBytes());
                bufferOut.flush();
                DataService service = new DataService(DataOperationCodes.STORE_FILE_IN_SERVER, dataServer.getFtpDataClientSocket(), ftpClientDir);
                service.setArgument(fileToStore.trim());
                service.start();
                service.join();
                bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
                bufferOut.flush();
                System.out.println("file stored to server"); //todo convert this string to a constant and other similar prints and use them in logger
            } else {
                bufferOut.write(NO_PERMISSION_RESPONSE.getBytes());
                bufferOut.flush();
            }
        } else if (args[0].equalsIgnoreCase(MKD)) {
            if (ftpUser.isCanUpload()) {
                String directoryToMake = getCorrectArgument(args);
                createDirectory(directoryToMake.trim());
                bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
                bufferOut.flush();
                System.out.println("file stored to server");
            } else {
                bufferOut.write(NO_PERMISSION_RESPONSE.getBytes());
                bufferOut.flush();
            }
        } else if (args[0].equalsIgnoreCase(RMD) || args[0].equalsIgnoreCase(DELE)) {
            if (ftpUser.isCanDelete()) {
                String fileOrDirectoryToDelete = getCorrectArgument(args);
                deleteDirectory(fileOrDirectoryToDelete.trim());
                bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
                bufferOut.flush();
                System.out.println("file stored to server");
            } else {
                bufferOut.write(NO_PERMISSION_RESPONSE.getBytes());
                bufferOut.flush();
            }
        } else if (args[0].equalsIgnoreCase(CWD)) {
            String path = getCorrectArgument(args);
            setTheCorrectPath(path);
            bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(CDUP)) {
            ftpClientDir = FtpServer.FILE_DIRECTORY;
            bufferOut.write(DATA_CONNECTION_COMMAND_COMPLETE.getBytes());
            bufferOut.flush();
        } else if (args[0].equalsIgnoreCase(SIZE)) {
            String path = getCorrectArgument(args);
            setTheCorrectPath(path);
            long size = getDirectorySize();
            if(size!=-1){
                String positiveResponse = SIZE_POSITIVE_RESPONSE+size+"\r\n";
                bufferOut.write(positiveResponse.getBytes());
            }
            else{
                bufferOut.write(SIZE_NEGATIVE_RESPONSE.getBytes());
            }
            bufferOut.flush();

        } else {
            bufferOut.write(COMMAND_NOT_IMPLEMENTED_RESPONSE.getBytes());
            bufferOut.flush();
        }

    }

    private long getDirectorySize() {
        File file = new File(ftpClientDir);
       if(file.exists())
       {
           if(file.isDirectory())
           {
               File[] files = file.listFiles();
               return Objects.requireNonNull(files).length;
           }
           else
               return file.length();
       }
       else
           return -1;
    }

    private void setTheCorrectPath(String path) {
        if (path.equals("..") || path.contains(".."))
            ftpClientDir = FtpServer.FILE_DIRECTORY;
        else if (path.contains(FtpServer.DRIVE_LABEL))
            ftpClientDir = path + "\\";
        else
            ftpClientDir = ftpClientDir + path + "\\";
    }

    public String getCorrectArgument(String[] args) {
        String correctArgument = args[1];
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                correctArgument += " " + args[i];
            }
        }
        return correctArgument;
    }

    private void deleteDirectory(String fileOrDirectoryToDelete) {
        File file = new File(ftpClientDir + fileOrDirectoryToDelete);
        boolean deleted = file.delete();
        System.out.println("Directory downloaded: " + deleted);
    }

    private void createDirectory(String directoryToMake) {
        File dirc = new File(ftpClientDir + directoryToMake);
        boolean created = dirc.mkdir();
        System.out.println("Directory downloaded: " + created);
    }

// todo replace all print statements with logger
    //todo should i close the serversocket or is it handled by jgc??
    //todo regular string expression used to print or log as constants
    //todo handle active ftp issues
    //todo rename variable names to something suitable
    //todo log messages should make sense
    //todo fix the issue that causes CWD /D:\NaFtpDir\..\ in winscp
    //todo make sure ur ftp server works with google chrome
}
