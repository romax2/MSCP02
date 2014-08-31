package pgtproject.cs.man.ac.uk.mbrainz;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;
import pgtproject.cs.man.ac.uk.setlistfm.SFMSongDataDBImporter;

public class SFMMBComparator {
	private static Logger logger = Logger.getLogger(SFMMBComparator.class);

	public static void main(String[] args) {
		
		Connection conn0 = null;
		PreparedStatement sql = null;
		
		try {
			
			Class.forName("org.sqlite.JDBC");
	        conn0 = DriverManager.getConnection(Constants.CONN_STRING);
	        conn0.setAutoCommit(false);
	        String query = "select distinct mbid from sfm_artist where populated = \'false\'";
			sql = conn0.prepareStatement(query);
			
			
			ResultSet rs = sql.executeQuery();
			List<String> mbArtistList = new ArrayList<String>();
			while(rs.next()){
				String mbid = rs.getString("mbid");
				mbArtistList.add(mbid);
			}
			
			
			
			
			
			DatabaseManager dbman = new DatabaseManager();
			File mbFile = new File("data/mbrainzdata.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document MBDoc = dBuilder.parse(mbFile);
			
			//Element MBRoot = MBDoc.getDocumentElement();
			
			File artistFile = new File("data/lma-partial-with-songs.xml");
			//File artistFile = new File("data/test-songs.xml");
			Document LMADoc = dBuilder.parse(artistFile);
			
			NodeList lmaArtistList = LMADoc.getElementsByTagName("lmaArtist");
			System.out.println("Total of Artists: " + lmaArtistList.getLength());
			for (int i = 0; i < lmaArtistList.getLength(); i++){
				Node node = lmaArtistList.item(i);
				Element lmaArtist = (Element)node;
				///ADD CONTROL IF
				if(lmaArtist.getAttribute("mbsonglinked").equals("true")){
					String foafName = lmaArtist.getElementsByTagName("foafName").item(0).getTextContent();
					String MBID = lmaArtist.getAttribute("mbid");
					//GET MB WORKS
					XPath xPath = XPathFactory.newInstance().newXPath();
					NodeList nodes = (NodeList)xPath.evaluate("//*[@id = '"+MBID+"']",MBDoc, XPathConstants.NODESET);
					Element MBArtist = (Element) nodes.item(0);
					NodeList MBWorks = MBArtist.getElementsByTagName("mbWork");
					//GET LMA SONG NAMES
					NodeList lmaSongNames = lmaArtist.getElementsByTagName("songName");
					//CHECK STRING SIMILARITY
					for (int m = 0; m < lmaSongNames.getLength(); m++){
						//System.out.println("size:" + lmaSongNames.getLength());
						Node lmaSongNameNode = lmaSongNames.item(m);
						Element lmaSongName = (Element)lmaSongNameNode;
						if(!lmaSongName.getAttribute("MBlinkAttempt").equals("true")){
							//logger.info("------------------------------------------------------------------------------------------------");
							String lmaSongNameTextRaw = lmaSongName.getTextContent();
							//System.out.println("lma song name:" + lmaSongNameTextRaw + " ID: " + lmaSongName.getAttribute("suggestedMBID"));
							HashMap<String, Integer>weightMap=new HashMap<String, Integer>();
							String lmaString = null;
							String mbString = null;
							String mbUniqueTitleRaw = null;
							
							String lmaSongNameText = lmaSongNameTextRaw.toLowerCase();
							lmaSongNameText = lmaSongNameText.replaceAll("&amp;","and");
							lmaSongNameText = lmaSongNameText.replaceAll("&lt;"," ");
							lmaSongNameText = lmaSongNameText.replaceAll("&gt;"," ");
							lmaSongNameText = lmaSongNameText.replaceAll("%"," ");
							lmaSongNameText = lmaSongNameText.replaceAll("\\*"," ");
							lmaSongNameText = lmaSongNameText.replaceAll("#"," ");
							lmaSongNameText = lmaSongNameText.replaceAll("\\[", " ").replaceAll("\\]"," ");
							lmaSongNameText = lmaSongNameText.replaceAll("\\(", " ").replaceAll("\\)"," ");
							lmaSongNameText = lmaSongNameText.trim();
							lmaSongNameText = lmaSongNameText.trim().replaceAll("\\s+", " ");
							lmaString = Normalizer.normalize(lmaSongNameText, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
							
							HashMap<String, String>decMap=new HashMap<String, String>();
							
							for (int n = 0; n < MBWorks.getLength(); n++){
								mbString = null;
								mbUniqueTitleRaw = null;
								Node mbWorkNode = MBWorks.item(n);
								Element mbWork = (Element)mbWorkNode;
								String mbWorkURI = mbWork.getElementsByTagName("mbWorkURI").item(0).getTextContent();
								mbUniqueTitleRaw = mbWork.getElementsByTagName("mbUniqueTitle").item(0).getTextContent();
								decMap.put(mbWorkURI, mbUniqueTitleRaw);///
								
								
								String mbUniqueTitle = mbUniqueTitleRaw.toLowerCase();
								mbUniqueTitle = mbUniqueTitle.replaceAll("&amp;","and");
								mbUniqueTitle = mbUniqueTitle.replaceAll("&lt;"," ");
								mbUniqueTitle = mbUniqueTitle.replaceAll("&gt;"," ");
								mbUniqueTitle = mbUniqueTitle.replaceAll("%"," ");
								mbUniqueTitle = mbUniqueTitle.replaceAll("\\*"," ");
								mbUniqueTitle = mbUniqueTitle.replaceAll("#"," ");
								mbUniqueTitle = mbUniqueTitle.replaceAll("\\[", " ").replaceAll("\\]"," ");
								mbUniqueTitle = mbUniqueTitle.replaceAll("\\(", " ").replaceAll("\\)"," ");
								mbUniqueTitle = mbUniqueTitle.trim();
								mbUniqueTitle = mbUniqueTitle.trim().replaceAll("\\s+", " ");
								mbString = Normalizer.normalize(mbUniqueTitle, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
								
								//INSERT WEIGHTING METHOD
								int distance = StringUtils.getLevenshteinDistance(lmaString, mbString);
								
								
								//System.out.println("TEST " + i + ", distance" + distance + " | " + foafName + " [" + lmaSongNameTextRaw + " | " + lmaString + "][" + mbString + " | " + mbUniqueTitleRaw + "]");
								if(distance<=3){
									weightMap.put(mbWorkURI,distance);	
								}
							}
							
							if(weightMap.size()>0){
								int minValueInMap=(Collections.min(weightMap.values()));  // This will return min value in the Hashmap
								
								List<String> SuggestedMBUris = new ArrayList<String>();
								
						        for (Entry<String, Integer> entry : weightMap.entrySet()) {  // Iterate through hashmap
						            if (entry.getValue()==minValueInMap) {
						            	SuggestedMBUris.add(entry.getKey());
						            }
						        }
						        /*if(SuggestedMBUris.size()==0){
						        	lmaSongName.setAttribute("MBlinkAttempt", "insufficient");
						        	dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
									System.out.println("Out of threshold " + i + ": " + foafName + " | " + lmaSongNameTextRaw);
								    logger.error("Out of threshold " + i + ": " + foafName + " | " + lmaSongNameTextRaw);
						        }else*/ 
						        if(SuggestedMBUris.size()==1){
						        	lmaSongName.setAttribute("MBlinkAttempt", "approximated");
						        	lmaSongName.setAttribute("dist", String.valueOf(minValueInMap));
						        	lmaSongName.setAttribute("suggestedMBID", SuggestedMBUris.get(0));
						        	dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
						        	//dbman.updateSourceFile("data/test-songs.xml",LMADoc);
						        	System.out.println("SIM " + i + ": dist=" + minValueInMap + " | " + foafName + " [" + lmaSongNameTextRaw + " | " + decMap.get(SuggestedMBUris.get(0)) + "]");
						        	logger.info("SIM " + i + ": dist=" + minValueInMap + " | " + foafName + " [" + lmaSongNameTextRaw + " | " + decMap.get(SuggestedMBUris.get(0)) + "]");
								    //logger.info("SIM " + i + ": dist=" + minValueInMap + " | " + foafName + " [" + lmaSongNameTextRaw + " | " + lmaString + "][" + mbString + " | " + mbUniqueTitleRaw + "]");
						        }else{
						        	lmaSongName.setAttribute("MBlinkAttempt", "ambiguous");
						        	lmaSongName.setAttribute("suggestedMBID", "");
						        	dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
						        	//dbman.updateSourceFile("data/test-songs.xml",LMADoc);
						        	System.out.println("Ambiguous " + i + ": " + foafName + " | " + lmaSongNameTextRaw);
								    logger.error("Ambiguous " + i + ": " + foafName + " | " + lmaSongNameTextRaw);
						        }
								
							}else{
					        	lmaSongName.setAttribute("MBlinkAttempt", "insufficient");
					        	dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
					        	//dbman.updateSourceFile("data/test-songs.xml",LMADoc);
								System.out.println("Out of threshold " + i + ": " + foafName + " | " + lmaSongNameTextRaw);
							    logger.error("Out of threshold " + i + ": " + foafName + " | " + lmaSongNameTextRaw);
							}
							
							
							
						}

					}
				}
			}
		} catch (Exception e) {
			logger.error("error", e);
			System.out.println("Error happened.");
			// TODO: handle exception
		}

	}

}
