package TransportLayer;

/*
 * SoundReceiver.java
 *
 * Created on 15 January 2003, 15:43
 */

/**
 *
 * @author  abj
 */
import AudioLayer.AudioManager;
import Others.*;
import java.net.*;
import java.io.*;
import java.util.Vector;
import javax.sound.sampled.LineUnavailableException;

public class SoundReceiver {
    
    static DatagramSocket receiving_socket;
    private static AudioManager audioManager = new AudioManager();
    
    public static void main (String[] args) throws LineUnavailableException, IOException{
     
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
        Vector<byte[]> myVoice = new Vector<>(128) ;
        
        for(int i = 0; i < 128; i++)
                {
         
            try{
                //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, 0,256);

                receiving_socket.receive(packet);
                
               
                 myVoice.add(buffer);
                
                
                //System.out.println(packet.getLength());
                
                
                //Get a string from the byte buffer
                
                //audioManager.PlayAudioByte(buffer);
                
                //String str = new String(buffer);
                //Display it
                //System.out.print(str);

                //The user can type EXIT to quit
               // if (str.substring(0,4).equals("EXIT")){
               //      running=false;
               // }
            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
           
        }
         audioManager.PlayAudio(myVoice);
        
        //Close the socket
        receiving_socket.close();
    }
    
    
}
