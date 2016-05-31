import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import javax.swing.JSlider;
import javax.swing.SwingConstants;



public class PersonalUI extends JFrame implements ActionListener {

	JPanel 												InitPanel;
	JLabel 												WelcomeLabel;
	JLabel 												WarningLabel;
	JButton 											playButton;
	JButton 											finishButton;
	JSlider												slider;

	ArrayList<File>										noiseFiles;
	Iterator<Triple<Double, Double, File>> 				SentenceIter;
	Iterator<ArrayList<Triple<Double, Double, File>>> 	ListIter;
	Triple<Double, Double, File> 						triple = new Triple<Double, Double, File>();

	File												currentFile;
	File												currentNoiseFile;
	Double												currentNoise;

	StringBuilder 										logFile = new StringBuilder();
    DilationRate 										dilationRateSpeech;
    DilationRate 										dilationRateNoise;
    DilationPlaying										dilationPlaying;

	boolean												stayLocked = false;
	int													FilesRemaining = 0;
	int													FilesInList = 0;
	int 												condition = 1;
	int 												sentence = 1;
	int 												currentSentence = 0;
	
	public PersonalUI() { begin(); }
	
	public synchronized void begin()  {
				

		DanUI.Phase = new String("Personal");
		System.out.println("Phase: " + DanUI.Phase);
		
	    synchronized (logFile) {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
    		logFile.append(timeStamp + ": BEGIN PERSONALIZATION PHASE \n\n");
		}

	    final JPanel InitPanel = new JPanel();
	    InitPanel.setLayout(null);
	    Font font = new Font("Verdana", Font.BOLD, 40);

	    WelcomeLabel = new JLabel("Personalization Phase");
	    WelcomeLabel.setBounds(0, 1*DanUI.height/10, DanUI.width, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    InitPanel.add(WelcomeLabel);

	    WarningLabel = new JLabel("Condition " + condition + " , Sentence " + sentence + " of 6");
	    WarningLabel.setBounds(0, 2*DanUI.height/10, DanUI.width, 50);
	    WarningLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WarningLabel.setFont(font);
	    InitPanel.add(WarningLabel);

	    Random rand = new Random();
	    int rate = rand.nextInt(60)+40;
	    slider = new JSlider(40, 101, rate);
	    synchronized (logFile) {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
    		logFile.append(timeStamp + ": Randomize rate to " + rate + " \n\n");
		}

	    TallSliderUI tallUI = new TallSliderUI (slider, DanUI.scaleFactor);
	    slider.setUI(tallUI);

	    slider.setBounds(2*DanUI.width/10, 4*DanUI.height/10, 2500, tallUI.getThumbSize().height + tallUI.getTickLength());
	    slider.setFont(font);
	    slider.setExtent(1);
	    slider.putClientProperty("JComponent.sizeVariant", "large");
	    slider.setMajorTickSpacing(10);
	    slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(false);
	    slider.setSnapToTicks(true);

	    setDilations();
	    
	    InitPanel.add(slider);
	    
	    playButton = new JButton("Play");
	    playButton.setFont(font);
	    playButton.setBounds(3*DanUI.width/10-500, 8*DanUI.height/10, 500, 50);
	    playButton.setHorizontalAlignment(SwingConstants.CENTER);
	    playButton.putClientProperty("JComponent.sizeVariant", "large");
	    InitPanel.add(playButton);

	    finishButton = new JButton("Finish");
	    finishButton.setFont(font);
	    finishButton.setBounds(7*DanUI.width/10, 8*DanUI.height/10, 500, 50);
	    finishButton.setHorizontalAlignment(SwingConstants.CENTER);
	    InitPanel.add(finishButton);

	    add(InitPanel);


	    setSize(DanUI.width, DanUI.height);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
			
		ListIter = DanUI.PersonalList.iterator();
		SentenceIter = ListIter.next().iterator();
		currentSentence++;
		triple = SentenceIter.next();

		// get the list of noise files
		noiseFiles = new ArrayList<File>(Arrays.asList(DanUI.noiseDirectory.listFiles())) ;
		Collections.shuffle(noiseFiles);
		Random rnd = new Random();
		int i = rnd.nextInt(noiseFiles.size());
		currentNoiseFile = noiseFiles.get(i);

		Iterator<Double> noiseLevelsIter;
		noiseLevelsIter 	= DanUI.noiseLevels.iterator();
	    currentNoise 		= (Double) triple.getNoise();
	    
    	playButton.setEnabled(true); finishButton.setEnabled(false);
    	
	    playButton.addActionListener( e -> {
				    	
    		// Disable buttons until end of clip (Reactivated at end of DualPlay thread)
    		// set dilationRates just in case user has not touched the slider    		
	    	playButton.setEnabled(false); finishButton.setEnabled(false);
	    	setDilations();
	    	
        	// create the unocked lock we will send to the thread
        	dilationPlaying = new DilationPlaying( true );
        	
        	// 	if this is the last sentence in a given list 
        	//	(i.e., last sentence to be played at this noise level)
        	// 	then we want the play button to stay locked. 
        	//	This GAURANTEES that the next button press will be a finish button
    		stayLocked = !SentenceIter.hasNext();

    		synchronized (logFile) {
    			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Play clip: " + triple.getSentence().toString() + "\n");
	    		logFile.append("Play noise: " + currentNoiseFile.toString() + "\n");
	    		logFile.append("Noise Level: " + triple.getNoise().toString() + "\n\n");
    		}
    		
    		String labelString = new String();
    		
    		// check if we have more sentences in the list
    		if (SentenceIter.hasNext()) { currentSentence++; }
    		else 						{ currentSentence = 1; condition++; }
    		
			labelString = new String("Condition " + condition + " , Sentence " + currentSentence + " of 6");
    		
    		currentFile = (File) triple.getSentence();
    		currentNoise = (Double) triple.getNoise();

//    		System.out.println("Noise at " + currentNoise);
    		Thread dualThread = new Thread(	
    				new DualPlay(	currentFile,  00.0, 
	        						currentNoiseFile, currentNoise, 
	        						dilationRateSpeech, dilationRateNoise, 
	        						playButton, finishButton, 
	        						DanUI.Mode.equals("Hearing Aid"), 
	        						dilationPlaying, stayLocked, logFile, WarningLabel, labelString
	        		)
	        );
        	
	        dualThread.start();  dualThread.setPriority(Thread.MAX_PRIORITY);
	        // both buttons locked until the thread ends and the audio stops.        	
        	
        	// test for condition:  
        	//		If next sentence in list, queue it up
        	//		if not, take care of the rest in the finish button
        	
	        //	If there IS a next sentence in this list, queue it up.	
    		Random rnd2 = new Random();
    		currentNoiseFile = noiseFiles.get(rnd2.nextInt(noiseFiles.size()));
    		if (SentenceIter.hasNext()) { triple = SentenceIter.next(); }
	    });

	    finishButton.addActionListener( e -> {
	    	// if we hit this button:
	    	// There are no more sentences for this list/noise level (forced to do all six)
	    	 
	    	// Therefore, write to the appropriate log file
	    	// and store the noise level (key) and dilation ratio (value)
	    	// in finalvalues hashmap
	    	
	    	double finalDil = (double) slider.getValue() / 100;
		    synchronized (logFile) {
				String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    		logFile.append(timeStamp + "Final Dilation for Noise: " + triple.getNoise() + " is: " + finalDil + "\n");
			}

	    	updateTest((Double) triple.getNoise(), finalDil);
	    	
	    	// Whether or not there are more sentences in the current list,
	    	// as long as there is a LIST, we should always:
	    	//		get the next list
	    	//		get the next sentence from the new list
	    	//		get the next noise level
	    	//		unlock play and lock finish (because each noise level needs one listen.)
   		    if (ListIter.hasNext()) {
   				SentenceIter = ListIter.next().iterator();
   				triple = SentenceIter.next();
	    		playButton.setEnabled(true); finishButton.setEnabled(false);   		    	
	    	    FilesRemaining = FilesInList;
	    	    
	    	    
//	    	    Random rand = new Random();
   		    	slider.setValue(rand.nextInt(60)+40);
   			    synchronized (logFile) {
   					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
   		    		logFile.append(timeStamp + ": Randomize rate to " + slider.getValue() + " \n\n");
   				}

   		    	slider.repaint();
   		    }
   		    
   		    // Otherwise, no more lists means tear this down and launch the test UI
   		    else { 	
   		    	this.dispose(); 
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

   				Iterator<Triple<Double, Double, File>> it3 = DanUI.testList.iterator();
   				while (it3.hasNext()) { it3.next(); }

   				// this is a holdover from debugging routines. 
   				
   		    	Collections.shuffle(DanUI.testList);
   				it3 = DanUI.testList.iterator();
   				while (it3.hasNext()) { it3.next(); }

   		        TestUI test = new TestUI();
   		        if (!test.isDisplayable()) { test.setUndecorated(true); }   		        
   		        test.setVisible(true);
   		 
   		    }
	    });

	    
	    // sets dilationRate for speech to the correct value
	    // if in hearing aid mode, sets dilationRate for noise as well
	    // writes time and action to the log string
	    slider.addMouseListener(new MouseListener() {
	        public void mouseClicked(MouseEvent e) {}
	        public void mousePressed(MouseEvent e) {}
	        public void mouseEntered(MouseEvent e) {}
	        public void mouseExited(MouseEvent e) {}

	        public void mouseReleased(MouseEvent e) {
	        	double rate = (double) slider.getValue() / 100 ;
	        	
	        	if (DanUI.Mode.equals("Telephone")) {
		        	dilationRateSpeech.setRate( rate );
		        	dilationRateNoise.setRate( 1.0);
	        	}

	        	if (DanUI.Mode.equals("Hearing Aid")) {
		        	dilationRateSpeech.setRate( rate );
		        	dilationRateNoise.setRate( rate );
	        	}

	        	if (DanUI.Mode.equals("Reverse Telephone")) {
		        	dilationRateSpeech.setRate( 1.0);
		        	dilationRateNoise.setRate( 1.0 );
	        	}

	    		synchronized (logFile) {
	    			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());

		    		logFile.append(timeStamp + ": ");
		        	logFile.append("set rate: " + Double.toString(rate) + "\n");
	    		}
	        }
	    });
	}

	private void updateTest(Double noise, double finalDil) {
		// TODO Auto-generated method stub
		java.util.Iterator<Triple<Double, Double, File>> TripleIt = DanUI.testList.iterator();
		
		// scan list until we find the FIRST matching noise level
		// replace dilation rate
		while(TripleIt.hasNext()) {
			Triple search = TripleIt.next();
			if ((double) search.getNoise() == (double) noise) { search.setDilation(finalDil); break; }
		}
		
//		System.out.println("\n\n");
//		TripleIt = DanUI.testList.iterator();
//		System.out.println("Partially Updated Test structure");
//		while(TripleIt.hasNext()) { System.out.println(TripleIt.next()); }
//		System.out.println("\n\n");

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	public void setDilations() {
		
    	if (DanUI.Mode.equals("Telephone")) { 
	        dilationRateSpeech = new DilationRate( (double) slider.getValue() / 100 );
	        dilationRateNoise = new DilationRate( 1.0 );
    	}

    	if (DanUI.Mode.equals("Hearing Aid")) { 
	        dilationRateSpeech = new DilationRate( (double) slider.getValue() / 100 );
	        dilationRateNoise = new DilationRate( (double) slider.getValue() / 100 );
    	}

    	if (DanUI.Mode.equals("Reverse Telephone")) { 
	        dilationRateSpeech = new DilationRate( 1.0 );
	        dilationRateNoise = new DilationRate( (double) slider.getValue() / 100 );
    	}

	}
}
