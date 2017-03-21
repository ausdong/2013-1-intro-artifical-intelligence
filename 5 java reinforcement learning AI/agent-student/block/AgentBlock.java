// $Id: AgentBlock.java 1634 2013-03-22 21:44:04Z cengiz $

package block;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// Redundant ?

public class AgentBlock extends Block{
  AgentAI _AICore;

  public AgentBlock(AgentAI _AICore) {
    super();
    nutritious = false;
    blocked = true; 

    this._AICore = _AICore;
  }

  public String toString() { return "A"; }

  public AgentAI getAgent() { return _AICore; }
}
