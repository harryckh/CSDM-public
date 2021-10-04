

package dk.aau.cs.idq.indoorentities;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * AccsGraph
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public class AccsGraph {

	public static SortedMap<Integer, Door> Nodes = new TreeMap<Integer, Door>();				// the Graph Nodes (each as a Door)

	public static SortedMap<Integer, Par> Partitions = new TreeMap<Integer, Par>();				// the Graph Edges (each as a Partition)

	/**
	 * delete the note from the graph by nodeID
	 * 
	 * @param nodeID
	 * @return False as failure
	 */
	public static boolean delNode(int nodeID) {
		Iterator<Integer> itr = Nodes.keySet().iterator();
		while (itr.hasNext()) {
			int index = itr.next();
			if (nodeID == index) {
				Nodes.remove(index);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * get the note from the graph by nodeID
	 * 
	 * @param nodeID
	 * @return the found door
	 */
	public static Door findNode(int nodeID){
		Door findDoor = null;
		Iterator<Integer> itr = Nodes.keySet().iterator();
		while (itr.hasNext()) {
			int index = itr.next();
			if (nodeID == index) {
				findDoor = Nodes.get(index);
				break;
			}
		}
		return findDoor;
	}
	
	/**
	 * delete the partition from the graph by parID
	 * 
	 * @param parID
	 * @return False as failure
	 */
	public static boolean delPar(int parID) {
		Iterator<Integer> itr = Partitions.keySet().iterator();
		while (itr.hasNext()) {
			int index = itr.next();
			if (parID == index) {
				Partitions.remove(index);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * get the partition from the graph by parID
	 * @param parID
	 * @return the found partition
	 */
	public static Par findPar(int parID){
		Par findPar = null;
		Iterator<Integer> itr = Partitions.keySet().iterator();
		while (itr.hasNext()) {
			int index = itr.next();
			if (parID == index) {
				findPar = Partitions.get(index);
				break;
			}
		}
		return findPar;
	}
	
	

}
