/**
 * @author Miguel Bermudez
 * @date September 19, 2012
 *
 */

package huesort;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.*;

import toxi.color.*;


public class HueSort extends PApplet {
	
	String listOfImages = "data/artwork.txt";
	PImage a;

	public void setup() {
		File f = new File(listOfImages);
		println("listOfImages: " + listOfImages);
		if(f.exists()) {
			String[] imageList = loadStrings(f);
			if (imageList.length > 0 ) {
				for (int i = 0; i < imageList.length; i++) {
					println(imageList[i].trim());
				}
			}
		} else {
			println("*** MISSING " + listOfImages + " ****");
		}
		
		noLoop();
	}

	public void draw() {
	}
}
