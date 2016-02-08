package Others;

/*
 * TextReceiver.java
 *
 * Created on 15 January 2003, 15:43
 */

/**
 *
 * @author  abj
 */
import java.net.*;
import java.io.*;

public class TextReceiver {
    
    static DatagramSocket receiving_socket;
    
    public static void main (String[] args){
     
        //Port to open socket on
        
        int PORT = 55555;

        //Open a socket to receive from on port PORT

        try{
		receiving_socket = new DatagramSocket(PORT);
	} catch (SocketException e){
                System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
		e.printStackTrace();
                System.exit(0);
	}

        boolean running = true;
        
        while (running){
         
            try{
                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] buffer = new byte[80];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 80);

                receiving_socket.receive(packet);

                //Get a string from the byte buffer
                String str = new String(buffer);
                //Display it
                System.out.print(str);

                //The user can type EXIT to quit
                if (str.substring(0,4).equals("EXIT")){
                     running=false;
                }
            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
    }
    
    
}
