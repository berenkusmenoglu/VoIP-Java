package TransportLayer;

/*
 * TextReceiver.java
 *
 * Created on 15 January 2003, 15:43
 */
/**
 *
 * @author abj
 */
import AudioLayer.AudioManager;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import networkscw.NetworksCW.SocketType;
import static networkscw.NetworksCW.SocketType.*;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class VoiceReceiverThread implements Runnable {

    static DatagramSocket receiving_socket;
    private SocketType socketType = Type0;

    public VoiceReceiverThread(SocketType type) {
        this.socketType = type;
    }

    AudioManager audioManager = new AudioManager();

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        //Port to open socket on
        int PORT = 8000;

        //Open a socket to receive from on port PORT
        try {
            switch (socketType) {
                case Type0:
                    receiving_socket = new DatagramSocket(PORT);
                    break;
                case Type1:
                    receiving_socket = new DatagramSocket2(PORT);
                    break;
                case Type2:
                    receiving_socket = new DatagramSocket3(PORT);
                    break;
                case Type3:
                    receiving_socket = new DatagramSocket4(PORT);
                    break;

            }

        } catch (SocketException e) {
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }

        boolean running = true;

        while (running) {

            try {

                byte[] buffer = new byte[1000];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 80);

                receiving_socket.receive(packet);

                String str = new String(buffer);

                if (!str.isEmpty()) {
                    System.out.print(str.trim());
                }

                if (str.substring(0, 4).equalsIgnoreCase("EXIT")) {
                    running = false;
                }

            } catch (IOException e) {
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();

    }
}
