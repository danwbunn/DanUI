
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JLabel;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.complex.ComplexUtils;
import org.apache.commons.math.transform.FastFourierTransformer;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class DualPlay implements Runnable {

	// Sound related storage
	byte[] 			inputBytes;
	double[] 		inputSpeechSamples;
	double[] 		inputNoiseSamples;
    double[] 		speechSamples;
    double[] 		noiseSamples;
    double[] 		speechInput;
    double[] 		noiseInput;

    double[][] 		inputSampleFrames;
    double[][] 	speechFourierFrames;
    double[][] 	noiseFourierFrames;
    double[] 		speechAccumulator;
    double[] 		noiseAccumulator;
    double[] 		hann;
    Double 			dilation;

    // Sound related objects
	AudioInputStream 		speechAIS;
	SourceDataLine 			speechLine;
	SourceDataLine.Info 	speechInfo;
    SourceDataLine 			SourceDataLine;
    AudioBuffer 			speechProcessedData;
    AudioBuffer 			noiseProcessedData;
    static AudioFormat 		speechFormat;
	File 					speechFile;	
	AudioInputStream 		noiseAIS;
	SourceDataLine 			noiseLine;
	SourceDataLine.Info 	noiseInfo;
    static AudioFormat 		noiseFormat;
	File 					noiseFile;	
	double 					speechGain;
	double					noiseGain;
	FloatControl 			gain;

    FastFourierTransformer fft = new FastFourierTransformer();
	Boolean 		playing 			= false;
	boolean 		hearingAid			= false;
	boolean 		stayLocked			= false;
	int 			index				= 0;
	DilationRate	dilationRateSpeech;
	DilationRate	dilationRateNoise;
	DilationPlaying	dilationPlaying;
	float			gainValue;
	
	JButton			playButton;
	JButton			finishButton;
	JLabel			label;
	String			labelString;
	StringBuilder			logFile;

	// use this one
	public DualPlay(File speechFile, Double speechGain, 
			File noiseFile, Double noiseGain, 
			DilationRate dilRateSpeech, DilationRate dilRateNoise, 
			JButton playButton, JButton finishButton, 
			boolean hearingAid, DilationPlaying dilPlay, 
			boolean stayLocked, StringBuilder logFile, 
			JLabel label, String labelString) {

	this.speechFile = speechFile;
	this.speechGain = speechGain;
	this.noiseFile = noiseFile;
	this.noiseGain = noiseGain;
	this.dilationPlaying = dilPlay;
	this.dilationRateSpeech = dilRateSpeech;
	this.dilationRateNoise = dilRateNoise;
	this.playButton = playButton;
	this.finishButton = finishButton;
	this.hearingAid = hearingAid;  // hearing aid true --> dilation rates co-vary.
	this.stayLocked = stayLocked;
	this.logFile = logFile;
	this.label = label;
	this.labelString = labelString;
	playButton.setEnabled(false);
	finishButton.setEnabled(false);

}
	public DualPlay(File speechFile, float speechGain, 
					File noiseFile, float noiseGain, 
					DilationRate dilRateSpeech, DilationRate dilRateNoise, 
					JButton playButton, JButton finishButton, 
					boolean hearingAid, DilationPlaying dilPlay, 
					boolean stayLocked, StringBuilder logFile, 
					JLabel label, String labelString) {

		this.speechFile = speechFile;
		this.speechGain = speechGain;
		this.noiseFile = noiseFile;
		this.noiseGain = noiseGain;
    	this.dilationPlaying = dilPlay;
    	this.dilationRateSpeech = dilRateSpeech;
    	this.dilationRateNoise = dilRateNoise;
    	this.playButton = playButton;
    	this.finishButton = finishButton;
    	this.hearingAid = hearingAid;  // hearing aid true --> dilation rates co-vary.
    	this.stayLocked = stayLocked;
    	this.logFile = logFile;
    	this.label = label;
    	this.labelString = labelString;
    	playButton.setEnabled(false);
    	finishButton.setEnabled(false);
    	
	}

	public DualPlay(File speechFile, double speechGain, 
					File noiseFile, double noiseGain, 
					DilationRate dilRateSpeech, DilationRate dilRateNoise, 
					JButton playButton, JButton finishButton, 
					boolean hearingAid, DilationPlaying dilPlay, 
					boolean stayLocked, StringBuilder logFile) {

		this.speechFile = speechFile;
		this.speechGain = speechGain;
		this.noiseFile = noiseFile;
		this.noiseGain = noiseGain;
    	this.dilationPlaying = dilPlay;
    	this.dilationRateSpeech = dilRateSpeech;
    	this.dilationRateNoise = dilRateNoise;
    	this.playButton = playButton;
    	this.finishButton = finishButton;
    	this.hearingAid = hearingAid;  // hearing aid true --> dilation rates co-vary.
    	this.stayLocked = stayLocked;
    	this.logFile = logFile;
    	playButton.setEnabled(false);
    	finishButton.setEnabled(false);
	}

	public void setDilation(Double dil) { this.dilation = dil; }
	
	public void run() {
		int frameWidth = 1024;

			try {

				speechAIS = AudioSystem.getAudioInputStream(speechFile);
				speechFormat = speechAIS.getFormat();
				speechInfo = new DataLine.Info(SourceDataLine.class, speechAIS.getFormat(),
			        ((int) speechAIS.getFrameLength() * speechFormat.getFrameSize()));
				speechLine = (SourceDataLine) AudioSystem.getLine(speechInfo);
				// This sets the buffer size
				// Seems to be related to the audio glitching -- bigger is better
				// Might be related to latency -- smaller is better -- not sure about this one
				// speechLine.open(speechFormat, (int) speechFormat.getSampleRate() * 1);
				speechLine.open(speechFormat, (int) 44100/10);
				speechLine.start();

				inputSpeechSamples = new double[(int) speechAIS.getFrameLength()]; 				

				speechInput = Utils.readAsBytes(inputSpeechSamples, speechGain, speechAIS);
				int numSpeechSamples = + Array.getLength(inputSpeechSamples);
				int numSpeechFrames = (int) (Math.floor(numSpeechSamples - 3*frameWidth/4)/256);
				speechFourierFrames 		= new double[numSpeechFrames][frameWidth*2]; // in-place

				speechAccumulator = new double[1024];
				Utils.preProcessFrames(inputSpeechSamples, speechFourierFrames, speechAccumulator);		// does a bunch of FFTs and other organizations

		        speechProcessedData = new AudioBuffer(200);
		        // why was this here?  Debugging?
			} catch (UnsupportedAudioFileException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace();
			} catch (LineUnavailableException e) { e.printStackTrace();
			}

			try {

				noiseAIS = AudioSystem.getAudioInputStream(noiseFile);
				noiseSamples = new double[(int) noiseAIS.getFrameLength()]; 				
				inputNoiseSamples = new double[(int) noiseAIS.getFrameLength()]; 				
				noiseInput = Utils.readAsBytes(inputNoiseSamples, noiseGain, noiseAIS);
				int numNoiseSamples = + Array.getLength(inputNoiseSamples);
				int numNoiseFrames = (int) (Math.floor(numNoiseSamples - 3*frameWidth/4)/256);
				noiseFourierFrames 		= new double[numNoiseFrames][frameWidth*2]; // in-place

				noiseAccumulator = new double[1024];
				Utils.preProcessFrames(inputNoiseSamples, noiseFourierFrames, noiseAccumulator);		// does a bunch of FFTs and other organizations

		        noiseProcessedData = new AudioBuffer(200);
			} catch (UnsupportedAudioFileException e) { e.printStackTrace();
			} catch (IOException e) { e.printStackTrace();
			}

		    		    

        // Create the first four subframes (256 * 4 = 1024 samples = 2048 bytes

		playButton.setEnabled(false);
        int frameSize = 1024;
        
        Vector<double[]> speechSubFrames = new Vector<double[]>(4);
        speechSubFrames.add(new double[frameSize/4]); speechSubFrames.add(new double[frameSize/4]); 
        speechSubFrames.add(new double[frameSize/4]); speechSubFrames.add(new double[frameSize/4]); 

        Vector<double[]> noiseSubFrames = new Vector<double[]>(4);
        noiseSubFrames.add(new double[frameSize/4]); noiseSubFrames.add(new double[frameSize/4]); 
        noiseSubFrames.add(new double[frameSize/4]); noiseSubFrames.add(new double[frameSize/4]); 

        double speechInterpolation = 0;
        double noiseInterpolation = 0;

        int speechCurrentFrame = 0;
        int noiseCurrentFrame = 0;
        
        double speechOutMag; double speechDeltaPhase;            
        double noiseOutMag;	 double noiseDeltaPhase;            

        double[] hann = Utils.initHann(1024);

        double[] speechHoldingFrame = new double[frameSize * 2]; // 1024
        double[] noiseHoldingFrame = new double[frameSize * 2]; // 1024

        Complex[] speechDilatedFrame = new Complex[frameSize]; // 1024
        Complex[] noiseDilatedFrame = new Complex[frameSize]; // 1024

        byte[] speechOutputBytes = new byte[frameSize/2]; // 512
        byte[] noiseOutputBytes = new byte[frameSize/2]; // 512
        byte[] addedOutputBytes = new byte[frameSize/2]; // 512
        double[] addedSamples = new double[frameSize/4];

        // 50 subframes is about 0.3 seconds of audio
        // Good grief, this is never filled; real halt condition is later in loop
//      	while (speechProcessedData.lbq.size()<50 || noiseProcessedData.lbq.size()<50) {
        // classic codeSmell:  Invoking the garbage collector in Java
        // Will remove these later after I have a chance to test properly
        System.gc();
          	while (speechProcessedData.lbq.isEmpty() || noiseProcessedData.lbq.isEmpty() ) {
          		
          		int tag = 0;
          		double speechFraction1 	= 1 - speechInterpolation;
		      	double speechFraction2 	= 0 + speechInterpolation;
		    	double noiseFraction1 	= 1 - noiseInterpolation;
		    	double noiseFraction2 	= 0 + noiseInterpolation;

		    	for (int i = 0; i < frameSize; i++) {
		    		
	        		  speechOutMag 	= speechFraction1 * Utils.getMag(speechFourierFrames[speechCurrentFrame], i)
	        				     	+ speechFraction2 * Utils.getMag(speechFourierFrames[speechCurrentFrame+1], i);

	          		  speechDeltaPhase 	= Utils.getArg(speechFourierFrames[speechCurrentFrame+1], i)
	          		  			 		- Utils.getArg(speechFourierFrames[speechCurrentFrame], i);

	        		  noiseOutMag 	= noiseFraction1 * Utils.getMag(noiseFourierFrames[noiseCurrentFrame], i)
	        				  		+ noiseFraction2 * Utils.getMag(noiseFourierFrames[noiseCurrentFrame+1], i);

	        		  noiseDeltaPhase 	= Utils.getArg(noiseFourierFrames[noiseCurrentFrame+1], i)
        		  			 			- Utils.getArg(noiseFourierFrames[noiseCurrentFrame], i);


        		  while (speechDeltaPhase >  Math.PI) { speechDeltaPhase = speechDeltaPhase - Math.PI * 2;}
        		  while (speechDeltaPhase < -Math.PI) { speechDeltaPhase = speechDeltaPhase + Math.PI * 2;}
        		  while (noiseDeltaPhase >  Math.PI) { noiseDeltaPhase = noiseDeltaPhase - Math.PI * 2;}
        		  while (noiseDeltaPhase < -Math.PI) { noiseDeltaPhase = noiseDeltaPhase + Math.PI * 2;}

        		  speechAccumulator[i] = speechAccumulator[i] + speechDeltaPhase;
        		  noiseAccumulator[i] = noiseAccumulator[i] + noiseDeltaPhase;

        		  speechHoldingFrame[i*2] 	= speechOutMag * Math.cos(speechAccumulator[i]);
        		  speechHoldingFrame[i*2 + 1] = speechOutMag * Math.sin(speechAccumulator[i]);

        		  noiseHoldingFrame[i*2] 	= noiseOutMag * Math.cos(noiseAccumulator[i]);
        		  noiseHoldingFrame[i*2 + 1] = noiseOutMag * Math.sin(noiseAccumulator[i]);
        	  }
        	  
            // the following frame contains 1024 samples of interpolated frame
            //  in complex form.  Must be allocated to subframes properly.
		    	DoubleFFT_1D fft2 = new DoubleFFT_1D(frameSize * 1); // space for both real and imag


	        	fft2.complexInverse(speechHoldingFrame, true);
	        	fft2.complexInverse(noiseHoldingFrame, true);

            for (int i = 0; i < frameSize/4; i++) {
            	
            	speechSubFrames.elementAt(0)[i] = speechSubFrames.elementAt(0)[i] + speechHoldingFrame[2*i + 0 * frameSize/2] * hann[i + 0 * frameSize/4] * 2 / 3;
            	speechSubFrames.elementAt(1)[i] = speechSubFrames.elementAt(1)[i] + speechHoldingFrame[2*i + 1 * frameSize/2] * hann[i + 1 * frameSize/4] * 2 / 3;
            	speechSubFrames.elementAt(2)[i] = speechSubFrames.elementAt(2)[i] + speechHoldingFrame[2*i + 2 * frameSize/2] * hann[i + 2 * frameSize/4] * 2 / 3;
            	speechSubFrames.elementAt(3)[i] = speechSubFrames.elementAt(3)[i] + speechHoldingFrame[2*i + 3 * frameSize/2] * hann[i + 3 * frameSize/4] * 2 / 3;

            	noiseSubFrames.elementAt(0)[i] = noiseSubFrames.elementAt(0)[i] + noiseHoldingFrame[2*i + 0 * frameSize/2] * hann[i + 0 * frameSize/4] * 2 / 3;
            	noiseSubFrames.elementAt(1)[i] = noiseSubFrames.elementAt(1)[i] + noiseHoldingFrame[2*i + 1 * frameSize/2] * hann[i + 1 * frameSize/4] * 2 / 3;
            	noiseSubFrames.elementAt(2)[i] = noiseSubFrames.elementAt(2)[i] + noiseHoldingFrame[2*i + 2 * frameSize/2] * hann[i + 2 * frameSize/4] * 2 / 3;
            	noiseSubFrames.elementAt(3)[i] = noiseSubFrames.elementAt(3)[i] + noiseHoldingFrame[2*i + 3 * frameSize/2] * hann[i + 3 * frameSize/4] * 2 / 3;
            }
            
            // assume subFrame element 0 is complete; change it back to bytes and send to audio buffer
            // Utils.samplesToBytes(speechOutputBytes, speechSubFrames.elementAt(0));
            // Utils.samplesToBytes(noiseOutputBytes, noiseSubFrames.elementAt(0));

            for (int i = 0; i < Array.getLength(speechSubFrames.elementAt(0)); i++) {
            	addedSamples[i] = speechSubFrames.elementAt(0)[i] + noiseSubFrames.elementAt(0)[i];            
            }
            Utils.samplesToBytes(addedOutputBytes, addedSamples);            
            
            speechLine.write(addedOutputBytes, 0, addedOutputBytes.length);
            speechSubFrames.removeElementAt(0);
            speechSubFrames.add(new double[frameSize/4]); 
            speechInterpolation = speechInterpolation + dilationRateSpeech.rate ;

            noiseSubFrames.removeElementAt(0);
            noiseSubFrames.add(new double[frameSize/4]); 
            noiseInterpolation = noiseInterpolation + dilationRateNoise.rate ;
            
            if (speechInterpolation >= 1) {
                speechInterpolation = speechInterpolation - 1;
                speechCurrentFrame++;
            }
            
            if (noiseInterpolation >= 1) {
                noiseInterpolation = noiseInterpolation - 1;
                noiseCurrentFrame++;
            }

            if (noiseCurrentFrame+1 == noiseFourierFrames.length) {
            	noiseCurrentFrame=1;
            }
            
            // this is where we really break out of the loop
            // there is a return statement at the end.
            // Sheesh
            if ( !dilationPlaying.isPlaying() || (((speechCurrentFrame+1) == speechFourierFrames.length) )  ) {
            	if (!stayLocked) { 
            		playButton.setEnabled(true); 
            		finishButton.setEnabled(false);
            		if (DanUI.Phase.equals("Practice")) {
                		finishButton.setEnabled(true);            			
            		}
            	}
            	if (stayLocked) { finishButton.setEnabled(true); }
            	if (label != null) { label.setText(labelString); }
            	dilationPlaying.stop();
            	synchronized (logFile) {
        			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
            		logFile.append(timeStamp + ": stop \n");
            		speechLine.drain();
            		speechLine.close();
            	}
            	return;
            }
            if ((speechCurrentFrame+1) == speechFourierFrames.length) speechCurrentFrame=0;
      	}
    }
}

