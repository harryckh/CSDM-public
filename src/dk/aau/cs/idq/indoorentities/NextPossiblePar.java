
package dk.aau.cs.idq.indoorentities;

/**
 * NextPossiblePar to describe the touchable partitions of a given partition
 * within an interval
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 *
 */
public class NextPossiblePar implements Comparable<Object> {

	public Door possibleTroughDoor; // the through door to touch one partition

	public Par possibleNextPar; // the touchable partition

	public double distanceToContinue; // when arrive at the possibleTroughDoor, how long can be continued

	/// ---
	public double timeArrive; /// the time of arriving this partition
	/// ---

	/**
	 * Constructor Function
	 * 
	 * @param possibleTroughDoor
	 * @param possibleNextPar
	 * @param distanceToContinue
	 */
	public NextPossiblePar(Door possibleTroughDoor, Par possibleNextPar, double distanceToContinue) {
		super();
		this.possibleTroughDoor = possibleTroughDoor;
		this.possibleNextPar = possibleNextPar;
		this.distanceToContinue = distanceToContinue;
	}

	public NextPossiblePar(Door possibleTroughDoor, Par possibleNextPar, double distanceToContinue, double time) {
		super();
		this.possibleTroughDoor = possibleTroughDoor;
		this.possibleNextPar = possibleNextPar;
		this.distanceToContinue = distanceToContinue;
		this.timeArrive = time;
	}

	/**
	 * randomly decide a position in the possbileNextPar
	 * 
	 * @return a random point in the current possibleNextPar
	 */
	public Point randomPositionInPossiblePar() {
		int count = 0;
		Point[] randomPointPair = this.possibleTroughDoor.randomWalk(distanceToContinue);

		// make sure that the point is contained in the PossibleNextPar
		while ((!possibleNextPar.contain(randomPointPair[0])) && (!possibleNextPar.contain(randomPointPair[1]))) {
			count++;
			randomPointPair = this.possibleTroughDoor.randomWalk(distanceToContinue);
		}
		if (count > 0) {
			// System.out.println((count+1) + " times hit!");
		}

		// if it contains point1, choose it, otherwise, choose the other one
		if (possibleNextPar.contain(randomPointPair[0]))
			return randomPointPair[0];
		else {
			return randomPointPair[1];
		}

	}

	/**
	 * @return the possibleTroughDoor
	 */
	public Door getPossibleTroughDoor() {
		return possibleTroughDoor;
	}

	/**
	 * @param possibleTroughDoor the possibleTroughDoor to set
	 */
	public void setPossibleTroughDoor(Door possibleTroughDoor) {
		this.possibleTroughDoor = possibleTroughDoor;
	}

	/**
	 * @return the possibleNextPar
	 */
	public Par getPossibleNextPar() {
		return possibleNextPar;
	}

	/**
	 * @param possibleNextPar the possibleNextPar to set
	 */
	public void setPossibleNextPar(Par possibleNextPar) {
		this.possibleNextPar = possibleNextPar;
	}

	/**
	 * @return the distanceToContinue
	 */
	public double getDistanceToContinue() {
		return distanceToContinue;
	}

	/**
	 * @param distanceToContinue the distanceToContinue to set
	 */
	public void setDistanceToContinue(double distanceToContinue) {
		this.distanceToContinue = distanceToContinue;
	}

	
	
	public double getTimeArrive() {
		return timeArrive;
	}

	public void setTimeArrive(double timeArrive) {
		this.timeArrive = timeArrive;
	}

	/**
	 * test one sampledPoint is contained in this NextPossiblePar
	 * 
	 * @param sampledPoint
	 * @return boolean value
	 */
	public boolean Contain(SampledPoint sampledPoint) {

		double dist = this.possibleTroughDoor
				.eDist(new Point(sampledPoint.getSampledX(), sampledPoint.getSampledY(), sampledPoint.getmFloor()));
		// if(sampledPoint.getSpID() == 15633){System.out.println(dist + "," +
		// this.distanceToContinue);}
		if (dist <= this.distanceToContinue) {
			return true;
		}
		return false;
	}

	/**
	 * test one sampledPoint is contained in this NextPossiblePar
	 * 
	 * @param point
	 * @return boolean value
	 */
	public boolean Contain(Point point) {

		double dist = this.possibleTroughDoor.eDist(new Point(point.getX(), point.getY(), point.getmFloor()));
		// if(sampledPoint.getSpID() == 15633){System.out.println(dist + "," +
		// this.distanceToContinue);}
		if (dist <= this.distanceToContinue) {
			return true;
		}
		return false;
	}

	/**
	 * test if this PossbileNextPar can be fully covered during the walking period
	 * 
	 * @return boolean value
	 */
	public boolean isFullyCovered() {
		// TODO Auto-generated method stub
		if (this.possibleNextPar.getMaxDist(possibleTroughDoor) <= this.distanceToContinue) {
			return true;
		}
		return false;
	}

	/**
	 * get a MBR for the PossbileNextPar
	 * 
	 * @return a rectangle MBR
	 */
	public Rect getMBR() {
		// TODO Auto-generated method stub

		/*
		 * if(this.possibleTroughDoor.getX() == this.possibleNextPar.getX1()){ // from
		 * left side double xBound = this.possibleTroughDoor.getX() +
		 * this.distanceToContinue; double yUpperBound = this.possibleTroughDoor.getY()
		 * + this.distanceToContinue; double yLowerBound =
		 * this.possibleTroughDoor.getY() - this.distanceToContinue;
		 * 
		 * Rect rect = new Rect(this.possibleTroughDoor.getX(), xBound, yLowerBound,
		 * yUpperBound); return this.possibleNextPar.intersection(rect); }
		 * 
		 * if(this.possibleTroughDoor.getX() == this.possibleNextPar.getX2()){ // from
		 * right side double xBound = this.possibleTroughDoor.getX() -
		 * this.distanceToContinue; double yUpperBound = this.possibleTroughDoor.getY()
		 * + this.distanceToContinue; double yLowerBound =
		 * this.possibleTroughDoor.getY() - this.distanceToContinue;
		 * 
		 * Rect rect = new Rect(xBound, this.possibleTroughDoor.getX(), yLowerBound,
		 * yUpperBound); return this.possibleNextPar.intersection(rect); }
		 * 
		 * if(this.possibleTroughDoor.getY() == this.possibleNextPar.getY1()){ // from
		 * bottom side double yBound = this.possibleTroughDoor.getX() +
		 * this.distanceToContinue; double xUpperBound = this.possibleTroughDoor.getX()
		 * + this.distanceToContinue; double xLowerBound =
		 * this.possibleTroughDoor.getX() - this.distanceToContinue;
		 * 
		 * Rect rect = new Rect(xLowerBound, xUpperBound,
		 * this.possibleTroughDoor.getY(), yBound); return
		 * this.possibleNextPar.intersection(rect); }
		 * 
		 * if(this.possibleTroughDoor.getY() == this.possibleNextPar.getY2()){ // from
		 * top side double yBound = this.possibleTroughDoor.getX() -
		 * this.distanceToContinue; double xUpperBound = this.possibleTroughDoor.getX()
		 * + this.distanceToContinue; double xLowerBound =
		 * this.possibleTroughDoor.getX() - this.distanceToContinue;
		 * 
		 * Rect rect = new Rect(xLowerBound, xUpperBound, yBound,
		 * this.possibleTroughDoor.getY()); return
		 * this.possibleNextPar.intersection(rect); }
		 */

		Rect rect = new Rect(this.possibleTroughDoor.getX() - this.distanceToContinue,
				this.possibleTroughDoor.getX() + this.distanceToContinue,
				this.possibleTroughDoor.getY() - this.distanceToContinue,
				this.possibleTroughDoor.getY() + this.distanceToContinue);
		rect.setmFloor(this.possibleNextPar.getmFloor());
		return this.possibleNextPar.intersection(rect);

	}

	public String toString() {
		return this.possibleTroughDoor.getmID() + "," + this.possibleNextPar.getmID() + "," + this.distanceToContinue
				+ ", " + this.timeArrive;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		NextPossiblePar another = (NextPossiblePar) o;
		if (this.possibleNextPar.getmID() < another.possibleNextPar.getmID()) {
			return 1;
		} else if (this.possibleNextPar.getmID() == another.possibleNextPar.getmID()) {
			if (this.possibleTroughDoor.getmID() < another.possibleTroughDoor.getmID()) {
				return 1;
			} else if (this.possibleTroughDoor.getmID() == another.possibleTroughDoor.getmID()) {
				if (this.distanceToContinue < another.distanceToContinue) {
					return 1;
				} else if (this.distanceToContinue == another.distanceToContinue) {
					return 0;
				} else {
					return -1;
				}
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/// ------------ for contain checking ------------------
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((possibleNextPar == null) ? 0 : possibleNextPar.hashCode());
		result = prime * result + ((possibleTroughDoor == null) ? 0 : possibleTroughDoor.hashCode());
		return result;
	}

	/// ------------ for contain checking ------------------
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NextPossiblePar other = (NextPossiblePar) obj;
		if (possibleNextPar == null) {
			if (other.possibleNextPar != null)
				return false;
		} else if (!(possibleNextPar.getmID() == other.possibleNextPar.getmID()))
			return false;
		if (possibleTroughDoor == null) {
			if (other.possibleTroughDoor != null)
				return false;
		} else if (!(possibleTroughDoor.getmID() == other.possibleTroughDoor.getmID()))
			return false;
		return true;
	}

}
