/** Class SVGPanel is the heart of this program. This is where all the 
 * functional features such as creating shapes, resizing shapes, moving shapes
 * and so on are implemented. It contains two inner that extends MouseAdapter
 * and MouseMotionAdapter to handle events occurring using mouse.
 *
 * 
 * @version 1.1 Built in April 3, 2012.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class SVGPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	protected final double handleSize = 8.0, normZoomScale = 1;
	protected final int defaultWidth = 1;
	protected final Dimension defaultPortView = new Dimension(500, 500);
	protected static final int numOfColors = 148;
	protected int maxX, maxY, currentX, currentY, shapeCount, handleIndex,
			groupIndex = 0;
	protected double x, y, x2, y2, rectWidth, rectHeight, circRadius,
			strokeWidth = defaultWidth, zoomScale, xClicked, yClicked,
			updatedX, updatedY, xReleased, yReleased, updatedWidth,
			updatedHeight;
	protected boolean wSpecified = false, hSpecified = false,
			shapeRemoved = false, drawingShape = false, dragging = false,
			resizing = false, isSaved = true;
	protected String shapeType, lineEnd;

	protected Color strokeColor, fill, initWidth, initHeight, selectColor;
	protected BasicStroke stroke;
	protected Element element;
	protected NodeList elements;
	protected NamedNodeMap atrbs;
	protected Iterator<Shape> iterator;
	protected Graphics2D g2;
	protected Shape currentShape, cursorOverShape, tempShape, newShape,
			selShape;
	protected Rectangle2D east, south, southeast;
	protected ArrayList<Rectangle2D> handles = new ArrayList<Rectangle2D>();
	protected Cursor curCursor;
	protected ArrayList<Shape> allShapes = new ArrayList<Shape>();
	protected ArrayList<Shape> intersectedShapes = new ArrayList<Shape>();
	protected ArrayList<Shape> selectedShapes = new ArrayList<Shape>();
	protected Dimension currentCoordinate;
	protected JFrame strokeProp;
	protected JLabel enterStrWidth;
	protected JPanel strokePanel;
	protected JTextField strWidth;
	protected JButton alterStrokeButt, alterColorButt;
	protected JRectangle select = null, srndRect = null;;
	protected ArrayList<Shape[]> group = new ArrayList<Shape[]>();
	protected JFileChooser fileChooser;

	/**
	 * Constructor for SVGPanel class to create new instances of this class and
	 * initialize some variables
	 */
	public SVGPanel(NodeList elements, String width, String height) {
		this.elements = elements;
		initDimen(unitConversion(width), unitConversion(height));
		createShapes(elements);
		setBackground(Color.white);
		addMouseListener(new MyMouseListener());
		addMouseMotionListener(new MyMouseMotionListener());
		zoomScale = 1.0;
	}

	/**
	 * Constructor for SVGPanel class to create new instances of this class and
	 * initialize some variables
	 */
	public SVGPanel(int width, int height) {
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.white);
		addMouseListener(new MyMouseListener());
		addMouseMotionListener(new MyMouseMotionListener());
		zoomScale = 1.0;
	}

	/**
	 * method to initialize the dimensions of the svg document
	 */
	public void initDimen(double width, double height) {
		if (height == -1 && width == -1) {
			setPreferredSize(defaultPortView);
			wSpecified = false;
			hSpecified = false;
		} else if (height == -1) {
			setPreferredSize(new Dimension((int) width, defaultPortView.height));
			wSpecified = true;
			hSpecified = false;
		} else if (width == -1) {
			setPreferredSize(new Dimension(defaultPortView.width, (int) height));
			wSpecified = false;
			hSpecified = true;
		} else {
			setPreferredSize(new Dimension((int) width, (int) height));
			wSpecified = true;
			hSpecified = true;
		}
		// initialize zoom scale
		zoomScale = 1.0;
	}

	/**
	 * mutator to set the zoom scale
	 */
	public void setZoomScale(double scale) {
		zoomScale = scale / 100;
	}

	/**
	 * Accessor to get the zoomScale
	 */
	public double getZoomScale() {
		return zoomScale;
	}

	/**
	 * method to create instances of different types of shape using the elements
	 * passed to this class
	 */
	public void createShapes(NodeList elements) {
		try {
			for (int i = 0; i < elements.getLength(); i++) {
				// fetch an element

				element = (Element) elements.item(i);
				atrbs = element.getAttributes();

				// check if it is a rect element
				if (element.getNodeName().equalsIgnoreCase("rect")) {
					// get the attributes and their values
					if (atrbs.getNamedItem("x") != null)
						x = unitConversion(atrbs.getNamedItem("x")
								.getNodeValue());
					else
						x = 0;

					if (atrbs.getNamedItem("y") != null)
						y = unitConversion(atrbs.getNamedItem("y")
								.getNodeValue());
					else
						y = 0;

					// if rect element has no width or height then the element
					// will be ignored
					if (atrbs.getNamedItem("width") == null
							|| atrbs.getNamedItem("height") == null)
						continue;
					else {
						rectWidth = unitConversion(atrbs.getNamedItem("width")
								.getNodeValue());

						rectHeight = unitConversion(atrbs
								.getNamedItem("height").getNodeValue());
					}
					// get the color of the shape
					if (atrbs.getNamedItem("fill") != null) {
						if (atrbs.getNamedItem("fill").getNodeValue()
								.equalsIgnoreCase("none"))
							fill = null;
						else
							fill = getColor(atrbs.getNamedItem("fill")
									.getNodeValue());
					} else
						fill = null;

					// check to see if the shape has stroke
					if (atrbs.getNamedItem("stroke") != null) {
						strokeColor = getColor(atrbs.getNamedItem("stroke")
								.getNodeValue());
						// check if the element has a stroke
						if (strokeColor != null) {
							// get the width of the stroke
							if (atrbs.getNamedItem("stroke-width") != null) {
								if (atrbs.getNamedItem("stroke-width")
										.getNodeValue() != null)
									strokeWidth = unitConversion(atrbs
											.getNamedItem("stroke-width")
											.getNodeValue());
								// set to default if there is no stroke-width
								// specified
								else
									strokeWidth = defaultWidth;
							} else
								strokeWidth = defaultWidth;

							stroke = new BasicStroke((float) strokeWidth);
						}
						// set stroke to null if there is no stroke attribute
						else
							stroke = null;
					} else {
						stroke = null;
					}
					// creating the shape object
					allShapes.add(new JRectangle(shapeCount + 1, x, y,
							rectWidth, rectHeight, fill, stroke, strokeColor));
					shapeCount++;
				}
				// check if it is a circle element if not rect
				else if (element.getNodeName().equalsIgnoreCase("circle")) {
					if (atrbs.getNamedItem("r") != null)
						circRadius = unitConversion(atrbs.getNamedItem("r")
								.getNodeValue());
					else
						continue;

					if (atrbs.getNamedItem("cx") != null)
						x = unitConversion(atrbs.getNamedItem("cx")
								.getNodeValue()) - circRadius;
					else
						x = (-1) * circRadius;

					if (atrbs.getNamedItem("cy") != null)
						y = unitConversion(atrbs.getNamedItem("cy")
								.getNodeValue()) - circRadius;
					else
						y = (-1) * circRadius;

					// get the color of the shape
					if (atrbs.getNamedItem("fill") != null) {
						if (atrbs.getNamedItem("fill").getNodeValue()
								.equalsIgnoreCase("none"))
							fill = null;
						else
							fill = getColor(atrbs.getNamedItem("fill")
									.getNodeValue());
					} else
						fill = null;

					// check if the shape has stroke attribute
					if (atrbs.getNamedItem("stroke") != null) {
						strokeColor = getColor(atrbs.getNamedItem("stroke")
								.getNodeValue());
						// check if the element has a stroke
						if (strokeColor != null) {
							// get the width of the stroke
							if (atrbs.getNamedItem("stroke-width") != null) {
								if (atrbs.getNamedItem("stroke-width")
										.getNodeValue() != null)
									strokeWidth = unitConversion(atrbs
											.getNamedItem("stroke-width")
											.getNodeValue());
								// set to default if there is no stroke-width
								// specified
								else
									strokeWidth = defaultWidth;
							} else
								strokeWidth = defaultWidth;

							stroke = new BasicStroke((float) strokeWidth);
						}
						// set stroke to null if there is no stroke attribute
						else
							stroke = null;
					} else
						stroke = null;
					// creating the shape object
					allShapes.add(new JCircle(shapeCount + 1, x, y,
							circRadius * 2, fill, stroke, strokeColor));
					shapeCount++;
				}
				// check if it is a line element if not circle and rect
				else if (element.getNodeName().equalsIgnoreCase("line")) {
					if (atrbs.getNamedItem("stroke") != null) {
						if (atrbs.getNamedItem("x1") != null)
							x = unitConversion(atrbs.getNamedItem("x1")
									.getNodeValue());
						else
							x = 0;

						if (atrbs.getNamedItem("y1") != null)
							y = unitConversion(atrbs.getNamedItem("y1")
									.getNodeValue());
						else
							y = 0;

						if (atrbs.getNamedItem("x2") != null)
							x2 = unitConversion(atrbs.getNamedItem("x2")
									.getNodeValue());
						else
							x2 = 0;

						if (atrbs.getNamedItem("y2") != null)
							y2 = unitConversion(atrbs.getNamedItem("y2")
									.getNodeValue());
						else
							y2 = 0;

						strokeColor = getColor(atrbs.getNamedItem("stroke")
								.getNodeValue());
						if (strokeColor == null)
							continue;

						// Get stroke width
						if (atrbs.getNamedItem("stroke-width") != null)
							strokeWidth = unitConversion(atrbs.getNamedItem(
									"stroke-width").getNodeValue());
						// set to default if there is no width defined for
						// stroke
						else
							strokeWidth = defaultWidth;

						stroke = new BasicStroke((float) strokeWidth);
						allShapes.add(new JLine(shapeCount + 1, x, y, x2, y2,
								null, stroke, strokeColor));
						shapeCount++;
					}
				}

			}

		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * overriding paint method to draw all the created shapes to on the frame
	 */
	public void paint(Graphics g) {
		super.paint(g);
		g2 = (Graphics2D) g;

		// initializing the view port
		maxX = defaultPortView.width;
		maxY = defaultPortView.height;

		iterator = allShapes.iterator();
		// drawing rectangle objects
		while (iterator.hasNext()) {
			currentShape = iterator.next();
			currentCoordinate = currentShape.drawShape(g2, zoomScale);

			currentX = (int) currentCoordinate.getWidth();
			currentY = (int) currentCoordinate.getHeight();

			updateViewPort(currentX, currentY);
		}
		g2.setStroke(new BasicStroke());
		if (select != null)
			select.drawShape(g2, zoomScale);

		if (srndRect != null)
			srndRect.drawShape(g2, zoomScale);

		if (selectedShapes.size() != 0 && !dragging) {
			for (int i = 0; i < selectedShapes.size(); i++)
				handles = selectedShapes.get(i).drawHandles(g2, zoomScale);
		}

		if (curCursor != null)
			setCursor(curCursor);
	}
	/**
	 * method to update the view port after each shape is painted on the frame
	 */
	public void updateViewPort(int currentX, int currentY) {
		// after drawing the shape, change the viewport to the based on the new
		// shape's coordinates
		if ((currentX > maxX && !wSpecified)
				&& (currentY > maxY && !hSpecified)) {
			maxX = currentX;
			maxY = currentY;
		}

		else if (currentX > maxX && !wSpecified) {
			maxX = currentX;
			if (maxY < defaultPortView.height)
				maxY = defaultPortView.height;
		} else if (currentY > maxY && !hSpecified) {
			maxY = currentY;
			if (maxX < defaultPortView.width)
				maxX = defaultPortView.width;
		} else
			return;

		setPreferredSize(new Dimension(maxX, maxY));
	}
	/**
	 * method get color to get the return the equivalent color attribute
	 */
	public static Color getColor(String colorName) throws IOException {
		FileInputStream colorsFile, rgbFile;
		DataInputStream inColor, inRgb;
		BufferedReader brColor, brRgb;
		String strLine, strLine2, colorFileName = "src/files/colornames", rgbFileName = "src/files/rgbs";
		int j = 0;
		StringTokenizer tokens, rgb;
		String r, g, b;
		String rgbPattern = "rgb\\(\\s*[0-9]{1,3}\\s*,\\s*[0-9]{1,3}\\s*,\\s*[0-9]{1,3}\\s*\\)";
		String rgbPercent = "rgb\\(\\s*[0-9]{1,3}%\\s*,\\s*[0-9]{1,3}%\\s*,\\s*[0-9]{1,3}%\\s*\\)";
		String rgbHexa = "#{0,1}[0-9a-fA-F]{6}";

		final String colorNames[] = new String[numOfColors];

		final Color[] colors = new Color[numOfColors];

		colorName = colorName.trim();
		// reading from color file and create colors
		colorsFile = new FileInputStream(colorFileName);
		inColor = new DataInputStream(colorsFile);
		brColor = new BufferedReader(new InputStreamReader(inColor));
		while ((strLine = brColor.readLine()) != null) {
			colorNames[j] = strLine;
			j++;
		}
		j = 0;

		// reading from rgb file and create colors
		rgbFile = new FileInputStream(rgbFileName);
		inRgb = new DataInputStream(rgbFile);
		brRgb = new BufferedReader(new InputStreamReader(inRgb));

		while ((strLine2 = brRgb.readLine()) != null) {
			tokens = new StringTokenizer(strLine2);
			colors[j] = new Color(Integer.parseInt(tokens.nextToken()),
					Integer.parseInt(tokens.nextToken()),
					Integer.parseInt(tokens.nextToken()));
			j++;
		}

		for (int i = 0; i < colorNames.length; i++) {
			if (colorName.equalsIgnoreCase(colorNames[i]))
				return colors[i];
		}

		if (colorName.matches(rgbPattern)) {
			colorName = colorName.substring(4, colorName.length() - 1);
			rgb = new StringTokenizer(colorName, ",");
			return new Color(Integer.parseInt(rgb.nextToken().trim()),
					Integer.parseInt(rgb.nextToken().trim()),
					Integer.parseInt(rgb.nextToken().trim()));
		} else if (colorName.matches(rgbPercent)) {
			float red, green, blue;
			colorName = colorName.substring(4, colorName.length() - 1);
			rgb = new StringTokenizer(colorName, ",");
			r = rgb.nextToken().trim();
			g = rgb.nextToken().trim();
			b = rgb.nextToken().trim();
			red = Float.parseFloat(r.substring(0, (r.length() - 1)));
			green = Float.parseFloat(g.substring(0, (g.length() - 1)));
			blue = Float.parseFloat(b.substring(0, (b.length() - 1)));
			return new Color(red / 100, green / 100, blue / 100);
		} else if (colorName.matches(rgbHexa)) {
			if (colorName.charAt(0) == '#')
				colorName = colorName.substring(1);
			r = colorName.substring(0, 2);
			g = colorName.substring(2, 4);
			b = colorName.substring(4, 6);
			return new Color(Integer.parseInt(r, 16), Integer.parseInt(g, 16),
					Integer.parseInt(b, 16));
		}

		return null;
	}

	/**
	 * method unitConversion to convert all units to pixel for easier manipulation
	 */
	public static double unitConversion(String unit) {
		String unitPattern = "[0-9]{1,}(cm|pt|pc|mm|in|px){1}";

		if (unit.matches(unitPattern)) {
			float measure = Float.parseFloat(unit.substring(0,
					unit.length() - 2));
			if (unit.substring(unit.length() - 2).equals("cm"))
				return measure * 35.43307;

			else if (unit.substring(unit.length() - 2).equals("pt"))
				return measure * 1.25;

			else if (unit.substring(unit.length() - 2).equalsIgnoreCase("pc"))
				return measure * 15;

			else if (unit.substring(unit.length() - 2).equalsIgnoreCase("mm"))
				return measure * 3.543307;

			else if (unit.substring(unit.length() - 2).equalsIgnoreCase("in"))
				return measure * 90;

			else
				return Float.parseFloat(unit);
		} else {
			try {
				return Float.parseFloat(unit);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
	}
	/**
	 * inner class MyMouseListener and MyMotionMouseListener to handle events occurred by mouse
	 */
	class MyMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			xClicked = e.getX();
			yClicked = e.getY();
			selShape = selectShape(xClicked, yClicked, zoomScale);
			if (selShape != null && !drawingShape) {
				if (selectedShapes.size() <= 1 && !selShape.getGroupStat()) {

					selectedShapes.clear();
					selectedShapes.add(selShape);
					if (selShape instanceof JLine) {
						if (((JLine) selShape).getEndPoint(zoomScale).contains(
								xClicked, yClicked))
							lineEnd = "endPoint";
						else
							lineEnd = "startPoint";
					}
					srndRect = null;
				} else if (selShape.getGroupStat()) {
					groupIndex = getGroupIndex(selShape);
					selectedShapes.clear();
					for (int k = 0; k < group.get(groupIndex).length; k++)
						selectedShapes.add(group.get(groupIndex)[k]);

					srndRect = getSrndingRect(selectedShapes);
				} else {
					boolean thisShapeSelected = false;
					for (int i = 0; i < selectedShapes.size(); i++)
						if (selectedShapes.get(i).getID() == selShape.getID())
							thisShapeSelected = true;
					if (!thisShapeSelected) {
						selectedShapes.clear();
						selectedShapes.add(selShape);
					}
					srndRect = null;
				}
				drawingShape = false;
				dragging = false;
			} else if ((handleIndex = selectHandle(xClicked, yClicked)) != -1) {
				srndRect = null;
				resizing = true;
			} else if (drawingShape) {
				if (shapeType.equals("Rectangle"))
					newShape = new JRectangle(shapeCount + 1,
							(xClicked / zoomScale), (yClicked / zoomScale), 0,
							0, Color.gray, null, null);

				else if (shapeType.equals("Oval"))
					newShape = new JCircle(shapeCount + 1,
							(xClicked / zoomScale), (yClicked / zoomScale), 0,
							Color.gray, null, null);

				else if (shapeType.equals("Line"))
					newShape = new JLine(shapeCount + 1,
							(xClicked / zoomScale), (yClicked / zoomScale),
							(xClicked / zoomScale), (yClicked / zoomScale),
							Color.gray, new BasicStroke(8), Color.gray);

				allShapes.add(newShape);
				shapeCount++;
				srndRect = null;
				repaint();
			} else {
				selectColor = new Color(0, 0, 255, 128);
				select = new JRectangle(-1, (xClicked / zoomScale),
						(yClicked / zoomScale), 0, 0, selectColor, null, null);

				selectedShapes.clear();
				if (!drawingShape)
					curCursor = Cursor.getDefaultCursor();

				srndRect = null;
			}

			repaint();
		}

		public void mouseReleased(MouseEvent e) {
			selShape = selectShape(xClicked, yClicked, zoomScale);
			if (selShape != null && selShape.getGroupStat()) {
				srndRect = getSrndingRect(selectedShapes);
			}
			if (resizing) {
				resizing = false;
			}
			if (drawingShape) {
				selectedShapes.add(newShape);

				drawingShape = false;
				shapeType = null;

				// remove shapes that were not drawn properly
				if (newShape instanceof JRectangle) {
					JRectangle castedRect = (JRectangle) newShape;
					if (castedRect.getWidth(zoomScale) <= 0
							|| castedRect.getHeight(zoomScale) <= 0) {
						allShapes.remove(allShapes.size() - 1);
						selectedShapes.clear();
					}
				} else if (newShape instanceof JCircle) {
					if (((JCircle) newShape).getDiameter(zoomScale) <= 0) {
						allShapes.remove(allShapes.size() - 1);
						selectedShapes.clear();
					}
				}

			} else if (select != null) {
				if (select.getWidth(zoomScale) == 0
						|| select.getHeight(zoomScale) == 0)
					selectedShapes.clear();

				for (int i = 0; i < allShapes.size(); i++) {
					if (allShapes.get(i).intersect(select, zoomScale)) {
						if (allShapes.get(i).getGroupStat()) {
							groupIndex = getGroupIndex(allShapes.get(i));
							for (int k = 0; k < group.get(groupIndex).length; k++)
								selectedShapes.add(group.get(groupIndex)[k]);
						} else
							selectedShapes.add(allShapes.get(i));
					}
				}
				select = null;
			}
			dragging = false;
			repaint();
		}
	}
	class MyMouseMotionListener extends MouseMotionAdapter {
		public void mouseDragged(MouseEvent e) {
			xReleased = e.getX();
			yReleased = e.getY();
			srndRect = null;
			if (selectedShapes.size() != 0 && !drawingShape && !resizing) {

				dragging = true;
				if (selectedShapes.size() == 1
						&& selectedShapes.get(0) instanceof JLine) {
					if (lineEnd.equals("endPoint")) {
						updatedX = ((JLine) selectedShapes.get(0))
								.getX2(zoomScale) + xReleased - xClicked;
						updatedY = ((JLine) selectedShapes.get(0))
								.getY2(zoomScale) + yReleased - yClicked;
					} else {
						updatedX = ((JLine) selectedShapes.get(0))
								.getX(zoomScale) + xReleased - xClicked;
						updatedY = ((JLine) selectedShapes.get(0))
								.getY(zoomScale) + yReleased - yClicked;
					}
					updateShapeCoordinates(selectedShapes.get(0), updatedX,
							updatedY, zoomScale);
				} else {
					for (int i = 0; i < selectedShapes.size(); i++) {
						if (selectedShapes.get(i) instanceof JLine) {
							updatedX = ((JLine) selectedShapes.get(i))
									.getX2(zoomScale) + xReleased - xClicked;
							updatedY = ((JLine) selectedShapes.get(i))
									.getY2(zoomScale) + yReleased - yClicked;
							lineEnd = "endPoint";
							updateShapeCoordinates(selectedShapes.get(i),
									updatedX, updatedY, zoomScale);
						}
						lineEnd = "startPoint";
						updatedX = selectedShapes.get(i).getX(zoomScale)
								+ xReleased - xClicked;
						updatedY = selectedShapes.get(i).getY(zoomScale)
								+ yReleased - yClicked;

						updateShapeCoordinates(selectedShapes.get(i), updatedX,
								updatedY, zoomScale);
					}
				}

				xClicked = xReleased;
				yClicked = yReleased;
				isSaved = false;
			}

			else if (resizing) {
				Shape selShape = selectedShapes.get(0);
				Rectangle2D handle = handles.get(handleIndex);
				double strokeSize;

				handle.setRect(xReleased, yReleased, handleSize, handleSize);

				if (selShape.getStroke() == null)
					strokeSize = 0;
				else
					strokeSize = selShape.getStroke().getLineWidth();

				if (handleIndex == 0 || handleIndex == 3) {

					if (handleIndex == 0)
						selShape.setX(handles.get(0).getCenterX() / zoomScale);
					else
						selShape.setY(handles.get(3).getCenterY() / zoomScale);

					updatedWidth = Math.abs(handles.get(0).getCenterX()
							- handles.get(3).getCenterX())
							- (strokeSize / 2);
					updatedHeight = Math.abs(handles.get(0).getCenterY()
							- handles.get(3).getCenterY())
							- (strokeSize / 2);
				} else if (handleIndex == 1 || handleIndex == 2) {
					if (handleIndex == 2) {
						selShape.setX(handles.get(2).getCenterX() / zoomScale);
						selShape.setY(handles.get(2).getCenterY() / zoomScale);
					}
					updatedWidth = Math.abs(handles.get(1).getCenterX()
							- handles.get(2).getCenterX())
							- (strokeSize / 2);
					updatedHeight = Math.abs(handles.get(1).getCenterY()
							- handles.get(2).getCenterY())
							- (strokeSize / 2);
				} else if (handleIndex == 4 || handleIndex == 5) {
					if (handleIndex == 4)
						selShape.setY(handles.get(4).getCenterY() / zoomScale);

					updatedWidth = ((JRectangle) selShape).getWidth(zoomScale);
					updatedHeight = Math.abs(handles.get(4).getCenterY()
							- handles.get(5).getCenterY())
							- (strokeSize / 2);
				} else if (handleIndex == 6 || handleIndex == 7) {
					if (handleIndex == 6)
						selShape.setX(handles.get(6).getCenterX() / zoomScale);

					updatedWidth = Math.abs(handles.get(7).getCenterX()
							- handles.get(6).getCenterX())
							- (strokeSize / 2);
					updatedHeight = ((JRectangle) selShape)
							.getHeight(zoomScale);
				}

				if (selShape instanceof JRectangle) {
					((JRectangle) selShape).setWidth(updatedWidth / zoomScale);
					((JRectangle) selShape)
							.setHeight(updatedHeight / zoomScale);
				} else if (selShape instanceof JCircle) {
					if (handleIndex == 1)
						((JCircle) selShape)
								.setRadius((((updatedHeight + updatedWidth) / 2) / zoomScale));
				}
				isSaved = false;
			}

			else if (drawingShape) {
				if (shapeType.equals("Rectangle")) {
					((JRectangle) newShape).setWidth((xReleased - xClicked)
							/ zoomScale);
					((JRectangle) newShape).setHeight((yReleased - yClicked)
							/ zoomScale);
				} else if (shapeType.equals("Oval")) {
					((JCircle) newShape).setRadius((xReleased - xClicked)
							/ zoomScale);
				} else if (shapeType.equals("Line")) {
					((JLine) newShape).setX2(xReleased / zoomScale);
					((JLine) newShape).setY2(yReleased / zoomScale);
				}
				isSaved = false;
			} else if (select != null) {
				select.setHeight((yReleased - yClicked) / zoomScale);
				select.setWidth((xReleased - xClicked) / zoomScale);
			}
			repaint();
		}

		public void mouseMoved(MouseEvent e) {

			cursorOverShape = selectShape(e.getX(), e.getY(), zoomScale);
			if (cursorOverShape != null && !drawingShape)
				curCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			else if (drawingShape)
				curCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			else {
				if (selectedShapes.size() == 1
						&& (handleIndex = selectHandle(e.getX(), e.getY())) != -1)

					curCursor = Cursor.getPredefinedCursor(handleIndex + 4);
				else
					curCursor = Cursor.getDefaultCursor();
			}
			repaint();
		}
	}
	/**
	 * method getGroupIndex to get find the index for a particular shape
	 */
	public int getGroupIndex(Shape sh) {
		for (int i = 0; i < group.size(); i++)
			for (int j = 0; j < group.get(i).length; j++)
				if (group.get(i)[j].getID() == sh.getID())
					return i;
		return -1;
	}
	/**
	 * method selectHandle to determine if a handle has been selected if yes which one.
	 */
	public int selectHandle(double x, double y) {
		if (selectedShapes.size() == 1
				&& !(selectedShapes.get(0) instanceof JLine)) {
			if (selectedShapes.get(0) instanceof JRectangle) {
				for (int i = 0; i < handles.size(); i++)
					if (handles.get(i).contains(x, y))
						return i;
			} else if (handles.get(1).contains(x, y))
				return 1;
		}
		return -1;
	}
	/**
	 * method updateShapeCoordinates to change the coordinated when a shape is dragged
	 */
	public void updateShapeCoordinates(Shape selectedShape, double newX,
			double newY, double zoomScale) {
		double selectedShapeWidth = selectedShape.getBounds(zoomScale)
				.getWidth();
		double selectedShapeHeight = selectedShape.getBounds(zoomScale)
				.getHeight();

		if (selectedShape instanceof JLine) {
			if (lineEnd.equals("startPoint")) {
				selectedShape.setX(newX / zoomScale);
				selectedShape.setY(newY / zoomScale);
			} else {
				((JLine) selectedShape).setX2(newX / zoomScale);
				((JLine) selectedShape).setY2(newY / zoomScale);
			}
		} else {
			if (newX > 0 && newX + selectedShapeWidth < getWidth())
				selectedShape.setX(newX / zoomScale);
			else if (newX > 0)
				selectedShape.setX((getWidth() - selectedShapeWidth)
						/ zoomScale);
			else
				selectedShape.setX(0);

			if (newY > 0 && (newY + selectedShapeHeight) < getHeight())
				selectedShape.setY(newY / zoomScale);
			else if (newY > 0)
				selectedShape.setY((getHeight() - selectedShapeHeight)
						/ zoomScale);
			else
				selectedShape.setY(0);
		}
	}

	public Shape selectShape(double shapeX, double shapeY, double zoomScale) {
		intersectedShapes.clear();

		for (int i = 0; i < allShapes.size(); i++)
			if (allShapes.get(i).contains(shapeX, shapeY, zoomScale))
				intersectedShapes.add(allShapes.get(i));

		if (intersectedShapes.size() == 0)
			return null;

		else if (intersectedShapes.size() == 1)
			return intersectedShapes.get(0);

		else {
			int maxIndex = 0;
			for (int j = 0; j < intersectedShapes.size(); j++) {
				for (int i = 0; i < allShapes.size(); i++) {
					if (intersectedShapes.get(j).getID() == allShapes.get(i)
							.getID()) {
						if (i > maxIndex) {
							maxIndex = i;
							break;
						}
					}
				}
			}
			return allShapes.get(maxIndex);
		}
	}

	public void changeColor() {
		if (selectedShapes.size() == 0)
			JOptionPane.showMessageDialog(null, "Please select a shape first");
		else {
			Color bgColor = JColorChooser.showDialog(this,
					"Choose Background Color", getBackground());
			if (bgColor != null) {
				for (int i = 0; i < selectedShapes.size(); i++)
					selectedShapes.get(i).setColor(bgColor);
			}
			isSaved = false;
			repaint();
		}
	}

	public void changeStroke() {
		if (selectedShapes.size() == 0)
			JOptionPane.showMessageDialog(null, "Please select a shape first");
		else {
			if (strokeProp != null)
				strokeProp.dispose();

			strokeProp = new JFrame();
			strokePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			strokeProp.setMinimumSize(new Dimension(260, 100));
			strokeProp.setTitle("Stroke Properties");
			strokeProp.setLocation(20, 50);
			strokeProp.setResizable(false);
			strokeProp.validate();
			strWidth = new JTextField(5);
			if (selectedShapes.size() == 1) {
				if (selectedShapes.get(0).getStroke() == null)
					strWidth.setText("0.0");
				else
					strWidth.setText(Double.toString(selectedShapes.get(0)
							.getStroke().getLineWidth()));
			}
			enterStrWidth = new JLabel("Enter new width(0-20):");
			alterStrokeButt = new JButton("Okay");
			alterColorButt = new JButton("Change Color");
			alterColorButt
					.setToolTipText("Change stoke color for the selected shape");
			alterStrokeButt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (strWidth.getText().equals(""))
						JOptionPane.showMessageDialog(null,
								"Please enter the new stroke width");
					else {
						try {
							Float newStrWidth = Float.parseFloat(strWidth
									.getText());
							if (newStrWidth < 0 || newStrWidth > 20) {
								JOptionPane.showMessageDialog(null,
										"Enter a numeric value between 0-20");
								if (selectedShapes.size() == 1) {
									if (selectedShapes.get(0).getStroke() == null)
										strWidth.setText("0.0");
									else
										strWidth.setText(Double
												.toString(selectedShapes.get(0)
														.getStroke()
														.getLineWidth()));
								}

							} else {
								BasicStroke newStroke;
								if (newStrWidth == 0)
									newStroke = null;
								else
									newStroke = new BasicStroke(newStrWidth);

								for (int i = 0; i < selectedShapes.size(); i++)
									selectedShapes.get(i).setStroke(newStroke);

								isSaved = false;
								repaint();
							}
						} catch (Exception error) {
							JOptionPane.showMessageDialog(null,
									"Enter a numeric value only");
							if (selectedShapes.size() == 1) {
								if (selectedShapes.get(0).getStroke() == null)
									strWidth.setText("0.0");
								else
									strWidth.setText(Double
											.toString(selectedShapes.get(0)
													.getStroke().getLineWidth()));
							}

						}

					}
				}
			});

			alterColorButt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Color bgColor = JColorChooser.showDialog(strokeProp,
							"Choose Background Color", getBackground());
					if (bgColor != null) {
						for (int i = 0; i < selectedShapes.size(); i++)
							selectedShapes.get(i).setStrokeColor(bgColor);

						isSaved = false;
					}
					repaint();
				}
			});

			strokePanel.add(enterStrWidth);
			strokePanel.add(strWidth);
			strokePanel.add(alterStrokeButt);
			strokePanel.add(alterColorButt);
			strokePanel.revalidate();
			strokeProp.pack();
			strokeProp.add(strokePanel);
			strokeProp.setVisible(true);

		}
	}

	public void removeShape() {
		if (selectedShapes.size() == 0)
			JOptionPane.showMessageDialog(null, "Please select a shape first");
		else {
			for (int j = 0; j < selectedShapes.size(); j++) {
				for (int i = 0; i < allShapes.size(); i++) {
					if (selectedShapes.get(j).getID() == allShapes.get(i)
							.getID()) {
						allShapes.remove(i);
						isSaved = false;
					}
				}
				// if the selected shapes belongs to a group, remove the group
				// as well
				if ((groupIndex = getGroupIndex(selectedShapes.get(j))) != -1)
					group.remove(groupIndex);
			}
			selectedShapes.clear();
			repaint();
		}
		srndRect = null;
	}

	public void bringFront() {
		if (selectedShapes.size() != 0) {
			for (int j = 0; j < selectedShapes.size(); j++) {
				tempShape = selectedShapes.get(j);
				for (int i = 0; i < allShapes.size(); i++)
					if (allShapes.get(i).getID() == tempShape.getID())
						allShapes.remove(i);

				allShapes.add(tempShape);
				isSaved = false;
			}
			repaint();
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select a shape/shapes first");
		}
	}

	public void selectAll() {
		selectedShapes.clear();
		for (int j = 0; j < allShapes.size(); j++)
			selectedShapes.add(allShapes.get(j));
		srndRect = null;
		repaint();
	}

	public void group() {
		if (selectedShapes.size() >= 2) {
			Shape[] toBeGrouped = new Shape[selectedShapes.size()];
			srndRect = getSrndingRect(selectedShapes);
			for (int i = 0; i < selectedShapes.size(); i++) {
				selectedShapes.get(i).setGroupStat(true);
				toBeGrouped[i] = selectedShapes.get(i);
			}
			group.add(toBeGrouped);
			Collections.reverse(group);
			isSaved = false;
			repaint();
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select two or more shapes to be grouped");
		}
	}

	public JRectangle getSrndingRect(ArrayList<Shape> selShapes) {
		double minX = selShapes.get(0).getBounds(zoomScale).getX();
		double minY = selShapes.get(0).getBounds(zoomScale).getY();
		double maxX = minX + selShapes.get(0).getBounds(zoomScale).getWidth();
		double maxY = minY + selShapes.get(0).getBounds(zoomScale).getHeight();
		for (int i = 0; i < selShapes.size(); i++) {
			double thisX = selShapes.get(i).getBounds(zoomScale).getX();
			double thisY = selShapes.get(i).getBounds(zoomScale).getY();
			double thisWidth = thisX
					+ selShapes.get(i).getBounds(zoomScale).getWidth();
			double thisHeight = thisY
					+ selShapes.get(i).getBounds(zoomScale).getHeight();
			if (thisX < minX)
				minX = thisX;
			if (thisY < minY)
				minY = thisY;
			if (thisWidth > maxX)
				maxX = thisWidth;
			if (thisHeight > maxY)
				maxY = thisHeight;
		}
		return new JRectangle(-100, minX - 10, minY - 10,
				Math.abs(maxX - minX) + 20, Math.abs(maxY - minY) + 20, null,
				new BasicStroke(3), Color.black);
	}

	public void ungroup() {
		if (selectedShapes.size() >= 2) {
			for (int i = 0; i < selectedShapes.size(); i++) {
				if (!selectedShapes.get(i).getGroupStat()) {
					JOptionPane
							.showMessageDialog(null,
									"One or more of the selected shapes does not belong to any group");
					return;
				}
			}

			for (int i = 0; i < selectedShapes.size(); i++) {
				selectedShapes.get(i).setGroupStat(false);
				if ((groupIndex = getGroupIndex(selectedShapes.get(i))) != -1)
					group.remove(groupIndex);
			}
			srndRect = null;
			isSaved = false;
			selectedShapes.clear();
			repaint();
		} else {
			JOptionPane.showMessageDialog(null,
					"Please select a group to ungroup");
		}
	}

	public File saveAs() {
		File newFile = null;
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File("New Document.svg"));
		}
		int returnVal = fileChooser.showSaveDialog(SVGPanel.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			newFile = fileChooser.getSelectedFile();

			createSVGFile(newFile);
			isSaved = true;
		}
		fileChooser.setSelectedFile(null);
		return newFile;
	}

	public void save(File file) {
		createSVGFile(file);
		isSaved = true;
	}

	public void createSVGFile(File file) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// creating svg element and its attributes
			Element svg = doc.createElement("svg");

			// namespace for the svg elements
			Attr xmlns = doc.createAttribute("xmlns");
			xmlns.setValue("http://www.w3.org/2000/svg");
			// version of the svg
			Attr version = doc.createAttribute("version");
			version.setValue("1.1");
			// width of the svg file
			Attr width = doc.createAttribute("Width");
			width.setValue(Double.toString(getPreferredSize().getWidth()));
			// height of the svg file
			Attr height = doc.createAttribute("Height");
			height.setValue(Double.toString(getPreferredSize().getHeight()));

			// adding attributes to svg element
			svg.setAttributeNode(xmlns);
			svg.setAttributeNode(version);
			svg.setAttributeNode(width);
			svg.setAttributeNode(height);

			for (int i = 0; i < group.size(); i++) {
				Element g = doc.createElement("g");
				for (int j = 0; j < group.get(i).length; j++) {
					Element thisElement = writeEachElementToFile(doc,
							group.get(i)[j]);
					g.appendChild(thisElement);
				}
				svg.appendChild(g);
			}

			for (int i = 0; i < allShapes.size(); i++) {
				if (!allShapes.get(i).getGroupStat()) {
					Element thisElement = writeEachElementToFile(doc,
							allShapes.get(i));
					svg.appendChild(thisElement);
				}
			}
			// add svg element to the document
			doc.appendChild(svg);
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public Element writeEachElementToFile(Document doc, Shape shape) {
		String r, g, b;
		if (shape instanceof JRectangle) {
			JRectangle thisRect = (JRectangle) shape;
			Element rect = doc.createElement("rect");

			Attr x = doc.createAttribute("x");
			x.setValue(Double.toString(thisRect.getX(normZoomScale)));

			Attr y = doc.createAttribute("y");
			y.setValue(Double.toString(thisRect.getY(normZoomScale)));

			Attr rectWidth = doc.createAttribute("width");
			rectWidth
					.setValue(Double.toString(thisRect.getWidth(normZoomScale)));

			Attr rectHeight = doc.createAttribute("height");
			rectHeight.setValue(Double.toString(thisRect
					.getHeight(normZoomScale)));

			rect.setAttributeNode(x);
			rect.setAttributeNode(y);
			rect.setAttributeNode(rectWidth);
			rect.setAttributeNode(rectHeight);

			if (thisRect.getColor() != null) {
				Attr fill = doc.createAttribute("fill");
				r = Integer.toString(thisRect.getColor().getRed());
				g = Integer.toString(thisRect.getColor().getGreen());
				b = Integer.toString(thisRect.getColor().getBlue());
				fill.setValue("rgb(" + r + "," + g + "," + b + ")");
				rect.setAttributeNode(fill);
			}
			if (thisRect.getStroke() != null) {
				Attr stroke = doc.createAttribute("stroke");
				r = Integer.toString(thisRect.getStrokeColor().getRed());
				g = Integer.toString(thisRect.getStrokeColor().getGreen());
				b = Integer.toString(thisRect.getStrokeColor().getBlue());
				stroke.setValue("rgb(" + r + "," + g + "," + b + ")");
				Attr strokeWidth = doc.createAttribute("stroke-width");
				strokeWidth.setValue(Double.toString(thisRect.getStroke()
						.getLineWidth()));

				rect.setAttributeNode(stroke);
				rect.setAttributeNode(strokeWidth);
			}
			return rect;
		} else if (shape instanceof JCircle) {
			JCircle thisCirc = (JCircle) shape;
			Element circ = doc.createElement("circle");

			Attr cx = doc.createAttribute("cx");
			cx.setValue(Double.toString(thisCirc.getX(normZoomScale)
					+ (thisCirc.getDiameter(normZoomScale) / 2)));

			Attr cy = doc.createAttribute("cy");
			cy.setValue(Double.toString(thisCirc.getY(normZoomScale)
					+ (thisCirc.getDiameter(normZoomScale / 2))));

			Attr rad = doc.createAttribute("r");
			rad.setValue(Double.toString(thisCirc.getDiameter(normZoomScale) / 2));

			circ.setAttributeNode(cx);
			circ.setAttributeNode(cy);
			circ.setAttributeNode(rad);

			if (thisCirc.getColor() != null) {
				Attr fill = doc.createAttribute("fill");
				r = Integer.toString(thisCirc.getColor().getRed());
				g = Integer.toString(thisCirc.getColor().getGreen());
				b = Integer.toString(thisCirc.getColor().getBlue());
				fill.setValue("rgb(" + r + "," + g + "," + b + ")");
				circ.setAttributeNode(fill);
			}
			if (thisCirc.getStroke() != null) {
				Attr stroke = doc.createAttribute("stroke");
				r = Integer.toString(thisCirc.getStrokeColor().getRed());
				g = Integer.toString(thisCirc.getStrokeColor().getGreen());
				b = Integer.toString(thisCirc.getStrokeColor().getBlue());
				stroke.setValue("rgb(" + r + "," + g + "," + b + ")");
				circ.setAttributeNode(stroke);

				Attr strokeWidth = doc.createAttribute("stroke-width");
				strokeWidth.setValue(Double.toString(thisCirc.getStroke()
						.getLineWidth()));
				circ.setAttributeNode(strokeWidth);
			}
			return circ;
		} else if (shape instanceof JLine) {
			JLine thisLine = (JLine) shape;
			Element line = doc.createElement("line");

			Attr x1 = doc.createAttribute("x1");
			x1.setValue(Double.toString(thisLine.getX(normZoomScale)));

			Attr y1 = doc.createAttribute("y1");
			y1.setValue(Double.toString(thisLine.getY(normZoomScale)));

			Attr x2 = doc.createAttribute("x2");
			x2.setValue(Double.toString(thisLine.getX2(normZoomScale)));

			Attr y2 = doc.createAttribute("y2");
			y2.setValue(Double.toString(thisLine.getY2(normZoomScale)));

			Attr stroke = doc.createAttribute("stroke");
			r = Integer.toString(thisLine.getStrokeColor().getRed());
			g = Integer.toString(thisLine.getStrokeColor().getGreen());
			b = Integer.toString(thisLine.getStrokeColor().getBlue());
			stroke.setValue("rgb(" + r + "," + g + "," + b + ")");

			Attr strokeWidth = doc.createAttribute("stroke-width");
			strokeWidth.setValue(Double.toString(thisLine.getStroke()
					.getLineWidth()));

			line.setAttributeNode(x1);
			line.setAttributeNode(y1);
			line.setAttributeNode(x2);
			line.setAttributeNode(y2);
			line.setAttributeNode(stroke);
			line.setAttributeNode(strokeWidth);

			return line;
		}
		return null;
	}

	public boolean isSaved() {
		return isSaved;
	}

	public void drawShape(String shapeType) {
		this.shapeType = shapeType;
		drawingShape = true;
		srndRect = null;
		selectedShapes.clear();
		repaint();
	}
}