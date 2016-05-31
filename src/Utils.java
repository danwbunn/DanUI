import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Utils {

  public Utils() {

  }

 public static double getMag(double[] cArray, int index) {
	  return Math.sqrt(Math.pow(cArray[2*index], 2.0) + Math.pow(cArray[2*index+1], 2.0));
  }
  
  public static double getArg(double[] cArray, int index) {
	  return Math.atan2(cArray[2*index+1], cArray[2*index]); 
  }
  
  public static double[] initHann(int length) {
	    double[] hann = new double[length];
	    for (int i = 0; i < length; i++) {
	      hann[i] = 0.5 * (1 - Math.cos((2 * Math.PI * i / (length - 1))));
	    }
	    return hann;
	  }

  public static void copyRange(double[] from, double[] to, int from_start,
      int to_start, int num) {
    for (int i = 0; i < num; i++) {
      to[to_start + i] = from[from_start + i];
    }
  }

  public static void copyRange(Complex[] from, Complex[] to, int from_start,
      int to_start, int num) {
    for (int i = 0; i < num; i++) {
      to[to_start + i] = from[from_start + i];
    }
  }

  public static void windowTransform(double[] samples, Complex[] transform,
      double[] hann, FastFourierTransformer fft) {
    double[] temp = new double[hann.length];
    Complex[] temp2 = new Complex[hann.length];

    for (int i = 0; i < hann.length; i++) {
      temp[i] = samples[i] * hann[i];
    }

    transform = Arrays.copyOfRange(fft.transform(temp), 0, hann.length / 2);
    for (int i = 0; i < hann.length / 2; i++) {
    }
  }

  public static void bytesToSamples(byte[] bytes, double[] doubles) {
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = (double) (((bytes[2 * i + 1] & 0xFF) << 8) | (bytes[2 * i] & 0xFF)) / 32768;
      if (doubles[i] > 1.0) {
        doubles[i] = doubles[i] - 2.0;
      }
    }
  }

  public static void samplesToBytes(byte[] bytes, double[] doubles) {
    for (int i = 0; i < doubles.length; i++) {

      if (doubles[i] < 0) {
        doubles[i] = doubles[i] + 2;
      }
      short s = (short) (doubles[i] * 32768);
      bytes[2 * i + 0] = (byte) (s & 0xFF);
      bytes[2 * i + 1] = (byte) ((s >> 8) & 0xFF);
    }
  }

  public static AudioFormat buildGenericAudioFormat(int sampleRate) {
    int sampleSize = 16; // 16 bits ==> 2 bytes per sample
    int channels = 1; // mono channel format
    boolean signed = true; // signed PCM data
    boolean endian = false; // littleEndian Data
    // Instantiate the format object
    AudioFormat format = new AudioFormat(sampleRate, sampleSize, channels,
        signed, endian);
    return format;

  }

  /*
   * public static AudioFormat buildCDAudioFormat() { int sampleRate = 44100; //
   * 44.1 KHz, CD Quality int sampleSize = 16; // 16 bits ==> 2 bytes per sample
   * int channels = 1; // mono channel format boolean signed = true; // signed
   * PCM data boolean endian = false; // littleEndian Data // Instantiate the
   * format object AudioFormat format = new AudioFormat(sampleRate, sampleSize,
   * channels, signed, endian); return format; }
   */

	ArrayList<File>		personalDirs;
	ArrayList<File>		noiseFiles;
	String 				practiceDirectory = new String(DanUI.baseDirectory + "\\Materials\\Audio Clips\\Practice");

  public static void test(String string) {
    System.out.println("The string is : " + string);
    
  }

public static double[] readAsBytes(double[] inputSamples, double gain, AudioInputStream AIS) throws IOException {
	// Convert Wav format data to an array of double-formatted samples
	
	double gainFactor = (double) Math.pow(10, gain/20.0);
    int byteCursor = 0;
    int sampleCursor = 0;

    // initialize array lengths
    // inputSamples = new double[(int) AIS.getFrameLength()];
    byte[] inputBytes = new byte[512];

    while (512 < AIS.available()) {
      // read 512 byte-pairs into inputBytes
      AIS.read(inputBytes, 0, 512);
      // convert bytes to double
      for (int i = 0; i < 256; i++) {
        inputSamples[i + sampleCursor] = (double) (((inputBytes[2 * i + 1] & 0xFF) << 8) | (inputBytes[2 * i] & 0xFF)) / 32768;
        if (inputSamples[i + sampleCursor] > 1.0) {
          inputSamples[i + sampleCursor] = inputSamples[i + sampleCursor] - 2.0;
        }
        inputSamples[i + sampleCursor] *= gainFactor;
      }
      // advance cursor
      byteCursor = byteCursor + 512;
      sampleCursor = sampleCursor + 256;
    }
    sampleCursor = 0;
    double rmsVal = 0;
    double[] samples = new double[256];

    while (sampleCursor + 256 < inputSamples.length) {
      for (int i = sampleCursor; i < sampleCursor + 256; i++) {
        rmsVal = rmsVal + Math.pow(Math.abs(inputSamples[i]), 2);
      }
      rmsVal = 20 * Math.log10(Math.sqrt(rmsVal / 256));
      rmsVal = 0;
      sampleCursor += 256;
    }
    return inputSamples;
  }

// needs to return both fourierFrames[][] and accum[]
// pass these by references, and make sure they are allocated prior to call
// This routine uses JTransforms FFT and complex numbering scheme, and is blisteringly fast. 

public static void preProcessFrames(double[] inputSamples, double[][] FourierFrames, double[] accum) {
	// this all needs to be converted to new method
	
	// Convert array of samples into:
	// 	Overlapping arrays of samples, and
	//  Overlapping frames of FFTs
	// These FFTs are full 1024-point complex value arrays

	int numSamples = + Array.getLength(inputSamples);
	int frameWidth = 1024;
	int numFrames = (int) (Math.floor(numSamples - 3*frameWidth/4)/256);
	
	DoubleFFT_1D fft2 = new DoubleFFT_1D(frameWidth); // space for both real and imag
	double[][] inputSampleFrames = new double[numFrames][frameWidth]; // in-place
	double[] hann = initHann(frameWidth);
	int currentFrame = 0; int cursor = 0;
	while (currentFrame < numFrames) {
		for (int i = 0; i < frameWidth; i++) {
			inputSampleFrames[currentFrame][i] = hann[i] * inputSamples[i+cursor];
			FourierFrames[currentFrame][i] = inputSampleFrames[currentFrame][i]; 
			FourierFrames[currentFrame][i+frameWidth] = 0; 
		}
		fft2.realForwardFull(FourierFrames[currentFrame]);
		currentFrame++; cursor+=256;
	}		
	
    for (int i = 0; i < frameWidth; i++) {
        accum[i] = Math.atan2(FourierFrames[0][2*i+1], FourierFrames[0][2*i]);
    }
}



}
