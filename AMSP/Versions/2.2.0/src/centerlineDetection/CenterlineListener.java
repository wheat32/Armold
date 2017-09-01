package centerlineDetection;

public interface CenterlineListener extends IntersectionType
{
	/**
	 * <b>This abstract method reports a <code>Direction</code> called direction.</b>
	 * <p>
	 * The <code>report</code> method is called whenever a scan is completed and the results are sent out
	 * through this method.
	 * 
	 * @param direction <code>Direction</code> - provides a <code>Direction</code> received from a scan.
	 * @author Krish
	 * @since 1.0.0 
	 */
	void report(Direction direction);
	/**
	 * <b>This abstract method gives a <code>CenterlineDetector</code> called det.</b>
	 * <p>
	 * The intended use for this is to use the <code>CenterlineDetector</code> given call the 
	 * <code>addListener()</code> method to add an object as a listener.
	 * 
	 * @param det <code>CenterlineDetector</code> - provides a <code>CenterlineDetector</code>.
	 * @author Krish
	 * @since 1.0.0 
	 */
	void becomeListener(CenterlineDetector det);
}