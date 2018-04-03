package paint;

import java.awt.*;

public class Drawing {
	private Shape shape;
	private Shape innerShape;
	private Color strokeColor, fillColor;
	private int strokeWidth;
	private float transparency;
	private boolean isBrush;
	
	public Drawing(Shape shape, Shape innerShape, Color strokeColor, Color fillColor, int strokeWidth, float transparency, boolean isBrush) {
		this.shape = shape;
		this.innerShape = innerShape;
		this.strokeColor = strokeColor;
		this.fillColor = fillColor;
		this.strokeWidth = strokeWidth;
		this.transparency = transparency;
		this.isBrush = isBrush;
	}

	public Shape getShape() {
		return shape;
	}
	
	public Shape getInnerShape() {
		return innerShape;
	}

	public Color getStrokeColor() {
		return strokeColor;
	}

	public Color getFillColor() {
		return fillColor;
	}
	
	public int getStrokeWidth() {
		return strokeWidth;
	}
	
	public float getTransparency() {
		return transparency;
	}
	
	public boolean isBrush() {
		return isBrush;
	}
}
