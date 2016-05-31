import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexUtils;
import org.apache.commons.math.transform.FastFourierTransformer;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Output;

public class SinglePlay implements Runnable {

	// Sound related storage
	byte[] 			inputBytes;
	double[] 		inputSamples;
    double[] 		input;

    double[][] 		inputSampleFrames;
    double[][] 		FourierFrames;
    double[] 		accum;
    double[] 		hann;
    Double 			dilation;

    // Sound related objects
	AudioInputStream 		AIS;
	SourceDataLine 			line;
	SourceDataLine.Info 	Info;
    SourceDataLine 			SourceDataLine;
    AudioBuffer 			processedData;
    static AudioFormat 		Format;
	File 					soundFile;	
	FloatControl 			gain;

	int frameSize = 1024;
	DoubleFFT_1D fft2 = new DoubleFFT_1D(frameSize * 1); // space for both real and imag

	Boolean 		playing 			= false;
	boolean 		repeating			= false;
	boolean 		stayLocked			= false;
	int 			index				= 0;
	DilationRate	dilationRate;
	DilationPlaying	dilationPlaying;
	float			gainValue;
	
	JButton			playButton;
	JButton			finishButton;
	StringBuilder			logFile;

	public SinglePlay(File soundFile, DilationRate dilRate, 
					  JButton playButton, JButton finishButton, 
					  float gainValue, boolean repeating, 
					  DilationPlaying dilPlay, boolean stayLocked, 
					  StringBuilder logFile) {
		
		this.soundFile = soundFile;
    	this.dilationRate = dilRate;
    	this.dilationPlaying = dilPlay;
    	this.playButton = playButton;
    	this.finishButton = finishButton;
    	this.gainValue = gainValue;
    	this.repeating = repeating;
    	this.stayLocked = stayLocked;
    	this.logFile = logFile;
    	playButton.setEnabled(false);
    	finishButton.setEnabled(false);
    	this.logFile = logFile;

	}

	public SinglePlay(File soundFile, double dilRate, 
					  JButton playButton, JButton finishButton, 
					  float gainValue, boolean repeating, 
					  DilationPlaying dilPlay, boolean stayLocked, 
					  StringBuilder logFile) {
		this.soundFile = soundFile;
    	this.dilationRate = new DilationRate(dilRate);
    	this.dilationPlaying = dilPlay;
    	this.playButton = playButton;
    	this.finishButton = finishButton;
    	this.gainValue = gainValue;
    	this.repeating = repeating;
    	this.stayLocked = stayLocked;
    	this.logFile = logFile;
    	playButton.setEnabled(false);
    	finishButton.setEnabled(false);
    	this.logFile = logFile;
	}

	public void setDilation(Double dil) {
		this.dilation = dil;
	}
	

	public void run() {
			try {
				
				AIS = AudioSystem.getAudioInputStream(soundFile);
				Format = AIS.getFormat();
				Info = new DataLine.Info(SourceDataLine.class, AIS.getFormat(),
			        ((int) AIS.getFrameLength() * Format.getFrameSize()));
				line = (SourceDataLine) AudioSystem.getLine(Info);
				line.open(Format, (int) Format.getSampleRate() / 10);
				line.start();

				inputSamples = new double[(int) AIS.getFrameLength()]; 				
				input = Utils.readAsBytes(inputSamples, gainValue, AIS);

				int frameWidth = 1024;
				int numSamples = + Array.getLength(inputSamples);
				int numFrames = (int) (Math.floor(numSamples - 3*frameWidth/4)/256);
				FourierFrames 		= new double[numFrames][frameWidth*2]; // in-place
				accum = new double[frameWidth];

				Utils.preProcessFrames(inputSamples, FourierFrames, accum);		// does a bunch of FFTs and other organizations

		        processedData = new AudioBuffer(20);
				gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
				gain.setValue(gainValue);
			} catch (UnsupportedAudioFileException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace();
			} catch (LineUnavailableException e) { e.printStackTrace();
			}

        // Create the first four subframes (256 * 4 = 1024 samples = 2048 bytes

		playButton.setEnabled(false);
        int frameSize = 1024;
        
        Vector<double[]> subFrames = new Vector<double[]>(4);
        subFrames.add(new double[frameSize/4]); subFrames.add(new double[frameSize/4]); 
        subFrames.add(new double[frameSize/4]); subFrames.add(new double[frameSize/4]); 

        double interpolation = 0;
        int currentFrame = 0;
        double outMag;
        double deltaPhase;            
        double[] holdingFrame = new double[frameSize * 2];  // will be argument to fft2

        double[] hann = Utils.initHann(1024);
        Complex[] dilatedFrame = new Complex[frameSize]; // 1024
        byte[] outputBytes = new byte[frameSize/2]; // 512

        System.gc();
      	while (processedData.lbq.isEmpty()) {
        	  double frac1 = 1 - interpolation;
        	  double frac2 = 0 + interpolation;

        	  for (int i = 0; i < frameSize; i++) {

        		  outMag = 	frac1 * Utils.getMag(FourierFrames[currentFrame], i)
        				  + frac2 * Utils.getMag(FourierFrames[currentFrame+1], i);

          		  deltaPhase = Utils.getArg(FourierFrames[currentFrame+1], i)
          		  			 - Utils.getArg(FourierFrames[currentFrame], i);
          		  
        		  while (deltaPhase >  Math.PI) { deltaPhase = deltaPhase - Math.PI * 2;}
        		  while (deltaPhase < -Math.PI) { deltaPhase = deltaPhase + Math.PI * 2;}
        		  accum[i] = accum[i] + deltaPhase;

        		  holdingFrame[i*2] 	= outMag * Math.cos(accum[i]);
        		  holdingFrame[i*2 + 1] = outMag * Math.sin(accum[i]);
        	  }
        	  
            // the following frame contains 1024 samples of interpolated frame
            //  in complex form.  Must be allocated to subframes properly.
        	fft2.complexInverse(holdingFrame, true);


            for (int i = 0; i < frameSize/4; i++) {
 
            	// Add the hann-windowed real values into the subFrames
            	subFrames.elementAt(0)[i] = subFrames.elementAt(0)[i] + holdingFrame[2*i + 0 * frameSize/2] * hann[i + 0 * frameSize/4] * 2 / 3;
            	subFrames.elementAt(1)[i] = subFrames.elementAt(1)[i] + holdingFrame[2*i + 1 * frameSize/2] * hann[i + 1 * frameSize/4] * 2 / 3;
            	subFrames.elementAt(2)[i] = subFrames.elementAt(2)[i] + holdingFrame[2*i + 2 * frameSize/2] * hann[i + 2 * frameSize/4] * 2 / 3;
            	subFrames.elementAt(3)[i] = subFrames.elementAt(3)[i] + holdingFrame[2*i + 3 * frameSize/2] * hann[i + 3 * frameSize/4] * 2 / 3;
            }
            
            // assume subFrame element 0 is complete; change it back to bytes and send to audio buffer
            Utils.samplesToBytes(outputBytes, subFrames.elementAt(0));
            line.write(outputBytes, 0, outputBytes.length);
            subFrames.removeElementAt(0);
            subFrames.add(new double[frameSize/4]); 
            interpolation = interpolation + dilationRate.rate ;
            if (interpolation > 1) {
                interpolation = interpolation - 1;
                currentFrame++;
            }
            if ( !dilationPlaying.isPlaying() || (((currentFrame+1) == FourierFrames.length) )  ) {
            	if (!stayLocked) { playButton.setEnabled(true); }
            	finishButton.setEnabled(true);
            	dilationPlaying.stop();
            	synchronized (logFile) {
//            		System.out.println(logFile.toString());
        			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
            		logFile.append(timeStamp + ": stop \n");
//            		System.out.println(logFile.toString());
            	}
            	return;
            }
            if ((currentFrame+1) == FourierFrames.length) currentFrame=0;
      	}
    }          
}
