/**
 * 
 */
package algorithm;

import java.util.*;

import dk.aau.cs.idq.indoorentities.Door;
import dk.aau.cs.idq.indoorentities.IndoorSpace;
import dk.aau.cs.idq.indoorentities.LeavePair;
import dk.aau.cs.idq.indoorentities.NextPossiblePar;
import dk.aau.cs.idq.indoorentities.Par;
import dk.aau.cs.idq.indoorentities.Point;
import dk.aau.cs.idq.indoorentities.Rect;
import dk.aau.cs.idq.indoorentities.SampledPoint;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.Functions;
import dk.aau.cs.idq.utilities.OTTConstant;

import java.util.Map.Entry;
import org.apache.commons.math3.random.GaussianRandomGenerator;

/**
 * IndoorUR model the uncertainty region in [recordTime, recordTime+T_{Max}]
 * 
 */
public class IndoorUR {

	private SampledPoint mPoint; // the position of the Sample

	private Par curINPar; // par of the Sample Point located in

	private int recordTime; // the record time of this Sample

	private int lastGenTime; // the last guassian points generation time

	// the Gaussian distributed Points points in each time stamp
	private List<List<Point>> gaussianPointsList = new ArrayList<List<Point>>();

	// for each time, part to points
	private List<HashMap<Integer, ArrayList<Point>>> ptsParMapList = new ArrayList<>();

	// storing the sum of probability of prev time point in time t
	private List<Double> betaList = new ArrayList<Double>();

	/// ---------------------------------
	private List<NextPossiblePar> uncertainPars = new ArrayList<NextPossiblePar>(); // uncertain next possible pars

	// storing the actual[0] and virtual sample point
	private List<SampledPoint> mPointsList = new ArrayList<SampledPoint>();

	// r_record-d([0]-[x])
	private List<Double> radList = new ArrayList<Double>();

	private List<Par> initialPars = new ArrayList<Par>(); // initial Pars of the UR

	private List<List<Par>> parsList = new ArrayList<List<Par>>(); // *additional* Pars at each time

	private double r_record; /// the minimum radius of the ur i.e., r at recordTime

	private double r_indoor; /// the minimum indoor radius of the ur i.e., r at recordTime, for type 2 objec

	/// the type of UR/object 1=single ;2=connected ;3=disconnected
	private int initialType;

	private List<TypeTime> typeTimes = new ArrayList<TypeTime>();

	/// ---------------------------------
	// grouping related.

	private int groupID;// index of the group

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	/// ---------------------------------

	/**
	 * Constructor Function
	 * 
	 * @param mPoint
	 * @param recordTime
	 * @param flag       = 1: generate monteCarloPoint
	 * 
	 * @param r_min      //also use to decide the type of the UR
	 */
	public IndoorUR(SampledPoint mPoint, int recordTime, double r_min) {
		this.mPoint = mPoint;
		this.recordTime = recordTime;
		this.lastGenTime = recordTime;
		this.r_record = 0.5 + AlgCSDM.random.nextDouble() * (r_min - 0.5);
		this.curINPar = mPoint.getCurPar();

		calcInitialType();// decide the type of UR
		setSamplePointsList(); // create those virtual sp
		setCurInPars(); // find the initial pars
		setTypeTimes(); // set the types

		// -----------------------------------------------
		// create an empty one to keep the correspondingness of index
		// actual mapping is created on first retrieve
		for (int i = 0; i < recordTime + OTTConstant.maxSamplingPeriod; i++) {
			ptsParMapList.add(new HashMap<>());
		}
//			
		gaussianPointsList.add(genNewGaussianPointsList(recordTime, 0));
		betaList.add(1.0);// first one

		normalizeGaussianPointsProb(gaussianPointsList.get(0), null);

		// -----------------------------------------------
		// check.
//		for (Par p : initialPars)
//			System.out.println("p: " + p);
//
//		for (SampledPoint sp : mPointsList)
//			System.out.println("sp: " + sp.toString());
//
//		System.out.println("uncertainParsList: " + uncertainPars.size());
//		for (NextPossiblePar l : uncertainPars) {
//			System.out.println("l: " + l.toString());
//		}

//		System.out.println("getCurInPars:");
//		System.out.println(initialPars);
//		System.out.println(getCurInPars(recordTime + 2));
//		System.out.println(getCurInPars(recordTime + 10));
//		for(Point p :getGaussianPoints(recordTime+OTTConstant.maxSamplingPeriod-1)) {
//			System.out.println(p);
//			for(Par par:getCurInPars2(recordTime + 10)) {
//				if(par.contain(p))
//					System.out.println("OK @ " + par.getmID());
//			}
//		}
		// ----------------------------------------------

	}

	/**
	 * 
	 * @return get the initial type of the object at time=recordTime
	 */
	public void calcInitialType() {

		initialType = 1; // default

		Par par = mPoint.getCurPar();
		List<Integer> doors = par.getmDoors();
		// for each door
		for (int doorID : doors) {

			if (mPoint.eDist(IndoorSpace.gDoors.get(doorID)) < r_record) {
				initialType = 2;
				break;
			}
		}
		// Y1 X1---X2
		// |
		// |
		// Y2
		if (mPoint.getSampledX() - r_record < par.getX1() || mPoint.getSampledX() + r_record > par.getX2()
				|| mPoint.getSampledY() - r_record < par.getY1() || mPoint.getSampledY() + r_record > par.getY2())
			initialType = 3;
		return;
	}

	// create a virtual sample point for each involved part
	private void setSamplePointsList() {

		initialPars.add(mPoint.getCurPar());// actual point in [0]

		// -----
		// find out initial pars.
		double marginLeft = mPoint.getSampledX() - r_record;
		double marginRight = mPoint.getSampledX() + r_record;
		double marginTop = mPoint.getSampledY() - r_record;
		double marginBottom = mPoint.getSampledY() + r_record;

		Iterable<Par> pars = IndoorSpace.gRTree.find(marginLeft, marginTop, marginRight, marginBottom);
		for (Par par : pars) {
//			System.out.println("" + par.toString());

			if (par.getmID() + IndoorSpace.gNumberParsPerFloor * mPoint.getmFloor() == mPoint.getCurPar().getmID())
				continue;
			double dist = par.getMinDist(mPoint);
			if (dist <= r_record) {
				initialPars.add(IndoorSpace.gPartitions
						.get(par.getmID() + IndoorSpace.gNumberParsPerFloor * mPoint.getmFloor()));

			}
		}
		// -----
		// find the virtual sample Point for each part
		for (Par par : initialPars) {
			// ---
			double x = mPoint.getSampledX();
			double y = mPoint.getSampledY();

			if (x < par.getX1())
				x = par.getX1();
			else if (x > par.getX2())
				x = par.getX2();

			if (y < par.getY1())
				y = par.getY1();
			else if (y > par.getY2())
				y = par.getY2();
			// ---
			SampledPoint pt = new SampledPoint(mPoint.getObjID(), x, y, mPoint.getmFloor(), par);
			mPointsList.add(pt);
			radList.add(r_record - pt.eDist(mPoint));
		}
		// -------

		// gen uncertainParsList
		for (int i = 0; i < mPointsList.size(); i++) {
			Par p = initialPars.get(i);
			SampledPoint sp = mPointsList.get(i);
			double rad = radList.get(i);
			getUncertainPars2(p, sp, rad);
		}

	}

	/**
	 * 
	 * @param curTime
	 * @return List of current reachable part, based on uncertainPars
	 */
	private void setCurInPars() {
		// TODO Auto-generated method stub

		for (int i = 0; i < OTTConstant.maxSamplingPeriod; i++)
			parsList.add(new ArrayList<Par>());

		parsList.get(0).addAll(initialPars);

		for (NextPossiblePar pp : uncertainPars) {
			boolean flag = true;
			for (List<Par> pars : parsList) {
				if (pars.contains(pp.getPossibleNextPar())) {
					flag = false;
					break;
				}
			}
			if (flag && (int) Math.ceil(pp.timeArrive) < OTTConstant.maxSamplingPeriod)
				parsList.get((int) Math.ceil(pp.timeArrive)).add(pp.getPossibleNextPar());
		}
		return;
	}

	/**
	 * 
	 * @param curTime
	 * @return List of current reachable part, based on parsList
	 */
	public List<Par> getCurInPars(int curTime) {
		// TODO Auto-generated method stub
		int timeInterval = curTime - recordTime;

		if (timeInterval < 0) {
			System.err.println("timeInterval<0 @ getCurInPars.");
			System.exit(-1);
		}

		List<Par> pars = new ArrayList<Par>();
		for (int i = 0; i <= timeInterval; i++) {
			pars.addAll(parsList.get(i));
		}

		return pars;
	}

	/**
	 * set the typeTimes list which identify the type in the object's life time.
	 * 
	 * Type relationship: 1 --> 2; 2--> 3; 3-->2
	 * 
	 */
	private void setTypeTimes() {
		typeTimes = new ArrayList<TypeTime>();
		int curType = initialType;

		int startTime = recordTime;
		int endTime = recordTime + OTTConstant.maxSamplingPeriod;

		if (curType == 2) {
			calcRiType2();
		}
		while (true) {
//			System.out.println("startTime: " + startTime + " endTime: " + endTime + " curType:" + curType);
			if (curType == 1) {
				// --- find the time change to 2 ---
				// find distance to the closest doors in this part
				double minDist = Constant.DISTMAX;
				List<Integer> doors = mPoint.getCurPar().getmDoors();
				// for each door
				for (int doorID : doors) {
					double dist = mPoint.eDist(IndoorSpace.gDoors.get(doorID));
					if (dist < minDist) {
						minDist = dist;
					}
				}
				// calc. time by minDist
				int time = (int) Math.ceil(minDist / DataGenConstant.maxVelocity);

				if (time < OTTConstant.maxSamplingPeriod) {
					endTime = recordTime + time;
					typeTimes.add(new TypeTime(1, startTime, endTime));
					curType = 2;
				} else {
					typeTimes.add(new TypeTime(1, startTime, endTime));
					break;
				}
			} else if (curType == 2) {
				// --- find the time change to 3b ---
				// find distance to the closest doors in other part
				int minTime = OTTConstant.maxSamplingPeriod;
				// find the time stamp that it connects to partition other than current ones
				for (int i = startTime + 1; i < parsList.size(); i++) {
					List<Par> pars = parsList.get(i);
					if (pars.size() > 0) {
						minTime = i;
						break;
					}
				}
				endTime = recordTime + minTime;
//				System.out.println("minTime:" + minTime + " endTime:" + endTime);
				// -------------
				// this part visit time change type 3
				typeTimes.add(new TypeTime(2, startTime, endTime));
				curType = 3;
			} else if (curType == 3) {
				// --- find the time change to 2 ---
				// TODO two possible cases:
				// change to 3 -> 3 forever
				// change to 2 -> loop again
				typeTimes.add(new TypeTime(3, startTime, endTime));
				break;
			} else {
				System.err.println("Error curType value");
				return;
			}
			startTime = endTime + 1;
			endTime = recordTime + OTTConstant.maxSamplingPeriod;
			if (startTime > recordTime + OTTConstant.maxSamplingPeriod)
				break;

		} // end while
	}

	public void updateUR(int curTime) {

		if (curTime <= this.recordTime) {
			return;
		}

		if (curTime <= lastGenTime) {
			// already generated.
			return;
		}

		if (curTime > lastGenTime + 1) {
			updateUR(curTime - 1);
		}

		// System.out.println("updating UR @curTime=" + curTime);
		// -- copy exisiting points from prev time
		// -- change prob of existing points
		int timeinterval = curTime - recordTime;
		double newRadius = r_record + timeinterval * DataGenConstant.maxVelocity; // maximum distance from center
		double sigma = newRadius / 6;

		double cX = mPoint.getSampledX();
		double cY = mPoint.getSampledY();

//		LinkedList<Point> oldPoints = new LinkedList<Point>();
		List<Point> oldPoints = new ArrayList<Point>();

		if (AlgCSDM.Distribution != 0) {
			for (Point pt : gaussianPointsList.get(timeinterval - 1)) {
				Point newPt = new Point(pt);
				double prob = calcProb(AlgCSDM.Distribution, newRadius, pt.getX() - cX, pt.getY() - cY);

				newPt.setProb(prob);
				oldPoints.add(newPt);
			}
		} else {
			for (Point pt : gaussianPointsList.get(timeinterval - 1)) {
				Point newPt = new Point(pt);
				// double next_X = x * sigma + cX; // if (Z-u)/sigma follows N(0,1), then Z
				// follows N(u, sigma^2)
				// double next_Y = y * sigma + cY;
				double x = (pt.getX() - cX) / sigma;
				double y = (pt.getY() - cY) / sigma;

				double pro_x = calProbabilityOfGaussian(x);
				double pro_y = calProbabilityOfGaussian(y);

				newPt.setProb(pro_x * pro_y);
				oldPoints.add(newPt);
			}
		}
		// -------------------------------------------------------
		// gen incremental points
		List<Point> newPoints = genNewGaussianPointsList(curTime, oldPoints.size());

		// normalization of the two arrayLists
		normalizeGaussianPointsProb(oldPoints, newPoints);

		// calculate the beta value
		double sum = 0;
		for (int i = 0; i < oldPoints.size(); i++) {
//			System.out.println(oldPoints.get(i).getProb());
			sum += oldPoints.get(i).getProb();
		}
		betaList.add(sum);

		// -----------------------------------------------
		// merging the two lists
		oldPoints.addAll(newPoints);
		gaussianPointsList.add(oldPoints);
		lastGenTime = curTime;

		return;

	}

	/**
	 * generate new Gaussian distributed points
	 *
	 */
	private List<Point> genNewGaussianPointsList(int curTime, int oldSize) {
		List<Point> newPoints = new ArrayList<Point>();

		int numNewGaussianPoints = 10;
		double newRadius = r_record;
		/// ------------- generate new discrete samples ----------------------
		int timeinterval = curTime - recordTime;
		if (timeinterval == 0) {
			// initial gen
//			numNewGaussianPoints = 10;
			numNewGaussianPoints = (int) (10 * Math.pow(r_record / 3.0, 2)); // r=3 = 10
			newRadius = r_record;
//			System.out.println("0numNewGaussianPoints: " + numNewGaussianPoints + " newRadius: " + newRadius + " r_record: " + r_record + " "
//					+ Math.pow(newRadius / r_record, 2) + " oldSize:" + oldSize);

		} else {
			// incremental
			newRadius = r_record + timeinterval * DataGenConstant.maxVelocity; // maximum distance from center
			/// (1/5)^2*100 = 4
//			numNewGaussianPoints = (int) (4 * Math.pow(timeinterval * DataGenConstant.maxVelocity / r_record, 2)
//					- oldSize);// increase by ratio
			numNewGaussianPoints = (int) (10 * Math.pow(newRadius / 3.0, 2) - oldSize);// increase by ratio
			numNewGaussianPoints = Math.min(numNewGaussianPoints, oldSize);
//			System.out.println("numNewGaussianPoints: " + numNewGaussianPoints + " newRadius: " + newRadius + " r_record: " + r_record + " "
//					+ Math.pow(newRadius / r_record, 2) + " oldSize:" + oldSize);
		}
		int cnt = 0;
		double sigma = newRadius / 6; // the standard deviation of X/Y Gaussian distribution: (mean-3*sigma, mean
										// +3*sigma) can cover 99% probability

		double cX = this.mPoint.getSampledX();// the mean of X Gaussian distribution
		double cY = this.mPoint.getSampledY();// the mean of Y Gaussian distribution
		if (AlgCSDM.Distribution != 0) {
			while (cnt < numNewGaussianPoints) { // gen Gaussian distributed points
				double x = AlgCSDM.random.nextDouble() * newRadius * 2 - newRadius;
				double y = AlgCSDM.random.nextDouble() * newRadius * 2 - newRadius;
				double next_X = x + cX; // if (Z-u)/sigma follows N(0,1), then Z follows N(u, sigma^2)
				double next_Y = y + cY;

				double prob = calcProb(AlgCSDM.Distribution, newRadius, x, y);
				// will become uniform after normalization
				Point next_p = new Point(next_X, next_Y, this.mPoint.getmFloor(), prob);
				if (Functions.isInBuilding(next_p) && isPointValid(curTime, next_p)) {
					newPoints.add(next_p);
					cnt++;
				}
			}
		} else {
			// guass
			while (cnt < numNewGaussianPoints) { // gen Gaussian distributed points
				double x = AlgCSDM.random.nextGaussian();// generate random number as offset from N(0,1)
				double y = AlgCSDM.random.nextGaussian();
				double pro_x = calProbabilityOfGaussian(x);
				double pro_y = calProbabilityOfGaussian(y);
				double next_X = x * sigma + cX; // if (Z-u)/sigma follows N(0,1), then Z follows N(u, sigma^2)
				double next_Y = y * sigma + cY;

				Point next_p = new Point(next_X, next_Y, this.mPoint.getmFloor(), pro_x * pro_y);
				if (Functions.isInBuilding(next_p) && isPointValid(curTime, next_p)) {
					newPoints.add(next_p);
					cnt++;
				}
			}
		}

		return newPoints;
	}

	private double calcProb(int dis, double rad, double x, double y) {
		// TODO Auto-generated method stub
		if (dis == 1)
			return 1; // normalized will become uniform

		double dist = Math.sqrt(x * x + y * y);
		if (dis == 2) {
			if (dist < rad)
				return 1.0 - (dist / rad);
			else
				return 0;
		}
		if (dis == 3)
			return 1.0 / (dist + 1);
		if (dis == 4)
			return 1.0 / ((dist + 1) * (dist + 1));
		if (dis == 5)
			return Math.exp(-dist);

		System.err.println("calcProb Error");
		System.exit(-1);
		return 0;
	}

	/*
	 * check if the generated point is valid based on the type of UR and curTime
	 */
	private boolean isPointValid(int curTime, Point next_p) {
		// TODO Auto-generated method stub

		int type = findCurType(curTime);
//		System.out.println("findCurType:" + type + " " + curTime + " " + typeTimes.toString());

		// all points are valid for type 1
		if (type == 1) {
			next_p.setCurrentPar(curINPar);
			return true;
		}
//		if (type == 2) {
//			if (mPoint.iDist(next_p) <= r_record + (curTime - recordTime) * DataGenConstant.maxVelocity) {
//				return true;
//			}
//		}

		if (type == 2 || type == 3) {

			for (int i = 0; i < curTime - recordTime + 1; i++) {
				for (Par par : parsList.get(i)) {

					if (par.contain(next_p)) {
						// ---
						next_p.setCurrentPar(par);
						// ---
						return true;

					}
				}
			}
		}

		return false;
	}

	/**
	 * find the type at curTime based on typeTimes
	 * 
	 * @return
	 */
	public int findCurType(int curTime) {
		// TODO Auto-generated method stub
		for (TypeTime tt : typeTimes) {
//			System.out.println(tt.toString());
			if (tt.startTime <= curTime && tt.endTime >= curTime)
				return tt.type;
		}
		System.err.println("findCurType error @curTime=" + curTime + " recordTime=" + recordTime);
		System.exit(-1);
		return -1;
	}

	// obtain the beta probability of probList[prev][target]
	// which mean for the points generated at prevTime,
	// sum of their prob at targetTime
	public double getBeta(int targetTime) {
//		if (targetTime - recordTime < 0 || targetTime - recordTime > betaList.size())
//			System.out.println("targetTime: " + targetTime + " recordTime:" + recordTime + " objID: "
//					+ mPoint.getObjID() + " " + betaList.size());
		return betaList.get(targetTime - recordTime);
	}

	/**
	 * calculate probability of a gaussian value
	 * 
	 * @param gaussian_v
	 * @return
	 */
	public static double calProbabilityOfGaussian(double gaussian_v) {
		double pro = (1 / (Math.sqrt(2 * Constant.PI))) * Math.exp(-Math.pow(gaussian_v, 2) / 2);
		return pro;
	}

	/**
	 * normalize the picked gasssian points to sum prob = 1
	 */
	private void normalizeGaussianPointsProb(List<Point> oldPoints, List<Point> newPoints) {

		double sum = 0;

		for (Point pt : oldPoints) {
			sum += pt.getProb();
		}

		if (newPoints != null) {
			for (Point pt : newPoints) {
				sum += pt.getProb();
			}
		}
		for (Point pt : oldPoints) {
			pt.setProb(pt.getProb() / sum);
		}
		if (newPoints != null) {
			for (Point pt : newPoints) {
				pt.setProb(pt.getProb() / sum);
			}
		}
		return;

	}

	/**
	 * @return the mPoint
	 */
	public SampledPoint getmPoint() {
		return mPoint;
	}

	/**
	 * @param mPoint the mPoint to set
	 */
	public void setmPoint(SampledPoint mPoint) {
		this.mPoint = mPoint;
	}

	/**
	 * @return the recordTime
	 */
	public int getRecordTime() {
		return recordTime;
	}

	/**
	 * @param recordTime the recordTime to set
	 */
	public void setRecordTime(int recordTime) {
		this.recordTime = recordTime;
	}

	public List<Point> getGaussianPoints(int curTime) {
		return gaussianPointsList.get(curTime - recordTime);
	}

	public List<List<Point>> getGaussianPointsList() {
		return gaussianPointsList;
	}

	public double getR_min() {
		return r_record;
	}

	public void setR_min(double r) {
		r_record = r;
	}

	public List<TypeTime> getTypeTimes() {
		return typeTimes;
	}

	public HashMap<Integer, ArrayList<Point>> getPtsParMap(int curTime) {
		return ptsParMapList.get(curTime - recordTime);
	}

	public void setPtsParMap(int curTime, HashMap<Integer, ArrayList<Point>> map) {
		ptsParMapList.set(curTime - recordTime, map);
	}

	public int getInitialType() {
		return initialType;
	}

	/**
	 * Calculate the ri= max_i |c,o[i]|_{maxI}
	 * 
	 * at record time !
	 * 
	 * @return
	 */
	private void calcRiType2() {
		// TODO Auto-generated method stub

		r_indoor = 0;
		double maxDist = 0;
		// for each door in cur part
		// calc by ri
		for (int dID : curINPar.getmDoors()) {
			double dist = r_record - mPoint.eDist(IndoorSpace.gDoors.get(dID));
			if (dist > 0) {
				dist += r_record + dist; // max. possible dist from center to a point in the region
				if (dist > maxDist)
					maxDist = dist;
			}
		}

		r_indoor = maxDist;
	}

	public double getR_i() {
		return r_indoor;
	}

	/**
	 * Find all the possible reachable partitions in time < maxSamplingPeriod
	 * 
	 * @param curDoor
	 * @param curPar
	 * @param dist
	 * @param time
	 * @return
	 */
	private void getUncertainPars2(Par par, SampledPoint sp, double rad) {

//		List<NextPossiblePar> nextPossiblePars = new ArrayList<NextPossiblePar>();
		double maxGoing = OTTConstant.maxSamplingPeriod * DataGenConstant.maxVelocity;

		Iterator<LeavePair> itr = par.getLeaveablePars().iterator();
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			Par nextPar = IndoorSpace.gPartitions.get(nextLeavePair.getmParID());

			//-----
			//avoid duplicate pars.
			if (initialPars.contains(nextPar))
				continue;
			//------

			double p2dDist = sp.eDist(nextDoor) - rad;
			double timeArrive = p2dDist / DataGenConstant.maxVelocity;
			// System.out.println(p2dDist);
			// if (p2dDist < maxGoing) {
			if (p2dDist > 0 && timeArrive < OTTConstant.maxSamplingPeriod) {
				NextPossiblePar nextPossiblePar = new NextPossiblePar(nextDoor, nextPar, p2dDist, timeArrive);
				int temp = dominateCheck(uncertainPars, nextPossiblePar);
				if (temp >= 0) {
					// update existing one
					uncertainPars.get(temp).setDistanceToContinue(maxGoing - p2dDist);
					uncertainPars.get(temp).setTimeArrive(timeArrive);
					getExtendedNextPossiblePars2(nextDoor, nextPar, (maxGoing - p2dDist), timeArrive, uncertainPars);
				} else if (temp == -2) {
					// not exist
					uncertainPars.add(nextPossiblePar);
					getExtendedNextPossiblePars2(nextDoor, nextPar, (maxGoing - p2dDist), timeArrive, uncertainPars);

				}
//				nextPossiblePars.addAll(extendedPossiblePars);
			}
		}
		return;
	}

	/**
	 * 
	 * @param uncertainPars2
	 * @param nextPossiblePar
	 * @return the index of NextPossiblePar that is dominated by @nextPossiblePar
	 * 
	 *         dominate = same par, same door, more remainingDist and can go more
	 * 
	 *         -1 if found dominate -2 if not found
	 */
	private int dominateCheck(List<NextPossiblePar> uncertainPars2, NextPossiblePar nextPossiblePar) {
		// TODO Auto-generated method stub
		for (int i = 0; i < uncertainPars2.size(); i++) {
			NextPossiblePar pp = uncertainPars2.get(i);
			if (nextPossiblePar.equals(pp)) {
				if (nextPossiblePar.getDistanceToContinue() > pp.getDistanceToContinue())
					return i;
				else
					return -1;
			}
		}

		return -2;
	}

	private void getExtendedNextPossiblePars2(Door curDoor, Par curPar, double remainGoing, double time,
			List<NextPossiblePar> mPossiblePars) {
		// TODO Auto-generated method stub

//		List<NextPossiblePar> mPossiblePars = new ArrayList<NextPossiblePar>();
		Iterator<LeavePair> itr = curPar.getLeaveablePars().iterator();
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			Par nextPar = IndoorSpace.gPartitions.get(nextLeavePair.getmParID());
			if (initialPars.contains(nextPar))
				continue;
			// double nextDist2Door = curDoor.eDist(nextDoor);
			double nextDist2Door = IndoorSpace.gD2DMatrix[nextDoor.getmID() % IndoorSpace.gNumberDoorsPerFloor][curDoor
					.getmID() % IndoorSpace.gNumberDoorsPerFloor];
			// if ((nextDist2Door != 0) && nextDist2Door < onGoing) {
			double timeArrive = nextDist2Door / DataGenConstant.maxVelocity + time;
			if ((nextDist2Door != 0) && nextDist2Door < remainGoing && timeArrive < OTTConstant.maxSamplingPeriod) {
				NextPossiblePar nextPossiblePar = new NextPossiblePar(nextDoor, nextPar, remainGoing - nextDist2Door,
						timeArrive);
				int temp = dominateCheck(uncertainPars, nextPossiblePar);
				if (temp >= 0) {
					// update existing one
					uncertainPars.get(temp).setDistanceToContinue(remainGoing - nextDist2Door);
					uncertainPars.get(temp).setTimeArrive(timeArrive);
					getExtendedNextPossiblePars2(nextDoor, nextPar, (remainGoing - nextDist2Door), timeArrive,
							uncertainPars);
				} else if (temp == -2) {
					// not exist
					uncertainPars.add(nextPossiblePar);
					getExtendedNextPossiblePars2(nextDoor, nextPar, (remainGoing - nextDist2Door), timeArrive,
							uncertainPars);

				}

			}
		}
		return;
	}

}
