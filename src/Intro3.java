//package thesis;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JSlider;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Intro3 extends JFrame {

	private JPanel contentPane;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Intro3 frame = new Intro3();
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
	public Intro3() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				setExtendedState (JFrame.MAXIMIZED_BOTH);
			}
		});
		//mtest = new test();
		int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int height = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	    Font font = new Font("Verdana", Font.BOLD, 40);
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 958, 762);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton button = new JButton("Return to Practice");
		button.addActionListener(e-> {
			//public void actionPerformed(ActionEvent e) {
				this.dispose();
				
		        TrainingUI training = new TrainingUI();
		        if (!training.isDisplayable()) { training.setUndecorated(true); }
		        training.setVisible(true);
			//}
		});		
		button.setFont(font);
		button.setBounds(width/10, 8*height/10, 500, 50);
		button.setHorizontalAlignment(SwingConstants.CENTER);
		button.putClientProperty("JComponent.sizeVariant", "large");		
		contentPane.add(button);
		
	    JLabel InstrLabel = new JLabel("<html>If you are ready to begin the test, press the BEGIN TEST button. If you would like to experiment with practicing dilation more, press the RETURN TO PRACTICE.</html>");
	    InstrLabel.setBounds(width/10, 2*height/10, 1500, 300);
	    InstrLabel.setHorizontalAlignment(SwingConstants.CENTER);
	    InstrLabel.setFont(font);
	    contentPane.add(InstrLabel);
	    
		JButton button_1 = new JButton("Begin Test");
		button_1.addActionListener(e-> {
			//public void actionPerformed(ActionEvent e) {
				this.dispose();
				
		        Test test = new Test();
		        if (!test.isDisplayable()) { test.setUndecorated(true); }
		        test.setVisible(true);
				test.setTitle("Test 1"); 
			//}
		});
		button_1.setFont(font);
		button_1.setBounds(6*width/10, 8*height/10, 500, 50);
		button_1.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(button_1);
	}

}
