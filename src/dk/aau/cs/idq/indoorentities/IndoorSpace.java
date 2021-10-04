
package dk.aau.cs.idq.indoorentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.khelekore.prtree.PRTree;

import dk.aau.cs.idq.utilities.DataGenConstant;

/**
 * IndoorSpace models the indoor space for the whole life span
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public class IndoorSpace {

	/************************************************
	 * INDOOR ENTITIES
	 ************************************************/
	/** all the doors of the indoor space */
	public static List<Door> gDoors = new ArrayList<Door>();

	/** all the partitions of the indoor space */
	public static List<Par> gPartitions = new ArrayList<Par>();

	/** all the moving objects of the indoor space */
	public static List<IdrObj> gIdrObjs = new ArrayList<IdrObj>();

	/** all the sampled points */
	public static List<SampledPoint> gSampledPoints = new ArrayList<SampledPoint>();

	/************************************************
	 * DATA STRUCTURES
	 ************************************************/
	/** the in-memory R-Tree */
	public static PRTree<Par> gRTree = new PRTree<Par>(new RectConverter(), DataGenConstant.RTree_BranchFactor);

	/** the in-memory D2D Matrix */
	public static HashMap<String, D2Ddistance> gD2D = new HashMap<String, D2Ddistance>();

	/** the in-memory D2D Matrix */
	public static double[][] gD2DMatrix = new double[220][220];

	/** the in-memory P2P Matrix */
	public static int[][] gP2PMatrix = new int[141][141];

	/** the in-memory P2P Matrix */
	public static int[][][] gP2PMatrix2 = new int[141][141][10];

	
	/** the in-memory Objects Size Table */
	public static SortedMap<Integer, Integer> sizeObjsTable = new TreeMap<Integer, Integer>();

	/** current observed indoor objects */
	/// <objID, Obj>
//	public static SortedMap<Integer, IdrObj> observedObj = new TreeMap<Integer, IdrObj>();

	/// current observed indoor objects separated by floors
	public static ArrayList<ArrayList<IdrObj>> observedObjsList = new ArrayList<ArrayList<IdrObj>>(
			DataGenConstant.nFloor);


	/** OTT */
	/// <objID, recordTime>
//	public static Hashtable<Integer, Integer> OTT = new Hashtable<Integer, Integer>();
	public static HashMap<Integer, Integer> OTT = new HashMap<Integer, Integer>();
//	public static ArrayList<ConcurrentHashMap<Integer, Integer>> OTT = new ArrayList<ConcurrentHashMap<Integer, Integer>>();

	/** Ground Truth */
//	public static Hashtable<Integer, Integer> GroundTruth = new Hashtable<Integer, Integer>();

	/************************************************
	 * COUNTER
	 ************************************************/
	/** the Number of Doors per floor */
	public static int gNumberDoorsPerFloor;

	/** the Number of Partitions per floor */
	public static int gNumberParsPerFloor;


}
