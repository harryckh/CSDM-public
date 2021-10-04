
package dk.aau.cs.idq.indoorentities;

import java.io.Serializable;

/**
 * LeavePair
 * to describe the direct-touchable partitions
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see java.lang.Comparable
 *
 */
public class LeavePair implements Comparable<Object>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8665981856084827438L;

	private int mDoorID;				// the through door to touch one partition
	
	private int mParID;					// the direct-touchable partition
	

	/**
	 * Constructor Function
	 * 
	 * @param mDoorID
	 * @param mParID
	 */
	public LeavePair(int mDoorID, int mParID) {
		super();
		this.mDoorID = mDoorID;
		this.mParID = mParID;
	}

	/**
	 * @return the mDoorID
	 */
	public int getmDoorID() {
		return mDoorID;
	}

	/**
	 * @param mDoorID the mDoorID to set
	 */
	public void setmDoorID(int mDoorID) {
		this.mDoorID = mDoorID;
	}

	/**
	 * @return the mParID
	 */
	public int getmParID() {
		return mParID;
	}

	/**
	 * @param mParID the mParID to set
	 */
	public void setmParID(int mParID) {
		this.mParID = mParID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		LeavePair other = (LeavePair) o;
		if(this.mDoorID > other.mDoorID)
			return 1;
		else if (this.mDoorID == other.getmDoorID()){
			return this.mParID > other.mParID ? 1 : (this.mParID == other.mParID ? 0 : -1);
		}else{
			return -1;
		}
	}
	
	/**
	 * toString
	 * 
	 * @return <mDoorID, mParID>
	 */
	public String toString(){
		String tempString = "<" + this.mDoorID + " , " + this.mParID + "> ";
		return tempString;
	}

	/**
	 * test this LeavePair can be fully covered during the walking period 
	 * 
	 * @param remainDist
	 * @return a boolean value
	 */
	public double canBeFullyCovered(double remainDist) {
		// TODO Auto-generated method stub
		Door curDoor = IndoorSpace.gDoors.get(this.getmDoorID());
		Par curPar = IndoorSpace.gPartitions.get(this.getmParID());
		
		double maxDist = curPar.getMaxDist(curDoor);
		
		if(maxDist <= remainDist){
			return remainDist - maxDist;
		}else
			return -1;
	}

	
}
