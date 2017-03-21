// $Id: FoodBlock.java 1622 2013-03-20 04:27:33Z cengiz $

package block;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// Redundant ?

public class FoodBlock extends Block{
  public FoodBlock() {
    super();
    nutritious = true;
    blocked = false; 
  }
  public String toString() { return "F"; }
}
