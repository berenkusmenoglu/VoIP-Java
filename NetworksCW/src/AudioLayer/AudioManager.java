package AudioLayer;

import java.util.Vector;
import java.util.Iterator;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;



/**
 *
 * @author Beren
 */
public class AudioManager {

   

    Vector<byte[]> voiceVector = new Vector<>();
    int i = 0;

    /**
     * Records audio for a given time period.
     *
     * @param time
     * @return
     * @throws LineUnavailableException
     * @throws IOException
     */
    public Vector<byte[]> RecordAudio(float time) throws LineUnavailableException, IOException {

        //Initialise AudioPlayer
        AudioRecorder recorder = new AudioRecorder();

        //Recording time in seconds
        float recordTime = time;

        //Capture audio data and add to voiceVector
        System.out.println("Recording Audio...");

        for (int i = 0; i < Math.ceil(recordTime / 0.032); i++) {
            byte[] buffer = recorder.getBlock();
            voiceVector.add(buffer);
        }

        //Close audio input
        recorder.close();

        return voiceVector;
    }

    /**
     * Plays a given audio file.
     *
     * @param voiceVector
     * @throws LineUnavailableException
     * @throws IOException
     */
    public void PlayAudio(Vector<byte[]> voiceVector) throws LineUnavailableException, IOException {
        AudioPlayer player = new AudioPlayer();
        System.out.println("Playing Audio...");

        Iterator<byte[]> voiceItr = voiceVector.iterator();
        while (voiceItr.hasNext()) {
            player.playBlock(voiceItr.next());
        }

        //Close audio output
        player.close();
    }

    public void PlayAudioByte(byte[] voiceByteArray) throws LineUnavailableException, IOException {
        voiceVector.add(voiceByteArray);
        AudioPlayer player = new AudioPlayer();
        System.out.println("Playing Audio... " + i);
        i++;

        Iterator<byte[]> voiceItr = voiceVector.iterator();
        while (voiceItr.hasNext()) {
            player.playBlock(voiceItr.next());
        }

        //Close audio output
        player.close();
    }
}
