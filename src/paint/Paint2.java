package paint;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Paint2 {
	private final int STROKE_WIDTH = 21;
	
	private JFrame window;
	private JButton brushBut, lineBut, ellipseBut, rectBut, eraserBut, strokeBut, fillBut, clearBut;
	private int currentAction = 1;
	private JLabel transparencyLabel;
	private JLabel strokeWidthLabel;
	private JLabel depthLabel;
	private JSlider transparencySlider;
	private JSlider strokeWidthSlider;
	private JSlider depthSlider;
	private Color strokeColor = Color.BLACK; 
	private Color fillColor = Color.BLACK;
	private ArrayList<Drawing> drawings = new ArrayList<Drawing>();
	private int strokeWidth = STROKE_WIDTH;
	private float transparency = 1.0f;
	private DecimalFormat transparencyFormat = new DecimalFormat("0.00");
	private int depth = 0;
	private boolean shiftDown;
	
	
	private Paint2() {
		window = new JFrame("Java Paint");
		window.setSize(1200, 800);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);
		
		DrawingPanel drawingPanel = new DrawingPanel();
		drawingPanel.setOpaque(false);
	
		brushBut = makeButton("brush.png", 1);
		lineBut = makeButton("line.png", 2);
		ellipseBut = makeButton("ellipse.png", 3);
		rectBut = makeButton("rectangle.png", 4);
		eraserBut = makeButton("eraser.png", 5);
		
		strokeBut = makeColorButton("stroke.png", 6, true);
		strokeBut.setBackground(strokeColor);
		fillBut = makeColorButton("fill.png", 7, false);
		fillBut.setBackground(fillColor);
		
		clearBut = new JButton("Clear");
		clearBut.setBackground(Color.WHITE);
		clearBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawings.clear();
				drawingPanel.repaint();
			}
		});
		
		transparencyLabel = new JLabel("Transparency: " + transparencyFormat.format(transparency));
		transparencyLabel.setHorizontalAlignment(JLabel.CENTER);
		transparencySlider = new JSlider(1, 100, 100);
		transparencySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				transparency = (float) (transparencySlider.getValue() * 0.01f);
				transparencyLabel.setText("Transparency: " + transparencyFormat.format(transparency));
			}
		});
		
		
		strokeWidthLabel = new JLabel(makeStrokeWidthText());
		strokeWidthLabel.setHorizontalAlignment(JLabel.CENTER);
		strokeWidthSlider = new JSlider(1, 80, STROKE_WIDTH);
		strokeWidthSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				strokeWidth = strokeWidthSlider.getValue();
				strokeWidthLabel.setText(makeStrokeWidthText());
				if (strokeWidth % 2 == 0) strokeWidth++; // Si es par lo hacemos impar
			}
		});
		
		depthLabel = new JLabel(makeDepthText());
		depthLabel.setHorizontalAlignment(JLabel.CENTER);
		depthSlider = new JSlider(-5, 5, depth);
		depthSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				depth = depthSlider.getValue();
				depthLabel.setText(makeDepthText());
			}
		});
		
		JPanel transparencyBox = new JPanel(new GridLayout(1, 2));
		transparencyBox.add(transparencyLabel);
		transparencyBox.add(transparencySlider);
		
		JPanel strokeWidthBox = new JPanel(new GridLayout(1, 2)); 
		strokeWidthBox.add(strokeWidthLabel);
		strokeWidthBox.add(strokeWidthSlider);

		JPanel depthBox = new JPanel(new GridLayout(1, 2));
		depthBox.add(depthLabel);
		depthBox.add(depthSlider);
		
		Box slidersBox = Box.createVerticalBox();
		slidersBox.add(strokeWidthBox);
		slidersBox.add(transparencyBox);
		slidersBox.add(depthBox);
		slidersBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(brushBut);
		buttonPanel.add(lineBut);
		buttonPanel.add(ellipseBut);
		buttonPanel.add(rectBut);
		buttonPanel.add(strokeBut);
		buttonPanel.add(fillBut);
		buttonPanel.add(eraserBut);
		buttonPanel.add(clearBut);
		buttonPanel.add(slidersBox);
		
		window.setJMenuBar(makeMenu());
		
		window.add(drawingPanel, BorderLayout.CENTER);
		window.add(buttonPanel, BorderLayout.SOUTH);
		window.setVisible(true);
	}
	
	private JMenuBar makeMenu() {
		JMenuBar mb = new JMenuBar();
		
		JMenu file = new JMenu("File");
		JMenu about = new JMenu("About");

		JMenuItem newMenu = new JMenuItem("New...");
		JMenuItem open = new JMenuItem("Open...");
		JMenuItem save = new JMenuItem("Save...");
		JMenuItem importMenu = new JMenuItem("Import...");
		JMenuItem export = new JMenuItem("Export...");
		JMenuItem exit = new JMenuItem("Exit");
		
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(".");	// Luego sacar el "." para usar la home
				FileFilter filter1 = new ExtensionFileFilter("jpt");
				fc.setFileFilter(filter1);
				fc.addChoosableFileFilter(filter1);
				
				int status = fc.showOpenDialog(window);
				
				if (status == JFileChooser.APPROVE_OPTION) {
					// openFile(fc.getSelectedFile());
				}
			}
		});
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(".");// Luego sacar el "." para usar la home
				int status = fc.showSaveDialog(window);
				
				if (status == JFileChooser.APPROVE_OPTION) {
					// saveFile(fc.getSelectedFile());
				}
			}
		});
		
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		file.add(newMenu);
		file.add(open);
		file.add(save);
		file.addSeparator();
		file.add(importMenu);
		file.add(export);
		file.addSeparator();
		file.add(exit);
		
		mb.add(file);
		mb.add(about);
		return mb;
	}
	
	class ExtensionFileFilter extends FileFilter {
		String extension;
		
		public ExtensionFileFilter(String extension) {
			this.extension = extension;
		}
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
			      return true;
			} else {
				String path = f.getAbsolutePath().toLowerCase();
				if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
			          return true;
			        }
			}
			return false;
		}

		@Override
		public String getDescription() {
			return null;
		}
	}

	private String makeStrokeWidthText() {
		String strokeWidthText = "";
		if (strokeWidth < 10) { 
			strokeWidthText = "Stroke Width:   " + strokeWidth;
		} else {
			strokeWidthText = "Stroke Width: " + strokeWidth;
		}
		
		return strokeWidthText;
	}
	
	private String makeDepthText() {
		String depthText = "";
		if (depth >= 0 ) {
			depthText = "Depth:  " + depth;
		} else {
			depthText = "Depth: " + depth;
		}
		
		return depthText;
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
		but.setBackground(Color.LIGHT_GRAY);
		
		but.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentAction = actionNum;
				System.out.println(currentAction);
			}
		});
		return but;
	}
	
	@SuppressWarnings("serial")
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
					
					Shape aShape = null;
					Shape innerShape = null;
					
					switch (currentAction) {
						case 2: aShape = drawLine(drawStart.x, drawStart.y,
								drawEnd.x, drawEnd.y);
								break;
						case 3: aShape = drawEllipse(drawStart.x, drawStart.y,
									drawEnd.x, drawEnd.y);
								innerShape = drawInnerEllipse(drawStart.x, drawStart.y,
									drawEnd.x, drawEnd.y, strokeWidth);
								break;
						case 4: aShape = drawRectangle(drawStart.x, drawStart.y,
									drawEnd.x, drawEnd.y);
								innerShape = drawInnerRectangle(drawStart.x, drawStart.y,
										drawEnd.x, drawEnd.y, strokeWidth);
								break;
					}
					
					drawings.add(new Drawing(aShape, innerShape, strokeColor, fillColor, strokeWidth, transparency, false));
					
					drawStart = null;
					drawEnd = null;
					repaint();
				}
			});
			
			this.addMouseMotionListener(new MouseMotionListener() {
				public void mouseMoved(MouseEvent e) {}
				
				public void mouseDragged(MouseEvent e) {
					if (currentAction == 1) {	// Brush
						int x = e.getX();
						int y = e.getY();
						
						System.out.println("X: " + x + "\nY: " + y);
						Shape aShape = new Ellipse2D.Float(x, y, strokeWidth, strokeWidth);
						drawings.add(new Drawing(aShape, aShape, strokeColor, strokeColor, strokeWidth, transparency, true));
					} else if (currentAction == 5) {	// Eraser
						int x = e.getX();
						int y = e.getY();
						
						System.out.println("X: " + x + "\nY: " + y);
						Shape aShape = new Ellipse2D.Float(x, y, strokeWidth, strokeWidth);
						drawings.add(new Drawing(aShape, aShape, Color.WHITE, Color.WHITE, strokeWidth, 1, true));
					}
					
					drawEnd = new Point(e.getX(), e.getY());
					shiftDown = e.isShiftDown();
					repaint();
				}
			});
		}
		
		private Line2D.Float drawLine(int x1, int y1, int x2, int y2) {
			Line2D.Float line = null;
			
			if (shiftDown) {	// Make straight lines
				if ((y2 > (y1 + 50)) || (y2 < (y1 - 50))) {
					line = new Line2D.Float(x1, y1, x1, y2);
				} else {
					line = new Line2D.Float(x1, y1, x2, y1);						
				}				 
			} else {			// Regular lines
				line = new Line2D.Float(x1, y1, x2, y2); 
			}
			
			return line;
		}
		
		private Ellipse2D.Double drawEllipse(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);
			int height = Math.abs(y1 - y2);
			int width = 0;
			
			if (shiftDown) {	// Circles
				if (x2 > x1) {
					width = height;
				} else {
					x = x1 - height;
					width = x1 - x;				
				}
			} else {			// Ellipses
				width = Math.abs(x1 - x2);				
			}
		
			
			Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, width, height);
			return ellipse;
		}
		
		private Ellipse2D.Double drawInnerEllipse(int x1, int y1, int x2, int y2, int stWidth) {
			int x = Math.min(x1, x2) + stWidth / 2 + 1; 
			int y = Math.min(y1, y2) + stWidth / 2 + 1;
			
			int height = Math.abs(y1 - y2) - stWidth;
			int width = 0;
			
			if (shiftDown) {
				if (x2 > x1) {
					width = height;
				} else {
					x = x1 - height - stWidth / 2;
					width = x1 - x - stWidth / 2;
				}
			} else {
				width = Math.abs(x1 - x2) - stWidth;				
			}
			
			
			int dif = strokeWidth / 10 + 1;
			if (depth > 0) {
				x -= dif;
				y -= dif;
				
				width += dif * depth;
				height += dif * depth;
				
			} else if (depth < 0) {
				x -= dif * (-depth);
				y -= dif * (-depth);
				
				width += dif * (-depth) + dif;
				height += dif * (-depth) + dif;
			}
			
			Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, width, height);
			return ellipse;
		}
		
		private Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);
			int height = Math.abs(y1 - y2);
			int width = 0;
			
			if (shiftDown) {		// Square
				if (x2 > x1) {
					width = height;
				} else {
					x = x1 - height;
					width = x1 - x;				
				}
			} else {				// Rectangle
				width = Math.abs(x1 - x2);				
			}
		
			Rectangle2D.Float rectangle = new Rectangle2D.Float(x, y, width, height);
			return rectangle;
		}
		
		private Rectangle2D.Float drawInnerRectangle(int x1, int y1, int x2, int y2, int stWidth) {
			int x = Math.min(x1, x2) + stWidth / 2 + 1; 
			int y = Math.min(y1, y2) + stWidth / 2 + 1;
			
			int height = Math.abs(y1 - y2) - stWidth;
			int width = 0;
			
			if (shiftDown) {
				if (x2 > x1) {
					width = height;
				} else {
					x = x1 - height - stWidth / 2;
					width = x1 - x - stWidth / 2;
				}
			} else {
				width = Math.abs(x1 - x2) - stWidth;				
			}
			
			int dif = strokeWidth / 10 + 1;
			if (depth > 0) {
				x -= dif;
				y -= dif;
				
				width += dif * depth;
				height += dif * depth;
				
			} else if (depth < 0) {
				x -= dif * (-depth);
				y -= dif * (-depth);
				
				width += dif * (-depth) + dif;
				height += dif * (-depth) + dif;
			}
			
			Rectangle2D.Float rectangle = new Rectangle2D.Float(x, y, width, height);
			return rectangle;
		}
		
		private Rectangle2D.Float drawBackground(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);
			int height = Math.abs(y1 - y2);
			int width = Math.abs(x1 - x2);				
		
			Rectangle2D.Float rectangle = new Rectangle2D.Float(x, y, width, height);
			return rectangle;
		}
		
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Para hacer el background blanco
			
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			Shape background = drawBackground(this.getX(), this.getY(), this.getWidth(), this.getHeight());
			g2d.setPaint(Color.WHITE);
			g2d.fill(background);
			
			// Dibuja las formas guardadas en el ArrayList
			for (Drawing drawing : drawings) {
				Shape shape = drawing.getShape();
				Shape innerShape = drawing.getInnerShape();
				Color strokeColor = drawing.getStrokeColor();
				Color fillColor = drawing.getFillColor();
				int strokeWidth = drawing.getStrokeWidth();
				boolean isBrush = drawing.isBrush();
				
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, drawing.getTransparency()));
				
				g2d.setStroke(new BasicStroke(strokeWidth));
				
				if (innerShape != null) {
					g2d.setPaint(fillColor);
					g2d.fill(innerShape);
				}
				
				if (!isBrush) {
					if (shape != null) {
						g2d.setPaint(strokeColor);
						g2d.draw(shape);
					}
				}
			}
			
			if (drawStart != null && drawEnd != null) {
								
				Shape tempShape = null;
				Shape innerShape = null;
				switch (currentAction) {
					case 2: 
						tempShape = drawLine(drawStart.x, drawStart.y,
							drawEnd.x, drawEnd.y);
						break;
					case 3: 
						tempShape = drawEllipse(drawStart.x, drawStart.y,
							drawEnd.x, drawEnd.y);
						innerShape = drawInnerEllipse(drawStart.x, drawStart.y,
							drawEnd.x, drawEnd.y, strokeWidth);
						break;
					case 4:
						tempShape = drawRectangle(drawStart.x, drawStart.y,
								drawEnd.x, drawEnd.y);
						innerShape = drawInnerRectangle(drawStart.x, drawStart.y,
								drawEnd.x, drawEnd.y, strokeWidth);
						break;
				}
				
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
				g2d.setStroke(new BasicStroke(strokeWidth));

				if (innerShape != null) {
					g2d.setPaint(fillColor);
					g2d.fill(innerShape);
				}
				
				if (tempShape != null) {
					g2d.setPaint(strokeColor);
					g2d.draw(tempShape);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new Paint2();
	}
}
