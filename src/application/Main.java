package application;
	

import java.util.Hashtable;

import view.DisplayButtons;
import view.DisplayTelemetry;
import view.PlotISSandGroundTrack;
import model.ISSTelemetryReceiver;
import model.TLEMonitor;
import model.TLEandTelemetryDAO;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.util.Duration;



public class Main extends Application {
	
	//Counters for control the frequency of polling telemetry and TLE
	private int telemetryDelay = 0;
	private int TLEDelay = 0;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			
			//Set the stage for the show
			primaryStage.setTitle("ISS Mission Control");
			primaryStage.setWidth(720);
			primaryStage.setHeight(620);
			
			//Create canvas and graphicsContext for drawing on the current page
			Canvas missionControlBackground = new Canvas(720, 620);
		    GraphicsContext missionControlGraphic = missionControlBackground.getGraphicsContext2D();
		    
		    //Import the map image
		    Image map = new Image("map.gif", 720, 360, false, false);
		    missionControlGraphic.drawImage(map, 0, 0);
		    
			//Add the image , button and graphic to the scene
			Group missionControlView = new Group();     
			missionControlView.getChildren().addAll(missionControlBackground);
			missionControlView.getChildren().addAll(DisplayButtons.getViewTelemetryHistoryButton());
			missionControlView.getChildren().addAll(DisplayButtons.getViewTLEHistoryButton());
			
			//Add the whole groud to the scene
			Scene scene = new Scene(missionControlView, 720, 620);
			
			//Create a mission control and TLE monitor for receiving telemetry
			ISSTelemetryReceiver MissionControl = new ISSTelemetryReceiver();
			TLEMonitor ISSTLEMonitor = new TLEMonitor();
			
			//Communicate with ISS for new telemetry
			MissionControl.updateTelemetry();
			MissionControl.updateElapsedTime();
			ISSTLEMonitor.updateTLE();
			
			//Read new telemetry
    		Hashtable<String, String> telemetrySet = MissionControl.getTelemetry();
    		Hashtable<String, String> TLESet = ISSTLEMonitor.getTLE();
    		
    		//Display telemetry and plot location
    		PlotISSandGroundTrack.plotISS(missionControlGraphic, telemetrySet.get("lat"), telemetrySet.get("lon"));
    		DisplayTelemetry.displayTelemetry(missionControlGraphic, MissionControl.getElapsedTime(), telemetrySet, TLESet);
    		
			//Constantly update the ISS location on the map and its telemetry
			Timeline missionTimeLine = new Timeline();
		    missionTimeLine.setCycleCount(Animation.INDEFINITE);
	        KeyFrame update = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
	        	
	        	public void handle(ActionEvent event) {
	        		
	        		//clear the old canvas and add a new one for drawing
	        		missionControlView.getChildren().clear();
	        		
	        		//Create canvas and graphicsContext for drawing on the current page
	    			Canvas missionControlBackground = new Canvas(720, 620);
	    		    GraphicsContext missionControlGraphic = missionControlBackground.getGraphicsContext2D();
	    		    
	    		    //Import the map image
	    		    Image map = new Image("map.gif", 720, 360, false, false);
	    		    missionControlGraphic.drawImage(map, 0, 0);
	    		    
	    		    //Add the new canvas and buttons
	        		missionControlView.getChildren().addAll(missionControlBackground);
	    			missionControlView.getChildren().addAll(DisplayButtons.getViewTelemetryHistoryButton());
	    			missionControlView.getChildren().addAll(DisplayButtons.getViewTLEHistoryButton());
	    		    
	    			//Communicate with ISS for new telemetry
	    			if (telemetryDelay > 1){
	    				//System.out.println("Updating telemetry...");
	    				MissionControl.updateTelemetry();
	    				telemetryDelay = 0;
	    			} else {
	    				telemetryDelay = telemetryDelay + 1;
	    			}
	    			MissionControl.updateElapsedTime();
	    			if (TLEDelay > 58){
	    				//System.out.println("Updating TLE...");
	    				ISSTLEMonitor.updateTLE();
	    				TLEDelay = 0;
	    			} else {
	    				TLEDelay = TLEDelay + 1;
	    			}
	    			
	    			//Read new telemetry
	        		Hashtable<String, String> telemetrySet = MissionControl.getTelemetry();
	        		Hashtable<String, String> TLESet = ISSTLEMonitor.getTLE();
	        
	        		//Display telemetry and plot location
	        		PlotISSandGroundTrack.plotGroundTrack(missionControlGraphic, 
	        				TLEandTelemetryDAO.getThreeOrbitsByPeriod(ISSTLEMonitor.getPeriod()));
	        		PlotISSandGroundTrack.plotISS(missionControlGraphic, telemetrySet.get("lat"), telemetrySet.get("lon"));
	        		DisplayTelemetry.displayTelemetry(missionControlGraphic, MissionControl.getElapsedTime(), telemetrySet, TLESet);
	        	}
	        });
	        missionTimeLine.getKeyFrames().add(update);
	        missionTimeLine.play();

			//Open App
			primaryStage.setScene(scene);
			primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
