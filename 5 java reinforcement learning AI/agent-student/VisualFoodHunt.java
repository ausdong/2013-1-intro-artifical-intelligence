// $Id: VisualFoodHunt.java 1619 2013-03-20 03:47:14Z cengiz $

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import java.awt.*;
import javax.swing.*;

import java.awt.event.*;
import javax.swing.event.*;

import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// Visual interface and startup file. If ran, FoodHunt is interfaced
// with the visual i/o.

public class VisualFoodHunt implements GridDebug, AgentDebug {
  boolean debugSwitch = true;
  Grid grid;
  FrequencyCanvas gridCanvas;
  AgentAI agent;
  Hashtable inputs;
  /** Points to display variable panels */
  Hashtable<String, JLabel> displays;
  ActionEvent uIAction;

  JFrame simulatorConsole;
  Border emptyBorder;
  Container pane;
  JPanel leftPanel, rightPanel;

  static final int TEXT_FIELD_WIDTH = 10;
  static final String fieldId = "Field";
  static final String buttonId = "Button";

  boolean shouldExit = false;

  public VisualFoodHunt() {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception exc) {
      displayMessage("Error loading L&F: " + exc);
    }

    // for methods in the interface Debug 
    simulatorConsole = new JFrame();

    pane = simulatorConsole.getContentPane();
    pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS)); // BorderLayout better ?
    //    listener = new CatchAction();
    inputs = new Hashtable();
    displays = new Hashtable();

    emptyBorder = new EmptyBorder(5,5,5,5);

    leftPanel = new JPanel();
    rightPanel = new JPanel();

    leftPanel.setBorder(new CompoundBorder(emptyBorder, new SoftBevelBorder(BevelBorder.LOWERED))); 
    rightPanel.setBorder(emptyBorder);    

    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

    pane.add(leftPanel);
    pane.add(rightPanel);
  }

  // Main method of this  thread
  public synchronized void run() {		

    while (!shouldExit) {
      try {
	wait();			// First, wait for a notify
      } catch (InterruptedException e) {
	displayMessage("Input interface ready."); //do nothing, we were expecting this
      }

      //simulatorConsole.setSize(simulatorConsole.getLayout().preferredLayoutSize(simulatorConsole));

      simulatorConsole.pack();
      simulatorConsole.show();
    }
    // end here ?
  }

  public void updateUI() {
      simulatorConsole.pack();
  }

  // 
  // Debugging & visualization interfaces

  // set grid variable and add grid to layout
  public void setGrid(Grid grid) {
    this.grid = grid;

    try {
      gridCanvas.setGrid(grid);
      gridCanvas.invalidate();
      gridCanvas.repaint();
      //displayMessage(""+gridCanvas.getLayout().preferredLayoutSize(gridCanvas));
    } catch (NullPointerException e) {
      leftPanel.add(gridCanvas  = new FrequencyCanvas(grid));
    }
  }

  public void setAgent(AgentAI agent) {
    this.agent = agent;
  }

  // TODO: make it a debug output with debug level and later send output to scrolling text box and not stdout
  public void displayMessage(String message) {

    if (debugSwitch) 
      System.out.println(message);
  }

  public void displayWarning(String message) {
    JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE);
    JDialog dialog = pane.createDialog(simulatorConsole.getContentPane(), "Warning");
    dialog.show();
    pane.getValue();    
  }

  public void displayScene() {
    gridCanvas.requestRepaint();
  }

  public void displayFrequency() {
  }

  public void displayDirection(double direction) {
    System.out.println("Agent "+agent.getName()+"direction: "+direction);
  }

  interface SwitchAction {
    void action(String variable, ActionEvent event);
  }

  class Field implements SwitchAction {
    public void action(String variable, ActionEvent event) {
      JTextComponent field = (JTextComponent) event.getSource();
      setVariable(variable, field.getText());      
    }
  }

  // For catching UI events
  public void actionPerformed(ActionEvent event) {
    String compoundCommand = event.getActionCommand();
    int separatorIndex = compoundCommand.indexOf('/');
    String command = compoundCommand.substring(0, separatorIndex);
    String variable = compoundCommand.substring(separatorIndex + 1,
						compoundCommand.length());

    Hashtable switchHash = new Hashtable();
    switchHash.put(fieldId, new Field());

    //    try {
    ((SwitchAction)switchHash.get(command)).action(variable, event);
    //    } catch (NullPointerException e) {
    //displayMessage("Unhandled event '"+command+"' compared with '"+fieldId+
    //"' in actionPerformed() or event handler resulted in NullPointerException");
    //}
  }

  public void setVariable(String variable, Object val) {
    inputs.put(variable, val);
  }

  public Object getVariable(String variable) { // Get the value of the variable been input
    return inputs.get(variable);
  }

  // Creates a text label and an editable field to take the value
  public void setInputVariable(String variable, Object defVal) {
    JPanel outline = new JPanel();
    outline.setBorder(emptyBorder);
    outline.setLayout(new BoxLayout(outline, BoxLayout.X_AXIS));

    JLabel label = new JLabel(variable+" :");
    outline.add(label);

    JTextField field = new JTextField((String)defVal, TEXT_FIELD_WIDTH);
    field.setActionCommand(""+fieldId+"/"+variable);
    field.addActionListener(this);

    outline.add(field);

    leftPanel.add(outline);	// Add text boxes and sliders to left side

    setVariable(variable, defVal);
  }

  public void setInputVariableRange(String variable, Number minVal, Number maxVal, Number defVal) {
    JPanel outline = new JPanel();
    outline.setBorder(new TitledBorder(variable));
    //outline.setLayout(new BoxLayout(outline, BoxLayout.X_AXIS));

    int min = ((Integer)minVal).intValue();
    int max = ((Integer)maxVal).intValue();

    JSlider range = new JSlider(min, max, ((Integer)defVal).intValue());

    range.setPaintLabels(true);
    range.setPaintTicks(true);
    range.setMajorTickSpacing((max - min)/2);
    range.setMinorTickSpacing((max - min)/4);
    range.setPaintTrack(true);
    range.updateUI();

    class SliderChangeListener implements ChangeListener {
      String variable;

      SliderChangeListener(String variable) { this.variable = variable; }
      public void stateChanged(ChangeEvent event) {
	setVariable(variable, new Integer(((JSlider)event.getSource()).getValue()));
      }
    }
    range.addChangeListener(new SliderChangeListener(variable));

    outline.add(range);

    leftPanel.add(outline);	// Add text boxes and sliders to left side

    setVariable(variable, defVal);
  }

  /** Continuously display contents of variable value. */
  public void setDisplayVariable(String variable, Object initVal) {
    JPanel outline = new JPanel();
    outline.setBorder(new EtchedBorder());

    JLabel varLabel = new JLabel(variable+" :");
    outline.add(varLabel);

    JLabel valLabel = new JLabel(""+initVal);
    outline.add(valLabel);

    // Save value object for updating later
    displays.put(variable, valLabel);

    rightPanel.add(outline);	// Add display boxes to right side?
  }

  public void updateDisplayVariable(String variable, Object newVal) {
    displays.get(variable).setText(""+newVal);
  }

  // Push button
  public void setAction(String variable, ActionListener listener) {
    JPanel outline = new JPanel();
    outline.setBorder(emptyBorder);

    JButton action = new JButton(variable);
    action.setActionCommand(variable);
    action.addActionListener(listener);

    outline.add(action);

    rightPanel.add(outline);	// Add text boxes and sliders to left side    
    
  }

  public void setTitle(String title) {
    simulatorConsole.setTitle(title);
  }

  // SAme as Stdio... put it in common parent class : Debug ?
  public Object clone() {
    try {
      return super.clone(); 
    } catch (CloneNotSupportedException e) {
      System.out.println(""+e);
      throw new Error("Fatal.");
    }
  }

  synchronized public void exit() {
    //System.exit(0); Wait for the user to close windows
    shouldExit = true; notify();
  }

  public static void main(String[] args) throws ObstacleInPathException {
    int width = 10, height = 10;
    
    if (args.length>1) {
      width = Integer.parseInt(args[0]);
      height = Integer.parseInt(args[1]);
    }

    Debug iface = (Debug) new VisualFoodHunt();
    Thread inputThread = new Thread(iface);
    inputThread.start();
    
    new FoodHunt(width, height, iface, inputThread);
  }  

  /** Open a dialog to select file. */
  public String selectFile(File curdir, String filter) {
    JFileChooser chooser = new JFileChooser(curdir);
    chooser.setFileFilter(new FileNameExtensionFilter("Filtered", filter));
    int returnVal = chooser.showOpenDialog(gridCanvas);
    if(returnVal == JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile().getName();
    } else 
      return null;
  }

}
