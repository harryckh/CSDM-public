package algorithm;

import java.io.FileWriter;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import dk.aau.cs.idq.datagen.DataGen;
import dk.aau.cs.idq.datagen.OTTGen;
import dk.aau.cs.idq.indoorentities.*;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.OTTConstant;

public class AlgCSDM {

	// -------------------------------------------------------
	// input parameters.

	// 0 = guass, 1=uniform, 2= zip
	public static int Distribution = 0;

	private int monTimestampsSize = 60;

	public static double distTH = 3;
	public static double r_min = 2;

	public static int batchProcessing = 1;
	public static int betaPruning = 1;

	// File name for both save and load
	public static String sizeTable = "objsSize" + (DataGenConstant.nObjects) + ".txt";
	public static String trajectory = "Trajectories" + (DataGenConstant.nObjects) + ".txt";
	public static String resultFile = "result" + (DataGenConstant.nObjects) + ".txt";
	public static String statFile = "stat" + (DataGenConstant.nObjects) + ".txt";
	public static String sizeStat = "timestat" + (DataGenConstant.nObjects) + ".txt";

	// Stat. storing multiple trials
	private static long grandAvgRunTime = 0;
	private static long grandAvgMem = 0;
	private static long gradAvgNumOfResultsPerSecond = 0;

	// -------------------------------------------------------
	// global result here.
	public HashMap<Integer, ArrayList<String>> result = new HashMap<Integer, ArrayList<String>>();
	public int totalNumOfResults;

	// global random are used
	// two separate seeds to avoid interference
	public static Random random = new Random(1);
	public static Random randomUpdateSeed = new Random(1); // for OTT

	private int printTimeStat = 1;

	public AlgCSDM(int monTimestampsSize, int printTimeStat) {
		this.monTimestampsSize = monTimestampsSize;
		this.printTimeStat = printTimeStat;
	}

	public AlgCSDM() {
		// TODO Auto-generated constructor stub
	}

	// main framework here.
	// when an object o1 update /insert
	// i.e., OTT updateH
	// we check o1 with all other objects o2 in OTT
	public void CSDM(int trialNum) throws Exception {

//		System.out.println("Total Number of Objects: " + (DataGenConstant.nFloor * DataGenConstant.nObjPerFloor));
//		IndoorSpace.observedObjsList = new ArrayList<HashMap<Integer, IdrObj>>();
//		for (int i = 0; i < DataGenConstant.nFloor; i++) {
//			IndoorSpace.observedObjsList.add(new HashMap<Integer, IdrObj>());
//		}
		IndoorSpace.observedObjsList = new ArrayList<ArrayList<IdrObj>>();
		for (int i = 0; i < DataGenConstant.nFloor; i++) {
			IndoorSpace.observedObjsList.add(new ArrayList<IdrObj>());
		}

		OTTGen ottGen = new OTTGen();
		if (trialNum == 1)
			ottGen.generateOTT(0, 0);// initialize everything including indoor space

		ExecutorService executor = Executors.newSingleThreadExecutor();

		System.out.println("Trial " + trialNum + " : Running from t=0 to " + monTimestampsSize + " ... ");
		// main execution loop
		for (int curTime = 0; curTime < monTimestampsSize; curTime++) {
//			System.out.println("=== t=" + curTime + " === ");

			List<List<IdrObj>> newObjsList = ottGen.genOTTbyTime_new(curTime, 1);// read new objects

			int cntNewAndUpdate = 0;
			int floor = 0;
//			System.out.println("----------- new/update objects -------------");
			for (List<IdrObj> newObjs : newObjsList) {
//				System.out.println("Floor:"+(floor++) + " size:" + newObjs.size());
//				for(IdrObj oo:newObjs) {
//					System.out.println(oo.toString());
//				}
				cntNewAndUpdate += newObjs.size();
			}

			int cntExpired = 0;
			int newResultSize = 0;
			int cntUpdate = 0;

			// remove the updating obj from OTT
			// to avoid same obj that updated loc join with old loc
			for (List<IdrObj> newObjs : newObjsList) {
				for (int i = 0; i < newObjs.size(); i++) {
					if (IndoorSpace.OTT.get(newObjs.get(i).getmID()) != null) {
						cntUpdate++;
						IndoorSpace.OTT.remove(newObjs.get(i).getmID());
					}
				}
			}

			// ----------------------------------------------------------
			// Query update here.
			// thread pool implementation.
			Future<ArrayList<ArrayList<String>>> fu = executor.submit(new QueryUpdate(curTime, newObjsList));
			ArrayList<ArrayList<String>> newResult = fu.get();

			newResultSize = maintainResult(curTime, newResult);
			cntExpired = maintainOTT(curTime, newObjsList);
			// ----------------------------------------------------------

//			System.out.println("#update:" + cntUpdate);
//			System.out.println("#newObjs: " + (cntNewAndUpdate - cntUpdate));
//			System.out.println("#removedObjs: " + cntExpired);
//			System.out.println("#newResultSize:" + newResultSize);
			if (trialNum == 1)
				printResult(curTime, cntUpdate, cntNewAndUpdate - cntUpdate, cntExpired);
		} // end for

		// shutdown the update thread
		executor.shutdown();
		printStat(trialNum);
	}

	private void printResult(int curTime, int numOfUpdate, int numOfNew, int numOfExpired) {
		try {
			FileWriter fw;

			if (curTime == 0)
				fw = new FileWriter(DataGen.outputPath + "/" + resultFile);
			else
				fw = new FileWriter(DataGen.outputPath + "/" + resultFile, true);
			fw.write("t=" + curTime + "\n");

			fw.write("#updateObjs:" + numOfUpdate + "\n");
			fw.write("#newObjs:" + numOfNew + "\n");
			fw.write("#expiredObjs:" + numOfExpired + "\n");
			fw.write("Result:" + "\n");
			for (int i = 0; i < curTime + OTTConstant.predictingPeriod; i++) {
				if (result.get(i) != null)
					fw.write(i + " " + result.get(i) + "\n");
			}
			fw.write("\n");

			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void printStat(int i) {
		// save stat to file
		try {
			FileWriter fw = new FileWriter(DataGen.outputPath + "/" + statFile);

//			System.out.println("stat @t=" + curTime + " " + "cnt:" + QueryUpdate.cntRun);
//			System.out.println("totalRunTime/cnt:" + QueryUpdate.totalRunTime / QueryUpdate.cntRun + " ("
//					+ (double) QueryUpdate.totalRunTime / (QueryUpdate.cntRun * 1000000000.0) + "s)");
//			System.out.println("totalMem/cnt:" + QueryUpdate.totalMem / QueryUpdate.cntRun);
//			System.out.println("resultSize/cnt:" + totalNumOfResults / QueryUpdate.cntRun);

//			fw.write((QueryUpdate.totalRunTime / QueryUpdate.cntRun) + "\n");
//			fw.write((QueryUpdate.totalMem / QueryUpdate.cntRun) + "\n");
//			fw.write(totalNumOfResults * 1.0 / QueryUpdate.cntRun + "\n");
//			fw.write(QueryUpdate.cntRun + "\n");
//			fw.write(QueryUpdate.df.format(QueryUpdate.totalRunTime / (QueryUpdate.cntRun * 1000000000.0)) + "\n\n");

			grandAvgRunTime += QueryUpdate.totalRunTime / (QueryUpdate.cntRun);
			grandAvgMem += QueryUpdate.totalMem / (QueryUpdate.cntRun);
			gradAvgNumOfResultsPerSecond += totalNumOfResults / (QueryUpdate.cntRun);

			/* stat avg over multiple runs */
			fw.write((grandAvgRunTime / (i)) + "\n");
			fw.write((grandAvgMem / (i)) + "\n");
			fw.write(gradAvgNumOfResultsPerSecond / (i) + "\n");
			fw.write(QueryUpdate.cntRun + "\n");
//			fw.write(QueryUpdate.df.format(grandAvgRunTime / (i * 1000000000.0)) + "\n");
			fw.write("\n");

			/* input parameters */
			fw.write(distTH + "\n");
			fw.write(r_min * 2 + "\n");
			fw.write(batchProcessing + "\n");
			fw.write(betaPruning + "\n\n");

			fw.write(monTimestampsSize + "\n\n");

			fw.write(OTTConstant.predictingPeriod + "\n");
			fw.write(OTTConstant.minSamplingPeriod + "\n");
			fw.write(OTTConstant.maxSamplingPeriod + "\n\n");

//			fw.write(DataGenConstant.scale + "\n");
			fw.write(DataGenConstant.nFloor + "\n");
			fw.write(DataGenConstant.nObjPerFloor + "\n");
			fw.write(DataGenConstant.nObjects + "\n\n");

			fw.write(i + "\n");

			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Maintain the OTT
	 * 
	 * @return number of revmoed objects
	 */
	private int maintainOTT(int curTime, List<List<IdrObj>> newObjsList) {
		// TODO Auto-generated method stub
		int sumRemove = 0;
		// ---- adding new obj
		for (List<IdrObj> newObjs : newObjsList) {

			for (int i = 0; i < newObjs.size(); i++)
				IndoorSpace.OTT.put(newObjs.get(i).getmID(), curTime);
		}
		// --- removed expired
		// need to use iterator here, as we need to remove entry inside the loop
		Iterator<Entry<Integer, Integer>> it = IndoorSpace.OTT.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Integer> entry = it.next();

//			System.out.println(entry.getValue()+ " "+ curTime);

			// +1 since it is checking the next time second
			if (entry.getValue() + OTTConstant.maxSamplingPeriod <= curTime + 1) {
				// remove OTT entry and the obj
//				System.out.println("Removing Object: " + entry.getKey() + " tl:" + entry.getValue());
				for (int i = 0; i < DataGenConstant.nFloor; i++) {

//					IdrObj oo = IndoorSpace.observedObjsList.get(i).remove(entry.getKey());
					// ----
					List<IdrObj> objs = IndoorSpace.observedObjsList.get(i);
					for (int j = 0; j < objs.size(); j++) {
						if (objs.get(j).getmID() == entry.getKey()) {
//							System.out.println("Removing " + objs.get(j).getmID());
							if (objs.get(j) != null)
								objs.get(j).setUr(null);
							objs.remove(j);
							break;
						}
					}
					// ---
				}
				it.remove();
				sumRemove++;
			}
		}

		return sumRemove;
	}

	/**
	 * Maintain the global result by removing expired result
	 * 
	 * @return number of new results
	 */
	private int maintainResult(int curTime, ArrayList<ArrayList<String>> newResult) {
		// TODO Auto-generated method stub
		int sumNewResults = 0;

//		System.out.println("New Result @curTime=" + curTime + ":");
		for (int i = 0; i < OTTConstant.predictingPeriod; i++) {
			if (newResult.get(i) != null) {
//				System.out.println((i+curTime) + " " + newResult.get(i).size() + " " + newResult.get(i));
				sumNewResults += newResult.get(i).size();

				// add to main result
				if (result.get(i + curTime) != null) {
					result.get(i + curTime).addAll(newResult.get(i));
				} else
					result.put(i + curTime, newResult.get(i));
			}
			// -----------------
		}
		totalNumOfResults += sumNewResults;
		// remove expired result
		result.remove(curTime - 1);

//		System.out.println("Result:");
//		for (int i = 0; i < curTime + OTTConstant.predictingPeriod; i++) {
//			if (result.get(i) != null)
//				System.out.println(i + " " + result.get(i).size() + " " + result.get(i));
//		}

		return sumNewResults;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// parameter pre-checking.
		if (OTTConstant.minSamplingPeriod > OTTConstant.maxSamplingPeriod) {
			System.err.println("OTTConstant.minSamplingPeriod>OTTConstant.maxSamplingPeriod");
			System.exit(-1);
		}
		if (OTTConstant.predictingPeriod > OTTConstant.maxSamplingPeriod) {
			System.err.println("OTTConstant.maxPredictingPeriod>OTTConstant.maxSamplingPeriod");
			System.exit(-1);
		}

		// -----
		QueryUpdate.cntRun = 0;
		QueryUpdate.totalRunTime = 0;
		QueryUpdate.totalMem = 0;
		IndoorSpace.OTT = new HashMap<Integer, Integer>();

		AlgCSDM csdm = new AlgCSDM();
		csdm.result = new HashMap<Integer, ArrayList<String>>();
		csdm.totalNumOfResults = 0;

		System.gc();

		csdm.CSDM(1); // actual run

	}

}
