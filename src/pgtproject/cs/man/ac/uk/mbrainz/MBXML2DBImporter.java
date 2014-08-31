package pgtproject.cs.man.ac.uk.mbrainz;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Types;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class MBXML2DBImporter {
	private static Logger logger = Logger.getLogger(MBXML2DBImporter.class);

	public static void main(String[] args) {
		
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			File artistFile = new File("dbdata/mbrainzdata-clean.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(artistFile);
			
			NodeList mbArtistList = doc.getElementsByTagName("mbArtist");
			
			System.out.println("Total MB Artists: " + mbArtistList.getLength());
			logger.info("Total MB Artists: " + mbArtistList.getLength());
			
			//Iterate over result nodes
			for (int i = 0; i < mbArtistList.getLength(); i++){
				Node node = mbArtistList.item(i);
				Element mbArtist = (Element)node;
				String mbArtistUri = mbArtist.getElementsByTagName("mbURI").item(0).getTextContent();
				String mbArtistId = mbArtist.getAttribute("id");
				if(!mbArtist.getAttribute("in_db").equals("true")){
					Connection conn = null;
					try {
						Class.forName("org.sqlite.JDBC");
				        conn = DriverManager.getConnection("jdbc:sqlite:C:\\DissertationDB\\MScProjectDB.s3db");
				        conn.setAutoCommit(false);
				        
				        PreparedStatement ps = null;

						String mbName = mbArtist.getElementsByTagName("mbName").item(0).getTextContent();
						String mbUniqueName = mbArtist.getElementsByTagName("mbUniqueName").item(0).getTextContent();
						String mbSortName = mbArtist.getElementsByTagName("mbSortName").item(0).getTextContent();
						//String mbTypeNode = mbArtist.getElementsByTagName("mbType").item(0).getTextContent();
						Node mbTypeNode = mbArtist.getElementsByTagName("mbType").item(0);
						
						ps = conn.prepareStatement("INSERT INTO MB_ARTIST(mbArtistId, mbArtistURI, mbName, mbUniqueName, mbSortName, mbType) values (?,?,?,?,?,?)");
						
						ps.setString(1, mbArtistId);
						ps.setString(2, mbArtistUri);
						ps.setString(3, mbName);
						ps.setString(4, mbUniqueName);
						ps.setString(5, mbSortName);
						//ps.setString(6, mbType);
						if(mbTypeNode == null){ps.setNull(6,Types.NULL);} else {ps.setString(6, mbTypeNode.getTextContent());}
						ps.executeUpdate();
						
						NodeList mbWorkList = mbArtist.getElementsByTagName("mbWork");
						if (mbWorkList.getLength()>0){
							ps = conn.prepareStatement("INSERT INTO MB_WORK(mbWorkId,mbWorkURI, mbArtistURI, mbArtistId, mbUniqueTitle, mbTitle, mbWorkType, mbDisambiguation) values (?,?,?,?,?,?,?,?)");
							for (int j = 0; j < mbWorkList.getLength(); j++){
								Node node2 = mbWorkList.item(j);
								Element mbWork = (Element)node2;
								if(!mbWork.getAttribute("in_db").equals("true")){
									
									String mbWorkId = mbWork.getAttribute("id");
									String mbWorkURI = mbWork.getElementsByTagName("mbWorkURI").item(0).getTextContent();
									String mbUniqueTitle = mbWork.getElementsByTagName("mbUniqueTitle").item(0).getTextContent();
									String mbTitle = mbWork.getElementsByTagName("mbTitle").item(0).getTextContent();
									//String mbWorkType = mbWork.getElementsByTagName("mbWorkType").item(0).getTextContent();
									//String mbDisambiguation = mbWork.getElementsByTagName("mbDisambiguation").item(0).getTextContent();
									Node mbWorkTypeNode = mbWork.getElementsByTagName("mbWorkType").item(0);
									Node mbDisambiguationNode = mbWork.getElementsByTagName("mbDisambiguation").item(0);
									 
									ps.setString(1, mbWorkId);
									ps.setString(2, mbWorkURI);
									ps.setString(3, mbArtistUri);
									ps.setString(4, mbArtistId);
									ps.setString(5, mbUniqueTitle);
									ps.setString(6, mbTitle);
									//ps.setString(6, mbWorkType);
									//ps.setString(7, mbDisambiguation);
									if(mbWorkTypeNode == null){ps.setNull(7,Types.NULL);} else {ps.setString(7, mbWorkTypeNode.getTextContent());}
									if(mbDisambiguationNode == null){ps.setNull(8,Types.NULL);} else {ps.setString(8, mbDisambiguationNode.getTextContent());}
									ps.addBatch();
									mbWork.setAttribute("in_db","true");
								}
							}
							
								int[] updateCounts = ps.executeBatch();
								
								conn.commit();
								mbArtist.setAttribute("in_db","true");
								dbman.updateSourceFile("dbdata/mbrainzdata-clean.xml",doc);
								System.out.println("Processed " + i + ": " + mbArtistUri);
								conn.close();
						}else{
							
							conn.commit();
							
							mbArtist.setAttribute("in_db","no_works");
							dbman.updateSourceFile("dbdata/mbrainzdata-clean.xml",doc);
							System.out.println("NO WORKS " + i + ": " + mbArtistUri);
							logger.error("NO WORKS " + i + ": " + mbArtistUri);
							conn.close();
						}
					
					} catch (Exception e) {
						logger.error("FAILED " + i + ": " + mbArtistUri,e);
						System.out.println("FAILED " + i + ": " + mbArtistUri);
						try {
							conn.rollback();
							mbArtist.setAttribute("in_db","error");
							dbman.updateSourceFile("dbdata/mbrainzdata-clean.xml",doc);
						}catch(Exception ex){}
						throw new RuntimeException(e);
					}finally {
						try {
							conn.close();
						} catch (Exception e) {}
						conn = null;
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
