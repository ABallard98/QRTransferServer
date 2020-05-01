import java.net.Socket;
import java.io.*;

public class FileReaderWriter {

    public static synchronized boolean saveTransaction(Boolean sent, Socket socket, String filename){
        String transaction = filename;
        if(sent == true){ //if sent to the server
            transaction += " SENT TO ";
        } else{
            transaction += " RECEIVED FROM ";
        }
        transaction += socket.getRemoteSocketAddress().toString();
        try {
            FileWriter fw = new FileWriter("transactions.txt", true);
            fw.write("\n" + transaction);
            fw.close();
            return true;
        } catch(Exception e){
            return false;
        }
    }





}
