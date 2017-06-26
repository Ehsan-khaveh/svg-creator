/** This class is an extension of JFrame to contain our SVGPanel. All the button commands
 * in GUI class are passed to this class and accordingly passed to SVGPanel class by \
 * this class
 * 
 * @version 1.1 Built in April 3, 2012.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;

import javax.swing.*;

import org.w3c.dom.*;

public class GraphicPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static final String undefined = "-1"; 
	private static final int defaultWidth = 500;
	private static final int defaultHeight = 500;
	protected int initWdth, initHght;
	protected boolean firstTime = true;
	AffineTransform at ;
	Dimension dimension;
	SVGPanel graphics;
	String width, height;
	JPanel docPanel;
	JFrame docProp;
	JTextField newWidth, newHeight;
	JLabel widthLabel, heightLabel;
	JButton editButt, cancelButt;
	
	public GraphicPanel(NodeList elements)
	{
		width = undefined;
		height = undefined;
		setBackground(Color.gray);
		setBorder(BorderFactory.createEtchedBorder());
		for(int i=0; i<elements.getLength();i++)
		{
			Element element = (Element)elements.item(i);
			NamedNodeMap atrbs = element.getAttributes();
			
			if(element.getNodeName().equalsIgnoreCase("svg"))
			{
				if(atrbs.getNamedItem("width") != null)
					width = atrbs.getNamedItem("width").getNodeValue();
				if(atrbs.getNamedItem("height") != null)
					height = atrbs.getNamedItem("height").getNodeValue();
			}
		}
		graphics = new SVGPanel(elements, width, height);
		add(graphics);
	}
	
	public GraphicPanel()
	{
		setBackground(Color.gray);
		setBorder(BorderFactory.createEtchedBorder());
		graphics = new SVGPanel(defaultWidth, defaultHeight);
		add(graphics);
	}
	public double getPanelWidth()
	{
		return graphics.getWidth();
	}
	public double getPanelHeight()
	{
		return graphics.getHeight();
	}
	public void zoom(double scale)
	{
		if(firstTime)
		{
			initWdth = graphics.getWidth();
			initHght = graphics.getHeight();
		}
		firstTime = false;
		graphics.initDimen(initWdth*(scale/100),initHght*(scale/100));
		at = AffineTransform.getTranslateInstance(graphics.getWidth(),graphics.getHeight());
		at.scale(scale, scale);
		graphics.setZoomScale(scale);
		revalidate();
        repaint();
	}
	public void selectAll()
	{
		graphics.selectAll();
	}
	public void changeColor()
	{
		graphics.changeColor();
	}
	public void changeStroke()
	{
		graphics.changeStroke();
	}
	public void createShape(String shapeType)
	{
		graphics.drawShape(shapeType);
	}
	public void resizePanel()
	{
		if(docProp != null)
			docProp.dispose();
		
		docProp = new JFrame();
		docPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		docProp.setMinimumSize(new Dimension(180, 130));
		docProp.setLocation(20,50);
		docProp.setTitle("Properties");
		docProp.setResizable(false);
		docProp.validate();
		newWidth = new JTextField(5);
		newHeight = new JTextField(5);
		newWidth.setText(Integer.toString((int)(graphics.getWidth()/graphics.getZoomScale())));
		newHeight.setText(Integer.toString((int)(graphics.getHeight()/graphics.getZoomScale())));
		
		widthLabel = new JLabel("Width: ");
		heightLabel = new JLabel("Height: ");
		editButt = new JButton("Edit");
		cancelButt = new JButton("Cancel");
		
		editButt.setToolTipText("Change the document dimension to the following values");
		editButt.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(newWidth.getText().equals(""))
					JOptionPane.showMessageDialog(null,"Please enter a value for width");
				else if(newHeight.getText().equals(""))
					JOptionPane.showMessageDialog(null,"Please enter a value for height");
				else
				{
					try
					{
						int updatedWidth = Integer.parseInt(newWidth.getText());
						int updatedHeight = Integer.parseInt(newHeight.getText());
						if(updatedWidth<100 || updatedWidth>2000)
						{
							JOptionPane.showMessageDialog(null,"Enter a numeric value between 100-2000 for width");
							newWidth.setText("0");
						}
						else if(updatedHeight<100 || updatedHeight>2000)
						{
							JOptionPane.showMessageDialog(null,"Enter a numeric value between 100-2000 for height");
							newHeight.setText("0");
						}
						else
						{
							graphics.setPreferredSize(new Dimension(updatedWidth, updatedHeight));
							revalidate();
						}
					}
					catch(Exception error)
					{
						JOptionPane.showMessageDialog(null,"Enter an integer value only");
						newWidth.setText(Integer.toString((int)(graphics.getWidth()/graphics.getZoomScale())));
						newHeight.setText(Integer.toString((int)(graphics.getHeight()/graphics.getZoomScale())));
					}
					
				}
			}
		}
		);
		
		cancelButt.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				docProp.dispose();
			}
		}
		);
		
		docPanel.add(widthLabel);
		docPanel.add(newWidth);
		docPanel.add(heightLabel);
		docPanel.add(newHeight);
		docPanel.add(editButt);
		docPanel.add(cancelButt);
		docPanel.revalidate();
		docProp.add(docPanel);
		docProp.setVisible(true);
		
	}
	public void removeShape()
	{
		graphics.removeShape();
	}
	public void bringFront()
	{
		graphics.bringFront();
	}
	public void ungroup()
	{
		graphics.ungroup();
	}
	public void group()
	{
		graphics.group();
	}
	public void save(File file)
	{
		graphics.save(file);
	}
	public File saveAs()
	{
		return graphics.saveAs();
	}
	public boolean isSaved()
	{
		return graphics.isSaved();
	}
}