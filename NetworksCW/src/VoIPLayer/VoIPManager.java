package VoIPLayer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Beren
 */
public class VoIPManager {

    public  byte[] intsToBytes(int[] ints) {
        ByteBuffer bb = ByteBuffer.allocate(ints.length * 4);
        IntBuffer ib = bb.asIntBuffer();
        for (int i : ints) {
            ib.put(i);
        }
        return bb.array();
    }

    public  int[] bytesToInts(byte[] bytes) {
        int[] ints = new int[bytes.length / 4];
        ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
        return ints;
    }
}
