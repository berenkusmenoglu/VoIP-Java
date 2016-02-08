package TransportLayer;

/*
 * TextSender.java
 *
 * Created on 15 January 2003, 15:29
 */
/**
 *
 * @author abj
 */
import AudioLayer.AudioManager;
import java.net.*;
import java.io.*;
import networkscw.NetworksCW.SocketType;
import static networkscw.NetworksCW.SocketType.*;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class VoiceSenderThread implements Runnable {

    static DatagramSocket sending_socket;
    private final AudioManager audioManager = new AudioManager();

    private SocketType socketType = Type0;

    public VoiceSenderThread(SocketType type) {
        this.socketType = type;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        //Port to send to
        int PORT = 8000;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        //Open a socket to send from
        //We dont need to know its port number as we never send anything to it.
        try {
            switch (socketType) {
                case Type0:
                    sending_socket = new DatagramSocket();
                    break;
                case Type1:
                    sending_socket = new DatagramSocket2();
                    break;
                case Type2:
                    sending_socket = new DatagramSocket3();
                    break;
                case Type3:
                    sending_socket = new DatagramSocket4();
                    break;

            }

        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        boolean running = true;

        while (running) {
            try {
                String str = "";
                byte[] buffer = null;
                str = in.readLine();
                buffer = str.getBytes();

                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

                //Send it
                sending_socket.send(packet);

            } catch (IOException e) {
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();

    }
}
