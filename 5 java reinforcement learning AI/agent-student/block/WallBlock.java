// $Id: WallBlock.java 1622 2013-03-20 04:27:33Z cengiz $

package block;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class WallBlock extends Block{
  public WallBlock() {
    super();
    nutritious = false;
    blocked = true; 
  }

  public String toString() { 
    if (Grid.displayType == 0) return "X";
    else return "\\";
  }
}
