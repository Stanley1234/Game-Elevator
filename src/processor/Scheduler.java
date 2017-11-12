package processor;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import container.Building;
import elevator.Elevator;

public class Scheduler implements Runnable {
	
	
	public static final int UP_COMMAND = 0;
	public static final int DOWN_COMMAND = 1;
	

	// waiting list
    private ArrayList<ArrayList<Integer>> innerWaitingList;
 	private ArrayList<Integer> upWaitingList;
	private ArrayList<Integer> downWaitingList;
	
	private ArrayList<Elevator> elevators;
	
	private boolean wasSingalled = false;
	
	
	public Scheduler() {
		upWaitingList = new ArrayList<>();
		downWaitingList = new ArrayList<>();
		innerWaitingList = new ArrayList<>();
		elevators = new ArrayList<>();
	}
	
	public void addElevator(Elevator newElevator) throws InvalidParameterException {
		// currently, only supports one elevator running simultaneously
		if(newElevator == null)
			throw new InvalidParameterException("Elevator instance cannot be null");
		
		elevators.add(newElevator);
		innerWaitingList.add(new ArrayList<>());
		
	}
	
	
	public synchronized void addOuterCommand(int floor, int commandType) throws InvalidParameterException{
		
		if(floor <= 0 || floor > Building.FLOORS_NUM)
			return;
		
		switch(commandType) {
		case UP_COMMAND:
			upWaitingList.add(floor);
			break;
		case DOWN_COMMAND:
			downWaitingList.add(floor);
			break;
		default:
			throw new InvalidParameterException("Command Type Error");
		}
		
		doNotify();
	}
	
	
	public synchronized void addInnerCommand(int floor, int index) {
		
		if(floor <= 0 || floor > Building.FLOORS_NUM || index >= elevators.size())
			return;
		innerWaitingList.get(index).add(floor);
		
		doNotify();
		
	}
	

	
	private synchronized boolean hasAbove(ArrayList<Integer> collection, int level) {
		
		if(collection == null)
			return false;
		
		for(Integer floor : collection) {
			if(floor >= level)
				return true;
		}
		return false;
	}
	
	private synchronized boolean hasBelow(ArrayList<Integer> collection, int level) {
		
		if(collection == null)
			return false;
		
		for(Integer floor : collection) {
			if(floor <= level)
				return true;
		}
		return false;
	}
	
	
	private synchronized void addAbove(ArrayList<Integer> collection, int index, int level, boolean reverse) {
		
		Elevator curInstance = elevators.get(index);
		synchronized (curInstance) {
			ArrayList<Integer> tmpList = new ArrayList<>(curInstance.getCurExecutingList());
			
			if(collection == null)
				return;
			
			
			for (Iterator<Integer> iter = collection.iterator(); iter.hasNext();) {
				Integer floor = iter.next();
	
				if (floor >= level) {
					tmpList.add(floor);
					iter.remove();
				}
			}
	
			// ascending
			if(!reverse)
				Collections.sort(tmpList);
			else
				Collections.sort(tmpList, Collections.reverseOrder());
	
			tmpList = new ArrayList<>(new LinkedHashSet<>(tmpList));
		
		
			curInstance.setCurExecutingList(tmpList);
		}
	
	}
	
	private synchronized void addBelow(ArrayList<Integer> collection, int index, int level, boolean reverse) {
		if(collection == null)
			return;
		Elevator curInstance = elevators.get(index);
		synchronized (curInstance) {
			
			ArrayList<Integer> tmpList = new ArrayList<>(curInstance.getCurExecutingList());
			
			
			for (Iterator<Integer> iter = collection.iterator(); iter.hasNext();) {
				Integer floor = iter.next();
	
				if (floor <= level) {
					tmpList.add(floor);
					iter.remove();
				}
			}
			// descending
			if(reverse)
				Collections.sort(tmpList, Collections.reverseOrder());
			else
				Collections.sort(tmpList);
			
			tmpList = new ArrayList<>(new LinkedHashSet<>(tmpList));
			curInstance.setCurExecutingList(tmpList);
		}
	}
	
	private int estimateElevatorFloor(int height) {
		return (int)(Math.floor(height / Building.HEIGHT_PER_FLOOR)) + 1;
	}
	
	
	private boolean hasRequests() {
		
		
		synchronized (this) {
			if(!upWaitingList.isEmpty())
				return true;
		}
		
		synchronized (this) {
			if(!downWaitingList.isEmpty())
				return true;
				
		}
		
		synchronized (this) {
			for(int i = 0;i < innerWaitingList.size();i ++)
				if(!innerWaitingList.get(i).isEmpty())
					return true;
				
		}
		
		return false;
	}
	
	private synchronized void computeExecutingList() {
		
		
		if(!hasRequests())
			return;
		
		Integer[] sequence = new Integer[elevators.size()];
		for(int i = 0;i < sequence.length;i ++)
			sequence[i] = i;
		Collections.shuffle(Arrays.asList(sequence));
		
		for(int i = 0;i < sequence.length;i ++) {
			int index = sequence[i];
			Elevator elevator = elevators.get(index);
			
			
			int curFloor;
			synchronized (elevator) {
				curFloor = estimateElevatorFloor(elevator.getCurHeight());
			}
			
			int curState;
			synchronized (elevator) {
				curState = elevator.getState();
			}

			// up / down waiting list
			if(curState == Elevator.MOVE_UP_RECEIVE_UP) {
				if(hasAbove(upWaitingList, curFloor)) {
					addAbove(upWaitingList, index, curFloor, false);			
				} 
				 
			} else if(curState == Elevator.MOVE_DOWN_RECEIVE_DOWN) {
				if(hasBelow(downWaitingList, curFloor)) {
					addBelow(downWaitingList, index, curFloor, true);
				}
				
			} else if(curState == Elevator.STAND){

					if(hasAbove(innerWaitingList.get(index), curFloor)) {

						addAbove(innerWaitingList.get(index), index, curFloor, false);
						elevator.setState(Elevator.MOVE_UP_RECEIVE_UP);
						
					} else if(hasBelow(innerWaitingList.get(index), curFloor)) {
						addBelow(innerWaitingList.get(index), index, curFloor, true);
						elevator.setState(Elevator.MOVE_DOWN_RECEIVE_DOWN);
						
					} else if(hasAbove(upWaitingList, curFloor)) {
								
					 	addAbove(upWaitingList, index, curFloor, false);
						elevator.setState(Elevator.MOVE_UP_RECEIVE_UP);
						
					} else if(hasAbove(downWaitingList, curFloor)) {
						addAbove(downWaitingList, index, curFloor, true);
						elevator.setState(Elevator.MOVE_UP_RECEIVE_DOWN);
						

					} else if(hasBelow(downWaitingList, curFloor)) {
						addBelow(downWaitingList, index, curFloor, true);
						elevator.setState(Elevator.MOVE_DOWN_RECEIVE_DOWN);
						

					} else if(hasBelow(upWaitingList, curFloor)) {
						addBelow(upWaitingList, index, curFloor, false);
						elevator.setState(Elevator.MOVE_DOWN_RECEIVE_UP);
						
					} 
				
				
					
			}
			
			// inner waiting list
			if(curState == Elevator.MOVE_DOWN_RECEIVE_DOWN
					|| curState == Elevator.MOVE_DOWN_RECEIVE_UP) {
				addBelow(innerWaitingList.get(index), index, curFloor, true);
			} else if(curState == Elevator.MOVE_UP_RECEIVE_DOWN
					|| curState == Elevator.MOVE_UP_RECEIVE_UP)
				addAbove(innerWaitingList.get(index), index, curFloor, false);
		}
		
		
		
	}
	
	public void doNotify() {
		synchronized (this) {
			wasSingalled = true;
			notify();
		}
		
	}
	
	@Override
	public void run() {
		
		while (true) {

			if (elevators.size() == 0) {
				System.err.println("No Elevator Available");
				return;
			}
			
			computeExecutingList();
			
			synchronized (this) {
				try {
					while(!wasSingalled) {
						wait();
					}
				} catch (InterruptedException e) {				
					e.printStackTrace();
				}	
				wasSingalled = false;
			}

		}

	}
	
	
	public void draw(Graphics dbg) {
		
		final int GAP = 40;
		final int SMALL_GAP = 20;
		
		dbg.setFont(new Font("TimesRoman", Font.PLAIN, 14));
		dbg.setColor(Color.BLACK);
		
		
		// draw cur list
		for(int i = 0;i < elevators.size();i ++) {
			dbg.drawString("CUR" + i, 600 + i * GAP, 480);
			
			
			Elevator elevator = elevators.get(i);
			int executingListSize;
			ArrayList<Integer> executingList = elevator.getCurExecutingList();
			synchronized (elevator) {
				executingListSize = executingList.size();
				for(int j = 0;j < executingListSize;j ++) {
					dbg.drawString(executingList.get(j) + "", 600 + i * GAP, 480 + (j + 1) * SMALL_GAP);
				}
			}
				
			
			
		}
		
		// draw inner list
		for(int i = 0;i < elevators.size();i ++) {
			dbg.drawString("INN" + i, 600 + i * GAP, 320);
			
			
			
			int listSize;
			ArrayList<Integer> innList = innerWaitingList.get(i);
			
			synchronized (this) {
				listSize = innList.size();
				for(int j = 0;j < listSize;j ++) {
					dbg.drawString(innList.get(j) + "", 600 + i * GAP, 320 + (j + 1) * SMALL_GAP);
				}
			}
				
			
		}
		
		
		dbg.drawString("UPW", 600, 80);
		synchronized (this) {
			for(int i = 0;i < upWaitingList.size();i ++)
				dbg.drawString(upWaitingList.get(i) + "", 600, 100 + i * SMALL_GAP);
		}
		
		dbg.drawString("DOW", 640, 80);
		synchronized (this) {
			for(int i = 0;i < downWaitingList.size();i ++)
				dbg.drawString(downWaitingList.get(i) + "", 640, 100 + i * SMALL_GAP);
		}
		
		
		
	}
}
