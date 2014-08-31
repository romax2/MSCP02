package pgtproject.cs.man.ac.uk.mbrainz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.musicbrainz.Controller.Artist;
import org.musicbrainz.modelWs2.AliasWs2;
import org.musicbrainz.modelWs2.Entity.ArtistWs2;
import org.musicbrainz.modelWs2.Entity.WorkWs2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class CleanMBWorkLinker {
	private static Logger logger = Logger.getLogger(CleanMBWorkLinker.class);

	public static void main(String[] args) {
		try {
			DatabaseManager dbman = new DatabaseManager();
			File mbFile = new File("data/mbrainzdata.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document MBDoc = dBuilder.parse(mbFile);
			
			Element MBRoot = MBDoc.getDocumentElement();
			
			File artistFile = new File("data/lma-partial-with-songs.xml");
			Document LMADoc = dBuilder.parse(artistFile);
			
			NodeList lmaArtistList = LMADoc.getElementsByTagName("lmaArtist");
			System.out.println("Total of Artists: " + lmaArtistList.getLength());
			for (int i = 0; i < lmaArtistList.getLength(); i++){
				Node node = lmaArtistList.item(i);
				Element lmaArtist = (Element)node;
				///ADD CONTROL IF
				System.out.println(i);
				if(lmaArtist.getAttribute("mbsonglinked").isEmpty() || lmaArtist.getAttribute("mbsonglinked").equals("")){
					//System.out.println("--");
					
					String foafName = lmaArtist.getElementsByTagName("foafName").item(0).getTextContent();
					String MBID = lmaArtist.getAttribute("mbid");
					
					NodeList lmaSongs = lmaArtist.getElementsByTagName("song");
					//System.out.println(MBID);
					if (lmaSongs.getLength()>0){
						
						XPath xPath = XPathFactory.newInstance().newXPath();
						NodeList nodes = (NodeList)xPath.evaluate("//*[@id = '"+MBID+"']",MBDoc, XPathConstants.NODESET);
						Element MBArtist = (Element) nodes.item(0);
						System.out.println("MBID(MBDoc): " + MBArtist.getAttribute("id"));//MANUALLLLLL  mbsonglinked="notfoundinmbdata"
						//Element MBArtist = MBDoc.getElementById(MBID);
						NodeList MBWorks = MBArtist.getElementsByTagName("mbWork");
						
						if (MBWorks.getLength()>0){
								Map<String, String> wMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
							for (int k = 0; k < MBWorks.getLength(); k++){
								Node MBWorkNode = MBWorks.item(k);
								Element MBWork = (Element)MBWorkNode;
								String workUri = MBWork.getElementsByTagName("mbWorkURI").item(0).getTextContent();
								String workTitle = MBWork.getElementsByTagName("mbUniqueTitle").item(0).getTextContent();
								System.out.println("workTitle: " + workTitle);
								wMap.put(workTitle, workUri);
							}
							
							for (int l = 0; l < lmaSongs.getLength(); l++){
								Node lmaSongNode = lmaSongs.item(l);
								Element lmaSong = (Element)lmaSongNode;
								
								String lmaUri = lmaSong.getAttribute("id");
								Node nodePrefTrackLabel = lmaSong.getElementsByTagName("prefTrackLabel").item(0);
								if(nodePrefTrackLabel!=null){
									Element ePrefTrackLabel = (Element)nodePrefTrackLabel;
									String lmaPrefTrackLabel = ePrefTrackLabel.getTextContent();
									String cleanLmaName = lmaPrefTrackLabel.trim().replaceAll("\\s+", " ");
									String mbUri = wMap.get(cleanLmaName);
									
									if(mbUri!=null){
										System.out.println("mbUri(link): " + mbUri);
										PrintWriter pw = new PrintWriter(new FileOutputStream(new File("C:\\Proyectos\\LinkedDataEnv\\SetlistFMClient\\results\\MBSongsLinks.txt"), true));				
										pw.println("");
										pw.println("<" + lmaUri + "/mb-sim> a sim:Similarity ;");
										pw.println("sim:method etree:simpleWorkMusicBrainzMatch ;");
										pw.println("sim:object <" + mbUri + "> ;");
										pw.println("sim:subject <" + lmaUri + "> ;");
										pw.println("sim:weight \"1.0\"^^xsd:double ;");
										pw.println("prov:wasAttributedTo <http://etree.linkedmusic.org/person/mario-ramirez> .");
										pw.close();
										
										lmaSong.setAttribute("mblinked", "true");
										//dbman.updateSourceFile("data/lmaFullData4mbzlinks.xml",LMADoc);
									}else{
										lmaSong.setAttribute("mblinked", "false");
										//dbman.updateSourceFile("data/lmaFullData4mbzlinks.xml",LMADoc);
									}
								}else{
									lmaSong.setAttribute("mblinked", "noTrackLabel");
									//dbman.updateSourceFile("data/lmaFullData4mbzlinks.xml",LMADoc);
								}
							}
							
							NodeList lmaSongNames = lmaArtist.getElementsByTagName("songName");
							for (int m = 0; m < lmaSongNames.getLength(); m++){
								Node lmaSongNameNode = lmaSongNames.item(m);
								Element lmaSongName = (Element)lmaSongNameNode;
								String songName = lmaSongName.getTextContent().trim().replaceAll("\\s+", " ");
								System.out.println("songName: " + songName);
								String mbUri = wMap.get(songName);
								
								if(mbUri!=null){
									lmaSongName.setAttribute("suggestedMBID", mbUri);
									lmaSongName.setAttribute("MBlinkAttempt", "true");
									//dbman.updateSourceFile("data/lmaFullData4mbzlinks.xml",LMADoc);
								}else{
									lmaSongName.setAttribute("MBlinkAttempt", "notfound");
									//dbman.updateSourceFile("data/lmaFullData4mbzlinks.xml",LMADoc);
								}
							}
							
							lmaArtist.setAttribute("mbsonglinked","true");
					        dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
					        System.out.println("Processed " + i + ", name:" + foafName);
					        logger.info("Processed " + i + ", name:" + foafName);

							 //System.out.println("Processed " + i + ": " + foafName);
							
							
						}else{
							lmaArtist.setAttribute("mbsonglinked","nombworks");
							dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
							 System.out.println("No MB Works " + i + ", name:" + foafName);
						     logger.info("No MB Works " + i + ", name:" + foafName);
						}
					}else{
						lmaArtist.setAttribute("mbsonglinked","nolmasongs");
						dbman.updateSourceFile("data/lma-partial-with-songs.xml",LMADoc);
						System.out.println("No LMA Songs " + i + ", name:" + foafName);
					    logger.info("No LMA Songs " + i + ", name:" + foafName);
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
