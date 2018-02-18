package model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that managing all telemetry
 * @author jmwx
 *
 */
public class ISSTelemetryReceiver {
	
	/**
	 * All constant
	 */
	//Time stamp for launched time of Zarya is 911542800
	//Which is is 06:20:00 of 20th November 1998 UTC 
	private static final Long ZaryaLaunchedTime = 911542800L;
	//URL that return ISS location only
	private static final String locationChannel = "http://api.open-notify.org/iss-now.json";
	//URL that return ISS location as well as some flight data
	private static final String flightDataChannel = "https://api.wheretheiss.at/v1/satellites/25544";
	
	
	
	/**
	 * Json classes:
	 * locationChannel:	timestamp:	int
	 * 					message:	string
	 * 					iss_position:	latitude:	string
	 * 									longitude:	string
	 * flightDataChannel:	timestamp:	int
	 * 						latitude:	double
	 * 						longitude:	double
	 * 						altitude:	double
	 * 						velocity:	double
	 * 						visibility:	string
	 * 						solar_lat:	double
	 * 						solar_lon:	double
	 */
	
	
	/**
	 * All telemetry, use string for them instead of int, float Long etc, so that it can be easily display
	 */
	private Timestamp elapsedTime;	//The elapsed time since first module(Zarya) launched into the orbit
	private TelemetryStatus status;	//Indicate if the status of the current telemetry
	private Timestamp updateTime;	//The telemetry's time, in UTC
	private String latitude;	//In decimal degree in reference to the Earth
	private String longitude;	//In decimal degree in reference to the Earth
	private String solar_latitude;	//In decimal degree in reference to the Sun
	private String solar_longitude;	//In decimal degree in reference to the Sun
	private String altitude;	//Kilometres above sea level (m)
	private String velocity;	//In kilometre per second (km/s)
	private String delta_V;	//In kilometre per second per second (m/s/s) 
	private String delta_H;	//In kilometre per second per second (m/s/s)
	private String lighting;	//The nature lighting in ISS
	
	
	
	/**
	 * Initial state(No telemetry at all)
	 */
	public ISSTelemetryReceiver() {
		
		updateElapsedTime();
		status = TelemetryStatus.LostConnection;
		updateTime = new Timestamp(System.currentTimeMillis());
		latitude = "-";
		longitude = "-";
		solar_latitude = "-";
		solar_longitude = "-";
		altitude = "-";
		velocity = "-";
		delta_V = "-";
		delta_H = "-";
		lighting = "-";
		//startElapsedTimer();
		//startTelemetryTimer();
	}

		

	/**
	 * The telemetry displayer request telemetry from the telemetry receiver
	 * @return a list of telemetry
	 */
	public Hashtable<String, String> getTelemetry(){
		
		Hashtable<String, String> telemetrySet = new Hashtable<String, String>();
		
		//Add all telemetry to the telemetry set
		telemetrySet.put("status", status.toString());
		telemetrySet.put("updateTime", timestampToUTCTime(updateTime));
		telemetrySet.put("lat", latitude);
		telemetrySet.put("lon", longitude);
		telemetrySet.put("sl_lat", solar_latitude);
		telemetrySet.put("sl_lon", solar_longitude);
		telemetrySet.put("H", altitude);
		telemetrySet.put("V", velocity);
		telemetrySet.put("dH", delta_H);
		telemetrySet.put("dV", delta_V);
		telemetrySet.put("LT", lighting);
		
		return telemetrySet;
	}
	
	
	
	/**
	 * Getter for mission elapsed time
	 * @return elapsedTime in the format of ddddhhmmss
	 */
	public String getElapsedTime(){
		return elapsedTimeToString();
	}
	
	
	
	/**
	 * Use to channel to get position channel, the location channel is the primary
	 * source for the ISS position, if the location channel is fail, use the flight data channel 
	 */
	public void updateTelemetry(){
		
		//For calucate delta
		Timestamp lastUpdateTime = updateTime;	
		String lastAltitude = altitude;
		String lastVelocity = velocity;
		
		JSONObject locationTelemetry = new JSONObject();
		JSONObject flightDataTelemetry = new JSONObject();
		
		//Get data from location channel and flight data channel
		try {
			locationTelemetry = requestTelemetryFromLocationChannel();
			flightDataTelemetry = requestTelemetryFromFlightDataChannel();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		//Identify the telemetry status for the current update
		try{
			if ((locationTelemetry.get("message").equals("success")) 
					&& (flightDataTelemetry.get("message").equals("success"))){
				status = TelemetryStatus.Connected;
			} else if ((locationTelemetry.get("message").equals("lostConnection")) 
				&& (flightDataTelemetry.get("message").equals("success"))){
				status = TelemetryStatus.LostLocationSource;
			}else if ((locationTelemetry.get("message").equals("success")) 
				&& (flightDataTelemetry.get("message").equals("lostConnection"))){
				status = TelemetryStatus.LostFlightDataSource;
			}else {
				status = TelemetryStatus.LostConnection;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		//Identify which telemetry source to use for the location.
		//Depend on telemetry status, get the location of ISS and update time from different telemetry source
		if(status.equals(TelemetryStatus.LostLocationSource)){	//Use flight data channel as location source
			try{
				Long readTimeStampAsLong = flightDataTelemetry.getLong("timestamp");
				updateTime = new Timestamp(readTimeStampAsLong.longValue() * 1000);
				latitude = flightDataTelemetry.get("latitude").toString();
				longitude = flightDataTelemetry.get("longitude").toString();	
			} catch (Exception e){
				//e.printStackTrace();
			}
			
		}else if (status.equals(TelemetryStatus.LostConnection)){	//Lost all telemetry, no latitude and longitude
			updateTime = new Timestamp(System.currentTimeMillis());
			latitude = "-";
			longitude = "-";
			
		} else {	//Location telemetry sources are nominal
			
			try{
				Long readTimeStampAsLong = locationTelemetry.getLong("timestamp");
				updateTime = new Timestamp(readTimeStampAsLong.longValue() * 1000);
				JSONObject location = locationTelemetry.getJSONObject("iss_position");
				latitude = location.get("latitude").toString();
				longitude = location.get("longitude").toString();	
			} catch (Exception e){
				//e.printStackTrace();
			}
		}
		
		//Check for flight data, only read flight data when the flight data source is nominal
		if((status.equals(TelemetryStatus.LostLocationSource)) || (status.equals(TelemetryStatus.Connected))){	//Flight data channel is nominal
			try{
				solar_latitude = flightDataTelemetry.get("solar_lat").toString();
				solar_longitude = flightDataTelemetry.get("solar_lon").toString();
				altitude = flightDataTelemetry.get("altitude").toString();
				velocity = flightDataTelemetry.get("velocity").toString();
				lighting = flightDataTelemetry.get("visibility").toString();
			} catch (Exception e){
				//e.printStackTrace();
			}
			
		} else {	//Flight data channel is lost
			solar_latitude = "-";
			solar_longitude = "-";
			altitude = "-";
			velocity = "-";
			lighting = "-"; 
		}
		
		//Only calculate the delta only when the flight data source is not lost
		if ((!(status.equals(TelemetryStatus.LostFlightDataSource))) 
				&& (!(status.equals(TelemetryStatus.LostConnection)))){
			
			//Calculate delta_H, only when the last telemetry update receive altitude data correctly
			if (!(lastAltitude.equals("-"))){
				//Calcute dH and dt
				double dH = (Double.parseDouble(altitude) * 1000) - Double.parseDouble(lastAltitude);
				double dt = ((new Long(updateTime.getTime() - lastUpdateTime.getTime())).doubleValue()) / 1000;
				delta_H = Double.toString(dH/dt);
			}
		
			//Calculate delta_V, only when the last telemetry update receive velocity data correctly
			if (!(lastVelocity.equals("-"))){
				//Calcute dV and dt
				double dV = (Double.parseDouble(velocity) / 3.6) - (Double.parseDouble(lastVelocity) * 1000);
				double dt = ((new Long(updateTime.getTime() - lastUpdateTime.getTime())).doubleValue()) / 1000;
				delta_V = Double.toString(dV/dt);
			}
			
		//The case that unable update delta
		//(no current flight telemetry or no old delta for calculate the new delta)
		} else {	
			delta_H = "-";
			delta_V = "-";
		}
		
		//Tidy telemetry, keep them in the same number of decimal place, and change the unit from km and h to m and s
		tidyUpTelemetry();
		
		//Store the new telemetry to the data base, so that it can be used for analysic later
		TLEandTelemetryDAO.addNewTelemetry(getTelemetry(), updateTime, getElapsedTime());
		
	}
	
	
	
	/**
	 * Request the location data only
	 * @return a Json object about the time, the status and location
	 */
	private JSONObject requestTelemetryFromLocationChannel(){
		
		JSONObject dataInJson = new JSONObject();
		
		try {
			//Connect to to the API
			URL url = new URL(locationChannel);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
			connection.setConnectTimeout(2000);
		    connection.setReadTimeout(2000);
			
			//When the response code is 200, that mean connection is successful, read the json data.
			//If the response code is not 200, return "lostConnection" as the "message" field
			if(connection.getResponseCode() == 200){
	            //Open the stream and put it into BufferedReader, and change it to a Json object
	            BufferedReader rawData = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            dataInJson = new JSONObject(rawData.readLine());
	            rawData.close();
			} else {
				dataInJson.put("message", "lostConnection");
			}
     
		} catch (Exception e) {
			try{
				dataInJson.put("message", "lostConnection");
			} catch(Exception e1){
				//e1.printStackTrace();
			}
			//e.printStackTrace();
		}
		return dataInJson;
	}
	
	
	
	/**
	 * Request flight data
	 * @return a Json object about the the location and the flight data of the ISS
	 */
	private JSONObject requestTelemetryFromFlightDataChannel(){
		
		JSONObject dataInJson = new JSONObject();
		
		try {
			//Connect to to the API
			URL url = new URL(flightDataChannel);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(2000);
		    connection.setReadTimeout(2000);

			//When the response code is 200, that mean connection is successful, read the json data.
			//If the response code is not 200, return "lostConnection" as the "message" field
			//Otherwise a "success" in "message"
			if(connection.getResponseCode() == 200){
	            //Open the stream and put it into BufferedReader, and change it to a Json object
	            BufferedReader rawData = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            dataInJson = new JSONObject(rawData.readLine());
	            dataInJson.put("message", "success");
	            rawData.close();
			} else {
				dataInJson.put("message", "lostConnection");
			}
     
		} catch (Exception e) {
			try{
				dataInJson.put("message", "lostConnection");
			} catch(Exception e1){
				//e1.printStackTrace();
			}
			//e.printStackTrace();
		}
		return dataInJson;
	}
	
	
	
	/**
	 * A timer for keep calculate the time since the first module launched into the orbit (once every second)
	 */
	private void startElapsedTimer(){
		Thread elapsedTimer = new Thread(){
			@Override
			public void run(){
				while(true){
					try {
						//Calculate elapsed time once per second
						//System.out.println("Updating elapsed time...");
						updateElapsedTime();
						Thread.sleep(1000);	
					} catch (Exception e) {
						e.printStackTrace();
					} 
			    }
			}
		};
		elapsedTimer.start();
	}
	
	
	
	/**
	 * A timer for keep communicating with ISS for telemetry (once every three seconds)
	 */
	private void startTelemetryTimer(){
		Thread telemetryTimer = new Thread(){
			@Override
			public void run(){
				while(true){
					try {
						//Request telemetry from ISS once per three seconds
						System.out.println("Updating telemetry...");
						updateTelemetry();
						Thread.sleep(3000);	
					} catch (Exception e) {
						e.printStackTrace();
					} 
			    }
			}
		};
		telemetryTimer.start();
	}
	
	
	
	/**
	 * Helper for convert a Timestamp object to a UTC time string
	 * @param timestamp 
	 * @return The UTC date and time in a string format
	 */
	private String timestampToUTCTime(Timestamp timestamp){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return formatter.format(timestamp);
	}
	
	
	
	/**
	 * Helper for calculate new mission elapsed time
	 */
	public void updateElapsedTime(){
		//Get current time
		Long currentTimeStamp = System.currentTimeMillis() / 1000L;
		//Calculate the new mission elapsed time
		elapsedTime = new Timestamp(currentTimeStamp - ZaryaLaunchedTime);
	}
	
	
	
	/**
	 * Change the time stamp for mission elapsed time to dddd:hh:mm:ss format
	 * @return String of the elapsed timein dddd:hh:mm:ss format
	 */
	private String elapsedTimeToString(){
		
		//Change timesampt to Long
		Long elapsedTimeInSecond = new Long(elapsedTime.getTime());
		
		//Find out how many days, hours, minutes and seconds
		Long days = elapsedTimeInSecond / (3600 * 24);
		Long hours = (elapsedTimeInSecond - (days * 24 * 3600)) / 3600;
		Long minutes = ((elapsedTimeInSecond - (days * 24 * 3600)) - (hours * 3600)) / 60;
		Long seconds = (elapsedTimeInSecond - (days * 24 * 3600)) - (hours * 3600) - (minutes * 60);
		//Put the days, hours minutes and seconds in a "dddd:hh:mm:ss" string
		String hoursSection = ":" + hours.toString();
		String minutesSection = ":" + minutes.toString();
		String secondsSection = ":" + seconds.toString();
		if (hours < 10){
			hoursSection = ":0" + hours.toString();
		}
		if (minutes < 10){
			minutesSection = ":0" + minutes.toString();
		}
		if (seconds < 10){
			secondsSection = ":0" + seconds.toString();
		}
		
		return days + hoursSection + minutesSection + secondsSection;
	}
	
	
	/**
	 * Tidy up telemetry, so that:
	 * - telemetry alway has 4 decimal place
	 * - all distance are using "kilometre" as unit
	 * - all time are using "second" as unit
	 * therefore the monitor can display the telemetry tidily
	 */
	private void tidyUpTelemetry(){
		
		DecimalFormat fourDecimalPlace = new DecimalFormat("#.0000"); 
		try{
			//Ensure location data has 4 decimal place only
			latitude = fourDecimalPlace.format(Double.parseDouble(latitude));
			longitude = fourDecimalPlace.format(Double.parseDouble(longitude));
			solar_latitude = fourDecimalPlace.format(Double.parseDouble(solar_latitude));
			solar_longitude = fourDecimalPlace.format(Double.parseDouble(solar_longitude));
			
			//Change altitude's unit from km to m
			altitude = String.valueOf(Math.round(Double.parseDouble(altitude) * 1000));
			
			//Change velocity's unit from km/h to km/s
			velocity = fourDecimalPlace.format(Double.parseDouble(velocity) / 3600);
			
			//Ensure the delta values has 4 decimal place only
			delta_H = fourDecimalPlace.format(Double.parseDouble(delta_H));
			delta_V = fourDecimalPlace.format(Double.parseDouble(delta_V));
		} catch (Exception e){
			//e.printStackTrace();
		}
	}
}


