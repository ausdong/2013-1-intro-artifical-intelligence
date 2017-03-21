// $Id: Grid.java 1644 2013-03-25 05:07:12Z cengiz $

package grid;

import java.io.StringReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.awt.event.*;
import java.lang.*;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// The world definition
public class Grid implements ActionListener {

  String name;			// Grid identifier
  protected Block[][] map;	// Representation of the world (make it a linklist of blocks)
  int width, height;
  public static int displayType = 0;	// 0 for visual, 1 for frequency

  public Thread ifaceThread;		// Interface thread 

  int foodX, foodY;		// Assuming there's one food location in grid

  // Change it to LinkList: (se type)
  Vector agents;		// All agents in this world 
  public GridDebug iface;		// Debugging & visualization interface

  public static final int MAX_GRID_WIDTH=50;
  public static final int MAX_GRID_HEIGHT=50;

  final int starvationCount = 2000;	// Maximum steps an agent is allowed to roam
  int countDown = starvationCount;

  // Default actions in main grid
  SwitchAction stepItem, exploreItem, createAgentItem, loadAgentItem, saveGridItem;

  /** Create empty grid */
  public Grid(String name,int width, int height, GridDebug iface, Thread ifaceThread) {
    createGrid(name, width, height, iface, ifaceThread);
  }

  /** Create random grid */
  public Grid(String name, int width, int height, double spaceToWallRatio,
	      GridDebug iface, Thread ifaceThread) {
    createGrid(name, width, height, iface, ifaceThread);

    int x,y;

    // Some way of creating walls ?
    for (y=0; y<height; y++) 
      for (x=0; x<width; x++) {
	map[y][x] = 
	  ( x==0 || y==0 || x==width-1 || y==height-1 || Math.random() > spaceToWallRatio)?
	  (Block)new WallBlock():(Block)new EmptyBlock();
      }
    
  }

  /** 
   * Create grid from string. 
   * @param textMap String containing map.
   */
  public Grid(String name, int width, int height, String textMap,
	      GridDebug iface, Thread ifaceThread) {
    createGrid(name, width, height, iface, ifaceThread);

    StringReader stream = new StringReader(textMap);
    int ch, index = 0, x = 0, y = 0;
    Block block = null;

    try {
      while ( (ch = stream.read()) != -1) {
	x = index%width;
	y = index/width;
	switch (ch) {
	case 'X':		// Wall
	  block = new WallBlock();
	  break;

	case 'F':		// Food
	  block = new FoodBlock();
	  break;

	case 'D':
	  block = new DepositBinBlock();
	  break;

	case 'B':
	  block = new BallBlock();
	  break;

	case '\n':
	  continue;
	case ' ':
	  block = new EmptyBlock();
	  break;
	default:
	  iface.displayMessage("Unknown character in map: '" + ch + "'.");
	  break;
	}
	index++;
	if (ch == -1) break;

	// Place the block in map
	map[y][x] = block;
      }

    } catch (IOException e) {
      throw new Error(""+e);
    }    
  }

  // ----------------------------------------------------------------------
  // Create an empty grid
  // ----------------------------------------------------------------------
  void createGrid(String name, int width, int height, GridDebug iface, Thread ifaceThread) {
    this.width = width;
    this.height = height;
    this.name = name;

    map = new Block[height][width];	// Create empty grid

    this.ifaceThread = ifaceThread;
    
    setInterface(iface);

    iface.setTitle(this.name);

    agents = new Vector();

    stepItem = new Step(); 
    exploreItem = new Explore(); 
    //createAgentItem = new CreateAgentAction(); 
    loadAgentItem = new LoadAgentAction(); 
    saveGridItem = new SaveGridAction();
  }

  // ----------------------------------------------------------------------
  // Try to occupy a place in the grid
  // ----------------------------------------------------------------------
  public Block occupy(int x, int y, Block newBlock) throws ObstacleInPathException { 
    Block retval = map[y][x];

    if (map[y][x].isBlocked())
      throw new ObstacleInPathException(); // Should this happen in Block ?
    else map[y][x] = newBlock;

    return retval;
  }

  // ----------------------------------------------------------------------
  // Release occupied place from the grid
  // ----------------------------------------------------------------------
  private Block release(int x, int y, Block replace) {
    Block retval = map[y][x];

    map[y][x] = replace;

    return retval;
  }

  // ----------------------------------------------------------------------
  // Release occupied place from the grid
  // ----------------------------------------------------------------------
  public Block move(int x, int y, int direction, Block originalBlock)
    throws ObstacleInPathException {
    Block mover = release(x, y, originalBlock);

    int newX, newY;

    newX = x + Direction.xInc(direction);
    newY = y + Direction.yInc(direction);

    try { originalBlock = occupy(newX, newY, mover); }
    catch (ObstacleInPathException e) {
      originalBlock = occupy(x, y, mover); // Put it back
      throw e;
    }

    //FoodHunt.debug("Agent '"+mover+"' moved to : "+x+", "+y);
    return originalBlock; 
  }

  // ----------------------------------------------------------------------  
  // Places new food in world
  // ----------------------------------------------------------------------
  public Block placeBlock(int x, int y, Block newBlock) throws ObstacleInPathException { 
    Block retVal = occupy(x, y, newBlock);

    foodX=x; foodY=y;

    return retVal;
  }
  
  // ----------------------------------------------------------------------
  // Gives birth to new agent
  // ----------------------------------------------------------------------
  public Block placeAgent(AgentAI _AICore, int x, int y) throws ObstacleInPathException { 
    Block block;
    AgentInfo newAgent =
      new AgentInfo(_AICore, x, y, this, block = occupy(x, y, new AgentBlock(_AICore))); 

    _AICore.bringToLife(newAgent);

    agents.add(newAgent);	// Add agent to list

    return block;
  }

  // ---------------------------------------------------------------------- 
  // Moves all agents
  // ----------------------------------------------------------------------
  public void advanceTime() throws AgentSuccessfulException { 
    // Move all agents

     for (Enumeration list = agents.elements() ; list.hasMoreElements() ;) {
      ((AgentInfo)list.nextElement()).requestMove();      
    }
  }

  // ---------------------------------------------------------------------- 
  // Makes agents see 9 blocks
  // ----------------------------------------------------------------------
  public Block[][] see(int x, int y) {	// To make agent see surroundings
    int mx, my, i, j;
    Block[][] retval = new Block[3][3];

    
    for (i=0, my = y-1; my <= y+1; my++, i++)
      for (j=0, mx = x-1; mx <= x+1; mx++, j++) {
	retval[i][j] = (Block) map[my][mx].clone();
	//FoodHunt.debug("x="+mx+", y="+my+", Block="+map[my][mx]+"/"+retval[i][j]+"/");
      }

    //FoodHunt.debug("View from grid: "+retval);
    return retval;
  }

  // ---------------------------------------------------------------------- 
  // Makes peers see a single block
  // ----------------------------------------------------------------------
  public Block getBlock(int x, int y) {
    return map[y][x];
  }

  /** Get full map 
   */
  public Block[][] getMap() {
    return map;
  }

  // ---------------------------------------------------------------------- 
  // Makes peers set a single block
  // (agent doesn't know the grid pointer of the real world)
  // ----------------------------------------------------------------------
  public void setBlock(int x, int y, Block block) {
    map[y][x] = block;
  }

  // ---------------------------------------------------------------------- 
  // Sets the debugging interface
  // ----------------------------------------------------------------------
  void setInterface(GridDebug iface) {
    this.iface = iface;
    this.iface.setGrid(this);
  }

  // ---------------------------------------------------------------------- 
  // Makes agents smell
  // ----------------------------------------------------------------------
  public double smell(int x, int y) {	// To make agent smell food
    return Direction.dirArctan(x, y, foodX, foodY);
  }

  // ----------------------------------------------------------------------
  // Displays grid on text screen
  // ----------------------------------------------------------------------
  public String toString() {
    int x, y;
    String retval = new String();
    String icon;

    for (y=0; y<height; y++) {
      for (x=0; x<width; x++) {
	if ( map[y][x] != null)
	  icon = ""+ map[y][x];
	else 
	  icon = "?";
	retval = retval + icon + ((displayType!=0)?"\t":"");
      }
      retval = retval + "\n";
    }
    return retval;
  }

  // ----------------------------------------------------------------------
  // Returns width of grid
  // ----------------------------------------------------------------------
  public int getWidth() {
    return width;
  }

  // ----------------------------------------------------------------------
  // Returns width of grid
  // ----------------------------------------------------------------------
  public int getHeight() {
    return height;
  }

  // ----------------------------------------------------------------------
  // Returns name of grid
  // ----------------------------------------------------------------------
  public String getName() {
    return name;
  }

  // ----------------------------------------------------------------------
  // Inner interface for the action switches
  interface SwitchAction {
    String getName();
    void action();
  }

  // ----------------------------------------------------------------------
  // The action when user wants to advance one step of time on grid
  public class Step implements SwitchAction {
    public final String name = "Step";

    public String getName() { return name; }

    public void action() {
      iface.displayMessage("Stepping once.");
      try {
	advanceTime();
      } catch (AgentSuccessfulException e) {
	iface.displayScene();
	iface.displayFrequency();    
	iface.displayWarning(""+e);
	//iface.exit();
      }
      
      iface.displayScene();
      iface.displayFrequency();    

      if (countDown-- == 0) {
	iface.displayWarning("Food not found at the end of "+
				  starvationCount+" steps.");
	iface.exit();
      }
    }
  }

  // ----------------------------------------------------------------------
  // The action when user wants to explore the whole grid
  public class Explore implements SwitchAction, Runnable {
    public final String name = "Explore";
    boolean pressed = false;

    public String getName() { return name; }

    public void action() {
      if (pressed) {
	iface.displayWarning("Grid already explored!");
	return;
      }

      pressed = true;
      (new Thread(this)).start();
            
    }
    public void run() {
      iface.displayMessage("Exploring...");
      while ( countDown-- > 0 ) {
	try {
	  advanceTime();
	  iface.displayScene();
	  iface.displayFrequency();    
	} catch (AgentSuccessfulException e) {
	  iface.displayWarning(""+e);
	  iface.exit();
	  pressed = false;
	  return;
	}
      }

      iface.displayScene();
      iface.displayFrequency();    

      iface.displayWarning("Food not found at the end of "+
				starvationCount+" steps.");
      iface.exit();
      pressed = false;
    }
  }

  // ----------------------------------------------------------------------
  // Places agent randomly
  void placeAgentRandomly(AgentAI agent) throws ObstacleInPathException {
    boolean adequatePlace = false;
    int watchDog = 500; 
    Block occupiedBlock;

    // Place agent randomly
    while (!adequatePlace) {
      try {
	occupiedBlock = placeAgent(agent,
				   (int)(getWidth()*Math.random()),
				   (int)(getHeight()*Math.random()));

	if (occupiedBlock instanceof EmptyBlock) // Only place in uninhabited places
	  adequatePlace = true;
      } catch (ObstacleInPathException e) {
	if (watchDog-- != 0)
	  adequatePlace = false;
	else throw new ObstacleInPathException();
      }
    }
    iface.displayMessage("Agent placed after "+(500-watchDog)+" trials.");
  }

  /** The action when user wants to place agent on grid */
  public class CreateAgentAction implements SwitchAction {
    int agentNumber = 0;

    public final String name = "Create agent";

    public String getName() { return name; }

    public void action() {
      try {
	  placeAgentRandomly(/*new ExplorerAgent("Expo", (AgentDebug)iface)*/
			   /*new RecallerAgent("Expo", (AgentDebug)iface)*/
			   new LearnerAgent("Q-wise-"+agentNumber++, (AgentDebug)iface)
			   /*new TalkingAgent("Talkative-"+agentNumber++, (AgentDebug)iface)*/
			   /*new DumbAgent("Dumbo", (AgentDebug)iface)*/);
      } catch (ObstacleInPathException e) {
	throw new
	  Error("Fatal: failed to place blocks in grid. Not enough free space on grid ?");
      }
    }
  }

  /** Save current grid design as text file. */
  public class SaveGridAction implements SwitchAction {

    public final static String name = "Export grid";

    public String getName() { return name; }

    public void action() {
      String filename;
      String path = "grid/";
      try {
	filename = 
	  iface.selectFile(new File(getClass().getClassLoader().getResource(path).toURI()), 
			   "txt");
      } catch (URISyntaxException e) {
	iface.displayWarning("Failed to browse directory for saving grid: " + e);
	return;
      }

      try {
	FileWriter writer = new FileWriter(path + filename);
	writer.write("" + Grid.this);
	writer.close();
      } catch (IOException e) {
	iface.displayWarning("Error saving grid in '" + filename + "' text file: " + e);
	return;
      }
    }
  }

  /**
   * Action for loading agent from file
   */
  public class LoadAgentAction implements SwitchAction {
    public final String name = "Load agent";

    public String getName() { return name; }

    public void action() {
      ClassLoader loader = getClass().getClassLoader();

      String filename;

      try {
	filename = 
	  iface.selectFile(new File(loader.getResource("agent/ballgame/").toURI()), "class");
      } catch (URISyntaxException e) {
	throw new Error("Failed to open classpath directory for loading agent classes.");
      }

      // will become the agen't name on the arena
      filename = filename.substring(0, filename.indexOf("."));
      // TODO: check if name already exists in arena

      try {
	Class<?>[] types = {String.class, AgentDebug.class};
	placeAgentRandomly((AgentAI)
			   (Class.forName("agent.ballgame." + filename).getConstructor(types).
			    newInstance(filename, iface)));

	iface.setDisplayVariable(filename, "0");

	// update UI?
	iface.updateUI();
	    
	/*
	// load files in ballgame package dir
	iface.displayMessage("File: ");

	File[] files = 
	new File(getClass().getClassLoader().getResource("agent/ballgame/").toURI()).listFiles();
	// TODO; use FileNameFilter or ClassLoader.getResources to just pickup .class files

	iface.displayMessage("Found files:" + files.length);
	*/
      } catch (ReflectiveOperationException e) {
	throw new Error("Failed to load agent class: " + e);
      } catch (ObstacleInPathException e) {
	throw new
	  Error("Fatal: failed to place blocks in grid. Not enough free space on grid ?");
      } 

    }
  }

  // ----------------------------------------------------------------------
  // From the ActionListener interface
  public void actionPerformed(ActionEvent event) {

    Hashtable switchHash = new Hashtable();

    switchHash.put(stepItem.getName(), stepItem);
    switchHash.put(exploreItem.getName(), exploreItem);
    //switchHash.put(createAgentItem.getName(), createAgentItem);
    switchHash.put(loadAgentItem.getName(), loadAgentItem);
    switchHash.put(saveGridItem.getName(), saveGridItem);

    ((SwitchAction)switchHash.get(event.getActionCommand())).action();
  }

  // ----------------------------------------------------------------------
  // Make other agents hear a message broadcast by one agent
  public void broadcastSurroundings(AgentInfo fromAgentInfo, int x, int y, Block[][] surroundings) {
    for (Enumeration list = agents.elements() ; list.hasMoreElements() ;) {
      ((AgentInfo)list.nextElement()).hearSurroundings(fromAgentInfo, x,y,surroundings);
    }    
  }

}
