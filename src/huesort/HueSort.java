/**
 * @author Miguel Bermudez
 * @date September 19, 2012
 *
 */

package huesort;

import processing.core.*;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import toxi.color.*;
import toxi.color.theory.*;
import toxi.math.*;

//import colorLib.*;
//import colorLib.calculation.MedianCut;




public class HueSort extends PApplet {
	
	int IMG_NUM = 0; //DEBUGGING, set to 0 to use all images in text file
	int IMG_SAMPLES = 512;
	int IMG_RESIZE_MAXWIDTH = 120;
	int SWATCH_SIZE = 40; 
	int processcount = 0;
	
	Float IMG_TOLERANCE = 0.1f;
	Float MIN_BRIGHTNESS = 0.40f;
	Float MIN_SATURATION = 0.50f;
	
	Boolean BATCH_MODE = true;
	Boolean IMG_RESIZE = false;
	Boolean DRAW_INTO_BUFFER = true;
	Boolean DRAW_OUTPUT_FRAME = false;
	Boolean SAVE_IMAGE = true;
	
	String LIST_OF_IMAGES = "data/artwork.txt";
	String SINGLE_IMAGE = "data/images/Antoi_Jacqu_DT1992.jpg";
	File CURRENT_FILE;
	
	HashMap<String, TColor> imageHues = new HashMap<String, TColor>();
	Map<String, TColor> sortedimageHues;
	

	public void setup() {
		size(1400,800);
		smooth(); Locale locale = Locale.US;
		
		if (BATCH_MODE) {
			File f = new File(LIST_OF_IMAGES);
			//println("listOfImages: " + listOfImages); //DEBUGGING
			if(f.exists()) {
				String[] imageList = loadStrings(f);
				if (imageList.length > 0 ) { int count = IMG_NUM == 0 ? imageList.length : IMG_NUM;
					for (int i = 0; i < count; i++) {
						//println(imageList[i].trim()); //DEBUGGING
						File w = new File(imageList[i].trim());
						CURRENT_FILE = w;
						processNewImage(CURRENT_FILE.getPath());
					}
				}
			} else { println("*** MISSING " + LIST_OF_IMAGES + " ****");
			}
		} else {
			File w = new File(SINGLE_IMAGE);
			CURRENT_FILE = w;
			processNewImage(CURRENT_FILE.getPath());
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
	 * Begins the new load of each each and subsequent color data processing.
	 * 
	 * @param String imageFileName
	 */
	void processNewImage(String imageFileName) {
		println(String.format("Processing %d of %d -> %s", ++processcount, IMG_NUM, imageFileName));
		PImage quantizedImg, a;
		Histogram quantizedHist;
		
		a = loadImage(imageFileName);
		quantizedImg = processViaMedianCut(a, 5);
		
		quantizedHist = Histogram.newFromARGBArray(quantizedImg.pixels, quantizedImg.pixels.length/10, IMG_TOLERANCE, false);
		
		//get the first entry, which is the most numerous
		TColor t =  (quantizedHist.getEntries().get(0)).getColor();
		
		//find something brighter
		quantloop:
		for (int j = 0; j < quantizedHist.getEntries().size(); j++) {
			HistEntry h = quantizedHist.getEntries().get(j);
			TColor _t = h.getColor();
			
			if (t.brightness() > MIN_BRIGHTNESS) {
//				exit out of loop on first occurrence of something brighter than threshold
				t = _t;
//				println(String.format("Found brightness: %f", t.brightness()));
				break quantloop;
//				break;
			} 
		}
		
		//add filename and hue to hashmap
		imageHues.put(CURRENT_FILE.getPath(), t);
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
	 * Take source image and reduce to the number of colors 
	 * and save in the work image
	 * 
	 * @param PImage mSourceImg
	 * @param int numColors
	 * 
	 * @return PImage
	 */
		
	PImage processViaMedianCut(PImage mSourceImg, int numColors) {
		PImage image_reduced;
		
		MedianCut mmc = new MedianCut(mSourceImg.pixels, mSourceImg.width, mSourceImg.height);
		mmc.convert(numColors);
		Image mmcImage = mmc.makeImage();
		
		image_reduced = new PImage(mmcImage);
		return image_reduced;
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
		
		for (Entry<String, TColor> entry : sortedimageHues.entrySet())
		{		    
			String file = entry.getKey(); 
			PImage p = loadImage(file);
			PImage outputframe;
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
				  pg.rect(0, 0, SWATCH_SIZE, SWATCH_SIZE);
				  pg.fill(TColor.WHITE.toARGB());
				  pg.text("h:"+c.hue(), 0, (1.2f*SWATCH_SIZE));
				  //pg.text("s:"+c.saturation(), 0, (1.2f*swatchSize + 14));
				  //pg.text("b:"+c.brightness(), 0, (1.2f*swatchSize + 28));
				pg.endDraw();
				
				outputframe = createImage(p.width, p.height, ARGB);
				outputframe = pg.get();
				if (DRAW_OUTPUT_FRAME) {
					image(outputframe, widthCounter, lineCounter);
				}
				println(String.format("%d-%s", filecount, imageBasename ));
				if (SAVE_IMAGE) {
					outputframe.save(String.format("./hueImages/%d-%s", filecount, imageBasename ));
				}
			} else {
				image(p, widthCounter, lineCounter);
				fill(c.toARGB());
				stroke(TColor.WHITE.toARGB());
				rect(widthCounter, lineCounter, SWATCH_SIZE, SWATCH_SIZE);
				fill(TColor.WHITE.toARGB());
				text(c.hue(), widthCounter, lineCounter+(1.5f*SWATCH_SIZE));
			}
			
//			println( ": " + file + "\t\tHue: " + c.hue() 
//					+ "\tColor: "+ c.toHex()); 
			    
			widthCounter+=p.width;
			filecount++;
					  
		}
	}
	
	
}//Class
