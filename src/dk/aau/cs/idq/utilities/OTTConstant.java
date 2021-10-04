
package dk.aau.cs.idq.utilities;

/**
 * Constant Values in OTT Generating
 * 
 *
 */
public class OTTConstant {

	/************************************************
	 * PARAMETERS FOR GENERATING OTT
	 ************************************************/
	/**
	 * To define a monitoring and predicting timewindow of the current time
	 * 
	 * !!! cannot be larger than maxSamplingPeriod !!!
	 */
	public static int predictingPeriod = 10;

	/** T_M: min time of updating */
	public static int minSamplingPeriod = 5;

	/** T_M: maximum time of not updating, assume dead object and remove from OTT */
	public static int maxSamplingPeriod = 20;

	/** probability of and object updates its location in a time instance */
	/** derived from the geometric distribution: */
	/** expected number of trials to have an update successfully */
//	public static double updateProb = 1 / (maxSamplingPeriod );

	public static double updateProb = 1.0 / (maxSamplingPeriod-minSamplingPeriod);
//	public static double updateProb = 1.0;

}
