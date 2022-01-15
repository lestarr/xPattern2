package lucene;

public class SearchResult {
	
	public double colValueLeft = -1.0;
	public double colValueRight = -1.0;
	public int freq;
	
	public SearchResult(int freq) {
		this.freq = freq;
	}
	
	public SearchResult(int freq, double colLeft, double colRight) {
		this.freq = freq;
		this.colValueLeft = colLeft;
		this.colValueRight = colRight;
	}
			
}
