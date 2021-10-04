
package dk.aau.cs.idq.utilities;

import java.math.BigDecimal;

import dk.aau.cs.idq.indoorentities.Rect;

/**
 * Constant Values in Data Generating
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public class DataGenConstant {

	/************************************************
	 * PARAMETERS FOR INDOOR SPACES
	 ************************************************/
	public static int scale = 10; // integer here.

	/** dimensions of the floor */
	public static double floorRangeX = 60.0 * scale;
	public static double floorRangeY = 60.0 * scale;

	/** numbers of the floor */
	public static int nFloor = 20;
//	public static int nFloor = 2;

	/** length of stairway between two floors */
	public static double lenStairway = 4; //follow tkde14

	/** length of stairway of the whole building */
	public static double buildingHeight = lenStairway * (nFloor - 1);

	/** four outdoor rectangles */
	public static Rect outRectTop = new Rect(10 * scale, 50 * scale, 0 * scale, 5 * scale);
	public static Rect outRectBottom = new Rect(10 * scale, 50 * scale, 55 * scale, 60 * scale);
	public static Rect outRectLeft = new Rect(0 * scale, 5 * scale, 10 * scale, 50 * scale);
	public static Rect outRectRight = new Rect(55 * scale, 60 * scale, 10 * scale, 50 * scale);

	/************************************************
	 * PARAMETERS FOR GENERATING INDOOR OBJECTS
	 ************************************************/
	/** total life span (unit: second) */
//	public static int totalLifecycle = 7200;
	public static int totalLifecycle = 60;

	/** numbers of generated objects per floor */
	public static int nObjPerFloor = 500;
//	public static int nObjPerFloor = 10;

	/** numbers of generated objects in total */
	public static int nObjects = nFloor * nObjPerFloor;

	/** numbers of new added objects per second */
//	public static int newEnterObjects = nObjPerFloor/10;
	public static int newEnterObjects = 1; // follow TKDE18
//	public static int newEnterObjects = 0; 

	/************************************************
	 * ID COUNTERS FOR INDOOR ENTITIES
	 ************************************************/
	/** the ID counter of Partitions */
	public static int mID_Par = 0;

	/** the ID counter of Doors */
	public static int mID_Door = 0;

	/** the ID counter of Indoor Objects */
	public static int mID_IdrObj = 0;

	/** the ID counter of Queries */
	public static int mID_Queries = 0;

	/** the ID counter of SampledPoints */
	public static int mID_SampledPoints = 0;

	/************************************************
	 * PARAMETERS FOR R-TREE
	 ************************************************/
	/** branch factor of the R-Tree */
	public static int RTree_BranchFactor = 20;

	/************************************************
	 * PARAMETERS FOR MOVING OF INDOOR OBJECTS
	 ************************************************/
	/** maximum moving speed of indoor objects */
	public static double maxVelocity = 1;// follow TKDE18

	/** time interval (second) */
	public static int atomicInterval = 1;

	/** the longest distance that object can move to in the next time interval */
	public static double maxAtomicWalkingDistance = maxVelocity * atomicInterval;

	/** probability of entering the stairway for a random-walking object */
	public static int probEnterStairway = 70;

	/************************************************
	 * PARAMETERS FOR UNCERTAIN SAMPLING OF INDOOR OBJECTS
	 ************************************************/
	/** margin for uncertain box */
	public static double UNCERTAINTY_SIZE = 0.25;

	/** radius for uncertain region */
	public static double UNCERTAINTY_RADIUS = 0.25;

	/** number of sampling points in uncertain box */
	public static int SAMPLE_SIZE = 1;
//	public static int SAMPLE_SIZE = 1;

	/** contributes of each sampling point in uncertain box */
	public static double percentContributes = new BigDecimal((1.0 / SAMPLE_SIZE)).setScale(2, BigDecimal.ROUND_HALF_UP)
			.doubleValue();

	/************************************************
	 * PARAMETERS FOR DRAWING THE FLOOR
	 ************************************************/
	/** the zoom level : enlarge the whole floor */
	public static int zoomLevel = 1;

}
