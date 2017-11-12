package container;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import animation.Renderer;
import elevator.ClientElevator;
import elevator.Elevator;
import processor.Scheduler;

public class Building {
	
	public static final int FLOORS_NUM = 20;
	public static final int HEIGHT_PER_FLOOR = 30;

	Scheduler scheduler;
	Elevator e1;
	Elevator e2;
	Elevator e3;
	
	public Building() {
		
		scheduler = new Scheduler();
		
		e1 = new ClientElevator(scheduler);
		Thread t1 = new Thread((ClientElevator)e1, "ClientElevator1-Thread");
		t1.start();
		
		e2 = new ClientElevator(scheduler);
		Thread t2 = new Thread((ClientElevator)e2, "ClientElevator2-Thread");
		t2.start();
		
		e3 = new ClientElevator(scheduler);
		Thread t3 = new Thread((ClientElevator)e3, "ClientElevator3-Thread");
		t3.start();
		
		scheduler.addElevator(e1);
		scheduler.addElevator(e2);
		scheduler.addElevator(e3);
		
		Thread tsThread = new Thread(scheduler, "TS-Thread");
		tsThread.start();
		
		
		
	}
	
	public void addInnerCommand(int destFloor, int index) {
		scheduler.addInnerCommand(destFloor, index);
	}
	
	public void addOuterCommand(int destFloor, int dir) {
		scheduler.addOuterCommand(destFloor, dir);
	}
	
	/*
	 * draw() is called by the rendered class
	 * */
	public void draw(Graphics dbg) {
		drawElevators(dbg);
		drawBuildings(dbg);
		drawWaitingList(dbg);
		drawFloorTag(dbg);
	}
	
	
	private void drawElevators(Graphics dbg) {
		//System.out.println("Building:" + Thread.currentThread().getName());
		e1.draw(dbg, 100);
		e2.draw(dbg, 300);
		e3.draw(dbg, 500);
	}
	
	private void drawBuildings(Graphics dbg) {
		dbg.setColor(new Color(183, 87, 23));
		dbg.fillRect(40, 0, 60, Renderer.PHEIGHT);
		dbg.fillRect(130, 0, 60, Renderer.PHEIGHT);
		
		dbg.fillRect(240, 0, 60, Renderer.PHEIGHT);
		dbg.fillRect(330, 0, 60, Renderer.PHEIGHT);
		
		dbg.fillRect(440, 0, 60, Renderer.PHEIGHT);
		dbg.fillRect(530, 0, 60, Renderer.PHEIGHT);
	}
	
	private void drawWaitingList(Graphics dbg) {
		
		scheduler.draw(dbg);
		
	}
	
	private void drawFloorTag(Graphics dbg) {
		
		dbg.setColor(Color.BLUE);
		dbg.setFont(new Font("TimesRoman", Font.PLAIN, 10));
		for(int i = 0;i < Building.FLOORS_NUM;i ++) {
			dbg.drawString(i + 1 + "", 80, Renderer.PHEIGHT - i * Building.HEIGHT_PER_FLOOR);
		}

		for(int i = 0;i < Building.FLOORS_NUM;i ++) {
			dbg.drawString(i + 1 + "", 280, Renderer.PHEIGHT - i * Building.HEIGHT_PER_FLOOR);
		}
		
		for(int i = 0;i < Building.FLOORS_NUM;i ++) {
			dbg.drawString(i + 1 + "", 480, Renderer.PHEIGHT - i * Building.HEIGHT_PER_FLOOR);
		}
		
	}
	
}
