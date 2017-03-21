// $Id: Debug.java 1619 2013-03-20 03:47:14Z cengiz $

package iface;

import java.lang.reflect.*;
import java.awt.event.*;
import java.io.File;

public interface Debug extends Cloneable, ActionListener, Runnable { 
  boolean debugSwitch = false;

  Object clone();		// From Cloneable (modified to be public here)

  void displayMessage(String message); // Display message
  void displayWarning(String message); // Display warning (debug mode only)
  // Declare limited range variable to be input
  void setInputVariableRange(String variable, Number minVal, Number maxVal, Number defVal);
  void setInputVariable(String variable, Object defval); // Declare variable to be input
  Object getVariable(String variable); // Get the value of the variable been input
  
  void setAction(String variable, ActionListener listener); // Wait for interaction and call method
  void setTitle(String title);	// Set title of interface
  void exit();			// Terminate process

  /** For loading agent class, etc. */
  String selectFile(File curdir, String filter);

  /** Set a variable to be displayed. */
  void setDisplayVariable(String variable, Object initVal);

  /** Update display value of variable */
  void updateDisplayVariable(String variable, Object newVal);
}
