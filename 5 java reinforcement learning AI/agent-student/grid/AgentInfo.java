// $Id: AgentInfo.java 1621 2013-03-20 04:22:30Z cengiz $

package grid;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// Agent's connection to the Grid (like an interface)
public class AgentInfo {

  private int x,y;		// Location of agent
  AgentAI agent;
  AgentBlock agentBlock;	// The block that the agent is located
  Block occupiedBlock;		// The block that existed before agent came here
  Grid grid;			// Do we need Grid ?

    /** Agent has access to one bag to collect balls */
    BallBag bag;		

    /** Keep ball score */
    int ballTally = 0;

  // Constructor for imaginary interfaces
  public AgentInfo(AgentAI agent) {
    this.agent = agent;
  }

  // Constructor to match with a real grid
  public AgentInfo(AgentAI agent, int x, int y, Grid grid, Block occupiedBlock) {
    this.agent = agent;
    this.x = x;
    this.y = y;
    this.grid = grid;
    this.occupiedBlock = occupiedBlock;

    this.occupiedBlock.stepOnIt();	// Increase step counter
    this.bag = new BallBag(this);
  }

  // Put these in an interface: (?)
  public Block[][] see() {	// To make agent see surroundings
    return grid.see(x,y);
  }

  // Agent uses this to broadcast
  public void broadcastSurroundings(Block[][] surroundings) {
    Block[][] clonedSurroundings = new Block[3][3];

    // Clone surroundings first and give away copies and not originals coming from the grid!
    for (int y = 0; y < 3; y++) 
      for (int x = 0; x < 3; x++) {
	try {
	  clonedSurroundings[y][x] = (Block) surroundings[y][x].clone();
	} catch (NullPointerException e) {
	  // do nothing
	} 
      }

    grid.broadcastSurroundings(this, x, y, clonedSurroundings);
  }

  // Grid uses this to reach agent
    public void hearSurroundings(AgentInfo fromAgentInfo, int x, int y, Block[][] surroundings) {
	// disabled for CS325
	//((TalkingAgent)agent).hearSurroundings(fromAgentInfo, x - this.x, y - this.y, surroundings);
    }

  double smell() { return grid.smell(x,y); }	// To make agent smell food

  public void requestMove() throws AgentSuccessfulException {	// Ask agent to move
      try {
	  agent.move();
      } catch (ObstacleInPathException e) {
	  return;			// Couldn't move :(
      }
  }

  // Pick up something from the current block
  public void pickBall() {
      if (occupiedBlock instanceof BallBlock) { 
	  try {
	      bag.addBall();
	  } catch (BallBagFullException e) {
	      grid.iface.displayMessage(agent.getName() + ": BAG FULL!");
	      return;
	  }
	  occupiedBlock = new EmptyBlock(); // Empty it
      }
  }
  
  // Pick up something from the current block
  public Block pickUp() {
    Block retVal = occupiedBlock;
    occupiedBlock = new EmptyBlock(); // Empty it

    return retVal;
  }

    /** Bag full of balls? */
    public boolean isBagFull() {
	return bag.isFull();
    }

    public void depositBalls() {
	if (occupiedBlock instanceof DepositBinBlock) { 
	    ballTally += bag.deposit();
	}
	// display score on debug message
	grid.iface.displayMessage(agent.getName() + " deposited " + ballTally + " balls.");
	// and on display label
	grid.iface.updateDisplayVariable(agent.getName(), "" + ballTally);
    }

  // Put down something on the current block
  public void putDown(Block newBlock) {
    occupiedBlock = newBlock;
  }

  // Tell the grid about the move
  public Block move(int direction) throws ObstacleInPathException { 
    int newX, newY;
    Block occupiedBlock;	// Can it be avoided ?

    newX = x + Direction.xInc(direction);
    newY = y + Direction.yInc(direction);

    try {
      occupiedBlock = grid.move(x, y, direction, this.occupiedBlock);
    } catch (ObstacleInPathException e) {
      agent.ouch();		// Hit the wall!
      throw e;			// Do nothing, move unsuccessful
    } 
    
    x = newX; 
    y = newY;
    this.occupiedBlock = occupiedBlock;
    
    this.occupiedBlock.stepOnIt(); // Increase step counter
    
    // for testing
    //grid.iface.updateDisplayVariable(agent.getName(), 
    //				     "" + this.occupiedBlock.visitCount());

    return occupiedBlock;
  }

  public int getMoves() { return agent.getMoves(); }
}
