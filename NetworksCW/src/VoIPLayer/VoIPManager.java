package VoIPLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Date;
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
              //  receivingSocket = new DatagramSocket(8000);
                break;
            case Type1:
                sendingSocket = new DatagramSocket2();
              //  receivingSocket = new DatagramSocket2(8000);
                break;
            case Type2:
                sendingSocket = new DatagramSocket3();
             //   receivingSocket = new DatagramSocket3(8000);
                break;
            case Type3:
                sendingSocket = new DatagramSocket4();
              //  receivingSocket = new DatagramSocket4(8000);
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
    int i = 0;

    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
        sendingSocket.send(packet);

        //printPacketInfo(packet);
    }

    /**
     *
     * @param buffer
     * @param bufferSize
     * @return
     * @throws IOException
     */
    public void ReceiveVoice(byte[] buffer, int bufferSize) throws IOException {
        DatagramPacket packet = new DatagramPacket(buffer, 0, bufferSize);
        receivingSocket.receive(packet);

    }

    public void sendDummyPacket(int PORT, InetAddress clientIP, int dummyCount) throws IOException {

        byte[] buffer = new byte[256];;
        for (int packetNo = 0; packetNo < dummyCount; packetNo++) {
            String str = String.valueOf(packetNo);
            buffer = str.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);
            System.out.println("Sending packet: " + packetNo + " to "
                    + packet.getAddress() + " with data   "
                    + str);
            sendingSocket.send(packet);
            DatagramPacket receivedPacket = receiveDummyPacket();
            if (receivedPacket.getData() == null) {
                break;
            }
        }

    }

    public DatagramPacket receiveDummyPacket() throws IOException {

        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, 0, 256);
        receivingSocket.receive(packet);
        String str = new String(packet.getData());

        System.out.println("Packet received from:"
                + packet.getSocketAddress() + " with data: " + str);

        return packet;
    }


    /**
     *
     * @param packet
     */
    public void printPacketInfo(DatagramPacket packet) {

        System.out.println("Packet number: " + i + "\n"
                + "Packet address: " + packet.getAddress() + "\n"
                + "Packet socket address: " + packet.getSocketAddress() + "\n"
                + "Packet length: " + packet.getLength() + "\n"
                + "Packet offset: " + packet.getOffset() + "\n"
                + "Packet port: " + packet.getPort() + "\n"
                + "Packet data: " + packet.getData() + "\n");
        i++;
    }
}
