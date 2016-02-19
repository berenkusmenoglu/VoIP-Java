package VoIPLayer;

import CMPC3M06.AudioPlayer;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.list;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

    class CustomPacket implements Comparable<CustomPacket> {

        public long packetID;
        public byte[] packetData;

        private CustomPacket(long numberFromBuffer, byte[] arrayToPlay) {
            this.packetData = arrayToPlay;
            this.packetID = numberFromBuffer;
        }

        public long getPacketID() {
            return packetID;
        }

        public void setPacketID(long packetID) {
            this.packetID = packetID;
        }

        public byte[] getPacketData() {
            return packetData;
        }

        public void setPacketData(byte[] packetData) {
            this.packetData = packetData;
        }

        @Override
        public int compareTo(CustomPacket o) {
            return (int) (this.packetID - o.packetID);
        }

        @Override
        public String toString() {
            return "packetID= " + packetID + ", packetData= " + packetData;
        }

    }

    class PacketComparator implements Comparator<CustomPacket> {

        @Override
        public int compare(CustomPacket customPacket1, CustomPacket customPacket2) {
            return (int) (customPacket1.getPacketID() - customPacket2.getPacketID());
        }

    }

    DatagramSocket sendingSocket;
    DatagramSocket receivingSocket;
    AudioPlayer player;

    byte[] currentBuffer;
    ArrayList receivedPackets = new ArrayList();

    /**
     *
     * @param socketType
     * @param a
     * @throws SocketException
     * @throws javax.sound.sampled.LineUnavailableException
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
        byte[] mergedArray = mergeArrays(packetIdentifier, givenBuffer);

        return mergedArray;

    }

    public long getNumberFromBuffer(byte[] givenBuffer) {

        byte[] longBuffer = new byte[8];

        System.arraycopy(givenBuffer, 0, longBuffer, 0, 8);

        long packetID = byteArray2Long(longBuffer);

        return packetID;

    }

    public byte[] stripPacket(byte[] givenBuffer) {

        byte[] newArray = new byte[givenBuffer.length - 8];

        System.arraycopy(givenBuffer, 8, newArray, 0, newArray.length);

        return newArray;

    }

    /**
     * Method to fix voice issues on the receiving side.
     *
     * @param type
     * @param recePackets
     * @return
     */
    public byte[] fixVoice(SocketType type, ArrayList recePackets) {

        byte[] arrayToPlay = new byte[512];

        switch (type) {
            case Type0:

                break;
            case Type1:

                break;
            case Type2:

                break;
            case Type3:

                break;

        }

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
    int expected = 0;
    int error = 0;
    int sortInterval = 10;
    int currentID = 0;
    ArrayList<CustomPacket> listToSort = new ArrayList<CustomPacket>();

    CustomPacket previous = new CustomPacket(0, new byte[512]);

    public void ReceiveVoice(byte[] buffer, int bufferSize) throws IOException {

        DatagramPacket packet = new DatagramPacket(buffer, 0, bufferSize);

        receivingSocket.receive(packet);
        
       

        byte[] arrayToPlay = stripPacket(packet.getData());

        CustomPacket current = new CustomPacket(getNumberFromBuffer(packet.getData()), arrayToPlay);

        int packetNumber = (int) current.getPacketID();
        int type = 1;

        if (type == 0) {
            player.playBlock(arrayToPlay);
        } else if (type == 1) {

            //FILLING THE GAPS UNTILL PACKET NUMBER MATCHES EXPECTED
            while (expected <= packetNumber) {

                //MISSING PACKETS FIX
                if (current.packetID == expected) {

                    //PREVIOUS BECOMES CURRENT
                    previous = current;
                    previous.setPacketID(expected);

                } else {

                    //CURRENT BECOMES PREVIVOUS
                    current = previous;

                }

                //PLAY CURRENT
               // System.out.println("Playing " + current.packetID);
                player.playBlock(current.getPacketData());

                expected++;
            }

        } else if (type == 2) {

            //ADD CURRENT PACKET TO THE LIST
            listToSort.add(current);
            expected++;
            if (expected % sortInterval == 0 && expected > 0) {

                //SORTING THE ARRAY
                Collections.sort(listToSort, new PacketComparator());

                //FIXING PACKET LOSS
                for (int a = 0; a < listToSort.size(); a++) {

       
                    while (currentID <= listToSort.get(a).packetID) {
                    System.out.println("Packet :" + listToSort.get(a).packetID + " compare : " + currentID);
                        //MISSING PACKETS FIX
                        if (listToSort.get(a).packetID == currentID) {

                     
                            //PREVIOUS BECOMES CURRENT
                            previous = listToSort.get(a);
                            previous.setPacketID(currentID);

                        } else {

                            //CURRENT BECOMES PREVIVOUS
                           
                            listToSort.add(a, previous);

                        }
                        //System.out.println(listToSort.get(a) + "\n");

                        currentID++;
                    }
           

                   

                }
     
                //PLAYING SORTED ARRAY
                for (int b = 0; b < listToSort.size(); b++) {

                   // System.out.println("Playing :" + listToSort.get(b).packetID);
                    player.playBlock(listToSort.get(b).packetData);

                }

                //CLEARING THE LIST
                listToSort.clear();

            }

        } else if (type == 3) {
            
       
                //MISSING PACKETS FIX
                if (current.packetID == expected) {

                    //PREVIOUS BECOMES CURRENT
                    previous = current;
                    previous.setPacketID(expected);

                } else {

                    //CURRENT BECOMES PREVIVOUS
                    current = previous;

                }

                //PLAY CURRENT
                System.out.println("Playing " + current.toString());
                player.playBlock(current.getPacketData());

                expected++;
       
          
        }

        ///////////////////////////////
        /*TEST
        
         if(nm != getNumberFromBuffer(packet.getData()))
         { 
         error++;
         System.out.println("Packet No: " + getNumberFromBuffer(packet.getData()) + " compare: " + nm);
       
         System.err.println("ERROR IN THE PACKET, error no: " + error );
           
         }
            
        
        
         */
        //receivedPackets.add(cp);
        //arrayToPlay = fixVoice(SocketType.Type0, receivedPackets);
    }

}
