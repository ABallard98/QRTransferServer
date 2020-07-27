
public class Main {

    public static void main(String[] args){
        FileReaderWriter.deleteAllStoredFiles();
        System.out.println("Booting...");
        ServerThread serverThread = new ServerThread();
        serverThread.run();
    }


}
