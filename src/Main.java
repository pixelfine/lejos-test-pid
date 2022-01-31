import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class Main {

	public static void main(String[] args) {
		System.out.println("Left  =Prev Color");
		System.out.println("Right =Next Color");
		System.out.println("UP    =Next Code");
		System.out.println("DOWN  =Save Color");
		System.out.println("ENTER =RUN");
		System.out.println("ESCAPE=QUIT");
		ColorManager colorM = new ColorManager(new EV3ColorSensor(SensorPort.S4));
		Algorithm algo 		= new Algorithm(colorM);
		
		
		colorM.run();
		algo.onEnterPressed();
		
		
		
		Button.ESCAPE.waitForPressAndRelease();
		System.out.println("Escaped");
		Motor.A.setSpeed(0);
		Motor.B.setSpeed(0);
		Motor.A.close();
		Motor.B.close();
		colorM.stop();
        Sound.beepSequence();
	}
}