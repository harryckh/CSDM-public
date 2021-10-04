
package dk.aau.cs.idq.indoorentities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;

/**
 * Door
 * to describe a door 
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see dk.aau.cs.idq.indoorentities.Point
 *
 */
public class Door extends Point implements Comparable<Object> {

	private int mID;																// the ID

	private Door previousDoor;														// the previous Door for routing
	
	private List<Integer> mPartitions = new ArrayList<Integer>();					// the accessible partitions

	private double distFromStart;

	/**
	 * Constructor Function
	 * 
	 * @param x
	 * @param y
	 */
	public Door(double x, double y) {
		super(x, y, 0);
		this.previousDoor = null;
		this.distFromStart = Constant.DISTMAX;
		this.mID = ++DataGenConstant.mID_Door;
	}

	/**
	 * Constructor Function
	 * 
	 * @param x
	 * @param y
	 * @param mID
	 */
	public Door(double x, double y, int mID) {
		super(x, y, 0);
		this.mID = mID;
		this.previousDoor = null;
		this.distFromStart = Constant.DISTMAX;
	}

	/**
	 * Constructor Function
	 * 
	 * @param x
	 * @param y
	 * @param mFloor
	 * @param mID
	 */
	public Door(double x, double y, int mFloor, int mID) {
		super(x, y, mFloor);
		this.mID = mID;
		this.previousDoor = null;
		this.distFromStart = Constant.DISTMAX;
	}

	/**
	 * Constructor Function
	 * 
	 * @param another is a point
	 */
	public Door(Point another) {
		super(another);
		this.previousDoor = null;
		this.distFromStart = Constant.DISTMAX;
		this.mPartitions = new ArrayList<Integer>();
	}

	/**
	 * Constructor Function
	 * 
	 * @param anotherDoor is a door
	 */
	public Door(Door anotherDoor) {
		super(anotherDoor.getX(), anotherDoor.getY(), anotherDoor.getmFloor());
		this.mID = ++DataGenConstant.mID_Door;
		this.previousDoor = anotherDoor.getPreviousDoor();
		this.mPartitions = anotherDoor.getmPartitions();
		this.distFromStart = anotherDoor.getDistFromStart();
	}

	/**
	 * add a accessible partition for this door
	 * 
	 * @param parID
	 */
	public void addPar(int parID) {
		if (!this.mPartitions.contains(parID)) {
			this.mPartitions.add(parID);
		}
	}

	/**
	 * toString
	 * 
	 * @return mID+x+y+mFloor+mPars
	 */
	public String toString() {
		String outputString = this.getmID() + " " + this.getX() + " "
				+ this.getY() + " " + this.getmFloor() + " ";

		Iterator<Integer> itr = this.mPartitions.iterator();

		while (itr.hasNext()) {
			outputString = outputString + itr.next() + " ";
		}

		return outputString;
	}

	/**
	 * @return the mID
	 */
	public int getmID() {
		return mID;
	}

	/**
	 * @param mID
	 *            the mID to set
	 */
	public void setmID(int mID) {
		this.mID = mID;
	}

	/**
	 * @return the previousDoor
	 */
	public Door getPreviousDoor() {
		return previousDoor;
	}

	/**
	 * @param previousDoor
	 *            the previousDoor to set
	 */
	public void setPreviousDoor(Door previousDoor) {
		this.previousDoor = previousDoor;
	}

	/**
	 * @return the mPartitions
	 */
	public List<Integer> getmPartitions() {
		return mPartitions;
	}

	/**
	 * @param mPartitions
	 *            the mPartitions to set
	 */
	public void setmPartitions(List<Integer> mPartitions) {
		this.mPartitions = mPartitions;
	}

	/**
	 * @return the distFromStart
	 */
	public double getDistFromStart() {
		return distFromStart;
	}

	/**
	 * @param distFromStart
	 *            the distFromStart to set
	 */
	public void setDistFromStart(double distFromStart) {
		this.distFromStart = distFromStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Door otherdoor = (Door) arg0;
		return this.mID > otherdoor.mID ? 1 : (this.mID == otherdoor.mID ? 0
				: -1);
	}

	/**
	 * get the direct-touchable <door, partition>
	 *
	 */
	public void genLeaveablePar() {
		// TODO Auto-generated method stub
		
		Iterator<Integer> itrmPars = this.mPartitions.iterator();
		Object[] itemArray = this.mPartitions.toArray();
		while (itrmPars.hasNext()) {
			int index = itrmPars.next();
			Par par = AccsGraph.Partitions.get(index);
			//System.out.print("doorID: " + this.mID + " parID: " + index + " ");
			for (int i = 0; i < itemArray.length; i++) {
				int item = (int) itemArray[i];
				if (item != index) {
					//System.out.print(item + " ");
					par.getLeaveablePars().add(new LeavePair(this.mID, item));
				}
			}
			//System.out.println();
		}
	}

	public void scale(int scale) {
		// TODO Auto-generated method stub
		setX(getX()*scale);
		setY(getY()*scale);
	}

}
