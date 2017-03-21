// $Id: Block.java 1619 2013-03-20 03:47:14Z cengiz $

package block;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class Block implements Cloneable {
  int visited;			// How many times has this block been visited by an agent
  boolean blocked, nutritious;
  public int qValue;

  Block() {
    visited = 0;
    qValue = 0;
  }
  
  public boolean isBlocked() { return blocked; } // Can agent go thru this block ?
  public boolean isNutritious() { return nutritious; } // Can agent eat this ?
  public void stepOnIt() { visited++; }	// An agent stepped on this block
  public String toString() {
    if (Grid.displayType == 0) return " ";
    else return ""+qValue;
  }

  public int visitCount() { return visited; }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error("Fatal: cloning not supported?");
    }
  }
}
