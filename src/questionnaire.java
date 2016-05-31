import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

public class questionnaire extends JFrame {

	StringBuilder 		logFile = new StringBuilder();

	JRadioButton rdbtnIStronglyAgree[] = new JRadioButton[3];
	JRadioButton rdbtnIAgree[] = new JRadioButton[3];
	JRadioButton rdbtnNeutral[] = new JRadioButton[3];
	JRadioButton rdbtnIDisagree[] = new JRadioButton[3];
	JRadioButton rdbtnIStronglyDisagree[] = new JRadioButton[3];
	
	
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					questionnaire frame = new questionnaire();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public questionnaire() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
		});
		int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    Font font = new Font("Verdana", Font.BOLD, 40);
	    Font font1 = new Font("Verdana", Font.BOLD, 18); 
		
	    ButtonGroup BTanswers[]=new ButtonGroup[3];
	    
	    for (int i=0;i<3;i++)
	    {
	    	BTanswers[i]=new ButtonGroup();
	    	rdbtnIStronglyAgree[i] = new JRadioButton("I Strongly agree");
	    	rdbtnIAgree[i] = new JRadioButton("I agree");
	    	rdbtnNeutral[i] = new JRadioButton("Neutral");
	    	rdbtnIDisagree[i] = new JRadioButton("I disagree");
	    	rdbtnIStronglyDisagree[i] = new JRadioButton("I Strongly disagree");
	    	BTanswers[i].add(rdbtnIStronglyAgree[i]);
	    	BTanswers[i].add(rdbtnIAgree[i]);
	    	BTanswers[i].add(rdbtnNeutral[i]);
	    	BTanswers[i].add(rdbtnIDisagree[i]);
	    	BTanswers[i].add(rdbtnIStronglyDisagree[i]);
	    }
	    setTitle("Questionnaire");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 655, 665);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnNewButton = new JButton("Finish");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (logFile) {
					String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
		    		logFile.append(timeStamp + ": QUESTIONNAIRE ANSWER \n");
		    		for (int qi=0;qi<3;qi++)
		    		{
		    			logFile.append("Q"+(qi+1)+":");
		    			if (rdbtnIStronglyAgree[qi].isSelected())
		    				logFile.append("Strongly Agree\n");
		    			if (rdbtnIAgree[qi].isSelected())
		    				logFile.append("Agree\n");
		    			if (rdbtnNeutral[qi].isSelected())
		    				logFile.append("Neutral\n");
		    			if (rdbtnIDisagree[qi].isSelected())
		    				logFile.append("Disagree\n");
		    			if (rdbtnIStronglyDisagree[qi].isSelected())
		    				logFile.append("Strongly Disagree\n");
		    		}
					try {
						FileWriter fileWriter = new FileWriter(DanUI.SubjectRecord, true);
						BufferedWriter writer = new BufferedWriter(fileWriter);
			    		synchronized (logFile) {
				    		writer.append(logFile.toString()); 
				    		writer.close();
				    		logFile = new StringBuilder();
		   	    		}
		    		} catch (Exception e1) { e1.printStackTrace(); }
				}
				System.exit(0);
			}
		});
		btnNewButton.setFont(font);
		btnNewButton.setBounds(8*width/10, 8*height/10, 250, 50);
		btnNewButton.setHorizontalAlignment(SwingConstants.CENTER);	
		contentPane.add(btnNewButton);
		
		JLabel lblThankYouFor = new JLabel("Thank you for taking this test. Please answer the following questions on your experience:");
		lblThankYouFor.setFont(font1);
		lblThankYouFor.setBounds(0, ((0*6)+0)*height/30+100, 1000, 50);
		lblThankYouFor.setHorizontalAlignment(SwingConstants.LEFT);				
		contentPane.add(lblThankYouFor);
		
		JLabel lblInGeneral = new JLabel("1)  In general, audio dilation for listening skill was useful:");
		lblInGeneral.setFont(font1);
		lblInGeneral.setBounds(0, ((0*6)+1)*height/30+100, 1000, 50);
		lblInGeneral.setHorizontalAlignment(SwingConstants.LEFT);				

		contentPane.add(lblInGeneral);
		
		JLabel lblSlowingSpeech = new JLabel("2) Slowing speech for the audio files I listened to was useful:");
		lblSlowingSpeech.setFont(font1);
		lblSlowingSpeech.setBounds(0, ((1*6)+1)*height/30+100, 1000, 50);
		lblSlowingSpeech.setHorizontalAlignment(SwingConstants.LEFT);				

		contentPane.add(lblSlowingSpeech);
		
		JLabel lblTheAbility = new JLabel("3) The ability to control audio dilation was easy to use:");
		lblTheAbility.setFont(font1);
		lblTheAbility.setBounds(0, ((2*6)+1)*height/30+100, 1000, 50);
		lblTheAbility.setHorizontalAlignment(SwingConstants.LEFT);				

		contentPane.add(lblTheAbility);
			
		for (int i=0;i<3;i++)
		{
			rdbtnIStronglyAgree[i].setFont(font1);
			rdbtnIStronglyAgree[i].setBounds(width/30, ((i*6)+2)*height/30+100, 1000, 50);
			rdbtnIStronglyAgree[i].setHorizontalAlignment(SwingConstants.LEFT);			
			contentPane.add(rdbtnIStronglyAgree[i]);
			
			
			rdbtnIAgree[i].setFont(font1);
			rdbtnIAgree[i].setBounds(width/30, ((i*6)+3)*height/30+100, 1000, 50);
			rdbtnIAgree[i].setHorizontalAlignment(SwingConstants.LEFT);			
	
			contentPane.add(rdbtnIAgree[i]);
			
	
			rdbtnNeutral[i].setFont(font1);
			rdbtnNeutral[i].setBounds(width/30, ((i*6)+4)*height/30+100, 1000, 50);
			rdbtnNeutral[i].setHorizontalAlignment(SwingConstants.LEFT);			
	
			contentPane.add(rdbtnNeutral[i]);
			
	
			rdbtnIDisagree[i].setFont(font1);
			rdbtnIDisagree[i].setBounds(width/30, ((i*6)+5)*height/30+100, 1000, 50);
			rdbtnIDisagree[i].setHorizontalAlignment(SwingConstants.LEFT);			
	
			contentPane.add(rdbtnIDisagree[i]);
			
			rdbtnIStronglyDisagree[i].setFont(font1);
			rdbtnIStronglyDisagree[i].setBounds(width/30, ((i*6)+6)*height/30+100, 1000, 50);
			rdbtnIStronglyDisagree[i].setHorizontalAlignment(SwingConstants.LEFT);			
			contentPane.add(rdbtnIStronglyDisagree[i]);
		}
	}
}
