import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.math.util.MultidimensionalCounter.Iterator;

public class DanUI {

	//  DilationSPIN is the entry point, and performs most of the set-up and infrastructure used later:
	//    Directory locations, pre-populated and pre-randomized lists of file objects, etc
	//    Audio is not pre-processed here, or it would not be garbage collected
	
	//  Strings and directory objects for various sound files, text files, and subject records
	//  All of these are based off baseDirectory, which should make modification simple. 
	//
	//  Note that constant elaboration of the protocol has rendered a once simple structure complex.
	
	//  							Base directory								
	static 	String 					baseDirectory 						= new String("C:\\Users\\Dan\\Desktop\\BaseDirectory");

	//								Location of Gettysburg Address file for training
	static 	String 					trainingDirectory 					= new String(baseDirectory + "\\Materials\\Audio Clips\\Training");

	//								Location of quizzes tracks
	static 	String 					quizzesTracksDirectory 					= new String(baseDirectory + "\\Materials\\Audio Clips\\Quizzes Tracks");

	//								Location of quizzes texts
	static 	String 					quizzesTextsDirectory 					= new String(baseDirectory + "\\Materials\\Audio Clips\\Quizzes Texts");	
	
	//								Location of six (6) "List 13" sentences for practice
	static 	String 					practiceDirectory 					= new String(baseDirectory + "\\Materials\\Audio Clips\\Practice");
	
	//								Location of five (5) sentences from each of Lists 10 and 11
	//								These test five of the six classic SPIN test points	(one list dilated, one not dilated)
	static	String 					testDirectory 						= new String(baseDirectory + "\\Materials\\Audio Clips\\Test");
	
	//								Location of eight (8) sentences from Lists 10, 11, and 12
	//								These test four of the non-standard SPIN test points
	//								This is a work-around; if designing from start, would do otherwise
	static	String 					testExtraDirectory 					= new String(baseDirectory + "\\Materials\\Audio Clips\\TestExtra");
	
	//								Location of text file(s) for the instructions
	static 	File 					instructionsDirectory 				= new File(baseDirectory + "\\Materials\\Instructions");

	//								Location of lists 1 through 5, corresponding to 5 of the 6 classic SPIN points
	//								These are used for the personalization phase
	static 	File 					personalDirectory 					= new File(baseDirectory + "\\Materials\\Audio Clips\\Personal");
	
	//								Location of lists 6 through 9, corresponding to 4 of the non-standard SPIN points
	//								These are also used for the personalization phase
	static 	File 					personalExtraDirectory 				= new File(baseDirectory + "\\Materials\\Audio Clips\\PersonalExtra");
	static 	File 					noiseDirectory 						= new File(baseDirectory + "\\Materials\\Audio Clips\\Noise");
	
	//								Location of 12 babble noise files
	//								Used in both personalization and test phases
	static 	File					SubjectDirectory 					= new File(baseDirectory + "\\Subject Records");

	// 								Location of the text file for the subject record
	//								Filename to be populated later, but needs to be allocated here as it is accessed from multiple threads. 
	static 	File					SubjectRecord;

	static 	int						maxPractice 						= 3;
	static 	int						maxPersonal 						= 3;
	static 	boolean 				locked 								= true;
	static 	String					Mode								= "Telephone";
	static	String					Phase								= "Setup";
	static	ArrayList<Double>		noiseLevels							= new ArrayList<Double>();
	static	ArrayList<Double>		noiseLevelsExtra					= new ArrayList<Double>();
	static  ArrayList<File>			personalizeDirs;
	static  ArrayList<File> 		listOfDirs; 
    public	static					TrainingUI							training;	
	static  ArrayList<ArrayList<Triple<Double,Double,File>>> PersonalList = new ArrayList<ArrayList<Triple<Double, Double, File>>>();
	
// integer-based Mode replaces boolean-based hearingAid
// this allows us to do reverse telephone mode, where noise is dilated
//      			Mode 0 ---> Telephone
//					Mode 1 ---> Hearing Aid
//					Mode 2 ---> Reverse Telephone
	static	ArrayList<Triple<Double, Double, File>> testList  = new ArrayList<Triple<Double, Double, File>>();
	
// 	width and height are programmatically derived pixel-referenced dimensions of the screen
//	Used in the rest of the UI threads to set object sizes appropriately on screens of different size and resolution
//	Scalefactor controls the size of slider knob and slider bar tickmarks
	
	static 	int		width 	=	(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	static 	int		height	= (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	static	int		scaleFactor = 3; //This is to set the size of the slider. Switch back to 
	//old value of 2500

	public static int test_num;
	
	public static int quizzes_num;
	public static int quiz_num;
	public static int[] remained_quizzes;
	
	public static void main(String[] args) {
		
//		Initialize the test_num
		test_num=1;
		
//		Initialize quizzes;
		remained_quizzes=new int[13];
		for (quizzes_num=0;quizzes_num<=12;quizzes_num++)
		{
			remained_quizzes[quizzes_num]=quizzes_num+1;
		}
		quizzes_num=13;
		
//		Noise levels intended for "standard" levels
		noiseLevels.add(-20.0);			
		noiseLevels.add(-15.0);
		noiseLevels.add(-10.0);			
		noiseLevels.add(-5.0);			
		noiseLevels.add(0.0);

// 		Noise levels intended for "extra" levels		
		noiseLevelsExtra.add(-2.5);		
		noiseLevelsExtra.add((double)(-6.666));		
		noiseLevelsExtra.add((double)(-8.333));		
		noiseLevelsExtra.add(-12.5);		
		
		// *************************************************************************************
		// Make test structure
		// *************************************************************************************
		
		// Get 1st standard list, random order
		// Make triples with null dilation rates
		File directory = new File(testDirectory + "\\List1");
		ArrayList<File> testFiles = new ArrayList<File>(Arrays.asList(directory.listFiles()));
		Collections.shuffle(testFiles);
		java.util.Iterator<File> FileIt = testFiles.iterator();
		java.util.Iterator<Double> NoiseIt = noiseLevels.iterator();
		while (FileIt.hasNext()) {
			Triple<Double, Double, File> t = new Triple<Double, Double, File>();
			t.setSentence(FileIt.next());
			t.setNoise(NoiseIt.next());
			testList.add(t);
		}
		
		// Get 2nd standard list, random order
		// Make triples with 0.99 dilation rates
		directory = new File(testDirectory + "\\List2");
		testFiles = new ArrayList<File>(Arrays.asList(directory.listFiles()));
		Collections.shuffle(testFiles);
		FileIt = testFiles.iterator();
		java.util.Iterator<Double> noiseIt = noiseLevels.iterator();
		while (FileIt.hasNext()) {
			Triple<Double, Double, File> t = new Triple<Double, Double, File>();
			t.setSentence(FileIt.next());
			t.setDilation(1.0);
			t.setNoise(noiseIt.next());
			testList.add(t);
		}

		// extended
		// Get extra list, 1-3 in random order then 4-6 in random order
		// Pair with extra noise levels, null or 0.99 dilation rates as appropriate
		directory = new File(testExtraDirectory + "\\List1");
		ArrayList<File> testFiles1 = new ArrayList<File>(Arrays.asList(directory.listFiles()));
		ArrayList<File> testFiles2 = new ArrayList<File>(Arrays.asList(directory.listFiles()));
		for (int i = 0; i<4; i++) {	testFiles1.remove(4); testFiles2.remove(0);}
		
		Collections.shuffle(testFiles1); Collections.shuffle(testFiles2);

		FileIt = testFiles1.iterator();
		java.util.Iterator<Double> noiseExtraIt = noiseLevelsExtra.iterator();
		while (noiseExtraIt.hasNext()) {
			Triple<Double, Double, File> t = new Triple<Double, Double, File>();
			t.setSentence(FileIt.next());
			t.setNoise(noiseExtraIt.next());
			testList.add(t);
		}

		FileIt = testFiles2.iterator();
		noiseExtraIt = noiseLevelsExtra.iterator();
		while (noiseExtraIt.hasNext()) {
			Triple<Double, Double, File> t = new Triple<Double, Double, File>();
			t.setSentence(FileIt.next());
			t.setDilation(1.0);
			t.setNoise(noiseExtraIt.next());
			testList.add(t);
		}

		// test print test structure
		
		java.util.Iterator<Triple<Double, Double, File>> TripleIt = testList.iterator();
//		System.out.println("Partially Populated Test structure");
//		while(TripleIt.hasNext()) { System.out.println(TripleIt.next()); }
//		System.out.println("\n\n");
		
		
		// *************************************************************************************
		// Make personalization structure
		// *************************************************************************************
		
		// Get the list of directories, as files
	    // Each directory will contain several sound clips, to be used with a sing noise level
		personalizeDirs = new ArrayList<File>(Arrays.asList(DanUI.personalDirectory.listFiles()));
		
		listOfDirs = new ArrayList<File>(Arrays.asList(personalDirectory.listFiles()));
		java.util.Iterator<File> DirIter = listOfDirs.iterator();
		Collections.shuffle(listOfDirs);
		NoiseIt = noiseLevels.iterator();

		while(DirIter.hasNext()) {

			File Dir = DirIter.next();
			ArrayList<File> listOfFiles = new ArrayList<File>(Arrays.asList(Dir.listFiles()));
			Collections.shuffle(listOfFiles);

			java.util.Iterator<File> FileIter = listOfFiles.iterator();
			ArrayList<Triple<Double, Double, File>> temp = new ArrayList<Triple<Double, Double, File>>();
			double Noise = NoiseIt.next();
			while(FileIter.hasNext()) {
				Triple<Double, Double, File> t = new Triple<Double, Double, File>();
				t.setSentence(FileIter.next());
				t.setNoise(Noise);
				temp.add(t);
			}
			PersonalList.add(temp);
		}

		personalizeDirs = new ArrayList<File>(Arrays.asList(DanUI.personalExtraDirectory.listFiles()));
		
		listOfDirs = new ArrayList<File>(Arrays.asList(personalExtraDirectory.listFiles()));
		DirIter = listOfDirs.iterator();
		Collections.shuffle(listOfDirs);
		NoiseIt = noiseLevelsExtra.iterator();

		while(DirIter.hasNext()) {

			File Dir = DirIter.next();
			ArrayList<File> listOfFiles = new ArrayList<File>(Arrays.asList(Dir.listFiles()));
			Collections.shuffle(listOfFiles);

			java.util.Iterator<File> FileIter = listOfFiles.iterator();
			ArrayList<Triple<Double, Double, File>> temp = new ArrayList<Triple<Double, Double, File>>();
			double Noise = NoiseIt.next();
			while(FileIter.hasNext()) {
				Triple<Double, Double, File> t = new Triple<Double, Double, File>();
				t.setSentence(FileIter.next());
				t.setNoise(Noise);
				temp.add(t);
			}
			PersonalList.add(temp);
		}
		
		Collections.shuffle(PersonalList);

		// test print of Personal Structure
		
		java.util.Iterator<ArrayList<Triple<Double, Double, File>>> ListIter = PersonalList.iterator();
//		while (ListIter.hasNext()) {
//			java.util.Iterator<Triple<Double, Double, File>> TempIt = ListIter.next().iterator();
//			while(TempIt.hasNext()) { System.out.println(TempIt.next()); }
//			System.out.println("\n");			
//		}
				
        InitUI init;
		init = new InitUI();
		init.setResizable(false);
		if (!init.isDisplayable()) { init.setUndecorated(true); }
		init.setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {}

}
