// $Id: Direction.java 1622 2013-03-20 04:27:33Z cengiz $

package grid;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class Direction {
  public static final int EAST=0, SOUTH=1, WEST=2, NORTH=3;

  int direction;

  public Direction(int direction) {
    // Assert correctness and throw some exception...
    this.direction = direction;
  }

  public String toString() {
    switch (direction) {
    case Direction.NORTH:
      return "North";
    case Direction.SOUTH:
      return "South";
    case Direction.WEST:
      return "West";
    case Direction.EAST:
      return "East";
    default:
      throw new Error("Direction out of range."); // Fatal
    }    
  }

  public int value() { return direction; }

  public static int xInc(int direction) {
    int x=0;

    switch (direction) {
    case Direction.NORTH:
      break;
    case Direction.SOUTH:
      break;
    case Direction.WEST:
      x = -1;
      break;
    case Direction.EAST:
      x = +1;
      break;
    default:
      throw new Error("Direction out of range."); // Fatal
    }
    return x;
  }

  public static double
  dirArctan(int x1, int y1, int x2, int y2) {
  
    double angle = java.lang.Math.atan2(y2 - y1, x2 - x1);

    // Convert polarized angle to absolute angle
    if (angle < 0) angle = 2*java.lang.Math.PI + angle;
    
    return angle / (Math.PI/2);	// Return direction between 0-4, not 0-2pi
  }


  public static int 
  yInc(int direction) {
    int y=0;

    switch (direction) {
    case Direction.NORTH:
      y = -1;
      break;
    case Direction.SOUTH:
      y = +1;
      break;
    case Direction.WEST:
      break;
    case Direction.EAST:
      break;
    default:
      throw new Error("Direction out of range."); // Fatal
    }
    return y;
  }
}
