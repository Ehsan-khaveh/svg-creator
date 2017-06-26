/** Class JLine is a subclass of Shape contains all the implementations and
 * stores all the information about line objects. All the abstract methods
 * in Shape class are implemented as required in this class
 *
 * @version 1.1 Built in April 3, 2012.
 */

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class JLine extends Shape{
	
	protected double x2, y2;
	protected Line2D lineObj;
	private double pointRadius;
	private int scaleEnlgPoint;
	
	public JLine(int id, double x, double y, double x2, double y2, Color color,BasicStroke stroke, Color strokeColor)
	{
		super(id, x,y,color, stroke, strokeColor);
		this.x2 = x2;
		this.y2 = y2;
		if(getStroke().getLineWidth()<6)
			scaleEnlgPoint = 4;
		else
			scaleEnlgPoint = 2;
	}
	
	public double getX2(double zoomScale)
	{
		return x2*zoomScale;
	}
	public double getY2(double zoomScale)
	{
		return y2*zoomScale;
	}
	public void setX2(double newX2)
	{
		x2 = newX2;
	}
	public void setY2(double newY2)
	{
		y2 = newY2;
	}
	public Rectangle2D getBounds(double zoomScale)
	{
		double startX, startY, endX, endY,width, height, strokeWidth;
		
		strokeWidth = stroke.getLineWidth();
		startX = (Math.min(x,x2)-strokeWidth)*zoomScale;
		startY = (Math.min(y,y2)-strokeWidth)*zoomScale;
		endX = (Math.max(x,x2)+strokeWidth)*zoomScale;
		endY = (Math.max(y,y2)+strokeWidth)*zoomScale;
		width = endX-startX;
		height = endY-startY;
		return new Rectangle2D.Double(startX,startY,width,height);
	}
	public Dimension drawShape(Graphics2D g2, double zoomScale)
	{
		lineObj = new Line2D.Double(getX(zoomScale), getY(zoomScale), getX2(zoomScale), getY2(zoomScale));
		//draw the shape
		g2.draw(lineObj);

		//draw the stroke for the shape if it has any
		if(getStroke() != null)
		{
			g2.setPaint(getStrokeColor());
			g2.setStroke(new BasicStroke((float)(getStroke().getLineWidth()*zoomScale)));
			g2.draw(lineObj);
		}
		newDim.width = (int)(Math.max(getX(zoomScale), getX2(zoomScale))+getStroke().getLineWidth()*zoomScale);
		newDim.height = (int)(Math.max(getY(zoomScale), getY2(zoomScale))+getStroke().getLineWidth()*zoomScale);
		
		return newDim;
	}
	public boolean contains(double x, double y, double zoomScale)
	{
		if(getStartPoint(zoomScale).contains(x,y) || getEndPoint(zoomScale).contains(x,y))
			return true;
		else
			return false;
	}
	public Ellipse2D getStartPoint(double zoomScale)
	{
		
		pointRadius = stroke.getLineWidth()*zoomScale;
		return new Ellipse2D.Double(getX(zoomScale)-pointRadius,getY(zoomScale)-pointRadius,pointRadius*scaleEnlgPoint, pointRadius*scaleEnlgPoint);
	}
	public Ellipse2D getEndPoint(double zoomScale)
	{
		pointRadius = stroke.getLineWidth()*zoomScale;
		return new Ellipse2D.Double(getX2(zoomScale)-pointRadius,getY2(zoomScale)-pointRadius,pointRadius*scaleEnlgPoint, pointRadius*scaleEnlgPoint);
	}
	public boolean intersect(JRectangle rect, double zoomScale)
	{
		double startPointX = this.getStartPoint(zoomScale).getX();
		double startPointY = this.getStartPoint(zoomScale).getY();
		double endPointX = this.getEndPoint(zoomScale).getX();
		double endPointY = this.getEndPoint(zoomScale).getY();
		double selectX = rect.getBounds(zoomScale).getX();
		double selectY = rect.getBounds(zoomScale).getY();
		
		if (startPointX >= selectX &&
			startPointX < selectX + rect.getBounds(zoomScale).getWidth() &&
			startPointY >= selectY &&
			startPointY < selectY + rect.getBounds(zoomScale).getHeight())
		return true;
		
		else if (endPointX >= selectX &&
				endPointX < selectX + rect.getBounds(zoomScale).getWidth() &&
				endPointY >= selectY &&
				endPointY < selectY + rect.getBounds(zoomScale).getHeight())
		return true;
		
		else
			return false;
	}
	public ArrayList<Rectangle2D> drawHandles(Graphics2D g2D, double zoomScale)
	{
		g2D.setPaint(Color.black);
		g2D.fill(getStartPoint(zoomScale));
		g2D.fill(getEndPoint(zoomScale));
		return null;
	}
}
