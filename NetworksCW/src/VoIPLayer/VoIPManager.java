package VoIPLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
    
    byte[] currentBuffer;

    /**
     * 
     * @param ints
     * @return 
     */
    public byte[] intsToBytes(int[] ints) {
        ByteBuffer bb = ByteBuffer.allocate(ints.length * 4);
        IntBuffer ib = bb.asIntBuffer();
        for (int i : ints) {
            ib.put(i);
        }
        return bb.array();
    }

    /**
     * 
     * @param bytes
     * @return 
     */
    public int[] bytesToInts(byte[] bytes) {
        int[] ints = new int[bytes.length / 4];
        ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
        return ints;
    }

    /**
     * 
     * @param socketType
     * @throws SocketException 
     */
    public void setSocketType(SocketType socketType) throws SocketException {
        switch (socketType) {
            case Type0:
                sendingSocket = new DatagramSocket();
             //   receivingSocket = new DatagramSocket();
                break;
            case Type1:
                sendingSocket = new DatagramSocket2();
             //   receivingSocket = new DatagramSocket2();
                break;
            case Type2:
                sendingSocket = new DatagramSocket3();
            //    receivingSocket = new DatagramSocket3();
                break;
            case Type3:
                sendingSocket = new DatagramSocket4();
            //    receivingSocket = new DatagramSocket4();
                break;

        }
        
    }

    /**
     * 
     * @param PORT
     * @param buffer
     * @param clientIP
     * @throws IOException 
     */
    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
        sendingSocket.send(packet);
        //System.out.println("Buffer info: " + buffer.length);
    }

    /**
     * 
     * @param buffer
     * @param bufferSize
     * @return
     * @throws IOException 
     */
    public DatagramPacket ReceiveVoice(byte[] buffer, int bufferSize) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, 0, bufferSize);
        receivingSocket.receive(packet);
      
        return packet;
    }
}
