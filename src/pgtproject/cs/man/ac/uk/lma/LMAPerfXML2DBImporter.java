package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.bean.LMAPerformance;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class LMAPerfXML2DBImporter {
	private static Logger logger = Logger.getLogger(LMAPerfXML2DBImporter.class);

	public static void main(String[] args) {

		
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			File artistFile = new File("dbdata/lma-clean-perf-data.xml");
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
				if(!lmaArtist.getAttribute("perf_in_db").equals("true")){
					String lmaArtistUri = lmaArtist.getElementsByTagName("lmaArtistUri").item(0).getTextContent();
					String mbid = lmaArtist.getAttribute("mbid");
					NodeList lmaPerformanceList = lmaArtist.getElementsByTagName("performance");
					if (lmaPerformanceList.getLength()>0){
						try {
							Class.forName("org.sqlite.JDBC");
					        Connection conn = DriverManager.getConnection(Constants.CONN_STRING);
					        conn.setAutoCommit(false);
					        
					        PreparedStatement ps = null;
					        ps = conn.prepareStatement("INSERT INTO LMA_PERFORMANCE(lmaPerformanceUri, lmaArtistUri, mbid, date, prefPerformanceLabel, lmaVenueURI, location, placeName, prefPlaceLabel, lastfmLocURI, geonameLocURI) values (?,?,?,?,?,?,?,?,?,?,?)");
					        
							for (int j = 0; j < lmaPerformanceList.getLength(); j++){
								Node node2 = lmaPerformanceList.item(j);
								Element lmaPerformance = (Element)node2;
								if(!lmaPerformance.getAttribute("in_db").equals("true")){
									String lmaPerformanceUri = lmaPerformance.getAttribute("id");
									String date = lmaPerformance.getElementsByTagName("date").item(0).getTextContent();
									String prefPerformanceLabel = lmaPerformance.getElementsByTagName("prefPerfLabel").item(0).getTextContent();
									String lmaVenueURI = lmaPerformance.getElementsByTagName("lmaVenueURI").item(0).getTextContent();
									String location = lmaPerformance.getElementsByTagName("location").item(0).getTextContent();
									String placeName = lmaPerformance.getElementsByTagName("placeName").item(0).getTextContent();
									String prefPlaceLabel = lmaPerformance.getElementsByTagName("prefPlaceLabel").item(0).getTextContent();
									String lastfmLocURI = lmaPerformance.getElementsByTagName("lastfmLocURI").item(0).getTextContent();
									String gnameLocURI = lmaPerformance.getElementsByTagName("gnameLocURI").item(0).getTextContent();
									
									ps.setString(1, lmaPerformanceUri);
									ps.setString(2, lmaArtistUri);
									ps.setString(3, mbid);
									ps.setString(4, date);
									ps.setString(5, prefPerformanceLabel);
									ps.setString(6, lmaVenueURI);
									ps.setString(7, location);
									ps.setString(8, placeName);
									ps.setString(9, prefPlaceLabel);
									ps.setString(10, lastfmLocURI);
									ps.setString(11, gnameLocURI);
									
									ps.addBatch();
									lmaPerformance.setAttribute("in_db","true");
								}
							}
						
							int[] updateCounts = ps.executeBatch();
							
							conn.commit();
							lmaArtist.setAttribute("perf_in_db","true");
							dbman.updateSourceFile("dbdata/lma-clean-perf-data.xml",doc);
							System.out.println("Processed " + i + ": " + lmaArtistUri);
							conn.close();
							
						} catch (Exception e) {
							lmaArtist.setAttribute("perf_in_db","error");
							dbman.updateSourceFile("dbdata/lma-clean-perf-data.xml",doc);
							logger.error("FAILED " + i + ": " + lmaArtistUri,e);
							System.out.println("FAILED " + i + ": " + lmaArtistUri);
						}

					}else{
						lmaArtist.setAttribute("perf_in_db","no_performances");
						dbman.updateSourceFile("dbdata/lma-clean-perf-data.xml",doc);
						System.out.println("NO PERFORMANCES " + i + ": " + lmaArtistUri);
						logger.error("NO PERFORMANCES " + i + ": " + lmaArtistUri);
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
