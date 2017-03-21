// $Id: DepositBinBlock.java 1622 2013-03-20 04:27:33Z cengiz $

package block;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class DepositBinBlock extends Block{
    public DepositBinBlock() {
    super();
    nutritious = false;
    blocked = false; 
  }
  public String toString() { 
    return "D";
  }

}
