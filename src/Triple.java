
public class Triple<X, Y, Z> { 

	public X noiseLevel; 
	public Y dilationRate; 
	public Z Sentence; 

  public Triple(X noiseLevel, Y dilationRate, Z Sentence) { 
    this.noiseLevel = noiseLevel; 
    this.dilationRate = dilationRate; 
    this.Sentence= Sentence; 
  } 
  
  public Triple () {
	  
  }
  
  public X getNoise() 		{ return noiseLevel; }
  public Y getDilation() 	{ return dilationRate; }
  public Z getSentence() 	{ return Sentence; }

  public void setNoise		(X noiseLevel) 		{ this.noiseLevel = noiseLevel; }
  public void setDilation	(Y dilationRate) 	{ this.dilationRate = dilationRate; }
  public void setSentence	(Z Sentence)		{ this.Sentence= Sentence; }

  public String toString() {
	  String string = new String("Noise level: " + noiseLevel + ", Dilation rate: " + dilationRate + ", Sentence: " + Sentence);
	  return string;
  }
} 
