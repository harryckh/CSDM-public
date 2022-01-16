package dk.aau.cs.idq.indoorentities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import algorithm.AlgCSDM;
import algorithm.IndoorUR;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.WalkingType;

/**
 * IdrObj describe the Moving Indoor Object
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public class IdrObj {

	private int mID; 											// ID

	private Point mTruePos; 									// the position when it is a floor point

	private int mWalkingType = WalkingType.INFLOOR; 			// the walking type, default as Indoor 0

	private StaircasePoint mStairCasePos; 						// the position when it is a staircase point

	private Par curPar; 										// current belonging par (when it is a floor point)

	private boolean willOutStaircase; 							// True as will go out staircase

	private ArrayList<Point> mSampledPoints = null; 			// Sampled Points

	private HashMap<Integer, Double> UncertainSampledProb; 		// uncertain sampled result


	private double radius;
	//-----------
	private IndoorUR ur;
	
	private double[] d2sc;

	
	public IndoorUR getUr() {
		return ur;
	}

	public void setUr(IndoorUR ur) {
		this.ur = ur;
	}
	
	
	public double[] getD2sc() {
		return d2sc;
	}

	public void setD2sc(double[] d2sc) {
		this.d2sc = d2sc;
	}

	//-----------
	/**
	 * Constructor Function regard it as a floor point
	 * 
	 * @param mTruePos
	 */
	public IdrObj(Point mTruePos) {
		super();
		this.mID = ++DataGenConstant.mID_IdrObj;
		this.mTruePos = mTruePos;
		this.curPar = mTruePos.getCurrentPar();
	}

	/**
	 * Constructor Function regard it as a floor point
	 * 
	 * @param mID
	 * @param mTruePos
	 */
	public IdrObj(int mID, Point mTruePos) {
		super();
		this.mID = mID;
		this.mTruePos = mTruePos;
		this.curPar = mTruePos.getCurrentPar();
	}
	
	
	/**
	 * Constructor Function regard it as a floor point
	 * 
	 * @param mID
	 * @param mTruePos
	 */
	public IdrObj(int mID, Point mTruePos, Par curPar) {
		super();
		this.mID = mID;
		this.mTruePos = mTruePos;
		this.curPar = curPar;
	}

	/**
	 * Constructor Function regard it as a staircase point
	 * 
	 * @param mID
	 * @param mWalkingType
	 * @param mStairCasePos
	 */
	public IdrObj(int mID, int mWalkingType, StaircasePoint mStairCasePos) {
		this.setmWalkingType(mWalkingType);
		this.setmStairCasePos(mStairCasePos);
	}

	/**
	 * get extended touchable partitions with next interval
	 * 
	 * @param curDoor
	 *            the entrance of current partition
	 * @param curPar
	 *            the current partition
	 * @param onGoing
	 *            the distance can walk from the curDoor within this room
	 * @return mPossiblePars ArrayList for extended touchable partitions
	 */
	private List<NextPossiblePar> getExtendedNextPossiblePars(Door curDoor,
			Par curPar, double onGoing) {
		// TODO Auto-generated method stub

		List<NextPossiblePar> mPossiblePars = new ArrayList<NextPossiblePar>();
		Iterator<LeavePair> itr = curPar.getLeaveablePars().iterator(); // get all the direct-touchable partitions of current partition
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			Par nextPar = IndoorSpace.gPartitions
					.get(nextLeavePair.getmParID());
			double nextDist2Door = curDoor.eDist(nextDoor); // distance between current door and next though door
			if ((nextDist2Door != 0) && nextDist2Door < onGoing) { // if can walk through it
				NextPossiblePar nextPossiblePar = new NextPossiblePar(nextDoor,
						nextPar, (onGoing - nextDist2Door));
				mPossiblePars.add(nextPossiblePar);
				List<NextPossiblePar> extendedPossiblePars = getExtendedNextPossiblePars(
						nextDoor, nextPar, (onGoing - nextDist2Door));
				mPossiblePars.addAll(extendedPossiblePars);
			}
		}
		return mPossiblePars;
	}

	/**
	 * get the probabilistic sampled result for a Floor Point
	 * 
	 * @return sampledProbMap HashMap<Integer, Double> the probabilistic sampled
	 *         result for each Partition
	 */
	public HashMap<Integer, Double> getFloorPointSampledProb() {

		this.mSampledPoints = mTruePos.getSampledPoints();
		return null;
//		int spSize = this.mSampledPoints.size();
//		List<Double> contributes = genContributes(spSize);
//		HashMap<Integer, Double> sampledProbMap = new HashMap<Integer, Double>();
//		
//		if (this.mSampledPoints != null) {
//			int i = 0;
//			for (Point item : this.mSampledPoints) {
//				Par curPar = item.getCurrentPar();
//				SampledPoint sampledPoint = new SampledPoint(this.mID, item.getX(), item.getY(), item.getmFloor(), curPar, contributes.get(i));
//				IndoorSpace.gSampledPoints.add(sampledPoint);
//				curPar.addSampledObj(sampledPoint);
//				int curParID = curPar.getmID();
//				if (sampledProbMap.containsKey(curParID)) {
//					double tempProb = new BigDecimal(
//							sampledProbMap.get(curParID)
//									+ sampledPoint.getContributes())
//							.setScale(2, BigDecimal.ROUND_HALF_UP)
//							.doubleValue();
//					sampledProbMap.put(curParID, tempProb);
//				} else {
//					sampledProbMap.put(curParID,
//							sampledPoint.getContributes());
//				}
//				i++;
//			}
//		}
//		return sampledProbMap;
	}
	
	private List<Double> genContributes(int count) {
		// TODO Auto-generated method stub
		List<Double> contributes = new ArrayList<Double>();
		int[] contr = new int[count];
		Random random = new Random();
		for(int i = 0; i < 100; i++){
			int randint = random.nextInt(count);
			int temp = contr[randint];
			contr[randint] = temp + 1;
		}
		
		for(int j = 0; j < count; j++){
			double c = (double)contr[j] / 100;
			contributes.add(c);
		}
		
		return contributes;
	}

	/**
	 * get all the next touchable partitions within next interval iterated
	 * algorithm (call on Function getExtendedNextPossiblePars)
	 * 
	 * @return nextPossiblePars
	 */
	public List<NextPossiblePar> getNextPossiblePars() {

		List<NextPossiblePar> nextPossiblePars = new ArrayList<NextPossiblePar>();
		Iterator<LeavePair> itr = this.curPar.getLeaveablePars().iterator(); // get all the direct-touchable partition of current partition
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			Par nextPar = IndoorSpace.gPartitions
					.get(nextLeavePair.getmParID());
			double nextDist2Door = this.mTruePos.eDist(nextDoor);
			if (nextDist2Door < DataGenConstant.maxAtomicWalkingDistance) {
				NextPossiblePar nextPossiblePar = new NextPossiblePar(
						nextDoor,
						nextPar,
						(DataGenConstant.maxAtomicWalkingDistance - nextDist2Door));
				nextPossiblePars.add(nextPossiblePar);
				List<NextPossiblePar> extendedPossiblePars = getExtendedNextPossiblePars(
						nextDoor,
						nextPar,
						(DataGenConstant.maxAtomicWalkingDistance - nextDist2Door));
				nextPossiblePars.addAll(extendedPossiblePars);
			}
		}
		return nextPossiblePars;
	}

	/**
	 * get the distance between current position and the nearest door to the
	 * staircase
	 * 
	 * @param staircaseType
	 *            to show which staircase this point is approaching
	 * @return if can touch it return the distance, otherwise return DISTMAX
	 */
	public double possibleToStairCase(int staircaseType) {

		int stairDoorID = -1;
		double nextOffset = Constant.DISTMAX;
		
		switch (staircaseType) {
		case 1:
			// find left stairDoor
			stairDoorID = 208 + this.mTruePos.getmFloor()
					* IndoorSpace.gNumberDoorsPerFloor; //204
			break;
		case 2:
			// find top stairDoor
			stairDoorID = 210 + this.mTruePos.getmFloor()
					* IndoorSpace.gNumberDoorsPerFloor; //206
			break;
		case 3:
			// find bottom stairDoor
			stairDoorID = 213 + this.mTruePos.getmFloor()
					* IndoorSpace.gNumberDoorsPerFloor; //209
			break;
		case 4:
			// find right stairDoor
			stairDoorID = 215 + this.mTruePos.getmFloor()
					* IndoorSpace.gNumberDoorsPerFloor; //211
			break;
		default:
			stairDoorID = -1;
			break;
		}
		if (stairDoorID >= 0) {
			Door stairDoor = IndoorSpace.gDoors.get(stairDoorID);
			double dist2stairDoor = this.mTruePos.eDist(stairDoor);
			//System.out.println(dist2stairDoor);
			if (dist2stairDoor < DataGenConstant.maxAtomicWalkingDistance) {
				nextOffset = DataGenConstant.maxAtomicWalkingDistance
						- dist2stairDoor;
			}
			return nextOffset;
		} else
			return Constant.DISTMAX;

	}

	/**
	 * randomly decide a position in current partition for next time interval
	 * 
	 * @return randomPointPair is contained by the current partition
	 */
	public Point randomNextPositionInCurrentPar() {
		// System.out.println("randomNextPositionInCurrentPar");
		int count = 0;
		Point[] randomPointPair = this.mTruePos
				.randomWalk(DataGenConstant.maxAtomicWalkingDistance);
		while ((!this.curPar.contain(randomPointPair[0]))
				&& (!this.curPar.contain(randomPointPair[1]))) {
			count++;
			randomPointPair = this.mTruePos
					.randomWalk(DataGenConstant.maxAtomicWalkingDistance);
		}
		if (count > 0) {
			//System.out.println((count + 1) + " times hit!");
		}
		if (this.curPar.contain(randomPointPair[0]))
			return randomPointPair[0];
		else {
			return randomPointPair[1];
		}
	}

	/**
	 * if the object will enter the staircase
	 * 
	 * @return True as entering
	 */
	public boolean willEnterStairWay() {
		int randomInt = (AlgCSDM.random).nextInt(100);
		if (this.willOutStaircase) {
			return false;
		} else {
			if (randomInt < DataGenConstant.probEnterStairway) {
				return true;
			} else
				return false;
		}
	}

	/**
	 * walk in the indoor space
	 * 
	 * @return mWalkingType return what the walking type of object is after
	 *         walking for an interval
	 */
	public int walk() {
		//System.out.println("ID:" + this.mID);
		// current status
		if (mWalkingType > 0) { // in the current time, the object is in the
								// stairway, it is a staircase point
			
			// distance to the possible door out the stairway
			double offsetRange = this.mStairCasePos.getOffsetOutStaircase();

			if (offsetRange < DataGenConstant.maxAtomicWalkingDistance) { // decide
																			// to
																			// go
																			// out
																			// the
																			// staircase
				walkOutTheStairCase(offsetRange); // walk out the staircase
				this.setWillOutStaircase(Boolean.TRUE); // just walk outside,
														// and try not to come
														// back to the staircase
														// again
				this.setmWalkingType(WalkingType.INFLOOR); // set the walking
															// type as in-floor
															// walking
				
			} else { // decide to stay in the staircase
				walkInTheStairCase(); // walk in the staircase
				this.setWillOutStaircase(Boolean.FALSE); // random walk next
															// time and do not
															// change the
															// walking type
			}

		} else {
			
			int staircaseType = this.curPar.inStairCase(); // determine which
															// room this object
															// is in
			if (staircaseType > 0) { // in one of the four staircase room

				double offsetRange = this.possibleToStairCase(staircaseType); // how
																				// long
																				// distance
																				// between
																				// the
																				// enter-in-staircase
																				// door
																				// and
																				// current
																				// position
				//System.out.println("offsetRange:" + offsetRange);
				if (offsetRange < DataGenConstant.maxAtomicWalkingDistance
						&& willEnterStairWay()) {// decide to enter the
													// staircase
					this.setmWalkingType(staircaseType); // set the walking type
															// as walking in one
															// staircase way
					walkEnterTheStairCase(offsetRange); // walk into the
														// staircase
					this.setWillOutStaircase(Boolean.FALSE); // just walk into
																// the
																// staircase,
																// and try not
																// to come back
																// to the
																// staircase
																// room again
				} else { // decide not to enter for this moment or can not touch
							// the enter-in-staircase door
					if (this.willOutStaircase) { // try to walk out the
													// staircase room
						walkAwayFromStairCase();
					} else
						walkInTheSameFloor(); // random walk in the floor, it is
												// still a floor point for the
												// next interval
					this.setmWalkingType(WalkingType.INFLOOR); // set the
																// walking type
																// as in-floor
																// walking
				}
			} else {// still remain in the current floor
				this.setmWalkingType(WalkingType.INFLOOR); // set the walking
															// type as in-floor
															// walking, do not
															// change it
				walkInTheSameFloor(); // random walk in the floor, it is still a
										// floor point for the next interval
			}
		}
		return this.mWalkingType;
	}

	/**
	 * for the next interval, object walk in the staircase recall the function
	 * moveInStairway()
	 * 
	 */
	private void walkInTheStairCase() {
		// TODO Auto-generated method stub
		// System.out.println(this.mID + " walkInTheStairCase");
		this.mStairCasePos.moveInStairWay();
	}

	/**
	 * for the next interval, object walk out the staircase
	 * 
	 * @param the
	 *            longest distance it can walk in the floor after going through
	 *            the relevant door
	 */
	private void walkOutTheStairCase(double offsetRange) {
		// TODO Auto-generated method stub
		//System.out.println(this.mID + " walkOutTheStairCase");
		double randomOffset = ((AlgCSDM.random).nextDouble()) * offsetRange; // the
																			// distance
																			// it
																			// will
																			// walk,
																			// randomly
																			// decided

		//System.out.println("randomOffset:" + randomOffset);
		int nextEnteredFloor = this.mStairCasePos.getDestFloor(); // to get the
																	// next
																	// entered
																	// floor
		//System.out.println("nextEnteredFloor: " + nextEnteredFloor);
		Par nextEnteredPar = IndoorSpace.gPartitions.get(this.mStairCasePos
				.getNextEnteredPar()); // to get the next entered partition
		Door nextEnteredDoor = IndoorSpace.gDoors.get(this.mStairCasePos
				.getNextEnteredDoor()); // to get the next entered door

		Point[] randomPointPair = nextEnteredDoor.randomWalk(randomOffset); // the
																			// offset
																			// from
																			// the
																			// nextEnteredDoor

		// make sure that it will in the current partition
		int count = 0;
		while ((!nextEnteredPar.contain(randomPointPair[0]))
				&& (!nextEnteredPar.contain(randomPointPair[1]))) {
			count++;
			randomPointPair = nextEnteredDoor.randomWalk(randomOffset);
		}
		if (count > 0) {
			//System.out.println((count + 1) + " times hit!");
		}

		if (this.curPar.contain(randomPointPair[0])) {
			Point nextPos = new Point(randomPointPair[0].getX(),
					randomPointPair[0].getY(), nextEnteredFloor);
			this.setmTruePos(nextPos);
			this.setmWalkingType(WalkingType.INFLOOR);
			this.setCurPar(nextEnteredPar); // set the current par, it is
											// important to initialize it!
		} else {
			Point nextPos = new Point(randomPointPair[1].getX(),
					randomPointPair[1].getY(), nextEnteredFloor);
			this.setmTruePos(nextPos);
			this.setmWalkingType(WalkingType.INFLOOR);
			this.setCurPar(nextEnteredPar);
		}
	}

	/**
	 * for the next interval, object walk into the staircase
	 * 
	 * @param the
	 *            longest distance it can walk in the stairway after going
	 *            through the relevant door
	 */
	private void walkEnterTheStairCase(double offsetRange) {
		// TODO Auto-generated method stub
		//System.out.println(this.mID + " walkEnterTheStairCase");
		int curFloor = this.curPar.getmFloor();
		int destFloor = randomDestFloor(curFloor);
		double randomOffset = ((AlgCSDM.random).nextDouble()) * offsetRange; // the
																			// distance
																			// it
																			// will
																			// walk,
																			// randomly
																			// decided,
																			// can
																			// be
																			// positive(upstairs)
																			// or
																			// negative(downstairs)
		if (curFloor == 0) { // can only go upstairs
			randomOffset = Math.abs(randomOffset);
			StaircasePoint stairCasePos = new StaircasePoint(this.mWalkingType,
					randomOffset, destFloor, 1); // direction as going upstairs
			this.setmStairCasePos(stairCasePos);
		}
		if (curFloor == DataGenConstant.nFloor - 1) { // can only go downstairs
			randomOffset = -Math.abs(randomOffset);
			StaircasePoint stairCasePos = new StaircasePoint(this.mWalkingType,
					(curFloor * DataGenConstant.lenStairway) - randomOffset,
					destFloor, -1);// direction as going downstairs
			this.setmStairCasePos(stairCasePos);
		} else {
			if (destFloor < curFloor) {
				randomOffset = -randomOffset;
			}
			StaircasePoint stairCasePos = new StaircasePoint(this.mWalkingType,
					(curFloor * DataGenConstant.lenStairway) + randomOffset,
					destFloor, (randomOffset > 0) ? 1 : -1); // direction is
																// decided by
																// offset
			this.setmStairCasePos(stairCasePos);
		}
	}

	/**
	 * randomly decide a destination floor
	 * 
	 * @param curFloor
	 * @return a destination floor (positive integer)
	 */
	private int randomDestFloor(int curFloor) {
		// TODO Auto-generated method stub
		int destFloor = curFloor;
		while (destFloor == curFloor) {
			destFloor = (AlgCSDM.random).nextInt(DataGenConstant.nFloor);
		}
		return destFloor;
	}

	/**
	 * for the next interval, object walk in the floor
	 * 
	 */
	private void walkInTheSameFloor() {
		// TODO Auto-generated method stub
		// System.out.println(this.mID + " walkInTheSameFloor");

		List<NextPossiblePar> nextPossiblePars = this.getNextPossiblePars();
		int nppSize = nextPossiblePars.size();
		int pickone = (int) (Math.random() * (nppSize + 1));

		if (pickone == nppSize) {
			this.setmTruePos(randomNextPositionInCurrentPar()); // in the
																// current
																// partition
		} else {
			this.setmTruePos(nextPossiblePars.get(pickone)
					.randomPositionInPossiblePar()); // go through the door to
														// one of the touchable
														// partitions
		}

	}

	/**
	 * for the next interval, object walk in the floor but try to walk outside
	 * the staircase room
	 * 
	 */
	private void walkAwayFromStairCase() {
		// TODO Auto-generated method stub

		List<NextPossiblePar> nextPossiblePars = this.getNextPossiblePars();
		int nppSize = nextPossiblePars.size();

		if (nppSize > 0) { // pick one of the touchable partitions in the first
							// place
			int pickone = (int) (Math.random() * (nppSize));
			this.setmTruePos(nextPossiblePars.get(pickone)
					.randomPositionInPossiblePar());
			this.setWillOutStaircase(Boolean.FALSE); // let the object random
														// walk next time
														// interval

		} else { // can just stay in the staircase room
			this.setmTruePos(randomNextPositionInStairCase());
		}

	}

	/**
	 * recall Function randomWalkAway()
	 * 
	 * @return a random floor point
	 */
	private Point randomNextPositionInStairCase() {
		// TODO Auto-generated method stub

		int count = 0;
		Point[] randomPointPair = this.mTruePos
				.randomWalkAway(DataGenConstant.maxAtomicWalkingDistance);

		// make sure that it is contained by the current Partition
		while ((!this.curPar.contain(randomPointPair[0]))
				&& (!this.curPar.contain(randomPointPair[1]))) {
			count++;
			randomPointPair = this.mTruePos
					.randomWalkAway(DataGenConstant.maxAtomicWalkingDistance);
		}
		if (count > 0) {
			//System.out.println((count + 1) + " times hit!");
		}
		if (this.curPar.contain(randomPointPair[0]))
			return randomPointPair[0];
		else {
			return randomPointPair[1];
		}
	}

	/**
	 * toString
	 * 
	 * @return mID+mStairCaseID+mOffset+UR or mID+x+y+mFloor+curPar+UR
	 */
	public String toString() {
		if (this.mWalkingType > 0) {
			String tempSring = this.mID + "\t*\t"
					+ this.mStairCasePos.getmStaircaseID() + "\t"
					+ this.mStairCasePos.getmOffset(); //  + "\t" + this.mStairCasePos.getSampledPointsString();
			return tempSring;
		} else {
			String tempSring = this.mID + "\t#\t" + this.mTruePos.getX() + "\t"
					+ this.mTruePos.getY() + "\t" + this.mTruePos.getmFloor()
					+ "\t" + this.getCurPar().getmID() ;///+ "\t" + getSampledPointsString();
			return tempSring;
		}

	}

	public String getSampledPointsString() {

		this.mSampledPoints = mTruePos.getSampledPoints();

		String tempString = "";

		for (Point item : this.mSampledPoints) {
			tempString = tempString + item + " ";
		}

		return tempString;
	}

	/**
	 * get the uncertain sampled result 1. if it is floor point, get sampled
	 * floor points 2. if it is staircase point, get sampled staircase points
	 * 
	 * @return the uncertainSampledProb
	 */
	public HashMap<Integer, Double> getUncertainSampledProb() {
		if (this.mWalkingType > 0) {
			this.UncertainSampledProb = this.mStairCasePos.getSampledProb();
		} else {
			this.UncertainSampledProb = this.getFloorPointSampledProb();
		}
		return this.UncertainSampledProb;
	}

	/**
	 * @param uncertainSampledProb
	 *            the uncertainSampledProb to set
	 */
	public void setUncertainSampledProb(
			HashMap<Integer, Double> uncertainSampledProb) {
		UncertainSampledProb = uncertainSampledProb;
	}

	/**
	 * toString
	 * 
	 * @return \\[\\<ParID, Prob\\>, ...\\]
	 */
	public String getCurrentUncertainRecord() {
		getUncertainSampledProb();
		return "";
//		String tempString = "[";
//		for (Entry<Integer, Double> key : this.UncertainSampledProb.entrySet()) {
//			tempString = tempString + " < " + key.getKey() + " , "
//					+ key.getValue() + " > ";
//		}
//		return tempString + "]";
	}

	/**
	 * @return the mWalkingType
	 */
	public int getmWalkingType() {
		return mWalkingType;
	}

	/**
	 * @param mWalkingType
	 *            the mWalkingType to set
	 */
	public void setmWalkingType(int mWalkingType) {
		this.mWalkingType = mWalkingType;
	}

	/**
	 * @return the mStairCasePos
	 */
	public StaircasePoint getmStairCasePos() {
		return mStairCasePos;
	}

	/**
	 * @param mStairCasePos
	 *            the mStairCasePos to set
	 */
	public void setmStairCasePos(StaircasePoint mStairCasePos) {
		this.mStairCasePos = mStairCasePos;
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
	 * @return the mTruePos
	 */
	public Point getmTruePos() {
		return mTruePos;
	}

	/**
	 * @param mTruePos
	 *            the mTruePos to set
	 */
	public void setmTruePos(Point mTruePos) {
		this.mTruePos = mTruePos;
		this.curPar = mTruePos.getCurrentPar();
	}

	/**
	 * @return the curPar
	 */
	public Par getCurPar() {
		return curPar;
	}

	/**
	 * @param curPar
	 *            the curPar to set
	 */
	public void setCurPar(Par curPar) {
		this.curPar = curPar;
	}

	/**
	 * @return the willOutStaircase
	 */
	public boolean isWillOutStaircase() {
		return willOutStaircase;
	}

	/**
	 * @param willOutStaircase
	 *            the willOutStaircase to set
	 */
	public void setWillOutStaircase(boolean willOutStaircase) {
		this.willOutStaircase = willOutStaircase;
	}

	/**
	 * @return the mSampledPoints
	 */
	public ArrayList<Point> getmSampledPoints() {
		return mSampledPoints;
	}

	/**
	 * @param mSampledPoints
	 *            the mSampledPoints to set
	 */
	public void setmSampledPoints(ArrayList<Point> mSampledPoints) {
		this.mSampledPoints = mSampledPoints;
	}

	/**
	 * @param string
	 */
	public void setmSampledPoints(String string) {
		// TODO Auto-generated method stub
		ArrayList<Point> mSampledPoints = new ArrayList<Point>();
		
		String []items = string.split(" ");
		for(String item : items){
			Point point = Point.parse(item);
			if(point != null){
				mSampledPoints.add(point);
			}
		}
		
		this.setmSampledPoints(mSampledPoints);
	}

	/**
	 * parse a string into an IdrObj
	 * 
	 * @param readoneline
	 * @return an IdrObj
	 */
	public static IdrObj parse(String readoneline) {
		// TODO Auto-generated method stub
		String[] items = readoneline.split("\t");
		int mID = Integer.valueOf(items[0]);
		double x = Double.valueOf(items[2]);
		double y = Double.valueOf(items[3]);
		int mFloor = Integer.valueOf(items[4]);
		int curParID = Integer.valueOf(items[5]);
		IdrObj newObj = new IdrObj(mID, new Point(x, y,mFloor),IndoorSpace.gPartitions.get(curParID));
		return newObj;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

}
