package elevator;

import java.awt.Graphics;
import java.util.ArrayList;

import processor.Scheduler;

public abstract class Elevator {
	
	
	
	// states of elevator
	public static final int MOVE_UP_RECEIVE_DOWN = 1;
	public static final int MOVE_UP_RECEIVE_UP = 2;
	public static final int MOVE_DOWN_RECEIVE_UP = 3;
	public static final int MOVE_DOWN_RECEIVE_DOWN  = 4;
	public static final int STAND = 7;
	
	protected int curState = Elevator.STAND;
	protected int curHeight;
	
	protected boolean doorOpen;
	protected Scheduler schedulerReference;
	protected volatile ArrayList<Integer> curExecutingList;


	
	
	public Elevator(Scheduler ref) {
		curExecutingList = new ArrayList<>();
		schedulerReference = ref;
		
		curState = Elevator.STAND;
		curHeight = 0;
	}
	
	
	
	public abstract void open();	
	//public abstract void close();
	public abstract void draw(Graphics dbg, int startX);
	
	public synchronized int getState() {
		return curState;
	}
	
	public synchronized void setState(int state) {
		this.curState = state;
	}
	
	public int getCurHeight() {
		return curHeight;
	}
	
	public synchronized ArrayList<Integer> getCurExecutingList() {
		return curExecutingList;
	}
	
	public synchronized void setCurExecutingList(ArrayList<Integer> newExecutingList) {
		if(newExecutingList == null)
			return;
		curExecutingList = newExecutingList;
		notify();
	}

}
