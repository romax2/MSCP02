package pgtproject.cs.man.ac.uk.mbrainz;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

public class MBInfoCollector {
	private static Logger logger = Logger.getLogger(MBInfoCollector.class);

	public static void main(String[] args) {
		
		String MBID = null;
		
		try {
			DatabaseManager dbman = new DatabaseManager();
			File mbFile = new File("data/mbrainzdata.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document MBDoc = dBuilder.parse(mbFile);
			
			Element MBRoot = MBDoc.getDocumentElement();
			
			File artistFile = new File("data/lmaArtistsSimple4mbz.xml");
			Document LMADoc = dBuilder.parse(artistFile);
			
			NodeList lmaArtistList = LMADoc.getElementsByTagName("lmaArtist");
			System.out.println("Total of Artists: " + lmaArtistList.getLength());
			logger.info("Total of Artists: " + lmaArtistList.getLength());
			for (int i = 0; i < lmaArtistList.getLength(); i++){
				MBID =  null;
				Node node = lmaArtistList.item(i);
				Element lmaArtist = (Element)node;
				///ADD CONTROL IF
				if(lmaArtist.getAttribute("mbproc").isEmpty() || lmaArtist.getAttribute("mbproc").equals("")){
					
					MBID = lmaArtist.getAttribute("mbid");
					
					Artist artist = new Artist();
					artist.getIncludes().setWorks(true);
			        ArtistWs2 wsArtist = artist.getComplete(MBID);
			        List <WorkWs2> wsWorks = wsArtist.getWorks();
			        System.out.println("MBID:" + MBID +"|Works:" + wsWorks.size());
			        logger.info("MBID:" + MBID +"|Works:" + wsWorks.size());
			        
			        Element mbArtist = MBDoc.createElement("mbArtist");
			        mbArtist.setAttribute("mbid", wsArtist.getId());
			        
			        if(wsArtist.getIdUri()!=null && !wsArtist.getIdUri().isEmpty() && !wsArtist.getIdUri().equalsIgnoreCase("") && !wsArtist.getIdUri().equalsIgnoreCase("null")){
				        Element mbURI = MBDoc.createElement("mbURI");
				        mbURI.setTextContent(wsArtist.getIdUri());
				        mbArtist.appendChild(mbURI);
			        }
			        
			        if(wsArtist.getName()!=null && !wsArtist.getName().isEmpty() && !wsArtist.getName().equalsIgnoreCase("") && !wsArtist.getName().equalsIgnoreCase("null")){
				        Element mbName = MBDoc.createElement("mbName");
				        mbName.setTextContent(wsArtist.getName());
				        mbArtist.appendChild(mbName);
			        }
			        
			        if(wsArtist.getUniqueName()!=null && !wsArtist.getUniqueName().isEmpty() && !wsArtist.getUniqueName().equalsIgnoreCase("") && !wsArtist.getUniqueName().equalsIgnoreCase("null")){
				        Element mbUniqueName = MBDoc.createElement("mbUniqueName");
				        mbUniqueName.setTextContent(wsArtist.getUniqueName());
				        mbArtist.appendChild(mbUniqueName);
			        }
			        
			        if(wsArtist.getSortName()!=null && !wsArtist.getSortName().isEmpty() && !wsArtist.getSortName().equalsIgnoreCase("") && !wsArtist.getSortName().equalsIgnoreCase("null")){
				        Element mbSortName = MBDoc.createElement("mbSortName");
				        mbSortName.setTextContent(wsArtist.getSortName());
				        mbArtist.appendChild(mbSortName);
			        }
			        
			        if(wsArtist.getType()!=null && !wsArtist.getType().isEmpty() && !wsArtist.getType().equalsIgnoreCase("") && !wsArtist.getType().equalsIgnoreCase("null")){
				        Element mbType = MBDoc.createElement("mbType");
				        mbType.setTextContent(wsArtist.getType());
				        mbArtist.appendChild(mbType);
			        }
			        
			        if(wsWorks.size()>0){
				        Element mbWorks = MBDoc.createElement("mbWorks");
				        
			        	for(int k=0; k < wsWorks.size(); k++){
			        		WorkWs2 wsWork = wsWorks.get(k);
			        		
			        		Element mbWork = MBDoc.createElement("mbWork");
			        		mbWork.setAttribute("id", wsWork.getId());
			        		
			        		if(wsWork.getIdUri()!=null && !wsWork.getIdUri().isEmpty() && !wsWork.getIdUri().equalsIgnoreCase("") && !wsWork.getIdUri().equalsIgnoreCase("null")){
				        		Element mbWorkURI = MBDoc.createElement("mbWorkURI");
						        mbWorkURI.setTextContent(wsWork.getIdUri());
						        mbWork.appendChild(mbWorkURI);
			        		}
			        		
			        		if(wsWork.getUniqueTitle()!=null && !wsWork.getUniqueTitle().isEmpty() && !wsWork.getUniqueTitle().equalsIgnoreCase("") && !wsWork.getUniqueTitle().equalsIgnoreCase("null")){
				        		Element mbUniqueTitle = MBDoc.createElement("mbUniqueTitle");
						        mbUniqueTitle.setTextContent(wsWork.getUniqueTitle());
						        mbWork.appendChild(mbUniqueTitle);
					        }
					        
			        		if(wsWork.getTitle()!=null && !wsWork.getTitle().isEmpty() && !wsWork.getTitle().equalsIgnoreCase("") && !wsWork.getTitle().equalsIgnoreCase("null")){
				        		Element mbTitle = MBDoc.createElement("mbTitle");
						        mbTitle.setTextContent(wsWork.getTitle());
						        mbWork.appendChild(mbTitle);
					        }
					        
			        		if(wsWork.getType()!=null && !wsWork.getType().isEmpty() && !wsWork.getType().equalsIgnoreCase("") && !wsWork.getType().equalsIgnoreCase("null")){
						        Element mbWorkType = MBDoc.createElement("mbWorkType");
						        mbWorkType.setTextContent(wsWork.getType());
						        mbWork.appendChild(mbWorkType);
					        }
					        
			        		if(wsWork.getDisambiguation()!=null && !wsWork.getDisambiguation().isEmpty() && !wsWork.getDisambiguation().equalsIgnoreCase("") && !wsWork.getDisambiguation().equalsIgnoreCase("null")){
						        Element mbDisambiguation = MBDoc.createElement("mbDisambiguation");
						        mbDisambiguation.setTextContent(wsWork.getDisambiguation());
						        mbWork.appendChild(mbDisambiguation);
					        }
					        
					        List <AliasWs2> wsAliases = wsWork.getAliases();
			        		
	
			        		 if(wsAliases.size()>0){
			        			 
			        			 Element mbAliases = MBDoc.createElement("mbAliases");
			        			 
			        			 for(int l=0; l < wsAliases.size(); l++){
			        				 AliasWs2 wsAlias = wsAliases.get(l);
			        				Element mbAlias = MBDoc.createElement("mbAlias");
			        				
			        				if(wsAlias.getScript()!=null && !wsAlias.getScript().isEmpty() && !wsAlias.getScript().equalsIgnoreCase("") && !wsAlias.getScript().equalsIgnoreCase("null")){
				        				Element mbAliasScript = MBDoc.createElement("mbAliasScript");
				        				mbAliasScript.setTextContent(wsAlias.getScript());
				        				mbAlias.appendChild(mbAliasScript);
			        				}
			        				
			        				if(wsAlias.getType()!=null && !wsAlias.getType().isEmpty() && !wsAlias.getType().equalsIgnoreCase("") && !wsAlias.getType().equalsIgnoreCase("null")){
				        				Element mbAliasType = MBDoc.createElement("mbAliasType");
				        				mbAliasType.setTextContent(wsAlias.getType());
				        				mbAlias.appendChild(mbAliasType);
			        				}
			        				
			        				if(wsAlias.getValue()!=null && !wsAlias.getValue().isEmpty() && !wsAlias.getValue().equalsIgnoreCase("") && !wsAlias.getValue().equalsIgnoreCase("null")){
				        				Element mbAliasValue = MBDoc.createElement("mbAliasValue");
				        				mbAliasValue.setTextContent(wsAlias.getValue());
				        				mbAlias.appendChild(mbAliasValue);
			        				}
			        				
			        				mbAliases.appendChild(mbAlias);
			        			 }
			        			 mbWork.appendChild(mbAliases);
			        		 }
			        		 mbWorks.appendChild(mbWork);
			        	}
			        	mbArtist.appendChild(mbWorks);
			        }
			        MBRoot.appendChild(mbArtist);
			        lmaArtist.setAttribute("mbproc","true");
			        dbman.updateSourceFile("data/mbrainzdata.xml",MBDoc);
			        dbman.updateSourceFile("data/lmaArtistsSimple4mbz.xml",LMADoc);
			        System.out.println("Processed " + i + ", mbname:" + wsArtist.getName());
			        logger.info("Processed " + i + ", mbname:" + wsArtist.getName());
					 //System.out.println("Processed " + i + ": " + foafName);
				}
				
				Thread.sleep(1100);
			}

		} catch (Exception e) {
			logger.error("ERROR WITH MBID: " + MBID, e);
			// TODO: handle exception
		}
		
		
		
		
		
		
		// TODO Auto-generated method stub
		
		/*Artist artist = new Artist();
		artist.getSearchFilter().setLimit((long)30);
		artist.getSearchFilter().setMinScore((long)50);
		 
        artist.search("   PInk   fLoYd   ");
         List<ArtistResultWs2> results  =  artist.getFirstSearchResultPage();
         
         if (results.isEmpty()) return;
         
         ArtistWs2 pf = results.get(0).getArtist();
         
         artist = new Artist();
         ArtistWs2 pinkFloyd= artist.lookUp(pf);
         
         System.out.println("The actual name:" + pinkFloyd.getName());*/
         
         
         /*
         Artist artist = new Artist();
         artist.getIncludes().setWorks(true);
         ArtistWs2 wsArtist = artist.getComplete("65f4f0c5-ef9e-490c-aee3-909e7ae6b2ab");
         List <WorkWs2> wsWorks = wsArtist.getWorks();
         System.out.println("Total Works of this artist:" + wsWorks.size());
         if(wsWorks.size()>0){
        	 for(int i=0; i < wsWorks.size(); i++){
        		WorkWs2 wsWork = wsWorks.get(i);
        		System.out.println("Work " + i +"==> MBID: " + wsWork.getId() + " | name: " + wsWork.getUniqueTitle());
        		System.out.println("--------==> Disambiguation: " + wsWork.getDisambiguation() + " | title: " + wsWork.getTitle());
        		System.out.println("--------==> IDURI: " + wsWork.getIdUri() + " | ISWC: " + wsWork.getIswc());
        		System.out.println("--------==> Type: " + wsWork.getType());
        		List <AliasWs2> wsAliases = wsWork.getAliases();
        		 if(wsAliases.size()>0){
        			 String a = "";
        			 for(int j=0; j < wsAliases.size(); j++){
        				 AliasWs2 wsAlias = wsAliases.get(j);
        				 a = a + wsAlias.getValue() + "; ";
        			 }
        			 System.out.println("--------==> Aliases: " + a);
        		 }
        		 System.out.println("==========================================================================================================");
        		 
        	 }
         }
         System.out.println("The actual name:" + wsArtist.getName());*/
         
         
 

	}

}
