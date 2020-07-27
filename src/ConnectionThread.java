import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class implements a runnable to handle the requests for file transfers from other devices
 */
public class ConnectionThread implements Runnable {

    private Socket socket; //Socket of other device

    /**
     * Default constructor for ConnectionThread
     * @param socket
     */
    public ConnectionThread(Socket socket){
        this.socket = socket; //allocating socket of other device
    }

    /**
     * This method is used to read an instruction sent from the target device. If the instruction is a 'SENDING'
     * instruction, then the method downloadFile is called. If the instruction is a 'DOWNLOAD' instruction, then the
     * sendFile instruction is called.
     */
    @Override
    public void run() {
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String instruction = dis.readUTF();
            System.out.println("INSTRUCTION RECEIVED: " + instruction);

            String typeOfInstruction = getInstruction(instruction);

            if(typeOfInstruction.equalsIgnoreCase("SENDING")){ //IF UPLOADING A FILE TO SERVER
                downloadFile(instruction, dis);
            } else if (typeOfInstruction.equalsIgnoreCase("DOWNLOAD")){ //IF DOWNLOADING A FILE FROM SERVER
                sendFile(instruction,socket);
            } else if (typeOfInstruction.equalsIgnoreCase("WAIT_FOR_DOWNLOAD")){
                //todo implement socket waiting for file to be uploaded
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to get the type of instruction from the instruction sent from
     * the connecting device
     * @param s - String s
     * @return - Instruction as a String
     */
    private String getInstruction(String s){
        Scanner in = new Scanner(s);
        in.useDelimiter("-");
        String instruction = in.next();
        return instruction;
    }

    /**
     * This method implements the process to send a file from the server, to the target device. This method is also
     * synchronized to prevent deadlocks on the ServerThread HashMap
     * @param instruction - instruction received by target device
     * @param dis - DataInputStream
     */
    private synchronized void downloadFile(String instruction, DataInputStream dis) {

        HashMap fileHashMap = ServerThread.fileHashMap;

        long startTime = System.nanoTime();

        Scanner instructionReading = new Scanner(instruction);
        instructionReading.useDelimiter("-");
        instructionReading.next(); //skip "SENDING"

        String filename = instructionReading.next();
        int fileSize = instructionReading.nextInt();

        //Reading the message from the client
        File newTempFile = new File("src/files/"+filename);

        try {
            newTempFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(newTempFile);
            byte[] buffer = new byte[4096];
            int read = 0;
            int totalRead = 0;
            int remaining = fileSize;
            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                fos.write(buffer, 0, read);
            }
            dis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            ServerThread.fileHashMap.put(filename, newTempFile);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        System.out.println("FILES IN HASH MAP: " + fileHashMap.size());

        long endTime = System.nanoTime();
        long duration = ((endTime - startTime)/1000000);
        System.out.println("File uploaded from server in " + duration + " milliseconds");
        //save transaction history
        FileReaderWriter.saveTransaction(false,socket,filename);

    }//end of downloadFile

    /**
     * This method implements the process to download a file to the server from the target device. This method is also
     * synchronized to prevent deadlocks on the ServerThread HashMap
     * @param instruction - instruction received by target device
     * @param sock - Socket of target device
     */
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

                System.out.println("File sent to server in " + duration + " milliseconds");

                //save transaction history
                FileReaderWriter.saveTransaction(true,socket,filename);
            } catch(Exception e){
                e.printStackTrace();
            }
        } else {
            System.out.println("Error - could not find file");
        }
    }//end of sendFile

    private synchronized void mobileToDesktopTransfer(Socket sock){

    }
}
