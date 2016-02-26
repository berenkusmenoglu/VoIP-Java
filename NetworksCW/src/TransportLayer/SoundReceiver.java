package TransportLayer;

/*
 * SoundReceiver.java
 *
 * Created on 15 January 2003, 15:43
 */
/**
 *
 * @author abj
 */
import AudioLayer.AudioManager;
import CMPC3M06.AudioPlayer;
import Others.*;
import java.net.*;
import java.io.*;
import java.util.Vector;
import javax.sound.sampled.LineUnavailableException;

public class SoundReceiver {

    static DatagramSocket receiving_socket;
    private static AudioManager audioManager = new AudioManager();

    public static void main(String[] args) throws LineUnavailableException, IOException {

        //Port to open socket on
        int PORT = 55555;

        //Open a socket to receive from on port PORT
        try {
            receiving_socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }

        boolean running = true;
        AudioPlayer player = new AudioPlayer();
        while (running) {

            try {

                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] buffer = new byte[256];

                DatagramPacket packet = new DatagramPacket(buffer, 0, 256);
                receiving_socket.receive(packet);

               

                player.playBlock(packet.getData());

            } catch (IOException e) {
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }

        }

        //Close the socket
        receiving_socket.close();
    }

}
