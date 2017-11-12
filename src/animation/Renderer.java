package animation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import container.Building;

@SuppressWarnings("serial")
public class Renderer extends JPanel implements Runnable{

	
	
	private static final int PERIOD = 45;
	private static final int MAX_NO_DELAYS = 20;
	private static final int MAX_SKIP_FRAMES = 10;
	
	public static final int PWIDTH = 800;
	public static final int PHEIGHT = 600;
	
	private Thread animator = null;
	private Image dbImage = null;
	private Graphics dbg = null;
	
	private int fps;
	
	
	private Building building = null;
	
	public Renderer(Building building) {
		// intialize the panel
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		
		// intialize the listener
		setFocusable(true);
		requestFocus();
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		this.building = building;
	}

	@Override
	public void addNotify() {

		super.addNotify();
		if (animator == null) {
			animator = new Thread(this);
			animator.start();
		}
	}

	
	private void gameRender() {
		if (dbImage == null) {
			dbImage = this.createImage(PWIDTH, PHEIGHT);
			if (dbImage == null) {
				System.err.println("cannot create dbgImage");
				return;
			} else {
				dbg = dbImage.getGraphics();
			}
		}
		
		dbg.setColor(Color.WHITE);
		dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

		
		building.draw(dbg);
		
		dbg.setFont(new Font("TimesRoman", Font.PLAIN, 22));
		dbg.drawString("FPS: " + fps, 
				Renderer.PWIDTH - 100, Renderer.PHEIGHT - 20);
		
	}
	

	

	private void paintScreen() {
		Graphics mainG = this.getGraphics(); // get the pen for panel
		if (mainG != null && dbImage != null) {
			mainG.drawImage(dbImage, 0, 0, null);
			Toolkit.getDefaultToolkit().sync();
			mainG.dispose();
		}
	}

	@Override
	public void run() {

		/*
		 * Nanosecond: timeDiff, beforeTime, afterTime, sleepingTime,
		 * oversleepTime Millsecond: excess, period
		 */
		long timeDiff, afterTime, sleepingTime, oversleepTime = 0L;
		int no_delays = 0;
		long excess = 0L;

		int fpsCnt = 0;
		
		while (true) {
			long beforeTime = System.nanoTime();

			//gameUpdate();
			gameRender();
			paintScreen();

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepingTime = Renderer.PERIOD * 1000000L - timeDiff - oversleepTime;

			if (sleepingTime > 0) { // enough time for threads to sleep
				try {
					Thread.sleep(sleepingTime / 1000000L); // nano -> ms
					oversleepTime = (System.nanoTime() - afterTime) - sleepingTime; // error
				} catch (Exception e) {
				}
			} else {
				oversleepTime = 0L;
				excess -= sleepingTime / 1000000L; // nano -> ms
				no_delays++;
			}

			// garbage collector
			if (no_delays >= MAX_NO_DELAYS) {
				Thread.yield();
				no_delays = 0;
			}

			// excess time
			int skips = 0;
			while ((excess >= Renderer.PERIOD) && (skips < MAX_SKIP_FRAMES)) {
				excess -= Renderer.PERIOD;
				skips++;
			}
			
			fpsCnt ++;
			if(fpsCnt >= 20) {
				fps = (int) (1000 / ((float)timeDiff / 100000L));
				
				fpsCnt = 0;
			}
			
			dbg.setColor(Color.BLUE);

		}
	}

}
