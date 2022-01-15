package modelutils;

import java.util.List;

import util.MyUtils;

public class Vector {
	
		private Double[] values;
		
		public Vector(Double[] values) {
			this.values = values;
		}
		
		public Vector (List<Double> list) {
			this.values  = list.toArray(new Double[list.size()]);
		}
		
		public int size() {
			return values.length;
		}
		
		public Double[] values() {
			return this.values;
		}
		
		public double get(int i) {
			if(i >= size() || i < 0) throw new NullPointerException();
			return values[i];
		}
		
		public void set(int i, double value) {
			if(i >= size() || i < 0) throw new NullPointerException();
			values[i] = value;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(double d: values)
				sb.append(MyUtils.rdouble(d)+", ");
			return sb.toString();
		}

		public double vectorSum() {
			double sum = 0.0;
			for(double d: this.values) {
				sum = sum + d;
			}
			return sum;
		}

}
