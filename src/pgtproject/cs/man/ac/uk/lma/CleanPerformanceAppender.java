package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.bean.LMAArtist;
import pgtproject.cs.man.ac.uk.bean.LMAPerformance;
import pgtproject.cs.man.ac.uk.bean.LMASong;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class CleanPerformanceAppender {
	private static Logger logger = Logger.getLogger(CleanPerformanceAppender.class);

	public static void main(String[] args) {

		
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			File artistFile = new File("results/lma-clean-data.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(artistFile);
			
			NodeList lmaArtistList = doc.getElementsByTagName("lmaArtist");
			
			System.out.println("Total LMA Artists: " + lmaArtistList.getLength());
			logger.info("Total LMA Artists: " + lmaArtistList.getLength());
			
			//Iterate over result nodes
			for (int i = 0; i < lmaArtistList.getLength(); i++){
				Node node = lmaArtistList.item(i);
				Element lmaArtist = (Element)node;
				if(!lmaArtist.getAttribute("appender").equals("true")){
					String lmaArtistUri = lmaArtist.getElementsByTagName("lmaArtistUri").item(0).getTextContent();
					String foafName = lmaArtist.getElementsByTagName("foafName").item(0).getTextContent();
				
					try {
						LMAArtist artist = dbman.getLmaArtist(lmaArtistUri);
						List<LMAPerformance> performances = artist.getPerformances();
						
						if (performances.size()>0){
							Element ePerformances = doc.createElement("performances");
							
							for (LMAPerformance pf : performances) {
								Element ePerformance = doc.createElement("performance");
								
								ePerformance.setAttribute("id", pf.getLmaPerformanceURI());
								
								Element date = doc.createElement("date");
								date.setTextContent(pf.getDate());
								ePerformance.appendChild(date);
								
								Element gnameLocURI = doc.createElement("gnameLocURI");
								gnameLocURI.setTextContent(pf.getGeonamesLocationURI());
								ePerformance.appendChild(gnameLocURI);
								
								Element lastfmLocURI = doc.createElement("lastfmLocURI");
								lastfmLocURI.setTextContent(pf.getLastfmLocationURI());
								ePerformance.appendChild(lastfmLocURI);
								
								Element lmaVenueURI = doc.createElement("lmaVenueURI");
								lmaVenueURI.setTextContent(pf.getLmaVenueURI());
								ePerformance.appendChild(lmaVenueURI);
								
								Element location = doc.createElement("location");
								location.setTextContent(pf.getLocation());
								ePerformance.appendChild(location);
								
								Element placeName = doc.createElement("placeName");
								placeName.setTextContent(pf.getPlaceName());
								ePerformance.appendChild(placeName);
								
								Element prefPerfLabel = doc.createElement("prefPerfLabel");
								prefPerfLabel.setTextContent(pf.getPrefPerformanceLabel());
								ePerformance.appendChild(prefPerfLabel);
								
								Element prefPlaceLabel = doc.createElement("prefPlaceLabel");
								prefPlaceLabel.setTextContent(pf.getPrefPlaceLabel());
								ePerformance.appendChild(prefPlaceLabel);
								
								List<LMASong>songs = pf.getSongs();
								
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
									ePerformance.appendChild(eSongs);
									ePerformance.setAttribute("scnt", Integer.toString(songs.size()));
								}else{
									ePerformance.setAttribute("scnt", "0");
									logger.error("NO SONGS " + i + ": " + lmaArtistUri + " | " + foafName + " | " + pf.getLmaPerformanceURI());
								}
								ePerformances.appendChild(ePerformance);
							}
							lmaArtist.appendChild(ePerformances);
							
							lmaArtist.setAttribute("appender","true");
							dbman.updateSourceFile("results/lma-clean-data.xml",doc);
							logger.info("Processed " + i + ": " + lmaArtistUri + " | " + foafName);
							System.out.println("Processed " + i + ": " + lmaArtistUri + " | " + foafName);
						}else{
							lmaArtist.setAttribute("appender","noperformances");
							dbman.updateSourceFile("results/lma-clean-data.xml",doc);
							logger.error("NO PERFORMANCES " + i + ": " + lmaArtistUri + " | " + foafName);
							System.out.println("NO PERFORMANCES " + i + ": " + lmaArtistUri + " | " + foafName);
						}
					} catch (IOException e) {
						lmaArtist.setAttribute("appender","error");
						dbman.updateSourceFile("results/lma-clean-data.xml",doc);
						logger.error("FAILED " + i + ": " + lmaArtistUri + " | " + foafName);
						System.out.println("Failed " + i + ": " + foafName);
					}
				} else {
					System.out.print(i+", ");
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
