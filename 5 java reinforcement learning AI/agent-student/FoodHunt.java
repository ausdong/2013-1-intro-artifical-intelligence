// $Id: FoodHunt.java 1643 2013-03-25 04:42:07Z cengiz $

import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.net.URISyntaxException;

import agent.*;
import grid.*;
import block.*;
import iface.*;

public class FoodHunt implements ActionListener {

  //Grid grid;			// Remove this from here, FoodHunt can have multiple grids!
  AgentAI agent;
  boolean debugSwitch = false;
  //SimulatorConsole console;
  public Debug iface;
  Thread ifaceThread;

    /** Button names as constants */
    final String ButtonCreateRandGrid = "Create random grid";
    final String ButtonLoadGrid = "Load grid from file";

  FoodHunt (int width, int height, Debug iface, Thread thread) throws ObstacleInPathException {
    boolean found = false;
    this.iface = iface;
    ifaceThread = thread;
    
    gridCreator();
  }

  interface SwitchAction {
    void action();
  }

  class CreateGridAction implements SwitchAction {
    public void action() {
      Grid grid;
      
      iface.displayMessage("Creating random grid...");
      String name = (String)iface.getVariable("Name");
      int width = ((Integer)iface.getVariable("Width")).intValue();
      int height = ((Integer)iface.getVariable("Height")).intValue();
      // Specify space-wall ratio
      double s_WRatio = (double)((Integer)iface.getVariable("Space-wall percentage")).
	intValue()/100.0; 

      Debug gridIface;
      try {
	// Make a new instance of VisualFoodHunt, etc, but this time
	// we put different stuff inside
	gridIface = (Debug) (iface.getClass()).newInstance();
      } catch (IllegalAccessException e) {
	throw new Error("Fatal: "+e);
      } catch (InstantiationException e) {
	throw new Error("Fatal: "+e);
      }

      Thread inputThread = new Thread(gridIface);
      inputThread.start();

      grid = new Grid(name, width, height, s_WRatio, (GridDebug)gridIface, inputThread);

      try {
	placeBlock(grid, new DepositBinBlock());
	placeBlock(grid, new DepositBinBlock());
	placeBlock(grid, new DepositBinBlock());

	for (int i=0; i<20; i++) {
	  placeBlock(grid, new BallBlock());
	}
	
      } catch (ObstacleInPathException e) {
	throw new Error("Fatal: failed to place blocks in grid. Not enough free space on grid ?");
      }

      grid.iface.displayScene();
      // console = new SimulatorConsole("FoodHunt", grid);

      // Add some buttons
      grid.iface.setAction((grid.new Step()).name, grid);
      grid.iface.setAction((grid.new Explore()).name, grid);
      //grid.iface.setAction((grid.new CreateAgentAction()).name, grid);
      grid.iface.setAction((grid.new LoadAgentAction()).name, grid);
      grid.iface.setAction(Grid.SaveGridAction.name, grid);

      // notify interface that description is complete ?
      grid.ifaceThread.interrupt();
    }
  }

  class LoadGridAction implements SwitchAction {
    public void action() {
      String map = "";
      Grid grid;
      int width = 0, height = 0;
      
      iface.displayMessage("Loading grid...");
      String filename, path = "grid/";
      try {
	filename = 
	  iface.selectFile(new File(getClass().getClassLoader().getResource(path).toURI()), 
			   "txt");
      } catch (URISyntaxException e) {
	iface.displayWarning("Failed to browse directory for loading grid: " + e);
	return;
      }

      try {
	BufferedReader reader
	  = new BufferedReader(new FileReader(path + filename));
	while (reader.ready()) {
	  String row = reader.readLine();
	  if (width == 0) width = row.length();
	  map += row + "\n";
	  height++;
	}
	reader.close();
      } catch (IOException e) {
	iface.displayWarning("Error loading grid from '" + filename + "' text file: " + e);
	return;
      }
      iface.displayMessage("Read " + filename + "...");

      // Debug:
      // iface.displayMessage(map);

      Debug gridIface;
      try {
	// Make a new instance of VisualFoodHunt, etc, but this time
	// we put different stuff inside
	gridIface = (Debug) (iface.getClass()).newInstance();
      } catch (IllegalAccessException e) {
	throw new Error("Fatal: "+e);
      } catch (InstantiationException e) {
	throw new Error("Fatal: "+e);
      }

      Thread inputThread = new Thread(gridIface);
      inputThread.start();

      grid = new Grid(filename, width, height, map, (GridDebug)gridIface, inputThread);

      grid.iface.displayScene();

      // Add some buttons
      grid.iface.setAction((grid.new Step()).name, grid);
      grid.iface.setAction((grid.new Explore()).name, grid);
      //grid.iface.setAction((grid.new CreateAgentAction()).name, grid);
      grid.iface.setAction((grid.new LoadAgentAction()).name, grid);
      grid.iface.setAction(Grid.SaveGridAction.name, grid);

      // notify interface that description is complete ?
      grid.ifaceThread.interrupt();
    }
  }

  public void actionPerformed(ActionEvent event) {

    Hashtable switchHash = new Hashtable();
    switchHash.put(ButtonCreateRandGrid, new CreateGridAction());
    switchHash.put(ButtonLoadGrid, new LoadGridAction());

    ((SwitchAction)switchHash.get(event.getActionCommand())).action();
  }

  public void gridCreator() {
    //    Integer width, height;
    iface.setTitle("Grid spawner");
    
    // Get width 
    iface.setInputVariableRange("Width", new Integer(1),
				new Integer(Grid.MAX_GRID_WIDTH),
				new Integer(20));
    
    // Get height
    iface.setInputVariableRange("Height", new Integer(1),
				new Integer(Grid.MAX_GRID_HEIGHT),
				new Integer(20)); 

    // Get space-wall ratio
    iface.setInputVariableRange("Space-wall percentage",
				new Integer(1), new Integer(100),
				new Integer(80)); 

    // Get name
    iface.setInputVariable("Name", "World");

    iface.setAction(ButtonCreateRandGrid, this);
    iface.setAction(ButtonLoadGrid, this);

    ifaceThread.interrupt();
  }

  void placeBlock(Grid grid, Block newBlock) throws ObstacleInPathException{
    boolean adequatePlace = false;
    int watchDog = 500;		// Watchdog counter
    Block occupiedBlock;

    // Place food randomly
    while (!adequatePlace) {
      try {
	occupiedBlock = grid.placeBlock((int)(grid.getWidth()*Math.random()),
					(int)(grid.getHeight()*Math.random()), newBlock);
	
	if (occupiedBlock instanceof EmptyBlock) // Only place in uninhabited places
	  adequatePlace = true;
      } catch (ObstacleInPathException e) {
	if (watchDog-- != 0)
	  adequatePlace = false;
	else throw new ObstacleInPathException();
      }
    }

    iface.displayMessage("Food placed after "+(500-watchDog)+" trials.");
  }
}
