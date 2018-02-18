package view;

import java.util.List;
import java.util.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.TLEandTelemetryDAO;

/**
 *Helper for plot the ISS and its' ground track on the map   
 *
 */
public class PlotISSandGroundTrack {
	
	/**
	 * Clear the last ISS plot and add the new plot on the map
	 * @param mapGraphic - The GraphicsContext that ISS going to be putted on
	 * @param lat 
	 * @param lon
	 */
	public static void plotISS(GraphicsContext graphic, String lat, String lon){
		
		try{
			//Record the location for the deleting later
			double latitude_Y = Double.parseDouble(lat);
			double longitude_X = Double.parseDouble(lon);
			
			//Note the origin of the map is at the top left corner, not the centre,
			//and latitude and longitude have a range of -90 ~ 90 and -180 ~ 180, but the map is 0 ~ 360 and 0 ~ 720
			//so the latitude and longitude have to change to the map's coordinate
			latitude_Y = (90d - latitude_Y) * 2d - 15d;
			if (latitude_Y < 0){
				latitude_Y = latitude_Y + 360d;
			}
			longitude_X = (longitude_X + 180d) * 2d - 15d;
			if (longitude_X < 0){
				longitude_X = longitude_X + 720d;
			}
			
			//Import the iss icon to the map
		    Image iss = new Image("iss.gif", 30, 30, false, false);
		    graphic.drawImage(iss, longitude_X, latitude_Y);
		    
		    
		//Display "Lost Connection" on the screen
		} catch (Exception e){
			graphic.setFill(Color.RED);
			Font bigFont = Font.font("Times New Roman", FontWeight.BOLD, 30.0);
			graphic.setFont(bigFont);
			graphic.fillText("Lost Connection", 210, 200);
		}
		
	}
	

	public static void plotGroundTrack(GraphicsContext graphic,
			List<Map<String, Object>> threeOrbitsByPeriod) {
		
		for(Map<String, Object> oneSet : threeOrbitsByPeriod) {
			
			//Record the location for the deleting later
			Double latitude_Y = (double)oneSet.get("latitude");
			Double longitude_X = (double)oneSet.get("longitude");
			
			//Note the origin of the map is at the top left corner, not the centre,
			//and latitude and longitude have a range of -90 ~ 90 and -180 ~ 180, but the map is 0 ~ 360 and 0 ~ 720
			//so the latitude and longitude have to change to the map's coordinate
			latitude_Y = (90d - latitude_Y) * 2d;
			longitude_X = (longitude_X + 180d) * 2d;
			
			//Draw a red dot
			graphic.setFill(Color.rgb(230, 255, 15, 0.2));
			graphic.fillOval(longitude_X, latitude_Y, 3, 3); //x, y, width, high
		}
	}
}
