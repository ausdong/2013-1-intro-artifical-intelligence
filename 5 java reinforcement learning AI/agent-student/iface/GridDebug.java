// $Id: GridDebug.java 1619 2013-03-20 03:47:14Z cengiz $

package iface;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public interface GridDebug extends  Debug {
  void setGrid(Grid grid);
  void displayScene();
  void displayFrequency();
  void updateUI();
}
