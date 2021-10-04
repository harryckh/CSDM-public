
package dk.aau.cs.idq.indoorentities;

import org.khelekore.prtree.PointND;

/**
 * an interface Range
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public interface Range {
	
	/** get minimum distance between the Range and one Point */
	public abstract double getMinDist(PointND point);
	
	/** get maximum distance between the Range and one Point */
	public abstract double getMaxDist(PointND point);
	
	/** reflection for a given axis and a given pivot */
	public abstract void reflection(int axis, int pivot);
	

}
