import java.text.DecimalFormat;

public class ColorRGB {
	//public static Double EUCLIDIANDIFF = 0.1;
	
	public static Double SEUIL 		   = 0.5;
	public static Double DIFFSEUIL     = 0.13;
	
	public static Double MINVAL 	   = 0.00000000001;
	
	public Double red;
	public Double green;
	public Double blue;
	
	protected static DecimalFormat dec = new DecimalFormat("#");
	
	public ColorRGB(Double red, Double green, Double blue) {
		this.red=red;
		this.green=green;
		this.blue=blue;
	}
	
	public ColorRGB(float [] sample) {
		red   = Double.valueOf(sample[0]);
		green = Double.valueOf(sample[1]);
		blue  = Double.valueOf(sample[2]);
	}
	
	public Double euclidian(ColorRGB other) {
		return Math.sqrt(Math.pow((this.red - other.red), 2) + Math.pow((this.green - other.green), 2) + Math.pow((this.blue - other.blue), 2));
	}
	
	//138 143 100
	
	private Double rg =0.;
	private Double rb =0.;
	private Double bg =0.;
	private Double sum=0.;
	public  Double intensity(ColorRGB other) {
		rg = Math.abs((this.red +(this.green))-(other.red +(other.green )))*1000;
		rb = Math.abs((this.red +(this.blue ))-(other.red +(other.blue  )))*1000;
		bg = Math.abs((this.blue+(this.green))-(other.blue+(other.green )))*1000;
		/*if(rg>DIFFSEUIL || rb>DIFFSEUIL || bg>DIFFSEUIL) {
			return 999999999.;
		}*/
		sum = Math.pow(rg, 3)+Math.pow(rb, 3)+Math.pow(bg, 3);
		/*if(sum>ColorRGB.SEUIL) {
			return 999999999.;
		}*/
		return sum;
	}
	
	public Double yiq(ColorRGB other) {
		int red1 = (int)(this.red*1000);
	    int red2 = (int)(other.red*1000);
	    int rmean = (red1 + red2) >> 1;
	    int r = red1 - red2;
	    int g = (int)((this.green - other.green)*1000);
	    int b = (int)((this.blue - other.blue)*1000);
	    return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
	}
	
	
	public boolean equal(ColorRGB other) {
		if(intensity(other) > ColorRGB.SEUIL) {
			return false;
		}
		return true;
	}
	
	public float[] toSample(){
		return new float[]{red.floatValue(), green.floatValue(), blue.floatValue()};
	}
	
	@Override
	public String toString() {
		return "["+ColorRGB.dec.format(this.red*1000)+";"+ColorRGB.dec.format(this.green*1000)+";"+ColorRGB.dec.format(this.blue*1000)+"]";
	}
}

class IndexedColor extends ColorRGB{
	int code;
	public IndexedColor(Double red, Double green, Double blue, int code) {
		super(red, green, blue);
		this.code=code;
	}
	public IndexedColor(float [] sample, int code) {
		super(sample);
		this.code=code;
	}
	
	public IndexedColor(ColorRGB c, int code) {
		super(c.red, c.green, c.blue);
		this.code=code;
	}
	
	@Override
	public String toString() {
		return "#"+this.code+":\n["+ColorRGB.dec.format(this.red*1000)+";"+ColorRGB.dec.format(this.green*1000)+";"+ColorRGB.dec.format(this.blue*1000)+"]";
	}
}


