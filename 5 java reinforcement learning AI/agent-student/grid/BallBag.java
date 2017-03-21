// $Id: AgentInfo.java 1602 2013-03-17 04:43:04Z cengiz $

package grid;

import agent.*;
import grid.*;
import block.*;
import iface.*;

// Bag to collect balls
public class BallBag {
    /** Bag has a limit */
    final int ballCapacity = 5;

    /** Number of balls in bag */
    int ballsCarried = 0;

    /** Connects to agent's embodiment on grid */
    AgentInfo agent;

    public BallBag(AgentInfo agent) {
	this.agent = agent;
    }

    public boolean isFull() {
	assert ballsCarried <= ballCapacity : "Exceeded ball capacity?";
	return (ballsCarried == ballCapacity);
    }

    public void addBall() throws BallBagFullException {
	if (isFull()) throw new BallBagFullException("Cannot add; bag full!");
	ballsCarried++;
    }

    public int deposit() {
	int numDeposited = ballsCarried;
	ballsCarried = 0;
	return numDeposited;
    }

}
