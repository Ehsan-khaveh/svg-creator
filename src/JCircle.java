/** Class JCircle is a subclass of Shape contains all the implementations and
 * stores all the information about circle objects. All the abstract methods
 * in Shape class are implemented as required in this class
 * 
 * @version 1.1 Built in April 3, 2012.
 */



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class JCircle extends Shape {

	double radius;
	protected Ellipse2D circObj;
	
	public JCircle(int id, double x, double y, double radius, Color color,
			BasicStroke stroke, Color strokeColor) {
		super(id, x, y, color, stroke, strokeColor);
		this.radius = radius;
	}

	public double getDiameter(double zoomScale) {
		return radius*zoomScale;
	}
	
	public void setRadius(double newRadius) {
		radius = newRadius;
	}
	
	public Rectangle2D.Double getBounds(double zoomScale) {
		double strokeWidth;
		if(stroke != null)
			strokeWidth = stroke.getLineWidth();
		else
			strokeWidth = 0;
		
		return new Rectangle2D.Double((x - strokeWidth/2) * zoomScale,
				(y - strokeWidth/2) * zoomScale,
				(radius + strokeWidth) * zoomScale,
				(radius + strokeWidth) * zoomScale);
	}

	public Dimension drawShape(Graphics2D g2, double zoomScale)
	{
		circObj = new Ellipse2D.Double(getX(zoomScale), getY(zoomScale), getDiameter(zoomScale), getDiameter(zoomScale));
		//draw the shape
		if(getColor() != null)
		{
			g2.setPaint(getColor());
			g2.fill(circObj);
		}
		else
			g2.draw(circObj);
		
		//draw the stroke for the shape if it has any
		if(getStroke() != null)
		{
			g2.setPaint(getStrokeColor());
			g2.setStroke(new BasicStroke((float)(getStroke().getLineWidth()*zoomScale)));
			g2.draw(circObj);
			
			newDim.width = (int)(getX(zoomScale)+getDiameter(zoomScale)+getStroke().getLineWidth()*zoomScale);
			newDim.height = (int)(getY(zoomScale)+getDiameter(zoomScale)+getStroke().getLineWidth()*zoomScale);
		}
		else
		{
			newDim.width = (int)(getX(zoomScale)+getDiameter(zoomScale));
			newDim.height= (int)(getY(zoomScale)+getDiameter(zoomScale));
		}
		
		return newDim;
	}
	
	public boolean contains(double x, double y, double zoomScale) {
		double ellw = getDiameter(zoomScale);
		if (ellw <= 0.0) {
			return false;
		}
		double normx = (x - getX(zoomScale)) / ellw - 0.5;
		double ellh = getDiameter(zoomScale);
		if (ellh <= 0.0) {
			return false;
		}
		double normy = (y - getY(zoomScale)) / ellh - 0.5;
		return (normx * normx + normy * normy) < 0.25;
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
		
		
		g2D.setPaint(Color.black);
		for(int j=0; j<handles.size(); j++)
			g2D.fill(handles.get(j));
		
		return handles;
	}
}