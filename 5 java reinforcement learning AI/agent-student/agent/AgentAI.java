// $Id: AgentAI.java 1644 2013-03-25 05:07:12Z cengiz $

package agent;

import java.awt.Color;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public abstract class AgentAI {

  protected String name;
  protected Color color = Color.red;
  protected Block[][] surroundings;	// Should go into dumb agent ? 
  protected AgentInfo agentInfo;		// Connection to world
  protected AgentDebug iface;		// Debugging & visualization interface
  protected Block occupiedBlock;		// The block before we stepped on it

  protected int moves;
  protected double chaoticBehaviour = 0.3; // Ratio of giving irrational decisions
    //    int lastDirection;		// Last given choice of direction

  AgentAI(String name, AgentDebug iface) {
    this.name = name;
    setDebugInterface(iface);
  }

  // --------------------------------------------------------------------------------
  // Connect to grid, birth with a brand new body
  // --------------------------------------------------------------------------------
  public void bringToLife(AgentInfo agentInfo) {
    this.agentInfo = agentInfo;
    moves = 0;

    surroundings = agentInfo.see(); // Initial view of surroundings
  }

  // --------------------------------------------------------------------------------
  // Return a random direction
  // --------------------------------------------------------------------------------  
  protected final int randomDirection() {
    return (int)(Math.random() * 4);
  }

  // --------------------------------------------------------------------------------
  // Return a random direction, but first see that it's empty
  // --------------------------------------------------------------------------------  
  final int decideRandomDirection() {
    int direction, watchDog = 500;

    do {
      direction = randomDirection();
      watchDog--;
      if (watchDog == 0) throw new Error(name+": Trapped!");
    } while (surroundings
	     [1+Direction.yInc(direction)]
	     [1+Direction.xInc(direction)].isBlocked());

    return direction;
  }

  // -------------------------------------------------------------------------------- 
  // Move towards direction and randomly every once in a while
  // --------------------------------------------------------------------------------
  final int decideOrientedDirection(double direction, boolean isChaotic)
       throws ObstacleInPathException { 
	 
    iface.displayMessage("Oriented move");

    if (isChaotic && Math.random() < chaoticBehaviour)
      return decideRandomDirection(); // Introduce some chaos if desirable

    int intDirection = (int)Math.round(direction) % 4; // Gives principal direction

    iface.displayMessage("Direction: "+intDirection+", "+direction);

    if (! surroundings
	[1+Direction.yInc(intDirection)][1+Direction.xInc(intDirection)]
	.isBlocked()) 
      // If principal direction is free
      return intDirection;
    
    intDirection = (int)Math.ceil(direction) % 4;

    iface.displayMessage("Secondary direction: "+intDirection);

    if (! surroundings
	[1+Direction.yInc(intDirection)][1+Direction.xInc(intDirection)]
	.isBlocked()) 
      // If left direction is free
      return intDirection;

    intDirection = (int)Math.floor(direction) % 4;

    iface.displayMessage("Third direction: "+intDirection);

    if (! surroundings
	[1+Direction.yInc(intDirection)][1+Direction.xInc(intDirection)]
	.isBlocked()) 
      // If right direction is free
      return intDirection;
    
    // Be deterministic and report error
    throw new
	ObstacleInPathException("decideOrientedDirection: Cannot go in "+
				direction+" direction.");
    // return decideRandomDirection(); // Otherwise toss a quarter    
  }

  // --------------------------------------------------------------------------------
  // Look for food in the 8 blocks of visual field, and returns the direction
  // towards food (chaoticity optional), otherwise raises FoodNotFoundException
  // --------------------------------------------------------------------------------
  final int immediateFoodDirection() throws FoodNotFoundException {
    int x, y;

    // Look around for food in immediate surroundings
    for (y=0; y<3; y++)
	for (x=0; x<3; x++)
	    if (surroundings[y][x].isNutritious()) {
		try {
		    return decideOrientedDirection(Direction.dirArctan(1,1,x,y), false);
		} catch (ObstacleInPathException e) {
		    // do nothing and continue the loop
		}
	    }

    throw new FoodNotFoundException("No food in here!"); // Otherwise
  }

  // --------------------------------------------------------------------------------
  // Look for food in the 8 blocks of visual field, and returns the direction
  // towards unvisited block (chaoticity optional), 
  // otherwise raises FoodNotFoundException
  // --------------------------------------------------------------------------------
  final int immediateUnvisitedDirection() throws
  FoodNotFoundException, ObstacleInPathException {
    int x, y;
    int min = Integer.MAX_VALUE, minx = -1, miny = -1, visited;
    double direction;

    // Look around for food in immediate surroundings
    for (y=0; y<3; y++)
      for (x=0; x<3; x++) {
	visited = surroundings[y][x].visitCount();
	if (!surroundings[y][x].isBlocked() && min > visited) 
	  { min = visited; minx = x; miny = y; }
      }

    if (minx != -1) {
      iface.displayMessage("Minimum visited count: " + min);
      direction = Direction.dirArctan(1, 1, minx, miny);
      try {
	return decideOrientedDirection(direction, false);
      } catch (ObstacleInPathException e) {
	// do nothing and continue the loop
	throw new
	  ObstacleInPathException("immediateUnvisitedDirection: Cannot go in direction "+
				  direction+" !"); // Otherwise
      }
    } else
      throw new FoodNotFoundException("All visited around here!"); // Otherwise
  }

  // --------------------------------------------------------------------------------
  // Main decisive action
  // Default action: Look for immediate contact with food in visual area,
  // go towards it if there, otherwise move randomly. Even with a oriented
  // aim, there is a change of random movement (See chaoticBehaviour). Maybe
  // chaos should be suppressed at this point.
  // --------------------------------------------------------------------------------
  protected int decideDirection() throws AgentSuccessfulException {
    try {
      return immediateFoodDirection();
    } catch (FoodNotFoundException e) {
      return decideRandomDirection();	// Dumb agent by default
    }
  }

  // --------------------------------------------------------------------------------
  // Basic motor movement, returns direction to which it went
  // --------------------------------------------------------------------------------
  public int move()
      throws AgentSuccessfulException, ObstacleInPathException {
    int dir;

    //iface.displayMessage("View of agent:");
    //iface.displayMessage(""+surroundings);

    occupiedBlock = agentInfo.move(dir = decideDirection());

    // These lines are NOT reached if above results in exception
    moves++;
    iface.displayMessage("Move direction: " + new Direction(dir));

    if (occupiedBlock.isNutritious()) {
      throw new AgentSuccessfulException("Found food in "+moves+" steps!");
    }

    surroundings = agentInfo.see(); // look around

    return dir;
  }

  // --------------------------------------------------------------------------------
  // Happens when decision is faulty
  // --------------------------------------------------------------------------------
  public void ouch() {
    iface.displayMessage("I hit the wall! Ouch!!!");
  }

  public int getMoves() { return moves; }

  public String getName() { return name; }

  public Color getColor() { return color; }
  public void setColor(Color color) { this.color = color; }

  // Not public! iface should be given to the constructor from now on...
  void setDebugInterface(AgentDebug iface) { 
    this.iface = iface;
    this.iface.setAgent(this);
  }
}
