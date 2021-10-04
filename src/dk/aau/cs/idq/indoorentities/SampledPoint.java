/**
 * 
 */
package dk.aau.cs.idq.indoorentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dk.aau.cs.idq.utilities.Constant;
import dk.aau.cs.idq.utilities.DataGenConstant;

/**
 * SampledPoint
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.20
 * @see java.lang.Comparable
 */
public class SampledPoint implements Comparable<Object>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3765157229810673423L;

	private int spID;
	
	private int objID;
	
	private double sampledX;
	
	private double sampledY;
	
	private int mFloor;
	
	private Par curPar;
	
	private double contributes;
	

	/**
	 * Constructor Function
	 * 
	 * @param objID
	 * @param sampledX
	 * @param sampledY
	 * @param mFloor
	 */
	public SampledPoint(int objID, double sampledX, double sampledY, int mFloor, Par curPar) {
		super();
		this.spID = ++DataGenConstant.mID_SampledPoints;
		this.objID = objID;
		this.sampledX = sampledX;
		this.sampledY = sampledY;
		this.mFloor = mFloor;
		this.curPar = curPar;
	}
	
	/**
	 * Constructor Function
	 * 
	 * @param objID
	 * @param sampledX
	 * @param sampledY
	 * @param mFloor
	 */
	public SampledPoint(int objID, double sampledX, double sampledY, int mFloor, Par curPar, double contributes) {
		super();
		this.spID = ++DataGenConstant.mID_SampledPoints;
		this.objID = objID;
		this.sampledX = sampledX;
		this.sampledY = sampledY;
		this.mFloor = mFloor;
		this.curPar = curPar;
		this.contributes = contributes;
	}
	
	/**
	 * Constructor Function
	 * 
	 * @param spID
	 * @param objID
	 * @param sampledX
	 * @param sampledY
	 * @param mFloor
	 * @param curPar
	 */
	public SampledPoint(int spID, int objID, double sampledX, double sampledY, int mFloor, Par curPar) {
		super();
		this.spID = spID;
		this.objID = objID;
		this.sampledX = sampledX;
		this.sampledY = sampledY;
		this.mFloor = mFloor;
		this.curPar = curPar;
	}
	
	/**
	 * Constructor Function
	 * 
	 * @param spID
	 * @param objID
	 * @param sampledX
	 * @param sampledY
	 * @param mFloor
	 * @param curPar
	 */
	public SampledPoint(int spID, int objID, double sampledX, double sampledY, int mFloor, Par curPar, double contributes) {
		super();
		this.spID = spID;
		this.objID = objID;
		this.sampledX = sampledX;
		this.sampledY = sampledY;
		this.mFloor = mFloor;
		this.curPar = curPar;
		this.contributes = contributes;
	}

	public SampledPoint(SampledPoint sp1) {
		// TODO Auto-generated constructor stub
		super();
		this.spID = sp1.spID;
		this.objID = sp1.objID;
		this.sampledX = sp1.sampledX;
		this.sampledY = sp1.sampledY;
		this.mFloor = sp1.mFloor;
		this.curPar = sp1.curPar;
		this.contributes = sp1.contributes;
	}

	/**
	 * @return the objID
	 */
	public int getObjID() {
		return objID;
	}

	/**
	 * @param objID the objID to set
	 */
	public void setObjID(int objID) {
		this.objID = objID;
	}

	/**
	 * @return the sampledX
	 */
	public double getSampledX() {
		return sampledX;
	}

	/**
	 * @param sampledX the sampledX to set
	 */
	public void setSampledX(double sampledX) {
		this.sampledX = sampledX;
	}

	/**
	 * @return the sampledY
	 */
	public double getSampledY() {
		return sampledY;
	}

	/**
	 * @param sampledY the sampledY to set
	 */
	public void setSampledY(double sampledY) {
		this.sampledY = sampledY;
	}

	/**
	 * @return the mFloor
	 */
	public int getmFloor() {
		return mFloor;
	}

	/**
	 * @param mFloor the mFloor to set
	 */
	public void setmFloor(int mFloor) {
		this.mFloor = mFloor;
	}
	
	/**
	 * toString
	 * 
	 * @return [objID,(x,y,mFloor)]
	 */
	public String toString(){
		return this.spID + "," + this.objID + "," + this.sampledX + "," + this.sampledY + "," + this.mFloor + "," + this.curPar.getmID() + "," + this.contributes;
	}
	
	private List<NextPossiblePar> getExtendedNextPossiblePars(Door curDoor, Par curPar, double onGoing) {
		// TODO Auto-generated method stub
		
		List<NextPossiblePar> mPossiblePars = new ArrayList<NextPossiblePar>();
		Iterator<LeavePair> itr = curPar.getLeaveablePars().iterator(); 
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			Par nextPar = IndoorSpace.gPartitions
					.get(nextLeavePair.getmParID());
			//double nextDist2Door = curDoor.eDist(nextDoor);
			double nextDist2Door = IndoorSpace.gD2DMatrix[nextDoor.getmID()%IndoorSpace.gNumberDoorsPerFloor][curDoor.getmID()%IndoorSpace.gNumberDoorsPerFloor];
			if ((nextDist2Door != 0) && nextDist2Door < onGoing) {
				NextPossiblePar nextPossiblePar = new NextPossiblePar(nextDoor,
						nextPar, (onGoing - nextDist2Door));
				mPossiblePars.add(nextPossiblePar);
				List<NextPossiblePar> extendedPossiblePars = getExtendedNextPossiblePars(
						nextDoor, nextPar, (onGoing - nextDist2Door));
				mPossiblePars.addAll(extendedPossiblePars);
			}
		}
		return mPossiblePars;
	}
	
	/**
	 * 
	 */
	public List<NextPossiblePar> getUncertainPars(int timeInterval){
		
		double maxGoing = timeInterval * DataGenConstant.maxVelocity;
		
		List<NextPossiblePar> nextPossiblePars = new ArrayList<NextPossiblePar>();
		
		Iterator<LeavePair> itr = this.curPar.getLeaveablePars().iterator();
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			Par nextPar = IndoorSpace.gPartitions.get(nextLeavePair.getmParID());
			
			double p2dDist = this.eDist(nextDoor);
			//System.out.println(p2dDist);
			if (p2dDist < maxGoing) {
				NextPossiblePar nextPossiblePar = new NextPossiblePar(nextDoor,nextPar,(maxGoing - p2dDist));
				nextPossiblePars.add(nextPossiblePar);
				List<NextPossiblePar> extendedPossiblePars = getExtendedNextPossiblePars(nextDoor,nextPar,(maxGoing - p2dDist));
				nextPossiblePars.addAll(extendedPossiblePars);
			}
		}
		return nextPossiblePars;
		
	}
	
	public List<NextPossiblePar> getUncertainParsD2D(int timeInterval){
		
		List<NextPossiblePar> nextPossiblePars = new ArrayList<>();
		
		double maxGoing = timeInterval * DataGenConstant.maxVelocity;
		
		Map<Integer, Double> p2dDistList = new HashMap<>();
		
		Iterator<LeavePair> itr = this.curPar.getLeaveablePars().iterator();
		
		while (itr.hasNext()) {
			LeavePair nextLeavePair = itr.next();
			Door nextDoor = IndoorSpace.gDoors.get(nextLeavePair.getmDoorID());
			double p2dDist = this.eDist(nextDoor);
			if(p2dDist < maxGoing){
				p2dDistList.put(nextLeavePair.getmDoorID()%IndoorSpace.gNumberDoorsPerFloor, p2dDist);
			}
		}
		
		
		double marginLeft = this.sampledX - maxGoing;
		double marginRight = this.sampledX + maxGoing;
		double marginTop = this.sampledY - maxGoing;
		double marginBottom = this.sampledY + maxGoing;
		
		Iterable<Par> pars = IndoorSpace.gRTree.find(marginLeft, marginTop, marginRight, marginBottom);
		
		Set<Integer> doorsID = new HashSet<>();
		
		for(Par par : pars){
			doorsID.addAll(IndoorSpace.gPartitions.get(par.getmID() + (IndoorSpace.gNumberParsPerFloor * this.mFloor)).getmDoors());
		}
		
		//System.out.println(doorsID);
		for(int doorID : doorsID){
			Door door = IndoorSpace.gDoors.get(doorID);
			if(this.eDist(door) < maxGoing){
				int modeID = doorID % IndoorSpace.gNumberDoorsPerFloor;
				double minDist = Constant.DISTMAX;
				for(Entry<Integer, Double> entry : p2dDistList.entrySet()){
					double dist = IndoorSpace.gD2DMatrix[entry.getKey()][modeID] + entry.getValue();
					if(dist < minDist){
						minDist = dist;
					}
				}
				if(minDist < maxGoing){
					for(int parID : door.getmPartitions()){
						Par par = IndoorSpace.gPartitions.get(parID);
						if(parID != this.curPar.getmID()){
							nextPossiblePars.add(new NextPossiblePar(IndoorSpace.gDoors.get(doorID), par, (maxGoing - minDist)));
						}
					}
				}
			}
		}
		
		return nextPossiblePars;
		
	}

	private double eDist(Door door) {
		// TODO Auto-generated method stub
		return Math.sqrt(Math.pow(this.sampledX - door.getX(), 2) + Math.pow(this.sampledY - door.getY(), 2));
	}
	
	public double eDist(Point point) {
		return Math.sqrt(Math.pow(this.sampledX - point.getX(), 2) + Math.pow(this.sampledY - point.getY(), 2));
	}
	
	public double eDist(SampledPoint point) {
		return Math.sqrt(Math.pow(this.sampledX - point.getSampledX(), 2) + Math.pow(this.sampledY - point.getSampledY(), 2));
	}

/**
 * 
 * @param p2 
 * @return the shortest indoor distance from this point to p2
 */
	private double iDist(Point p2) {

		if (curPar == p2.getCurrentPar()) {
			return this.eDist(p2);
		}

		double shortestDist = Constant.DISTMAX;
		// try distance from each pair of di to dj
		for (int d1 : curPar.getmDoors()) {
			Door di = IndoorSpace.gDoors.get(d1);
			double temp = this.eDist(di);
			for (int d2 : p2.getCurrentPar().getmDoors()) {
				Door dj = IndoorSpace.gDoors.get(d2);
				double tempDist = temp + IndoorSpace.gD2DMatrix[d1][d2] + p2.eDist(dj);
				if (tempDist < shortestDist) {
					shortestDist = tempDist;
				}
			}
		}
		return shortestDist;
	}
	
	/**
	 * @return the curPar
	 */
	public Par getCurPar() {
		return curPar;
	}

	/**
	 * @param curPar the curPar to set
	 */
	public void setCurPar(Par curPar) {
		this.curPar = curPar;
	}

	/**
	 * @return the spID
	 */
	public int getSpID() {
		return spID;
	}

	/**
	 * @param spID the spID to set
	 */
	public void setSpID(int spID) {
		this.spID = spID;
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		SampledPoint other = (SampledPoint) o;
		return this.spID > other.spID ? 1
				: (this.spID == other.spID ? 0 : -1);
	}

	public static SampledPoint parse(String string) {
		// TODO Auto-generated method stub
		
		String []items = string.split(",");
		Par curPar = IndoorSpace.gPartitions.get(Integer.valueOf(items[5]));
		SampledPoint sp = new SampledPoint(Integer.valueOf(items[0]), Integer.valueOf(items[1]), Double.valueOf(items[2]), Double.valueOf(items[3]), Integer.valueOf(items[4]), curPar, Double.valueOf(items[6]));
		curPar.addSampledObj(sp);
		
		return sp;
		
	}

	/**
	 * @return the contributes
	 */
	public double getContributes() {
		return contributes;
	}

	/**
	 * @param contributes the contributes to set
	 */
	public void setContributes(double contributes) {
		this.contributes = contributes;
	}

}
