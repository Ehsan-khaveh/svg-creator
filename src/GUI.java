/** Class GUI which is the main class of this program handles drawing the main layout 
 * for the programs as well as performing actions binded to all the buttons and menu items
 * in the main layout. This is where the "Main" method is implemented. 
 *
 * 
 * @version 1.1 Built in April 3, 2012.
 */


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JFileChooser;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class GUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	// declaring constants
	static final int FPS_MIN = 10;
	static final int FPS_MAX = 200;
	static final int FPS_INIT = 100;
	static final int MAJOR_ZOOM_SPACING = 50;
	static final int MINOR_ZOOM_SPACING = 25;
	// Components and variables declaration
	protected JPanel mainPanel, menuPanel, imagePanel, sidebar, sUpper, sBelow,
			zoom, zoomScalePanel;
	protected JMenuBar bar;
	protected JMenu menuFile, menuHelp, menuAbout;
	protected JMenuItem itemOpen, itemSave, itemSaveAs, itemExit, itemNew,
			DocumentProp;
	protected JButton selectAllButt, bringFrontButt, groupButt, ungroupButt,
			fillButt, circButt, rectButt, strokeButt, lineButt, removeButt,
			invisibleButt;
	protected ImageIcon ungroup, selectAll, group, bringFront, fill, circ,
			rect, line, stroke, remove;
	protected GridLayout grLayout;
	protected BorderLayout mainPanelLayout = new BorderLayout(12, 12);
	protected JSlider slider;
	protected JLabel fileName, fileSize, dimensions, zoomPercent;
	protected JTextField location;
	protected JFileChooser fileCh;
	protected Hashtable<Integer, JLabel> labelTable;
	protected JScrollPane jsp;
	protected File file;
	protected GraphicPanel graphicPanel;
	protected ChangeListener sizeAction;

	protected boolean chosen = false, newDoc = false;
	protected int returnVal;
	protected double zoomScale;

	/**
	 * Class GUI constructor to create a new instance of GUI class
	 */
	public GUI() {
		initComponent();
	}

	/**
	 * method initComponent to initialize all the GUI components
	 */

	public void initComponent() {
		// create panels and assign their position
		menuPanel = new JPanel();
		imagePanel = new JPanel();
		sidebar = new JPanel();
		sUpper = new JPanel();
		sBelow = new JPanel();
		zoom = new JPanel();
		slider = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);

		mainPanel = new JPanel(mainPanelLayout);
		mainPanel.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
		add(mainPanel);

		menuPanel.setBorder(BorderFactory.createEtchedBorder());
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.LINE_AXIS));
		menuPanel.setBorder(new EmptyBorder(new Insets(0, 15, 0, 0)));

		// creating image buttons for fill, stroke, circle, rectangle and line
		fill = createImageIcon("Images/fill.png", "Fill");
		fillButt = new JButton("Fill", fill);
		fillButt.setToolTipText("Change the color of a shape");
		fillButt.setActionCommand("Fill");
		fillButt.addActionListener(this);

		stroke = createImageIcon("Images/stroke.png", "Fill");
		strokeButt = new JButton("Stroke", stroke);
		strokeButt.setToolTipText("Change the color of stroke");
		strokeButt.setActionCommand("Stroke");
		strokeButt.addActionListener(this);

		circ = createImageIcon("Images/oval.png", "Oval");
		circButt = new JButton("Oval", circ);
		circButt.setToolTipText("Create an oval shape");
		circButt.setActionCommand("Oval");
		circButt.addActionListener(this);

		rect = createImageIcon("Images/rect.png", "Rectangle");
		rectButt = new JButton("Rectangle", rect);
		rectButt.setToolTipText("Create a rectangular shape");
		rectButt.setActionCommand("Rectangle");
		rectButt.addActionListener(this);

		line = createImageIcon("Images/line.png", "Line");
		lineButt = new JButton("Line", line);
		lineButt.setToolTipText("Create a line");
		lineButt.setActionCommand("Line");
		lineButt.addActionListener(this);

		remove = createImageIcon("Images/remove.png", "Remove");
		removeButt = new JButton("Remove", remove);
		removeButt.setToolTipText("Remove a shape");
		removeButt.setActionCommand("Remove");
		removeButt.addActionListener(this);

		// create and set buttons on side bar Upper panel
		selectAll = createImageIcon("Images/selectAll.png", null);
		selectAllButt = new JButton(selectAll);
		selectAllButt.setToolTipText("Select all the shapes");
		selectAllButt.setActionCommand("SelectAll");
		selectAllButt.addActionListener(this);

		group = createImageIcon("Images/group.png", null);
		groupButt = new JButton(group);
		groupButt.setToolTipText("Group selected shapes");
		groupButt.setActionCommand("Group");
		groupButt.addActionListener(this);

		ungroup = createImageIcon("Images/ungroup.png", null);
		ungroupButt = new JButton(ungroup);
		ungroupButt.setToolTipText("Ungroup the selected group of shapes");
		ungroupButt.setActionCommand("Ungroup");
		ungroupButt.addActionListener(this);

		bringFront = createImageIcon("Images/front.png", null);
		bringFrontButt = new JButton(bringFront);
		bringFrontButt.setToolTipText("Bring the selected shapes to the front");
		bringFrontButt.setActionCommand("bringFront");
		bringFrontButt.addActionListener(this);

		// creating an invisible button and binding a keystroke to it to select
		// all the shapes using Command+A
		invisibleButt = new JButton();
		invisibleButt.setVisible(false);
		invisibleButt.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if (graphicPanel != null)
					graphicPanel.selectAll();
			}
		});
		// Bind a keystroke to the button to act as accelerator.
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_A,
				InputEvent.META_DOWN_MASK);
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks,
				"PRESS");
		mainPanel.getActionMap().put("PRESS", invisibleButt.getAction());

		// adding buttons to the menuePanel
		menuPanel.add(fillButt);
		menuPanel.add(strokeButt);
		menuPanel.add(removeButt);
		menuPanel.add(rectButt);
		menuPanel.add(circButt);
		menuPanel.add(lineButt);
		menuPanel.add(invisibleButt);

		mainPanel.add(menuPanel, BorderLayout.NORTH);

		if (!chosen) {
			imagePanel.setBorder(BorderFactory.createEtchedBorder());
			mainPanel.add(imagePanel, BorderLayout.CENTER);
			circButt.setEnabled(false);
			rectButt.setEnabled(false);
			fillButt.setEnabled(false);
			slider.setEnabled(false);
			selectAllButt.setEnabled(false);
			bringFrontButt.setEnabled(false);
			groupButt.setEnabled(false);
			ungroupButt.setEnabled(false);
			strokeButt.setEnabled(false);
			lineButt.setEnabled(false);
			removeButt.setEnabled(false);
		} else if (!newDoc) {
			drawImage(file);
		}

		sidebar.setBorder(BorderFactory.createEtchedBorder());
		grLayout = new GridLayout(2, 2, 5, 5);
		sUpper.setLayout(grLayout);
		sUpper.setBorder(new EmptyBorder(new Insets(30, 40, 20, 40)));
		sidebar.setLayout(new BorderLayout());
		sidebar.add(sUpper, BorderLayout.NORTH);
		sidebar.add(zoom, BorderLayout.CENTER);
		sidebar.add(sBelow, BorderLayout.SOUTH);

		mainPanel.add(sidebar, BorderLayout.EAST);

		// create and set MenuBar
		bar = new JMenuBar();
		setJMenuBar(bar);

		// create and set menu
		menuFile = new JMenu("File");
		menuHelp = new JMenu("Help");
		menuAbout = new JMenu("About");
		bar.add(menuFile);
		bar.add(menuHelp);
		bar.add(menuAbout);

		// create and set drop down menu
		itemNew = new JMenuItem("New", createImageIcon("Images/new.png", "new"));
		itemNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				KeyEvent.META_DOWN_MASK));

		itemOpen = new JMenuItem("Open", createImageIcon("Images/open.png",
				"open"));
		itemOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				KeyEvent.META_DOWN_MASK));

		itemSave = new JMenuItem("Save", createImageIcon("Images/save.png",
				"save"));
		itemSave.setAccelerator((KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.META_DOWN_MASK)));

		itemSaveAs = new JMenuItem("Save As", createImageIcon(
				"Images/save.png", "save"));
		itemSaveAs.setAccelerator((KeyStroke.getKeyStroke(KeyEvent.VK_S,
				KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_MASK, false)));

		itemExit = new JMenuItem("Exit", createImageIcon("Images/exit.png",
				"exit"));
		itemExit.setAccelerator((KeyStroke.getKeyStroke(KeyEvent.VK_E,
				KeyEvent.META_DOWN_MASK)));

		DocumentProp = new JMenuItem("Document Properties", createImageIcon(
				"Images/prop.png", "Prop"));
		DocumentProp.setAccelerator((KeyStroke.getKeyStroke(KeyEvent.VK_P,
				KeyEvent.META_DOWN_MASK)));

		// Add ActionListener to menu items and buttons
		itemNew.addActionListener(this);
		itemOpen.addActionListener(this);
		itemSave.addActionListener(this);
		itemSaveAs.addActionListener(this);
		itemExit.addActionListener(this);
		DocumentProp.addActionListener(this);

		slider.addChangeListener(sizeAction);

		menuFile.add(itemNew);
		menuFile.add(itemOpen);
		menuFile.add(itemSave);
		menuFile.add(itemSaveAs);
		menuFile.add(DocumentProp);
		menuFile.add(itemExit);

		sUpper.add(selectAllButt);
		sUpper.add(bringFrontButt);
		sUpper.add(groupButt);
		sUpper.add(ungroupButt);

		zoom.setLayout(new BorderLayout());
		zoom.setBorder(new EmptyBorder(new Insets(0, 30, 0, 30)));
		zoom.add(new JLabel("-"), BorderLayout.WEST);
		zoom.add(new JLabel("+"), BorderLayout.EAST);
		// set the label and ticks for slider
		slider.setMajorTickSpacing(MAJOR_ZOOM_SPACING);
		slider.setMinorTickSpacing(MINOR_ZOOM_SPACING);
		labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(FPS_MIN), new JLabel("10%"));
		labelTable.put(new Integer(FPS_MAX), new JLabel("200%"));
		slider.setLabelTable(labelTable);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		zoom.add(slider, BorderLayout.CENTER);
		zoomPercent = new JLabel("");
		zoomScalePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		zoomScalePanel.add(zoomPercent);
		zoom.add(zoomScalePanel, BorderLayout.NORTH);
		sBelow.setLayout(new GridLayout(5, 2, 0, 25));
		sBelow.setBorder(new EmptyBorder(new Insets(20, 20, 30, 20)));
		sBelow.add(new JLabel("Name:"));
		fileName = new JLabel();
		sBelow.add(fileName);
		sBelow.add(new JLabel("Size:"));
		fileSize = new JLabel();
		sBelow.add(fileSize);
		sBelow.add(new JLabel("Dimensions:"));
		dimensions = new JLabel();
		sBelow.add(dimensions);
		sBelow.add(new JLabel("Location:"));
		location = new JTextField("", 6);
		location.setEditable(false);
		sBelow.add(location);

		// defining a change listener for the zoom slider
		sizeAction = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();

				zoomScale = slider.getValue();
				zoomPercent.setText(Integer.toString((int) zoomScale) + "%");
				graphicPanel.zoom(zoomScale);
			}
		};

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (graphicPanel != null) {
					if (graphicPanel.isSaved())
						System.exit(0);
					else {
						int n = confirmSave();
						if (n == 0)
							System.exit(0);
						else if (n == 2) {
							graphicPanel.save(file);
							System.exit(0);
						}

					}
				} else
					System.exit(0);
			}
		});

	}

	/**
	 * method actionPerformed overridden to handle different events caused by
	 * invoked by components
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Open")) {
			if (fileCh == null) {
				fileCh = new JFileChooser();
				// Add a custom file filter
				fileCh.addChoosableFileFilter(new SVGFilter());
			}

			returnVal = fileCh.showOpenDialog(GUI.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if (graphicPanel != null && !graphicPanel.isSaved()) {
					int n = confirmSave();
					if (n == 1)
						return;
					else if (n == 2) {
						if (newDoc) {
							file = graphicPanel.saveAs();
							if (file != null)
								JOptionPane.showMessageDialog(null,
										"Document Saved!");
						} else {
							graphicPanel.save(file);
							JOptionPane.showMessageDialog(null,
									"Document Saved!");
						}
					}
				}
				file = fileCh.getSelectedFile();
				String fileN = file.getName();
				if (fileN.substring(fileN.length() - 4)
						.equalsIgnoreCase(".svg")) {
					chosen = true;
					newDoc = false;
					this.remove(mainPanel);
					initComponent();
					location.setText(file.getAbsolutePath());
					fileName.setText(fileN);
					fileSize.setText(file.length() + " bytes");
					zoomPercent.setText("100%");
				} else {
					JOptionPane.showMessageDialog(null,
							"The file is not in a supported format");
				}
			}
			fileCh.setSelectedFile(null);
		} else if (e.getActionCommand().equals("New")) {
			if (graphicPanel != null && !graphicPanel.isSaved()) {
				int n = confirmSave();
				if (n == 1)
					return;
				else if (n == 2) {
					if (newDoc) {
						file = graphicPanel.saveAs();
						if (file != null)
							JOptionPane.showMessageDialog(null,
									"Document Saved!");
					} else {
						graphicPanel.save(file);
						JOptionPane.showMessageDialog(null, "Document Saved!");
					}
				}
			}

			chosen = true;
			newDoc = true;
			this.remove(mainPanel);
			initComponent();
			fileName.setText("New Document.svg");
			zoomPercent.setText("100%");
			graphicPanel = new GraphicPanel();
			JScrollPane jsp = new JScrollPane(graphicPanel);
			mainPanel.add(jsp, BorderLayout.CENTER);
		} else if (e.getActionCommand().equals("Save")) {
			if (chosen && !graphicPanel.isSaved()) {
				if (newDoc) {
					file = graphicPanel.saveAs();
					if (file != null) {
						newDoc = false;
						JOptionPane.showMessageDialog(null, "Document Saved!");
					}

				} else {
					graphicPanel.save(file);
					JOptionPane.showMessageDialog(null, "Document Saved!");
				}
			}
		} else if (e.getActionCommand().equals("Save As")) {
			file = graphicPanel.saveAs();
			if (file != null) {
				newDoc = false;
				JOptionPane.showMessageDialog(null, "Document Saved!");
			}

		} else if (e.getActionCommand().equals("Document Properties")) {
			if (graphicPanel != null)
				graphicPanel.resizePanel();
			else
				JOptionPane.showMessageDialog(null,
						"You should open a document first");
		}

		else if (e.getActionCommand().equals("Exit")) {
			if (graphicPanel != null) {
				if (graphicPanel.isSaved())
					System.exit(0);
				else {
					int n = confirmSave();
					if (n == 0)
						System.exit(0);
					else if (n == 2) {
						graphicPanel.save(file);
						System.exit(0);
					}

				}
			} else
				System.exit(0);
		}

		else if (e.getActionCommand().equals("Fill"))
			graphicPanel.changeColor();

		else if (e.getActionCommand().equals("Stroke"))
			graphicPanel.changeStroke();

		else if (e.getActionCommand().equals("Line")
				|| e.getActionCommand().equals("Rectangle")
				|| e.getActionCommand().equals("Oval"))
			graphicPanel.createShape(e.getActionCommand());

		else if (e.getActionCommand().equals("Remove"))
			graphicPanel.removeShape();

		else if (e.getActionCommand().equals("SelectAll"))
			graphicPanel.selectAll();
		else if (e.getActionCommand().equals("bringFront"))
			graphicPanel.bringFront();
		else if (e.getActionCommand().equals("Group")) {
			graphicPanel.group();
		} else if (e.getActionCommand().equals("Ungroup"))
			graphicPanel.ungroup();

	}

	/**
	 * method confirm save to ask the user whether he or she wants to save the
	 * changes before closing the document
	 */
	public int confirmSave() {
		Object[] options = { "Don't Save", "Cancel", "Save" };
		int n = JOptionPane.showOptionDialog(GUI.this,
				"Do you want to save the changes u made to this document?", "",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, options, options[2]);

		return n;

	}

	/**
	 * method drawImage to process the file and pass its elements (svg tags) to
	 * the new instance of graphicalPanel
	 */
	public boolean drawImage(File file) {
		NodeList elements;

		elements = processFile(file);
		if (elements != null) {
			graphicPanel = new GraphicPanel(elements);
			JScrollPane jsp = new JScrollPane(graphicPanel);
			mainPanel.add(jsp, BorderLayout.CENTER);
			fileSize.setText(Integer.toString((int) graphicPanel
					.getPanelWidth())
					+ " X "
					+ Integer.toString((int) graphicPanel.getPanelHeight()));
			return true;
		}
		return false;
	}

	/**
	 * this method process the chosen file by the user to be opened
	 */
	public NodeList processFile(File file) {
		NodeList elements = null;

		// Create a factory
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		// Use the factory to create a builder
		DocumentBuilder builder;
		try {
			fac.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
			builder = fac.newDocumentBuilder();
			Document doc = builder.parse(file.getAbsolutePath());
			elements = doc.getElementsByTagName("*");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"The file is not in a supported format");
		}

		return elements;
	}

	/**
	 * method createImageIcon to create icons using the path given to the icon
	 * and a description for it
	 */
	protected ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * implementation of method "Main"
	 */
	public static void main(String[] args) {
		GUI gui = new GUI();
		gui.setMinimumSize(new Dimension(800, 720));
		gui.setTitle("The Awesome Photo Editor");
		gui.setSize(900, 710);
		gui.setVisible(true);
		gui.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

}
