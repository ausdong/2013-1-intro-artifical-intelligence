// $Id: ObstacleInPathException.java 1604 2013-03-18 04:46:51Z cengiz $

package agent;

import agent.*;
import grid.*;
import block.*;
import iface.*;


public class ObstacleInPathException extends Exception {
  public ObstacleInPathException() {
    super();
  }

  public ObstacleInPathException(String name) {
    super(name);
  }
}
