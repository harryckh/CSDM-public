package dk.aau.cs.idq.indoorentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import algorithm.AlgCSDM;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;

/**
 * Staircase Point to describe a object's position when going upstairs or
 * downstairs
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see org.khelekore.prtree.DistanceCalculator
 *
 */
public class StaircasePoint {

	/** the ID of partitions in the 1st floor */
	private static int[] pIDs = { 128, 129, 130, 131 };

	/** the ID of doors to the staircase in the 1st floor */
	private static int[] dIDs = { 208, 210, 213, 215 };  //{ 204, 206, 209, 211 };

	private int mStaircaseID; // the entered staircaseID 1LEFT 2TOP 3BOTTOM
								// 4RIGHT

	private double mOffset; // the offset from the ground

	private int moveDirection; // the moving direction when entering the
								// staircase

	private int destFloor; // the destination floor of this staircase point

	private ArrayList<StaircasePoint> mSampledPoints; // the sampled points

	/**
	 * Constructor Function
	 * 
	 * @param mStaircaseID
	 *            the entered staircaseID 1LEFT 2TOP 3BOTTOM 4RIGHT
	 * @param mOffset
	 *            the offset from the ground
	 * @param destFloor
	 *            the destination floor
	 * @param moveDirection
	 *            the possible out floor of next time interval
	 */
	public StaircasePoint(int mStaircaseID, double mOffset, int destFloor,
			int moveDirection) {
		super();
		this.mStaircaseID = mStaircaseID;
		this.mOffset = mOffset;
		this.destFloor = destFloor;
		this.moveDirection = moveDirection;
		this.mSampledPoints = null;
	}

	/**
	 * the object walk within next time interval staying the current staircase
	 * 
	 */
	public void moveInStairWay() {
		// TODO Auto-generated method stub

		// make sure that the distance will be larger than 0.9*MaxDistInterval
		double randomOffset = this.moveDirection
				* ((AlgCSDM.random).nextDouble() * 0.1 + 0.9)
				* DataGenConstant.maxAtomicWalkingDistance;
		this.mOffset = this.mOffset + randomOffset;
		if (this.mOffset < 0) { // can only move upstairs
			this.mOffset = Math.abs(this.mOffset);
		}
		if (this.mOffset > DataGenConstant.buildingHeight) { // can only move
																// downstairs
			this.mOffset = 2 * DataGenConstant.buildingHeight - this.mOffset;
		}

	}

	/**
	 * get the next entered partition
	 * 
	 * @return the next entered partition
	 */
	public int getNextEnteredPar() {
		// TODO Auto-generated method stub
		return pIDs[(this.mStaircaseID - 1)] + this.destFloor
				* IndoorSpace.gNumberParsPerFloor;
	}

	/**
	 * get the next entered partition
	 * 
	 * @return the next entered door
	 */
	public int getNextEnteredDoor() {
		// TODO Auto-generated method stub
		return dIDs[(this.mStaircaseID - 1)] + this.destFloor
				* IndoorSpace.gNumberDoorsPerFloor;
	}

	/**
	 * get the probabilistic sampled result for a Staircase Point
	 * 
	 * @return sampledProbMap HashMap<Integer, Double> the probabilistic sampled
	 *         result for each Partition
	 */
	public HashMap<Integer, Double> getSampledProb() {
		// TODO Auto-generated method stub

		// this.mSampledPoints = getSampledPoints();
		HashMap<Integer, Double> sampledProbMap = new HashMap<Integer, Double>();

		if (this.mSampledPoints != null) {

			int lowerfloor = (int) (this.mOffset / DataGenConstant.lenStairway); // the lower floor of current position

			double middlePoint = (lowerfloor + 0.5)
					* DataGenConstant.lenStairway; // the middle point of the
													// lower floor and upper
													// floor

			// make sure that uncertain box would not be < 0 or >
			// building_height
			double maxUncertain = Math.min(this.mOffset
					+ DataGenConstant.UNCERTAINTY_SIZE,
					DataGenConstant.buildingHeight);
			double minUncertain = Math.max(this.mOffset
					- DataGenConstant.UNCERTAINTY_SIZE, 0);

			if (maxUncertain < middlePoint) { // all the sampled points are
												// belong to the lower floor
				int parIndex = pIDs[(this.mStaircaseID - 1)] + (lowerfloor)
						* IndoorSpace.gNumberParsPerFloor;
				sampledProbMap.put(parIndex, 1.0);
			} else if (minUncertain > middlePoint) { // all the sampled points
														// are belong to the
														// upper floor
				int parIndex = pIDs[(this.mStaircaseID - 1)] + (lowerfloor + 1)
						* IndoorSpace.gNumberParsPerFloor;
				sampledProbMap.put(parIndex, 1.0);
			} else {
				double tempProb1 = 0;
				double tempProb2 = 0;
				for (StaircasePoint item : this.mSampledPoints) {
					if (item.mOffset > middlePoint) {
						tempProb2 = tempProb2
								+ DataGenConstant.percentContributes;
					} else {
						tempProb1 = tempProb1
								+ DataGenConstant.percentContributes;
					}
				}

				tempProb1 = new BigDecimal(tempProb1).setScale(2,
						BigDecimal.ROUND_HALF_UP).doubleValue();
				tempProb2 = new BigDecimal(tempProb2).setScale(2,
						BigDecimal.ROUND_HALF_UP).doubleValue();

				if (tempProb1 != 0) {
					int parIndex = pIDs[(this.mStaircaseID - 1)]
							+ (lowerfloor + 1)
							* IndoorSpace.gNumberParsPerFloor;
					sampledProbMap.put((parIndex), tempProb1);
				}
				if (tempProb2 != 0) {
					int parIndex = pIDs[(this.mStaircaseID - 1)] + (lowerfloor)
							* IndoorSpace.gNumberParsPerFloor;
					sampledProbMap.put((parIndex), tempProb2);
				}

			}
		}
		return sampledProbMap;
	}

	/**
	 * get the distance between the destination door and the current position
	 * 
	 * @return DISTMAX if can not move out for the next moment
	 */
	public double getOffsetOutStaircase() {

		double offset = this.destFloor * DataGenConstant.lenStairway
				- this.mOffset;
		if (this.moveDirection > 0 && offset > 0
				&& offset < DataGenConstant.maxAtomicWalkingDistance)
			return DataGenConstant.maxAtomicWalkingDistance - offset;
		if (this.moveDirection < 0 && offset < 0
				&& offset > -DataGenConstant.maxAtomicWalkingDistance)
			return DataGenConstant.maxAtomicWalkingDistance + offset;
		return Constant.DISTMAX;
	}

	/**
	 * get sampled points
	 * 
	 * @return ArrayList<Double>
	 */
	public ArrayList<StaircasePoint> getSampledPoints() {

		ArrayList<StaircasePoint> sampledPoints = new ArrayList<StaircasePoint>();
		int insideSampledCount = 0;

		// make sure that uncertain box would not be < 0 or > building_height
		double maxUncertain = Math.min(this.mOffset
				+ DataGenConstant.UNCERTAINTY_SIZE,
				DataGenConstant.buildingHeight);
		double minUncertain = Math.max(this.mOffset
				- DataGenConstant.UNCERTAINTY_SIZE, 0);
		double rangeUncertain = maxUncertain - minUncertain;

		while (insideSampledCount < DataGenConstant.SAMPLE_SIZE) { // find a
																	// number of
																	// SAMPLE_SIZE
																	// sampled
																	// points
//			Random random = new Random();
			Random random = AlgCSDM.random;

			double position = random.nextDouble() * (rangeUncertain)
					+ minUncertain;
			StaircasePoint point = new StaircasePoint(this.mStaircaseID,
					position, this.destFloor, this.moveDirection);
			sampledPoints.add(point);
			insideSampledCount++;
		}

		return sampledPoints;
	}

	/**
	 * @return the mStaircaseID
	 */
	public int getmStaircaseID() {
		return mStaircaseID;
	}

	/**
	 * @param mStaircaseID
	 *            the mStaircaseID to set
	 */
	public void setmStaircaseID(int mStaircaseID) {
		this.mStaircaseID = mStaircaseID;
	}

	/**
	 * @return the mOffset
	 */
	public double getmOffset() {
		return mOffset;
	}

	/**
	 * @param mOffset
	 *            the mOffset to set
	 */
	public void setmOffset(double mOffset) {
		this.mOffset = mOffset;
	}

	/**
	 * @return the moveDirection
	 */
	public int getMoveDirection() {
		return moveDirection;
	}

	/**
	 * @param moveDirection
	 *            the moveDirection to set
	 */
	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	/**
	 * @return the destFloor
	 */
	public int getDestFloor() {
		return destFloor;
	}

	/**
	 * @param destFloor
	 *            the destFloor to set
	 */
	public void setDestFloor(int destFloor) {
		this.destFloor = destFloor;
	}

	/**
	 * @return the mSampledPoints
	 */
	public ArrayList<StaircasePoint> getmSampledPoints() {
		return mSampledPoints;
	}

	/**
	 * @param mSampledPoints
	 *            the mSampledPoints to set
	 */
	public void setmSampledPoints(ArrayList<StaircasePoint> mSampledPoints) {
		this.mSampledPoints = mSampledPoints;
	}

	/**
	 * @return tempString
	 */
	public String getSampledPointsString() {
		// TODO Auto-generated method stub

		this.mSampledPoints = getSampledPoints();

		String tempString = "";

		for (StaircasePoint item : this.mSampledPoints) {
			tempString = tempString + item + " ";
		}

		return tempString;
	}

	/**
	 * toString
	 * 
	 * @return tempString (mStaircaseID, mOffset)
	 */
	public String toString() {

		String tempString = "(" + this.mStaircaseID + "," + this.mOffset + ")";
		return tempString;
	}

}
