import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread implements Runnable{

    public static HashMap<String, File> fileHashMap;
    private ServerSocket serverSocket;
    private final int PORT = 8007;

    public ServerThread(){
        this.fileHashMap = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket();
            this.serverSocket.setReuseAddress(true);
            this.serverSocket.bind(new InetSocketAddress(PORT));

            while(true) {
                Socket sock;
                System.out.println("Waiting for connection...");
                sock = serverSocket.accept();
                System.out.println("Accepted connection : " + sock);

                ConnectionThread connectionThread = new ConnectionThread(sock);
                Thread ct = new Thread(connectionThread);
                ct.start();
                //connectionThread.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void putFileInHashMap(File file){
        fileHashMap.put(file.getName(),file);
    }

    public static synchronized void getFileFromHashMap(File file){
        fileHashMap.get(file.getName());
    }
}
