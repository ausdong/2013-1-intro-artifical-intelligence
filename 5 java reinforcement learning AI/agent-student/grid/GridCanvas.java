// $Id: GridCanvas.java 1638 2013-03-23 20:40:26Z cengiz $

package grid;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class GridCanvas extends Canvas /*implements GridDebug*/ {
  Grid grid;

  public final static int BOXWIDTH = 20;
  boolean debugSwitch = true;
  int width, height;
  
  public GridCanvas(Grid grid) {
    this.grid = grid;
  }
  
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;

    String textImage;

    synchronized (grid) {
      // Update size of grid since it might have been changed
      width = grid.getWidth();
      height = grid.getHeight();

      // Note from CG: Historically the text map was used to convert to graphics. But
      // later I added direct access to the map without changing it all to
      // read from the map.
      // could do this now: Block[][] map = grid.getMap();
      
      // Get textual representation of grid
      try {
	  textImage = "" + grid; // calls toString()
      } catch (NullPointerException e) {
	textImage = " ";
      }
    }

    //FoodHunt.debug("GridCanvas.Paint(), size of canvas: "+getSize());

    // Find line length
    final int START = 0;

    //FoodHunt.debug("GridCanvas.Paint(), width of grid: "+width);

    StringReader stream = new StringReader(textImage);

    int ch, index = 0, x, y;
    Color savedColor = g.getColor();

    // Grid lines
    g.setColor(Color.black);
    
    // Vertical lines along the x axis
    for (x=0; x<=width; x++)
      g.drawLine(x * BOXWIDTH, 0, x * BOXWIDTH, height * BOXWIDTH);

    // Horizontal lines along the y axis
    for (y=0; y<=height; y++)
      g.drawLine(0, y * BOXWIDTH, width * BOXWIDTH, y * BOXWIDTH);

    try {
      while ( (ch = stream.read()) != -1) {
	x = (index%width)*BOXWIDTH;
	y = (index/width)*BOXWIDTH;
	switch (ch) {
	case 'X':		// Wall
	  g.setColor(Color.gray);
	  g.fill3DRect(x,y,BOXWIDTH,BOXWIDTH,true);
	  break;
	case 'A':		// Agent
	  AgentAI agent = 
	    ((AgentBlock) grid.getBlock(index%width, (int)index/width)).getAgent();
	  g2.setColor(agent.getColor());
	  g2.fillOval(x, y, BOXWIDTH, BOXWIDTH);
	  // Talk to grid and get the agent's first initial
	  g2.setColor(Color.white);
	  g2.drawString(agent.getName().charAt(0) + "", 
			x+BOXWIDTH/3, y+2*BOXWIDTH/3);
	  break;
	case 'F':		// Food
	  g2.setColor(Color.blue);
	  g2.fillOval(x, y, BOXWIDTH, BOXWIDTH);
	  break;
	case '?':		// Not explored
	  g2.setColor(getBackground());
	  g2.fillRect(x, y, BOXWIDTH, BOXWIDTH);
	  g2.setColor(Color.black);
	  g2.drawString("?", x+BOXWIDTH/2, y+BOXWIDTH/2);
	  break;
	case '\n':
	  continue;
	case ' ':
	  break;
	default:
	  g2.setColor(Color.black);
	  g2.drawString(String.valueOf((char)ch), x+BOXWIDTH/2, y+BOXWIDTH/2);
	  break;
	}
	index++;
	if (ch/*stream.read()*/ == -1) break;
	
      }
    } catch (IOException e) {
      throw new Error(""+e);
    }
    
    g.setColor(savedColor);
  } 
  
   public Dimension getMinimumSize() {
     //grid.iface.displayMessage("getMinimumSize() is called.");
     return new Dimension(grid.getWidth() * BOXWIDTH + 1, (grid.getHeight()+1) * BOXWIDTH + 1);
   }
   public Dimension getPreferredSize() {
     return getMinimumSize();
   }

  public void setGrid(Grid grid) {
    synchronized (this.grid) {
      this.grid = grid;
    }
  }

}
