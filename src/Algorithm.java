import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.Sounds;
import lejos.hardware.motor.Motor;
import lejos.utility.Delay;

public class Algorithm {
	
	public static long DELAY 			= 20;
	public static int  MAXRECORD        = 50;
	public static int  SPEEDA		    = 150;
	public static int  SPEEDB			= 150;
	public static int  MAXSPEED			= 700;
	public static int  MINSPEED			= 20;
	
	public static double kp		= 3.2;
	public static double kd		= 2.50;
	public static double ki		= 0.08;
	
	public double quotientAB = 1; 
	
	public int time=0;
	public long latestFetch;
	Record lastRecord = new Record(-1, 0, 0, 0);
	ColorManager cm;
	boolean isRunning=false;
	
	public Algorithm(ColorManager colorManager) {
		this.cm=colorManager;
	}
	
	public void onEnterPressed() {
		Button.ENTER.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				if(!isRunning) {
					isRunning=true;
					run();
				}
			}
			@Override public void keyReleased(Key k) {}
		});
	}
	
	
	/************************ALGORITHME********************************************/
	
	
	
	public void run() {
		Motor.A.setSpeed(SPEEDA);
		Motor.A.forward();
		
		Motor.B.setSpeed(SPEEDB);
		Motor.B.forward();
		//cm.runThread();
		
		double i=0;
		double d=0;
		double lasterror=0;
		double error = 0;
		double turn=0;
		
		int code=-1;
		double ratio = 0.0;
		
		boolean croisement=false;
		int lastGround=0;
		int lastLine  =0;
		int nk=1;
		while(true) {
			//Delay.msDelay(Algorithm.DELAY); 
			code = cm.isRecognizedFetch();
			/*if(code>0) {
				lastLine++;
				if(lastGround>30) {
					Sound.buzz();
					nk=6;
				}
				lastGround=0;
			}else {
				lastGround++;
				if(lastLine>30) {
					Sound.buzz();
					nk=6;
				}
				lastLine=0;
			}*/
			if(code == 2) {
				Motor.A.setSpeed(350);
				Motor.B.setSpeed(350);
				i=0; d=0; lasterror=0; error=0; turn=0;
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
				croisement=true;
			}else if(croisement==true) {
				Motor.A.setSpeed(SPEEDA);
				Motor.B.setSpeed(SPEEDB);
				croisement=false;
			}else if(code == 3) {
				Motor.A.setSpeed(1);
				Motor.B.setSpeed(1);
				i=0; d=0; lasterror=0; error=0; turn=0;
				Sound.buzz();
				return;
			}else {
				error= getError(code);
				//System.out.println("E|"+getError(code));
				i	 = i+error;
				d	 = error - lasterror;
				
				turn = kp * error + ki * i + kd * d;
				//if(nk>1) nk--;
				//System.out.println(code+"  "+turn);
				
				if		(SPEEDA+(int)turn<1) 		Motor.A.setSpeed(10);
				else if	(SPEEDA+(int)turn>MAXSPEED) Motor.A.setSpeed(MAXSPEED);
				else 								Motor.A.setSpeed(SPEEDA+(int)turn);
				
				if		(SPEEDB-(int)turn<1) 		Motor.B.setSpeed(10);
				else if	(SPEEDB+(int)turn>MAXSPEED) Motor.B.setSpeed(MAXSPEED);
				else 								Motor.B.setSpeed(SPEEDB-(int)turn);
				
				ratio = Motor.A.getSpeed()/Motor.B.getSpeed();
				lasterror = error;
				
				if(code >0 && ( ratio > 700/50)) {
					Sound.beep();
					Motor.A.setSpeed(50);
					Motor.B.setSpeed(500);
					i=0; d=0; lasterror=0; error=0; turn=0;
					Delay.msDelay(30*Algorithm.DELAY);
					Sound.buzz();
				}
				Delay.msDelay(Algorithm.DELAY);
			}
			
		}
	}
	
	
	
	public double getError(int code) {
		return (code>0) ? -30:25;
	}
	
	/*
	public double getError(int code) {
		double cdiff= cm.colorDiff();
		if(cdiff==0) cdiff=0.01;
		double diff = 40/cm.colorDiff();
		return (code>0) ? -(20+(2*diff)):15+(2*diff);
	}
	
	public double getTimeError(int code, float ground, float line) {
		if(code>0) {
			return ((System.currentTimeMillis()-ground)/200)*-5;
		}else {
			return ((System.currentTimeMillis()-line)/200)*2;
		}
	}
	*/
	
	
	public void addLst(LinkedList<Integer> lst, int value, int max) {
		if(lst.size()==max) {
			lst.remove(0);
		}
		lst.add(value);
	}
	
	
	
	/*
	
	
	public void run() {
		Motor.A.setSpeed(150);
		Motor.A.forward();
		
		Motor.B.setSpeed(10);
		Motor.B.forward();
		//cm.runThread();
		
		int code=-1;
		long lostTime=System.currentTimeMillis();
		long diffTime=System.currentTimeMillis();
		long lineTime=System.currentTimeMillis();
		lastRecord.time = System.currentTimeMillis();
		while(true) {
			//Delay.msDelay(Algorithm.DELAY); 
			time++;
			code = cm.isRecognizedFetch();
			if(code!= -1) {
				lostTime=System.currentTimeMillis();
				lastRecord.set(code, Motor.A.getSpeed(), Motor.B.getSpeed(), System.currentTimeMillis());
				if(lineTime<300) {
					Motor.A.setSpeed(200);
					Motor.B.setSpeed(400);
				}else {
					Motor.A.setSpeed(MINSPEED);
					Motor.B.setSpeed(MAXSPEED);
				}
			}else {
				diffTime = System.currentTimeMillis()-lastRecord.time;
				lineTime = System.currentTimeMillis();
				if(diffTime<300) {
					Motor.A.setSpeed(400);
					Motor.B.setSpeed(200);
				}else if(diffTime<2500) {
					Motor.A.setSpeed(MAXSPEED);
					Motor.B.setSpeed(MINSPEED);
				}else if(diffTime<2550){
					Sound.beep();
				}else {
					//Augmente tous les 0.1s soit 6 fois par secondes.
					Motor.A.setSpeed(limit(
							(int)((System.currentTimeMillis()-lostTime)/100)    
					));
					Motor.B.setSpeed(MAXSPEED);
				}
			}
		}
	}
	
	*/
	
	
	public void linearSearch() {
		Motor.A.setSpeed(10);
		Motor.B.setSpeed(10);
		int code =-1;
		long diffTime=System.currentTimeMillis();
		int direction=1;
		while(true) {
			code = cm.isRecognizedFetch();
			if(code!=-1) {
				lastRecord.set(code, Motor.A.getSpeed(), Motor.B.getSpeed(), System.currentTimeMillis());
				Motor.A.forward();
				Motor.B.forward();
				setSpeedLimited(300, 300, direction, 10);
			}else {
				diffTime = System.currentTimeMillis()-lastRecord.time;
				if(direction==-1) {
					if(diffTime<500) {
						direction=1;
						Motor.A.setSpeed(100);
						Motor.B.setSpeed(100);
						Motor.A.forward();
						Motor.B.backward();
					}else {
						direction=-1;
						Motor.A.setSpeed(100);
						Motor.B.setSpeed(100);
						Motor.A.backward();
						Motor.B.forward();
					}
				}else {
					if(diffTime<500) {
						direction=-1;
						Motor.A.setSpeed(100);
						Motor.B.setSpeed(100);
						Motor.A.backward();
						Motor.B.forward();
					}else {
						direction=1;
						Motor.A.setSpeed(100);
						Motor.B.setSpeed(100);
						Motor.A.forward();
						Motor.B.backward();
					}
				}
			}
		}
	}
	
	/*
	boolean rotateUntil(int direction, long time) {
		if(direction>0) {
			Motor.A.setSpeed( 100);
			Motor.B.setSpeed(-100);
		}else {
			Motor.A.setSpeed(-100);
			Motor.B.setSpeed( 100);
		}
		int code = cm.isRecognizedFetch();
		if(code!=-1) {
			while(true) {
				code = cm.isRecognizedFetch();
				if(code!=-1) {
					
				}else {
					
				}
			}
		}else {
			
		}
	}
	*/
	/************************ALGORITHME********************************************/
	
	//      -1<-     0      ->1
	void setSpeedLimited(int speedA, int speedB, int direction, int accel) {
		if(direction==0) {
			Motor.A.setSpeed(limit(speedA+accel));
			Motor.B.setSpeed(limit(speedB+accel));
		}else if(direction>0) {
			Motor.A.setSpeed(limit(speedA+accel));
			Motor.B.setSpeed(limit(speedB-(accel/2)));
		}else {
			Motor.A.setSpeed(limit(speedA-(accel/2)));
			Motor.B.setSpeed(limit(speedB+accel));
		}
	}
	
	int limit(int speed) {
		if(speed>MAXSPEED) return MAXSPEED;
		else if(speed<MINSPEED) return MINSPEED;
		else return speed;
	}
	
	int rotateSearch(int speedA, int speedB) {
		Motor.A.setSpeed(speedA);
		Motor.B.setSpeed(speedB);
		int code = cm.code;
		while(code!=-1) {
			time++;
			code = cm.code;
			if(code!= -1) 	{
				lastRecord.set(code, Motor.A.getSpeed(), Motor.B.getSpeed(), time);
				return code;
			}
			Motor.B.setSpeed(Motor.B.getSpeed()+50);
		}
		return -1;
	}
}


class Record{
	int code;
	int speedA;
	int speedB;
	int angle;
	long time;
	Record(int code, int speedA, int speedB, long time){
		this.code=code; 
		this.speedA=speedA;
		this.speedB=speedB;
		this.time=time;
	}
	void set(int code, int speedA, int speedB, long time){
		this.code=code; 
		this.speedA=speedA;
		this.speedB=speedB;
		this.time=time;
	}
}
