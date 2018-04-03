package paint;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Paint {
	private final int STROKE_WIDTH = 5;
	
	private JFrame window;
	private JButton brushBut, lineBut, ellipseBut, rectBut, strokeBut, fillBut, clearBut;
	private int currentAction;
	private Color strokeColor = Color.BLACK; 
	private Color fillColor = Color.BLACK;
	private ArrayList<Drawing> drawings = new ArrayList<Drawing>();
	private BasicStroke strokeWidth = new BasicStroke(STROKE_WIDTH);
	
	private Paint() {
		window = new JFrame("Java Paint");
		window.setSize(800, 600);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		
		DrawingPanel drawingPanel = new DrawingPanel();
		drawingPanel.setOpaque(false);
	
		brushBut = makeButton("brush.png", 1);
		lineBut = makeButton("line.png", 2);
		ellipseBut = makeButton("ellipse.png", 3);
		rectBut = makeButton("rectangle.png", 4);
		
		strokeBut = makeColorButton("stroke.png", 5, true);
		strokeBut.setBackground(strokeColor);
		fillBut = makeColorButton("fill.png", 6, false);
		fillBut.setBackground(fillColor);
		
		clearBut = new JButton("Clear");
		clearBut.setBackground(Color.WHITE);
		clearBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawings.clear();
				drawingPanel.repaint();
			}
		});
		
		Box box = Box.createHorizontalBox();
		box.add(brushBut);
		box.add(lineBut);
		box.add(ellipseBut);
		box.add(rectBut);
		box.add(strokeBut);
		box.add(fillBut);
		box.add(clearBut);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(box);
		
		window.add(drawingPanel, BorderLayout.CENTER);
		window.add(buttonPanel, BorderLayout.SOUTH);
		window.setVisible(true);
	}
	
	private JButton makeColorButton(String path, final int actionNum, final boolean stroke) {
		JButton but = new JButton();
		ImageIcon icon = new ImageIcon(path);
		but.setIcon(icon);
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stroke) {
					strokeColor = JColorChooser.showDialog(window, "Pick a Stroke Color", Color.BLACK);
					strokeBut.setBackground(strokeColor);
				} else {
					fillColor = JColorChooser.showDialog(window, "Pick a Fill Color", Color.BLACK);
					fillBut.setBackground(fillColor);
				}
			}
		});
		
		return but;
	}
	
	private JButton makeButton(String path, final int actionNum) {
		JButton but = new JButton();
		ImageIcon icon = new ImageIcon(path);
		but.setIcon(icon);
		
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentAction = actionNum;
				System.out.println(currentAction);
			}
		});
		return but;
	}
	
	private class DrawingPanel extends JPanel {
		private Point drawStart, drawEnd;
		
		public DrawingPanel() {
			
			this.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				
				public void mousePressed(MouseEvent e) {
					drawStart = new Point(e.getX(), e.getY());
					drawEnd = drawStart;
					repaint();
				}

				public void mouseReleased(MouseEvent e) {
					drawEnd = new Point(e.getX(), e.getY());
					Shape aShape = drawRectangle(drawStart.x, drawStart.y,
							drawEnd.x, drawEnd.y);
					
					drawings.add(new Drawing(aShape, strokeColor, fillColor));
					
					drawStart = null;
					drawEnd = null;
					repaint();
				}
			});
			
			this.addMouseMotionListener(new MouseMotionListener() {
				public void mouseMoved(MouseEvent e) {}
				
				public void mouseDragged(MouseEvent e) {
					drawEnd = new Point(e.getX(), e.getY());
					repaint();
				}
			});
		}
		
		private Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);
			int width = Math.abs(x1 - x2);
			int height = Math.abs(y1 - y2);
			
			Rectangle2D.Float rectangle = new Rectangle2D.Float(x, y, width, height);
			return rectangle;
		}
		
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			
			g2d.setStroke(strokeWidth);
			
			// Para hacer el background blanco
			
			Shape background = drawRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
			g2d.setPaint(Color.WHITE);
			g2d.fill(background);
			
			// Dibuja las formas guardadas en el ArrayLis
			for (Drawing drawing : drawings) {
				Shape shape = drawing.getShape();
				Color strokeColor = drawing.getStrokeColor();
				Color fillColor = drawing.getFillColor();
				
				g2d.setStroke(strokeWidth);
				g2d.setPaint(fillColor);
				g2d.fill(shape);
				g2d.setPaint(strokeColor);
				g2d.draw(shape);
			}
			
			if (drawStart != null && drawEnd != null) {
				// g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
				Shape tempRect = drawRectangle(drawStart.x, drawStart.y,
						drawEnd.x, drawEnd.y);
				
				g2d.setStroke(strokeWidth);
				g2d.setPaint(fillColor);
				g2d.fill(tempRect);
				g2d.setPaint(strokeColor);
				g2d.draw(tempRect);
			}
		}
	}
	
	public static void main(String[] args) {
		new Paint();
	}
}
