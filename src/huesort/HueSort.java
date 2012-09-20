/**
 * @author Miguel Bermudez
 * @date September 19, 2012
 *
 */

package huesort;

import processing.core.*;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import javax.media.jai.Histogram;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import toxi.color.*;
import toxi.color.theory.*;
import toxi.math.*;



public class HueSort extends PApplet {
	
	int IMG_NUM = 3;
	int IMG_SAMPLES = 200;
	Float IMG_TOLERANCE = 0.10f;
	boolean BATCH_MODE = true;
	String LIST_OF_IMAGES = "data/artwork.txt";
	String SINGLE_IMAGE = "data/artwork/1x/DT45.jpg";
	
	PImage a;
	HashMap<String, Float> imageHues = new HashMap<String, Float>();
	

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
		
		sortHueHash();
		
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
		toxi.color.Histogram hist = toxi.color.Histogram.newFromARGBArray(a.pixels, IMG_SAMPLES, IMG_TOLERANCE, false);
		List<HistEntry> aHistEntries = hist.getEntries();

		/*
		int p =0;
		for (Iterator iterator = aHistEntries.iterator(); iterator.hasNext();) {
			HistEntry histEntry = (HistEntry) iterator.next();
			TColor t = histEntry.getColor();
			println(p + ":  Color: " + histEntry.getColor().toHex()+ "\t\t" + histEntry.getFrequency()); //DEBUGGING
			p++;
		}
		*/
		
		//fist element of histogram is the most frequent color
		toxi.color.HistEntry h = aHistEntries.get(0);
		TColor t = h.getColor();
		//println("Dominant Hue: " + t.hue() + "\t Color: " + t.toHex() + "\t file: " + imageFileName); //DEBUGGING
		
		//add to hues hashmap
		imageHues.put(imageFileName, t.hue());
	}
	
	
	/**
	 *  
	 * Sort Image Hue Hash By Value
	 * 
	 */
	void sortHueHash() {
		// http://stackoverflow.com/questions/10711413/sort-java-hashmap-by-values-using-on-log-n-complexity
		
		List<Entry<String, Float>> entries 
			= new ArrayList<Entry<String, Float>>(imageHues.entrySet());
		
		Collections.sort(entries, new Comparator<Entry<String, Float>>() {
			public int compare(Entry<String, Float> left, Entry<String, Float> right) {
			    return right.getValue().compareTo(left.getValue());
			}
		});

		Map<String, Float> sortedMap = new LinkedHashMap<String, Float>(entries.size());
		
		for (Entry<String, Float> entry : entries) {
		  sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		println(sortedMap.toString()); //DEBUGGING
	}
	
	
}//Class
