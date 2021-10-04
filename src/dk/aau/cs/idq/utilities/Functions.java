
package dk.aau.cs.idq.utilities;

import org.apache.commons.math3.distribution.PoissonDistribution;

import dk.aau.cs.idq.indoorentities.Point;

/**
 * Utility Functions
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public class Functions {

	/**
	 * Helper function
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return the closest value of value in [min, max]
	 */
	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
	
	/**
	 * generate a random number in range [min,max]
	 */
	public static double rand_d(double min, double max) {
		return (Math.random() * (max - min)) + min;
	}

	/**
	 * generate the random integer with Poisson Distribution
	 * 
	 * @param Lamda parameter for Poisson Distribution
	 * @return x a random positive integer
	 */
	public static double P_rand(double Lamda) {
		double x = 0, b = 1, c = Math.exp(-Lamda), u;
		do {
			u = Math.random();
			b *= u;
			if (b >= c)
				x++;
		} while (b >= c);
		return x;
	}

	/**
	 * concatenate two parameters to form a key
	 * 
	 * @param i parameter 1
	 * @param j parameter 2
	 * @return string key
	 */
	public static String keyConventer(int i, int j) {
		return ((i <= j)) ? (i + "#" + j) : (j + "#" + i);
	}

	/**
	 * tests if the object is indoor
	 * 
	 * @return booleanValue True is in the building
	 * @param randomPoint
	 */
	public static boolean isInBuilding(Point randomPoint) {
		// TODO Auto-generated method stub
		double x = randomPoint.getX();
		double y = randomPoint.getY();
		if (x <= 0 || y <= 0 || x >= DataGenConstant.floorRangeX || y >= DataGenConstant.floorRangeY) {
			return false;
		}
		if (x < DataGenConstant.outRectLeft.getX2() && DataGenConstant.outRectLeft.containVertical(randomPoint)) {
			return false;
		}
		if (x > DataGenConstant.outRectRight.getX1() && DataGenConstant.outRectRight.containVertical(randomPoint)) {
			return false;
		}
		if (y < DataGenConstant.outRectTop.getY2() && DataGenConstant.outRectTop.containVertical(randomPoint)) {
			return false;
		}
		if (y > DataGenConstant.outRectBottom.getY1() && DataGenConstant.outRectBottom.containVertical(randomPoint)) {
			return false;
		}
		return true;
	}

	public static void main(String args[]) {
		double myLamda = 1;
		PoissonDistribution pd = new PoissonDistribution(myLamda);

		for (int i = 0; i < 0; i++) {
			double random = P_rand(myLamda);
			System.out.println(random + " > " + pd.probability((int) random));
		}

		System.out.println(269 % 141);

	}

}
