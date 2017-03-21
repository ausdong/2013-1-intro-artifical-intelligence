// $Id: ObstacleInPathException.java 1602 2013-03-17 04:43:04Z cengiz $

package grid;

import agent.*;
import grid.*;
import block.*;
import iface.*;


public class BallBagFullException extends Exception {
  public BallBagFullException() {
    super();
  }

  public BallBagFullException(String name) {
    super(name);
  }
}
