
package dk.aau.cs.idq.indoorentities;

import org.khelekore.prtree.PointND;

import dk.aau.cs.idq.utilities.Constant;

/**
 * Rect.class
 * the shape rectangle as MBRs or Partitions
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see java.lang.Comparable dk.aau.cs.idq.indoorentities.Range
 *
 */
public class Rect implements Range, Comparable<Object> {

	private double x1;			// minimum in the x-axis
	private double x2;			// maximum in the x-axis

	private double y1;			// minimum in the y-axis
	private double y2;			// minimum in the y-axis

	private int mRectID = 0;	// the ID
	private int mFloor = 0;		// the belonging floor

	private double mArea;		// the area of this partition

	/**
	 * Constructor Function
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 */
	public Rect(double x1, double x2, double y1, double y2) {
		super();
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.setmArea((x2-x1)*(y2-y1));
	}

	/**
	 * Constructor Function
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param mRectID
	 * @param mFloor
	 */
	public Rect(double x1, double x2, double y1, double y2, int mRectID,
			int mFloor) {
		super();
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.setmArea((x2-x1)*(y2-y1));
		this.mRectID = mRectID;
		this.mFloor = mFloor;
	}

	/**
	 * Constructor Function
	 * 
	 * @param another
	 */
	public Rect(Rect another) {
		super();
		this.x1 = another.getX1();
		this.x2 = another.getX2();
		this.y1 = another.getY1();
		this.y2 = another.getY2();
		this.setmArea(another.getmArea());
		this.mRectID = another.getmRectID();
		this.mFloor = another.getmFloor();
	}

	/**
	 * test if anotherRect is contained in
	 * 
	 * @param anotherRect another Object_Rect
	 * @return True as contained
	 */
	public boolean contain(Rect anotherRect) {
		if (x1 < anotherRect.getX1() && x2 > anotherRect.getX2()
				&& y1 < anotherRect.getY1() && y2 > anotherRect.getY2()
				&& mFloor == anotherRect.getmFloor())
			return true;
		return false;
	}

	/**
	 * test if point is contained in
	 * 
	 * @param point
	 * @return True as contained
	 */
	public boolean contain(Point point) {
		
		if (x1 <= point.getX() && x2 >= point.getX() && y1 <= point.getY()
				&& y2 >= point.getY() && mFloor == point.getmFloor())
			return true;
		return false;
	}
	
	public boolean contain(SampledPoint point) {
		
		if (x1 < point.getSampledX() && x2 > point.getSampledX() && y1 < point.getSampledY()
				&& y2 > point.getSampledY() && mFloor == point.getmFloor())
			return true;
		return false;
		
	}

	/**
	 * test if point is contained in the vertical direction (without testing the floors)
	 * 
	 * @param point
	 * @return True as contained
	 */
	public boolean containVertical(Point point) {
		if (x1 < point.getX() && x2 > point.getX() && y1 < point.getY()
				&& y2 > point.getY())
			return true;
		return false;
	}
	

	/**
	 * get the width of the rectangle
	 * 
	 * @return the width
	 */
	public double getWidth() {
		return this.getX2() - this.getX1();
	}

	/**
	 * get the height of the rectangle
	 * 
	 * @return the height
	 */
	public double getHeight() {
		return this.getY2() - this.getY1();
	}

	/**
	 * get the intersection part of two Rectangles
	 * 
	 * @param anotherRect
	 * @return A Object_Rect
	 */
	public Rect intersection(Rect anotherRect) {
		if (mFloor != anotherRect.getmFloor())
			return null;
		if (x1 > anotherRect.getX2() || x2 < anotherRect.getX1())
			return null;
		if (y1 > anotherRect.getY2() || y2 < anotherRect.getY1())
			return null;

		double nx1 = Math.max(x1, anotherRect.getX1());
		double nx2 = Math.min(x2, anotherRect.getX2());
		double ny1 = Math.max(y1, anotherRect.getY1());
		double ny2 = Math.min(y2, anotherRect.getY2());

		Rect RectOut = new Rect(nx1, nx2, ny1, ny2);
		RectOut.setmFloor(mFloor);
		return RectOut;
	}

	/**
	 * move it by offset
	 * 
	 * @param delta_x offset in the x-axis
	 * @param delta_y offset in the y-axis
	 */
	public void offset(double delta_x, double delta_y) {
		x1 += delta_x;
		x2 += delta_x;
		y1 += delta_y;
		y2 += delta_y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.aau.cs.idqindoorentities.Range#getMinDist(dk.aau.cs.idqindoorentities
	 * .Point)
	 */
	@Override
	public double getMinDist(PointND point) {
		// TODO Auto-generated method stub

		double x0 = point.getOrd(0);
		double y0 = point.getOrd(1);

		/*
		double t = ((x1 - x0) * (x1 - x2) + (y1 - y0) * (y1 - y2))
				/ ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

		double x = x1 + t * (x2 - x1);
		double y = y1 + t * (y2 - y1);
		
		if(this.contain(new Point(x0, y0, this.mFloor))){
			return 0;
		}

		if (t < 0) // |p0 p1|
			return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
		else if (t > 1) // |p0 p2|
			return Math.sqrt((x0 - x2) * (x0 - x2) + (y0 - y2) * (y0 - y2));
		else
			// |p0 p|
			return Math.sqrt((x0 - x) * (x0 - x) + (y0 - y) * (y0 - y));
		 */
		
		if(this.contain(new Point(x0, y0, this.mFloor))){
			return 0;
		}else if(x0 < this.x1){
			if(y0 < this.y1){
				return Math.sqrt((x0 - this.x1) * (x0 - this.x1) + (y0 - this.y1) * (y0 - this.y1));
			}else if(y0 > this.y2){
				return Math.sqrt((x0 - this.x1) * (x0 - this.x1) + (y0 - this.y2) * (y0 - this.y2));
			}else
			{
				return this.x1 - x0;
			}
		}else if(x0 > this.x2){
			if(y0 < this.y1){
				return Math.sqrt((x0 - this.x2) * (x0 - this.x2) + (y0 - this.y1) * (y0 - this.y1));
			}else if(y0 > this.y2){
				return Math.sqrt((x0 - this.x2) * (x0 - this.x2) + (y0 - this.y2) * (y0 - this.y2));
			}else
			{
				return x0 - this.x2;
			}
		}else{
			if(y0 < this.y1){
				return this.y1 - y0;
			}else if(y0 > this.y2){
				return y0 - this.y2;
			}else
				return 0;
		}
		
	}
	
	

	public double getMinDist(SampledPoint point) {
		// TODO Auto-generated method stub

		double x0 = point.getSampledX();
		double y0 = point.getSampledY();

		/*
		double t = ((x1 - x0) * (x1 - x2) + (y1 - y0) * (y1 - y2))
				/ ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

		double x = x1 + t * (x2 - x1);
		double y = y1 + t * (y2 - y1);

		if (t < 0) // |p0 p1|
			return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
		else if (t > 1) // |p0 p2|
			return Math.sqrt((x0 - x2) * (x0 - x2) + (y0 - y2) * (y0 - y2));
		else
			// |p0 p|
			return Math.sqrt((x0 - x) * (x0 - x) + (y0 - y) * (y0 - y));
		*/
		
		if(this.contain(new Point(x0, y0, this.mFloor))){
			return 0;
		}else if(x0 < this.x1){
			if(y0 < this.y1){
				return Math.sqrt((x0 - this.x1) * (x0 - this.x1) + (y0 - this.y1) * (y0 - this.y1));
			}else if(y0 > this.y2){
				return Math.sqrt((x0 - this.x1) * (x0 - this.x1) + (y0 - this.y2) * (y0 - this.y2));
			}else
			{
				return this.x1 - x0;
			}
		}else if(x0 > this.x2){
			if(y0 < this.y1){
				return Math.sqrt((x0 - this.x2) * (x0 - this.x2) + (y0 - this.y1) * (y0 - this.y1));
			}else if(y0 > this.y2){
				return Math.sqrt((x0 - this.x2) * (x0 - this.x2) + (y0 - this.y2) * (y0 - this.y2));
			}else
			{
				return x0 - this.x2;
			}
		}else{
			if(y0 < this.y1){
				return this.y1 - y0;
			}else if(y0 > this.y2){
				return y0 - this.y2;
			}else
				return 0;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.aau.cs.idqindoorentities.Range#getMaxDist(dk.aau.cs.idqindoorentities
	 * .Point)
	 */
	@Override
	public double getMaxDist(PointND pointnd) {
		// TODO Auto-generated method stub

		Point pt1 = new Point(x1, y1);
		Point pt2 = new Point(x1, y2);
		Point pt3 = new Point(x2, y1);
		Point pt4 = new Point(x2, y2);

		Point point = new Point(pointnd.getOrd(0), pointnd.getOrd(1));

		double[] dist = new double[4];
		dist[0] = point.eDist(pt1);
		dist[1] = point.eDist(pt2);
		dist[2] = point.eDist(pt3);
		dist[3] = point.eDist(pt4);

		double maxdist = 0.0;
		for (int i = 0; i < 4; i++) {
			if (maxdist < dist[i] + Constant.epsi)
				maxdist = dist[i];
		}
		return maxdist;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dk.aau.cs.idqindoorentities.Range#reflection(int, int)
	 */
	@Override
	public void reflection(int axis, int pivot) {
		// TODO Auto-generated method stub
		double x0, y0, xa, xb, ya, yb;
		if (0 == axis) {
			x0 = pivot;
			xa = x0 + (x0 - x2);
			xb = x0 + (x0 - x1);
			ya = y1;
			yb = y2;
		} else {
			y0 = pivot;
			xa = x1;
			xb = x2;
			ya = y0 + (y0 - y1);
			yb = y0 + (y0 - y2);
		}
		x1 = Math.min(xa, xb);
		x2 = Math.max(xa, xb);
		y1 = Math.min(ya, yb);
		y2 = Math.max(ya, yb);
	}

	/**
	 * toString
	 * 
	 * @return mRectID + x1 + x2 + y1 + y2
	 */
	public String toString() {
		String tempString = this.mRectID + " " + this.x1 + " " + this.x2 + " "
				+ this.y1 + " " + this.y2 + " " + this.mFloor;
		return tempString;
	}

	/**
	 * @return the x1
	 */
	public double getX1() {
		return x1;
	}

	/**
	 * @param x1
	 *            the x1 to set
	 */
	public void setX1(double x1) {
		this.x1 = x1;
	}

	/**
	 * @return the x2
	 */
	public double getX2() {
		return x2;
	}

	/**
	 * @param x2
	 *            the x2 to set
	 */
	public void setX2(double x2) {
		this.x2 = x2;
	}

	/**
	 * @return the y1
	 */
	public double getY1() {
		return y1;
	}

	/**
	 * @param y1
	 *            the y1 to set
	 */
	public void setY1(double y1) {
		this.y1 = y1;
	}

	/**
	 * @return the y2
	 */
	public double getY2() {
		return y2;
	}

	/**
	 * @param y2
	 *            the y2 to set
	 */
	public void setY2(double y2) {
		this.y2 = y2;
	}

	/**
	 * @return the mFloor
	 */
	public int getmFloor() {
		return mFloor;
	}

	/**
	 * @param mFloor
	 *            the mFloor to set
	 */
	public void setmFloor(int mFloor) {
		this.mFloor = mFloor;
	}

	/**
	 * @return the mArea
	 */
	public double getmArea() {
		return mArea;
	}

	/**
	 * @param mArea the mArea to set
	 */
	public void setmArea(double mArea) {
		this.mArea = mArea;
	}

	/**
	 * @return the mRectID
	 */
	public int getmRectID() {
		return mRectID;
	}

	/**
	 * @param mRectID
	 *            the mRectID to set
	 */
	public void setmRectID(int mRectID) {
		this.mRectID = mRectID;
	}
	
	/**
	 * test if this rectangle is adjacent to another rectangle
	 * 
	 * @param another
	 * @return boolean value
	 */
	boolean isAdjacent(Rect another){
		
		if((this.x1 == another.x1) || (this.x2 == another.x2) || (this.x1 == another.x2) || (this.x2 == another.x1)){
			if(this.y1 == another.y2){
				return true;
			}
			if(this.y2 == another.y1){
				return true;
			}
		}
		
		if((this.y1 == another.y1) || (this.y2 == another.y2) || (this.y1 == another.y2) || (this.y2 == another.y1)){
			if(this.x1 == another.x2){
				return true;
			}
			if(this.x2 == another.x1){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * test if the door is belonging to this partition
	 * 
	 * @param another
	 * @return True shows that it is
	 */
	public boolean testDoor(Door another) {
		if (Math.abs(another.getX() - this.getX1()) < Constant.epsi
				|| Math.abs(another.getX() - this.getX2()) < Constant.epsi) {
			if ((another.getY() >= this.getY1())
					&& (another.getY() <= this.getY2())) {
				// addDoor(pt);
				return true;
			}
		} else if (Math.abs(another.getY() - this.getY1()) < Constant.epsi
				|| Math.abs(another.getY() - this.getY2()) < Constant.epsi) {
			if ((another.getX() >= this.getX1())
					&& (another.getX() <= this.getX2())) {
				// addDoor(pt);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Rect otherRect = (Rect) o;
		return this.mRectID > otherRect.mRectID ? 1
				: (this.mRectID == otherRect.mRectID ? 0 : -1);

	}

}
