package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.bean.LMAArtist;
import pgtproject.cs.man.ac.uk.bean.LMAPerformance;
import pgtproject.cs.man.ac.uk.bean.LMASong;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class SongNameMBDescriptor {

	public static void main(String[] args) {

		
		try {
			DatabaseManager dbman = new DatabaseManager();
			
			//PrintWriter pw = new PrintWriter(new FileOutputStream(new File("C:\\Proyectos\\LinkedDataEnv\\SetlistFMClient\\results\\artistsNotInSetlistFM.txt")));
			//pw.print("");
			 
			
			
			File artistFile = new File("data/lmaFullData2.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(artistFile);
			
			NodeList resultList = doc.getElementsByTagName("result");
			
			System.out.println("Total results: " + resultList.getLength());
			//Iterate over result nodes
			for (int i = 0; i < resultList.getLength(); i++){
				Node node = resultList.item(i);
				
				if (node.getNodeType() == Node.ELEMENT_NODE){
					Element eResult = (Element)node;
					
					if(!eResult.getAttribute("appender").equals("true")){
					
						String lmaID = null;
						String foafName = null;
					//Iterate over binding nodes
						NodeList bindings = eResult.getElementsByTagName("binding");
						for (int j = 0; j < bindings.getLength(); j++){
							
							Node binding = bindings.item(j);
							if (binding.getNodeType() == Node.ELEMENT_NODE){
								Element eBinding = (Element)binding;
								
								if(eBinding.getAttribute("name").equals("foafName")){
									//System.out.println("here1");
									NodeList literal = eBinding.getElementsByTagName("literal");
									foafName = literal.item(0).getTextContent();
								} 
								
								if(eBinding.getAttribute("name").equals("lmaID")){
									NodeList uri = eBinding.getElementsByTagName("uri");
									lmaID = uri.item(0).getTextContent();
								}
																
							}
							
							
						}
						
						
						try {
							
							LMAArtist artist = dbman.getLmaArtist(lmaID.toString());
							
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
									}
									
									
									ePerformances.appendChild(ePerformance);
									
								}
								
								eResult.appendChild(ePerformances);
							}
							
							eResult.setAttribute("appender","true");
							dbman.updateSourceFile("data/lmaArtists.xml",doc);
							System.out.println("Processed " + i + ": " + foafName);
							
						} catch (IOException e) {
							eResult.setAttribute("appender","error");
							dbman.updateSourceFile("data/lmaArtists.xml",doc);
							System.out.println("Failed " + i + ": " + foafName);
						}
					}	
				}
			}

			
            System.out.println("XML file updated successfully");
			
			System.out.println("END.");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
