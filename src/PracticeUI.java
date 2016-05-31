
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;



public class PracticeUI extends JFrame implements ActionListener {

	// PracticeUI is a simple GUI for:
	//		Allowing the subject to familiarize him or herself with dilation in the presence of noise
	// 		Allowing the subject to understand how much time he has available for a typical clip
	//  	Allowing the subject to familiarize himself with the main speaker

	JLabel 				WelcomeLabel, WarningLabel;
	JButton 			playButton, finishButton;
	ArrayList<File>		practiceFiles, noiseFiles;
	JSlider				slider;

	File 				currentSoundFile, currentNoiseFile;
    DilationRate 		dilationRateSpeech, dilationRateNoise;
    DilationPlaying		dilationPlay;
	StringBuilder 		logFile = new StringBuilder();
	int 				positionInList = 0;
	int 				timesThruList = 0;

    public PracticeUI() { begin(); }
	
	public synchronized void begin()  {
		System.gc();
		DanUI.Phase		=	new String("Practice");
		System.out.println("Phase: " + DanUI.Phase);
		
		File directory = new File(DanUI.practiceDirectory);
		practiceFiles = new ArrayList<File>(Arrays.asList(directory.listFiles()));

		Collections.shuffle(practiceFiles);
		
		int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    final JPanel InitPanel = new JPanel();
	    InitPanel.setLayout(null);
	    Font font = new Font("Verdana", Font.BOLD, 40);

	    WelcomeLabel = new JLabel("Practice Phase");
	    WelcomeLabel.setBounds(0, 1*height/10, width, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    InitPanel.add(WelcomeLabel);

	    WarningLabel = new JLabel("");
	    WarningLabel.setBounds(0, 2*height/10, width, 50);
	    WarningLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WarningLabel.setFont(font);
	    InitPanel.add(WarningLabel);

	    slider = new JSlider(40, 101, 70);
	    TallSliderUI tallUI = new TallSliderUI (slider, DanUI.scaleFactor);
	    slider.setUI(tallUI);
	    slider.setBounds(2*width/10, 4*height/10, 2500, tallUI.getThumbSize().height + tallUI.getTickLength());
	    slider.setFont(font);
	    slider.setExtent(1);
	    slider.putClientProperty("JComponent.sizeVariant", "large");
	    slider.setMajorTickSpacing(10);
	    slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(false);
	    slider.setSnapToTicks(false);
	    InitPanel.add(slider);

	    dilationRateSpeech = new DilationRate( (double) slider.getValue() / 100 );
	    	    
	    playButton = new JButton("Play");
	    playButton.setFont(font);
	    playButton.setBounds(3*width/10, 8*height/10, 500, 50);
	    playButton.setHorizontalAlignment(SwingConstants.CENTER);
	    playButton.putClientProperty("JComponent.sizeVariant", "large");
	    InitPanel.add(playButton);

	    finishButton = new JButton("Finish");
	    finishButton.setFont(font);
	    finishButton.setEnabled(false);
	    finishButton.setBounds(7*width/10, 8*height/10, 500, 50);
	    finishButton.setHorizontalAlignment(SwingConstants.CENTER);
	    InitPanel.add(finishButton);

	    add(InitPanel);
	    synchronized (logFile) {
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
    		logFile.append(timeStamp + ": BEGIN PRACTICE PHASE \n");
		}

	    setSize(width, height);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);

	    playButton.addActionListener( e -> {
			
	    	int SentUsed = timesThruList  * practiceFiles.size() + positionInList +1;
	    	int SentTotal = DanUI.maxPractice * practiceFiles.size();
    	    WarningLabel.setText("Practice Sentences Remaining:  " + (SentTotal - SentUsed));

    	    playButton.setEnabled(false); finishButton.setEnabled(false);
	    	currentSoundFile = practiceFiles.get(positionInList);

	    	positionInList++;
	    	if (positionInList == practiceFiles.size()) {
	    		positionInList = 0;
	    		Collections.shuffle(practiceFiles);
	    		timesThruList++;
	    	}
	    	
		    dilationRateSpeech = new DilationRate( (double) slider.getValue() / 100 );
	        dilationPlay = new DilationPlaying( true );
	        boolean stayLocked = (timesThruList == DanUI.maxPractice);
        	Thread speechThread = new Thread(
        			new SinglePlay(
        					currentSoundFile, dilationRateSpeech, 
        					playButton, finishButton, 
        					+0, true, dilationPlay, 
        					stayLocked, logFile));
	        speechThread.start();
    		synchronized (logFile) {
    			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    		logFile.append(timeStamp + ": ");
	    		logFile.append("Play clip: " + currentSoundFile.toString() + ", ");
	    		logFile.append("at rate: " + Double.toString((double) slider.getValue() / 100) + "\n\n");
    		}
	    });

	    // This is where I would add the change listener if desired
	    
	    slider.addMouseListener(new MouseListener() {
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
	      });
	    
	    
	    finishButton.addActionListener( e -> {
	    	this.dispose();
	        PersonalUI personal = new PersonalUI();
	        if (!personal.isDisplayable()) { personal.setUndecorated(true); }
	        personal.setVisible(true);
			try {
				FileWriter fileWriter = new FileWriter(DanUI.SubjectRecord, true);
				BufferedWriter writer = new BufferedWriter(fileWriter);
	    		synchronized (logFile) {
		    		writer.append(logFile.toString()); 
		    		writer.close();
		    		logFile = new StringBuilder();
   	    		}
    		} catch (Exception e1) { e1.printStackTrace(); }	
	    });
	}

	public void actionPerformed(ActionEvent arg0) {}

}
