package elevator;

import java.awt.Color;
import java.awt.Graphics;
import animation.Renderer;
import container.Building;
import processor.Scheduler;

public class ClientElevator extends Elevator implements Runnable {

	private static final int WIDTH = 30;
	private static final int HEIGHT = 30;

	private final int curSpeed = 1;
	

	public ClientElevator(Scheduler ref) {
		super(ref);
	}

	private int getCurFloor() {
		return (int) (Math.floor(curHeight / Building.HEIGHT_PER_FLOOR)) + 1;
	}

	private void move() {

		int destFloor;

		synchronized (this) {
			destFloor = curExecutingList.get(0);
		}

		if (curHeight == (destFloor - 1) * Building.HEIGHT_PER_FLOOR) {
			return;
		}

		while ((destFloor - 1) * Building.HEIGHT_PER_FLOOR != curHeight) {

			int tmp;
			synchronized (this) {
				tmp = curExecutingList.get(0);
			}
			
			synchronized (this) {
				if (curState == Elevator.MOVE_UP_RECEIVE_DOWN || curState == Elevator.MOVE_UP_RECEIVE_UP) {
					if (tmp > getCurFloor())
						destFloor = tmp;
				} else if (curState == Elevator.MOVE_DOWN_RECEIVE_DOWN || curState == Elevator.MOVE_DOWN_RECEIVE_UP) {
					if (tmp < getCurFloor())
						destFloor = tmp;
				}
			}

			// re-update
			if(curState == Elevator.MOVE_UP_RECEIVE_DOWN) {
				if(destFloor < getCurFloor()) {
					curState = Elevator.MOVE_DOWN_RECEIVE_DOWN;
				}
			} else if(curState == Elevator.MOVE_DOWN_RECEIVE_UP) {
				if(destFloor > getCurFloor()) {
					curState = Elevator.MOVE_UP_RECEIVE_UP;
				}
			}

			synchronized (this) {
				if (curState == Elevator.MOVE_UP_RECEIVE_DOWN || curState == Elevator.MOVE_UP_RECEIVE_UP)
					curHeight += curSpeed;
				else if (curState == Elevator.MOVE_DOWN_RECEIVE_DOWN || curState == Elevator.MOVE_DOWN_RECEIVE_UP)
					curHeight -= curSpeed;
			}

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		doorOpen = true;
		open();
		doorOpen = false;
		
		synchronized (schedulerReference) {
			schedulerReference.doNotify();
		}

	}

	@Override
	public void open() {
		System.out.println("Door open");
		try {
			Thread.sleep(1000);

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				while (curExecutingList.isEmpty()) {
					try {
						wait();
					} catch (InterruptedException e) {	
						e.printStackTrace();
					}
				}
			}
			move();
			synchronized (this) {
				curExecutingList.remove(0);
			}
			synchronized (this) {
				if (curExecutingList.isEmpty()) {
					curState = Elevator.STAND;
				}
			}
		}
	}

	@Override
	public void draw(Graphics dbg, int startX) {
		// System.out.println(Thread.currentThread().getName());
		dbg.setColor(Color.RED);
		dbg.fillRect(startX, Renderer.PHEIGHT - curHeight - ClientElevator.HEIGHT, ClientElevator.WIDTH,
				ClientElevator.HEIGHT);
	}

}
