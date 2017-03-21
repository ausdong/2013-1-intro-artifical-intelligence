// $Id: AgentDebug.java 1602 2013-03-17 04:43:04Z cengiz $

package iface;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public interface AgentDebug extends Debug {
  void setAgent(AgentAI agent);
  void displayDirection(double direction);
}
