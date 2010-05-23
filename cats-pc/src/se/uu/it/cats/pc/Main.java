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
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import se.uu.it.cats.pc.Logger;

import se.uu.it.cats.pc.gui.Area;
import se.uu.it.cats.pc.gui.DataPanel;
import se.uu.it.cats.pc.gui.MainPanel;
import se.uu.it.cats.pc.gui.PanelBluetooth;
import se.uu.it.cats.pc.gui.PrintArea;
import se.uu.it.cats.pc.gui.tab.BillBoardTab;
import se.uu.it.cats.pc.gui.tab.MainTab;

public class Main
{
	private int windowWidth = 900;
	private int windowHeight = 700;

	MainTab _mainTab = null;

	public Main() {

		_mainTab = new MainTab();

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
		JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem("Show grid");
		checkBox.setState(true);
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem)event.getSource();
				_mainTab.getPrintArea().showGrid(source.getState());                
			}
		});
		view.add(checkBox);

		// Add slider-box to view-menu
		checkBox = new JCheckBoxMenuItem("Show zoom");
		checkBox.setState(false);
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem)event.getSource();
				_mainTab.getPrintArea().showSlider(source.getState());
			}
		});
		view.add(checkBox);

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

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Main", _mainTab);
		tabbedPane.addTab("BillBoard", new BillBoardTab());

		frame.getContentPane().add(tabbedPane);

		frame.setVisible(true);
		frame.setSize(windowWidth, windowHeight);
	}

	public void simulate() {

		while(true) {
			Area.getInstance().tick();
			_mainTab.repaint();
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

		new Main().simulate();
	}
}
