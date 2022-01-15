package model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * Contains model which consists of frequency tables of: prefixes and suffixes. Prefix is every word ngram, beginning from 0, suffix - every word ngram,
 * beginning from end:
 * _cars_: prefs --> _c, _ca, _car, sufs: s_, rs_, ars_, cars_ 
 * @author halyna.galanzina
 *
 */
public class LetterTokModel implements Serializable{
	
	private static final long serialVersionUID = 1113799434508676095L;

	public Map<String, Double> prefs = null;
	public Map<String, Double> sufs = null;
	public Map<String, Double> ngrams = null;
	private String lang;

	public LetterTokModel(String lang) {
		this.lang = lang;
	}

	public String getLang() {
		return this.lang;
	}
	
	public void serializeModel(String fpath) {
		try (
	      OutputStream file = new FileOutputStream(fpath);
	      OutputStream buffer = new BufferedOutputStream(file);
	      ObjectOutput output = new ObjectOutputStream(buffer);
	    ){
	      output.writeObject(this);
	    }  
	    catch(IOException ex){
	      
	    }
	}
	
	public static LetterTokModel readModel(String fpath) {
		try(
	      InputStream file = new FileInputStream(fpath);
	      InputStream buffer = new BufferedInputStream(file);
	      ObjectInput input = new ObjectInputStream (buffer);
	    ){
	      //deserialize the List
			LetterTokModel model = (LetterTokModel)input.readObject();
	      return model;
	    }
	    catch(ClassNotFoundException ex){
	    	System.err.println("Cannot perform input. Class not found. " + ex.getMessage());
	    }
	    catch(IOException ex){
	    	System.err.println("Cannot perform input. "+ex.getMessage());
	    }
		return null;
	}

}
