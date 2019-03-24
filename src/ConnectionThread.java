import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class ConnectionThread implements Runnable {

    private Socket socket;

    public ConnectionThread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String instruction = dis.readUTF();
            System.out.println("INSTRUCTION RECEIVED: " + instruction);

            if(instruction.contains("SENDING")){ //IF UPLOADING A FILE TO SERVER
                downloadFile(instruction, dis);
            } else if (instruction.contains("DOWNLOAD")){ //IF DOWNLOADING A FILE FROM SERVER
                sendFile(instruction,socket);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private synchronized void downloadFile(String instruction, DataInputStream dis) {

        HashMap fileHashMap = ServerThread.fileHashMap;

        long startTime = System.nanoTime();

        Scanner instructionReading = new Scanner(instruction);
        instructionReading.useDelimiter("-");
        instructionReading.next(); //skip "SENDING"

        String filename = instructionReading.next();
        int fileSize = instructionReading.nextInt();

        //Reading the message from the client
        File newTempFile = new File("files"+File.separator+filename);

        try {
            FileOutputStream fos = new FileOutputStream(newTempFile);
            byte[] buffer = new byte[4096];
            int read = 0;
            int totalRead = 0;
            int remaining = fileSize;
            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                //System.out.println("read " + totalRead + " bytes.");
                fos.write(buffer, 0, read);
            }
            dis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            //fileHashMap.put(filename, newTempFile);
            ServerThread.fileHashMap.put(filename, newTempFile);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        System.out.println("FILES IN HASH MAP: " + fileHashMap.size());

        long endTime = System.nanoTime();
        long duration = ((endTime - startTime)/1000000);
        System.out.println("File uploaded to server in " + duration + " milliseconds");

    }//end of downloadFile

    private synchronized void sendFile(String instruction, Socket sock){
        System.out.println("Attempting to send file to " + sock.toString());

        HashMap fileHashMap = ServerThread.fileHashMap;

        Scanner in = new Scanner(instruction);
        in.useDelimiter("-");
        in.next(); //skip the "DOWNLOAD"
        String filename = in.next();
        int filesize = in.nextInt();

        //find file to send
        File toSend = (File) fileHashMap.get(filename);
        if(toSend != null){ //if file was found
            try{
                long startTime = System.nanoTime();

                DataOutputStream dataOutputStream = new DataOutputStream(sock.getOutputStream());
                byte[] bytesArray = new byte[(int) toSend.length()];
                FileInputStream fis = new FileInputStream(toSend);
                fis.read(bytesArray); //read file into bytes[]

                dataOutputStream.writeInt(bytesArray.length); //send length
                dataOutputStream.write(bytesArray); //send bytes

                fis.close();
                dataOutputStream.close();

                long endTime = System.nanoTime();
                long duration = ((endTime - startTime)/1000000);

                System.out.println("File sent from server in " + duration + " milliseconds");

            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            System.out.println("Error - could not find file");
        }
    }//end of sendFile
}
