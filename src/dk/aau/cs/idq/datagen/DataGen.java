package dk.aau.cs.idq.datagen;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import algorithm.AlgCSDM;
import dk.aau.cs.idq.indoorentities.AccsGraph;
import dk.aau.cs.idq.indoorentities.D2Ddistance;
import dk.aau.cs.idq.indoorentities.Door;
import dk.aau.cs.idq.indoorentities.IdrObj;
import dk.aau.cs.idq.indoorentities.IndoorSpace;
import dk.aau.cs.idq.indoorentities.Par;
import dk.aau.cs.idq.indoorentities.Point;
import dk.aau.cs.idq.indoorentities.Rect;
import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.D2D;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.Functions;
import dk.aau.cs.idq.utilities.RoomType;

/**
 * Generate the synthetic raw data for experiment use 1. the whole indoor space
 * 2. the indoor moving objects 3. the trajectories of moving objects during a
 * certain period
 * 
 * @author lihuan
 * @version 0.1 / 2014.09.06
 * 
 */
public class DataGen {

	public static String outputPath = System.getProperty("user.dir");

	/**
	 * reflects the existing partitions in one axis(parameter), pivot is as well
	 * given
	 * 
	 * @param axis
	 * @param pivot
	 */
	public void reflection(int axis, int pivot) {

		int curSize = IndoorSpace.gPartitions.size();

		int xa, xb, ya, yb, x0, y0, x1, x2, y1, y2, type;

		for (int i = 0; i < curSize; i++) {

			x1 = (int) IndoorSpace.gPartitions.get(i).getX1();
			x2 = (int) IndoorSpace.gPartitions.get(i).getX2();
			y1 = (int) IndoorSpace.gPartitions.get(i).getY1();
			y2 = (int) IndoorSpace.gPartitions.get(i).getY2();
			type = IndoorSpace.gPartitions.get(i).getmType();

			if (0 == axis) {
				x0 = pivot;
				xa = x0 + (x0 - x2);
				xb = x0 + (x0 - x1);
				ya = y1;
				yb = y2;
			} else if (1 == axis) {
				y0 = pivot;
				xa = x1;
				xb = x2;
				ya = y0 + (y0 - y1);
				yb = y0 + (y0 - y2);
			} else {
				return;
			}

			Par pNew = new Par(Math.min(xa, xb), Math.max(xa, xb), Math.min(ya, yb), Math.max(ya, yb), type);

			IndoorSpace.gPartitions.add(pNew);

		}

	}

	/**
	 * reflects the existing partitions in one axis(parameter), pivot = 30 (the
	 * median line)
	 * 
	 * @param axis
	 */
	public void reflection(int axis) {
		int pivot = 30;
		reflection(axis, pivot);
	}

	/**
	 * generates the partitions in the left-top part of a single floor
	 * 
	 */
	public void initPartitions() {

		int id = 0;

		Par p1 = new Par(0.0, 0.0 + 10, 0.0, 10.0);
		IndoorSpace.gPartitions.add(p1);

		for (int i = 0; i < 2; i++) {
			Par p2 = new Par(5.0, 10.0, 10.0 + 3 * i, 13.0 + 3 * i);
			IndoorSpace.gPartitions.add(p2);
		}

		for (int i = 0; i < 3; i++) {
			Par p2 = new Par(5.0, 10.0, 16.0 + 4 * i, 16.0 + 4 + 4 * i);
			IndoorSpace.gPartitions.add(p2);
		}

		for (int i = 0; i < 2; i++) {
			Par p2 = new Par(10 + 3 * i, 10 + 3 + 3 * i, 5, 10);
			IndoorSpace.gPartitions.add(p2);
		}

		for (int i = 0; i < 3; i++) {
			Par p2 = new Par(16 + 4 * i, 16 + 4 + 4 * i, 5, 10);
			IndoorSpace.gPartitions.add(p2);
		}

		Par p2 = new Par(12, 16, 12, 16);
		IndoorSpace.gPartitions.add(p2);

		for (int i = 0; i < 3; i++) {
			Par p3 = new Par(16 + 4 * i, 16 + 4 + 4 * i, 12, 16);
			IndoorSpace.gPartitions.add(p3);
		}

		for (int i = 0; i < 3; i++) {
			Par p3 = new Par(12, 16, 16 + 4 * i, 16 + 4 + 4 * i);
			IndoorSpace.gPartitions.add(p3);
		}

		p2 = new Par(24, 28, 16, 20);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(24, 28, 20, 21, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(24, 26, 21, 24);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(26, 28, 21, 24);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(16, 20, 24, 28, id++);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(20, 21, 24, 28, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(21, 24, 24, 26);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(21, 24, 26, 28);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(10, 12, 10, 12, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(10, 12, 12, 20, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(10, 12, 20, 28, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(12, 20, 10, 12, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(20, 28, 10, 12, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		// hallway
		p2 = new Par(16, 24, 16, 24, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

	}

	/**
	 * generates the staircase
	 * 
	 */
	public void genStaircase() {

		Par p2 = new Par(5, 10, 28, 32, RoomType.STAIRCASE);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(28, 32, 5, 10, RoomType.STAIRCASE);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(28, 32, 50, 55, RoomType.STAIRCASE);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(50, 55, 28, 32, RoomType.STAIRCASE);
		IndoorSpace.gPartitions.add(p2);

	}

	/**
	 * generates the hallway
	 * 
	 */
	public void genHallway() {

		Par p2 = new Par(10, 12, 28, 32, 1);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(12, 24, 28, 32, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(28, 32, 10, 12, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(28, 32, 12, 24, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(28, 32, 36, 48, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(28, 32, 48, 50, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(36, 48, 28, 32, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(48, 50, 28, 32, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

		p2 = new Par(24, 36, 24, 36, RoomType.HALLWAY);
		IndoorSpace.gPartitions.add(p2);

	}

	/**
	 * generates the doors as entrances for each partition
	 * 
	 */
	public void initDoors() {

		Iterator<Par> itr = IndoorSpace.gPartitions.iterator();

		while (itr.hasNext()) {
			Par curPar = itr.next();
			Door aDoor;

			if (curPar.getmID() >= 1 && curPar.getmID() <= 5) {
				aDoor = new Door(curPar.getX2(), (curPar.getY1() + curPar.getY2()) / 2);
				IndoorSpace.gDoors.add(aDoor);
			}

			if (curPar.getmID() >= 26 && curPar.getmID() <= 28) {
				aDoor = new Door((curPar.getX1() + curPar.getX2()) / 2, curPar.getY2());
				IndoorSpace.gDoors.add(aDoor);
			}

			if (curPar.getmID() >= 11 && curPar.getmID() <= 17) {
				aDoor = new Door(curPar.getX1(), (curPar.getY1() + curPar.getY2()) / 2);
				IndoorSpace.gDoors.add(aDoor);
			}

			if (curPar.getmID() == 15 || curPar.getmID() == 16) {
				aDoor = new Door(curPar.getX2(), (curPar.getY1() + curPar.getY2()) / 2);
				IndoorSpace.gDoors.add(aDoor);
			}

			if (curPar.getmID() == 17 || curPar.getmID() == 25) {
				aDoor = new Door((curPar.getX1() + curPar.getX2()) / 2, curPar.getY2());
				IndoorSpace.gDoors.add(aDoor);
			}

			if (curPar.getmID() >= 22 && curPar.getmID() <= 24) {
				Door aDoor1 = new Door((curPar.getX1() + curPar.getX2()) / 2, curPar.getY1());
				Door aDoor2 = new Door((curPar.getX1() + curPar.getX2()) / 2, curPar.getY2());
				IndoorSpace.gDoors.add(aDoor1);
				IndoorSpace.gDoors.add(aDoor2);
			}

		}

	}

	/**
	 * tests if the door is the entrance of the partition, if it is, link them
	 * together
	 * 
	 */
	public void linkGraph() {

		Iterator<Par> itrPar = IndoorSpace.gPartitions.iterator();

		while (itrPar.hasNext()) {
			Par curPar = itrPar.next();
			Iterator<Door> itrDoor = IndoorSpace.gDoors.iterator();
			while (itrDoor.hasNext()) {
				Door curDoor = itrDoor.next();
				if (curPar.testDoor(curDoor) == true) {
					curPar.addDoor(curDoor.getmID());
					curDoor.addPar(curPar.getmID());
				}
			}
		}
	}

	/**
	 * load D2D Matrix
	 */
	public void loadD2DMatrix() {
		String d2dDir = outputPath + "/D2DMatrix.txt";
		File file = new File(d2dDir);
		if (!file.exists()) {
			FileWriter fw;
			try {
				fw = new FileWriter(d2dDir);
				for (int i = 0; i < IndoorSpace.gD2DMatrix.length; i++) {
					IndoorSpace.gD2DMatrix[i][i] = 0;
					for (int j = i + 1; j < IndoorSpace.gD2DMatrix.length; j++) {
						D2D d2d = new D2D();
						D2Ddistance result = d2d.d2dDistance(IndoorSpace.gDoors.get(i), IndoorSpace.gDoors.get(j));
						// System.out.println(result);
						IndoorSpace.gD2DMatrix[i][j] = result.getDistance();
						IndoorSpace.gD2DMatrix[j][i] = result.getDistance();
					}
				}
				for (int i = 0; i < IndoorSpace.gD2DMatrix.length; i++) {
					String templine = "";
					for (int j = 0; j < IndoorSpace.gD2DMatrix.length; j++) {
						templine = templine + IndoorSpace.gD2DMatrix[i][j] + " ";
					}
					fw.write(templine + "\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			try {
				FileReader fr = new FileReader(d2dDir);
				BufferedReader br = new BufferedReader(fr);
				String readOneLine;
				int i = 0;
				while ((readOneLine = br.readLine()) != null) {
					String[] items = readOneLine.split(" ");
					for (int j = 0; j < items.length; j++) {
						IndoorSpace.gD2DMatrix[i][j] = Double.valueOf(items[j]);
						// System.out.println(IndoorSpace.gD2DMatrix[i][j]);
					}
					i++;
				}
				br.close();
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void loadD2DIndex() {
		String d2dDir = outputPath + "/D2DIndex.txt";
		File file = new File(d2dDir);
		if (!file.exists()) {
			FileWriter fw;
			try {
				fw = new FileWriter(d2dDir);

				// --
				// for each row in d2dmatrix, sort and put in d2dindex
				for (int i = 0; i < IndoorSpace.gD2DMatrix.length; i++) {
					int size = IndoorSpace.gD2DMatrix[i].length;
					int[] indices = new int[size];
					for (int j = 0; j < indices.length; j++) {
						indices[j] = j;
					}
					double[] values = Arrays.copyOf(IndoorSpace.gD2DMatrix[i], size);
					bubbleSort(values, indices);
					//indices is what we want
					IndoorSpace.gD2DIndex[i]  = indices;
				}
				// --
				for (int i = 0; i < IndoorSpace.gD2DIndex.length; i++) {
					String templine = "";
					for (int j = 0; j < IndoorSpace.gD2DIndex.length; j++) {
						templine = templine + IndoorSpace.gD2DIndex[i][j] + " ";
					}
					fw.write(templine + "\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			try {
				FileReader fr = new FileReader(d2dDir);
				BufferedReader br = new BufferedReader(fr);
				String readOneLine;
				int i = 0;
				while ((readOneLine = br.readLine()) != null) {
					String[] items = readOneLine.split(" ");
					for (int j = 0; j < items.length; j++) {
						IndoorSpace.gD2DIndex[i][j] = Integer.valueOf(items[j]);
					}
					i++;
				}
				br.close();
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	/*
	 * (in-place) sort the values in @values indices are the original position of
	 * the values in @values
	 * 
	 * from small to large
	 */
	private static void bubbleSort(double[] values, int[] indices) {
		for (int k = 0; k < values.length; k++) {
			for (int l = k + 1; l < values.length; l++) {
				if (values[k] > values[l]) {
					double temp_value = values[k];
					values[k] = values[l];
					values[l] = temp_value;
					int temp_index = indices[k];
					indices[k] = indices[l];
					indices[l] = temp_index;
				}
			}
		}
	}

	/**
	 * load P2P Matrix modified from loadD2DMatrix
	 */
	public void loadP2PMatrix() {
		String p2pDir = outputPath + "/P2PMatrix.txt";
		File file = new File(p2pDir);
		if (!file.exists()) {
			FileWriter fw;
			try {
				fw = new FileWriter(p2pDir);
				for (int i = 0; i < IndoorSpace.gP2PMatrix.length; i++) {
					IndoorSpace.gP2PMatrix[i][i] = -1;
					List<Integer> doors1 = IndoorSpace.gPartitions.get(i).getmDoors();
					for (int j = 0; j < IndoorSpace.gP2PMatrix.length; j++) {

						if (i == j)
							continue;

						/// door dominate checking here.
						List<Integer> doors2 = IndoorSpace.gPartitions.get(j).getmDoors();

						// find potential minD1 by first door in d2
						double minDist = Constant.DISTMAX;
						int minD1 = -1;
						for (Integer d1 : doors1) {
							if (IndoorSpace.gD2DMatrix[d1][doors2.get(0)] < minDist) {
								minDist = IndoorSpace.gD2DMatrix[d1][doors2.get(0)];
								minD1 = d1;
							}
						}
						// if it to all d2 is =min. +d1di, d1 dominates other doors
						int dominate = 1;
						for (Integer d1 : doors1) {
							if (d1 == minD1)
								continue;
							for (Integer d2 : doors2) {
								if (IndoorSpace.gD2DMatrix[d1][d2] < IndoorSpace.gD2DMatrix[minD1][d2]
										+ IndoorSpace.gD2DMatrix[minD1][d1]) {
									dominate = 0;
									break;
								}
							}
						}
						// write it to p2pmartix
						if (dominate == 1) {
							IndoorSpace.gP2PMatrix[i][j] = minD1;
						} else
							IndoorSpace.gP2PMatrix[i][j] = -1;
					}
				}
				for (int i = 0; i < IndoorSpace.gP2PMatrix.length; i++) {
					String templine = "";
					for (int j = 0; j < IndoorSpace.gP2PMatrix.length; j++) {
						templine = templine + IndoorSpace.gP2PMatrix[i][j] + " ";
					}
					fw.write(templine + "\n");
				}
				fw.flush();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {

			try {
				FileReader fr = new FileReader(p2pDir);
				BufferedReader br = new BufferedReader(fr);
				String readOneLine;
				int i = 0;
				while ((readOneLine = br.readLine()) != null) {
					String[] items = readOneLine.split(" ");
					for (int j = 0; j < items.length; j++) {
						IndoorSpace.gP2PMatrix[i][j] = Integer.valueOf(items[j]);
//						 System.out.println(IndoorSpace.gP2PMatrix[i][j]);
					}
					i++;
				}
				br.close();
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * load P2P Matrix modified from loadD2DMatrix
	 */
	public void loadP2PMatrix2() {
		for (int i = 0; i < IndoorSpace.gP2PMatrix2.length; i++) {
			List<Integer> doors1 = IndoorSpace.gPartitions.get(i).getmDoors();
			for (int j = 0; j < IndoorSpace.gP2PMatrix2.length; j++) {
				int k = 0;
				if (i == j) {
					while (k < IndoorSpace.gP2PMatrix2[i][j].length) {
						IndoorSpace.gP2PMatrix2[i][j][k++] = -1;
					}
					continue;
				}

				/// door dominate checking here.
				List<Integer> doors2 = IndoorSpace.gPartitions.get(j).getmDoors();

				// for each door in doors1
				for (Integer door : doors1) {
					// if it to all d2 is =min. +d1di, d1 dominates other doors
					int NotBeingDominatedDoor = 1;
					// check if exist any d1 that dominate door
					for (Integer d1 : doors1) {
						if (d1 == door)
							continue;
						// d1 dominate door if all distances are shorter
						int longerFound = 0;
						for (Integer d2 : doors2) {
							if (IndoorSpace.gD2DMatrix[d1][d2] > IndoorSpace.gD2DMatrix[door][d2]
									+ IndoorSpace.gD2DMatrix[door][d1]) {
								longerFound = 1;
								break;
							}
						}
						if (longerFound == 1) {
							NotBeingDominatedDoor = 0;
							break;
						}
					}
					// write it to p2pmartix
					if (NotBeingDominatedDoor == 1) {
						IndoorSpace.gP2PMatrix2[i][j][k++] = door;
					} else {
						IndoorSpace.gP2PMatrix2[i][j][k++] = -1;
					}
				}
				while (k < IndoorSpace.gP2PMatrix2[i][j].length) {
					IndoorSpace.gP2PMatrix2[i][j][k++] = -1;
				}
			} // end j
		} // end i
//		for (int i = 0; i < IndoorSpace.gP2PMatrix2.length; i++) {
//			if (i == 139) {
//				for (int j = 0; j < IndoorSpace.gP2PMatrix2.length; j++) {
//					String templine = "";
//					for (int k = 0; k < IndoorSpace.gP2PMatrix2[i][j].length; k++) {
//						templine = templine + IndoorSpace.gP2PMatrix2[i][j][k] + " ";
//					}
//					System.out.println("i:" + i + " j:" + j + " " + templine);
//				}
//			}
//		}
	}

	/**
	 * generates the indoor space
	 * 
	 */
	public void genIndoorSpace() {

		// to get quadrant part of one floor
		initPartitions();

		int curSize = IndoorSpace.gPartitions.size();// curSize=32

		// to duplicate in the floor
//		System.out.println("initial partition size:"+curSize);
		for (int i = 0; i < curSize; i++) {
			Par curPar = IndoorSpace.gPartitions.get(i);

			// left-bottom part
			Par newPar = new Par(curPar);
			newPar.reflection(0, 30);
			IndoorSpace.gPartitions.add(newPar);

			// right-top part
			newPar = new Par(curPar);
			newPar.reflection(1, 30);
			IndoorSpace.gPartitions.add(newPar);

			// right-bottom part
			newPar = new Par(curPar);
			newPar.reflection(1, 30);
			newPar.reflection(0, 30);
			IndoorSpace.gPartitions.add(newPar);
			// System.out.println(curPar.toString());
			// System.out.println(newPar.toString());

		}

		// to generate the staircases and hallways
		genStaircase();
		genHallway();
		// System.out.println(indoorSpace.gPartitions.size());

		// add all the generated partitions into a global variant - gPartitions
		curSize = IndoorSpace.gPartitions.size();
		for (int i = 0; i < curSize; i++) {
			IndoorSpace.gPartitions.get(i).setmID(i);
			// System.out.println(IndoorSpace.gPartitions.get(i).toString());
			List<Integer> mDoor = new ArrayList<>();
			IndoorSpace.gPartitions.get(i).setmDoors(mDoor);
		}

		/*
		 * 
		 * Generating all the partitions finished! Begin to generate the doors and link
		 * doors and partitions together
		 */

		// to generate the entrance door for each partitions
		initDoors();

		int curDoorsSize = IndoorSpace.gDoors.size();
		// System.out.println(curDoorsSize);
		for (int i = 0; i < curDoorsSize; i++) {
			// System.out.println(indoorSpace.gDoors.get(i).toString());
			Door curDoor = IndoorSpace.gDoors.get(i);
			Door newDoor = new Door(curDoor);
			newDoor.rotate(new Point(10.0, 10.0), 1.5 * Constant.PI);
			// System.out.println(newDoor.toString());
			IndoorSpace.gDoors.add(newDoor);
		}
		// System.out.println(indoorSpace.gDoors.size());

		Door cornerDoor = new Door(10, 10); // left top corner door

		IndoorSpace.gDoors.add(cornerDoor);

		// Door cornerDoor1 = new Door(7.5, 10);
		// Door cornerDoor2 = new Door(10, 7.5);

		// IndoorSpace.gDoors.add(cornerDoor1);
		// IndoorSpace.gDoors.add(cornerDoor2);

		// to duplicate the doors by rotations
		curDoorsSize = IndoorSpace.gDoors.size();
		for (int i = 0; i < curDoorsSize; i++) {
			Door curDoor = IndoorSpace.gDoors.get(i);
			Door newDoor = new Door(curDoor);
			newDoor.reflection(1, 30);
			IndoorSpace.gDoors.add(newDoor);
		}

		curDoorsSize = IndoorSpace.gDoors.size();
		for (int i = 0; i < curDoorsSize; i++) {
			Door curDoor = IndoorSpace.gDoors.get(i);
			Door newDoor = new Door(curDoor);
			newDoor.reflection(0, 30);
			IndoorSpace.gDoors.add(newDoor);
		}

		for (int i = 0; i < IndoorSpace.gPartitions.size(); i++) {
			Par par = IndoorSpace.gPartitions.get(i);
			if (par.getmID() == 128 || par.getmID() == 133 || par.getmID() == 131 || par.getmID() == 138) {
				Door newDoor1 = new Door(par.getX1(), (par.getY1() + par.getY2()) / 2);
				Door newDoor2 = new Door(par.getX2(), (par.getY1() + par.getY2()) / 2);
				IndoorSpace.gDoors.add(newDoor1);
				IndoorSpace.gDoors.add(newDoor2);
			}
			if (par.getmID() == 129 || par.getmID() == 135 || par.getmID() == 136 || par.getmID() == 130) {
				Door newDoor1 = new Door((par.getX1() + par.getX2()) / 2, par.getY1());
				Door newDoor2 = new Door((par.getX1() + par.getX2()) / 2, par.getY2());
				IndoorSpace.gDoors.add(newDoor1);
				IndoorSpace.gDoors.add(newDoor2);
			}
		}

		for (int i = 0; i < IndoorSpace.gDoors.size(); i++) {
			IndoorSpace.gDoors.get(i).setmID(i);
			List<Integer> mPartitions = new ArrayList<>();
			IndoorSpace.gDoors.get(i).setmPartitions(mPartitions);
			// System.out.println(indoorSpace.gDoors.get(i).toString());
		}

		/// scale up/down the whole indoor space
		for (int i = 0; i < IndoorSpace.gPartitions.size(); i++) {
			Par curPar = IndoorSpace.gPartitions.get(i);
			curPar.scale(DataGenConstant.scale);
		}
		for (int i = 0; i < IndoorSpace.gDoors.size(); i++) {
			Door door = IndoorSpace.gDoors.get(i);
			door.scale(DataGenConstant.scale);
		}

		// to link the doors and partitions together
		linkGraph();

		for (int i = 0; i < IndoorSpace.gPartitions.size(); i++) {

			// D2D distance Matrix
			HashMap<String, D2Ddistance> hashMap = IndoorSpace.gPartitions.get(i).getD2dHashMap();
			IndoorSpace.gD2D.putAll(hashMap);

			// add all the partitions into the Graph
			AccsGraph.Partitions.put(i, IndoorSpace.gPartitions.get(i));
		}

		/*
		 * for(D2Ddistance item : IndoorSpace.gD2D.values()){
		 * System.out.println(item.toString()); }
		 * System.out.println(IndoorSpace.gD2D.get(Functions.keyConventer(5, 8)));
		 */

		// add all the doors as nodes into the Graph
		for (int i = 0; i < IndoorSpace.gDoors.size(); i++) {
			AccsGraph.Nodes.put(i, IndoorSpace.gDoors.get(i));
		}

	}

	/**
	 * initializes the R-Tree for indexing all the indoor partitions
	 * 
	 */
	public void initRTree() {
		IndoorSpace.gRTree.load(IndoorSpace.gPartitions);
		System.out.println("Init Rtree >> Number of Leaves: " + IndoorSpace.gRTree.getNumberOfLeaves());
	}

	/**
	 * duplicates a number of floors
	 * 
	 * @param floorNumber the number of floors in the whole building
	 */
	public void duplicateIndoorSpace(int floorNumber) {
		// TODO Auto-generated method stub

		int curSizePar = IndoorSpace.gPartitions.size();
		int curSizeDoor = IndoorSpace.gDoors.size();
		IndoorSpace.gNumberDoorsPerFloor = curSizeDoor;
		IndoorSpace.gNumberParsPerFloor = curSizePar;

//		System.out.println("one floor doors:" + curSizeDoor);
//		System.out.println("one floor partitions:" + curSizePar);

		for (int floorIndex = 1; floorIndex < floorNumber; floorIndex++) {

			for (int parIndex = 0; parIndex < curSizePar; parIndex++) {
				Par curPar = IndoorSpace.gPartitions.get(parIndex);
				Par newPar = new Par(curPar);
				newPar.setmFloor(floorIndex);
				newPar.setmID(parIndex + floorIndex * curSizePar);
				// System.out.println("-------------");
				// System.out.println(parIndex);
				// System.out.println(parIndex + floorIndex * curSizePar);
				List<Integer> mDoors = new ArrayList<Integer>();
				for (int mDoor : curPar.getmDoors()) {
					mDoors.add(mDoor + floorIndex * curSizeDoor);
				}
				newPar.setmDoors(mDoors);
				IndoorSpace.gPartitions.add(newPar);
				AccsGraph.Partitions.put(newPar.getmID(), newPar);
			}

			for (int doorIndex = 0; doorIndex < curSizeDoor; doorIndex++) {
				Door curDoor = IndoorSpace.gDoors.get(doorIndex);
				Door newDoor = new Door(curDoor);
				newDoor.setmFloor(floorIndex);
				newDoor.setmID(doorIndex + floorIndex * curSizeDoor);
				List<Integer> mPars = new ArrayList<Integer>();
				for (int mPar : curDoor.getmPartitions()) {
					mPars.add(mPar + floorIndex * curSizePar);
				}
				newDoor.setmPartitions(mPars);
				IndoorSpace.gDoors.add(newDoor);
				AccsGraph.Nodes.put(newDoor.getmID(), newDoor);
			}
		}

//
//		System.out.println("all floors doors:"+IndoorSpace.gDoors.size());
//		System.out.println("all floors partitions:"+IndoorSpace.gPartitions.size());

//		System.out.println(IndoorSpace.gPartitions.get(127).getmType());
//		System.out.println(IndoorSpace.gPartitions.get(128).getmType());
//		System.out.println(IndoorSpace.gPartitions.get(129));
//		System.out.println(IndoorSpace.gPartitions.get(130));
//		System.out.println(IndoorSpace.gPartitions.get(131));

//		System.out.println(IndoorSpace.gPartitions.get(128).getmDoors());
//
//		System.out.println(IndoorSpace.gPartitions.get(269).getmDoors());
//		System.out.println(IndoorSpace.gPartitions.get(270));
//		System.out.println(IndoorSpace.gPartitions.get(271));
//		System.out.println(IndoorSpace.gPartitions.get(272).getmType());

	}

	/**
	 * saves the generated doors and partitions
	 * 
	 * @return boolean value if writing files is accomplished successfully.
	 * @exception IOException
	 */
	public boolean saveDP() {
		try {
			FileWriter fwPar = new FileWriter(outputPath + "/Par.txt");
			Iterator<Par> itrPar = IndoorSpace.gPartitions.iterator();
			while (itrPar.hasNext()) {
				fwPar.write(itrPar.next().toString() + "\n");
			}
			fwPar.flush();
			fwPar.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			FileWriter fwDoor = new FileWriter(outputPath + "/Door.txt");
			Iterator<Door> itrDoor = IndoorSpace.gDoors.iterator();
			while (itrDoor.hasNext()) {
				fwDoor.write(itrDoor.next().toString() + "\n");
			}
			fwDoor.flush();
			fwDoor.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			FileWriter fwP2D = new FileWriter(outputPath + "/P2D.txt");
			Iterator<Par> itrPar = IndoorSpace.gPartitions.iterator();
			while (itrPar.hasNext()) {
				fwP2D.write(itrPar.next().toString() + "\n");
			}
			fwP2D.flush();
			fwP2D.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			FileWriter fwD2P = new FileWriter(outputPath + "/D2P.txt");
			Iterator<Door> itrDoor = IndoorSpace.gDoors.iterator();
			while (itrDoor.hasNext()) {
				Door curDoor = itrDoor.next();
				fwD2P.write(curDoor.toString() + "\n");
				curDoor.genLeaveablePar();
			}
			fwD2P.flush();
			fwD2P.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * generates the indoor objects the initialization
	 * 
	 */
	public void genIdrObj() {

//		Random random = new Random();
		Random random = AlgCSDM.random;
		for (int floorIndex = 0; floorIndex < DataGenConstant.nFloor; floorIndex++) {
			int nCount = 0;
			while (nCount < DataGenConstant.nObjPerFloor) {
				double x = random.nextDouble() * DataGenConstant.floorRangeX;
				double y = random.nextDouble() * DataGenConstant.floorRangeY;
				System.out.println(x);
				System.out.println(y);
				Point randomPoint = new Point(x, y, floorIndex);
				// to ensure that the objects are not in the outside
				if (Functions.isInBuilding(randomPoint)) {
					IdrObj randomIdrObj = new IdrObj(randomPoint);
					randomIdrObj.setmID(nCount + DataGenConstant.nObjPerFloor * floorIndex);
//					 System.out.println(randomIdrObj.toString());
					IndoorSpace.gIdrObjs.add(randomIdrObj);
					// randomIdrObj.getUncertainSampledProb();
					nCount++;
				}
			}
		}
	}

	/**
	 * new an object randomly
	 * 
	 * @return newObj a new indoor object
	 * @param entranceID the entrance that the object entered
	 * @param cursorObj  the serial number of the new object
	 */
	private IdrObj randomNewObj(int entranceID, int cursorObj) {
		// TODO Auto-generated method stub
		if (entranceID == 128) { // left entrance
			Point pos = new Point(IndoorSpace.gDoors.get(208).getX(), IndoorSpace.gDoors.get(208).getY(), 0);
			IdrObj newObj = new IdrObj(cursorObj, pos);
			newObj.walk();
			return newObj;
		}
		if (entranceID == 129) { // top entrance
			Point pos = new Point(IndoorSpace.gDoors.get(210).getX(), IndoorSpace.gDoors.get(210).getY(), 0);
			IdrObj newObj = new IdrObj(cursorObj, pos);
			newObj.walk();
			return newObj;
		}
		if (entranceID == 130) { // bottom entrance
			Point pos = new Point(IndoorSpace.gDoors.get(213).getX(), IndoorSpace.gDoors.get(213).getY(), 0);
			IdrObj newObj = new IdrObj(cursorObj, pos);
			newObj.walk();
			return newObj;
		}
		if (entranceID == 131) { // right entrance
			Point pos = new Point(IndoorSpace.gDoors.get(215).getX(), IndoorSpace.gDoors.get(215).getY(), 0);
			IdrObj newObj = new IdrObj(cursorObj, pos);
			newObj.walk();
			return newObj;
		}
		return null;
	}

	/**
	 * generates the new entered objects
	 * 
	 * @param time a given time interval.
	 */
	private void genNewEnteredIdrObj(int time) {
		// TODO Auto-generated method stub
		if (time > 0) {
			int cursorObj = IndoorSpace.gIdrObjs.size();
			List<IdrObj> newAddedObjs = new ArrayList<IdrObj>();

			// System.out.println(cursorObj);
//			int entranceID = 128 + ((new Random().nextInt(4)));
			int entranceID = 128 + ((AlgCSDM.random.nextInt(4)));
			// System.out.println(entranceID);
			int enteredCount = (int) Functions.P_rand(DataGenConstant.newEnterObjects);
			for (int i = 0; i < enteredCount; i++) {
				IdrObj newObj = randomNewObj(entranceID, cursorObj);
				System.out.println("New Added:" + newObj.toString());
				cursorObj++;
				newAddedObjs.add(newObj);
			}

			IndoorSpace.gIdrObjs.addAll(newAddedObjs);
		}
	}

	/**
	 * updates the trajectories for a given time interval
	 * 
	 * @param time the given time interval
	 * @exception IOException
	 */
	private void updateTrajectories(int time) {
		// TODO Auto-generated method stub
		if (time == 0) {
			try {
//				FileWriter fwTrajectories = new FileWriter(outputPath
//						+ "/Trajectories_" + DataGenConstant.nObjPerFloor + ".txt");
				FileWriter fwTrajectories = new FileWriter(outputPath + "/" + AlgCSDM.trajectory);

				for (IdrObj item : IndoorSpace.gIdrObjs) {
					fwTrajectories.write(time + "\t" + item.toString() + "\n");
				}
				fwTrajectories.flush();
				fwTrajectories.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				FileWriter fwTrajectories = new FileWriter(outputPath + "/" + AlgCSDM.trajectory, true);
				for (IdrObj item : IndoorSpace.gIdrObjs) {
					fwTrajectories.write(time + "\t" + item.toString() + "\n");
				}
				fwTrajectories.flush();
				fwTrajectories.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * generates the trajectories of all the indoor objects
	 * 
	 */
	private void genTrajectories() {
		// TODO Auto-generated method stub

		for (int time = 0; time < DataGenConstant.totalLifecycle; time++) {
			System.out.println("Current Time is " + time);
			// genNewEnteredIdrObj(time);
//			if(new Random().nextInt(100) < 25){
			if (AlgCSDM.random.nextInt(100) < 25) {
				genNewEnteredIdrObj(time);
			}
			System.out.println("time, IndoorSpace.gIdrObjs.size():" + time + " " + IndoorSpace.gIdrObjs.size());
			IndoorSpace.sizeObjsTable.put(time, IndoorSpace.gIdrObjs.size());
			for (IdrObj curIdrObj : IndoorSpace.gIdrObjs) {
				curIdrObj.walk();
			}
			updateTrajectories(time);
		}
	}

	/**
	 * draws one single floor
	 * 
	 */
	public void drawFloor() {
		PaintingPanel pp = new PaintingPanel();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(pp);
		frame.setBounds(0, 0, (int) (DataGenConstant.floorRangeX + 3) * DataGenConstant.zoomLevel,
				(int) (DataGenConstant.floorRangeY + 3) * DataGenConstant.zoomLevel);
		frame.setVisible(true);
	}

	/**
	 * saves the SizeObjsTable which records the numbers of indoor objects in each
	 * time interval
	 * 
	 * @exception IOException
	 */
	private void saveSizeObjsTable() {
		// TODO Auto-generated method stub
		try {
			FileWriter fwSOT = new FileWriter(outputPath + "/" + AlgCSDM.sizeTable);
			for (Entry<Integer, Integer> item : IndoorSpace.sizeObjsTable.entrySet()) {
				fwSOT.write(item.getKey() + " " + item.getValue() + "\n");
			}
			fwSOT.flush();
			fwSOT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * generates all the data
	 * 
	 */
	public void genAllData() {
		// TODO Auto-generated method stub

		genIndoorSpace();

		initRTree();

		duplicateIndoorSpace(DataGenConstant.nFloor);

		if (true == saveDP()) {
			System.out.println("Partitions & Doors Generating Finished!");
		}

		genIdrObj();

		long startTime = System.nanoTime();
		genTrajectories();
		long endTime = System.nanoTime();
		System.out.println("Running Time: " + (endTime - startTime) + "ns");

		drawFloor();

		saveSizeObjsTable();
		System.out.println("Generate All Data Done!");

	}

	class PaintingPanel extends JPanel {

		private static final long serialVersionUID = 6050646791177968917L;

		/**
		 * paint function, paint the indoor space with a zoom-level parameter 1.paint
		 * the partitions(black-line & yellow BG color) 2.paint the doors(red) 3.paint
		 * the indoor objects(blue)
		 * 
		 * @return @param @exception
		 */
		public void paint(Graphics g) {
			super.paint(g);
			paintPartitions(g);
			paintDoors(g);
			// paintIdrObjs(g);
		}

		/**
		 * paint indoor objects
		 * 
		 * @return @param g Graphics @exception
		 */
		private void paintIdrObjs(Graphics g) {
			// TODO Auto-generated method stub
			g.setColor(Color.BLUE);
			Iterator<IdrObj> itr3 = IndoorSpace.gIdrObjs.iterator();
			while (itr3.hasNext()) {
				IdrObj curIdrObj = itr3.next();
				g.fillOval((int) (curIdrObj.getmTruePos().getX() * DataGenConstant.zoomLevel),
						(int) (curIdrObj.getmTruePos().getY() * DataGenConstant.zoomLevel), 5, 5);
			}
		}

		/**
		 * paint doors
		 * 
		 * @return @param g Graphics @exception
		 */
		private void paintDoors(Graphics g) {
			// TODO Auto-generated method stub
			g.setColor(Color.RED);
			for (int i = 0; i < IndoorSpace.gNumberDoorsPerFloor; i++) {
				Door curDoor = IndoorSpace.gDoors.get(i);
				g.fillOval((int) (curDoor.getX() * DataGenConstant.zoomLevel),
						(int) (curDoor.getY() * DataGenConstant.zoomLevel), 5, 5);
				g.drawString(String.valueOf(curDoor.getmID()), (int) (curDoor.getX() * DataGenConstant.zoomLevel),
						(int) (curDoor.getY() * DataGenConstant.zoomLevel));
			}
		}

		/**
		 * paint partitions
		 * 
		 * @return @param g Graphics @exception
		 */
		private void paintPartitions(Graphics g) {
			// TODO Auto-generated method stub

			// four outdoor rectangles (is used in the is inBuilding function)
			g.setColor(Color.WHITE);
			g.fillRect(((int) DataGenConstant.outRectTop.getX1()) * DataGenConstant.zoomLevel,
					((int) DataGenConstant.outRectTop.getY1()) * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectTop.getWidth() * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectTop.getHeight() * DataGenConstant.zoomLevel);
			g.fillRect(((int) DataGenConstant.outRectBottom.getX1()) * DataGenConstant.zoomLevel,
					((int) DataGenConstant.outRectBottom.getY1()) * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectBottom.getWidth() * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectBottom.getHeight() * DataGenConstant.zoomLevel);
			g.fillRect(((int) DataGenConstant.outRectLeft.getX1()) * DataGenConstant.zoomLevel,
					((int) DataGenConstant.outRectLeft.getY1()) * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectLeft.getWidth() * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectLeft.getHeight() * DataGenConstant.zoomLevel);
			g.fillRect(((int) DataGenConstant.outRectRight.getX1()) * DataGenConstant.zoomLevel,
					((int) DataGenConstant.outRectRight.getY1()) * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectRight.getWidth() * DataGenConstant.zoomLevel,
					(int) DataGenConstant.outRectRight.getHeight() * DataGenConstant.zoomLevel);

			// all indoor partitions
			g.setColor(Color.BLACK);
			for (int i = 0; i < IndoorSpace.gNumberParsPerFloor; i++) {
				Par curPartition = IndoorSpace.gPartitions.get(i);
				if (curPartition.getmType() == RoomType.ROOM) {
					g.setColor(Color.YELLOW);
				}
				if (curPartition.getmType() == RoomType.HALLWAY) {
					g.setColor(Color.LIGHT_GRAY);
				}
				if (curPartition.getmType() == RoomType.STAIRCASE) {
					g.setColor(Color.GRAY);
				}
				g.fillRect((int) (curPartition.getX1() * DataGenConstant.zoomLevel),
						(int) (curPartition.getY1() * DataGenConstant.zoomLevel),
						(int) (curPartition.getX2() - curPartition.getX1()) * DataGenConstant.zoomLevel,
						(int) (curPartition.getY2() - curPartition.getY1()) * DataGenConstant.zoomLevel);
				g.setColor(Color.BLACK);
				g.drawRect((int) (curPartition.getX1() * DataGenConstant.zoomLevel),
						(int) (curPartition.getY1() * DataGenConstant.zoomLevel),
						(int) (curPartition.getX2() - curPartition.getX1()) * DataGenConstant.zoomLevel,
						(int) (curPartition.getY2() - curPartition.getY1()) * DataGenConstant.zoomLevel);
				g.drawString(String.valueOf(curPartition.getmID()),
						(int) (curPartition.getX1() + curPartition.getX2()) / 2 * DataGenConstant.zoomLevel,
						(int) (curPartition.getY1() + curPartition.getY2()) / 2 * DataGenConstant.zoomLevel);
			}
		}
	}

	/**
	 * test main function
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		System.out.println("args.length: " + args.length);
		if (args.length < 4) {
			System.out.println("usage: [totalLifecycle] [numOfObjPerFloor] [numOfFloors] [numOfObjs] ");
			System.out.println("e.g., java -jar dataGen.jar 60 500 20 10000");
			return;
		} else {
			int c = 0;

			DataGenConstant.totalLifecycle = Integer.parseInt(args[c++]);
			DataGenConstant.nObjPerFloor = Integer.parseInt(args[c++]);
			DataGenConstant.nFloor = Integer.parseInt(args[c++]);
			DataGenConstant.nObjects = Integer.parseInt(args[c++]);

			if (DataGenConstant.nObjects != DataGenConstant.nFloor * DataGenConstant.nObjPerFloor) {
				System.out.println("nObjects!= nFloor * nObjPerFloor");
				System.exit(-1);
			}
		}
		DataGen dataGen = new DataGen();
//		dataGen.saveDP();
		dataGen.genAllData();

	}
}