// $Id: EmptyBlock.java 1622 2013-03-20 04:27:33Z cengiz $

package block;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// Redundant ?

public class EmptyBlock extends Block{
  public EmptyBlock() {
    super();
    nutritious = false;
    blocked = false; 
  }
}
