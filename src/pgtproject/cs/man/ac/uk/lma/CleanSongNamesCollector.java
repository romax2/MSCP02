package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class CleanSongNamesCollector {
	private static Logger logger = Logger.getLogger(CleanSongNamesCollector.class);

	public static void main(String[] args) {
		
		try {
			
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
					
			DatabaseManager dbman = new DatabaseManager();
			
			File artistFile = new File("data/lma-partial-with-songs.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(artistFile);
			
			NodeList lmaArtistList = doc.getElementsByTagName("lmaArtist");
			
			System.out.println("Total Artists: " + lmaArtistList.getLength());
			logger.info("Total Artists: " + lmaArtistList.getLength());
			//Iterate over result nodes
			for (int i = 0; i < lmaArtistList.getLength(); i++){
				Node node = lmaArtistList.item(i);
				Element lmaArtist = (Element)node;
				String lmaArtistUri = lmaArtist.getElementsByTagName("lmaArtistUri").item(0).getTextContent();
				String foafName = lmaArtist.getElementsByTagName("foafName").item(0).getTextContent();
				
				NodeList trackLabels = lmaArtist.getElementsByTagName("prefTrackLabel");
				
				if (trackLabels.getLength()>0){
					SortedSet<String> ss=new TreeSet<String>();
					for (int k = 0; k < trackLabels.getLength(); k++){
						ss.add(trackLabels.item(k).getTextContent());
					}
					
					Element songNames = doc.createElement("songNames");

					Iterator it=ss.iterator();
			        while(it.hasNext()){
			        	Element songName = doc.createElement("songName");
						songName.setTextContent((String)it.next());
						songNames.appendChild(songName);
			        }
					
			        NodeList performances = lmaArtist.getElementsByTagName("performances");
			        lmaArtist.insertBefore(songNames, performances.item(0));
			        dbman.updateSourceFile("data/lma-partial-with-songs.xml",doc);
					System.out.println("Processed " + i + ": " + foafName);
					
					//logger.info("Processed|" + lmaArtistUri + "|songnames: "+ss.size()+"|"+foafName);
				}else{
					//logger.info("Processed|" + lmaArtistUri + "|songnames: 0|"+foafName);
				}
			}
			
			long endTime1 = System.nanoTime();
			long endTime2 = System.currentTimeMillis();

			System.out.println("That took " + (endTime1 - startTime1));
			System.out.println("That took " + (endTime2 - startTime2) + " milliseconds");
			
            System.out.println("XML file updated successfully");
			System.out.println("END.");
			
		} catch (Exception e) {
			//logger.error("Unexpected error: ", e);
			System.out.println("Unexpected error: ");
			e.printStackTrace();
		}
		
	}

}
