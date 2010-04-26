package se.uu.it.cats.pc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import se.uu.it.cats.pc.Logger;

import se.uu.it.cats.pc.gui.Area;
import se.uu.it.cats.pc.gui.DataPanel;
import se.uu.it.cats.pc.gui.PrintArea;

public class Main
{
	private Area  newArea;
	private DataPanel dataPanel;
	private PrintArea printNewArea;
	private int arenaWidth = 300;
	private int arenaHeight = 300;
	private int areaWidth = 500;
	private int areaHeight = 500;
	
	public Main() {
		
		newArea = Area.getInstance();
		printNewArea = new PrintArea(newArea, areaWidth, areaHeight);
		dataPanel = new DataPanel(newArea);
	  
		JFrame frame = new JFrame("Arena");
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		
		JMenuBar menubar = new JMenuBar();
	    ImageIcon icon = new ImageIcon("close.png");
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

	    JMenuItem fileClose = new JMenuItem("Close", icon);
	    fileClose.setMnemonic(KeyEvent.VK_C);
	    fileClose.setToolTipText("Exit application");
	    fileClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});

	    file.add(fileClose);
		menubar.add(file);

		frame.setJMenuBar(menubar);	
		
		JPanel panelBackground = new JPanel();
		JPanel panelData = new JPanel();
		JPanel panelArena = new JPanel();
		
		//frame.getContentPane().add(newArea);
		
		panelBackground.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JButton button1 = new JButton("Button Text");
		JLabel label1 = new JLabel("Label Text");
		
		/*panelLeft.add(button1, BorderLayout.NORTH);
		panelLeft.add(label1, BorderLayout.EAST);*/
		//panelLeft.add(button1);
		//panelLeft.add(label1);

		
		panelArena.add(printNewArea);
		panelData.add(dataPanel);
		
		panelBackground.add(BorderLayout.WEST, panelData);
		panelBackground.add(BorderLayout.EAST, panelArena);
		
		frame.getContentPane().add(panelBackground);
		//frame.getContentPane().add(panelArena);
		frame.pack();
		frame.show();

	    /*newArea = new Area(windowWidth, windowHeight);
	    f.getContentPane().add(newArea);
	    f.setSize(newArea.getAreaWidth(), newArea.getAreaHeight());
		f.show();
		pack();*/
	    frame.setVisible(true);
		
	    //f.getContentPane().add(new IteratorUnderStrike());
	    //f.setSize(850, 250);
	    //f.show();
	  }
	
	 public void simulate() {

		    while(true) {
		      newArea.tick();
		      printNewArea.repaint();
		      //System.out.println("tick");
		      try {
		        Thread.sleep(20);
		      }
		      catch (InterruptedException e) {}
		    }
		  }
	
	public static void main(String[] args)
	{
		Logger.init();
		//ColorSensor cs = new ColorSensor();
		//cs.run();
		
		/*Thread t4 = new Thread(new Mouse());
		t4.start();*/
		
		new Main().simulate();
	}
}
