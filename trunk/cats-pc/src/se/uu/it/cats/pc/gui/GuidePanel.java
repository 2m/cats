package se.uu.it.cats.pc.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.uu.it.cats.pc.actor.Area;

public class GuidePanel extends JPanel {
	
	private static JTextArea _orderList0;
	private static JTextArea _orderList1;
	private static JTextArea _orderList2;
	
	public GuidePanel() {
		
		for (int i = 0; i < Area.CAT_COUNT; i++) {
			add(new OrderPanel(i));
		}
		
	}
	
	private class OrderPanel extends JPanel {
		
		public OrderPanel(int i) {
			
			setLayout(new BorderLayout());
			
			if (i == 0) {
				_orderList0 = new JTextArea(30, 20);
				add(new JScrollPane(_orderList0), BorderLayout.NORTH);
				
				JButton button = new JButton("Clear");
				button.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						_orderList0.setText("");
					}					
				});
				add(button, BorderLayout.SOUTH);
			}
			else if (i == 1) {
				_orderList1 = new JTextArea(30, 20);
				add(new JScrollPane(_orderList1), BorderLayout.NORTH);
				
				JButton button = new JButton("Clear");
				button.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						_orderList1.setText("");
					}					
				});
				add(button, BorderLayout.SOUTH);
			}
			else if (i == 2) {
				_orderList2 = new JTextArea(30, 20);
				add(new JScrollPane(_orderList2), BorderLayout.NORTH);
				
				JButton button = new JButton("Clear");
				button.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						_orderList2.setText("");
					}					
				});
				add(button, BorderLayout.SOUTH);
			}
			
			
		}
		
	}
	
	public static void updateOrder(int id, String order) {
		if (id == 0) {
			_orderList0.append(order+"\n");
		}
		else if (id == 1) {
			_orderList1.append(order+"\n");
		}
		else if (id == 2) {
			_orderList2.append(order+"\n");
		}
	}

}
