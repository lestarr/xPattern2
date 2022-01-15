package modelparts;

import java.io.IOException;
import java.util.Set;

import model.WordSequences;

public abstract class Paradigm extends LangStructure{

	public Paradigm(int id, String label) {
		super(id, label);
		// TODO Auto-generated constructor stub
	}
	public Set<Object> features;
	public Set<Word> members;
	
	public abstract void train(WordSequences wsmodel);
	public abstract void tag(WordSequences wsmodel) throws IOException;

}
