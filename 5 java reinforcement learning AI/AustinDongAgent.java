// $Id: LearnerAgent.java 1604 2013-03-18 04:46:51Z cengiz $

/** INSTRUCTIONS
  *
  * - Rename this file and your agent class below with your own
  * name. Keep it in this directory and under the package
  * "agent.ballgame".
  *
  * - It MUST extend ExplorerAgent
  *
  * - Implement value iteration or Q-learning as an INNER class defined
  * WITHIN your agent class in this file. You will turn in ONE java
  * file.
  */

package agent.ballgame;

import java.lang.*;
import java.util.*;
import java.awt.Color;

import agent.*;
import grid.*;
import block.*;
import iface.*;
import util.*;

/** LearnerAgent's aim is to learn navigation in the grid to access goal locations. */
public class AustinDongAgent extends ExplorerAgent { 
  
  /** Example inner class for Q-learning */
  public class QLearning {
    /** map */
    Grid grid;
    
    public QLearning(Grid grid) {
      this.grid = grid;
    }
    
    public void setGrid(Grid newGrid) {
      grid = newGrid;
    }
    
    public void reset() {
      // ...
    }
    
    public void learn(LinkedList goalLocations) {
      // Q-learning algorithm here. Update forall(x,y)
      // grid.map[x][y].qValue here, which will be automatically
      // colored on the display.
    	Block b;
    	int width = grid.getWidth();
        int height = grid.getHeight();
	    switch (mindSet) {
	      case MINDSET_EXPLORE:
	          for(int y=-1; y<height+1; y++){
	          	for(int x=-1; x<width+1; x++){  
	          		b = grid.getBlock(x,y);
	          		if(b instanceof WallBlock) b.qValue = 0;
	          		else if(b instanceof Block){
	          			if(b.visitCount()==0) b.qValue = 100;
	          			else b.qValue = 0;
	          		}
	          	}
	          }
	      case MINDSET_GATHER:
	          for(int y=-1; y<height+1; y++){
	          	for(int x=-1; x<width+1; x++){  
	          		b = grid.getBlock(x,y);
	          		if(b instanceof BallBlock) b.qValue = 100;
	          		else b.qValue = 0;
	          	}
	          }
	      case MINDSET_DELIVER:
	          for(int y=-1; y<height+1; y++){
	          	for(int x=-1; x<width+1; x++){  
	          		b = grid.getBlock(x,y);
	          		if(b instanceof DepositBinBlock) b.qValue = 100;
	          		else b.qValue = 0;
	          	}
	          }
	      default:
	        throw new Error("Fatal: unrecognized mindset, what was I thinking?");
	    }

    }
    
    public int decideDirection(int x, int y) {
      // use the learned q-values to make a decision at point (x,y)
      int width = grid.getWidth();
      int height = grid.getHeight();
      goalLocations = new LinkedList();
      for(y=-1; y<height+1; y++){
    	for(x=-1; x<width+1; x++){  
    		if(isFavorable(x,y)) goalLocations.add(new Location(null, x,y, QLearning.Q_MAX));
    	}
      }
      return decideTowardsAGoal(goalLocations);
    }
    
  } // End of inner class QLearning
  
  // Set your class variables here; this is an example mindset
  // system. Feel free to implement your own exploration vs. exploitation logic.
  boolean isExplored = false;
  int mindSet = MINDSET_EXPLORE;
  final static int MINDSET_EXPLORE = 0, MINDSET_GATHER = 1, MINDSET_DELIVER = 2;
  
  /** example variables to learn different strategies for collecting balls vs. depositing them */
  QLearning depositBinRoute, ballCollectRoute, exploreRoute;
  
  // constructor MUST call super (ExplorerAgent) constructor
  public AustinDongAgent(String name, AgentDebug iface) {
    super(name, iface);  // Creates grid also
    
    // do your constructor stuff here
    color = Color.magenta; // Choose a color for your agent :)
  }
  
  /** This is the main move function */
  public int move() throws AgentSuccessfulException, ObstacleInPathException {
    int direction = -1;
    
    // This is a simplified "explore and then collect"
    // strategy. You will need to implement a more successful
    // strategy.
    switch (mindSet) {
      case MINDSET_EXPLORE:
        iface.displayMessage(name+": move: mindset = MINDSET_EXPLORE");
        try {
          return direction = super.move(); // Throws ObstacleInPathException or AgentSuccessfulException
        } catch (AgentSuccessfulException e) {
          // Map explored by explorer agent!
          mindSet = MINDSET_GATHER;
          
          grid.iface.displayScene();
          
          throw new AgentSuccessfulException(name+": Explored");
        }
        
      case MINDSET_GATHER:
        iface.displayMessage(name+": move: mindset = MINDSET_GATHER");
        if (!agentInfo.isBagFull()) {
          // First pick up ball if it was on this block (???)
          // use agentinfo.modifycurrent() to do it
          if (occupiedBlock instanceof BallBlock) { 
            agentInfo.pickBall();
            if (agentInfo.isBagFull())
              mindSet = MINDSET_DELIVER;
          }
          
          try {
            return direction = super.move(); // Throws ObstacleInPathException or AgentSuccessfulException
          } catch (AgentSuccessfulException e) {
            // All balls collected!
            throw new AgentSuccessfulException(name+": All balls collected!");
          }
        } else throw new Error("Fatal: MINDSET_GATHER but already full of balls ?");
        
      case MINDSET_DELIVER:
        iface.displayMessage(name+": move: mindset = MINDSET_DELIVER");
        if (occupiedBlock instanceof DepositBinBlock) { 
          agentInfo.depositBalls();
          mindSet = MINDSET_GATHER;
        }
        
        try {
          return direction = super.move(); // Throws ObstacleInPathException or AgentSuccessfulException
        } catch (AgentSuccessfulException e) {
          // All balls collected!
          throw new AgentSuccessfulException(name+": All balls collected!");
        }
      default:
        throw new Error("Fatal: unrecognized mindset, what am I thinking ?");
    }
    
  }
  
  /** 
   * Returns true if the block at given location is one of which we
   * want to go.
   */
  protected boolean isFavorable(int x, int y) {
    //iface.displayMessage(name+": LearnerAgent.isFavorable: ("+x+","+y+")");
    Block block;
    try {
      block = grid.getBlock(x, y);
    } catch (ArrayIndexOutOfBoundsException e) {
      block = null;
    }
    
    switch (mindSet) {
      case MINDSET_EXPLORE:
        return super.isFavorable(x, y); // Look for unexplored only
      case MINDSET_GATHER:
        return super.isFavorable(x, y) || (block instanceof BallBlock);
      case MINDSET_DELIVER:
        return block instanceof DepositBinBlock;
      default:
        throw new Error("Fatal: unrecognized mindset, what was I thinking?");
    }      
  }
  
  /** Put your main direction-deciding AI here. 
    */
  protected int decideTowardsAGoal(LinkedList goalLocations) {
    
    switch (mindSet) {
      case MINDSET_EXPLORE:
    	  exploreRoute.reset();
    	  exploreRoute.learn(goalLocations);
    	  return exploreRoute.decideDirection(posX, posY);
      case MINDSET_GATHER:
        ballCollectRoute.reset();
        ballCollectRoute.learn(goalLocations);
        return ballCollectRoute.decideDirection(posX, posY);
      case MINDSET_DELIVER:
        depositBinRoute.reset();
        depositBinRoute.learn(goalLocations);
        return depositBinRoute.decideDirection(posX, posY);
      default:
        throw new Error("Fatal: unrecognized mindset, what was I thinking?");
    }      
    
  }
  
  protected void setGrid(Grid newGrid) {
    synchronized (grid) {
      super.setGrid(newGrid); // Sets member var grid
      
      ballCollectRoute.setGrid(grid);
      depositBinRoute.setGrid(grid);
      exploreRoute.setGrid(grid);
    }
  }
}
