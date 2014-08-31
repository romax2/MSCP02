package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.bean.LMASong;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class CleanSubEventAppender {
	private static Logger logger = Logger.getLogger(CleanSubEventAppender.class);
	public static void main(String[] args) {

		
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			File artistFile = new File("results/lma-clean-data.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(artistFile);
			
			NodeList lmaPerformanceList = doc.getElementsByTagName("performance");
			
			System.out.println("Total LMA Performances: " + lmaPerformanceList.getLength());
			logger.info("Total LMA Performances: " + lmaPerformanceList.getLength());
			
			//Iterate over performance nodes
			for (int i = 0; i < lmaPerformanceList.getLength(); i++){
				Node node = lmaPerformanceList.item(i);
				Element lmaPerformance = (Element)node;
				if(!lmaPerformance.getAttribute("appender").equals("true") && !lmaPerformance.getAttribute("appender").equals("nosongs")){
					
					String lmaPerformanceUri = lmaPerformance.getAttribute("id"); 
					
					try {
						List<LMASong>songs = dbman.getSongsByLmaPerformanceURI(lmaPerformanceUri);
						
						if(songs.size()>0){
							Element eSongs = doc.createElement("songs");
							for (LMASong sng : songs) {
								
								Element eSong = doc.createElement("song");
								eSong.setAttribute("id", sng.getLmaSongURI());
								
								Element prefTrackLabel = doc.createElement("prefTrackLabel");
								prefTrackLabel.setTextContent(sng.getPrefTrackLabel());
								eSong.appendChild(prefTrackLabel);
								
								Element trackNumber = doc.createElement("trackNumber");
								trackNumber.setTextContent(sng.getTrackNumber());
								eSong.appendChild(trackNumber);
								
								eSongs.appendChild(eSong);
								
							}
							lmaPerformance.appendChild(eSongs);
							lmaPerformance.setAttribute("scnt", Integer.toString(songs.size()));
							lmaPerformance.setAttribute("appender","true");
							dbman.updateSourceFile("results/lma-clean-data.xml",doc);
							logger.info("Processed " + i + ": " + lmaPerformanceUri);
							System.out.println("Processed " + i + ": " + lmaPerformanceUri);
							
						}else{
							lmaPerformance.setAttribute("scnt", "0");
							lmaPerformance.setAttribute("appender","nosongs");
							dbman.updateSourceFile("results/lma-clean-data.xml",doc);
							logger.error("NO SONGS " + i + ": " + lmaPerformanceUri);
							System.out.println("NO SONGS " + i + ": " + lmaPerformanceUri);
						}
						
						
					} catch (Exception e) {
						lmaPerformance.setAttribute("appender","error");
						dbman.updateSourceFile("results/lma-clean-data.xml",doc);
						logger.error("FAILED " + i + ": " + lmaPerformanceUri, e);
						System.out.println("FAILED " + i + ": " + lmaPerformanceUri);
					}
				}
			}

			
			long endTime1 = System.nanoTime();
			long endTime2 = System.currentTimeMillis();

			System.out.println("That took " + (endTime1 - startTime1));
			System.out.println("That took " + (endTime2 - startTime2) + " milliseconds");
			logger.info("That took " + (endTime2 - startTime2) + " milliseconds");
			
            System.out.println("XML file updated successfully");
			System.out.println("END.");
			
		} catch (Exception e) {
			logger.error("Unexpected error: ", e);
			e.printStackTrace();
		}

	}

}
