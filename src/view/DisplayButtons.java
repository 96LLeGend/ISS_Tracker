package view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.DisplayHistoryInWeb;

/**
 * Helper for create and place the buttons
 * @author jmwx
 *
 */
public class DisplayButtons {
	
	/**
	 * Create and place the button that for viewing all the historical telemetry on a web page
	 * @return	A Button object for such button
	 */
	public static Button getViewTelemetryHistoryButton(){
		
		//Create button
		Button viewTelemetryHistory = new Button("View telemetry history");
		
		//Set font
		Font bigFont = Font.font("Times New Roman", FontWeight.THIN, 12.0);
		viewTelemetryHistory.setFont(bigFont);
		
		//Set position on the scene
		viewTelemetryHistory.setLayoutX(5);
		viewTelemetryHistory.setLayoutY(560);
		
		//Set button's size
		viewTelemetryHistory.setMaxSize(150, 25);
		viewTelemetryHistory.setMinSize(150, 25);
		
		//Redirect the action to display all the telemetry on a web page
		viewTelemetryHistory.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	DisplayHistoryInWeb.telemetryHistoryOnWeb();
		    }
		});
		
		return viewTelemetryHistory;
	}
	
	
	
	/**
	 * Create and place the button that for viewing all the historical TLE on a web page
	 * @return	A Button object for such button
	 */
	public static Button getViewTLEHistoryButton(){
		
		//Create button
		Button viewTLEHistory = new Button("View TLE history");
		
		//Set font
		Font bigFont = Font.font("Times New Roman", FontWeight.THIN, 12.0);	
		viewTLEHistory.setFont(bigFont);
		
		//Set position on the scene
		viewTLEHistory.setLayoutX(160);
		viewTLEHistory.setLayoutY(560);
		
		//Set button's size
		viewTLEHistory.setMaxSize(150, 25);
		viewTLEHistory.setMinSize(150, 25);
		
		//Redirect the action to display all TLE on a web page
		viewTLEHistory.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				DisplayHistoryInWeb.TLEHistoryOnWeb();
			}
		});
		
		return viewTLEHistory;
	}

}
