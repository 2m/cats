package se.uu.it.cats.pc.gui.tab;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import se.uu.it.cats.pc.gui.Area;
import se.uu.it.cats.pc.gui.DataPanel;
import se.uu.it.cats.pc.gui.MainPanel;
import se.uu.it.cats.pc.gui.PanelBluetooth;
import se.uu.it.cats.pc.gui.PrintArea;

public class MainTab extends JPanel
{
	private final int _arenaWidth = 400;
	private final int _arenaHeight = 400;
	
	PrintArea _printArea = null;
	DataPanel _dataPanel = null;
	PanelBluetooth _panelBluetooth = null;
	MainPanel _mainPanel = null;
	
	public MainTab()
	{
		super(new BorderLayout());
		
		((BorderLayout)getLayout()).setHgap(10);
		
		Area newArea = Area.getInstance();
		
		_mainPanel = new MainPanel(_arenaHeight);
		add(_mainPanel, BorderLayout.WEST);
		
		_printArea = new PrintArea(_arenaWidth, _arenaHeight);
		add(_printArea, BorderLayout.CENTER);
		
		_dataPanel = new DataPanel(_arenaHeight);
		add(_dataPanel, BorderLayout.EAST);
		
		_panelBluetooth = new PanelBluetooth();
		add(_panelBluetooth, BorderLayout.SOUTH);
	}
	
	public PrintArea getPrintArea()
	{
		return _printArea;
	}
	
	public void repaint()
	{
		super.repaint();
		
		try {
			_printArea.repaint();
			_dataPanel.repaint();
		}
		catch (NullPointerException ex) {
			
		}
	}
}
