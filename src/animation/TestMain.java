package animation;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import container.Building;

public class TestMain {
	
	public static void main(String[] args) {
		
		Building building = new Building();
		
		
		
		JFrame jFrame = new JFrame("Elevator");
		
		jFrame.setResizable(false);
		jFrame.setVisible(true);
		
		jFrame.setLocation(new Point(100, 80));
		
		jFrame.setLayout(new BorderLayout());
	
		
		jFrame.add(new Renderer(building), BorderLayout.CENTER);
		
		TextField cmdInputArea = new TextField();
		
		cmdInputArea.setFocusable(true);
		cmdInputArea.addKeyListener(new KeyAdapter() {
			

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					
					String text = cmdInputArea.getText();
					if(text.startsWith("o")) {
						
						StringTokenizer stk = new StringTokenizer(text);
						if(stk.countTokens() != 3)
							return;
						
						stk.nextToken();
						
						int floor = Integer.parseInt(stk.nextToken());
						int dir = Integer.parseInt(stk.nextToken());
						
						building.addOuterCommand(floor, dir);
						
					} else if(text.startsWith("i")) {
						
						StringTokenizer stk = new StringTokenizer(text);
						if(stk.countTokens() != 3)
							return;
						
						
						stk.nextToken();
						int floor = Integer.parseInt(stk.nextToken());
						int index = Integer.parseInt(stk.nextToken());
						
						building.addInnerCommand(floor, index);
					} 
					cmdInputArea.setText("");
					
				}
			}
		});
		
		jFrame.add(cmdInputArea, BorderLayout.SOUTH);
		jFrame.pack();
		
	}
}
