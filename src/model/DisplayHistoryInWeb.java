package model;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**Helper for open and display all the history on the web page
 * @author jmwx
 *
 */
public class DisplayHistoryInWeb {
	
	/**
	 * Open and display all telemetry history on the web page
	 */
	public static void telemetryHistoryOnWeb(){
		
		//Get all data from DAO
		List<Map<String, Object>> allTelemetry = TLEandTelemetryDAO.getAllTelemetry();
		
		//define a HTML String Builder
        StringBuilder htmlStringBuilder=new StringBuilder();
        
        //append the title
        htmlStringBuilder.append("<!DOCTYPE html><html><head><title>All telemetry</title>");
        
        //append the style of the table
        htmlStringBuilder.append(
        	"<style>table, th, td{ "
        	+ "border: 1px solid black;"
        	+ "border-collapse: collapse;"
        	+ "font-weight: lighter"
        	+ "}"
        	+ "th, td {"
        	+ "padding: 1px;"
        	+ "text-align: left; "
        	+ "}"
        	+ "</style></head><body><table style='width:100%'>"
        );
        
        //append the table's column names (First row)
        htmlStringBuilder.append(
        	"<tr>"
        		+ "<th>update time</th>"
        		+ "<th>elapsed time</th>"
        		+ "<th>status</th>"
        		+ "<th>latitude</th>"
        		+ "<th>longitude</th>"
        		+ "<th>solarLatitude</th>"
        		+ "<th>solarLongitude</th>"
        		+ "<th>altitude</th>"
        		+ "<th>velocity</th>"
        		+ "<th>deltaV</th>"
        		+ "<th>deltaH</th>"
        		+ "<th>lighting</th>"
        	+ "</tr>"
        );
        
        //append each individual set of telemetry
        for(Map<String, Object> oneSet : allTelemetry) {
        	htmlStringBuilder.append(
        		"<tr>"
        			+ "<th>" 
        			+ oneSet.get("updateTime").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("elapsedTime").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("status").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("latitude").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("longitude").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("solarLatitude").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("solarLongitude").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("altitude").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("velocity").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("deltaV").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("deltaH").toString()
        			+ "</th>"
        			+ "<th>" 
        			+ oneSet.get("lighting").toString()
        			+ "</th>"
        		+ "</tr>"
        	);
		}	
        		
        //append the end line
        htmlStringBuilder.append("</table></body></html>");
        
		//write html content to a file
		try {
			WriteToHTMLFile(htmlStringBuilder.toString(), "AllTelemetry.html");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Open the web page automaticity(But Permission denied)
		/*try {
			Runtime r = Runtime.getRuntime();
			r.exec(System.getProperty("user.dir") + File.separator + "AllTelemetry.html");
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
	}
	
	
	
	/**
	 * Open and display all TLE history on the web page
	 */
	public static void TLEHistoryOnWeb(){
		
		//Get all data from DAO
		List<Map<String, Object>> allTLE = TLEandTelemetryDAO.getAllTLE();
				
		//define a HTML String Builder
		StringBuilder htmlStringBuilder=new StringBuilder();
		        
		//append the title
		htmlStringBuilder.append("<!DOCTYPE html><html><head><title>All TLE</title>");
		
		//append the style of the table
        htmlStringBuilder.append(
        	"<style>table, th, td{ "
        	+ "border: 0px solid black;"
        	+ "border-collapse: collapse;"
        	+ "font-weight: lighter"
        	+ "}"
        	+ "th, td {"
        	+ "padding: 1px;"
        	+ "text-align: left; "
        	+ "}"
        	+ "</style></head><body><table style='width:100%'>"
        );
        
        //append each individual set of telemetry
        for(Map<String, Object> oneSet : allTLE) {
        	htmlStringBuilder.append(
            	"<tr>"
            		+ "<th>Update time: </th>"
            		+ "<th>" 
            		+ oneSet.get("updateTime").toString()
            		+ "</th>"
            	+ "</tr>"
            	+ "<tr>"
	        		+ "<th> </th>"
	        		+ "<th>" 
	        		+ oneSet.get("lineOne")
	        		+ "</th>"
        		+ "</tr>"
        		+ "<tr>"
	        		+ "<th> </th>"
	        		+ "<th>" 
	        		+ oneSet.get("lineTwo")
	        		+ "</th>"
        		+ "</tr>"
        		+ "<tr><th></th></tr>"
            );
        }
		
		//append the end line
        htmlStringBuilder.append("</body></html>");
        
        //write html content to a file
      	try {
      		WriteToHTMLFile(htmlStringBuilder.toString(), "AllTLE.html");
      	} catch (IOException e) {
      		e.printStackTrace();
      	}
		
	}
	
	
	
	/**
	 * Helper for writing a branch of text into a file
	 * @param webContent -The HTML for the web
	 * @param fileName -The file that the HTML going to be written
	 * @throws IOException
	 */
	public static void WriteToHTMLFile(String webContent, String fileName) throws IOException {
		
		//Get project directory which the file is going to be created here
        String projectPath = System.getProperty("user.dir");
        File html = new File(projectPath + File.separator + fileName);
        
        // if file does exists, then delete that one
        if (html.exists()) {
        	html.delete();
        }
        
        //write the content into the new file with OutputStreamWriter
        OutputStream outputStream = new FileOutputStream(html.getAbsoluteFile());
	    Writer webWriter=new OutputStreamWriter(outputStream);
	    webWriter.write(webContent);
	    webWriter.close();
    }

}
