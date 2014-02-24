package pc.test;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import pc.vision.DistortionFix;
import pc.vision.PitchConstants;

// A class to test DistortionFix using an image of the pitch

public class DistortionFixTest {
	
	final PitchConstants pitchConstants = new PitchConstants(0);
	DistortionFix distortionFix;
	BufferedImage image;
	
	DistortionFixTest(String fileName) {
		distortionFix = new DistortionFix(pitchConstants);
		try
	    {
	      image = ImageIO.read(new File(fileName));
	    }
	    catch (Exception e)
	    {
	    	e.printStackTrace();
	    }
	}
	
	double fixImage() {
		long startTime = System.nanoTime();
		
		//fixes the image 20 times to simulate video handling
		for (int i=0; i<20; i++) {      
			BufferedImage fixed = DistortionFix.removeBarrelDistortion(image, 0, 0, 0, 0);
	    	/*try {
				ImageIO.write(fixed, "jpg",new File("/afs/inf.ed.ac.uk/user/s10/s1004618/git/SDP-Project-Group-9/PcProject/src/pc/test/pitch-out"+i+".jpg"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	      
	    long endTime = System.nanoTime();
		double duration = (double)(endTime - startTime) / 1000000.0;

		return duration;
	}
	
	public static void main(String args[]) {
		DistortionFixTest dft = new DistortionFixTest("/afs/inf.ed.ac.uk/user/s10/s1004618/git/SDP-Project-Group-9/PcProject/src/pc/test/pitch.jpg");
		System.out.println(dft.fixImage());
	}
}
