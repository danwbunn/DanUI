import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DilationPlaying {

	boolean 							playing = true;
    public DilationPlaying(boolean b) 	{ playing = b; }
    public DilationPlaying() 			{ playing = true; }  
    public void stop() 					{ playing = false; }
    public boolean isPlaying()			{ return playing; }

}

