
import java.util.ArrayList;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;
public class ColorManager {
	EV3ColorSensor color; 
	SensorMode sensor;
	
	int currCode  = 0;
	int currIndex = 0;
	public ArrayList<IndexedColor> colorList = new ArrayList<IndexedColor>();
	
	float sample[];
	
	int code = -1;
	
	ColorManager(EV3ColorSensor colorSensor){
		this.color=colorSensor;
		this.sensor = color.getRGBMode();
		this.sample = new float[3];
		this.color.setFloodlight(true);
	}
	
	
	public void run() {
		
		Button.LEFT.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				prevColor();
				System.out.println(stateString());
			}
			@Override public void keyReleased(Key k) {}
		});
		
		Button.RIGHT.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				nextColor();
				System.out.println(stateString());
			}
			@Override public void keyReleased(Key k) {}
		});
		
		Button.UP.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				currCode++;
				System.out.println(stateString());
			}
			@Override public void keyReleased(Key k) {}
		});
		
		Button.DOWN.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				fetchSample();
				IndexedColor color = new IndexedColor(sample, currCode);
				insertColor(color);
				System.out.println(color.toString()+"\n"
						+ "Successfuly saved\n"
						+ "---------------\n");
			}
			@Override public void keyReleased(Key k) {}
		});
		/*
		Button.ENTER.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				fetchSample();
				if(isRecognized(new ColorRGB(sample))) {
					System.out.println("Recognized");
				}else {
					System.out.println("Not Recognized");
				}
			}
			@Override public void keyReleased(Key k) {}
		});
		*/
	}
	
	void runThread() {
		Thread t = new Thread() {
			public void run() {
				color.setFloodlight(true);
				while(true) {
					code = isRecognizedFetch();
					Delay.msDelay(Algorithm.DELAY);
				}
			}
		};
		t.start();
	}
	
	public void fetchSample() {
		sample[0] = 0;
		sample[1] = 0;
		sample[2] = 0;
		sensor.fetchSample(sample, 0);
	}
	
	public void nextColor() {
		if(currIndex<colorList.size()) currIndex++;
		if((!colorList.isEmpty()) && (currIndex!=colorList.size())) {
			currCode = colorList.get(currIndex).code;
		}
	}
	
	public void prevColor() {
		if(currIndex>0) currIndex--;
		if((!colorList.isEmpty()) && (currIndex!=colorList.size())) {
			currCode = colorList.get(currIndex).code;
		}
	}
	
	public void insertColor(IndexedColor c) {
		if(colorList.size()==currIndex) {
			colorList.add(c);
		}else {
			colorList.set(currIndex, c);
		}
	}
	
	public String stateString() {
		String res = "---------------\n"
				+ "[#code:"+currCode+"\n"
				+ "Index:"+currIndex+",\n"
				+ "Data:";
		if(currIndex>=colorList.size() || colorList.isEmpty()) {
			res+="empty";
		}else {
			res+=colorList.get(currIndex).toString();
		}
		return res+="]\n---------------\n";
	}
	
	
	private ColorRGB pcolor;       //Couleur du capteur
	private IndexedColor ecolor;   //Couleur de liste
	
	public int isRecognized(ColorRGB c) {
		Double eltIntensity=0.;
		Double codeDiff=0.;
		int colorCode 	= -1;
		Double minDiff	=999999999.;
		
		for(IndexedColor elt : colorList) {
			eltIntensity = c.yiq(c);
			if(eltIntensity<999999999) {
				codeDiff = Math.abs(c.yiq(elt) - eltIntensity);
				if(codeDiff<minDiff) {
					minDiff   = codeDiff;
					colorCode = elt.code;
					
					ecolor = elt;
				}
			}
		}pcolor=c;
		return colorCode;
	}
	
	public double colorDiff() {
		return pcolor.yiq(ecolor);
	}
	
	
	public int isRecognizedFetch() {
		fetchSample();
		return isRecognized(new ColorRGB(sample));
	}
	
	public void stop() {
		color.close();
	}
}
