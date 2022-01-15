package modelparts;

/**
 * Contatins info about title: isPart, isFlex, isRoot, isSuf, isPref, isGood (tested), isNew (not tested)
 * @author halyna.galanzina
 *
 */
public class WPartTitle {
	
	protected boolean isPart = false; // all but not flex
	protected boolean isFlex = false; //end with _
	protected boolean isRoot = false; //part before flex
	
	protected boolean isSuf = false; //should be tested
	protected boolean isPref = false;//should be tested
	
	protected boolean isGood = false; //tested or reliable because of 2 or more slice cuts
	protected boolean isNew = false; // not tested, not reliable
	
	
	
	public WPartTitle() {
		
	}
	
	public void setPart(boolean b) {
		isPart = b;
	}
	public boolean getPart() {
		return isPart;
	}
	
	public void setFlex(boolean b) {
		isFlex = b;
	}
	public boolean getFlex() {
		return isFlex;
	}
	
	public void setRoot(boolean b) {
		isRoot = b;
	}
	public boolean getRoot() {
		return isRoot;
	}
	
	public void setGood(boolean b) {
		isGood = b;
	}
	public boolean getGood() {
		return isGood;
	}
	
	public void setNew(boolean b) {
		isNew = b;
	}
	public boolean getNew() {
		return isNew;
	}
	

}
