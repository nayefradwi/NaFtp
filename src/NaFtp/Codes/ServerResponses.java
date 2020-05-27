package NaFtp.Codes;

import NaFtp.CommandConnection.FtpServer;

public interface ServerResponses {
    String WELCOME_MSG = "220 accepting users\r\n"; // welcome message
    String POSITIVE_AUTH_TLS_RESPONSE = "234 tls accepted\r\n";
    String POSITIVE_USER_RESPONSE = "230 login successful\r\n";
    String SYSTEM_NAME_RESPONSE = "215 WINDOWS10\r\n";
    String COMMAND_NOT_IMPLEMENTED_RESPONSE = "202 command not implemented\r\n";
    String ENTERING_PASSIVE_MODE_RESPONSE = "227 entering passive mode "; // send escape characters after the port
    String REQUEST_ACCEPTED_RESPONSE = "200 request accepted\r\n";
    String DATA_CONNECTION_COMMAND_COMPLETE = "250 data command accepted\r\n";
    String OPENING_BINARY_MODE = "150 list incoming\r\n";
    String NEED_PASSWORD_RESPONSE = "331 give me the password\r\n";
    String INVALID_USERNAME_PASSWORD_RESPONSE = "430 invalid username or pass\r\n";
    String NO_PERMISSION_RESPONSE = "450 you dont have permission\r\n";
    String PATH_NAME_CREATED_RESPONSE = "257 pathname "; // send escape characters as well as new pathname
    String SIZE_POSITIVE_RESPONSE = "213 "; // send escape characters as well as new pathname
    String SIZE_NEGATIVE_RESPONSE = "450 oops\r\n"; // send escape characters as well as new pathname


}
