import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;


public class InstrUI extends JFrame implements ActionListener {

	// InstrUI is a simple GUI for:
	//		Allowing the subject to read the instructions for the experiment after the investigator has explained them
	//		The interface allows the subject to click backward and forward through the instructions until they are satisfied. 
	public 			JPanel 			InitPanel;
	public static 	JLabel 			WelcomeLabel;
	public static 	JTextArea		InstructionField;
	public static 	JButton 		backButton;
	public static 	JButton 		nextButton;
    int 			index 			= 0;	// Keeps track of position in instructions list

	public InstrUI() { begin(); }
	
	public synchronized void begin() {
		DanUI.Phase		=	new String("Instructions");
		System.out.println("Phase: " + DanUI.Phase);

		final JPanel InitPanel = new JPanel();
	    InitPanel.setLayout(null);
	    Font font = new Font("Verdana", Font.BOLD, 40);
	    
	    // Title Banner
	    WelcomeLabel = new JLabel("Audio Dilation Instruction Phase");
	    WelcomeLabel.setBounds(0, 1*DanUI.height/10, DanUI.width, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    InitPanel.add(WelcomeLabel);

	    // Large field for instructions
	    InstructionField = new JTextArea();
	    InstructionField.setBounds(3*DanUI.width/10-500, 2*DanUI.height/10, 6*DanUI.width/10+225, 5*DanUI.height/10);
	    InstructionField.setLineWrap(true);
	    InstructionField.setWrapStyleWord(true);
	    InstructionField.setFont(font);
	    InstructionField.setLineWrap(true);
	    InitPanel.add(InstructionField);

	    // Next (forward) button
	    nextButton = new JButton("Next");
	    nextButton.setFont(font);
	    nextButton.setBounds(7*DanUI.width/10, 8*DanUI.height/10, 500, 50);
	    nextButton.setHorizontalAlignment(SwingConstants.CENTER);
	    InitPanel.add(nextButton);

	    // Back button
	    backButton = new JButton("Back");
	    backButton.setFont(font);
	    backButton.setBounds(3*DanUI.width/10-500, 8*DanUI.height/10, 500, 50);
	    backButton.setHorizontalAlignment(SwingConstants.CENTER);
	    backButton.setEnabled(false);
	    InitPanel.add(backButton);

	    add(InitPanel);

	    String instruction = new String();
	    String line = null;
	    
	    // Read instructions from the instructions file
	    // populate into a multi-line string
	    try {
	    	FileReader fr;
			if (DanUI.Mode.equals("Telephone")) fr  = new FileReader(DanUI.instructionsDirectory + "\\Instructions Telephone.txt" );
	    	else if (DanUI.Mode.equals("Hearing Aid")) fr = new FileReader(DanUI.instructionsDirectory + "\\Instructions Hearing Aid.txt" );
	    	else fr = new FileReader(DanUI.instructionsDirectory + "\\Instructions Reverse.txt" );
	    	
			BufferedReader br = new BufferedReader(fr);
			while((line = br.readLine()) != null) { instruction = instruction + line + "\n"; }
			br.close();
		} 
	    catch (FileNotFoundException e1) 	{ e1.printStackTrace(); } 
	    catch (IOException e1) 				{ e1.printStackTrace(); }
	    
	    // split into array of multi-line strings using five vertical dashes as a delimiter.
	    String[] instructions = instruction.split("-----");
	    int maxIndex = instructions.length - 1;
	    InstructionField.setText(instructions[index]);
	    
	    setSize(DanUI.width, DanUI.height);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);

	    // BackButton listener
	    // If there are no prior instructions strings, this is greyed out elsewhere
	    // If we were at last instruction pane and we move back, re-activate next button
	    // put previous set of instructions into instruction pane
	    // Update index and buttons as necessary
	    backButton.addActionListener( e -> {
	    	index--;
		    InstructionField.setText(instructions[index]);
		    
		    if(index == 0) { backButton.setEnabled(false); }
	    	if(index != maxIndex) { nextButton.setText("Next"); }
		    
	  	});
	    
	    // nextButton listener
	    // If not showing last sheet of instructions, advance instructions and put in pane. 
	    // If showing last sheet of instructions:
	    //	Write to log file
	    //	shut this UI down
	    //	start the Personalization UI
	    // If we advanced past the first screen, enable the back button
	    // If we advanced *TO* (not beyond) the last screen, change next to finish
	    
	    nextButton.addActionListener( e -> {
	    	if (index != maxIndex) {
		    	index++;
			    InstructionField.setText(instructions[index]);
	    	}
	    	else {
	    		try {
	        		BufferedWriter writer = null;
		   		    writer = new BufferedWriter(
		   		    		     new OutputStreamWriter(
		   		    				 new FileOutputStream(DanUI.SubjectRecord, true), "utf-8"));
		    		writer.write("Instructions Administered"); writer.newLine();
		    		writer.close();
	    		}
	    		catch (IOException ex) { }
	    		this.dispose();

//			Note that because the TrainingUI was created previously, we only bring it to the foreground
//	        TrainingUI training= new TrainingUI();

	        DanUI.training = new TrainingUI();
	        if (!DanUI.training.isDisplayable()) { DanUI.training.setUndecorated(true); }
	        DanUI.training.setVisible(true);

//				Uncomment these if we want to skip the Gettysburg Address for testing purposes
//		        PracticeUI practice = new PracticeUI();
//		        if (!practice.isDisplayable()) { practice.setUndecorated(true); }
//		        practice.setVisible(true);

	    		return;
	    	}
	    	
	    	if (index!= 0 ) { backButton.setEnabled(true); }
	    	if (index == maxIndex) { nextButton.setText("Finish"); }
	    	else { nextButton.setText("Next"); }
	    });
}

	@Override
	public void actionPerformed(ActionEvent arg0) { }
}
