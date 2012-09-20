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

import toxi.color.*;
import toxi.color.theory.*;
import toxi.math.*;



public class HueSort extends PApplet {
	
	int IMG_NUM = 90; //DEBUGGING, set to 0 to use all images in text file
	int IMG_SAMPLES = 1000;
	Float IMG_TOLERANCE = 0.10f;
	Boolean BATCH_MODE = true;
	String LIST_OF_IMAGES = "data/artwork.txt";
	String SINGLE_IMAGE = "data/artwork/1x/DT45.jpg";
	
	PImage a;
	HashMap<String, TColor> imageHues = new HashMap<String, TColor>();
	Map<String, TColor> sortedimageHues;
	

	public void setup() {
		size(1400,800);
		smooth();
		
		if (BATCH_MODE) {
			File f = new File(LIST_OF_IMAGES);
			//println("listOfImages: " + listOfImages); //DEBUGGING
			if(f.exists()) {
				String[] imageList = loadStrings(f);
				if (imageList.length > 0 ) {
					int count = IMG_NUM == 0 ? imageList.length : IMG_NUM;
					for (int i = 0; i < count; i++) {
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
		drawPImages();
		
		println ("\nUsing SAMPLES: " + IMG_SAMPLES + " TOLERANCE: " + IMG_TOLERANCE);
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
		toxi.color.Histogram hist = toxi.color.Histogram.newFromARGBArray(a.pixels, IMG_SAMPLES, IMG_TOLERANCE, false);
		
		List<HistEntry> aHistEntries = hist.getEntries();
		
		//fist element of histogram is the most frequent color
		toxi.color.HistEntry h = aHistEntries.get(0);
		TColor t = h.getColor();
		//println("Dominant Hue: " + t.hue() + "\t Color: " + t.toHex() + "\t file: " + imageFileName); //DEBUGGING
		
		//add to hues hashmap
		imageHues.put(imageFileName, t);
	}
	
	
	/**
	 *  
	 * Sort Image Hue Hash By Value
	 * 
	 */
	void sortHueHash() {
		// http://stackoverflow.com/questions/10711413/sort-java-hashmap-by-values-using-on-log-n-complexity
		//this is really ugly but couldn't figure out a cleaner way to sort a HashMap with a complex value
		//instead of a just simple value
		
		List<Entry<String, TColor>> entries 
			= new ArrayList<Entry<String, TColor>>((Collection<? extends Entry<String, TColor>>) imageHues.entrySet());
		
		Collections.sort(entries, new Comparator<Entry<String, TColor>>() {
			public int compare(Entry<String, TColor> left, Entry<String, TColor> right) {
				Float rightHue = right.getValue().hue();
				Float leftHue = left.getValue().hue();
				return rightHue.compareTo(leftHue);
			}
		});

		sortedimageHues= new LinkedHashMap<String, TColor>(entries.size());
		
		for (Entry<String, TColor> entry : entries) {
			sortedimageHues.put(entry.getKey(), entry.getValue());
		}
		
		//println(sortedimageHues.toString()); //DEBUGGING
	}
	
	/**
	 *  
	 * Draw Images to screen with color swatch on top
	 * 
	 */
	void drawPImages() {
		int widthCounter = 0;
		int lineCounter = 0;
		int maxHeight = 0;
		int swatchSize = 30;
		
		int i = 0;
		for (Entry<String, TColor> entry : sortedimageHues.entrySet())
		{		    
			String file = entry.getKey(); 
			PImage p = loadImage(file);
			TColor c = entry.getValue();
			
			p.resize(0, 100);
			maxHeight = max(p.height, maxHeight);
			if (widthCounter + p.width >= width) {
				widthCounter = 0;
				lineCounter += maxHeight;
				maxHeight = 0;
			}
			
			image(p, widthCounter, lineCounter);
			fill(c.toARGB());
			stroke(TColor.WHITE.toARGB());
			//rect(widthCounter, lineCounter+p.height-20, 20, 20);
			rect(widthCounter, lineCounter, swatchSize, swatchSize);
			fill(TColor.WHITE.toARGB());
			text(c.hue(), widthCounter, lineCounter+(1.5f*swatchSize));
			println(i + ": " + file + "\t\tHue: " + c.hue() 
					+ "\tColor: "+ c.toHex()); 
			    
			widthCounter+=p.width;
			i++;
					  
		}
	}
	
	
}//Class
