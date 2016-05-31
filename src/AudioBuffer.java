import java.io.ByteArrayOutputStream;
import java.util.concurrent.LinkedBlockingQueue;

//  This is a very simple AudioBuffer
//  It is designed to hold subFrames of data, 
//  Where each subFrame is an array of 512 bytes in wav format
//  When the Listener takes the subFrames out, they will get transformed to proper samples

public class AudioBuffer {

  ByteArrayOutputStream out;
  int value;
  int size;

  LinkedBlockingQueue lbq;
  public String name;

  public AudioBuffer(Integer size) {
    // Size is the total number of array subFrames available to the application
    lbq = new LinkedBlockingQueue<byte[]>(size);
    out = new ByteArrayOutputStream();
    this.size = size;
  }

  public synchronized void addData(byte[] data) throws InterruptedException {
    // adds array of 512 bytes (256 samples) of sound data to the buffer
    // increments status bar by appropriate amount, normalized to number of
    // arrays
    lbq.put(data);
  }

  public synchronized byte[] readData() throws InterruptedException {
    // subtracts array of 512 bytes (256 samples) of sound data to the buffer
    // decrements status bar by appropriate amount, normalized to number of
    // arrays
    byte[] data = new byte[512];
    data = (byte[]) lbq.take();
    return data;
  }
}

