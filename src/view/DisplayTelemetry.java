package view;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.TimeZone;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Helper for manage and display all the telemetry
 * @author jmwx
 *
 */
public class DisplayTelemetry {
	
	/**
	 * Set the background for the telemetry section and pipeline the telemetry from the Hashtable 
	 * to different sub-displayers
	 * @param graphic
	 * @param telemetrySet
	 */
	public static void displayTelemetry(GraphicsContext graphic, String elapsedTime, Hashtable<String, String> telemetrySet, Hashtable<String, String> TLESet){

		//The back ground
	    graphic.setFill(Color.rgb(15,15,15));
	    graphic.fillRect(0, 360, 720, 360);

		displayFormat(graphic);
	    displayStatusAndTimes(graphic, telemetrySet.get("status"), elapsedTime, telemetrySet.get("updateTime"));
	    displayTLE(graphic, TLESet.get("updateTime"), TLESet.get("lineOne"), TLESet.get("lineTwo"));
	    displayISSLocation(graphic, telemetrySet.get("lat"), telemetrySet.get("lon"), telemetrySet.get("sl_lat"), telemetrySet.get("sl_lon"));
	    displayFlightData(graphic, telemetrySet.get("H"), telemetrySet.get("V"), telemetrySet.get("dH"), telemetrySet.get("dV"), telemetrySet.get("LT"));
	}
	
	/**
	 * Set up the and display line that formatting the display area
	 * @param graphic
	 */
	private static void displayFormat(GraphicsContext graphic){
		 
		//The line between map and telemetry
		graphic.setFill(Color.rgb(255, 255, 225));
		graphic.fillRect(0, 360, 720, 5);
		
		//The line between telemetry and TLE
		graphic.setFill(Color.rgb(255, 255, 235));
		graphic.fillRect(500, 380, 1, 205);
		
		//The line divide telemetry onto two sections
		graphic.fillRect(360, 420, 1, 160);
		
	}
	
	/**
	 * Set up the and display the status and all times
	 * @param graphic
	 * @param status
	 * @param elapsedTime
	 * @param updateTime
	 */
	private static void displayStatusAndTimes(GraphicsContext graphic, String status, String elapsedTime, String updateTime){
		 
		//Get UTC time
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String UTC = formatter.format(new Timestamp(System.currentTimeMillis()));
		
		//The writing
		graphic.setTextAlign(TextAlignment.LEFT);
		graphic.setFill(Color.rgb(255, 255, 235));
		Font bigFont = Font.font("Times New Roman", FontWeight.THIN, 10.0);
		graphic.setFont(bigFont);
		graphic.fillText(UTC + " UTC", 10, 380);
		graphic.fillText("STOO: " + elapsedTime, 383, 380);
		graphic.fillText("Status: " + status, 10, 395);
		graphic.fillText("Last Update: " + updateTime, 318, 395);
	}
	
	/**
	 * Set up the and display TLE
	 * @param graphic
	 * @param updateTime
	 * @param lineOne
	 * @param lineTwo
	 */
	private static void displayTLE(GraphicsContext graphic, String updateTime, 
			String lineOne, String lineTwo){

		//The writing
		graphic.setFill(Color.rgb(255, 255, 235));
		Font bigFont = Font.font("Times New Roman", FontWeight.BOLD, 12.0);
		graphic.setFont(bigFont);
		graphic.fillText("TLE", 592, 400);
		bigFont = Font.font("Times New Roman", FontWeight.THIN, 10.0);
		graphic.setFont(bigFont);
		graphic.fillText("UPDATE TIME   " + updateTime, 510, 430);
		graphic.fillText("LINE 1   ", 510, 460);
		graphic.fillText("LINE 2   ", 510, 520);
		
		if ((lineOne != "-") && (lineTwo != "-")){
			//Split string to char array, so that it can be display in mutiple line
			char[] lineOneArray = lineOne.toCharArray();
			char[] lineTwoArray = lineTwo.toCharArray();
			
			//Break each line into 3 section
			String one_one = new String(lineOneArray, 0, 18);
			String one_two = new String(lineOneArray, 18, 26);
			String one_three = new String(lineOneArray, 44, 25);
			String two_one = new String(lineTwoArray, 0, 26);
			String two_two = new String(lineTwoArray, 26, 26);
			String two_three = new String(lineTwoArray, 52, 17);
			
			graphic.fillText(one_one, 510, 470);
			graphic.fillText(one_two, 510, 480);
			graphic.fillText(one_three, 510, 490);
			graphic.fillText(two_one, 510, 530);
			graphic.fillText(two_two, 510, 540);
			graphic.fillText(two_three, 510, 550);
		}
	}
	
	/**
	 * Display the location table
	 * @param graphic
	 * @param lat	geo-latitude
	 * @param lon 	geo-longitude
	 * @param sl_lat	solar-latitude
	 * @param sl_lon	solar-longitude
	 */
	private static void displayISSLocation(GraphicsContext graphic, String lat, 
			String lon, String sl_lat, String sl_lon){
		
		graphic.setFill(Color.rgb(255, 255, 235));
		Font bigFont = Font.font("Times New Roman", FontWeight.THIN, 10.0);
		graphic.setFont(bigFont);
		
		//Table for location
		/**
		 * ----------LOCATION--------
		 * 		|					|
		 * GEO	|LAT	12345678	|
		 * 		|LON 	12345678	|
		 * --------------------------
		 * 		|					|
		 * SL	|LAT	12345678	|
		 * 		|LON	12345678	|
		 * --------------------------
		 */
		graphic.fillRect(15, 430, 60, 1);
		graphic.fillRect(135, 430, 60, 1);
		graphic.fillRect(65, 435, 1, 80);
		graphic.fillRect(195, 430, 1, 90);
		graphic.fillRect(15, 475, 175, 1);
		graphic.fillRect(15, 520, 181, 1);
		
		
		//The Writing
		graphic.setTextAlign(TextAlignment.LEFT);
		graphic.fillText("LOCATION", 80, 435);
		graphic.fillText("GEO", 25, 460);
		graphic.fillText("SL", 30, 500);
		graphic.fillText("LAT", 70, 450);
		graphic.fillText("LON", 70, 465);
		graphic.fillText("LAT", 70, 490);
		graphic.fillText("LON", 70, 505);
		
		graphic.setTextAlign(TextAlignment.RIGHT);
		graphic.fillText(lat, 190, 450);
		graphic.fillText(lon, 190, 465);
		graphic.fillText(sl_lat, 190, 490);
		graphic.fillText(sl_lon, 190, 505);
		
	}
	
	/**
	 * Display other flight data
	 * @param graphic
	 * @param H		Altitude
	 * @param V		Velocity
	 * @param dH	Delta H
	 * @param dV	Delta V
	 * @param LT	The nature lighting in ISS
	 */
	private static void displayFlightData(GraphicsContext graphic, String H, 
			String V, String dH, String dV, String LT){
		
		graphic.setFill(Color.rgb(255, 255, 235));
		Font bigFont = Font.font("Times New Roman", FontWeight.THIN, 10.0);
		graphic.setFont(bigFont);

		graphic.setTextAlign(TextAlignment.LEFT);
		graphic.fillText("ALT", 240, 435);
		graphic.fillText("V  ", 240, 450);
		graphic.fillText("dH", 240, 465);
		graphic.fillText("dV ", 240, 480);
		graphic.fillText("LT ", 240, 495);
		
		graphic.setTextAlign(TextAlignment.RIGHT);
		graphic.fillText(H, 345, 435);
		graphic.fillText(V, 345, 450);
		graphic.fillText(dH, 345, 465);
		graphic.fillText(dV, 345, 480);
		graphic.fillText(LT, 345, 495);
	}
}
