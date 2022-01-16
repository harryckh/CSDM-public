/**

 * 
 */
package algorithm;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Random;

import dk.aau.cs.idq.indoorentities.IndoorSpace;
import dk.aau.cs.idq.indoorentities.Rect;
import dk.aau.cs.idq.utilities.DataGenConstant;
import dk.aau.cs.idq.utilities.OTTConstant;

/**
 * @author kai-ho
 *
 */
public class Experiment {

	public static int numOfTrials = 5; // number of times to be averaged

	public static int monTimestampsSize = 1000; // time stamp

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Runtime runtime = Runtime.getRuntime();
//		runtime.gc();
//		long startMem = runtime.totalMemory() - runtime.freeMemory();
//		long start = System.nanoTime();

//		System.out.println( runtime.maxMemory()/1024.0+ " " +runtime.totalMemory() /1024.0 + " "+ runtime.freeMemory()/1024.0);

		System.out.println("args.length: " + args.length);
		if (args.length < 13) {
			System.out.println(
					"usage: [numOfTrials] [totalLifecycle] [dis] [n/f] [f] [n] [d] [r] [batch] [prune] [T_p] [T_Min] [T_Max]");
			System.out.println("e.g., java -jar sdm.jar 5 60 0 500 20 10000 3 4 1 1 10 5 20");
			System.exit(-1);
		}

		// ------------------------------------------------
		// parameter setting in this run.

		int c = 0;
//		AlgCSDM.statFile = args[c++];
//		AlgCSDM.resultFile = "result" + AlgCSDM.statFile;
		
		numOfTrials = Integer.parseInt(args[c++]);
		monTimestampsSize = Integer.parseInt(args[c++]);

		AlgCSDM.Distribution = Integer.parseInt(args[c++]);
		DataGenConstant.nObjPerFloor = Integer.parseInt(args[c++]);
		DataGenConstant.nFloor = Integer.parseInt(args[c++]);
		DataGenConstant.nObjects = Integer.parseInt(args[c++]);
		
		if (DataGenConstant.nObjects != DataGenConstant.nFloor * DataGenConstant.nObjPerFloor) {
			System.out.println("nObjects!= nFloor * nObjPerFloor");
			System.exit(-1);
			;
		}

		AlgCSDM.sizeTable = "objsSize" + (DataGenConstant.nObjects) + ".txt";
		AlgCSDM.trajectory = "Trajectories" + (DataGenConstant.nObjects) + ".txt";

//		AlgCSDM.distTH = Integer.parseInt(args[c++]);
//		AlgCSDM.r_min = Integer.parseInt(args[c++]);
		AlgCSDM.distTH = Double.parseDouble(args[c++]);
		AlgCSDM.dia = Double.parseDouble(args[c++]);
		AlgCSDM.r_min = AlgCSDM.dia/2;

		AlgCSDM.batchProcessing = Integer.parseInt(args[c++]);
		AlgCSDM.betaPruning = Integer.parseInt(args[c++]);

		OTTConstant.predictingPeriod = Integer.parseInt(args[c++]);
		OTTConstant.minSamplingPeriod = Integer.parseInt(args[c++]);
		OTTConstant.maxSamplingPeriod = Integer.parseInt(args[c++]);

		// ------------------------------------------------
		// parameter correctness check.

		if (DataGenConstant.nFloor * DataGenConstant.nObjPerFloor != DataGenConstant.nObjects) {
			System.err.println("DataGenConstant.nFloor*DataGenConstant.nObjPerFloor!=DataGenConstant.nObjects");
			System.exit(-1);
		}
		if (OTTConstant.minSamplingPeriod > OTTConstant.maxSamplingPeriod) {
			System.err.println("OTTConstant.minSamplingPeriod>OTTConstant.maxSamplingPeriod");
			System.exit(-1);
		}
		if (OTTConstant.predictingPeriod > OTTConstant.maxSamplingPeriod) {
			System.err.println("OTTConstant.maxPredictingPeriod>OTTConstant.maxSamplingPeriod");
			System.exit(-1);
		}
		if (monTimestampsSize > DataGenConstant.totalLifecycle) {
			System.err.println(monTimestampsSize > DataGenConstant.totalLifecycle);
			System.exit(-1);
		}

		System.out.println("Number of Trials: " + numOfTrials);
		System.out.println("Total Life Cycle: " + monTimestampsSize);
		System.out.println("Number of objects: " + DataGenConstant.nObjects);
		System.out.println("epsilon: " + AlgCSDM.distTH + " dia: " + AlgCSDM.dia);
		System.out.println("batchProcessing: " + AlgCSDM.batchProcessing + " pruning: " + AlgCSDM.betaPruning);
		System.out.println("T_P: " + OTTConstant.predictingPeriod + " T_Min: " + OTTConstant.minSamplingPeriod
				+ " T_Max:" + OTTConstant.maxSamplingPeriod);
		System.out.println("Number of floors:" + DataGenConstant.nFloor);

		// ----------------------------------

		for (int i = 1; i <= numOfTrials; i++) {
//			System.out.println("---------------------------- i=" + i + " ----------------------------");
//					random = new Random(1);
			QueryUpdate.cntRun = 0;
			QueryUpdate.totalRunTime = 0;
			QueryUpdate.totalMem = 0;
			IndoorSpace.OTT = new HashMap<Integer, Integer>();
			AlgCSDM csdm = new AlgCSDM(monTimestampsSize, 0);
			AlgCSDM.random = new Random(1);
			AlgCSDM.randomUpdateSeed = new Random(1);
			csdm.result = new HashMap<Integer, ArrayList<String>>();
			csdm.totalNumOfResults = 0;
			System.gc();
			try {
				csdm.CSDM(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
