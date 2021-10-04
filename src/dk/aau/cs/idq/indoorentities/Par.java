package dk.aau.cs.idq.indoorentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.Functions;
import dk.aau.cs.idq.utilities.RoomType;

/**
 * Par
 * to describe an indoor entity - partition
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see dk.aau.cs.idq.indoorentities.Rect
 *
 */
public class Par extends Rect implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6701141365667669516L;

	private int mID;															// the ID

	private int mType = RoomType.ROOM; 											// room = 0; hallway = 1; staircase =2;

	private List<Integer> mDoors = new ArrayList<Integer>();					// the linked Doors

	private List<LeavePair> leaveablePars = new ArrayList<LeavePair>();			// the Partitions that this partition can touch for the next interval

//	private List<SampledPoint> mSampledPoints = new ArrayList<SampledPoint>();	// the indoor objects in the partition for the moment

	private HashMap<String, D2Ddistance> d2dHashMap;							// the distance between the relevant doors of this partition	
	
	private List<Point> MCPoints = new ArrayList<Point>();
	
	public static double mSize = 0.1;

	/**
	 * Constructor Function
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 */
	public Par(double x1, double x2, double y1, double y2) {
		super(x1, x2, y1, y2);
	}

	/**
	 * Constructor Function
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param type
	 */
	public Par(double x1, double x2, double y1, double y2, int type) {
		super(x1, x2, y1, y2);
		this.mID = ++DataGenConstant.mID_Par;
		this.setmType(type);
	}

	/**
	 * Constructor Function
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param mID
	 * @param type
	 */
	public Par(double x1, double x2, double y1, double y2, int mID, int type) {
		super(x1, x2, y1, y2);
		this.mID = mID;
		this.setmType(type);
	}

	/**
	 * Constructor Function
	 * 
	 * @param another
	 */
	public Par(Par another) {
		super(another.getX1(), another.getX2(), another.getY1(), another
				.getY2());
		this.setmType(another.getmType());
		this.setmFloor(another.getmFloor());
		this.setmSampledPoints(new ArrayList<SampledPoint>());
	}

	/**
	 * add a relevant door of this partition
	 * 
	 * @param doorID
	 */
	public void addDoor(int doorID) {
		if (!this.mDoors.contains(doorID)) {
			this.mDoors.add(doorID);
		}
	}
	
	/**
	 * add a contained object of this partition
	 * 
	 * @param sampledPoint
	 */
	public void addSampledObj(SampledPoint sampledPoint){
//		this.mSampledPoints.add(sampledPoint);
	}

	/**
	 * toString
	 * 
	 * @return mID+x1+x2+y1+y2+mFloor+mType+mDoors
	 */
	public String toString() {
		String outputString = this.getmID() + " " + this.getX1() + " "
				+ this.getX2() + " " + this.getY1() + " " + this.getY2() + " "
				+ this.getmFloor() + " " + this.getmType() + " ";

		Iterator<Integer> itr = this.mDoors.iterator();

		while (itr.hasNext()) {
			outputString = outputString + itr.next() + " ";
		}

		return outputString;
	}

	/**
	 * @return the mType
	 */
	public int getmType() {
		return mType;
	}

	/**
	 * @param mType
	 *            the mType to set
	 */
	public void setmType(int mType) {
		this.mType = mType;
	}

	/**
	 * @return the mDoors
	 */
	public List<Integer> getmDoors() {
		return mDoors;
	}

	/**
	 * @param mDoors
	 *            the mDoors to set
	 */
	public void setmDoors(List<Integer> mDoors) {
		this.mDoors = mDoors;
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
	 * @return the leaveablePars
	 */
	public List<LeavePair> getLeaveablePars() {
		return leaveablePars;
	}

	/**
	 * @param leaveablePars
	 *            the leaveablePars to set
	 */
	public void setLeaveablePars(List<LeavePair> leaveablePars) {
		this.leaveablePars = leaveablePars;
	}

	/**
	 * toString the touchable Partitions of this partition
	 * 
	 * @return tempString
	 */
	public String toStringLeaveablePars() {

		String tempString = " ";
		Iterator<LeavePair> itr = this.leaveablePars.iterator();

		while (itr.hasNext()){
			tempString = tempString + itr.next().toString();
		}
		return tempString;
	}

	/**
	 * @return the d2dHashMap
	 */
	public HashMap<String, D2Ddistance> getD2dHashMap() {
		d2dHashMap = new HashMap<String, D2Ddistance>();
		Collections.sort(this.mDoors);
		
		for (int i = 0; i < this.mDoors.size(); i++) {
			int index_1 = this.mDoors.get(i);
			Door door1 = IndoorSpace.gDoors.get(index_1);
			for (int j = i + 1; j < this.mDoors.size(); j++) {
				int index_2 = this.mDoors.get(j);
				Door door2 = IndoorSpace.gDoors.get(index_2);
				D2Ddistance d2dDist = new D2Ddistance(index_1, index_2, door1.eDist(door2));
				d2dHashMap.put(Functions.keyConventer(index_1, index_2), d2dDist);
			}
		}
		this.setD2dHashMap(d2dHashMap);
		return d2dHashMap;
	}

	/**
	 * @param d2dHashMap
	 *            the d2dHashMap to set
	 */
	public void setD2dHashMap(HashMap<String, D2Ddistance> d2dHashMap) {
		this.d2dHashMap = d2dHashMap;
	}
	
	/**
	 * test if the partition is a staircase, if it is, return the mStaircaseID
	 *  
	 * @return if it is, return the mStaircase, 128 -> 1, 129 -> 2, 130 -> 3, 131 -> 4
	 */
	public int inStairCase() {

		int mod = this.mID % IndoorSpace.gNumberParsPerFloor;

		if (mod >= 128 && mod <= 131) {
			return Math.abs(127 - mod);
		}

		return 0;
	}

	/**
	 * @return the mSampledPoints
	 */
	public List<SampledPoint> getmSampledPoints() {
//		return mSampledPoints;
		return null;
	}
	
	public List<Point> getMCPoints(){
		
		if(MCPoints.size() != 0){
			return this.MCPoints;
		}else
		{
			double xRange = this.getWidth();
			double yRange = this.getHeight();

			int xCount = (int) (xRange / mSize);
			int yCount = (int) (yRange / mSize);

			double startX = this.getX1();
			double startY = this.getY1();

			
			for (int i = 1; i < xCount; i++) {
				double cursorX = startX + mSize * i;
				for (int j = 1; j < yCount; j++) {
					double cursorY = startY + mSize * j;
					Point point = new Point(cursorX, cursorY,
							this.getmFloor());
					this.MCPoints.add(point);
				}
			}
			return this.MCPoints;
		}
	}

	/**
	 * @param mSampledPoints the mSampledPoints to set
	 */
	public void setmSampledPoints(List<SampledPoint> mSampledPoints) {
//		this.mSampledPoints = mSampledPoints;
	}
	
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Par other = (Par) o;
		return this.mID > other.mID ? 1
				: (this.mID == other.mID ? 0 : -1);

	}

	/**
	 * @param mCPoints the mCPoints to set
	 */
	public void setMCPoints(List<Point> mCPoints) {
		MCPoints = mCPoints;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Par other = (Par) obj;
		if (mID != other.mID)
			return false;
		return true;
	}

	public void scale(int s) {
		// TODO Auto-generated method stub
		setX1(getX1()*s);
		setX2(getX2()*s);
		setY1(getY1()*s);
		setY2(getY2()*s);
		
	}
	
	
	

}
