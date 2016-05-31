import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;

public class quiz extends JFrame {

	
	StringBuilder 		logFile = new StringBuilder();
	ArrayList<File>		practiceFiles;
	private JPanel contentPane;
	
	private int radio_num,check_num;

	private JLabel[] QLabels;
	private ButtonGroup[] AButtonGroups;
	private JCheckBox[][] ACheckBoxes;
	private JRadioButton[][] ARadioButtons;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					quiz frame = new quiz();
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
	public quiz() {
		QLabels = new JLabel[8];
		AButtonGroups=new ButtonGroup[8];
		ACheckBoxes = new JCheckBox[8][];
		ARadioButtons = new JRadioButton[8][];
		int q_num=0;
		radio_num=0;
		check_num=0;
		String qtype;
		String question;
		int question_num=0;
		int i=0,j=0,n=0;
		n=0;
		
		setTitle("Quiz 1");
			
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
		
		
	    // read questions
		File directory = new File(DanUI.quizzesTextsDirectory);
		//practiceFiles = new ArrayList<File>(Arrays.asList(directory.listFiles()));

    
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 893, 799);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel 				WelcomeLabel;	    
	    WelcomeLabel = new JLabel("Quiz "+DanUI.test_num);
	    WelcomeLabel.setBounds(4*width/10, 1*height/20, 200, 50);
	    WelcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    WelcomeLabel.setFont(font);
	    contentPane.add(WelcomeLabel);
		
		JButton btnNext = new JButton("Next");
		btnNext.addActionListener(e-> {
			synchronized (logFile) {
				String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SS").format(new Date());
	    		logFile.append(timeStamp + ": QUIZ ANSWER \n");
	    		int qn=1;
	    		for (int qi=0;qi<radio_num;qi++)
	    		{
	    			logFile.append("Q"+qn+":");
	    			for (int qj=0;qj<8;qj++)
	    			{
	    				//if (ARadioButtons[qi][qj]!=null)
	    				//{
	    					if (ARadioButtons[qi][qj].isSelected())
	    					{
	    						logFile.append((qj+1)+",");
	    					}
	    				//}
	    			}
	    			logFile.append("\n");
	    			qn++;
	    		}
	    		for (int qi=0;qi<check_num;qi++)
	    		{
	    			logFile.append("Q"+qn+":");
	    			for (int qj=0;qj<8;qj++)
	    			{
	    				//if (ACheckBoxes[qi][qj]!=null)
	    				//{
	    					if (ACheckBoxes[qi][qj].isSelected())
	    					{
	    						logFile.append((qj+1)+",");
	    					}
	    				//}
	    			}
	    			logFile.append("\n");
	    			qn++;
	    		}	    		
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

			//public void actionPerformed(ActionEvent arg0) {
			//max of 6 tests
				if (DanUI.test_num<6)
				{
					DanUI.test_num++;
					
					this.dispose();

					Test test = new Test();
			        if (!test.isDisplayable()) { test.setUndecorated(true); }
			        test.setVisible(true);
					test.setTitle("Test "+DanUI.test_num); 					
					
				}
				else
				{
					this.dispose();

					questionnaire mquest = new questionnaire();
			        if (!mquest.isDisplayable()) { mquest.setUndecorated(true); }
			        mquest.setVisible(true); 					
				}
			//}
		});
		btnNext.setFont(font);
		btnNext.setBounds(8*width/10, 1*height/20, 250, 50);
		btnNext.setHorizontalAlignment(SwingConstants.CENTER);
		
		contentPane.add(btnNext);
		
	    for (i=0;i<8;i++)
	    {
    		ARadioButtons[i]=new JRadioButton[8];
    		ACheckBoxes[i]=new JCheckBox[8];	    	
	    	for (j=0;j<8;j++)
	    	{
	    		ARadioButtons[i][j]=new JRadioButton();
	    		ACheckBoxes[i][j]=new JCheckBox();
	    	}
	    }
	    i=0;j=0;
		try {
			List <String> qlines=Files.readAllLines(Paths.get(DanUI.quizzesTextsDirectory+"\\QuizTrack"+DanUI.quiz_num+".txt"));
			
					
			while (i<qlines.size()){
				qtype=qlines.get(i);
				question=qlines.get(i+2);
				question_num=Integer.valueOf(qlines.get(i+1));
				QLabels[q_num] = new JLabel(question);
				QLabels[q_num].setFont(font1);
				QLabels[q_num].setBounds(0, n*height/30+100, 2000, 50);
				QLabels[q_num].setHorizontalAlignment(SwingConstants.LEFT);				
				contentPane.add(QLabels[q_num]);
				n++;
				q_num++;
				if (qtype.equals("c"))
				{
					//ACheckBoxes[check_num]=new JCheckBox[question_num];
					for (j=0;j<question_num;j++)
					{
						ACheckBoxes[check_num][j]=new JCheckBox (qlines.get(i+3+j));
						ACheckBoxes[check_num][j].setFont(font1);
						ACheckBoxes[check_num][j].setBounds(width/30, n*height/30+100, 2000, 50);
						ACheckBoxes[check_num][j].setHorizontalAlignment(SwingConstants.LEFT);	
						contentPane.add(ACheckBoxes[check_num][j]);		
						n++;
					}
					check_num++;
				}
				else
				{
					//ARadioButtons[radio_num]=new JRadioButton[question_num];
					AButtonGroups[radio_num]=new ButtonGroup();
					for (j=0;j<question_num;j++)
					{
						ARadioButtons[radio_num][j]=new JRadioButton (qlines.get(i+3+j));
						ARadioButtons[radio_num][j].setFont(font1);
						ARadioButtons[radio_num][j].setBounds(width/30, n*height/30+100, 2000, 50);
						ARadioButtons[radio_num][j].setHorizontalAlignment(SwingConstants.LEFT);	
						contentPane.add(ARadioButtons[radio_num][j]);
						AButtonGroups[radio_num].add(ARadioButtons[radio_num][j]);
						n++;
					}					
					radio_num++;
				}
				i=i+question_num+3;
			}
			

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		

	}
}
