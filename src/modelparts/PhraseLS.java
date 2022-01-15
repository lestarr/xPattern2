package modelparts;

import java.util.Objects;

public class PhraseLS extends LangStructure {
	private LangStructure left;
	private LangStructure right;
	public PhraseLS(LangStructure left, LangStructure right) {
		super(0, left.toString()+" "+right.toString());
		this.left = left;
		this.right = right;
	}
	
	public int level = 0;
	public boolean isCollocation = false;
	
	@Override
	public boolean equals(Object other) {
		if(!other.getClass().equals(this.getClass())) return false;
		return this.label.equals( ((Word)other).label);
	}
	
    @Override
    public int hashCode() {
        return Objects.hashCode(this.label);
    }
    
	public String toString() {
			return this.label;
	}

}
