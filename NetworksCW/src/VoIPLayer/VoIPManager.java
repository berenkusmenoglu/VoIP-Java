package VoIPLayer;

import CMPC3M06.AudioPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Date;
import javax.sound.sampled.LineUnavailableException;
import networkscw.NetworksCW.SocketType;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

/**
 *
 * @author Beren
 */
public class VoIPManager {

    DatagramSocket sendingSocket;
    DatagramSocket receivingSocket;
    AudioPlayer player;

    byte[] currentBuffer;



    /**
     *
     * @param socketType
     * @throws SocketException
     */
    public void setSocketType(SocketType socketType, char a) throws SocketException, LineUnavailableException {
        player = new AudioPlayer();
        switch (socketType) {
            case Type0:
                if (a == 's') {
                    sendingSocket = new DatagramSocket();
                } else {
                    receivingSocket = new DatagramSocket(55555);
                }
                break;
            case Type1:
                if (a == 's') {
                    sendingSocket = new DatagramSocket2();
                } else {
                    receivingSocket = new DatagramSocket2(55555);
                }
                break;
            case Type2:
                if (a == 's') {
                    sendingSocket = new DatagramSocket3();
                } else {
                    receivingSocket = new DatagramSocket3(55555);
                }
                break;
            case Type3:
                if (a == 's') {
                    sendingSocket = new DatagramSocket4();
                } else {
                    receivingSocket = new DatagramSocket4(55555);
                }
                break;

        }

    }
    
        public static byte[] long2ByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static long byteArray2Long(byte[] byteArray) {
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        long l = bb.getLong();
        return l;

    }

    public byte[] mergeArrays(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public byte[] giveNumberToBuffer(byte[] givenBuffer, long num) {

        byte[] packetIdentifier = long2ByteArray(num);
        byte[] mergedArray = mergeArrays(packetIdentifier,givenBuffer);

        return mergedArray;

    }

    public long getNumberFromBuffer(byte[] givenBuffer) {
        
        byte[] longBuffer = new byte[8] ;
        
        System.arraycopy(givenBuffer, 0, longBuffer, 0, 8);
        
        long packetID = byteArray2Long(longBuffer);

        return packetID;

    }

    public byte[] stripPacket(byte[] givenBuffer) {
     
        byte[] newArray = new byte[givenBuffer.length - 8];
        
        System.arraycopy(givenBuffer, 8, newArray, 0, newArray.length);
       
        return newArray;

    }
    
       public byte[] fixVoice (SocketType type, byte[] givenArray) {
     
        byte[] arrayToPlay = new byte[givenArray.length];
        

        return arrayToPlay;

    }
    

    /**
     *
     * @param PORT
     * @param buffer
     * @param clientIP
     * @throws IOException
     */
    int i = 0;

    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP, int number) throws IOException {

        buffer = giveNumberToBuffer(buffer, number);

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

        sendingSocket.send(packet);

    }

    /**
     *
     * @param buffer
     * @param bufferSize
     * @throws IOException
     */
    public void ReceiveVoice(byte[] buffer, int bufferSize) throws IOException {

        DatagramPacket packet = new DatagramPacket(buffer, 0, bufferSize);

        receivingSocket.receive(packet);

        System.out.println("Packet no: " + getNumberFromBuffer(packet.getData()));

        byte[] arrayToPlay = stripPacket(packet.getData());
        
        //arrayToPlay = fixVoice(SocketType.Type0, arrayToPlay);
        
        player.playBlock(arrayToPlay);
    }

   
}
