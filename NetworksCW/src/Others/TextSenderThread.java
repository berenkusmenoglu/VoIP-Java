package Others;

/*
 * TextSender.java
 *
 * Created on 15 January 2003, 15:29
 */
/**
 *
 * @author abj
 */
import java.net.*;
import java.io.*;
import networkscw.NetworksCW;
import static networkscw.NetworksCW.SocketType.*;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class TextSenderThread implements Runnable {

    public TextSenderThread(NetworksCW.SocketType type) {
         this.socketType = type;
    }

    
    static DatagramSocket sending_socket;
    private NetworksCW.SocketType socketType = Type1;

    boolean UIenabled;

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {

        //***************************************************
        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("139.222.6.6");  //CHANGE localhost to IP or NAME of client machine
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
        //DatagramSocket sending_socket;
        try {
           switch (socketType) {
                case Type1:
                   sending_socket = new DatagramSocket();
                    break;
                case Type2:
                    sending_socket = new DatagramSocket2();
                    break;
                case Type3:
                    sending_socket = new DatagramSocket3();
                    break;
                case Type4:
                    sending_socket = new DatagramSocket4();
                    break;

            }
        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Get a handle to the Standard Input (console) so we can read user input
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //***************************************************

        //***************************************************
        //Main loop.
        boolean running = true;

        while (running) {
            try {
                String str = "";
                byte[] buffer = null;

                str = in.readLine();
                
                //Convert it to an array of bytes
                //System.out.println(str);
                buffer = str.getBytes();
                //Read in a string from the standard input

                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

                //Send it
                sending_socket.send(packet);

                //The user can type EXIT to quit
                if (str.equals("EXIT")) {
                    running = false;
                }

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

//139.222.6.6