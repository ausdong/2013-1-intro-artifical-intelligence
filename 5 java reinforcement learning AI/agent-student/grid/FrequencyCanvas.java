// $Id: FrequencyCanvas.java 1607 2013-03-18 22:57:46Z cengiz $

package grid;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.color.*;
import java.io.*;
import java.lang.*;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class FrequencyCanvas extends GridCanvas {
  boolean alreadyPainting = false;
  Boolean paintRequest = new Boolean(false);

  // TODO: Use double-buffering
  BufferedImage offScreenBuffer;

  public FrequencyCanvas(Grid grid) {
    super(grid); 
  }
  
  public void paint(Graphics g) {

    try {
      while (true) {
	synchronized (paintRequest) {
//	  if (paintRequest.booleanValue()) grid.iface.displayMessage(grid.name+": canvas repeating paint for previously suspended request.");
	  paintRequest = new Boolean(false);	// Satisfying request now
	}
	offScreenBuffer = new BufferedImage((int)getPreferredSize().getWidth(),
					    (int)getPreferredSize().getHeight(),
					    BufferedImage.TYPE_USHORT_555_RGB);

	Graphics2D doubleBuffer = offScreenBuffer.createGraphics();

	doubleBuffer.setColor(getBackground());
	doubleBuffer.fillRect(0, 0, offScreenBuffer.getWidth(), offScreenBuffer.getHeight());
    
	super.paint(doubleBuffer);

	Graphics2D g2 = doubleBuffer /*(Graphics2D) g*/;

	//FoodHunt.debug("GridCanvas.Paint(), size of canvas: "+getSize());

	int x, y, maxCount = 0, minCount = Integer.MAX_VALUE;

	int count[][] = new int[height][width];

	synchronized (grid) {
	  for (y=0; y<height; y++)
	    for (x=0; x<width; x++) {
	      try {			// Catch null pointer exception!
		count[y][x] = grid.getBlock(x, y).qValue/*visitCount()*/; // Q or visit frequency ?
	      } catch (NullPointerException e) {	
		count[y][x] = 0;
	      }
	      maxCount = Math.max(count[y][x], maxCount);
	      if (count[y][x] > 0) minCount = Math.min(count[y][x], minCount);
	    }
	}

	Color maxColor = Color.red;
	float colorComponents[] = maxColor.getComponents(null);
	ColorSpace cSpace = maxColor.getColorSpace();

	if (maxCount != 0) {

	  Color savedColor = g2.getColor();

	  int origX, origY, curOrigX = 0, curOrigY = 0;
	  Shape rectangle = new Rectangle(0, 0, BOXWIDTH, BOXWIDTH);

	  double rawAlpha;

	  for (y=0; y<height; y++)
	    for (x=0; x<width; x++) {
	      rawAlpha = ((double) (count[y][x] - minCount)) / maxCount;
	      //grid.iface.displayMessage("FrequencyCanvas.paint().rawAlpha = "+rawAlpha+", minCount = "+minCount);

	      if (count[y][x] > minCount ) {
		// Translate origin instead of creating new rectangle
		origX = x * BOXWIDTH - curOrigX;
		origY = y * BOXWIDTH - curOrigY;
		g2.translate(origX, origY);
		curOrigX += origX; curOrigY += origY;

		g2.setColor(new Color(cSpace, colorComponents, (float)
				      Math.log((Math.E-1) * rawAlpha + 1)));
		g2.fill(rectangle);
		// 		  FoodHunt.debug("FrequencyCanvas: x: "+x+", y: "+y+
		// 	  	 ", alpha:"+((float) count[y][x])/maxCount);
	      }
	    }

	  g2.setColor(savedColor);
	}

	synchronized(offScreenBuffer) {
	  if (!g.drawImage(offScreenBuffer, 0, 0, getBackground(), this)) {
	    try {
	      wait();			// Wait for notify
	    } catch (InterruptedException e) {
	      grid.iface.displayMessage(""+e);
	    }
	  }
	}
	synchronized (paintRequest) { 
	  if (!paintRequest.booleanValue()) break;	// Out of while
	}
      }
    } finally {
      synchronized (paintRequest) { 
//	if (alreadyPainting) grid.iface.displayMessage(grid.name+": canvas terminated painting.");
	alreadyPainting = false;	// Finished painting

      }
    }
  } 
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    grid.iface.displayMessage("imageUpdated.");
    notify();
    return false;		// No further updates necessary
  }

  boolean a;
  public void update(Graphics g) {
    paint(g);			// Don't clear like the ancestor does!
  }

  public void requestRepaint() {
    boolean repaintable = false;

    synchronized (paintRequest) {
      if (alreadyPainting) { 
	//paintRequest = new Boolean(true); 
	//grid.iface.displayMessage(grid.name+": canvas skipping painting, and scheduling paint request when finished.");
	return;
      } else {
//	grid.iface.displayMessage(grid.name+": canvas started painting.");

	alreadyPainting = true;	// We started painting (better call repaint() soon after this)
	repaintable = true;
      }
    }

    if (repaintable) {
      repaint();	// Out of the synchronized block!
      
    }
  }

/*
  public void invalidate() {
    //doLayout();
    getParent().invalidate();
    //repaint();
  }*/
}
