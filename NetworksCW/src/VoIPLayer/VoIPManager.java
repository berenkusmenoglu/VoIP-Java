package VoIPLayer;

import CMPC3M06.AudioPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.CRC32;
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

    private final boolean interleave = false;
    private final boolean repetitionMethod = true;

    private final int interleaveNumber = 16;

    private ArrayList<CustomPacket> packetsToSend = new ArrayList<CustomPacket>(interleaveNumber);
    private DatagramSocket sendingSocket;
    private DatagramSocket receivingSocket;
    private AudioPlayer player;
    private SocketType type;
    private ArrayList<CustomPacket> packets;

    public VoIPManager(SocketType type) {
        this.type = type;

    }

    public void setType(SocketType type) {
        this.type = type;
    }

    public SocketType getType() {
        return this.type;
    }

    private CustomPacket[][] interleavePackets(CustomPacket[][] packetsArray) {

        int n = packetsArray.length;

        // TRANSPOSING
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                CustomPacket temp = packetsArray[i][j];
                packetsArray[i][j] = packetsArray[j][i];
                packetsArray[j][i] = temp;

            }
        }

        // REVERSING COLUMNS
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n / 2; row++) {
                CustomPacket temp = packetsArray[row][col];
                packetsArray[row][col] = packetsArray[n - row - 1][col];
                packetsArray[n - row - 1][col] = temp;

            }
        }

        return packetsArray;

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
            return new DatagramPacket(this.packetData, this.packetData.length, clientIP, PORT);
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

    /**
     *
     * @param socketType
     * @param a
     * @throws SocketException
     * @throws javax.sound.sampled.LineUnavailableException
     */
    public void readySocket(SocketType socketType, char a) throws SocketException, LineUnavailableException {
        player = new AudioPlayer();

        switch (socketType) {
            case Type1:
                if (a == 's') {
                    sendingSocket = new DatagramSocket();
                } else {
                    receivingSocket = new DatagramSocket(55555);
                }
                break;
            case Type2:
                if (a == 's') {
                    sendingSocket = new DatagramSocket2();
                } else {
                    receivingSocket = new DatagramSocket2(55555);
                }
                break;
            case Type3:
                if (a == 's') {
                    sendingSocket = new DatagramSocket3();
                } else {
                    receivingSocket = new DatagramSocket3(55555);
                }
                break;
            case Type4:
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

        if (type != SocketType.Type4) {

            byte[] longBuffer = new byte[8];

            System.arraycopy(givenBuffer, 0, longBuffer, 0, 8);

            long packetID = byteArray2Long(longBuffer);

            return packetID;

        } else {

            byte[] longBuffer = new byte[8];

            System.arraycopy(givenBuffer, 8, longBuffer, 0, 8);

            long packetID = byteArray2Long(longBuffer);

            //System.out.println("longBuffer : " + Arrays.toString(longBuffer));
            return packetID;
        }

    }

    public long getTotalFromBuffer(byte[] givenBuffer) {

        byte[] totalBuffer = new byte[8];

        System.arraycopy(givenBuffer, 0, totalBuffer, 0, 8);

        long totalNum = byteArray2Long(totalBuffer);

        return totalNum;
    }

    public byte[] stripPacket(byte[] givenBuffer) {

        if (type != SocketType.Type4) {
            byte[] newArray = new byte[givenBuffer.length - 8];

            System.arraycopy(givenBuffer, 8, newArray, 0, newArray.length);

            return newArray;
        } else {

            byte[] newArray = new byte[givenBuffer.length - 16];

            System.arraycopy(givenBuffer, 16, newArray, 0, newArray.length);

       // System.out.println("newArray : " + Arrays.toString(newArray));
            byte[] totalArray = new byte[8];

            System.arraycopy(givenBuffer, 0, totalArray, 0, totalArray.length);

        //System.out.println(Arrays.toString(totalArray));
            return newArray;
        }

    }

    ArrayList<CustomPacket> list1 = new ArrayList<CustomPacket>();
    ArrayList<CustomPacket> list2 = new ArrayList<CustomPacket>();

    /**
     * Method to fix voice issues on the receiving side.
     *
     * @param type
     * @param packet
     * @throws java.io.IOException
     */
    int frameNo = 0;
    int addedPacketTotal = 0;
    int currentMax = interleaveNumber;
    int squareRoot = (int) Math.sqrt(interleaveNumber);
    int seqNo = 1;
    int index = 1;

    int error = 0;
    int testNo = 0;
    int errorExpected = 0;
    int ceilingMax = 100;
    int ceilingMin = 32;

    CustomPacket[] sortArray = new CustomPacket[interleaveNumber];

    byte[] empty = new byte[512];

    CustomPacket emptyPacket = new CustomPacket(0, empty);

    CustomPacket prev = null;

    public void fixVoice(SocketType type, DatagramPacket packet) throws IOException {

        //System.out.println(packet.getData().length);
        long totalNum = getTotalFromBuffer(packet.getData());

        //System.out.println("TOTALNM : " + totalNum);
        byte[] arrayToPlay = stripPacket(packet.getData());

        CRC32 checker = new CRC32();

        checker.update(arrayToPlay);

        long comparison = checker.getValue();
        //System.out.println("COMP : " + comparison);
        // System.out.println(arrayToPlay.length);

        CustomPacket current = new CustomPacket(getNumberFromBuffer(packet.getData()), arrayToPlay);

        switch (type) {

            case Type1:

                player.playBlock(current.getPacketData());
                break;

            case Type2:

                if (!interleave) {
                    player.playBlock(current.getPacketData());

                } else {
                    // System.out.println(seqNo + " " + current.packetID);
                    if (current.packetID <= (seqNo * interleaveNumber)) {
                        sortArray[(int) (current.packetID - (((seqNo - 1) * interleaveNumber)) - 1)] = current;

                        //System.out.println(current.packetID+ " " + seqNo);
                    } else {

                        for (int k = 0; k < sortArray.length; k++) {
                            if (sortArray[k] != null) {
                                //  System.out.println(pckt.packetID + " " + seqNo);
                                player.playBlock(sortArray[k].packetData);
                            } else {
                                if (k > 0 && sortArray[k - 1] != null) {
                                    player.playBlock(sortArray[k - 1].packetData);
                                } else if (k > 1 && sortArray[k - 2] != null) {
                                    player.playBlock(sortArray[k - 2].packetData);
                                } else if (k > 2 && sortArray[k - 3] != null) {
                                    player.playBlock(sortArray[k - 3].packetData);
                                } else {
                                    player.playBlock(emptyPacket.packetData);
                                }
                            }
                        }

                        seqNo++;

                        sortArray = new CustomPacket[interleaveNumber];

                        if (current.packetID > ((seqNo) * interleaveNumber)) {
                            seqNo++;
                        }

                        sortArray[(int) (current.packetID - (((seqNo - 1) * interleaveNumber)) - 1)] = current;

                    }

                }

                break;
            case Type3:

                expected++;
                
                if (current.packetID != expected) {
                    if (prev != null && (prev.packetID - current.packetID == 1)) {  
                       // System.out.println("playing : " + current.packetID);
                        player.playBlock(current.packetData);
                      //  System.out.println("playing : " + prev.packetID);
                        player.playBlock(prev.packetData);
                        prev = null;
                    }
                    else if(prev == null)
                    {
                        prev = current;
                    }
                    else
                    {
                        //System.out.println("playing : " + current.packetID);
                        player.playBlock(current.packetData);
                    }
                   
                } else {
                  //  System.out.println("playing : " + current.packetID);
                    player.playBlock(current.packetData);
                }

                

                //System.out.println("Current : " + current);

                break;

            case Type4:

                if (totalNum == comparison) {
                    player.playBlock(current.packetData);
                    previous = current;
                } else if (previous != null) {
                    player.playBlock(previous.packetData);
                }

        }

    }

    CustomPacket[][] myArray = new CustomPacket[squareRoot][squareRoot];
    int i = squareRoot - 1;
    int j = 0;

    /**
     *
     * @param PORT
     * @param buffer
     * @param clientIP
     * @param number
     * @throws IOException
     */
    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP, int number) throws IOException {

        if (type == SocketType.Type4) {

            CRC32 checker = new CRC32();

            checker.update(buffer);

            long sum = checker.getValue();

            buffer = giveNumberToBuffer(buffer, number);

            CustomPacket packetToSend = new CustomPacket(getNumberFromBuffer(buffer), buffer);

            //System.out.println("BEFORE :" + sum);
            //System.out.println("Sum : " + sum);
            byte[] sumBuffer = long2ByteArray(sum);

            byte[] bufferToSend = mergeArrays(sumBuffer, buffer);

            packetToSend.setPacketData(bufferToSend);

            //System.out.println("bufferToSend : " + Arrays.toString(bufferToSend));
            //System.out.println(packetToSend.packetData.length);
            DatagramPacket newPacket = packetToSend.getPacket(clientIP, PORT);

            //System.out.println("bufferToSend : " + Arrays.toString(bufferToSend));
            sendingSocket.send(newPacket);
        } else if (interleave && type == SocketType.Type2) {

            buffer = giveNumberToBuffer(buffer, number);

            CustomPacket packetToSend = new CustomPacket(getNumberFromBuffer(buffer), buffer);

            myArray[i][j] = packetToSend;

            if (i >= 0) {
                i--;
            }

            if (i == -1) {
                i = squareRoot - 1;
                j++;
            }

            if (j == squareRoot) {
                j = 0;
            }

            if ((number % interleaveNumber == 0) && interleave) {

                for (CustomPacket[] myArray1 : myArray) {
                    for (int l = 0; l < myArray.length; l++) {
                        DatagramPacket packet = myArray1[l].getPacket(clientIP, PORT);
                        sendingSocket.send(packet);
                    }
                }
            }

        } else {

            buffer = giveNumberToBuffer(buffer, number);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

            sendingSocket.send(packet);

        }

        ////////////////
    }

    /**
     *
     * @param buffer
     * @param bufferSize
     * @throws IOException
     */
    int expected = 0;
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
        ///////////////////////////////
    }

}
