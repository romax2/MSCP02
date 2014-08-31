package pgtproject.cs.man.ac.uk.lma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.database.DatabaseManager;
import fm.setlist.api.model.Setlist;

public class SongNamesCollector {

	public static void main(String[] args) {
		
		try {
			
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
					
			DatabaseManager dbman = new DatabaseManager();
			
			File artistFile = new File("data/lmaFullData.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(artistFile);
			
			NodeList resultList = doc.getElementsByTagName("result");
			
			System.out.println("Total results: " + resultList.getLength());
			//Iterate over result nodes
			for (int i = 0; i < resultList.getLength(); i++){
				Node node = resultList.item(i);
				Element eResult = (Element)node;
				String foafName = null;
				//Iterate over binding nodes
				NodeList bindings = eResult.getElementsByTagName("binding");
				for (int j = 0; j < bindings.getLength(); j++){
					Node binding = bindings.item(j);
					Element eBinding = (Element)binding;
					
					if(eBinding.getAttribute("name").equals("foafName")){
						NodeList literal = eBinding.getElementsByTagName("literal");
						foafName = literal.item(0).getTextContent();
					} 
				}
						
				NodeList trackLabels = eResult.getElementsByTagName("prefTrackLabel");
				
				if (trackLabels.getLength()>0){
					SortedSet<String> ss=new TreeSet<String>();
					
					for (int k = 0; k < trackLabels.getLength(); k++){
						/*Node trackLabel = trackLabels.item(k);
						Element eTrackLabel = (Element)trackLabel;*/
						ss.add(trackLabels.item(k).getTextContent());
					}
					
					Element songNames = doc.createElement("songNames");

					Iterator it=ss.iterator();
			        while(it.hasNext()){
			        	Element songName = doc.createElement("songName");
						songName.setTextContent((String)it.next());
						songNames.appendChild(songName);
			        }
					
			        NodeList performances = eResult.getElementsByTagName("performances");
			        eResult.insertBefore(songNames, performances.item(0));
			        dbman.updateSourceFile("data/lmaFullData.xml",doc);
					System.out.println("Processed " + i + ": " + foafName);
				}
			}
			
			long endTime1 = System.nanoTime();
			long endTime2 = System.currentTimeMillis();

			System.out.println("That took " + (endTime1 - startTime1));
			System.out.println("That took " + (endTime2 - startTime2) + " milliseconds");
			
            System.out.println("XML file updated successfully");
			System.out.println("END.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
