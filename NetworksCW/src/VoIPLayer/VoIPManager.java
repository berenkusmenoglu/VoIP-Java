package VoIPLayer;

import CMPC3M06.AudioPlayer;
import static VoIPLayer.CustomPacket.getNumberFromBuffer;
import static VoIPLayer.CustomPacket.getTotalFromBuffer;
import static VoIPLayer.CustomPacket.giveNumberToBuffer;
import static VoIPLayer.CustomPacket.long2ByteArray;
import static VoIPLayer.CustomPacket.mergeArrays;
import static VoIPLayer.CustomPacket.stripPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
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
    private final boolean processing = false;
    private boolean first = true;

    private final int INTERLEAVE_NO = 16;
    private final int SORT_PERIOD_NO = 8;

    private DatagramSocket sendingSocket;
    private DatagramSocket receivingSocket;
    private AudioPlayer player;
    private SocketType type;

    private int squareRoot = (int) Math.sqrt(INTERLEAVE_NO);
    private int seqNo = 1;
    private int index = 1;
    private int seq = 0;
    private int i = squareRoot - 1;
    private int j = 0;
    private long savedNo = 0;

    private CustomPacket[] sortArray = new CustomPacket[INTERLEAVE_NO];
    private ArrayList<CustomPacket> receivedPackets = new ArrayList<CustomPacket>();
    private byte[] empty = new byte[512];

    private CustomPacket emptyPacket = new CustomPacket(0, empty);
    private CustomPacket prev = null;
    private CustomPacket[][] myArray = new CustomPacket[squareRoot][squareRoot];
    private CustomPacket previous = new CustomPacket(0, new byte[512]);

    public VoIPManager(SocketType type) {
        this.type = type;

    }

    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP, int number) throws IOException {

        if (type == SocketType.Type4) {

            CRC32 checker = new CRC32();

            checker.update(buffer);

            long sum = checker.getValue();

            buffer = giveNumberToBuffer(buffer, number);

            CustomPacket packetToSend = new CustomPacket(getNumberFromBuffer(buffer, type), buffer);

            byte[] sumBuffer = long2ByteArray(sum);

            byte[] bufferToSend = mergeArrays(sumBuffer, buffer);

            packetToSend.setPacketData(bufferToSend);

            DatagramPacket newPacket = packetToSend.getPacket(clientIP, PORT);

            sendingSocket.send(newPacket);

        } else if (interleave && type == SocketType.Type2) {

            buffer = giveNumberToBuffer(buffer, number);

            CustomPacket packetToSend = new CustomPacket(getNumberFromBuffer(buffer, type), buffer);

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

            if ((number % INTERLEAVE_NO == 0) && interleave) {

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

    }

    public void ReceiveVoice(byte[] buffer, int bufferSize) throws IOException {

        DatagramPacket packet = new DatagramPacket(buffer, 0, bufferSize);

        receivingSocket.receive(packet);

        fixVoice(type, packet);

    }

    public void setType(SocketType type) {
        this.type = type;
    }

    public SocketType getType() {
        return this.type;
    }

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

    public void fixVoice(SocketType type, DatagramPacket packet) throws IOException {

        long totalNum = getTotalFromBuffer(packet.getData());

        byte[] arrayToPlay = stripPacket(packet.getData(), type);

        CRC32 checker = new CRC32();

        checker.update(arrayToPlay);

        long comparison = checker.getValue();

        CustomPacket current = new CustomPacket(getNumberFromBuffer(packet.getData(), type), arrayToPlay);

        switch (type) {

            case Type1:

                player.playBlock(current.getPacketData());
                break;

            case Type2:
                if (processing) {

                    if (first) {
                        seqNo = (int) (current.packetID / INTERLEAVE_NO);
                        first = false;

                    }

                    if (!interleave) {
                        player.playBlock(current.getPacketData());

                    } else {

                        if (current.packetID <= (seqNo * INTERLEAVE_NO)) {
                            sortArray[(int) (current.packetID - (((seqNo - 1) * INTERLEAVE_NO)) - 1)] = current;

                        } else {

                            for (int k = 0; k < sortArray.length; k++) {
                                if (sortArray[k] != null) {

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

                            sortArray = new CustomPacket[INTERLEAVE_NO];

                            if (current.packetID > ((seqNo) * INTERLEAVE_NO)) {
                                seqNo++;
                            }

                            sortArray[(int) (current.packetID - (((seqNo - 1) * INTERLEAVE_NO)) - 1)] = current;

                        }

                    }
                } else {
                    player.playBlock(current.packetData);
                }

                break;

            case Type3:

                if (processing) {

                    receivedPackets.add(current);

                    if (receivedPackets.size() % SORT_PERIOD_NO == 0) {

                        Collections.sort(receivedPackets, new CustomPacket.PacketComparator());

                        for (int i = 0; i < receivedPackets.size(); i++) {

                            if (receivedPackets.get(i).packetID >= savedNo) {

                                player.playBlock(receivedPackets.get(i).packetData);

                            } else if (i > 0) {

                                if (receivedPackets.get(i - 1) != null) {

                                    player.playBlock(receivedPackets.get(i - 1).packetData);
                                }
                            } else if (i == 0) {

                                player.playBlock(emptyPacket.packetData);
                            }

                            if (i == receivedPackets.size() - 1) {
                                savedNo = current.packetID;
                            }

                        }

                        receivedPackets.clear();
                        seq++;
                    }
                } else {
                    player.playBlock(current.packetData);
                }

                break;

            case Type4:

                if (processing) {
                    if (totalNum == comparison) {
                        player.playBlock(current.packetData);
                        previous = current;
                    } else if (previous != null) {

                        player.playBlock(previous.packetData);
                    }

                } else {
                    player.playBlock(current.packetData);
                }

        }

    }

}