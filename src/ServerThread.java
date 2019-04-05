
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * This class implements a thread that creates a server socket, along with a HashMap to store transferred files.
 * When a connection is found, a ConnectionThread object is created to handle the request.
 */
public class ServerThread extends Thread{

    public static HashMap<String, File> fileHashMap; //HashMap for transferred files
    private ServerSocket serverSocket; //Server socket
    private final int PORT = 8007; //Port for server socket

    /**
     * Default constructor for ServerThread object
     */
    public ServerThread(){
        this.fileHashMap = new HashMap<>();
    }

    /**
     * In this method, the server socket will loop awaiting for a connection,
     * when a connection is found, a new ConnectionThread object is created and ran.
     */
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

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to put a file into the file HashMap
     * @param file - File to store
     */
    public static synchronized void putFileInHashMap(File file){
        fileHashMap.put(file.getName(),file);
    }

    /**
     * Method to return a file from the file HashMap
     * @param fileName - name of file to find
     */
    public static synchronized void getFileFromHashMap(String fileName){
        fileHashMap.get(fileName);
    }
}
