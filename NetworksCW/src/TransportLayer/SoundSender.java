package TransportLayer;

/*
 * SoundSender.java
 *
 * Created on 15 January 2003, 15:29
 */
/**
 *
 * @author abj
 */
import AudioLayer.AudioManager;
import CMPC3M06.AudioRecorder;
import Others.*;
import VoIPLayer.VoIPManager;
import java.net.*;
import java.io.*;
import java.util.Vector;
import javax.sound.sampled.LineUnavailableException;

public class SoundSender {

    static DatagramSocket sending_socket;
    private static AudioManager audioManager = new AudioManager();
    private VoIPManager voIPManager;

    public static void main(String[] args) throws LineUnavailableException, IOException {

        //***************************************************
        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Open a socket to send from
        //We dont need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.
   
        try {
            sending_socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        //***************************************************
        //Main loop.  
        AudioRecorder recorder = new AudioRecorder();
        boolean running = true;
        while (running) {
            try {

              

                byte[] buffer = recorder.getBlock();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
                sending_socket.send(packet);

            } catch (IOException e) {
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }

        //Close the socket
        sending_socket.close();
        //***************************************************
    }
}
