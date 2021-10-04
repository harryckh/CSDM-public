package dk.aau.cs.idq.datagen;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import algorithm.AlgCSDM;
import dk.aau.cs.idq.indoorentities.Door;
import dk.aau.cs.idq.indoorentities.IdrObj;
import dk.aau.cs.idq.indoorentities.IndoorSpace;
import dk.aau.cs.idq.indoorentities.Point;
import dk.aau.cs.idq.indoorentities.SampledPoint;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.OTTConstant;

/**
 * Generate the OTT for a given current time
 *
 * @author lihuan
 * @version 0.1 / 2014.09.06
 */
public class OTTGen {

	public String idrObjDir = System.getProperty("user.dir") + "/IdrObj";

	public List<List<SampledPoint>> samplesPointsInFloors = new ArrayList<>();

	FileReader frT;
	BufferedReader brT;

	public OTTGen() {

		for (int i = 0; i < DataGenConstant.nFloor; i++) {
			List<SampledPoint> samplesPointsInOneFloor = new ArrayList<>();
			samplesPointsInFloors.add(samplesPointsInOneFloor);
		}

		FileReader frT;
		try {
			frT = new FileReader(DataGen.outputPath + "/" + AlgCSDM.trajectory);
			brT = new BufferedReader(frT);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * generate the OTT for a given current time
	 *
	 * @param curTime the current time
	 */
	public void genOTTbyTime(int curTime, int flag) {
		// System.out.println(curTime);

		idrObjDir = idrObjDir + "_" + curTime + ".txt";
		File dir = new File(idrObjDir);
		if (dir.exists()) {
//			System.out.println("I am present in  place A");

			try {

				FileReader frTimeTable = new FileReader(DataGen.outputPath + "/timetable_" + curTime + ".txt");
				BufferedReader brTimeTable = new BufferedReader(frTimeTable);
				String readOneLine;
				while ((readOneLine = brTimeTable.readLine()) != null) {
					String[] items = readOneLine.split("\t");
					IndoorSpace.OTT.put(Integer.valueOf(items[0]), Integer.valueOf(items[1]));
				}
				frTimeTable.close();
				brTimeTable.close();

				FileReader frIdrObj = new FileReader(idrObjDir);
				BufferedReader brIdrObj = new BufferedReader(frIdrObj);
				while ((readOneLine = brIdrObj.readLine()) != null) {
					IdrObj newObj = IdrObj.parse(readOneLine);
//					IndoorSpace.observedObjs.put(newObj.getmID(), newObj);
					IndoorSpace.observedObjsList.get(newObj.getmTruePos().getmFloor()).add(newObj);

					if (flag == 1) {
						newObj.getFloorPointSampledProb();
					}
				}
				frIdrObj.close();
				brIdrObj.close();

				if (flag == 0) {
					FileReader frSampledPoint = new FileReader(
							DataGen.outputPath + "/sampledpoint_" + curTime + ".txt");
					BufferedReader brSampledPoint = new BufferedReader(frSampledPoint);
					while ((readOneLine = brSampledPoint.readLine()) != null) {
						SampledPoint sp = SampledPoint.parse(readOneLine);
						IndoorSpace.gSampledPoints.add(sp);
						samplesPointsInFloors.get(sp.getmFloor()).add(sp);
					}
					frSampledPoint.close();
					brSampledPoint.close();
				} else {
					FileWriter fwSampledPoint = new FileWriter(
							DataGen.outputPath + "/sampledpoint_" + curTime + ".txt");

					for (SampledPoint sp : IndoorSpace.gSampledPoints) {
						fwSampledPoint.write(sp.toString() + "\n");
						samplesPointsInFloors.get(sp.getmFloor()).add(sp);

					}
					fwSampledPoint.flush();
					fwSampledPoint.close();
				}

			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
//			System.out.println("I am present in  place B");
			int curSizeObjs = IndoorSpace.sizeObjsTable.get(curTime);
//			Random random = new Random();
			Random random = AlgCSDM.randomUpdateSeed;
			List<String> findStringLists = new ArrayList<>();
			for (int index = 0; index < curSizeObjs; index++) {
				int pickTime = curTime - random.nextInt(OTTConstant.minSamplingPeriod);
				if (pickTime >= 0) {
					findStringLists.add(new String(pickTime + "#" + index));
				}
			}

			getRecord(curTime, findStringLists);
		}

//		for (int i = 0; i < DataGenConstant.nFloor; i++) {
//			PRTree<SampledPoint> prt = new PRTree<>(new SampledPointConverter(), DataGenConstant.RTree_BranchFactor);
//			prt.load(samplesPointsInFloors.get(i));
//			// System.out.println(i +"-th floor has objects :" + prt.getNumberOfLeaves());
//			IndoorSpace.gSPRTree.add(prt);
//		}
	}

	/**
	 * generate the OTT for a given current time
	 *
	 * @param curTime the current time
	 * 
	 * @return List of new object id
	 */
	public List<List<IdrObj>> genOTTbyTime_new(int curTime, int flag) {
		// System.out.println(curTime);
		List<List<IdrObj>> newObjsList = new ArrayList<List<IdrObj>>();
		int curSizeObjs = IndoorSpace.sizeObjsTable.get(curTime);

		List<String> findStringLists = new ArrayList<>();
		for (int index = 0; index < curSizeObjs; index++) {

			/// --- skipping obj in OTT that are just updated
			if (IndoorSpace.OTT.get(index) != null) {
				int recordTime = IndoorSpace.OTT.get(index);
				if (recordTime >= curTime - OTTConstant.minSamplingPeriod)
					continue;
			}
			/// ----
			/// model the random update from the object
			if (AlgCSDM.randomUpdateSeed.nextDouble() < OTTConstant.updateProb) {
				findStringLists.add(new String(curTime + "#" + index));
			}
		}
		newObjsList = getRecord_new(curTime, findStringLists);

		return newObjsList;
	}

	/**
	 * read and retrieve useful records for a given current time and a given
	 * findStringLists
	 *
	 * @param curTime         the current time
	 * @param findStringLists which time for which object
	 */
	private void getRecord(int curTime, List<String> findStringLists) {
		// TODO Auto-generated method stub
		try {
			FileReader frT = new FileReader(DataGen.outputPath + "/" + AlgCSDM.trajectory);
			BufferedReader brT = new BufferedReader(frT);
			FileWriter fwOTT = new FileWriter(DataGen.outputPath + "/OTT_" + curTime + ".txt");
			FileWriter fwIdrObj = new FileWriter(idrObjDir);
			FileWriter fwTimeRecord = new FileWriter(DataGen.outputPath + "/timetable_" + curTime + ".txt");
			FileWriter fwSampledPoint = new FileWriter(DataGen.outputPath + "/sampledpoint_" + curTime + ".txt");
			String readOneLine;
			while ((readOneLine = brT.readLine()) != null) {
				String[] items = readOneLine.split("\t");
				if (Integer.valueOf(items[0]) + OTTConstant.minSamplingPeriod > curTime) {
					if (findStringLists.contains(items[0] + "#" + items[1])) {
						// fwOTT.write(items[1] + "\t" + items[5] + "\t" +
						// items[0] + "\n");
//						List<SampledPoint> gSampledPoints = IndoorSpace.gSampledPoints;
//						System.out.println("Sample points test "+gSampledPoints);
						if ((new String("#")).equals(items[2])) {
							int recordTime = Integer.valueOf(items[0]);
							int mID = Integer.valueOf(items[1]);
							double x = Double.valueOf(items[3]);
							double y = Double.valueOf(items[4]);
							int mFloor = Integer.valueOf(items[5]);
							int curParID = Integer.valueOf(items[6]);
							IdrObj newObj = new IdrObj(mID, new Point(x, y, mFloor),
									IndoorSpace.gPartitions.get(curParID));
							IndoorSpace.observedObjsList.get(mFloor).add(newObj);
							IndoorSpace.OTT.put(mID, recordTime);
							fwOTT.write(items[1] + "\t" + items[0] + "\t" + newObj.getCurrentUncertainRecord() + "\n");
							fwIdrObj.write(newObj.toString() + "\n");
						}
					}

					if (Integer.valueOf(items[0]) > curTime)
						break;
				}

			}
			fwOTT.flush();
			fwOTT.close();
			fwIdrObj.flush();
			fwIdrObj.close();

			for (Entry<Integer, Integer> entry : IndoorSpace.OTT.entrySet()) {
				fwTimeRecord.write(entry.getKey() + "\t" + entry.getValue() + "\n");
			}
			fwTimeRecord.flush();
			fwTimeRecord.close();

			for (SampledPoint sp : IndoorSpace.gSampledPoints) {

				fwSampledPoint.write(sp.toString() + "\n");
				samplesPointsInFloors.get(sp.getmFloor()).add(sp);

			}
			fwSampledPoint.flush();
			fwSampledPoint.close();

			brT.close();
			frT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * read and retrieve useful records for a given current time and a given
	 * findStringLists
	 *
	 * @param curTime         the current time
	 * @param findStringLists which time for which object
	 */
	private List<List<IdrObj>> getRecord_new(int curTime, List<String> findStringLists) {
		// TODO Auto-generated method stub
		List<List<IdrObj>> newObjsList = new ArrayList<List<IdrObj>>();
		for (int i = 0; i < DataGenConstant.nFloor; i++)
			newObjsList.add(new ArrayList<IdrObj>());
		try {
//			FileReader frT = new FileReader(DataGen.outputPath + "/" + AlgCSDM.trajectory);
//			BufferedReader brT = new BufferedReader(frT);

//			FileWriter fwOTT = new FileWriter(DataGen.outputPath + "/OTT_" + curTime + ".txt");
//			FileWriter fwIdrObj = new FileWriter(idrObjDir);
//			FileWriter fwTimeRecord = new FileWriter(DataGen.outputPath + "/timetable_" + curTime + ".txt");
//			FileWriter fwSampledPoint = new FileWriter(DataGen.outputPath + "/sampledpoint_" + curTime + ".txt");
			String readOneLine;
			while ((readOneLine = brT.readLine()) != null) {
				String[] items = readOneLine.split("\t");
//				if (Integer.valueOf(items[0]) + OTTConstant.minSamplingPeriod > curTime) {
				if (Integer.valueOf(items[0]) == curTime) {

//						System.out.println(readOneLine);
					if (findStringLists.contains(items[0] + "#" + items[1])) {
						// fwOTT.write(items[1] + "\t" + items[5] + "\t" +
						// items[0] + "\n");
						if ((new String("#")).equals(items[2])) {

							int recordTime = Integer.valueOf(items[0]);
							int mID = Integer.valueOf(items[1]);
							double x = Double.valueOf(items[3]);
							double y = Double.valueOf(items[4]);
							int mFloor = Integer.valueOf(items[5]);
							int curParID = Integer.valueOf(items[6]);
							IdrObj newObj = new IdrObj(mID, new Point(x, y, mFloor),
									IndoorSpace.gPartitions.get(curParID));

							//----
							ArrayList<IdrObj> a = IndoorSpace.observedObjsList.get(mFloor);
							for (int j = 0; j < a.size(); j++) {
								if (a.get(j).getmID() == mID) {
//									System.out.println("Removing old one: " + a.get(j).getmID());
									if (a.get(j) != null)
										a.get(j).setUr(null);
									a.remove(j);
									break;
								}
							}
							// ---
							IndoorSpace.observedObjsList.get(mFloor).add(newObj);
							newObjsList.get(mFloor).add(newObj);
							// delay adding to after processing
//							IndoorSpace.OTT.put(mID, recordTime);

							// ------
							newObj.getCurrentUncertainRecord();
//							fwOTT.write(items[1] + "\t" + items[0] + "\t" + newObj.getCurrentUncertainRecord() + "\n");
//							fwIdrObj.write(newObj.toString() + "\n");
						}
					}
				}
				if (Integer.valueOf(items[0]) > curTime)
					break;

			}
//			brT.close();
//			frT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newObjsList;
	}

	/**
	 * load Objects Size Table
	 */
	private static void loadSizeObjsTable() {
		// TODO Auto-generated method stub
		try {
//			FileReader frSOT = new FileReader(DataGen.outputPath + "/sizeObjsTable.txt");
			FileReader frSOT = new FileReader(DataGen.outputPath + "/" + AlgCSDM.sizeTable);
			BufferedReader brSOT = new BufferedReader(frSOT);
			String readOneLine;
			while ((readOneLine = brSOT.readLine()) != null) {
				String[] items = readOneLine.split(" ");
				IndoorSpace.sizeObjsTable.put(Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			}
			brSOT.close();
			frSOT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * to generate the OTT
	 * 
	 * /// flag = 0: read sampledPoint_curTime.txt /// = 1: generate and write
	 * sampledPoint_curTime.txt
	 */
	@SuppressWarnings("resource")
	public int generateOTT(int cTime, int flag) {
		// TODO Auto-generated method stub

		loadSizeObjsTable();
		DataGen dataGen = new DataGen();
		dataGen.genIndoorSpace();
		dataGen.initRTree();
		dataGen.saveDP();
		dataGen.duplicateIndoorSpace(DataGenConstant.nFloor);
		for (Door curDoor : IndoorSpace.gDoors) {
			curDoor.genLeaveablePar();
		}
		int curTime;

		if (cTime == -1) {
			System.out.println("please input an integer number as current time ( range[ 0, "
					+ DataGenConstant.totalLifecycle + " ) )");
			curTime = new Scanner(System.in).nextInt();
		} else {
			curTime = cTime;
		}
//		System.out.println("to get the OTT for current time: " + curTime);

		// TODO Auto-generated method stub

		dataGen.loadD2DMatrix();
		dataGen.loadP2PMatrix();
		dataGen.loadP2PMatrix2();
		// --
		// check.
//		for(int i=0;i<IndoorSpace.gP2PMatrix.length;i++) {
//			for(int j=0;j<IndoorSpace.gP2PMatrix.length;j++) {
//				if(IndoorSpace.gP2PMatrix[i][j]!=-1) {
//					if(!IndoorSpace.gPartitions.get(i).getmDoors().contains(IndoorSpace.gP2PMatrix[i][j])) {
//						System.out.println("error!");
//						System.exit(-1);
//					}
//				}
//			}
//		}
		// --

//		System.out.println("The number of doors in the indoor space");
//		System.out.println(IndoorSpace.gDoors.size());

		OTTGen ottGen = new OTTGen();
		if (curTime >= DataGenConstant.totalLifecycle) {
			System.out.println("Wrong Parameter!");
			return -1;
		} else {
//			ottGen.genOTTbyTime_new(curTime, flag);
			System.out.println("OTT DONE!");
			int t_min = curTime - OTTConstant.minSamplingPeriod + 1;
			if (t_min > 0) {
				return t_min;
			} else
				return 0;
		}

	}

}
