import java.awt.Event;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;



public class TrainingUI extends JFrame implements ActionListener {

	// TrainingUI is a simple GUI for:
	//		Allowing the subject to familiarize him or herself with dilation (no noise) by listening to and adjusting a long sound clip

	JButton 		externalButton;

	JLabel 				WelcomeLabel;
	JButton 			playButton, finishButton;
	ArrayList<File>		practiceFiles, noiseFiles;
	JSlider				slider;

	File 				currentSoundFile, currentNoiseFile;
    DilationRate 		dilationRateSpeech, dilationRateNoise;
    DilationPlaying		dilationPlay;
	StringBuilder 		logFile = new StringBuilder();
	int 				positionInList = 0;
	int 				timesThruList = 0;

	// 		These are used for the dilation process itself
	double[] 			hann;
	byte[] 				inputBytes;
	double[] 			inputSamples;
    double[][] 			FourierFrames;
    double[] 			accum;
    double[] 			input;
    double[][] 			inputSampleFrames;
    
    FastFourierTransformer fft = new FastFourierTransformer();
	AudioInputStream 		AIS;
	SourceDataLine 			line;
	SourceDataLine.Info 	Info;
	static AudioFormat 		Format;
    
    public TrainingUI() { begin(); }
        
	public synchronized void begin()  {
		
		DanUI.Phase		=	new String("Training");
		System.out.println("Phase: " + DanUI.Phase);
		
		File directory = new File(DanUI.trainingDirectory);
		practiceFiles = new ArrayList<File>(Arrays.asList(directory.listFiles()));

		Collections.shuffle(practiceFiles);
		
		int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    final JPanel InitPanel = new JPanel();
	    InitPanel.setLayout(null);
	    Font font = new Font("Verdana", Font.BOLD, 40);

	    // Title Banner
	    WelcomeLabel = new JLabel("Training Phase");
	    WelcomeLabel.setBounds(0, 1*height/10, width, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    InitPanel.add(WelcomeLabel);

	    // Slider, from 40 to 101 (so 100 can be selected) starting at 70 for the training session.
	    // Note the SliderUI call and application to the slider
	    slider = new JSlider(40, 101, 70);
	    TallSliderUI tallUI = new TallSliderUI (slider, DanUI.scaleFactor); //scaleFactor for actual sizing in DANUI line 85
	    slider.setUI(tallUI);
	    //slider.setBounds(2*width/10, 4*height/10, 2500, tallUI.getThumbSize().height + tallUI.getTickLength());
	    slider.setBounds(width/10, 6*height/10, 1500, tallUI.getThumbSize().height + tallUI.getTickLength());
	    slider.setFont(font);
	    slider.setExtent(1);
	    slider.putClientProperty("JComponent.sizeVariant", "large");
	    slider.setMajorTickSpacing(10);
	    slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(false);
	    slider.setSnapToTicks(false);
	    InitPanel.add(slider);

	    // Variable stores the dilation rate
	    // Initially set to the slider value
	    dilationRateSpeech = new DilationRate( (double) slider.getValue() / 100 );
	    	    
	    // Play button-- locked when clip is playing, unlocked when clip ends
	    playButton = new JButton("Play");
	    playButton.setFont(font);
	    playButton.setBounds(width/10, 8*height/10, 500, 50);
	    playButton.setHorizontalAlignment(SwingConstants.CENTER);
	    playButton.putClientProperty("JComponent.sizeVariant", "large");
	    InitPanel.add(playButton);

	    // Finish button-- locked when clip is playing, unlocked when clip ends
	    finishButton = new JButton("Finish");
	    finishButton.setFont(font);
	    finishButton.setEnabled(false);
	    finishButton.setBounds(6*width/10, 8*height/10, 500, 50);
	    finishButton.setHorizontalAlignment(SwingConstants.CENTER);
	    InitPanel.add(finishButton);

	    getContentPane().add(InitPanel);
	    
	    JLabel InstrLabel = new JLabel("<html>Press play and listen to the audio. Adjust the dilation slider bar to experience the dilation technique. Move slider to the left-most position to hear the maximum dilation to compare against the right-most position to hear natural audio.</html>");
	    InstrLabel.setBounds(width/10, 2*height/10, 1500, 300);
	    InstrLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    InstrLabel.setFont(font);
	    InitPanel.add(InstrLabel);
	    
	    synchronized (logFile) {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
    		logFile.append(timeStamp + ": BEGIN TRAINING PHASE \n");
		}

	    setSize(width, height);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);

    	currentSoundFile = practiceFiles.get(positionInList);
    	
		try {
			AIS = AudioSystem.getAudioInputStream(currentSoundFile);
			Format = AIS.getFormat();
			Info = new DataLine.Info(SourceDataLine.class, AIS.getFormat(),
		        ((int) AIS.getFrameLength() * Format.getFrameSize()));
			line = (SourceDataLine) AudioSystem.getLine(Info);
			line.open(Format, (int) Format.getSampleRate() / 2);
			line.start();
			inputSamples = new double[(int) AIS.getFrameLength()]; 
			input = Utils.readAsBytes(inputSamples, 0, AIS);
		} catch (Exception e1) { e1.printStackTrace(); }
		int frameWidth = 1024;
		int numSamples = + Array.getLength(inputSamples);
		int numFrames = (int) (Math.floor(numSamples - 3*frameWidth/4)/256);
		FourierFrames 		= new double[numFrames][frameWidth*2]; // in-place
		accum = new double[frameWidth];

		Utils.preProcessFrames(inputSamples, FourierFrames, accum);		// does a bunch of FFTs and other organizations

//		System.out.println("after process");

		// Event listener for the play button
		// Some material here is a holdover from design patten in personalization and test routines
		// Doesn't make much sense for  asingle clip, but does for multiple clips in pre-determined order
		playButton.addActionListener( e -> {
			
	    	int SentUsed = timesThruList  * practiceFiles.size() + positionInList +1;
	    	int SentTotal = DanUI.maxPractice * practiceFiles.size();

    	    playButton.setEnabled(false); 
    	    finishButton.setEnabled(false);

	    	positionInList++;
	    	if (positionInList == practiceFiles.size()) {
	    		positionInList = 0;
	    		Collections.shuffle(practiceFiles);
	    		timesThruList++;
	    	}
	    	
		    dilationRateSpeech = new DilationRate( (double) slider.getValue() / 100 );
	        dilationPlay = new DilationPlaying( true );
	        boolean stayLocked = true;   

	        // Note playButton and finishButton objects are sent as arguments to the sound-playing clip
	        // This is so that clip can, when it ends, set the buttons appropriately
	        
	        // The speechThread is a special class that takes preprocessed data
	        // Should probably be a sub class of SinglePlay rather than its own entity
	        
        	Thread speechThread = new Thread(
        			new SinglePlay(
        					currentSoundFile, dilationRateSpeech, 
        					playButton, finishButton, 
        					+0, true, dilationPlay, 
        					stayLocked, logFile));
	        speechThread.start();
    /*
	        synchronized (logFile) {
    			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Play clip: " + currentSoundFile.toString() + ", ");
	    		logFile.append("at rate: " + Double.toString((double) slider.getValue() / 100) + "\n\n");
    		}
    */
	    });

		// Slider/Mouse Listener - Changes dilation value when the mouse button is lifted.		
	   /* slider.addMouseListener(new MouseListener() {
	        public void mouseClicked(MouseEvent e) {}
	        public void mousePressed(MouseEvent e) {}
	        public void mouseEntered(MouseEvent e) {}
	        public void mouseExited(MouseEvent e) {}

	        public void mouseReleased(MouseEvent e) {
	        	dilationRateSpeech.setRate( (double) slider.getValue() / 100 );
	        
	        	synchronized (logFile) {
	        		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	        		logFile.append(timeStamp + ": ");
	        		logFile.append("set rate: " + Double.toString((double) slider.getValue() / 100) + "\n");
	        	}
	        }
	      }); */
		slider.addChangeListener(new ChangeListener() {
	    	double src=0;
	    	public void stateChanged(ChangeEvent e) {
	    	JSlider source =  (JSlider) e.getSource();
	    	src=1-source.getValue();
	    	dilationRateSpeech.setRate( (double) source.getValue() / 100 );
	    	int value = (int) Math.floor(100 * dilationRateSpeech.getRate());
	    	        synchronized (logFile) {
	    	        String timeStamp = new
	    	SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    	        logFile.append(timeStamp + ": ");
	    	        logFile.append("set rate: " + Double.toString((double)
	    	src / 100) + "\n");
	    	        }

	    	}
	    	});

	    // ChangeListener works and allows smooth adjustment of audio.
	    // Why did I not implement this before?  Worries about discrete data logging?  Probably.

	    //slider.addChangeListener(new ChangeListener() {
		//	public void stateChanged(ChangeEvent e) {
		//		JSlider source =  (JSlider) e.getSource();
		//		dilationRateSpeech.setRate( (double) source.getValue() / 100 );
		//	}
		//});

	    // Finish Button listener
	    // Simple in this case because we only listen once. 
	    
	    finishButton.addActionListener( e -> {
	    	this.dispose();
	        //PracticeUI practice = new PracticeUI();
	        //if (!practice.isDisplayable()) { practice.setUndecorated(true); }
	        //practice.setVisible(true);
	        Intro3 intro = new Intro3();
	        if (!intro.isDisplayable()) { intro.setUndecorated(true); }
	        intro.setVisible(true);
	        
	        try {
				FileWriter fileWriter = new FileWriter(DanUI.SubjectRecord, true);
				BufferedWriter writer = new BufferedWriter(fileWriter);
	    		synchronized (logFile) {
		    		writer.append(logFile.toString()); 
		    		writer.close();
		    		logFile = new StringBuilder();
   	    		}
    		} catch (Exception e1) { e1.printStackTrace(); }
			return;
	    });
	}
	

	public void actionPerformed(ActionEvent arg0) {}
}
