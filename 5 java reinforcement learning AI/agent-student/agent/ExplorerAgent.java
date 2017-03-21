// $Id: ExplorerAgent.java 1640 2013-03-24 22:12:26Z cengiz $

package agent;

import java.lang.*;
import java.util.*;

import agent.*;
import grid.*;
import block.*;
import iface.*;
import util.*;

// ExplorerAgent's aim is to explore the whole grid.

public class ExplorerAgent extends AgentAI {
  protected Grid grid;			// Grid in mind
  protected Debug gridIface;		// Grid's interface
  protected Thread inputThread;		// gridIface's thread
  protected int posX, posY;		// Position in this grid
  protected int gridWidth, gridHeight;	// Size of grid (be careful in updates!)
  protected int unexploredCount;        // Number of unexplored blocks that can be reached
  protected LinkedList goalLocations;	// Goal location list at a particular time

  // ----------------------------------------
  // Constructor
  // ----------------------------------------
  public ExplorerAgent(String name, AgentDebug iface) {
    super(name, iface);

    grid = createNewGridInMemory();
  }

  Grid createNewGridInMemory() {
    try {
      gridIface = (Debug) (iface.getClass()).newInstance();
    } catch (IllegalAccessException e) {
      throw new Error("Fatal: "+e);
    } catch (InstantiationException e) {
      throw new Error("Fatal: "+e);
    }
    
    inputThread = new Thread(gridIface);
    inputThread.start();

    posX = 1; posY = 1;	// Middle of nowhere

    return new Grid("Memory of " + name, 3, 3, // As big as us
		    (GridDebug)gridIface, inputThread); 
  }

  // ----------------------------------------
  // Called when positioned in grid (when born)
  // ----------------------------------------
  public void bringToLife(AgentInfo agentInfo) {
    super.bringToLife(agentInfo);

    // Build grid in mind
    memorizeSurroundings();

    grid.iface.displayScene();
    grid.ifaceThread.interrupt();
  }

  // ----------------------------------------
  // Returns true if the unexplored block at given location is reachable from any 4 main directions
  // ----------------------------------------
  boolean isExplorable(int x, int y) {
    int direction;
    
    for (direction = 0; direction < 4; direction++) {
      try {
	if (!grid.getBlock(x + Direction.xInc(direction),
			   y + Direction.yInc(direction)).isBlocked())
	  break;		// Out of for
      } catch (NullPointerException e) {
	// do nothing
      } catch (ArrayIndexOutOfBoundsException e) {
	// do nothing
      }
    }

    return (direction != 4); 	// True if any block that's not a wall exist
  }

  // ----------------------------------------
  // Returns true if the block at given location is one of which we want to go
  // ----------------------------------------
  protected boolean isFavorable(int x, int y) {
    try {
      if (grid.getBlock(x, y) == null) 
	// Exception either coming from getBlock() or explicitly generated
	throw new ArrayIndexOutOfBoundsException("Null Block");
    } catch (ArrayIndexOutOfBoundsException e) {
      return isExplorable(x,y);
    }
    return false;
  }

  // ----------------------------------------
  // Returns the euclidian distance between two points in 2D space
  // ----------------------------------------  
  int calculateDistance(int x1, int y1, int x2, int y2) {
	return (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2);
    }

  protected int decideTowardsAGoal(LinkedList goalLocations) {
    int minDistance = Integer.MAX_VALUE, minx = -1, miny = -1, distance;

    for (ListIterator list = goalLocations.listIterator(); list.hasNext() ;) {
      Location loc = (Location) list.next();

      // Find the spot which is closest to us
      distance = calculateDistance(loc.x, loc.y, posX, posY); 
      if (minDistance > distance) {
	minDistance = distance; 
	minx = loc.x;
	miny = loc.y;
      }
    }

    // Try to go to chosen spot
    try {
      return decideOrientedDirection(Direction.dirArctan(posX,posY,
							 minx,miny),
				     true); // Chaotism needed!
    } catch (ObstacleInPathException e) {
      iface.displayMessage(name+": "+e);
    }

    // Direction from which the smell comes
    return decideRandomDirection();	// Dumb agent otherwise

  }

  // ----------------------------------------
  // Main decision function returning the closest of the isFavorable(x,y) locations
  // ----------------------------------------  
  protected int decideDirection() throws AgentSuccessfulException { 
    int gridWidth = grid.getWidth(), gridHeight = grid.getHeight(),
      x, y;

    goalLocations = new LinkedList();
    
    // First scan the grid for empty blocks neighboring unexplored
    // blocks on 4 main directions
    // Warning: a wide scan is applied circling out of the grid!
    for (y = -1; y < gridHeight+1; y++)
      for (x = -1; x < gridWidth+1; x++) 
	if (isFavorable(x,y)) 
	  goalLocations.add(new Location(null, x, y, QLearning.Q_MAX));
  
    unexploredCount = goalLocations.size();

    // Try to go to spot (if any exists)
    if (goalLocations.isEmpty())
      throw new AgentSuccessfulException("No goal locations can be found, mission accomplished ?");
    
    return decideTowardsAGoal(goalLocations);
  }

  // ----------------------------------------
  // Main move function 
  // ----------------------------------------
  public int move() throws AgentSuccessfulException, ObstacleInPathException {
    int direction;
    
    try {
      direction = super.move(); // Throws ObstacleInPathException or AgentSuccessfulException
    } catch (AgentSuccessfulException e) {
      // if task is fulfilled (exception needs revision)
      iface.displayMessage(name+": move: Grid in mind of agent at the time of completion:");
      grid.iface.displayScene();

      throw new AgentSuccessfulException("No other places that can be reached!\n Map explored in "+moves+" steps!");
    }
    
    iface.displayMessage(name+": move(): "+ unexploredCount +" more unexplored reachable blocks");
    
    // if move successfull, do more
    posX += Direction.xInc(direction);
    posY += Direction.yInc(direction);
    
    iface.displayMessage(name+": move(): new position ("+posX+", "+posY+")");

    // Update grid
    memorizeSurroundings();

    grid.iface.displayScene();
    
    return direction;
  }

  // ----------------------------------------
  // Updates iconic representation from immediate view.
  // ----------------------------------------
  void memorizeSurroundings() {
    int x, y, retry;
    for (y=0; y<3; y++)
      for (x=0; x<3; x++) {
	for (retry = 0; ; retry++) 
	  try {
	    // Handle corners specially (don't see unreachable corners)
	    if (!((Math.abs((x-1)*(y-1)) == 1) && // A corner
		(surroundings[1][x].isBlocked() && surroundings[y][1].isBlocked()))) { // Surrounded by blocks
	      grid.setBlock(posX - 1 + x, posY - 1 + y, surroundings[y][x]);
	    }
	    break;	// If this is reached, exception didn't occur
	  } catch (ArrayIndexOutOfBoundsException e) {
	    if (retry > 0)
	      throw new Error("memorizeSurroundings: Fatal, grid expansion didn't help.");
	    // Expand Grid
	    expandGrid(posX - 1 + x, posY - 1 + y);
	  }
      }
  }

  // ----------------------------------------
  // (TODO: should it be put in Grid ?)
  // Called when iconic representation is not enough to represent new information.
  // Therefore expand the iconic map towards the new direction. 
  // (Either newX or newY will be negative or out of bounds here.)
  // ----------------------------------------
  void expandGrid(int newX, int newY) {
    int gridWidth = grid.getWidth(), gridHeight = grid.getHeight();

    iface.displayMessage(name+": expandGrid(): entering with position: ("+newX+", "+newY+") + w: "+
			 gridWidth+", h: "+gridHeight);

    int newWidth =Math.max(gridWidth, Math.max(gridWidth - newX, newX+1)),
      newHeight = Math.max(gridHeight, Math.max(gridHeight - newY, newY+1));

    Grid newGrid = new Grid(grid.getName(), newWidth, newHeight, grid.iface, inputThread);

    int offsetX = Math.max(Math.max(newX - newWidth, 0), -newX);
    int offsetY = Math.max(Math.max(newY - newHeight, 0), -newY);

    iface.displayMessage(name+": expandGrid(): newWidth: "+newWidth+", newHeight: "+newHeight+
			" offsetX: "+offsetX+", offsetY: "+offsetY );

    int x, y;
    for (y = 0; y < gridHeight; y++)
      for (x = 0; x < gridWidth; x++) {
	newGrid.setBlock(x + offsetX, y + offsetY, grid.getBlock(x, y));
      }

    // A new image in memory now
    setGrid(newGrid);

    // Replace agent according to landshift
    posX += offsetX;
    posY += offsetY;

    iface.displayMessage(name+": expandGrid(): updating agent position: ("+posX+", "+posY+")");

    grid.ifaceThread.interrupt(); // needed ?
  }

  protected void setGrid(Grid newGrid) {
    grid = newGrid;
  }
}
