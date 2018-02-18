package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


/**
 * An interface that provides access to an underlying database. 
 * @author jmwx
 *
 */
public class TLEandTelemetryDAO {

	//Set up connection field
	private static final String database = "???";
	private static final String host = "???";
	private static final String username = "???";
	private static final String password = "???";
	
	private static Connection dbConnection = null;
	
	/**
	 * Check if the database and the table is exist, if it is not, create them
	 */
	private static void initialiseDatabase(){
		try {
			//Create a database if not exist
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/"
					+ "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");
			dbConnection.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS ISSdata");
		    dbConnection.close();
		    
		    //Create tables for telemetry and TLE
		    dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/" + database +
                    "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");

		    dbConnection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS TELEMETRY " +
                   "(updateTime TIMESTAMP not NULL, " +
                   "elapsedTime VARCHAR(255), " +
                   "status ENUM('Connected', 'LostLocationSource', 'LostFlightDataSource', 'LostConnection'), " +
                   "latitude DOUBLE(6,4), " +
                   "longitude DOUBLE(7,4), " +
                   "solarLatitude DOUBLE(6,4), " +
                   "solarLongitude DOUBLE(7,4), " +
                   "altitude DOUBLE(6,0), " +
                   "velocity DOUBLE(5,4), " +
                   "deltaV DOUBLE(6,4), " +
                   "deltaH DOUBLE(7,4), " +
                   "lighting VARCHAR(255), " + 
                   "PRIMARY KEY ( updateTime ))");
		    
		    dbConnection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS TLE " +
	                   "(updateTime TIMESTAMP not NULL, " +
	                   "lineOne VARCHAR(255), " +
	                   "lineTwo VARCHAR(255), " +
	                   "PRIMARY KEY ( updateTime ))");
		    dbConnection.close();
		    
		} catch (Exception e){
			//e.printStackTrace();
		}
	}
	
	
	
	/**
	 * This function take the new set of telemetry and the current elapsed time as the input, and change 
	 * those input from display form(String) to their real data form(Double, Enum and String),
	 * then store them to the "TELEMETRY" table
	 * @param newTelemetrySet
	 * @param newElapsedTime
	 */
	public static void addNewTelemetry(Hashtable<String, String> newTelemetrySet, Timestamp newUpdateTime, String newElapsedTime){
		
		Double latitude = (double) Types.NULL;
		Double longitude = (double) Types.NULL;
		Double solarLatitude = (double) Types.NULL;
		Double solarLongitude = (double) Types.NULL;
		Double altitude = (double) Types.NULL;
		Double velocity = (double) Types.NULL;
		Double deltaV = (double) Types.NULL;
		Double deltaH = (double) Types.NULL;
		
		//Convert telemetry from their display form(String) to their storage form
		TelemetryStatus status = TelemetryStatus.valueOf(newTelemetrySet.get("status"));
		
		if (status.equals(TelemetryStatus.Connected) || status.equals(TelemetryStatus.LostLocationSource)){
			latitude = Double.parseDouble(newTelemetrySet.get("lat"));
			longitude = Double.parseDouble(newTelemetrySet.get("lon"));
			solarLatitude = Double.parseDouble(newTelemetrySet.get("sl_lat"));
			solarLongitude = Double.parseDouble(newTelemetrySet.get("sl_lon"));
			altitude = Double.parseDouble(newTelemetrySet.get("H"));
			velocity = Double.parseDouble(newTelemetrySet.get("V"));
			if (newTelemetrySet.get("dV") != "-"){
				deltaV = Double.parseDouble(newTelemetrySet.get("dV"));
			}
			if (newTelemetrySet.get("dH") != "-"){
				deltaH = Double.parseDouble(newTelemetrySet.get("dH"));
			}
		} 
		else if (status.equals(TelemetryStatus.LostFlightDataSource)){
			latitude = Double.parseDouble(newTelemetrySet.get("lat"));
			longitude = Double.parseDouble(newTelemetrySet.get("lon"));
		}
		
		//Ensure the database and table is exist
		initialiseDatabase();
		
		try{
			//Connect to the table
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/" + database +
                    "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");
			
			//Form query
		    String newQuery = "INSERT INTO TELEMETRY" + "(updateTime, elapsedTime, status, latitude,"
		    		+ " longitude, solarLatitude, solarLongitude, altitude, velocity, deltaV, deltaH,"
		    		+ " lighting) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		    PreparedStatement preparation = dbConnection.prepareStatement(newQuery); 
		    preparation.setTimestamp(1, newUpdateTime);
		    preparation.setString(2, newElapsedTime);
		    preparation.setString(3, status.name());
		    preparation.setDouble(4, latitude);
		    preparation.setDouble(5, longitude);
		    preparation.setDouble(6, solarLatitude);
		    preparation.setDouble(7, solarLongitude);
		    preparation.setDouble(8, altitude);
		    preparation.setDouble(9, velocity);
		    preparation.setDouble(10, deltaV);
		    preparation.setDouble(11, deltaH);
		    preparation.setString(12, newTelemetrySet.get("LT"));
		    
		    //Execute query and update database and close connection
		    preparation.execute();
		    dbConnection.close();
		} catch (Exception e){
			//e.printStackTrace();
		}
	}
	
	
	
	/**
	 * This function take the new set of TLE as the input, and change 
	 * those input from display form(String) to their real data form(Enum and String),
	 * then store them to the "TLE" table
	 * @param newTLE
	 */
	public static void addNewTLE(Hashtable<String, String> newTLE, Timestamp newUpdateTime){
		
		//Ensure the database and table is exist
		initialiseDatabase();
		
		try{
			//Connect to the table
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/" + database +
                    "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");
			
			//Check if the current TLE already in the data base, by see if it can find the TLE by updatetime
			String newQuery = "SELECT * FROM TLE WHERE updateTime = ?";
			PreparedStatement preparation = dbConnection.prepareStatement(newQuery);
			preparation.setTimestamp(1, newUpdateTime);
			ResultSet rs = preparation.executeQuery(); 
			boolean TLEexist = false;
			while (rs.next()){
				TLEexist = true;
			}
			
			//Only update TLE when the current TLE is not exist in the database
			if (TLEexist == false){
			    newQuery = "INSERT INTO TLE" + "(updateTime, lineOne, lineTwo) VALUES (?,?,?)";
			    preparation = dbConnection.prepareStatement(newQuery); 
			    preparation.setTimestamp(1, newUpdateTime);
			    preparation.setString(2, newTLE.get("lineOne"));
			    preparation.setString(3, newTLE.get("lineTwo"));
			    preparation.execute();
			}
		    
		    dbConnection.close();
		} catch (Exception e){
			//e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * Get all telemetry, used for display all telemetry on the web page
	 * @return List of maps that that include all telemetry
	 */
	public static List<Map<String, Object>> getAllTelemetry(){
		
		List<Map<String, Object>> allTelemetry = new ArrayList<Map<String, Object>>();
		
		//Ensure the database and table is exist
		initialiseDatabase();
				
		try{
			//Connect to the table
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/" + database +
		                  "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");
			
			//Check if the current TLE already in the data base, by see if it can find the TLE by updatetime
			String newQuery = "SELECT * FROM TELEMETRY";
			PreparedStatement preparation = dbConnection.prepareStatement(newQuery);
			ResultSet result = preparation.executeQuery(); 
			
			//Marshall the result set into the list of map
			while (result.next()){
				
				Map<String, Object> oneSet = new HashMap<String, Object>();
				
				oneSet.put("updateTime", result.getObject("updateTime"));
				oneSet.put("elapsedTime", result.getObject("elapsedTime"));
				oneSet.put("status", result.getObject("status"));
				oneSet.put("latitude", result.getObject("latitude"));
				oneSet.put("longitude", result.getObject("longitude"));
				oneSet.put("solarLatitude", result.getObject("solarLatitude"));
				oneSet.put("solarLongitude", result.getObject("solarLongitude"));
				oneSet.put("altitude", result.getObject("altitude"));
				oneSet.put("velocity", result.getObject("velocity"));
				oneSet.put("deltaV", result.getObject("deltaV"));
				oneSet.put("deltaH", result.getObject("deltaH"));
				oneSet.put("lighting", result.getObject("lighting"));
				
				allTelemetry.add(oneSet);
			}
				    
		    dbConnection.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return allTelemetry;
	}
	
	
	
	/**
	 * Get all TLE, used for display all TLE on the web page
	 * @return A resultSet that include all TLE
	 */
	public static List<Map<String, Object>> getAllTLE(){
		
		List<Map<String, Object>> allTLE = new ArrayList<Map<String, Object>>();
		
		//Ensure the database and table is exist
		initialiseDatabase();
				
		try{
			//Connect to the table
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/" + database +
		                  "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");
			
			//Check if the current TLE already in the data base, by see if it can find the TLE by updatetime
			String newQuery = "SELECT * FROM TLE";
			PreparedStatement preparation = dbConnection.prepareStatement(newQuery);
			ResultSet result = preparation.executeQuery(); 
			
			//Marshall the result set into the list of map
			while (result.next()){
				
				Map<String, Object> oneSet = new HashMap<String, Object>();
				
				oneSet.put("updateTime", result.getObject("updateTime"));
				oneSet.put("lineOne", result.getObject("lineOne"));
				oneSet.put("lineTwo", result.getObject("lineTwo"));
				
				allTLE.add(oneSet);
			}
				    
		    dbConnection.close();
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		return allTLE;
	}
	
	public static List<Map<String, Object>> getThreeOrbitsByPeriod(Long periodInSeconds){
		
		List<Map<String, Object>> locationWithinThreeOrbits = new ArrayList<Map<String, Object>>();
		
		//Calculate the total time span
		Long timeSpan = periodInSeconds * 3000L;
		
		//Calculate the start time for the time span
		Long startTimeInLong = (new Timestamp(System.currentTimeMillis()).getTime()) - timeSpan;
		Timestamp startTime = new Timestamp(startTimeInLong);
		
		//Ensure the database and table is exist
		initialiseDatabase();
						
		try{
			//Connect to the table
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ host + "/" + database +
				                "?user=" + username + "&password=" + password + "&verifyServerCertificate=false&useSSL=true");
					
			//Check if the current TLE already in the data base, by see if it can find the TLE by updatetime
			String newQuery = "SELECT latitude, longitude FROM TELEMETRY WHERE updateTime >= ? ";
			PreparedStatement preparation = dbConnection.prepareStatement(newQuery);
			preparation.setTimestamp(1, startTime);
			ResultSet result = preparation.executeQuery(); 
					
			//Marshall the result set into the list of map
			while (result.next()){
						
				Map<String, Object> oneSet = new HashMap<String, Object>();
						
				oneSet.put("latitude", result.getObject("latitude"));
				oneSet.put("longitude", result.getObject("longitude"));
				
				locationWithinThreeOrbits.add(oneSet);
			}
			    
			dbConnection.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return locationWithinThreeOrbits;
	}
}
