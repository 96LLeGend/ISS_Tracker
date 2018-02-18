package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

/**
 * Class that monitor the orbit of the ISS, via the Two-line element set
 * @author jmwx
 *
 */

public class TLEMonitor {

	//URL that return the ISS's current TLE
	private static final String TLEChannel = "https://api.wheretheiss.at/v1/satellites/25544/tles";
	
	private String lineOne;	//First line of the TLE
	private String lineTwo;	//Second line of the TLE
	private Timestamp updateTime;	//The current TLE's publish time, in UTC
	private Long period; //The time it take for ISS orbit 1 orbit.
	
	/**
	 * Initial state(No TLE at all)
	 */
	public TLEMonitor() {
		
		lineOne = "-";
		lineTwo = "-";
		updateTime = null;
		period = 0L;
		//startTLETimer();
	}
	
	
	
	/**
	 * Get the current TLE
	 * @return The update time, and the TLE 
	 */
	public Hashtable<String, String> getTLE(){
		
		Hashtable<String, String> TLE = new Hashtable<String, String>();
		
		//Add all line one, line two and current TLE's update time to a map
		TLE.put("lineOne", lineOne);
		TLE.put("lineTwo", lineTwo);
		if (updateTime != null){
			TLE.put("updateTime", timestampToUTCTime(updateTime));
		} else {
			TLE.put("updateTime", "-");
		}
		return TLE;
	}
	
	
	
	/**
	 * Get the period
	 * @return in long value that represente the period in second
	 */
	public Long getPeriod(){
		return period;
	}
	
	
	
	/**
	 * Request the TLE data with flight data, and change the buffer value(private)
	 */
	public void updateTLE(){
		
		try {
			//Connect to to the API
			URL url = new URL(TLEChannel);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
			connection.setConnectTimeout(2000);
		    connection.setReadTimeout(2000);
			
			//When the response code is 200, that mean connection is successful, read the json data.
			//If the response code is not 200, return "lostConnection" as the "message" field
			if(connection.getResponseCode() == 200){
	            //Open the stream and put it into BufferedReader, and change it to a Json object
	            BufferedReader rawData = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            JSONObject dataInJson = new JSONObject(rawData.readLine());
	            rawData.close();
	            
	            //Update the local value
	            Long readTimeStampAsLong = dataInJson.getLong("tle_timestamp");
				updateTime = new Timestamp(readTimeStampAsLong.longValue() * 1000);
				lineOne = dataInJson.get("line1").toString();
				lineTwo = dataInJson.get("line2").toString();
				setPeriod();
			} 
			
			//Store the data into the database
			TLEandTelemetryDAO.addNewTLE(getTLE(), updateTime);
			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
		
	
	/**
	 * A timer for keep requesting TLE (once every minutes)
	 */
	private void startTLETimer(){
		Thread TLETimer = new Thread(){
			@Override
			public void run(){
				while(true){
					try {
						System.out.println("Updating TLE...");
						updateTLE();
						Thread.sleep(60000);	
					} catch (Exception e) {
						e.printStackTrace();
					} 
			    }
			}
		};
		TLETimer.start();
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
	 * Helper for calculate the period of the ISS, this data is used for determind how many position
	 * the ground track should display(3 orbits)
	 */
	private void setPeriod(){
		
		//Convert line 2 to a char array, and extract column 53 to 63, which the information about
		//the mean motion (revolutions per day)
		char[] lineTwoInArray = lineTwo.toCharArray();  
		String meanMotion = new String(lineTwoInArray, 52, 11);
		
		//Calculate period in seconds, plus one so that it actually cover the time it takes for 3 revolution
		Double temp = (86400.0 / Double.parseDouble(meanMotion));
		period = new Long(temp.longValue()) + 1L; 
	}
}
