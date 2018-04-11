/*
 * 	Differences with Derek Banas' Paint:
 * 	
 *     Shift key to make squares, circles and horizontal and vertical lines
 *     Stroke size
 *     Eraser
 *     Depth, for a 'tridimensional' feel
 *     Clear button
 *     Undo and redo
 *     File opening and saving with Java Paint extension
 *     Exporting and importing BMP, JPG, PNG and GIF files
 *     Menus and shortcuts for all non painting operations
 *     Background colors on the stroke color and fill color buttons
 */

package paint;

import javax.imageio.ImageIO;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Paint implements Serializable {

	private static final long serialVersionUID = -812840923682573740L;

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
	private ArrayList<Drawing> undoList = new ArrayList<Drawing>();
	private File savedFile;
	private boolean differentFromSaved = false;
	private DrawingPanel drawingPanel;
	private BufferedImage image;
	
	private Paint() {
		window = new JFrame("Java Paint");
		window.setSize(1200, 800);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		drawingPanel = new DrawingPanel();
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
				Shape bg = new DrawingPanel().drawBackground(drawingPanel.getX(), drawingPanel.getY(), drawingPanel.getWidth(), drawingPanel.getHeight());
				Drawing background = new Drawing(bg, bg, Color.WHITE, Color.WHITE, 1, 1.0f, false);
				drawings.add(background);
				differentFromSaved = true;
				drawingPanel.repaint();
			}
		});
		clearBut.setMnemonic(KeyEvent.VK_C);
		
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
		file.setMnemonic(KeyEvent.VK_F);
		JMenu edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		JMenu about = new JMenu("About");
		about.setMnemonic(KeyEvent.VK_A);

		JMenuItem newMenu = new JMenuItem("New...");
		JMenuItem open = new JMenuItem("Open...");
		JMenuItem saveMenu = new JMenuItem("Save");
		JMenuItem saveAs = new JMenuItem("Save As...");
		JMenuItem importMenu = new JMenuItem("Import...");
		JMenuItem export = new JMenuItem("Export...");
		JMenuItem exit = new JMenuItem("Exit");
		
		newMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newFile();
			}
		});
		newMenu.setMnemonic(KeyEvent.VK_N);
		KeyStroke ctrlN = KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		newMenu.setAccelerator(ctrlN);
		
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (differentFromSaved) {
					sure("Do you wanto to save your work before opening a new file?");
				}
				
				open();
			}
		});
		open.setMnemonic(KeyEvent.VK_O);
		KeyStroke ctrlO = KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		open.setAccelerator(ctrlO);
		
		saveMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (savedFile != null) {
					saveFile(savedFile);
				} else {
					saveAs();
				}
			}
		});
		saveMenu.setMnemonic(KeyEvent.VK_S);
		KeyStroke ctrlS = KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		saveMenu.setAccelerator(ctrlS);
		
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAs();
			}
		});
		saveAs.setMnemonic(KeyEvent.VK_A);
		
		importMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importM();
			}
		});
		importMenu.setMnemonic(KeyEvent.VK_I);
		KeyStroke ctrlI = KeyStroke.getKeyStroke('I', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		importMenu.setAccelerator(ctrlI);
		
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		export.setMnemonic(KeyEvent.VK_E);
		KeyStroke ctrlE = KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		export.setAccelerator(ctrlE);
		
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		exit.setMnemonic(KeyEvent.VK_X);
		KeyStroke ctrlX = KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		exit.setAccelerator(ctrlX);
		
		JMenuItem undo = new JMenuItem("Undo");
		JMenuItem redo = new JMenuItem("Redo");
		
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					undo();
			}
		});
		
		KeyStroke ctrlZ = KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		undo.setAccelerator(ctrlZ);
		undo.setMnemonic(KeyEvent.VK_U);
		
		KeyStroke ctrlshZ = KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+ActionEvent.SHIFT_MASK);
		redo.setAccelerator(ctrlshZ);
		redo.setMnemonic(KeyEvent.VK_R);
		
		redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					redo();
			}
		});
		
		file.add(newMenu);
		file.add(open);
		file.addSeparator();
		file.add(saveMenu);
		file.add(saveAs);
		file.addSeparator();
		file.add(importMenu);
		file.add(export);
		file.addSeparator();
		file.add(exit);
		
		edit.add(undo);
		edit.add(redo);
		
		mb.add(file);
		mb.add(edit);
		mb.add(about);
		return mb;
	}

	private void newFile() {
		if (differentFromSaved) {
			sure("Do you want to save your work before creating a new file?");
		}
		
		savedFile = null;
		image = null;
		differentFromSaved = false;
		drawings.clear();
		undoList.clear();
		window.repaint();
	}
	
	private void importM() {
		JFileChooser fc = new JFileChooser(".");// Luego sacar el "." para usar la home
		ExtensionFileFilter filterJpg = new ExtensionFileFilter("JPG Files (*.jpg)", "jpg");
		ExtensionFileFilter filterPng = new ExtensionFileFilter("PNG Files (*.png)", "png");
		ExtensionFileFilter filterGif = new ExtensionFileFilter("GIF Files (*.gif)", "gif");
		ExtensionFileFilter filterBmp = new ExtensionFileFilter("BMP Files (*.bmp)", "bmp");

		fc.setFileFilter(filterJpg);
		fc.addChoosableFileFilter(filterJpg);
		fc.addChoosableFileFilter(filterPng);
		fc.addChoosableFileFilter(filterGif);
		fc.addChoosableFileFilter(filterBmp);
		fc.setDialogTitle("Import");
		
		int status = fc.showOpenDialog(window);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			if (!drawings.isEmpty() || image != null) {		
				
				@SuppressWarnings("serial")
				class CombineDialog extends JDialog implements ActionListener {
					int selectedAction;
					
					public CombineDialog() {
						super(window, "Combine or New File", true);
						JLabel message = new JLabel("Should we combine the picture with your present drawing?");
						message.setHorizontalAlignment(SwingConstants.CENTER);
						
						JPanel buttons = new JPanel();
						JButton combine = new JButton("Combine");
						combine.addActionListener(this);
						JButton newFile = new JButton("New File");
						newFile.addActionListener(this);
						
						buttons.add(combine);
						buttons.add(newFile);
						
						add(message, BorderLayout.CENTER);
						add(buttons, BorderLayout.SOUTH);
						
						setResizable(false);
						pack();
						setSize(getWidth() + 20, getHeight());
						setLocationRelativeTo(null);
						setVisible(true);
					}
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals("Combine")) {
							selectedAction = 0;
						} else if (e.getActionCommand().equals("New File")) {
							selectedAction = 1;
						}
						
						setVisible(false);
					}
				}
				
				CombineDialog cd = new CombineDialog();
				int action = cd.selectedAction;
				
				if (action == 1) newFile();
			}
			
			importImage(fc.getSelectedFile());
			differentFromSaved = true;
		}
	}
	
	private void importImage(File file) {
		try {
			image = ImageIO.read(file);
			differentFromSaved = true;
			drawingPanel.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void export() {
		JFileChooser fc = new JFileChooser(".");// Luego sacar el "." para usar la home
		ExtensionFileFilter filterJpg = new ExtensionFileFilter("JPG Files (*.jpg)", "jpg");
		ExtensionFileFilter filterPng = new ExtensionFileFilter("PNG Files (*.png)", "png");
		ExtensionFileFilter filterGif = new ExtensionFileFilter("GIF Files (*.gif)", "gif");
		ExtensionFileFilter filterBmp = new ExtensionFileFilter("BMP Files (*.bmp)", "bmp");
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filterJpg);
		fc.addChoosableFileFilter(filterJpg);
		fc.addChoosableFileFilter(filterPng);
		fc.addChoosableFileFilter(filterGif);
		fc.addChoosableFileFilter(filterBmp);
		fc.setDialogTitle("Export");

		int status = fc.showSaveDialog(window);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			if (fc.getSelectedFile().exists()) {
				int overwrite = JOptionPane.showConfirmDialog(window, "The file already exists. Do you want to overwrite it?");
				switch (overwrite) {
					case 0:
						break;
					case 1:
						export();
						return;
					case 2:
						return;
				}
			}
			
			String file = fc.getSelectedFile().toString().toLowerCase();
			
			if (file.endsWith(".jpg") || file.endsWith(".png") || file.endsWith(".gif") || file.endsWith(".bmp")) {
				String extension = file.substring(file.length() - 3);
				exportToFile(fc.getSelectedFile(), extension);
			} else {
				String sFile = fc.getSelectedFile().toString();
				
				String extension = "";
				if (fc.getFileFilter().getDescription().contains("jpg")) extension = "jpg"; 
				if (fc.getFileFilter().getDescription().contains("png")) extension = "png";
				if (fc.getFileFilter().getDescription().contains("gif")) extension = "gif";
				if (fc.getFileFilter().getDescription().contains("bmp")) extension = "bmp";
				sFile = sFile + "." + extension;
				exportToFile(new File(sFile), extension);
			}
		}	
	}
	
	private void exportToFile(File file, String extension) {
		BufferedImage bi = new BufferedImage(drawingPanel.getSize().width, drawingPanel.getSize().height, BufferedImage.TYPE_INT_RGB); 
		Graphics g = bi.createGraphics();
		drawingPanel.paint(g);
		g.dispose();
		try{
			ImageIO.write(bi, extension,file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sure(String title) {
		String message = "There have been changes since the last time you saved your painting.";
		int option = JOptionPane.showConfirmDialog(window, message, title, JOptionPane.YES_NO_OPTION);
		if (option == 0) {
			if (savedFile == null) {
				saveAs();
			} else {
				saveFile(savedFile);
			}
		}
	}
	
	private void open() {
		JFileChooser fc = new JFileChooser(".");	// Luego sacar el "." para usar la home
		FileFilter filter1 = new ExtensionFileFilter("Java Paint Files (*.jpt)", "jpt");
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filter1);
		fc.addChoosableFileFilter(filter1);
		
		int status = fc.showOpenDialog(window);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			savedFile = fc.getSelectedFile();
			openFile(savedFile);
		}	
	}
	
	private void exit() {
		if (differentFromSaved) {
			String title = "Do you want to save your work before exiting?";
			String message = "There have been changes since the last time you saved your painting.";
			int sure = JOptionPane.showConfirmDialog(window, message, title, JOptionPane.YES_NO_OPTION);
			
			if (sure == 1) {
				System.exit(0);
			} else {
				if (savedFile != null) {
					saveFile(savedFile);
				} else {
					saveAs();
					System.exit(0);
				}
			}
		} else {
			System.exit(0);
		}
	}
	
	private void saveAs() {
		JFileChooser fc = new JFileChooser(".");// Luego sacar el "." para usar la home
		FileFilter filter1 = new ExtensionFileFilter("Java Paint Files (*.jpt)", "jpt");
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(filter1);
		fc.addChoosableFileFilter(filter1);

		int status = fc.showSaveDialog(window);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			if (fc.getSelectedFile().exists()) {
				int overwrite = JOptionPane.showConfirmDialog(window, "The file already exists. Do you want to overwrite it?");
				switch (overwrite) {
					case 0:
						break;
					case 1:
						saveAs();
						return;
					case 2:
						return;
				}
			}
			
			String file = fc.getSelectedFile().toString().toLowerCase();
			
			if (file.endsWith(".jpt")) {
				savedFile = fc.getSelectedFile();
			} else {
				String sFile = fc.getSelectedFile().toString();
				sFile = sFile + ".jpt";
				savedFile = new File(sFile);
			}
			
			saveFile(savedFile);
		}
	}
	
	private void saveFile(File file) {
		try {
			ObjectOutputStream f = new ObjectOutputStream(new FileOutputStream(file));
			f.writeObject(drawings);
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		differentFromSaved = false;
	}
	
	@SuppressWarnings("unchecked")
	private void openFile(File file) {
		try {
			ObjectInputStream f = new ObjectInputStream(new FileInputStream(file));
			
			drawings.clear();
			drawings = (ArrayList<Drawing>) f.readObject();
			f.close();
			
			differentFromSaved = false;
			
			window.repaint();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	private void undo() {
		if (drawings.size() != 0) {
			undoList.add(drawings.get(drawings.size() - 1));
			drawings.remove(drawings.size() - 1);
			window.repaint();
		}
	}
	
	private void redo() {
		if (undoList.size() != 0) {
			drawings.add(undoList.get(undoList.size() - 1));
			undoList.remove(undoList.size() - 1);
			window.repaint();
		}
	}
	
	class ExtensionFileFilter extends FileFilter {
		String extension;
		String description;
		
		public ExtensionFileFilter(String extension) {
			this.extension = extension;
		}
		
		public ExtensionFileFilter(String description, String extension) {
			this.description = description;
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
			return description;
		}
		
		public String getExtension() {
			return extension;
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
					
					differentFromSaved = true;
					
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
						
						Shape aShape = new Ellipse2D.Float(x, y, strokeWidth, strokeWidth);
						drawings.add(new Drawing(aShape, aShape, strokeColor, strokeColor, strokeWidth, transparency, true));
						
						differentFromSaved = true;
					} else if (currentAction == 5) {	// Eraser
						int x = e.getX();
						int y = e.getY();
						
						Shape aShape = new Ellipse2D.Float(x, y, strokeWidth, strokeWidth);
						drawings.add(new Drawing(aShape, aShape, Color.WHITE, Color.WHITE, strokeWidth, 1, true));
						
						differentFromSaved = true;
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
				if ((y2 > (y1 + 4)) || (y2 < (y1 - 4))) {
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
		
		Rectangle2D.Float drawBackground(int x1, int y1, int x2, int y2) {
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

			if (image != null) {
				g2d.drawImage(image, null, 0, 0);
			}
			
			
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
		new Paint();
	}
}
