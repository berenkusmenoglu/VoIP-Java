package VoIPLayer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Comparator;
import networkscw.NetworksCW;
import networkscw.NetworksCW.SocketType;

class CustomPacket implements Comparable<CustomPacket> {

    public long packetID;
    public byte[] packetData;

    CustomPacket(long numberFromBuffer, byte[] arrayToPlay) {
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

    public static class PacketComparator implements Comparator<CustomPacket> {

        @Override
        public int compare(CustomPacket customPacket1, CustomPacket customPacket2) {
            return (int) (customPacket1.getPacketID() - customPacket2.getPacketID());
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

    public static byte[] mergeArrays(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static byte[] giveNumberToBuffer(byte[] givenBuffer, long num) {

        byte[] packetIdentifier = long2ByteArray(num);
        byte[] mergedArray = mergeArrays(packetIdentifier, givenBuffer);

        return mergedArray;

    }

    public static long getNumberFromBuffer(byte[] givenBuffer, SocketType type) {

        if (type != NetworksCW.SocketType.Type4) {

            byte[] longBuffer = new byte[8];

            System.arraycopy(givenBuffer, 0, longBuffer, 0, 8);

            long packetID = byteArray2Long(longBuffer);

            return packetID;

        } else {

            byte[] longBuffer = new byte[8];

            System.arraycopy(givenBuffer, 8, longBuffer, 0, 8);

            long packetID = byteArray2Long(longBuffer);

            return packetID;
        }

    }

    public static long getTotalFromBuffer(byte[] givenBuffer) {

        byte[] totalBuffer = new byte[8];

        System.arraycopy(givenBuffer, 0, totalBuffer, 0, 8);

        long totalNum = byteArray2Long(totalBuffer);

        return totalNum;
    }

    public static byte[] stripPacket(byte[] givenBuffer, SocketType type) {

        if (type != NetworksCW.SocketType.Type4) {
            byte[] newArray = new byte[givenBuffer.length - 8];

            System.arraycopy(givenBuffer, 8, newArray, 0, newArray.length);

            return newArray;
        } else {

            byte[] newArray = new byte[givenBuffer.length - 16];
            System.arraycopy(givenBuffer, 16, newArray, 0, newArray.length);
            byte[] totalArray = new byte[8];
            System.arraycopy(givenBuffer, 0, totalArray, 0, totalArray.length);

            return newArray;
        }

    }

}
