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

    /**
     * Method to delete all saved files on the server
     * @return - true if files successfully deleted
     */
    public static synchronized boolean deleteAllStoredFiles(){
        try{
            File dir = new File("Files"+File.separator);
            File[] files = dir.listFiles();
            for(File f : files){
                f.delete();
            }
            System.out.println("Saved files deleted successfully.");
            return true;
        } catch (Exception e){
            System.out.println("Saved files were not succesfully deleted.");
            return false;
        }
    }





}
