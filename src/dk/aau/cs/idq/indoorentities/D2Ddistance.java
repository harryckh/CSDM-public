
package dk.aau.cs.idq.indoorentities;

import java.io.Serializable;

/**
 * D2Ddistance
 * Euclidean distance between two given doors
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see java.lang.Comparable
 */
public class D2Ddistance implements Comparable<Object>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2234412950634730987L;

	private int doorID_1;					// the doorID 1
	
	private int doorID_2;					// the doorID 2
	
	private double distance;				// the calculated distance

	/**
	 * @param doorID_1
	 * @param doorID_2
	 * @param distance
	 */
	public D2Ddistance(int doorID_1, int doorID_2, double distance) {
		super();
		this.doorID_1 = doorID_1;
		this.doorID_2 = doorID_2;
		this.distance = distance;
	}

	/**
	 * @return the doorID_1
	 */
	public int getDoorID_1() {
		return doorID_1;
	}

	/**
	 * @param doorID_1 the doorID_1 to set
	 */
	public void setDoorID_1(int doorID_1) {
		this.doorID_1 = doorID_1;
	}

	/**
	 * @return the doorID_2
	 */
	public int getDoorID_2() {
		return doorID_2;
	}

	/**
	 * @param doorID_2 the doorID_2 to set
	 */
	public void setDoorID_2(int doorID_2) {
		this.doorID_2 = doorID_2;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public String toString(){
		String tempString = this.doorID_1 + "#" + this.doorID_2 + " = " + this.distance;
		return tempString;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		D2Ddistance another = (D2Ddistance)o;
		
		if(this.distance < another.distance){
			return -1;
		}else if(this.distance == another.distance){
			if(this.doorID_2 < another.doorID_2){
				return -1;
			}else if(this.doorID_2 == another.doorID_2){
				return 0;
			}else{
				return 1;
			} 
		}else{
			return 1;
		}
		
	}
	
	

}
