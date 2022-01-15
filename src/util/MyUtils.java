package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MyUtils {
	
	public static Writer getWriter(String outFile) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFile)), "UTF-8"));
		}catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
	
	public static BufferedReader getReader(String inFile) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFile)), "UTF-8"));
		}catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return in;
	}
	
	public static List<String> readLines(String inFile) throws IOException{
		List<String> lines = new ArrayList<>();
		String line;
		BufferedReader br = getReader(inFile);
		while((line = br.readLine())!= null ) {
			if(!line.startsWith("*"))
				lines.add(line);
		}
		br.close();
		return lines;
	}

	public static boolean isWord(String left) {
		if(left.matches(".*[\\p{L}].*")) 
			return true;
		return false;
	}


	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	public static String getSystemInput() {
		System.out.println("your input ...");
		 String words = null;
		try {
      words = in.readLine();
    }catch (Exception e) {
      System.out.println("Eingabefehler: ");
      e.printStackTrace();
    }
		return words;
	}
	
	public static PrintStream getPtrintStream(String path) throws FileNotFoundException, UnsupportedEncodingException {
		PrintStream ps = new PrintStream(new File(path), "utf8");
		return ps;

	}
	
	public static double rdouble(double d) {
    return Math.round (d * 10000.0) / 10000.0;  
	}

	public static void getTime(long start, long end) {
		long diff = end-start;
		long diffInMillis = diff/1000000;		
		System.out.println("time: " + MyUtils.rdouble(diffInMillis) + " msec");
	}
}
