package algorithm;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Map.Entry;

import dk.aau.cs.idq.indoorentities.*;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.OTTConstant;

/**
 * 
 * @author kai-ho
 *
 *
 *         Thread to calculate / process the update
 */
public class QueryUpdate implements Callable<ArrayList<ArrayList<String>>> {

	// global result here.
//	public static HashMap<Integer, ArrayList<String>> result = new HashMap<Integer, ArrayList<String>>();
	public static DecimalFormat df = new DecimalFormat("#.#####");

	// ---------------------

//	private Thread updateThread;

	private ArrayList<ArrayList<String>> newResult = new ArrayList<ArrayList<String>>();

	// "main" objId, group
	private HashMap<Integer, Group> newObjGroups = new HashMap<>();// to store the obj groups

	private int curTime;

	private List<List<IdrObj>> newObjsList;

	public QueryUpdate(int curTime, List<List<IdrObj>> newObjs) {
		this.curTime = curTime;
		this.newObjsList = newObjs;

		for (int i = 0; i < OTTConstant.maxSamplingPeriod; i++) {
			newResult.add(new ArrayList<String>());
		}

	}

	/*
	 * Main method for processing pairs
	 */
	public void processBatch() {

		// ---------------------------------------------------------------------
		// create a group for each new object
		for (List<IdrObj> newObjs : newObjsList) {
			for (int i = 0; i < newObjs.size(); i++) {
				IdrObj o1 = newObjs.get(i);
				int o1ID = o1.getmID();
				IndoorUR ur1 = new IndoorUR(o1, curTime, o1.getRadius());// initialize ur1 and ur2
				// create a group for each new object for merging purpose below
				Group newObjGroup = new Group(ur1, curTime);
				newObjGroups.put(o1ID, newObjGroup);
				ur1.setGroupID(o1ID);
				o1.setUr(ur1);

//				System.out.println(o1.getmID() + " " + newObjGroup.getInitialType() + " " + newObjGroup.getmPoint());
//				System.out.println(" - " + newObjGroup.getmPoint().getCurPar().toString());
			}
		}

		// ----
		// calc dist to staircases to other floors
		// sort the new objects by the distance to first staircase
		for (List<IdrObj> newObjs : newObjsList) {

			for (int i = 0; i < newObjs.size(); i++) {
				IndoorUR ur1 = newObjs.get(i).getUr();

				double[] temp = new double[4];
				c2scdiDists(ur1, temp);
				newObjs.get(i).setD2sc(temp);
			}

			// sort newObjs based on temp
			newObjs.sort(new Comparator<IdrObj>() {

				@Override
				public int compare(IdrObj o1, IdrObj o2) {
					// TODO Auto-generated method stub
					double[] d1 = o1.getD2sc();
					double[] d2 = o2.getD2sc();
//					System.out.println("d1[0]: " + d1[0] + " " + d2[0]);
					if (d1[0] > d2[0])
						return 1;
					else if (d1[0] < d2[0])
						return -1;
					else {
						if (d1[1] > d2[1])
							return 1;
						else if (d1[1] < d2[1])
							return -1;
						else {
							if (d1[2] > d2[2])
								return 1;
							else if (d1[2] < d2[2])
								return -1;
							else {
								if (d1[3] > d2[3])
									return 1;
								else
									return 0;
							}
						}
					}
				}
			});
			// check.
//			for (int i = 0; i < newObjs.size(); i++) {
//				System.out.println(i+" " + newObjs.get(i).getmID() + " " + newObjs.get(i).getD2sc()[0]
//						+ " " + newObjs.get(i).getD2sc()[1]
//								+ " " + newObjs.get(i).getD2sc()[2]
//										+ " " + newObjs.get(i).getD2sc()[3]);
//				
//			}

		}

		// ----------------------------------------------------------
		// for each new group
		// calculate distance with other group
		// merge groups if possible
		int f = 0;
		int objCnt = 0;
		for (List<IdrObj> newObjs : newObjsList) {

//			System.out.println("newObjs:" + (objCnt++) + " " + newObjs.size());
			for (int i = 0; i < newObjs.size(); i++) {
//				Group newObjGroup1 = newObjGroups.get(newObjs.get(i).getmID());
				IndoorUR ur1 = newObjs.get(i).getUr();
				// process objects in the same floor
				for (int j = i + 1; j < newObjs.size(); j++) {

					// early stopping pruning by d2sc
					if (!d2scCheck(newObjs.get(i).getD2sc(), newObjs.get(j).getD2sc(), 0, 1)) {
						break; // the later also have larger dist since the array is sorted
					}
					if (!d2scCheck(newObjs.get(i).getD2sc(), newObjs.get(j).getD2sc(), 1, 4)) {
						continue;
					}

					IndoorUR ur2 = newObjs.get(j).getUr();
					processO2O(ur1, ur2, curTime + OTTConstant.predictingPeriod, 1);
				}
				// ---------------------------------------------------------
				// process objects in different floors
				double minDist2SC = minc2scdiDist(ur1.getmPoint());
				// consider other floors if it is close to the staircase
				// min dist to staircse - farthest possible moving distance
				double d = minDist2SC - ur1.getR_i() - (OTTConstant.predictingPeriod * DataGenConstant.maxVelocity);
				d = Math.max(d, 0);

				if (d + DataGenConstant.lenStairway < AlgCSDM.distTH) {
					// multiple floor is possible
					for (int j = 0; j < DataGenConstant.nFloor; j++) {
						if (j == f)
							continue;
						if (d + Math.abs(f - j) * DataGenConstant.lenStairway < AlgCSDM.distTH) {
//							System.out.println("process floor:" + j);
							// for each new object in floor j
							for (int k = 0; k < newObjsList.get(j).size(); k++) {
								IndoorUR ur2 = newObjs.get(j).getUr();
								// grouping only in same floor
								processO2O(ur1, ur2, curTime + OTTConstant.predictingPeriod, 0);
							}
						}
					}
				}
			}
			f++;
		}
		// ---------------------------------------------------------------------
		// update the group center based on the urs in each group
		for (Entry<Integer, Group> e : newObjGroups.entrySet()) {
			e.getValue().updateCenter();
		}
		// ---------------------------------------------------------------------
		// for each group process with existing objects
		for (List<IdrObj> newObjs : newObjsList) {
			for (int i = 0; i < newObjs.size(); i++) {
				Group newObjGroup1 = newObjGroups.get(newObjs.get(i).getmID());
				if (newObjGroup1 == null || newObjGroup1.getURs().size() == 0)
					continue;
				SampledPoint sp = newObjGroup1.getmPoint();
				int floor = sp.getmFloor();
//				//// =========================================================
//				////  d2d range search 

				if (AlgCSDM.batchProcessing == 1) {
					HashSet<Integer> processedParsID = new HashSet<Integer>(); // storing the already processed par
					HashSet<Integer> processedObjsID = new HashSet<Integer>(); // storing the already processed obj
					int doorIDoffset = IndoorSpace.gNumberDoorsPerFloor * floor;

					// process curPar
					for (int diID : sp.getCurPar().getmDoors()) {
						Door di = IndoorSpace.gDoors.get(diID);
						processG2D(newObjGroup1, di, processedParsID, processedObjsID);
					}

					// same floor
					for (int diID : sp.getCurPar().getmDoors()) {
						for (int djID : IndoorSpace.gD2DIndex[diID - doorIDoffset]) {
							// note that this djID is not floor adjusted

							double d2dDist = IndoorSpace.gD2DMatrix[diID - doorIDoffset][djID];
							double bound = 2 * DataGenConstant.maxVelocity * OTTConstant.maxSamplingPeriod
									- AlgCSDM.distTH - newObjGroup1.getR_min() - AlgCSDM.r_min;
							if (d2dDist > bound)
								break;
//						System.out.println((diID - offset) + " " + djID+ " " + d2dDist + " " + bound);

							// floor fix to get gDoor
							Door dj = IndoorSpace.gDoors.get(djID + doorIDoffset);
							processG2D(newObjGroup1, dj, processedParsID, processedObjsID);
						}
					}

					// other floors

					double minDist2SC = minc2scdiDist(newObjGroup1.getmPoint());
					// consider other floors if it is close to the staircase
					// min dist to staircse - farthest possible moving distance
					double d = minDist2SC - newObjGroup1.getR_min()
							- (OTTConstant.maxSamplingPeriod * DataGenConstant.maxVelocity);
//				System.out.println("minDist2SC: " + minDist2SC + " " + newObjGroup1.getR_min() + " " + d + " " + floor);
					if (d + DataGenConstant.lenStairway > AlgCSDM.distTH) {
						continue;
					}
//				// multiple floor is possible
					for (int j = 0; j < DataGenConstant.nFloor; j++) {
						processedParsID = new HashSet<Integer>();// the par in diff floor must not repeat
						processedObjsID = new HashSet<Integer>();

						if (j == floor)
							continue;
						if (d + Math.abs(floor - j) * DataGenConstant.lenStairway > AlgCSDM.distTH) {
							continue;
						}
//							System.out.println("process floor:" + j);
						int offset2 = IndoorSpace.gNumberDoorsPerFloor * j;// for retrieve D2D dj
						for (Par sc : Distance.stcs) { // for each sc
							double minDist = Constant.DISTMAX;
							int minSC = -1;
							for (int dsc : sc.getmDoors()) {
								double dist = Distance.p2diDist(sp, IndoorSpace.gDoors.get(dsc));
								if (dist < minDist) {
									minDist = dist;
									minSC = dsc;
								}

							}
							for (int djID : IndoorSpace.gD2DIndex[minSC]) {

								if (minSC == djID)
									continue;

								double d2dDist = IndoorSpace.gD2DMatrix[minSC][djID] + minDist2SC
										+ Math.abs(floor - j) * DataGenConstant.lenStairway;
								double bound = 2 * DataGenConstant.maxVelocity * OTTConstant.maxSamplingPeriod
										+ AlgCSDM.distTH + newObjGroup1.getR_min() + AlgCSDM.r_min;
								if (d2dDist > bound)
									break;
//									System.out.println((diID - offset) + " " + djID+ " " + d2dDist + " " + bound);

								// floor fix to get gDoor
								Door dj = IndoorSpace.gDoors.get(djID + offset2);
								processG2D(newObjGroup1, dj, processedParsID, processedObjsID);
							}
						}
					}
				} else {
					//// =========================================================
					//no batch
					// process objects in the same floor
					processG2F(newObjs.get(i).getmID(), newObjGroup1, floor);

					double minDist2SC = minc2scdiDist(newObjGroup1.getmPoint());
					// consider other floors if it is close to the staircase
					// min dist to staircse - farthest possible moving distance
					double d = minDist2SC - newObjGroup1.getR_min()
							- (OTTConstant.maxSamplingPeriod * DataGenConstant.maxVelocity);
//				d = Math.max(d, 0);
//				System.out.println("minDist2SC: " + minDist2SC + " " + newObjGroup1.getR_min() + " " + d + " " + floor);
					if (d + DataGenConstant.lenStairway < AlgCSDM.distTH) {
						// multiple floor is possible
						for (int j = 0; j < DataGenConstant.nFloor; j++) {
							if (j == floor)
								continue;
							if (d + Math.abs(floor - j) * DataGenConstant.lenStairway < AlgCSDM.distTH) {
//							System.out.println("process floor:" + j);
								processG2F(newObjs.get(i).getmID(), newObjGroup1, j);
							}
						}
					}
				}
				// =========================================================
			}
		} // end for
			// ---------------------------------------------------------------------

	}

	/**
	 * process the group to the two partitions associated with dj
	 * 
	 * @param newObjGroup1
	 * @param dj
	 * @param processedParsID
	 */
	private void processG2D(Group newObjGroup1, Door dj, HashSet<Integer> processedParsID,
			HashSet<Integer> processedObjsID) {
		for (int parjID : dj.getmPartitions()) {

			if (processedParsID.contains(parjID)) {
				continue;
			}
			// process o2o
			processedParsID.add(parjID);
			Par parj = IndoorSpace.gPartitions.get(parjID);

			// for each object in the parj
			for (IdrObj o2 : parj.getmObjects()) {
				int o2ID = o2.getmID();

				if (processedObjsID.contains(o2ID))
					continue;

				processedObjsID.add(o2ID);

				int recordTime = IndoorSpace.OTT.getOrDefault(o2ID, -1);
				if (recordTime == -1)
					continue;

				int t_f = Math.min(recordTime + OTTConstant.maxSamplingPeriod, curTime + OTTConstant.predictingPeriod);
//									System.out.println("curTime:" + curTime + " t_f: " + t_f + " recordTime: " +recordTime);

				processG2O(newObjGroup1, o2.getUr(), t_f);
			}
		}
	}

	// return true if all sc in [start,end) is within dist range
	private boolean d2scCheck(double[] d2sc, double[] d2sc2, int start, int end) {

		// TODO Auto-generated method stub
//		for (int i = 0; i < d2sc.length; i++) {
		for (int i = start; i < end; i++) {
//			if (Math.abs(d2sc[i] - d2sc2[i]) > AlgCSDM.distTH)
			if (Math.abs(d2sc[i] - d2sc2[i]) > AlgCSDM.distTH
					+ OTTConstant.predictingPeriod * DataGenConstant.maxVelocity) {
//				System.out.println("prunedbysc!!");
//				System.out.println(d2sc[i]+ " " + d2sc2[i] + "  " + (AlgCSDM.distTH
//					+ OTTGenConstant.maxSamplingPeriod * DataGenConstant.maxVelocity*2));
				return false;
			}
		}

		return true;
	}

	// minimum distance from center to the staircase
	private double minc2scdiDist(SampledPoint sp) {
		// TODO Auto-generated method stub
		double minDist = Constant.DISTMAX;
		double dist = 0;
//		int tLBmin = Integer.MAX_VALUE;
//		int tUBmax = 0;
//
//		int[] tULB = new int[2];
//		List<Integer> doors_128 = Distance.stc_128.getmDoors();
//		for (int d : doors_128) {
//
//			// calculate the time ULB
//			calcTimeULB(newObjGroup1, IndoorSpace.gDoors.get(d), tULB);
//		}

		for (int d : Distance.stc_128.getmDoors()) {
			dist = Distance.p2diDist(sp, IndoorSpace.gDoors.get(d));
			if (dist < minDist)
				minDist = dist;
		}
		for (int d : Distance.stc_129.getmDoors()) {
			dist = Distance.p2diDist(sp, IndoorSpace.gDoors.get(d));
			if (dist < minDist)
				minDist = dist;
		}
		for (int d : Distance.stc_130.getmDoors()) {
			dist = Distance.p2diDist(sp, IndoorSpace.gDoors.get(d));
			if (dist < minDist)
				minDist = dist;
		}
		for (int d : Distance.stc_131.getmDoors()) {
			dist = Distance.p2diDist(sp, IndoorSpace.gDoors.get(d));
			if (dist < minDist)
				minDist = dist;
		}
		return minDist;
	}

	// minimum distance from center to the staircase
	private void c2scdiDists(IndoorUR ur1, double[] dist) {
		// TODO Auto-generated method stub
		Par[] pars = { Distance.stc_128, Distance.stc_129, Distance.stc_130, Distance.stc_131 };

		double minDist = Constant.DISTMAX;
		double temp = 0;

		// for each sc par
		for (int i = 0; i < pars.length; i++) {
			minDist = Constant.DISTMAX;
			temp = 0;
			for (int d : pars[i].getmDoors()) {
				temp = Distance.p2diDist(ur1.getmPoint(), IndoorSpace.gDoors.get(d));
				if (temp < minDist)
					minDist = temp;
			}
			dist[i] = minDist;
		}

		return;
	}

	/**
	 * 
	 * Processing a group to the existing objects in a floor
	 * 
	 * @param mID
	 * @param newObjGroup1
	 * @param floor
	 */
	private void processG2F(int mID, Group newObjGroup1, int floor) {

		List<IdrObj> sm = IndoorSpace.observedObjsList.get(floor);
		for (IdrObj o2 : sm) {

			int o2ID = o2.getmID();
			if (mID == o2ID)
				continue;

			int recordTime = IndoorSpace.OTT.getOrDefault(o2ID, -1);
			if (recordTime == -1)
				continue;

			int t_f = Math.min(recordTime + OTTConstant.maxSamplingPeriod, curTime + OTTConstant.predictingPeriod);
//			System.out.println("curTime:" + curTime + " t_f: " + t_f + " recordTime: " +recordTime);

			processG2O(newObjGroup1, o2.getUr(), t_f);
		}
		return;
	}

	/**
	 * Processing a group/object ur1 to another group/object ur2
	 * 
	 * @param ur1
	 * @param ur2
	 * @param t_f
	 * @param groupingFlag : 1=trigger grouping if pair is found; 0=not
	 * 
	 */
	private void processO2O(IndoorUR ur1, IndoorUR ur2, int t_f, int groupingFlag) {

		SampledPoint sp1 = ur1.getmPoint();
		SampledPoint sp2 = ur2.getmPoint();
		int o1ID = sp1.getObjID();
		int o2ID = sp2.getObjID();
		// ------
		// floor diff. pruning
		if (Math.abs(sp1.getmFloor() - sp2.getmFloor()) * DataGenConstant.lenStairway > AlgCSDM.distTH)
			return;
		// ------
//		System.out.println("----- (" + o1ID + ", " + o2ID + ") @curTime=" + curTime + " --------");

		// actual time range that need to be processed
		// [t_s, t_f]
		int t_s = curTime;

		int[] tULB = new int[2];

		// calculate the time ULB
		calcTimeULB(ur1, ur2, t_f, tULB);

//		System.out.println("tULB[0]: " + tULB[0] + " [1]: " + tULB[1]);

		if (AlgCSDM.betaPruning != -1) {

			if (tULB[0] >= t_f) {
				return;
			}
			if (tULB[0] > t_s) {
				t_s = tULB[0];
			}
			if (tULB[1] >= t_s) {
				insertResult(t_s, o1ID + "/" + o2ID + "v" + df.format(AlgCSDM.distTH));
				if (groupingFlag == 1 && sp1.getmFloor() == sp2.getmFloor()) {
					grouping(ur1, ur2, o2ID);
				}
				return;
			}
		}
//		System.out.println("calculating in time range: [" + t_s + "," + t_f + "]...");

		// ----------
		double beta1 = 1.0;
		double beta2 = 1.0;
		double distLBbyBeta = 0;
		double[] dULB = new double[2];
		calcDistULBType3(curTime, ur1, ur2, dULB);

		double lastDistLBbyBeta = dULB[0] > 0 ? dULB[0] : 0;// could be LB or actual dist
		double distUB = dULB[1];// could be UB or actual dist

		// *upper bound is monotonic increasing, only check the first one is enough
		if (AlgCSDM.betaPruning != -1 && distUB < AlgCSDM.distTH) {
			insertResult(curTime, o1ID + "/" + o2ID + "u" + df.format(distUB));
			if (groupingFlag == 1 && sp1.getmFloor() == sp2.getmFloor()) {
				grouping(ur1, ur2, o2ID);
			}
			return;
		}

		// ----------
		// for each time point in range
		for (int t = curTime; t < t_f; t++) {

			ur1.updateUR(t);
			ur2.updateUR(t);

//			System.out.println(
//					"t: " + t + " beta1: " + df.format(beta1) + " beta2 " + df.format(beta2) + " lastDistLBbyBeta: "
//							+ df.format(lastDistLBbyBeta) + " lastDistUBbyBeta: " + df.format(lastDistUBbyBeta));
			// -----------------------------------
			// beta region pruning
			beta1 = ur1.getBeta(t);
			beta2 = ur2.getBeta(t);

			if (AlgCSDM.betaPruning == 1) {
				// worse case that all new points have the most extreme case
				distLBbyBeta = beta1 * beta2 * lastDistLBbyBeta
						+ (1.0 - beta1 * beta2) * (lastDistLBbyBeta - DataGenConstant.maxVelocity * 2);
			} else {
				distLBbyBeta = lastDistLBbyBeta - DataGenConstant.maxVelocity * 2;
			}

			if (distLBbyBeta < 0)
				distLBbyBeta = 0;

			if (distLBbyBeta > AlgCSDM.distTH) {
				lastDistLBbyBeta = distLBbyBeta;
				continue;
			}
			lastDistLBbyBeta = distLBbyBeta;

			if (t < t_s) {
				continue;
			}
			// -----------------------------------

			// calculate actual expected distance
			double dist = Distance.calcExpDist2(t, ur1, ur2);

			if (dist != 0.0 && dist < AlgCSDM.distTH) {
				insertResult(t, o1ID + "/" + o2ID + ":" + df.format(dist));
				if (groupingFlag == 1 && sp1.getmFloor() == sp2.getmFloor()) {
					grouping(ur1, ur2, o2ID);
				}
				return;/// we only find the first time stamp
			}

			lastDistLBbyBeta = dist;
		} // end for each time point

	}

	/**
	 * put the objects in group ur2 to the group ur1
	 * 
	 * @param ur1
	 * @param ur2
	 * @param o2ID
	 */
	private void grouping(IndoorUR ur1, IndoorUR ur2, int o2ID) {

//		System.out.println("merging...");
		// move all urs from sp2.groupID to sp1.groupID
		Group group1 = newObjGroups.get(ur1.getGroupID());
		int group2ID = ur2.getGroupID();

		// check if it is already merged with other
		if (ur1.getGroupID() != group2ID) {
			group1.addURs(newObjGroups.get(group2ID).getURs());

			// set all objects' group to group1
			for (IndoorUR ur : newObjGroups.get(ur2.getGroupID()).getURs()) {
				ur.setGroupID(ur1.getGroupID());
			}

			// remove group2 to avoid duplicated processing later
			newObjGroups.remove(o2ID);
		}
	}

	/**
	 * Process a group to an object
	 * 
	 * @param spg
	 * @param ur2
	 * @param t_f
	 */
	private void processG2O(Group spg, IndoorUR ur2, int t_f) {

		int t_s = curTime;

		int[] tULB = new int[2];
		calcTimeULBType3(spg, ur2, tULB);

//		System.out.println("455 tULB[0]:" + tULB[0] + " " + tULB[1]);
		if (AlgCSDM.batchProcessing == 1) {

			if (tULB[0] >= t_f) {
				return;
			}
			if (tULB[0] > t_s) {
				t_s = tULB[0];
			}
			if (tULB[1] >= t_s) {
				for (IndoorUR ur1 : spg.getURs()) {
					insertResult(tULB[1], ur1.getmPoint().getObjID() + "/" + ur2.getmPoint().getObjID() + "g"
							+ df.format(AlgCSDM.distTH));
				}
				return;
			}
		}
		// process each ur in this group if not pruned
		for (IndoorUR ur1 : spg.getURs()) {
			processO2O(ur1, ur2, t_f, 0);
		}
	}

	/**
	 * insert s to newResult[time-curTime]
	 *
	 * @param time
	 * @param s
	 */
	private void insertResult(int time, String s) {
//		System.out.println("new result: @t=" + time + " " + s);
		newResult.get(time - curTime).add(s);
	}

	/**
	 * 
	 * calculate the t_{LB} time of two objects by
	 * 
	 * @param sp1
	 * @param sp2
	 * @param t_f
	 * @param curTime
	 * @param distTH
	 * @return min time that need to start actual calculation tLUB[0] = tLB, tLUB[1]
	 *         = tUB i.e., t <tLB can be safety pruned
	 */
	public void calcTimeULB(IndoorUR ur1, IndoorUR ur2, int t_f, int[] tULB) {

		int tt1i = 0;
		int tt2i = 0;
		int type1 = 0;
		int type2 = 0;
		int tLB = -1;
		int tUB = -1;

		List<TypeTime> tt1 = ur1.getTypeTimes();
		List<TypeTime> tt2 = ur2.getTypeTimes();

		int curTypeEndTime1 = tt1.get(0).endTime;
		int curTypeEndTime2 = tt2.get(0).endTime;

		int curTypeStartTime1 = tt1.get(0).startTime;
		int curTypeStartTime2 = tt2.get(0).startTime;

		while (tt1i < tt1.size() && tt2i < tt2.size()) {
			int[] temp = new int[2];

			type1 = tt1.get(tt1i).type;
			type2 = tt2.get(tt2i).type;
			curTypeEndTime1 = tt1.get(tt1i).endTime;
			curTypeEndTime2 = tt2.get(tt2i).endTime;

			curTypeStartTime1 = tt1.get(tt1i).startTime;
			curTypeStartTime2 = tt2.get(tt2i).startTime;

			// - check this pair
			if ((type1 == 1 || type1 == 2) && (type2 == 1 || type2 == 2)) {
				calcTimeULBType12(ur1, ur2, type1, type2, temp);
			} else if (type1 == 3 && type2 == 3) {
				calcTimeULBType3(ur1, ur2, temp);
			} else if (type2 == 3) {
				calcTimeULBType13(ur1, ur2, type1, temp);
			} else if (type1 == 3) {
				calcTimeULBType13(ur2, ur1, type2, temp);
			} else {
				System.err.println("timeLB error!");
				System.exit(-1);
			}

			// 3 cases here:
			// <max{startTime1, startTime2} -> break
			// > max {startTime1, startTime2} && <min{endTime1, endTime2} ->update and break
			// > min{endTime1, endTime2} -> continue;

			int breakCnt = 0;
			if (temp[0] <= Math.min(curTypeEndTime1, curTypeEndTime2)) {
				if (temp[0] >= Math.max(curTypeStartTime1, curTypeStartTime2)) {
					// LB fall in range [start,end]
					tLB = temp[0];
				}
				// LB < startTime
				// previous one is the largest we can get
				breakCnt++;
			} else {
				// if it is larger than this range
				// proceed to next range
				tLB = temp[0];

			}

			if (temp[1] <= Math.min(curTypeEndTime1, curTypeEndTime2)) {
				if (temp[1] >= Math.max(curTypeStartTime1, curTypeStartTime2)) {
					// UB fall in range [start,end]
					tUB = temp[1];
				}
				// UB < startTime
				// previous one is the largest we can get
				breakCnt++;
			} else {
				tUB = temp[1];

			}

			if (breakCnt == 2)
				break;

			// --update endTime and check next slot
			if (curTypeEndTime1 < curTypeEndTime2) {
				tt1i++;
			} else if (curTypeEndTime2 < curTypeEndTime1) {
				tt2i++;
			} else {
				tt1i++;
				tt2i++;
			}
		} // end while

		tULB[0] = tLB;
		tULB[1] = tUB;

		return;
	}

	/**
	 * 
	 * calculate the t_{LB} time of two type 1 objects by
	 * 
	 * @param sp1
	 * @param sp2
	 * @param timeLimit
	 * @param curTime
	 * @param distTH
	 * @return
	 */
	public static void calcTimeULBType12(IndoorUR ur1, IndoorUR ur2, int type1, int type2, int[] tULB) {

		SampledPoint sp1 = ur1.getmPoint();
		SampledPoint sp2 = ur2.getmPoint();
		List<Integer> doors1 = sp1.getCurPar().getmDoors();
		List<Integer> doors2 = sp2.getCurPar().getmDoors();
		double c2cDistance = Distance.p2piDist(sp1, sp2, doors1, doors2);
		double tl1 = ur1.getRecordTime();
		double tl2 = ur1.getRecordTime();

		double r1 = ur1.getR_i();
		double r2 = ur2.getR_i();

		// ---
		if (type1 == 2) {
			r1 = ur1.getRType2();
		}
		if (type2 == 2) {
			r2 = ur2.getRType2();
		}
		// ---

		tULB[0] = (int) (((c2cDistance - r1 - r2 - AlgCSDM.distTH) / DataGenConstant.maxVelocity + tl1 + tl2) / 2);
		tULB[1] = (int) (((c2cDistance + r1 + r2 - AlgCSDM.distTH) / (-DataGenConstant.maxVelocity) + tl1 + tl2) / 2);
		return;
	};

	/**
	 * TODO Lemma 3
	 * 
	 * @param
	 * @param
	 * @param timeLimit
	 * @param curTime
	 * @param distTH
	 * @return
	 */
	public void calcTimeULBType3(IndoorUR ur1, IndoorUR ur2, int[] tULB) {

		SampledPoint sp1 = ur1.getmPoint();
		SampledPoint sp2 = ur2.getmPoint();
		int tl1 = ur1.getRecordTime();
		int tl2 = ur2.getRecordTime();
		List<Par> pars1 = ur1.getCurInPars(tl1);
		List<Par> pars2 = ur2.getCurInPars(tl2);

		double r1 = ur1.getR_i();
		double r2 = ur2.getR_i();

//		System.out.println("pars1:");
//		for(Par p:pars1)
//			System.out.println(p.getmID());
//		System.out.println("pars2:");
//		for(Par p:pars2)
//			System.out.println(p.getmID());

		double minDist = Constant.DISTMAX;
		double maxDist = 0;
		for (Par par1 : pars1) {
			for (Par par2 : pars2) {

				if (par1.getmID() == par2.getmID()) {
					double dist = sp1.eDist(sp2);
					if (dist < minDist) {
						minDist = dist;
					}
					if (dist > maxDist) {
						maxDist = dist;
					}
					continue;
				}

				// ----
				int dd1ID = Distance.par2parDoor(par1, par2);
				int dd2ID = Distance.par2parDoor(par2, par1);
				if (dd1ID != -1 && dd2ID != -1) {
					double dist = sp1.eDist(IndoorSpace.gDoors.get(dd1ID)) + Distance.d2diDist(dd1ID, dd2ID)
							+ sp2.eDist(IndoorSpace.gDoors.get(dd2ID));
					if (dist < minDist) {
						minDist = dist;
					}
					if (dist > maxDist) {
						maxDist = dist;
					}
				} else if (dd1ID != -1) {
					List<Integer> doors2 = par2.getmDoors();
					Door dd1 = IndoorSpace.gDoors.get(dd1ID);
					double dist1 = sp1.eDist(dd1);
					for (Integer door2 : doors2) {
						Door d2 = IndoorSpace.gDoors.get(door2);
						double dist = dist1 + Distance.d2diDist(dd1, d2) + sp2.eDist(d2);
						if (dist < minDist) {
							minDist = dist;
						}
						if (dist > maxDist) {
							maxDist = dist;
						}
					}
				} else if (dd2ID != -1) {
					List<Integer> doors1 = par1.getmDoors();
					Door dd2 = IndoorSpace.gDoors.get(dd2ID);
					double dist2 = sp2.eDist(dd2);
					for (Integer door1 : doors1) {
						Door d1 = IndoorSpace.gDoors.get(door1);
						double dist = dist2 + Distance.d2diDist(d1, dd2) + sp2.eDist(d1);
						if (dist < minDist) {
							minDist = dist;
						}
						if (dist > maxDist) {
							maxDist = dist;
						}
					}
				} else {
					// ----
					List<Integer> doors1 = par1.getmDoors();

					List<Integer> doors2 = par2.getmDoors();
					for (Integer door1 : doors1) {
						for (Integer door2 : doors2) {
							Door d1 = IndoorSpace.gDoors.get(door1);
							Door d2 = IndoorSpace.gDoors.get(door2);
							double dist = sp1.eDist(d1) + Distance.d2diDist(d1, d2) + sp2.eDist(d2);
//						System.out.println("--dist: " + dist + " " +Distance.d2dDistMultifloor(d1, d2) );
							if (dist < minDist) {
								minDist = dist;
							}
							if (dist > maxDist) {
								maxDist = dist;
							}
						}
					}
				}
			}
		}

		tULB[0] = (int) ((((minDist - r1 - r2 - AlgCSDM.distTH) / (DataGenConstant.maxVelocity)) + tl1 + tl2) / 2);
		tULB[1] = (int) ((((maxDist + r1 + r2 - AlgCSDM.distTH) / (-DataGenConstant.maxVelocity)) + tl1 + tl2) / 2);

		return;
	}

	/**
	 * TODO Lemma 3
	 * 
	 * @param
	 * @param
	 * @param timeLimit
	 * @param curTime
	 * @param distTH
	 * @return
	 */
	public void calcTimeULBType3(Group ur1, IndoorUR ur2, int[] tULB) {

		SampledPoint sp1 = ur1.getmPoint();
		SampledPoint sp2 = ur2.getmPoint();
		int tl1 = ur1.getRecordTime();
		int tl2 = ur2.getRecordTime();
		List<Par> pars1 = ur1.getCurInPars(tl1);
		List<Par> pars2 = ur2.getCurInPars(tl2);

		double r1 = ur1.getR_min();
		double r2 = ur2.getR_i();

//		System.out.println("pars1:");
//		for(Par p:pars1)
//			System.out.println(p.getmID());
//		System.out.println("pars2:");
//		for(Par p:pars2)
//			System.out.println(p.getmID());

		double minDist = Constant.DISTMAX;
		double maxDist = 0;
		for (Par par1 : pars1) {
			for (Par par2 : pars2) {

				if (par1.getmID() == par2.getmID()) {
					double dist = sp1.eDist(sp2);
					if (dist < minDist) {
						minDist = dist;
					}
					if (dist > maxDist) {
						maxDist = dist;
					}
					continue;
				}

				// ----
				int dd1ID = Distance.par2parDoor(par1, par2);
				int dd2ID = Distance.par2parDoor(par2, par1);
				if (dd1ID != -1 && dd2ID != -1) {
					double dist = sp1.eDist(IndoorSpace.gDoors.get(dd1ID)) + Distance.d2diDist(dd1ID, dd2ID)
							+ sp2.eDist(IndoorSpace.gDoors.get(dd2ID));
					if (dist < minDist) {
						minDist = dist;
					}
					if (dist > maxDist) {
						maxDist = dist;
					}
				} else if (dd1ID != -1) {
					List<Integer> doors2 = par2.getmDoors();
					Door dd1 = IndoorSpace.gDoors.get(dd1ID);
					double dist1 = sp1.eDist(dd1);
					for (Integer door2 : doors2) {
						Door d2 = IndoorSpace.gDoors.get(door2);
						double dist = dist1 + Distance.d2diDist(dd1, d2) + sp2.eDist(d2);
						if (dist < minDist) {
							minDist = dist;
						}
						if (dist > maxDist) {
							maxDist = dist;
						}
					}
				} else if (dd2ID != -1) {
					List<Integer> doors1 = par1.getmDoors();
					Door dd2 = IndoorSpace.gDoors.get(dd2ID);
					double dist2 = sp2.eDist(dd2);
					for (Integer door1 : doors1) {
						Door d1 = IndoorSpace.gDoors.get(door1);
						double dist = dist2 + Distance.d2diDist(d1, dd2) + sp2.eDist(d1);
						if (dist < minDist) {
							minDist = dist;
						}
						if (dist > maxDist) {
							maxDist = dist;
						}
					}
				} else {
					// ----
					List<Integer> doors1 = par1.getmDoors();

					List<Integer> doors2 = par2.getmDoors();
					for (Integer door1 : doors1) {
						for (Integer door2 : doors2) {
							Door d1 = IndoorSpace.gDoors.get(door1);
							Door d2 = IndoorSpace.gDoors.get(door2);
							double dist = sp1.eDist(d1) + Distance.d2diDist(d1, d2) + sp2.eDist(d2);
//						System.out.println("--dist: " + dist + " " +Distance.d2dDistMultifloor(d1, d2) );
							if (dist < minDist) {
								minDist = dist;
							}
							if (dist > maxDist) {
								maxDist = dist;
							}
						}
					}
				}
			}
		}

		tULB[0] = (int) ((((minDist - r1 - r2 - AlgCSDM.distTH) / (DataGenConstant.maxVelocity)) + tl1 + tl2) / 2);
		tULB[1] = (int) ((((maxDist + r1 + r2 - AlgCSDM.distTH) / (-DataGenConstant.maxVelocity)) + tl1 + tl2) / 2);

		return;
	}

	/**
	 * TODO Lemma 3
	 * 
	 * @param
	 * @param
	 * @param timeLimit
	 * @param curTime
	 * @param distTH
	 * @return
	 */
	public void calcTimeULBType13(IndoorUR ur1, IndoorUR ur2, int type1, int[] tULB) {

		SampledPoint sp1 = ur1.getmPoint();
		SampledPoint sp2 = ur2.getmPoint();
		int tl1 = ur1.getRecordTime();
		int tl2 = ur2.getRecordTime();
		List<Par> pars1 = ur1.getCurInPars(tl1);
		List<Par> pars2 = ur2.getCurInPars(tl2);

		double r1 = ur1.getR_i();
		double r2 = ur2.getR_i();
		if (type1 == 2)
			r1 = ur1.getRType2();

		double minDist = Constant.DISTMAX;
		double maxDist = 0;
//		for (Par par1 : pars1) 
		{

			Par par1 = ur1.getmPoint().getCurPar();
			List<Integer> doors1 = par1.getmDoors();
			for (Par par2 : pars2) {
				List<Integer> doors2 = par2.getmDoors();
				for (Integer door1 : doors1) {
					for (Integer door2 : doors2) {
						Door Door1 = IndoorSpace.gDoors.get(door1);
						Door Door2 = IndoorSpace.gDoors.get(door2);
						double d2dDist = Distance.d2diDist(Door1, Door2);
						double p1d1 = sp1.eDist(Door1);
						double p2d2 = sp2.eDist(Door2);
						double tempLB = p1d1 + d2dDist + p2d2;
						if (tempLB < minDist) {
							minDist = tempLB;
						}
						if (tempLB > maxDist) {
							maxDist = tempLB;
						}
					}
				}
			}
		}

		tULB[0] = (int) (((minDist - r1 - r2 - AlgCSDM.distTH) / DataGenConstant.maxVelocity + tl1 + tl2) / 2);
		tULB[1] = (int) (((maxDist + r1 + r2 - AlgCSDM.distTH) / (-DataGenConstant.maxVelocity) + tl1 + tl2) / 2);

	}

	/**
	 * TODO Lemma 3
	 * 
	 * @param
	 * @param
	 * @param timeLimit
	 * @param curTime
	 * @param distTH
	 * @return
	 */
	public void calcTimeULB(IndoorUR ur1, Door d2, int[] tULB) {

		SampledPoint sp1 = ur1.getmPoint();
		int tl1 = ur1.getRecordTime();
		List<Par> pars1 = ur1.getCurInPars(tl1);

		double r1 = ur1.getR_i();

		double minDist = Constant.DISTMAX;
		double maxDist = 0;
		for (Par par1 : pars1) {
			List<Integer> doors1 = par1.getmDoors();

			if (doors1.contains(d2.getmID())) {
				continue;
			}

			for (Integer door1 : doors1) {
				Door d1 = IndoorSpace.gDoors.get(door1);
				double dist = sp1.eDist(d1) + Distance.d2diDist(d1, d2);
//						System.out.println("--dist: " + dist + " " +Distance.d2dDistMultifloor(d1, d2) );
				if (dist < minDist) {
					minDist = dist;
				}
				if (dist > maxDist) {
					maxDist = dist;
				}
			}
		}

		tULB[0] = (int) (((minDist + r1 + AlgCSDM.distTH) / DataGenConstant.maxVelocity + tl1));
		tULB[1] = (int) (((maxDist + r1 - AlgCSDM.distTH) / DataGenConstant.maxVelocity + tl1));

		return;
	}

	/**
	 * TODO Lemma 3
	 * 
	 * @param
	 * @param
	 * @param timeLimit
	 * @param curTime
	 * @param distTH
	 * @return the LB and UB at time t
	 */
	public void calcDistULBType3(int t, IndoorUR ur1, IndoorUR ur2, double[] dULB) {

		SampledPoint sp1 = ur1.getmPoint();
		SampledPoint sp2 = ur2.getmPoint();
		int tl1 = ur1.getRecordTime();
		int tl2 = ur2.getRecordTime();
		List<Par> pars1 = ur1.getCurInPars(t);
		List<Par> pars2 = ur2.getCurInPars(t);

		double r1 = ur1.getR_i();
		double r2 = ur2.getR_i();

//		System.out.println("pars1:");
//		for(Par p:pars1)
//			System.out.println(p.getmID());
//		System.out.println("pars2:");
//		for(Par p:pars2)
//			System.out.println(p.getmID());

		double minDist = Constant.DISTMAX;
		double maxDist = 0;
		for (Par par1 : pars1) {
			for (Par par2 : pars2) {

				if (par1.getmID() == par2.getmID()) {
					double dist = sp1.eDist(sp2);
					if (dist < minDist) {
						minDist = dist;
					}
					if (dist > maxDist) {
						maxDist = dist;
					}
					continue;
				}

				// ----
				int dd1ID = Distance.par2parDoor(par1, par2);
				int dd2ID = Distance.par2parDoor(par2, par1);
				if (dd1ID != -1 && dd2ID != -1) {
					double dist = sp1.eDist(IndoorSpace.gDoors.get(dd1ID)) + Distance.d2diDist(dd1ID, dd2ID)
							+ sp2.eDist(IndoorSpace.gDoors.get(dd2ID));
					if (dist < minDist) {
						minDist = dist;
					}
					if (dist > maxDist) {
						maxDist = dist;
					}
				} else if (dd1ID != -1) {
					List<Integer> doors2 = par2.getmDoors();
					Door dd1 = IndoorSpace.gDoors.get(dd1ID);
					double dist1 = sp1.eDist(dd1);
					for (Integer door2 : doors2) {
						Door d2 = IndoorSpace.gDoors.get(door2);
						double dist = dist1 + Distance.d2diDist(dd1, d2) + sp2.eDist(d2);
						if (dist < minDist) {
							minDist = dist;
						}
						if (dist > maxDist) {
							maxDist = dist;
						}
					}
				} else if (dd2ID != -1) {
					List<Integer> doors1 = par1.getmDoors();
					Door dd2 = IndoorSpace.gDoors.get(dd2ID);
					double dist2 = sp2.eDist(dd2);
					for (Integer door1 : doors1) {
						Door d1 = IndoorSpace.gDoors.get(door1);
						double dist = dist2 + Distance.d2diDist(d1, dd2) + sp2.eDist(d1);
						if (dist < minDist) {
							minDist = dist;
						}
						if (dist > maxDist) {
							maxDist = dist;
						}
					}
				} else {
					// ----
					List<Integer> doors1 = par1.getmDoors();

					List<Integer> doors2 = par2.getmDoors();
					for (Integer door1 : doors1) {
						for (Integer door2 : doors2) {
							Door d1 = IndoorSpace.gDoors.get(door1);
							Door d2 = IndoorSpace.gDoors.get(door2);
							double dist = sp1.eDist(d1) + Distance.d2diDist(d1, d2) + sp2.eDist(d2);
//						System.out.println("--dist: " + dist + " " +Distance.d2dDistMultifloor(d1, d2) );
							if (dist < minDist) {
								minDist = dist;
							}
							if (dist > maxDist) {
								maxDist = dist;
							}
						}
					}
				}
			}
		}
		dULB[0] = minDist - r1 - r2 - ((t - tl1) + (t - tl2)) * DataGenConstant.maxVelocity;
		dULB[1] = maxDist + r1 + r2 + ((t - tl1) + (t - tl2)) * DataGenConstant.maxVelocity;

		return;
	}

	// -----------------------------------------------

	public static long totalRunTime = 0;
	public static long totalMem = 0;
	public static long cntRun = 0;

	@Override
	public ArrayList<ArrayList<String>> call() throws Exception {

		// TODO Auto-generated method stub
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long startMem = runtime.totalMemory() - runtime.freeMemory();
		long start = System.nanoTime();

		processBatch(); // processing.

		long end = System.nanoTime();
		long endMem = runtime.totalMemory() - runtime.freeMemory();

		long time = end - start;
		long memory = (endMem - startMem) / 1024;

		totalRunTime += time;
		totalMem += memory;
		cntRun++;

//		System.out.println("stat @t=" + curTime + " " + "cnt:" + QueryUpdate.cntRun);
//		System.out.println("cur Run Time: " + time + " " + " (" + (double) time / 1000000000.0 + "s)");
//		System.out.println("totalRunTime/cnt:" + QueryUpdate.totalRunTime / QueryUpdate.cntRun + " ("
//				+ (double) QueryUpdate.totalRunTime / (QueryUpdate.cntRun * 1000000000.0) + "s)");
//		System.out.println("totalMem/cnt:" + QueryUpdate.totalMem / QueryUpdate.cntRun + " ");

		return newResult;
	}

}
