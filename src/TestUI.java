import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TestUI extends JFrame implements ActionListener {

	
	JPanel 				TestPanel;
	JLabel 				WelcomeLabel;
	JButton 			playButton, continueButton;
	JLabel 				instructionLabel;
	ArrayList<File>		testFiles, noiseFiles;
	File				noiseFile, speechFile;
	DilationPlaying		dilationPlay = new DilationPlaying();
	DilationRate		dilRateSpeech = new DilationRate();
	DilationRate		dilRateNoise = new DilationRate();
	StringBuilder 		logFile = new StringBuilder();
	Random 				random = new Random();

	public TestUI() { begin(); }

	public void actionPerformed(ActionEvent arg0) {
	}
	
	public synchronized void begin() {
		
		DanUI.Phase = new String("Test");
//			System.out.println("start test");

		Iterator<Triple<Double, Double, File>> it3 = DanUI.testList.iterator();

		final JPanel TestPanel = new JPanel();
	    TestPanel.setLayout(null);
	    Font font = new Font("Verdana", Font.BOLD, 40);

	    WelcomeLabel = new JLabel("Audio Dilation Speech-in-Noise Test Phase");
	    WelcomeLabel.setBounds(0, 1*DanUI.height/10, DanUI.width, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    TestPanel.add(WelcomeLabel);

	    playButton = new JButton("Listen");
	    playButton.setFont(font);
	    playButton.setBounds(3*DanUI.width/10-500, 8*DanUI.height/10, 500, 50);
	    playButton.setHorizontalAlignment(SwingConstants.CENTER);
	    playButton.putClientProperty("JComponent.sizeVariant", "large");
	    TestPanel.add(playButton);

	    continueButton = new JButton("Continue");
	    continueButton.setFont(font);
	    continueButton.setBounds(7*DanUI.width/10, 8*DanUI.height/10, 500, 50);
	    continueButton.setHorizontalAlignment(SwingConstants.CENTER);
	    continueButton.putClientProperty("JComponent.sizeVariant", "large");
	    continueButton.setEnabled(false);
	    TestPanel.add(continueButton);

	    instructionLabel = new JLabel();
	    instructionLabel.setBounds(0, 2*DanUI.height/10, DanUI.width, 50);
	    instructionLabel.setFont(font);
	    instructionLabel.setText("Press listen, and listen carefully to the sentence.");
	    instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    TestPanel.add(instructionLabel);

	    add(TestPanel);
	    setSize(DanUI.width, DanUI.height);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);

	    // Get the test sentences for dilation conditions
		File directory = new File(DanUI.testDirectory + "\\List1");
		testFiles = new ArrayList<File>(Arrays.asList(directory.listFiles()));
		Collections.shuffle(testFiles);

		// Get the noise sentences
		noiseFiles = new ArrayList<File>(Arrays.asList(DanUI.noiseDirectory.listFiles())) ;
		Iterator<File> noises = noiseFiles.iterator();
		Collections.shuffle(noiseFiles);
		noiseFile = noises.next();

		// Get keys (noise levels) as list from the finalValues HashMap
		// Shuffle the keys, and make an iterator over the key values
//	    ArrayList keys = new ArrayList(DanUI.finalValues.keySet());
//	    Collections.shuffle(keys);
//	    Iterator<Integer> keyIterator = keys.iterator();	    

//	    while (keyIterator.hasNext()) {
//	    	System.out.println("noise: " + keyIterator.next() + " sentence: " + testIterator.next());
//	    }

//		while (it3.hasNext()) {
//	    	System.out.println("noise: " + it3.next() + " sentence: " + it3.next());
//	    }
	    
		synchronized (logFile) {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
    		logFile.append(timeStamp + ": BEGIN TEST PHASE\n");
		}


	    playButton.addActionListener( e -> {	
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			Iterator<Thread> setIter = threadSet.iterator();
			
			while (setIter.hasNext()) {
				System.out.println(setIter.next());
			}
			
	    	Triple<Double, Double, File> 	t 				= it3.next();
	    	Double							noiseLevel 		= (Double) t.getNoise();	
	    	speechFile 										= (File) t.getSentence();
	    	dilationPlay 									= new DilationPlaying();
	    	
	    	if (DanUI.Mode.equals("Hearing Aid")) 	{
	    		dilRateNoise.setRate((double) t.getDilation());
	    		dilRateSpeech.setRate((double) t.getDilation());
	    	}
	    	else if (DanUI.Mode.equals("Telephone"))	{
	    		dilRateNoise.setRate(1.0);
	    		dilRateSpeech.setRate((double) t.getDilation());
	    	}
	    	else {
	    		dilRateNoise.setRate((double) t.getDilation());
	    		dilRateSpeech.setRate(1.0);	    		
	    	}
	    		
	    	// convert to second screen
	    	playButton.setEnabled(false);
	    	continueButton.setEnabled(true);
	    	int r = random.nextInt(noiseFiles.size()-1);
	    	noiseFile = noiseFiles.get(r);
	    	// System.out.println(noiseFile.getName());
	    	//System.out.println(speechFile.getName().length());
	    	int listNum = Integer.parseInt(speechFile.getName().substring(4, 6));
	    	int sentNum = Integer.parseInt(speechFile.getName().substring(7, 8));
	    	int code = 0;
	    	if (listNum == 10) code = sentNum;
	    	else if (listNum == 11) code = sentNum + 6;
	    	else if (listNum == 12) code = sentNum + 12;
	    	else if (listNum == 13) code = sentNum +6;
	    	//System.out.println(listNum + " - " + sentNum);
	    	instructionLabel.setText(" Repeat the sentence out loud, and press Continue. (" + code + ")");
	    	// write to logString
    		synchronized (logFile) {
    			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Play clip : " + speechFile.toString() + "\n");
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Noise clip : " + noiseFile.toString() + "\n");
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Noise Level: " + noiseLevel.toString() + "\n");
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Speech Rate : " + dilRateSpeech.getRate().toString() + "\n");
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("noise Rate : " + dilRateNoise.getRate().toString() + "\n");
    		}

    		// play test clip and noise
//	    	System.out.println("playing: " + speechFile.getName() + " at noise level: " + noiseLevel);
//	    	System.out.println("playing: " + noiseFile.getName() + " at noise level: " + noiseLevel);
//	    	System.out.println("Noise rate = " + dilRateNoise.getRate() + ", Speech rate = " +dilRateSpeech.getRate());
	    	Thread dualThread = new Thread(
	    			new DualPlay(
	    				speechFile, (double) 0, 
	    				noiseFile, (double) noiseLevel, 
			        	dilRateSpeech, dilRateNoise, 
			        	continueButton, continueButton, 
			        	DanUI.Mode.equals("Hearing Aid"), dilationPlay, 
			        	true, logFile)
			        );
			        dualThread.start();
			        dualThread.setPriority(Thread.MAX_PRIORITY);

	    });
	    
	    continueButton.addActionListener( e -> {
	    	// if there are still keys remaining:
	    	// if (keyIterator.hasNext()) {
	    	if (it3.hasNext()) {
		    	instructionLabel.setText("Press listen, and listen carefully to the sentence.");
		    	playButton.setEnabled(true);
		    	continueButton.setEnabled(false);
	    	}
	    	
	    	// else we're done-- tear down and write logfile
	    	else this.dispose();
	    	try {
   		    	FileWriter fileWriter = new FileWriter(DanUI.SubjectRecord, true);
   	    		BufferedWriter writer = new BufferedWriter(fileWriter);
   	    		synchronized (logFile) {
		    		writer.append(logFile.toString()); 
		    		writer.close();
		    		logFile = new StringBuilder();
   	    		}
			} catch (UnsupportedEncodingException | FileNotFoundException e1) { e1.printStackTrace();
			} catch (IOException e1) { e1.printStackTrace();
			}

	    });
	}

}
