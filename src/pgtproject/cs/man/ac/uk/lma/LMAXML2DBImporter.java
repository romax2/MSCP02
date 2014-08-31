package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.bean.LMAPerformance;
import pgtproject.cs.man.ac.uk.bean.LMASong;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class LMAXML2DBImporter {
	private static Logger logger = Logger.getLogger(LMAXML2DBImporter.class);

	public static void main(String[] args) {

		
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			File artistFile = new File("dbdata/lma-clean-data.xml");
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
				if(!lmaArtist.getAttribute("in_db").equals("true")){
					String lmaArtistUri = lmaArtist.getElementsByTagName("lmaArtistUri").item(0).getTextContent();
					String mbArtistUri = lmaArtist.getElementsByTagName("mbArtistUri").item(0).getTextContent();
					String foafName = lmaArtist.getElementsByTagName("foafName").item(0).getTextContent();
					String artistSkosPrefLabel = lmaArtist.getElementsByTagName("artistSkosPrefLabel").item(0).getTextContent();
					String mbid = lmaArtist.getAttribute("mbid");
					try {
						
						dbman.insertLMAArtist(lmaArtistUri, mbArtistUri, mbid, foafName, artistSkosPrefLabel);
						
						
						lmaArtist.setAttribute("in_db","true");
						dbman.updateSourceFile("dbdata/lma-clean-data.xml",doc);
						System.out.println("Inserted " + i + ": " + foafName);
					} catch (Exception e) {
						lmaArtist.setAttribute("in_db","false");
						dbman.updateSourceFile("dbdata/lma-clean-data.xml",doc);
						logger.error("FAILED " + i + ": " + lmaArtistUri + " | " + foafName,e);
						System.out.println("Failed " + i + ": " + foafName);
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
