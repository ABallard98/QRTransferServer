import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {

    public static void main(String[] args){
        System.out.println("Booting...");
        ServerThread serverThread = new ServerThread();
        serverThread.run();
    }


}
