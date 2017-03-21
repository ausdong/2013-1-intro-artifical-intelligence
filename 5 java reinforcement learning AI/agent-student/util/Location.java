// $Id: Location.java 1622 2013-03-20 04:27:33Z cengiz $

package util;

import java.lang.*;
import java.util.*;

import agent.*;
import grid.*;
import block.*;
import iface.*;
import util.*;

public class Location {
  public Block block;
  public int x, y;
  public int qValue;
    
  public Location(Block block, int x, int y, int qValue) {
    this.block = block;
    this.x = x;
    this.y = y;
    this.qValue = qValue;
  }
}
