package algorithm;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;

import dk.aau.cs.idq.indoorentities.Par;
import dk.aau.cs.idq.indoorentities.SampledPoint;

/**
 * 
 * @author kai-ho
 *
 *         batch processing
 *
 */
public class Group {

	private SampledPoint sp;
	private double r;
	private int recordTime;

	// [0] is itself
	private ArrayList<IndoorUR> indoorURs = new ArrayList<IndoorUR>();

	private int groupID; // equal to the object ID of the obj created this group
							// i.e., smallest object id for objects in this group
							// to indicate whether this group is going to be merged

	private HashMap<Integer, List<Par>> curInPars = new HashMap<>();

	public Group(IndoorUR ur1, int recordTime) {
		// TODO Auto-generated constructor stub
		groupID = ur1.getmPoint().getObjID();

		indoorURs.add(ur1);
		this.recordTime = recordTime;
		this.r = ur1.getR_i();
		this.sp = new SampledPoint(ur1.getmPoint());
	}

	public void addUR(IndoorUR sp) {
		this.indoorURs.add(sp);
	}

	public void removedUR(int i) {
		this.indoorURs.remove(i);
	}

	public ArrayList<IndoorUR> getURs() {
		return indoorURs;
	}

	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	/**
	 * re-calculate the center and radius of the group based on the objects in
	 * sampledPoints
	 */
	public void updateCenter() {
		if (indoorURs.size() <= 1)
			return;

		double x = 0;
		double y = 0;

		for (IndoorUR ur : indoorURs) {
			x += ur.getmPoint().getSampledX();
			y += ur.getmPoint().getSampledY();
		}
		x /= indoorURs.size();
		y /= indoorURs.size();

		this.sp.setSampledX(x);
		this.sp.setSampledY(y);

		// calc. new r_min
		double radius = 0;

		for (IndoorUR ur : indoorURs) {
			double d = ur.getmPoint().eDist(sp) + ur.getR_i();
			if (d > radius)
				radius = d;
		}
//		System.out.println("radius:" + radius + " x:" + x + " y: " + y);
		this.r = radius;

	}

	public double getR_min() {
		return r;
	}

	public int getRecordTime() {
		// TODO Auto-generated method stub
		return recordTime;
	}

	public SampledPoint getmPoint() {
		// TODO Auto-generated method stub
		return sp;
	}

	// union of all pars in each ur -> unique
	public List<Par> getCurInPars(int curTime) {

		// TODO Auto-generated method stub
		int timeInterval = curTime - recordTime;

		if (timeInterval < 0) {
			System.err.println("timeInterval<0 @ getCurInPars.");
			System.exit(-1);
		}

		List<Par> temp = curInPars.get(timeInterval);
		if (temp != null) {
			return temp;
		}

		// --------
		List<Par> pars = new ArrayList<Par>();

		for (IndoorUR ur1 : indoorURs) {
			List<Par> pars2 = ur1.getCurInPars(curTime);
			for (Par par : pars2) {
				if (!pars.contains(par))
					pars.add(par);
			}
		}

		curInPars.put(timeInterval, pars);

		return pars;

	}

	public void addURs(ArrayList<IndoorUR> uRs) {
		// TODO Auto-generated method stub
		indoorURs.addAll(uRs);
	}
}
