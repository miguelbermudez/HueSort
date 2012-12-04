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
	
	int IMG_NUM = 200; //DEBUGGING, set to 0 to use all images in text file
	int IMG_SAMPLES = 2000;
	int IMG_RESIZE_MAXWIDTH = 200;
	Float IMG_TOLERANCE = 0.1f;
	Float SAT_TOLERANCE = 0.4f;
	Float BRIGHT_TOLERANCE = 0.20f;
	
	Boolean BATCH_MODE = true;
	Boolean IMG_RESIZE = true;
	Boolean DRAW_INTO_BUFFER = true;
	Boolean FILTER_COLORS = true;
	
	String LIST_OF_IMAGES = "data/artwork.txt";
	String SINGLE_IMAGE = "data/artwork/1x/DT45.jpg";
	
	PImage a;
	PGraphics pg;
	HashMap<String, TColor> imageHues = new HashMap<String, TColor>();
	Map<String, TColor> sortedimageHues;
	

	public void setup() {
		size(1400,800);
		smooth();
		
		Locale locale = Locale.US;
		
		if (BATCH_MODE) {
			File f = new File(LIST_OF_IMAGES);
			//println("listOfImages: " + listOfImages); //DEBUGGING
			if(f.exists()) {
				String[] imageList = loadStrings(f);
				if (imageList.length > 0 ) {
					int count = IMG_NUM == 0 ? imageList.length : IMG_NUM;
					for (int i = 0; i < count; i++) {
						//println(imageList[i].trim()); //DEBUGGING
						File w = new File(imageList[i].trim());
						//processNewImage(imageList[i].trim());
						processNewImage(w.getPath());
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
		
		//println ("\nUsing SAMPLES: " + IMG_SAMPLES + " TOLERANCE: " + IMG_TOLERANCE);
		noLoop();
	}

	public void draw() {
	}
	
	/**
	 *  
	 * Get a histogram entry from the passed in list. If that hist entry's 
	 * brightness or saturation is below a certain threshold, try again.
	 * 
	 * Return the first result that satisfies those requirements.
	 * 
	 * @param List<HistEntry> colorlist
	 * @return 
	 * @return TColor t
	 */
	TColor getHisEntry( List<HistEntry> colorHistEntires) {
		TColor t;
		TColor selected_t = (colorHistEntires.get(0)).getColor();
		toxi.color.HistEntry h;
		int maxListIndex = colorHistEntires.size();
		int listCount = 0;
		
		mainloop:
		for (int i=0; i<colorHistEntires.size(); i++) {
			h = colorHistEntires.get(i);
			t = h.getColor();
			
			if (t.saturation() > SAT_TOLERANCE && t.brightness() > BRIGHT_TOLERANCE) {
				selected_t = t;
				listCount = i;
				break mainloop;
			}
		}
		//println(String.format("Returned color at %d - sat: %f, sat-tol: %f", listCount, selected_t.saturation(), SAT_TOLERANCE));
		return selected_t;
	}
	
	
	/**
	 *  
	 * Begins the new load of each each and subsequent color data processing.
	 * 
	 * @param String imageFileName
	 */
	void processNewImage(String imageFileName) {
		//PImage a;
		a = loadImage(imageFileName);
		//toxi.color.Histogram hist = toxi.color.Histogram.newFromARGBArray(a.pixels, IMG_SAMPLES, IMG_TOLERANCE, false);
		toxi.color.Histogram hist = toxi.color.Histogram.newFromARGBArray(a.pixels, a.pixels.length/4, IMG_TOLERANCE, true);

		
		List<HistEntry> aHistEntries = hist.getEntries();
		
		TColor t = TColor.newRandom();
		if (FILTER_COLORS) {
			//fist element of histogram is the most frequent color
			t = getHisEntry(aHistEntries);	
		} else {
		    toxi.color.HistEntry h = aHistEntries.get(0);
		    t = h.getColor();
		}
		
//		println("Dominant Hue: " + t.hue() + "\t Color: " + t.toHex() + "\t file: " + imageFileName); //DEBUGGING
//		println("\tIs color grey?: " + t.isGrey() + " file: " + imageFileName); //DEBUGGING
//		println("\tBrightness: " + t.brightness() + " Saturation: " + t.saturation() + "\t file: " + imageFileName + "\n"); //DEBUGGING
		
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
		int filecount = 0;
		int swatchSize = 50;
		
		
		int i = 0;
		for (Entry<String, TColor> entry : sortedimageHues.entrySet())
		{		    
			String file = entry.getKey(); 
			PImage p = loadImage(file);
			PImage frame;
			PGraphics pg; 
			TColor c = entry.getValue();
			File imageFilename = new File(file);
			String imageBasename = imageFilename.getName();
			
			if (IMG_RESIZE) {
				p.resize(0, IMG_RESIZE_MAXWIDTH);	
			} 
			
			maxHeight = max(p.height, maxHeight);
			if (widthCounter + p.width >= width) {
				widthCounter = 0;
				lineCounter += maxHeight;
				maxHeight = 0;
			}
			
						
			if (DRAW_INTO_BUFFER) {
				pg = createGraphics(p.width, p.height);
				pg.beginDraw();
			      pg.background(230);
				  pg.image(p, 0, 0, p.width, p.height);
				  pg.fill(c.toARGB());
				  pg.stroke(TColor.WHITE.toARGB());
				  pg.rect(0, 0, swatchSize, swatchSize);
				  pg.fill(TColor.WHITE.toARGB());
				  pg.text(c.hue(), 0, (1.5f*swatchSize));
				pg.endDraw();
				
				frame = createImage(p.width, p.height, ARGB);
				frame = pg.get();
				image(frame, widthCounter, lineCounter);
				println(String.format("%d-%s", filecount, imageBasename ));
				//frame.save(String.format("./hueImages/%d-%s", filecount, imageBasename ));
			} else {
				image(p, widthCounter, lineCounter);
				fill(c.toARGB());
				stroke(TColor.WHITE.toARGB());
				//rect(widthCounter, lineCounter+p.height-20, 20, 20);
				rect(widthCounter, lineCounter, swatchSize, swatchSize);
				fill(TColor.WHITE.toARGB());
				text(c.hue(), widthCounter, lineCounter+(1.5f*swatchSize));
			}
			
//			println(i + ": " + file + "\t\tHue: " + c.hue() 
//					+ "\tColor: "+ c.toHex()); 
			    
			widthCounter+=p.width;
			i++;
			filecount++;
					  
		}
	}
	
	
}//Class
