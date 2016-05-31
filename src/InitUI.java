import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


public class InitUI extends JFrame implements ActionListener {

	// InitUI is a simple GUI for:
	//		Setting the experiment mode (Telephone, Reverse Telephone, or Hearing Aid)
	// 		Logging the investigator
	//		Logging the SUbject ID
	// 		(Eventually) alerting the investigator that a parallel thread has finished executing and is ready to proceed
	
	public 			JPanel 					InitPanel;
	public static 	JLabel 					WelcomeLabel;
	public static 	JTextField 				SubjectIDField;
	public static 	JTextField 				ErrorField;
	public static 	JButton 				nextButton;
	public static 	JComboBox<String> 		InvestigatorCombo;
	public static	JRadioButton			Toefl;
	public static	JRadioButton			NonToefl;
	
	//public static 	JComboBox<String> 		ModeCombo;
    String[] 		Investigators 			= new String[] {"", "John Novak", "Dan Bunn", "Robert Kenyon"};
    //String[] 		Modes 					= new String[] {"Telephone", "Hearing Aid", "Reverse Telephone"};

	
	public InitUI() { begin(); }
	
	public synchronized void begin()  {
	    final JPanel InitPanel = new JPanel();
	    InitPanel.setLayout(null);
	    Font font = new Font("Verdana", Font.BOLD, 40);

	    //	Title Banner
	    WelcomeLabel = new JLabel("Audio Dilation Speech-in-Noise Test");
	    WelcomeLabel.setBounds(0, 1*DanUI.height/10, DanUI.width, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    InitPanel.add(WelcomeLabel);

	    // Text field fill-in for subject ID
	    SubjectIDField = new JTextField();
	    SubjectIDField.setBounds(7*DanUI.width/10, 2*DanUI.height/10, 500, 50);
	    SubjectIDField.setHorizontalAlignment(SwingConstants.CENTER);
	    SubjectIDField.setFont(font);
	    SubjectIDField.setText("test");
	    InitPanel.add(SubjectIDField);

	    // Text field which wlil be filled in if errors occur
	    ErrorField = new JTextField();
	    ErrorField.setBounds((int) 3*DanUI.width/10-500, 8*DanUI.height/10, 550, 50);
	    ErrorField.setHorizontalAlignment(SwingConstants.CENTER);
	    ErrorField.setFont(font);
	    InitPanel.add(ErrorField);

	    // Dropdown for investigator ID
	    InvestigatorCombo = new JComboBox<String>(Investigators);
	    InvestigatorCombo.setSelectedIndex(1);
	    InvestigatorCombo.setFont(font);
	    InvestigatorCombo.setBounds(3*DanUI.width/10-500, 2*DanUI.height/10, 550, 50);
	    InitPanel.add(InvestigatorCombo);

	    ButtonGroup BGtoefl=new ButtonGroup();
	    // Toefl and NonToefl initialize
	    Toefl = new JRadioButton("Toefl");
	    Toefl.setBounds(3*DanUI.width/10-500, 3*DanUI.height/10, 350, 50);
	    Toefl.setFont(font);
	    InitPanel.add(Toefl);
	    BGtoefl.add(Toefl);

	    NonToefl = new JRadioButton("Non-Toefl");
	    NonToefl.setBounds(3*DanUI.width/10-100, 3*DanUI.height/10, 350, 50);
	    NonToefl.setFont(font);
	    InitPanel.add(NonToefl);
	    BGtoefl.add(NonToefl);
	    
	    // Dropdown for experment mode
	    /*ModeCombo = new JComboBox<String>(Modes);
	    ModeCombo.setFont(font);
	    ModeCombo.setBounds(3*DanUI.width/10-500, 3*DanUI.height/10, 550, 50);
	    ModeCombo.setSelectedIndex(1);
	    InitPanel.add(ModeCombo);
     	*/
	    
	    // Button to advance to next phase
	    // In this case, brings TrainingUI in progress to the foreground
	    // TrainingUI had better have finished its processing by now. 
	    nextButton = new JButton("Next");
	    nextButton.setFont(font);
	    nextButton.setBounds(7*DanUI.width/10, 8*DanUI.height/10, 500, 50);
	    nextButton.setHorizontalAlignment(SwingConstants.CENTER);
	    InitPanel.add(nextButton);

	    add(InitPanel);
	    setSize(DanUI.width, DanUI.height);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);

	    // Functional programming style action listener for the Next button
	    // e is the event; everything in brackets is excuted on button press
	    nextButton.addActionListener( e -> {
	        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        Date now = new Date();
	        String DateString = sdfDate.format(now);
	        
	    	// Check to make sure we have populated subject ID
	    	if (SubjectIDField.getText().trim().equals("")) {
	    		ErrorField.setText("Subject ID Field must not be blank"); return;
	    	}
	    	
	    	// Check to make sure we have populated investigator
	    	if (InvestigatorCombo.getSelectedIndex() == 0) {
	    		ErrorField.setText("Investigator Field must not be blank"); return;
	    	}
	    	
	    	// Check to make sure there is no name clash	    	
	    	ArrayList<File> files = new ArrayList<File>(Arrays.asList(DanUI.SubjectDirectory.listFiles()));
	    	for (File file : files) {
	    		if (file.toString().equals(DanUI.SubjectDirectory.toString() + "\\" + SubjectIDField.getText() + ".txt")) {
		    		ErrorField.setText("Subject record already exists"); return;
	    		}	    		
	    	}
	    	
	    	// If no problems:
	    	// 	Get rid of this UI
	    	// 	Set up and write to log file
	    	//  invoke next UI

	    	// Get rid of this UI
    		this.dispose();
    		
    		// Set up and write initial information to the log file
    		// Some small changes to deal with the new Mode
    		DanUI.SubjectRecord = new File(DanUI.SubjectDirectory + "\\" + SubjectIDField.getText() + ".txt");
    		/*if (ModeCombo.getSelectedItem() == "Hearing Aid") { DanUI.Mode = new String("Hearing Aid"); }
    		else if (ModeCombo.getSelectedItem() == "Telephone") { DanUI.Mode = new String("Telephone"); }
    		else { DanUI.Mode = new String("Reverse Telephone");  }
    		*/
    		DanUI.Mode=new String("Hearing Aid");
    		
    		try {
        		BufferedWriter writer = null;
	   		    writer = new BufferedWriter(
	   		    		     new OutputStreamWriter(
	   		    				 new FileOutputStream(DanUI.SubjectRecord), "utf-8"));
	    		writer.write(DateString); writer.newLine();
	    		writer.write("Subject Identification String: " + SubjectIDField.getText()); writer.newLine();
	    		writer.write("Investigator Name: " + InvestigatorCombo.getSelectedItem()); writer.newLine();
	    		if (Toefl.isSelected())
	    			writer.write("Toefl (Yes/No): Yes\n");
	    		else
	    			writer.write("Toefl (Yes/No): No\n");
	    		//writer.write("Experimental Mode: " + ModeCombo.getSelectedItem()); writer.newLine();
	    		writer.close();
    		}
    		catch (IOException ex) { }

    		// Set up and begin Instruction UI
	        InstrUI instructions = new InstrUI();
	        //if (!instructions.isDisplayable()) { instructions.setUndecorated(true); }
	        //instructions.setVisible(true);
	        TrainingUI training = new TrainingUI();
	        if (!training.isDisplayable()) { training.setUndecorated(true); }
	        training.setVisible(true);
	        
	    });
	}

	public void actionPerformed(ActionEvent arg0) {	}
}
