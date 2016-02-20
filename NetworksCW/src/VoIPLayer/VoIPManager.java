package VoIPLayer;

import CMPC3M06.AudioPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    private boolean interleave = false;
    private int interleaveNumber = 16;
    private ArrayList<CustomPacket> packetsToSend = new ArrayList<CustomPacket>(interleaveNumber);
    private SocketType type;

    public VoIPManager() {
         type = SocketType.Type0;
    }

    private void interLeavePackets(ArrayList<CustomPacket> packetsToSend) {

        int n = (int) Math.sqrt(packetsToSend.size());

        CustomPacket[][] arrayToInterleave = new CustomPacket[n][n];

        // POPULATING ARRAY
        int a = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                arrayToInterleave[i][j] = packetsToSend.get(a);
                a++;

            }
        }

        // TRANSPOSING
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                CustomPacket temp = arrayToInterleave[i][j];
                arrayToInterleave[i][j] = arrayToInterleave[j][i];
                arrayToInterleave[j][i] = temp;

            }
        }

        // REVERSING COLUMNS
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n / 2; row++) {
                CustomPacket temp = arrayToInterleave[row][col];
                arrayToInterleave[row][col] = arrayToInterleave[n - row - 1][col];
                arrayToInterleave[n - row - 1][col] = temp;

            }
        }
        packetsToSend.clear();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {

                packetsToSend.add(arrayToInterleave[i][j]);
            }
        }

    }

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

        public DatagramPacket getPacket(InetAddress clientIP, int PORT) {
            DatagramPacket aPacket = new DatagramPacket(this.packetData, this.packetData.length, clientIP, PORT);

            return aPacket;
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
     * @param packet
     * @throws java.io.IOException
     */
    public void fixVoice(SocketType type, DatagramPacket packet) throws IOException {

        byte[] arrayToPlay = stripPacket(packet.getData());

        CustomPacket current = new CustomPacket(getNumberFromBuffer(packet.getData()), arrayToPlay);

        int packetNumber = (int) current.getPacketID();

        switch (type) {
            case Type0:

                break;
            case Type1:

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
                break;
            case Type2:
                if (interleave) {

                    listToSort.add(current);

                    expected++;

                    if (expected % interleaveNumber == 0 && expected > 0) {
                        Collections.sort(listToSort, new PacketComparator());

                        for (CustomPacket listToSort1 : listToSort) {
                            System.out.println("\t :" + listToSort1.packetID);
                            player.playBlock(listToSort1.packetData);
                        }
                        listToSort.clear();
                    }
                } else {
                    listToSort.add(current);
                    expected++;
                    if (expected % sortInterval == 0 && expected > 0) {

                        //SORTING THE ARRAY
                        Collections.sort(listToSort, new PacketComparator());

                        //FIXING PACKET LOSS
                        for (int a = 0; a < listToSort.size(); a++) {

                            while (currentID <= listToSort.get(a).packetID) {
                                //   System.out.println("Packet :" + listToSort.get(a).packetID + " compare : " + currentID);
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
                        for (CustomPacket listToSort1 : listToSort) {
                            //   System.out.println("Playing :" + listToSort1.packetID);
                            player.playBlock(listToSort1.packetData);
                        }

                        //CLEARING THE LIST
                        listToSort.clear();

                    }

                }
                //ADD CURRENT PACKET TO THE LIST

                break;
            case Type3:
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
                //System.out.println("Playing " + current.toString());
                player.playBlock(current.getPacketData());

                expected++;

                break;

        }

    }

    /**
     *
     * @param PORT
     * @param buffer
     * @param clientIP
     * @param number
     * @throws IOException
     */
    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP, int number) throws IOException {

        if (interleave) {

            buffer = giveNumberToBuffer(buffer, number);

            CustomPacket packetToSend = new CustomPacket(getNumberFromBuffer(buffer), buffer);

            packetsToSend.add(packetToSend);

            if (number % interleaveNumber == 0) {

                interLeavePackets(packetsToSend);

                //SENDING LIST
                for (CustomPacket packetsToSend1 : packetsToSend) {

                    DatagramPacket packet = packetsToSend1.getPacket(clientIP, PORT);
                    sendingSocket.send(packet);
                }
                packetsToSend.clear();
            }

        } else {

            buffer = giveNumberToBuffer(buffer, number);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

            sendingSocket.send(packet);
        }

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

        fixVoice(type, packet);

        ///////////////////////////////
        /*TEST
        
         if(nm != getNumberFromBuffer(packet.getData()))
         { 
         error++;
         System.out.println("Packet No: " + getNumberFromBuffer(packet.getData()) + " compare: " + nm);
       
         System.err.println("ERROR IN THE PACKET, error no: " + error );
           
         }
            
        
        
         */
    }

}
