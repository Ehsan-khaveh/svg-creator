/** Shape class is a super class for all the objects from in different shapes
 * it contains methods that are shared by all the subclasses, abstract methods that
 * are implemented in all the subclasses and methods that are overridden in all
 * or some subclasses  
 * 
 * @version 1.1 Built in April 3, 2012.
 */


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public abstract class Shape {

	public final double handleSize = 8.0; 
	protected int id;
	protected double x, y;
	protected Color color, strokeColor;
	protected BasicStroke stroke;
	protected Dimension newDim = new Dimension();
	protected boolean inGroup;
	ArrayList<Rectangle2D> handles = new ArrayList<Rectangle2D>();
	
	public Shape(int id, double x, double y, Color color, BasicStroke stroke, Color strokeColor){
		this.id = id;
		this.x = x;
		this.y = y;
		this.color = color;
		this.stroke = stroke;
		this.strokeColor = strokeColor;
		inGroup = false;
	}
	public int getID()
	{
		return id;
	}
	public double getX(double zoomScale)
	{
		return x*zoomScale;
	}
	public double getY(double zoomScale)
	{
		return y*zoomScale;
	}
	public void setX(double newX)
	{
		x = newX;
	}
	public void setY(double newY)
	{
		y = newY;
	}
	public Color getColor()
	{
		return color;
	}
	public void setColor(Color newColor)
	{
		color = newColor;
	}
	public BasicStroke getStroke()
	{
		return stroke;
	}
	public void setStroke(BasicStroke newStroke)
	{
		stroke = newStroke;
	}
	public Color getStrokeColor()
	{
		return strokeColor;
	}
	public boolean getGroupStat()
	{
		return inGroup;
	}
	public void setGroupStat(boolean status)
	{
		inGroup = status;
	}
	public void setStrokeColor(Color newColor)
	{
		strokeColor = newColor;
	}
	public abstract Rectangle2D getBounds(double zoomScale);
	public abstract boolean contains(double x, double y, double zoomScale);
	public abstract Dimension drawShape(Graphics2D g2, double zoomScale);
	public abstract boolean intersect(JRectangle rect, double zoomScale);
	public abstract ArrayList<Rectangle2D> drawHandles(Graphics2D g2D, double zoomScale);
}
