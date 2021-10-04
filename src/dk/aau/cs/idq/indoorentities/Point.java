package dk.aau.cs.idq.indoorentities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.khelekore.prtree.PointND;

import algorithm.AlgCSDM;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.Functions;

/**
 * Floor Point to describe a object's position when walking on one of the floors
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see org.khelekore.prtree.PointND
 *
 */
public class Point implements PointND, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8230759856415421231L;
	private double x; // x coordinate
	private double y; // y coordinate

	private int mFloor; // belonging floor

	private double prob; /// the probability of this point

	private double[] eDist2Doors = null; /// to be initizlied when first time retrieve

	private Par curPar = null;
	
	
	/**
	 * Constructor Function
	 * 
	 */
	public Point() {
		super();
		this.x = 0;
		this.y = 0;
		this.mFloor = 0;
	}

	/**
	 * Constructor Function
	 * 
	 * @param x
	 * @param y
	 */
	public Point(double x, double y) {
		super();
		this.x = x;
		this.y = y;
		this.mFloor = 0;
	}

	/**
	 * Constructor Function
	 * 
	 * @param x
	 * @param y
	 * @param mFloor
	 */
	public Point(double x, double y, int mFloor) {
		super();
		this.x = x;
		this.y = y;
		this.mFloor = mFloor;
	}

	/**
	 * Constructor Function
	 * 
	 * @param x
	 * @param y
	 * @param mFloor
	 */
	public Point(double x, double y, int mFloor, double prob) {
		super();
		this.x = x;
		this.y = y;
		this.mFloor = mFloor;
		this.prob = prob;
	}

	/**
	 * Constructor Function
	 * 
	 * @param point
	 */
	public Point(Point point) {
		super();
		this.x = point.x;
		this.y = point.y;
		this.mFloor = point.mFloor;
		// ---
		this.curPar = point.curPar;
		this.eDist2Doors = point.eDist2Doors; //can be shared as location does not change
//		if (point.eDist2Doors != null)
//			this.eDist2Doors = Arrays.copyOf(point.eDist2Doors, point.eDist2Doors.length);
		/// ----
	}

	/**
	 * test if it is equal to another Point
	 * 
	 * @param another
	 * @return True shows that it is equal to another Point
	 */
	public boolean isEqual(Point another) {
		if ((Math.abs(x - another.x) < Constant.epsi) && (Math.abs(y - another.y) < Constant.epsi))
			return true;
		else
			return false;
	}

	/**
	 * Euclidean Distance between two Point
	 * 
	 * @param another
	 * @return eDist
	 */
	public double eDist(Point another) {
		return Math.sqrt(Math.pow(x - another.x, 2) + Math.pow(y - another.y, 2));
	}

	public double eDist(SampledPoint another) {
		return Math.sqrt(Math.pow(x - another.getSampledX(), 2) + Math.pow(y - another.getSampledY(), 2));
	}

	/**
	 * moving a Point by offset
	 * 
	 * @param delta_x offset in x-axis
	 * @param delta_y offset in y-axis
	 */
	public void offset(double delta_x, double delta_y) {
		x += delta_x;
		y += delta_y;
	}

	/**
	 * reflection of a point
	 * 
	 * @param axis  decide the x-axis or y-axis
	 * @param pivot
	 */
	public void reflection(int axis, int pivot) {
		int x0, y0;
		if (0 == axis) {
			x0 = pivot;
			x = x0 + (x0 - x);
		}
		if (1 == axis) {
			y0 = pivot;
			y = y0 + (y0 - y);
		}
	}

	/**
	 * rotation of a point
	 * 
	 * @param pivot
	 * @param angle
	 */
	public void rotate(Point pivot, double angle) {
		double cx = x;
		double cy = y;

		double x0 = pivot.x;
		double y0 = pivot.y;

		double xv = (cx - x0) * Math.cos(angle) - (cy - y0) * Math.sin(angle) + x0;
		double yv = -(cx - x0) * Math.sin(angle) + (cy - y0) * Math.cos(angle) + y0;

		BigDecimal b = new BigDecimal(xv);
		x = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

		b = new BigDecimal(yv);
		y = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

	}

	/**
	 * get current belonging partition
	 * 
	 * @return current belonging partitions (null as not find one)
	 */
	public Par getCurrentPar() {
		if (curPar == null) {
//			System.out.println(1 / 0);
			Iterable<Par> RectArray = IndoorSpace.gRTree.find(this.x, this.y, this.x, this.y);
			Iterator<Par> itr = RectArray.iterator();
			if (itr.hasNext()) {
				int index = itr.next().getmID() + this.mFloor * IndoorSpace.gNumberParsPerFloor;
				Par resPar = IndoorSpace.gPartitions.get(index);
				// System.out.println("Current Partition #" + resPar.getmID());
				curPar = resPar;
				return resPar;
			} else {
				return null;
			}
		} else {
			return curPar;
		}
	}

	public void setCurrentPar(Par par) {
		this.curPar = par;
	}

	/**
	 * get sampled points
	 * 
	 * @return ArrayList<Point>
	 */
	public ArrayList<Point> getSampledPoints() {

		ArrayList<Point> sampledPoints = new ArrayList<Point>();
		sampledPoints.add(new Point(x, y, mFloor));
		return sampledPoints;
		// -------------------------------------------------
		/// no need random in our case

//		int insideSampledCount = 0;
////		Random random = new Random();
//		Random random = AlgCSDM.random;
//		int randomCount = random.nextInt(DataGenConstant.SAMPLE_SIZE) + 1;
//		// System.out.println("randomCount:" + randomCount);
//		while (insideSampledCount < randomCount) { // find a number of SAMPLE_SIZE sampled points
//
//			// the walking direction
//			double randomDegree = random.nextDouble() * 2 * Math.PI;
//
//			// the walking distance
//			double randomRadius = random.nextDouble() * DataGenConstant.UNCERTAINTY_RADIUS;
//
//			double randomPointX = this.x + randomRadius * Math.cos(randomDegree);
//			double randomPointY = this.y + randomRadius * Math.sin(randomDegree);
//
//			// randomly get a sampled point
//			Point point = new Point(randomPointX, randomPointY, this.mFloor);
//
//			if (Functions.isInBuilding(point)) {
//				sampledPoints.add(point);
//				insideSampledCount++;
//			}
//		}
//
//		return sampledPoints;
	}

	public static Point parse(String string) {
		String subString = string.substring(1, (string.length() - 1));
		// System.out.println(subString);
		String[] items = (subString).split(",");
		if (items.length == 3) {
			return new Point(Double.valueOf(items[0]), Double.valueOf(items[1]), Integer.valueOf(items[2]));
		} else
			return null;
	}

	/**
	 * randomly decide a next possible position within the maxDistance
	 * 
	 * @param maxDistance the maximum distance the object can walk within next time
	 *                    interval
	 * @return An array consists of two points (on the different halves of a circle)
	 */
	public Point[] randomWalk(double maxDistance) {
		// TODO Auto-generated method stub
		Point[] randomPointPair = new Point[2];
//		Random random = new Random();
		Random random = AlgCSDM.random;

		// the walking distance
		double walkDist = random.nextDouble() * maxDistance;

		// the walking direction
		double walkAngle = random.nextDouble() * 2 * Math.PI;

		randomPointPair[0] = new Point(this.x + walkDist * Math.cos(walkAngle), this.y + walkDist * Math.sin(walkAngle),
				this.mFloor);
		randomPointPair[1] = new Point(this.x + walkDist * Math.cos(walkAngle + Math.PI),
				this.y + walkDist * Math.sin(walkAngle + Math.PI), this.mFloor);

		return randomPointPair;
	}

	/**
	 * randomly decide a next possible position within the maxDistance and also more
	 * far away from the staircase
	 * 
	 * @param maxDistance the maximum distance the object can walk within next time
	 *                    interval
	 * @return An array consists of two points (on the different halves of a circle)
	 */
	public Point[] randomWalkAway(double maxDistance) {
		// TODO Auto-generated method stub
		Point[] randomPointPair = new Point[2];
//		Random random = new Random();
		Random random = AlgCSDM.random;

		// the walking distance is 0.9 - 1 * maxDistInterval, ensure object walk a long
		// distance within an interval
		double walkDist = (random.nextDouble() * (maxDistance * 0.1)) + (maxDistance * 0.9);

		// the walking direction
		double walkAngle = random.nextDouble() * 2 * Math.PI;

		randomPointPair[0] = new Point(this.x + walkDist * Math.cos(walkAngle), this.y + walkDist * Math.sin(walkAngle),
				this.mFloor);
		randomPointPair[1] = new Point(this.x + walkDist * Math.cos(walkAngle + Math.PI),
				this.y + walkDist * Math.sin(walkAngle + Math.PI), this.mFloor);

		return randomPointPair;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the mFloor
	 */
	public int getmFloor() {
		return mFloor;
	}

	/**
	 * @param mFloor the mFloor to set
	 */
	public void setmFloor(int mFloor) {
		this.mFloor = mFloor;
	}

	public double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public double geteDist2Door(int pos) {
		if (eDist2Doors == null) {
			List<Integer> doors = getCurrentPar().getmDoors();
			eDist2Doors = new double[doors.size()];
			for (int i = 0; i < doors.size(); i++) {
				eDist2Doors[i] = eDist(IndoorSpace.gDoors.get(doors.get(i)));
			}
		}
		return eDist2Doors[pos];
	}

	public void seteDist2Doors(double[] eDist2Doors) {
		this.eDist2Doors = eDist2Doors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.khelekore.prtree.PointND#getDimensions()
	 */
	@Override
	public int getDimensions() {
		// TODO Auto-generated method stub
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.khelekore.prtree.PointND#getOrd(int)
	 */
	@Override
	public double getOrd(int axis) {
		// TODO Auto-generated method stub
		if (0 == axis) {
			return this.x;
		} else if (1 == axis) {
			return this.y;
		} else
			return -1;
	}

	/**
	 * toString
	 * 
	 * @return (x, y,mFloor)
	 */
	public String toString() {
		String tempString = "(" + this.x + "," + this.y + "," + this.mFloor+"," + prob+ ")";
		return tempString;
	}

	public String uniqueCode() {
		return this.getCurrentPar().getmID() + "*" + this.x + "*" + this.y + "*" + this.mFloor;
	}

}
