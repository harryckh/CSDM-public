
package dk.aau.cs.idq.indoorentities;

import java.io.Serializable;

import org.khelekore.prtree.MBRConverter;

/**
 * RectConverter Modify the Rect.class to make it adaptable for the MBR
 * 
 * 
 * @author lihuan
 * @version 0.1 / 2014.10.06
 * @see org.khelekore.prtree.MBRConverter
 *
 */
public class RectConverter implements MBRConverter<Par>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4734832547848503165L;

	/*
	 * get the dimensions of the MBR
	 * 
	 * @see org.khelekore.prtree.MBRConverter#getDimensions()
	 */
	@Override
	public int getDimensions() {
		// TODO Auto-generated method stub
		return 2;
	}

	/*
	 * get the minimum value of a Par for a given axis
	 * 
	 * @see org.khelekore.prtree.MBRConverter#getMin(int, java.lang.Object)
	 */
	@Override
	public double getMin(int axis, Par t) {
		// TODO Auto-generated method stub
		if (0 == axis) {
			return t.getX1();
		} else if (1 == axis) {
			return t.getY1();
		} else
			return -1;
	}

	/*
	 * get the maximum value of a Par for a given axis
	 * 
	 * @see org.khelekore.prtree.MBRConverter#getMax(int, java.lang.Object)
	 */
	@Override
	public double getMax(int axis, Par t) {
		// TODO Auto-generated method stub
		if (0 == axis) {
			return t.getX2();
		} else if (1 == axis) {
			return t.getY2();
		} else
			return -1;
	}

}
