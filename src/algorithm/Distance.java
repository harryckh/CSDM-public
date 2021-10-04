package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import dk.aau.cs.idq.indoorentities.*;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;

public class Distance {
	// 4 staircases 
	public static Par stc_128 = IndoorSpace.gPartitions.get(128);
	public static Par stc_129 = IndoorSpace.gPartitions.get(129);
	public static Par stc_130 = IndoorSpace.gPartitions.get(130);
	public static Par stc_131 = IndoorSpace.gPartitions.get(131);

	public static Par[] stcs = { stc_128, stc_129, stc_130, stc_131 };


	/**
	 * calculate the expected distance between two URs in multifloors with
	 * probability consideration
	 * 
	 * @param ur1
	 * @param ur2
	 * @return
	 */
	public static double calcExpDist(int curTime, IndoorUR ur1, IndoorUR ur2) {
		double dist = 0;
		List<Point> points1 = ur1.getGaussianPoints(curTime);
		List<Point> points2 = ur2.getGaussianPoints(curTime);


		if (points1.size() == 0 || points2.size() == 0)
			return 0;

		if (ur1.findCurType(curTime) == 1 && ur2.findCurType(curTime) == 1) {
			// special case: both have only one door
			if (ur1.getmPoint().getCurPar().getmDoors().size() == 1
					&& ur2.getmPoint().getCurPar().getmDoors().size() == 1) {
				Door di = IndoorSpace.gDoors.get(ur1.getmPoint().getCurPar().getmDoors().get(0));
				Door dj = IndoorSpace.gDoors.get(ur2.getmPoint().getCurPar().getmDoors().get(0));

				double dij = d2diDist(di, dj); // calculate d2d indoor distance

				dist += dij;

				for (Point p1 : points1) {
					dist += p1.eDist(di) * p1.getProb();
				}
				for (Point p2 : points2) {
					dist += p2.eDist(dj) * p2.getProb();
				}

				return dist;
			}

			// find the shortest distance for each point pair
			List<Integer> doors1 = ur1.getmPoint().getCurPar().getmDoors();
			List<Integer> doors2 = ur2.getmPoint().getCurPar().getmDoors();
			for (Point p1 : points1) {
				for (Point p2 : points2) {
					double shortestDist = p2piDist(p1, p2, doors1, doors2);
//                        dist += shortestDist*proMap1.get(p1)*proMap2.get(p2);
					dist += shortestDist * p1.getProb() * p2.getProb();
				}
			}
//                dist = dist / (points1.size() * points2.size());

			return dist;

		}

		// not both type 1
		for (Point p1 : points1) {
//			System.out.println(p1.toString());
			List<Integer> doors1 = p1.getCurrentPar().getmDoors();
			for (Point p2 : points2) {
				double shortestDist = p2piDist(p1, p2, doors1, p2.getCurrentPar().getmDoors());
//					System.out.println(shortestDist + " " + p1.getProb() + " " + p2.getProb());
//                    dist += shortestDist*proMap1.get(p1)*proMap2.get(p2);
				dist += shortestDist * p1.getProb() * p2.getProb();
			}
		}

		return dist;

	}

	/**
	 * calculate the expected distance between two URs in multifloors with
	 * probability consideration
	 * 
	 * @param ur1
	 * @param ur2
	 * @return
	 */
	public static double calcExpDist2(int curTime, IndoorUR ur1, IndoorUR ur2) {
		double dist = 0;
		List<Point> points1 = ur1.getGaussianPoints(curTime);
		List<Point> points2 = ur2.getGaussianPoints(curTime);


		if (points1.size() == 0 || points2.size() == 0)
			return 0;

		if (ur1.findCurType(curTime) == 1 && ur2.findCurType(curTime) == 1) {

			return calcExpDistSub(curTime, points1, points2, ur1.getmPoint().getCurPar(), ur2.getmPoint().getCurPar());
		}

		// ----
		// from each sub-region in oi
		// to each sub-region in oj
		HashMap<Integer, ArrayList<Point>> cntHashMap1 = ur1.getPtsParMap(curTime);
		HashMap<Integer, ArrayList<Point>> cntHashMap2 = ur2.getPtsParMap(curTime);

//		HashMap<Integer, ArrayList<Point>> cntHashMap1 = new HashMap<>();
//		HashMap<Integer, ArrayList<Point>> cntHashMap2 = new HashMap<>();
		if (cntHashMap1.size() == 0) {
			for (Point p : points1) {
				cntHashMap1.computeIfAbsent(p.getCurrentPar().getmID(), k -> new ArrayList<>()).add(p);
			}
		}
		if (cntHashMap2.size() == 0) {
			for (Point p : points2) {
				cntHashMap2.computeIfAbsent(p.getCurrentPar().getmID(), k -> new ArrayList<>()).add(p);
			}
		}

		// part to part dist
		for (Entry<Integer, ArrayList<Point>> entry1 : cntHashMap1.entrySet()) {
			for (Entry<Integer, ArrayList<Point>> entry2 : cntHashMap2.entrySet()) {
				dist += calcExpDistSub(curTime, entry1.getValue(), entry2.getValue(),
						IndoorSpace.gPartitions.get(entry1.getKey()), IndoorSpace.gPartitions.get(entry2.getKey()));
//				System.out.println("temp:" + temp);
//				dist += temp;
			}
		}

		return dist;

	}

	/**
	 * Type 1.
	 * 
	 * @param curTime
	 * @param pts1    - points in par1
	 * @param pts2    - points in par2
	 * @param par1
	 * @param par2
	 * @return expected distance for points in par1 and par2 only
	 */
	private static double calcExpDistSub(int curTime, List<Point> pts1, List<Point> pts2, Par par1, Par par2) {
		double dist = 0;
		// if they are the same part.
		if (par1.getmID() == par2.getmID()) {
			for (Point pt1 : pts1) {
				for (Point pt2 : pts2) {
					dist += pt1.eDist(pt2) * pt1.getProb() * pt2.getProb();
				}
			}
			return dist;
		}

		int di = par2parDoor(par1, par2);
		int dj = par2parDoor(par2, par1);

//		System.out.println("di:" + di + " dj:" + dj + " " + par1.getmDoors() + " " + par2.getmDoors());
		if (di != -1 && dj != -1) {
			double dij = d2diDist(di, dj); // calculate d2d indoor distance
			double dist1 = 0.0;
			double dist2 = 0.0;
			double sum1 = 0.0;
			double sum2 = 0.0;
			Door ddi = IndoorSpace.gDoors.get(di);
			Door ddj = IndoorSpace.gDoors.get(dj);
			for (Point p1 : pts1) {
				dist1 += p1.eDist(ddi) * p1.getProb();
				sum1 += p1.getProb();
			}
			for (Point p2 : pts2) {
				dist2 += p2.eDist(ddj) * p2.getProb();
				sum2 += p2.getProb();
			}
			dist = dist1 * sum2 + dist2 * sum1 + dij * sum1 * sum2;

			return dist;
		} else if (di != -1) {
			return p2piDistFromDi(pts1, pts2, di, par2parDoors2(par2, par1));
		} else if (dj != -1) {
			return p2piDistFromDi(pts2, pts1, dj, par2parDoors2(par1, par2));
		}

		// otherwise
//		List<Integer> doors1 = par1.getmDoors();
//		List<Integer> doors2 = par2.getmDoors();

		int[] doors1 = par2parDoors2(par1, par2);
		int[] doors2 = par2parDoors2(par2, par1);


		for (Point p1 : pts1) {
			double distTemp = 0.0;
			for (Point p2 : pts2) {
				distTemp += p2piDist2(p1, p2, doors1, doors2) * p2.getProb();
			}
			dist += distTemp * p1.getProb();
		}

		return dist;
	}

	private static double p2piDistFromDi(List<Point> pts1, List<Point> pts2, int diID, List<Integer> doors2) {
		double dist;
		double dist1 = 0.0;
		double dist2 = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;

		Door di = IndoorSpace.gDoors.get(diID);

		for (Point p1 : pts1) {
			dist1 += p1.eDist(di) * p1.getProb();
			sum1 += p1.getProb();
		}

		for (Point p2 : pts2) {
			dist2 += p2diDist(diID, p2, doors2) * p2.getProb();
			sum2 += p2.getProb();
		}

		return dist1 * sum2 + dist2 * sum1;
//		return dist;
	}

	private static double p2piDistFromDi(List<Point> pts1, List<Point> pts2, int diID, int[] doors2) {
		double dist1 = 0.0;
		double dist2 = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;

		Door di = IndoorSpace.gDoors.get(diID);

		for (Point p1 : pts1) {
			dist1 += p1.eDist(di) * p1.getProb();
			sum1 += p1.getProb();
		}

		for (Point p2 : pts2) {
			dist2 += p2diDist(diID, p2, doors2) * p2.getProb();
			sum2 += p2.getProb();
		}

		return dist1 * sum2 + dist2 * sum1;
//		return dist;
	}

	/**
	 * Calculate indoor distance between two doors
	 * 
	 * Floor checking is included
	 * 
	 * @param di
	 * @param dj
	 * @return
	 */
	public static double d2diDist(Door di, Door dj) {

		if (di.getmFloor() == dj.getmFloor()) {
			return d2diDistSameFloor(di, dj);
		} else {
			return d2diDistDiffFloors(di, dj);
		}
	}

	public static double d2diDist(int di, int dj) {
		return d2diDist(IndoorSpace.gDoors.get(di), IndoorSpace.gDoors.get(dj));
	}

	/*
	 * Find the indoor dist in pre-computed matrix
	 */
	public static double d2diDistSameFloor(Door di, Door dj) {
		// di and dj are located in same floor
		int offseti = IndoorSpace.gNumberDoorsPerFloor * di.getmFloor();
		int offsetj = IndoorSpace.gNumberDoorsPerFloor * dj.getmFloor();
		double dij = IndoorSpace.gD2DMatrix[di.getmID() - offseti][dj.getmID() - offsetj];
		return dij;
	}

	/*
	 * Find the indoor dist with the consideration of staircase + staircase length
	 * in the middle
	 */
	public static double d2diDistDiffFloors(Door di, Door dj) {
		double staircase_length = Math.abs(di.getmFloor() - dj.getmFloor()) * DataGenConstant.lenStairway;
		int offset_i = IndoorSpace.gNumberDoorsPerFloor * di.getmFloor();
		int offset_j = IndoorSpace.gNumberDoorsPerFloor * dj.getmFloor();
		List<Integer> doors_128 = stc_128.getmDoors();
		List<Integer> doors_129 = stc_129.getmDoors();
		List<Integer> doors_130 = stc_130.getmDoors();
		List<Integer> doors_131 = stc_131.getmDoors();
		double minDist = Constant.DISTMAX;
		for (int d128 : doors_128) {
			if (IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d128]
					+ IndoorSpace.gD2DMatrix[d128][dj.getmID() - offset_j] < minDist) {
				minDist = IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d128]
						+ IndoorSpace.gD2DMatrix[d128][dj.getmID() - offset_j];
			}
		}
		for (int d129 : doors_129) {
			if (IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d129]
					+ IndoorSpace.gD2DMatrix[d129][dj.getmID() - offset_j] < minDist) {
				minDist = IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d129]
						+ IndoorSpace.gD2DMatrix[d129][dj.getmID() - offset_j];
			}
		}
		for (int d130 : doors_130) {
			if (IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d130]
					+ IndoorSpace.gD2DMatrix[d130][dj.getmID() - offset_j] < minDist) {
				minDist = IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d130]
						+ IndoorSpace.gD2DMatrix[d130][dj.getmID() - offset_j];
			}
		}
		for (int d131 : doors_131) {
			if (IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d131]
					+ IndoorSpace.gD2DMatrix[d131][dj.getmID() - offset_j] < minDist) {
				minDist = IndoorSpace.gD2DMatrix[di.getmID() - offset_i][d131]
						+ IndoorSpace.gD2DMatrix[d131][dj.getmID() - offset_j];
			}
		}
		return minDist + staircase_length;
	}

	public static double p2piDist(Point p1, Point p2, List<Integer> doors1, List<Integer> doors2) {

		if (p1.getCurrentPar() == p2.getCurrentPar()) {
			return p1.eDist(p2);
		}

		double shortestDist = Constant.DISTMAX;
		// try distance from each pair of di to dj

//		System.out.println("p2piDist: " + p1.toString() + " " + p2.toString());
//		System.out.println(doors1);
//		System.out.println(doors2);
		for (int d1 : doors1) {
			Door di = IndoorSpace.gDoors.get(d1);
			double temp = p1.eDist(di);
			for (int d2 : doors2) {
				Door dj = IndoorSpace.gDoors.get(d2);
				double tempDist = temp + d2diDist(di, dj) + p2.eDist(dj);
				if (tempDist < shortestDist) {
					shortestDist = tempDist;
				}
			}
		}
		return shortestDist;
	}

	public static double p2piDist2(Point p1, Point p2, List<Integer> doors1, List<Integer> doors2) {

		double shortestDist = Constant.DISTMAX;
		// try distance from each pair of di to dj

		for (int i = 0; i < doors1.size(); i++) {
			for (int j = 0; j < doors2.size(); j++) {
				double tempDist = p1.geteDist2Door(i) + d2diDist(doors1.get(i), doors2.get(j)) + p2.geteDist2Door(j);
				if (tempDist < shortestDist) {
					shortestDist = tempDist;
				}
			}
		}
		return shortestDist;
	}

	public static double p2piDist2(Point p1, Point p2, int[] doors1, int[] doors2) {

		double shortestDist = Constant.DISTMAX;
		// try distance from each pair of di to dj

		for (int i = 0; i < doors1.length; i++) {
			if (doors1[i] == -1)
				continue;
			for (int j = 0; j < doors2.length; j++) {
				if (doors2[j] == -1)
					continue;
				double tempDist = p1.geteDist2Door(i) + d2diDist(doors1[i], doors2[j]) + p2.geteDist2Door(j);
				if (tempDist < shortestDist) {
					shortestDist = tempDist;
				}
			}
		}
		return shortestDist;
	}

	public static double p2diDist(int d1, Point p2, List<Integer> doors2) {

		int temp = doors2.indexOf(d1);
		if (temp != -1) {
			return p2.geteDist2Door(temp);
		}

		double shortestDist = Constant.DISTMAX;

		for (int j = 0; j < doors2.size(); j++) {
			double tempDist = d2diDist(d1, doors2.get(j)) + p2.geteDist2Door(j);
			if (tempDist < shortestDist) {
				shortestDist = tempDist;
			}
		}
		return shortestDist;
	}

	public static double p2diDist(int d1, Point p2, int[] doors2) {

		for (int j = 0; j < doors2.length; j++) {
			if (doors2[j] == d1)
				return p2.geteDist2Door(j);
		}

		double shortestDist = Constant.DISTMAX;

		for (int j = 0; j < doors2.length; j++) {
			if (doors2[j] == -1)
				continue;
			double tempDist = d2diDist(d1, doors2[j]) + p2.geteDist2Door(j);
			if (tempDist < shortestDist) {
				shortestDist = tempDist;
			}
		}
		return shortestDist;
	}

	public static double p2piDist(SampledPoint p1, SampledPoint p2, List<Integer> doors1, List<Integer> doors2) {

		if (p1.getCurPar() == p2.getCurPar()) {
			return p1.eDist(p2);
		}

		double shortestDist = Constant.DISTMAX;
		// try distance from each pair of di to dj

		for (int d1 : doors1) {
			Door di = IndoorSpace.gDoors.get(d1);
			double temp = p1.eDist(di);
			for (int d2 : doors2) {
				Door dj = IndoorSpace.gDoors.get(d2);
				double tempDist = temp + d2diDist(di, dj) + p2.eDist(dj);
				if (tempDist < shortestDist) {
					shortestDist = tempDist;
				}
			}
		}
		return shortestDist;
	}

	public static double p2diDist(SampledPoint p1, Door d2) {
		List<Integer> doors1 = p1.getCurPar().getmDoors();
		if (doors1.contains(d2.getmID())) {
			return p1.eDist(d2);
		}

		double shortestDist = Constant.DISTMAX;
		// try distance from each pair of di to dj

		for (int d1 : doors1) {
			Door di = IndoorSpace.gDoors.get(d1);
			double tempDist = p1.eDist(di) + d2diDist(di, d2);
			if (tempDist < shortestDist) {
				shortestDist = tempDist;
			}
		}
		return shortestDist;
	}

	/**
	 * 
	 * @param par1
	 * @param par2
	 * @return dominate doorID of par1 if exist, -1 otherwise
	 */
	public static int par2parDoor(Par par1, Par par2) {

		if (par1.getmFloor() == par2.getmFloor()) {
//			System.out.println("aa " + IndoorSpace.gP2PMatrix[par1.getmID() % IndoorSpace.gP2PMatrix.length][par2.getmID()
//					% IndoorSpace.gP2PMatrix.length] + " " + IndoorSpace.gNumberDoorsPerFloor * par1.getmFloor());
			int temp = IndoorSpace.gP2PMatrix[par1.getmID() % IndoorSpace.gP2PMatrix.length][par2.getmID()
					% IndoorSpace.gP2PMatrix.length];
			if (temp == -1)
				return -1;
			return temp + IndoorSpace.gNumberDoorsPerFloor * par1.getmFloor();
		}

		// return the door if all fours staircases use this door
		int d = IndoorSpace.gP2PMatrix[par1.getmID() % IndoorSpace.gP2PMatrix.length][stcs[0].getmID()];
		if (d == -1)
			return -1;
		for (Par sc : stcs) {
			if (d != IndoorSpace.gP2PMatrix[par1.getmID() % IndoorSpace.gP2PMatrix.length][sc.getmID()])
				return -1;
		}
		return d + IndoorSpace.gNumberDoorsPerFloor * par1.getmFloor();
	}

	/**
	 * 
	 * @param par1
	 * @param par2
	 * @return dominate doorID of par1 if exist, -1 otherwise
	 */
	public static int[] par2parDoors2(Par par1, Par par2) {

		if (par1.getmFloor() == par2.getmFloor()) {
//			System.out.println("aa " + IndoorSpace.gP2PMatrix[par1.getmID() % IndoorSpace.gP2PMatrix.length][par2.getmID()
//					% IndoorSpace.gP2PMatrix.length] + " " + IndoorSpace.gNumberDoorsPerFloor * par1.getmFloor());
			int[] temp = Arrays.copyOf(
					IndoorSpace.gP2PMatrix2[par1.getmID() % IndoorSpace.gP2PMatrix2.length][par2.getmID()
							% IndoorSpace.gP2PMatrix2.length],
					IndoorSpace.gP2PMatrix2[par1.getmID() % IndoorSpace.gP2PMatrix2.length][par2.getmID()
							% IndoorSpace.gP2PMatrix2.length].length);
			for (int i = 0; i < temp.length; i++) {
				if (temp[i] != -1)
					temp[i] += IndoorSpace.gNumberDoorsPerFloor * par1.getmFloor();
			}
			return temp;
		}

		// include the door if any of the staircase use this door
		int[] temp = Arrays.copyOf(
				IndoorSpace.gP2PMatrix2[par1.getmID() % IndoorSpace.gP2PMatrix2.length][stcs[0].getmID()],
				IndoorSpace.gP2PMatrix2[par1.getmID() % IndoorSpace.gP2PMatrix2.length][stcs[0].getmID()].length);
		for (int k = 0; k < temp.length; k++) {

			if (temp[k] != -1)
				continue;

			int doorID = -1;
			for (int i = 1; i < stcs.length; i++) {
				doorID = IndoorSpace.gP2PMatrix2[par1.getmID() % IndoorSpace.gP2PMatrix2.length][stcs[i].getmID()][k];
				if (doorID != -1) {
					break;
				}

			}
			if (doorID != -1) {
				temp[k] = doorID;
			}
		}
		return temp;
	}
}
