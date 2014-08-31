package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class LMASongXML2DBImporter {
	private static Logger logger = Logger.getLogger(LMASongXML2DBImporter.class);

	public static void main(String[] args) {

		
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			File artistFile = new File("dbdata/lma-partial-with-songs02.xml");
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
				if(!lmaArtist.getAttribute("songs_in_db").equals("true")){
					String lmaArtistUri = lmaArtist.getElementsByTagName("lmaArtistUri").item(0).getTextContent();
					NodeList lmaPerformanceList = lmaArtist.getElementsByTagName("performance");
					if (lmaPerformanceList.getLength()>0){
						Connection conn = null;
						try {
							Class.forName("org.sqlite.JDBC");
					        conn = DriverManager.getConnection("jdbc:sqlite:C:\\DissertationDB\\MScProjectDB.s3db");
					        conn.setAutoCommit(false);
					        boolean checkflag = false;
					        
					        PreparedStatement ps = null;
					        ps = conn.prepareStatement("INSERT INTO LMA_SONG(lmaSongUri, lmaPerformanceUri, trackNumber, prefTrackLabel) values (?,?,?,?)");
					        
							for (int j = 0; j < lmaPerformanceList.getLength(); j++){
								Node node2 = lmaPerformanceList.item(j);
								Element lmaPerformance = (Element)node2;
								if(!lmaPerformance.getAttribute("songs_in_db").equals("true")){
									String lmaPerformanceUri = lmaPerformance.getAttribute("id");
									NodeList lmaSongList = lmaPerformance.getElementsByTagName("song");
									
									if (lmaSongList.getLength()>0){
										checkflag = true;
										for (int k = 0; k < lmaSongList.getLength(); k++){
											Node node3 = lmaSongList.item(k);
											Element lmaSong = (Element)node3;
											String lmaSongUri = lmaSong.getAttribute("id");
											String trackNumber = lmaSong.getElementsByTagName("trackNumber").item(0).getTextContent();
											String prefTrackLabel = lmaSong.getElementsByTagName("prefTrackLabel").item(0).getTextContent();
											
											ps.setString(1, lmaSongUri);
											ps.setString(2, lmaPerformanceUri);
											ps.setString(3, trackNumber);
											ps.setString(4, prefTrackLabel);
											ps.addBatch();
											lmaSong.setAttribute("in_db","true");
										}
									}else{
										lmaPerformance.setAttribute("songs_in_db","no_songs");
										//dbman.updateSourceFile("dbdata/lma-clean-perf-data.xml",doc);
										System.out.println("NO SONGS " + i + ": " + lmaPerformanceUri);
										logger.error("NO SONGS " + i + ": " + lmaPerformanceUri);
									}
								}
							}
							
							if(checkflag){
								int[] updateCounts = ps.executeBatch();
								
								conn.commit();
								lmaArtist.setAttribute("songs_in_db","true");
								dbman.updateSourceFile("dbdata/lma-partial-with-songs02.xml",doc);
								System.out.println("Processed " + i + ": " + lmaArtistUri);
								conn.close();
								
							}else{
								conn.close();
								lmaArtist.setAttribute("songs_in_db","no_songs");
								dbman.updateSourceFile("dbdata/lma-partial-with-songs02.xml",doc);
								System.out.println("Processed " + i + ": " + lmaArtistUri);
							}
						
							
							
						} catch (Exception e) {
							logger.error("FAILED " + i + ": " + lmaArtistUri,e);
							System.out.println("FAILED " + i + ": " + lmaArtistUri);
							try {
								conn.rollback();
								lmaArtist.setAttribute("perf_in_db","error");
								dbman.updateSourceFile("dbdata/lma-partial-with-songs02.xml",doc);
							}catch(Exception ex){}
							throw new RuntimeException(e);
						}finally {
							try {
								conn.close();
							} catch (Exception e) {}
							conn = null;
						}

					}else{
						lmaArtist.setAttribute("songs_in_db","no_performances");
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
