package se.uu.it.cats.pc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import se.uu.it.cats.pc.Logger;

import se.uu.it.cats.pc.gui.Area;
import se.uu.it.cats.pc.gui.DataPanel;
import se.uu.it.cats.pc.gui.MainPanel;
import se.uu.it.cats.pc.gui.PanelBluetooth;
import se.uu.it.cats.pc.gui.PrintArea;

public class Main
{
	private Area  newArea;
	private DataPanel dataPanel;
	private PrintArea printNewArea;
	private PanelBluetooth panelBluetooth;
	private MainPanel mainPanel;
	private int arenaWidth = 400;
	private int arenaHeight = 400;
	private int windowWidth = 900;
	private int windowHeight = 700;
	private JCheckBoxMenuItem sgrid;
	private JCheckBoxMenuItem sslider;
	
	public Main() {
		
		newArea = Area.getInstance();
		printNewArea = new PrintArea(newArea, arenaWidth, arenaHeight);
		dataPanel = new DataPanel(newArea, arenaHeight);
		panelBluetooth = new PanelBluetooth(windowWidth);
		mainPanel = new MainPanel(newArea, arenaHeight);
		
		// Create the Frame-window
		JFrame frame = new JFrame("Cats - Arena");
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		
		//Create menu-bar
		JMenuBar menubar = new JMenuBar();
	    ImageIcon icon = new ImageIcon("close.png");
		JMenu file = new JMenu("File");
		JMenu view = new JMenu("View");
		file.setMnemonic(KeyEvent.VK_F);
		view.setMnemonic(KeyEvent.VK_V);

		// Add grid-box to view-menu
		sgrid = new JCheckBoxMenuItem("Show grid");
		sgrid.setState(true);
		sgrid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (sgrid.getState() == false ) { //Worked with false for some reason
                    sgrid.setState(false);
                    printNewArea.showGrid(false);
                } else {
                    sgrid.setState(true);
                    printNewArea.showGrid(true);
                }
            }
        });
		view.add(sgrid);
		
		// Add slider-box to view-menu
		sslider = new JCheckBoxMenuItem("Show zoom");
		sslider.setState(false);
		sslider.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (sslider.getState() == false ) { //Worked with false for some reason
                	sslider.setState(false);
                    printNewArea.showSlider(false);
                } else {
                	sslider.setState(true);
                    printNewArea.showSlider(true);
                }
            }
        });
		view.add(sslider);
		
		//Add close-item to file-menu
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
		menubar.add(view);
		
		frame.setJMenuBar(menubar);	
		
		//Creation of panels
		JPanel panelBackground = new JPanel();
		JPanel panelData = new JPanel();
		JPanel panelArena = new JPanel();
		JPanel panelRawdata = new JPanel();
		JPanel panelSettings = new JPanel();
		
		//frame.getContentPane().add(newArea);
		
		panelBackground.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panelData.setBorder(BorderFactory.createLineBorder(Color.gray));
		panelArena.setBorder(BorderFactory.createLineBorder(Color.gray));
		panelRawdata.setBorder(new TitledBorder(
				new LineBorder(Color.gray, 1, false),
				"Connectivity",
				TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION)
		);
		panelSettings.setBorder(BorderFactory.createLineBorder(Color.gray));
		
		/*panelLeft.add(button1, BorderLayout.NORTH);
		panelLeft.add(label1, BorderLayout.EAST);*/
		//panelLeft.add(button1);
		//panelLeft.add(label1);

		
		panelArena.add(printNewArea);
		panelData.add(dataPanel);
		panelRawdata.add(panelBluetooth);
		panelSettings.add(mainPanel);
		
		panelBackground.add(BorderLayout.WEST, panelSettings);
		panelBackground.add(BorderLayout.CENTER, panelArena);
		panelBackground.add(BorderLayout.EAST, panelData);		
		panelBackground.add(BorderLayout.SOUTH, panelRawdata);
		
		
		
		frame.getContentPane().add(panelBackground);
		//frame.getContentPane().add(panelArena);
		//frame.pack();
		frame.show();

	    /*newArea = new Area(windowWidth, windowHeight);
	    f.getContentPane().add(newArea);
	    f.setSize(newArea.getAreaWidth(), newArea.getAreaHeight());
		f.show();
		pack();*/
	    frame.setVisible(true);
	    frame.setSize(windowWidth, windowHeight);
		
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
