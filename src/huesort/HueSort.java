/**
 * @author Miguel Bermudez
 * @date September 19, 2012
 *
 */

package huesort;

import processing.core.*;

import java.io.File;
import java.util.*;

import javax.media.jai.Histogram;

import toxi.color.*;
import toxi.color.theory.*;
import toxi.math.*;



public class HueSort extends PApplet {
	
	int IMG_NUM = 0;
	int IMG_SAMPLES = 100;
	float IMG_TOLERANCE = 0.20f;
	boolean BATCH_MODE = false;
	String LIST_OF_IMAGES = "data/artwork.txt";
	String SINGLE_IMAGE = "data/artwork/1x/DT45.jpg";
	
	PImage a;
	

	public void setup() {
		size(600,600);
		smooth();
		
		if (BATCH_MODE) {
			File f = new File(LIST_OF_IMAGES);
			//println("listOfImages: " + listOfImages); //DEBUGGING
			if(f.exists()) {
				String[] imageList = loadStrings(f);
				if (imageList.length > 0 ) {
					for (int i = 0; i < IMG_NUM; i++) {
						//println(imageList[i].trim()); //DEBUGGING		
						processNewImage(imageList[i].trim());
					}
				}
			} else {
				println("*** MISSING " + LIST_OF_IMAGES + " ****");
			}
		} else {
			processNewImage(SINGLE_IMAGE);
		}
		
		noLoop();
	}

	public void draw() {
	}
	
	
	
	/**
	 *  
	 * Begins the new load of each each and subsequent color data processing.
	 * 
	 * @param String imageFileName
	 */
	@SuppressWarnings("unused")
	void processNewImage(String imageFileName) {
		PImage a;
		a = loadImage(imageFileName);
		image(a, 0, 0);
		//ColorList aColorList = ColorList.createFromARGBArray(a.pixels, IMG_SAMPLES, false);
		toxi.color.Histogram hist = toxi.color.Histogram.newFromARGBArray(a.pixels, IMG_SAMPLES, IMG_TOLERANCE, false);
		//hist.compute(IMG_TOLERANCE, false);
		List<HistEntry> aHistEntries = hist.getEntries();

		
		int p =0;
		for (Iterator iterator = aHistEntries.iterator(); iterator.hasNext();) {
			HistEntry histEntry = (HistEntry) iterator.next();
			TColor t = histEntry.getColor();
			println(p + ":  Color: " + histEntry.getColor().toHex()+ "\t\t" + histEntry.getFrequency()); //DEBUGGING
			p++;
		}
		
		toxi.color.HistEntry h = aHistEntries.get(0);
		TColor t = h.getColor();
		println("Dominant Hue: " + t.hue());
		
		
	}
}
