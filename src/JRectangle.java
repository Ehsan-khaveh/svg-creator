/** Class JRectangle is a subclass of Shape contains all the implementations and
 * stores all the information about rectangular objects. All the abstract methods
 * in Shape class are implemented as required in this class
 * 
 * @version 1.1 Built in April 3, 2012.
 */


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class JRectangle extends Shape {

	protected double width, height;
	protected Rectangle2D rectObj;

	public JRectangle(int id, double x, double y, double width, double height,
			Color color, BasicStroke stroke, Color strokeColor) {
		super(id, x, y, color, stroke, strokeColor);
		this.width = width;
		this.height = height;
	}

	public double getWidth(double zoomScale) {
		return width*zoomScale;
	}
	

	public double getHeight(double zoomScale) 
	{
		return height*zoomScale;
	}
	
	public void setWidth(double newWidth) 
	{
		width = newWidth;
	}
	public void setHeight(double newHeight) 
	{
		height= newHeight;
	}

	public Rectangle2D getBounds(double zoomScale) {
		double strokeWidth;
		if(stroke != null)
			strokeWidth = stroke.getLineWidth();
		else
			strokeWidth = 0;
		return new Rectangle2D.Double((x - (strokeWidth/2)) * zoomScale,
				(y - (strokeWidth/2)) * zoomScale, (width + strokeWidth)
						* zoomScale, (height + strokeWidth) * zoomScale);
	}
	
	public Dimension drawShape(Graphics2D g2, double zoomScale)
	{
		rectObj = new Rectangle2D.Double(getX(zoomScale), getY(zoomScale), getWidth(zoomScale), getHeight(zoomScale));
		//draw the shape
		if(getColor() != null)
		{
			g2.setPaint(getColor());
			g2.fill(rectObj);
		}
		else
			g2.draw(rectObj);
		
		//draw the stroke for the shape if it has any
		if(getStroke() != null)
		{
			g2.setPaint(getStrokeColor());
			g2.setStroke(new BasicStroke((float)(getStroke().getLineWidth()*zoomScale)));
			g2.draw(rectObj);
			newDim.width = (int)(getX(zoomScale)+getWidth(zoomScale)+getStroke().getLineWidth()*zoomScale);
			newDim.height = (int)(getY(zoomScale)+getHeight(zoomScale)+getStroke().getLineWidth()*zoomScale);
		}
		else
		{
			newDim.width = (int)(getX(zoomScale)+getWidth(zoomScale));
			newDim.height = (int)(getY(zoomScale)+getHeight(zoomScale));
		}
		
		return newDim;
	}
	
	public boolean contains(double x, double y, double zoomScale) {
		double x0 = getX(zoomScale);
		double y0 = getY(zoomScale);
		return (x >= x0 && y >= y0 && x < x0 + getWidth(zoomScale) && y < y0
				+ getHeight(zoomScale));
	}
	
	public boolean intersect(JRectangle rect, double zoomScale)
	{
		if(this.getBounds(zoomScale).intersects(rect.getBounds(zoomScale)))
			return true;
		else
			return false;
	}
	
	public ArrayList<Rectangle2D> drawHandles(Graphics2D g2D, double zoomScale)
	{
		double x = this.getBounds(zoomScale).getX();
		double y = this.getBounds(zoomScale).getY();
		double w = this.getBounds(zoomScale).getWidth();
		double h = this.getBounds(zoomScale).getHeight();
		
		handles.clear();
		handles.add(new Rectangle.Double(x - (handleSize/2), y + h - (handleSize/2), handleSize, handleSize));//1
		handles.add(new Rectangle.Double(x + w - (handleSize/2), y + h - (handleSize/2), handleSize, handleSize));//2
		handles.add(new Rectangle.Double(x - (handleSize/2), y - (handleSize/2), handleSize, handleSize));//3
		handles.add(new Rectangle.Double(x + w - (handleSize/2), y - (handleSize/2), handleSize, handleSize));//4
		handles.add(new Rectangle.Double(x + w * 0.5 - (handleSize/2), y - (handleSize/2), handleSize, handleSize));//5
		handles.add(new Rectangle.Double(x + w * 0.5 - (handleSize/2), y + h - (handleSize/2), handleSize, handleSize));//6
		handles.add(new Rectangle.Double(x - (handleSize/2), y + h * 0.5 - (handleSize/2), handleSize, handleSize));//7
		handles.add(new Rectangle.Double(x + w - (handleSize/2), y + h * 0.5 - (handleSize/2), handleSize, handleSize));//8
		
		g2D.setPaint(Color.black);
		for(int j=0; j<handles.size(); j++)
			g2D.fill(handles.get(j));
		
		return handles;
	}
}
