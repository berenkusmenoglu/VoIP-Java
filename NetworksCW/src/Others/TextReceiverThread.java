package Others;

/*
 * TextReceiver.java
 *
 * Created on 15 January 2003, 15:43
 */
/**
 *
 * @author abj
 */
import java.net.*;
import java.io.*;
import networkscw.NetworksCW;
import networkscw.NetworksCW.SocketType;
import static networkscw.NetworksCW.SocketType.*;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class TextReceiverThread implements Runnable {

    public TextReceiverThread(SocketType type) {
        this.socketType = type;
    }

    static DatagramSocket receiving_socket;
    private NetworksCW.SocketType socketType = Type1;

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        //Port to open socket on
        int PORT = 55555;

        //Open a socket to receive from on port PORT
        try {
            switch (socketType) {
                case Type1:
                    receiving_socket = new DatagramSocket(PORT);
                    break;
                case Type2:
                    receiving_socket = new DatagramSocket2(PORT);
                    break;
                case Type3:
                    receiving_socket = new DatagramSocket3(PORT);
                    break;
                case Type4:
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
                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] buffer = new byte[80];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 80);
                System.out.println(new String(packet.getData()));
                receiving_socket.receive(packet);

                //Get a string from the byte buffer
                String str = new String(buffer);
                //Display it
                if (!str.isEmpty()) {
                    System.out.print(str.trim());
                }

                //The user can type EXIT to quit
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


// 139.222.6.6