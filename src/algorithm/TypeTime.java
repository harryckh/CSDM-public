package algorithm;
/**
 * 
 * @author kai-ho
 *
 * a class model the type of objects in time [startTime,endTime) 
 */
public class TypeTime {

	public int type;
	
	public int startTime;
	
	public int endTime;
	
	public TypeTime(int type, int startTime, int endTime) {
		super();
		this.type = type;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "[" + type + ", " + startTime + ", " + endTime + "]";
	}
	

}
