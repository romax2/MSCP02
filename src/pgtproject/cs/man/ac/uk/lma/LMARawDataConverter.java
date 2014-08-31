package pgtproject.cs.man.ac.uk.lma;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class LMARawDataConverter {
	private static Logger logger = Logger.getLogger(LMARawDataConverter.class);

	public static void main(String[] args) {

		try {
			DatabaseManager dbman = new DatabaseManager();
			File rawFile = new File("data/lma-raw-data.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document rawDoc = dBuilder.parse(rawFile);

			File cleanFile = new File("results/lma-clean-data.xml");
			Document cleanDoc = dBuilder.parse(cleanFile);
			Element cleanRoot = cleanDoc.getDocumentElement();

			NodeList resultList = rawDoc.getElementsByTagName("result");

			for (int i = 0; i < resultList.getLength(); i++) {
				Node node = resultList.item(i);
				Element eResult = (Element) node;
				if (eResult.getAttribute("redefined").isEmpty()	|| eResult.getAttribute("redefined").equals("")) {
					String slmaID = null;
					String sMBID = null;
					String sMBuri = null;
					String sfoafName = null;
					String sskosPrefLabel = null;
					// Iterate over binding nodes
					NodeList bindings = eResult.getElementsByTagName("binding");
					for (int j = 0; j < bindings.getLength(); j++) {
						try {
								Node binding = bindings.item(j);
								Element eBinding = (Element) binding;

								if (eBinding.getAttribute("name").equals("foafName")) {
									NodeList literal = eBinding.getElementsByTagName("literal");
									sfoafName = literal.item(0).getTextContent();
								}
								
								if (eBinding.getAttribute("name").equals("skosPrefLabel")) {
									NodeList literal = eBinding.getElementsByTagName("literal");
									sskosPrefLabel = literal.item(0).getTextContent();
								}

								if (eBinding.getAttribute("name").equals("lmaID")) {
									NodeList uri = eBinding.getElementsByTagName("uri");
									slmaID = uri.item(0).getTextContent();
								}

								if (eBinding.getAttribute("name").equals("MBID")) {
									NodeList uri = eBinding.getElementsByTagName("uri");
									sMBuri = uri.item(0).getTextContent();
									sMBID = sMBuri.replace("http://musicbrainz.org/artist/","");
									sMBID = sMBID.substring(0, sMBID.length() - 2);
								}
							
						} catch (Exception e) {
							eResult.setAttribute("redefined", "error");
							dbman.updateSourceFile("data/lma-raw-data.xml",rawDoc);
							logger.error("error with MBID: " + sMBID, e);
						}
					}
					
					
					try {
						Element lmaArtist = cleanDoc.createElement("lmaArtist");
						
						Element mbArtistUri = cleanDoc.createElement("mbArtistUri");
						mbArtistUri.setTextContent(sMBuri);
						lmaArtist.appendChild(mbArtistUri);
						
						Element lmaArtistUri = cleanDoc.createElement("lmaArtistUri");
						lmaArtistUri.setTextContent(slmaID);
						lmaArtist.appendChild(lmaArtistUri);
						
						Element foafName = cleanDoc.createElement("foafName");
						foafName.setTextContent(sfoafName);
						lmaArtist.appendChild(foafName);
						
						Element artistSkosPrefLabel = cleanDoc.createElement("artistSkosPrefLabel");
						artistSkosPrefLabel.setTextContent(sskosPrefLabel);
						lmaArtist.appendChild(artistSkosPrefLabel);
						
						lmaArtist.setAttribute("mbid", sMBID);
						cleanRoot.appendChild(lmaArtist);
						
						dbman.updateSourceFile("results/lma-clean-data.xml",cleanDoc);
						
						eResult.setAttribute("redefined", "true");
						dbman.updateSourceFile("data/lma-raw-data.xml",rawDoc);
						
						logger.info("Node " + i + " OK. MBID: " + sMBID + " | Artist: " + sfoafName);
						System.out.println("Node " + i + " OK. MBID: " + sMBID + " | Artist: " + sfoafName);
					} catch (Exception e) {
						eResult.setAttribute("redefined", "error");
						dbman.updateSourceFile("data/lma-raw-data.xml",rawDoc);
						logger.error("error with MBID: " + sMBID, e);
						System.out.println("error with MBID: " + sMBID);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Unexpected Error: ", e);
		}
	}

}
