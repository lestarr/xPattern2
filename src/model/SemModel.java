package model;

import java.util.HashSet;

import modelparts.Paradigm;

public class SemModel extends Paradigm {

	
	public SemModel(int id, String label) {
		super(id, label);
		features = new HashSet<>();
		members = new HashSet<>();
	}

	@Override
	public void train(WordSequences wsmodel) {
		
	}

	@Override
	public void tag(WordSequences wsmodel) {
		
	}

	

}
