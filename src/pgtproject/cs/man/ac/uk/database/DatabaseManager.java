package pgtproject.cs.man.ac.uk.database;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.bean.LMAArtist;
import pgtproject.cs.man.ac.uk.bean.LMAPerformance;
import pgtproject.cs.man.ac.uk.bean.LMASong;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Resource;

import fm.setlist.api.model.Artist;
import fm.setlist.api.model.Setlist;
import fm.setlist.api.model.Setlists;
import fm.setlist.api.model.Song;
import fm.setlist.api.model.Artists;

public class DatabaseManager {
	private static Logger logger = Logger.getLogger(DatabaseManager.class);


	/*****************************************************
  SELECT ?foafName ?skosPrefLabel ?lmaID ?MBID WHERE {
  ?lmaID rdf:type mo:MusicArtist .
  ?lmaID skos:prefLabel ?skosPrefLabel .
  ?lmaID foaf:name ?foafName .
  ?lmaID sim:subjectOf ?mbSIM .
  ?mbSIM sim:object ?MBID .
}
ORDER BY ASC(?foafName)
LIMIT 10 
	 */
	public ResultSet getArtistsFromLMA(){
		try{			
			String queryString = Constants.ALL_PREFIXES+
					"SELECT ?foafName ?skosPrefLabel ?lmaID ?MBID WHERE {\n"+
					"  ?lmaID rdf:type mo:MusicArtist .\n"+
					"  ?lmaID skos:prefLabel ?skosPrefLabel .\n"+
					"  ?lmaID foaf:name ?foafName .\n"+
					"  ?lmaID sim:subjectOf ?mbSIM .\n"+
					"  ?mbSIM sim:object ?MBID .\n"+
					"}\n"+
					"ORDER BY ASC(?foafName)\n";
			Query query = QueryFactory.create(queryString); 
			QueryExecution qExe = QueryExecutionFactory.sparqlService( Constants.LMA_SERVICE, query );
			ResultSet results = qExe.execSelect(); 
			return results;

		} finally {
			try {
			} catch (Exception e) {}
		}
	}



	public Artist getArtistFromSetlistFM(String MBID) throws JAXBException, IOException{
		Artist artist = null;
		try{
			URL url = new URL(Constants.SETLISTBASEURL + "/0.1/artist/" + MBID);
			JAXBContext ctxt = JAXBContext.newInstance(Artist.class);
			URLConnection conn = url.openConnection();
			Unmarshaller unmarshaller = ctxt.createUnmarshaller();
			artist = (Artist) unmarshaller.unmarshal(conn.getInputStream());
			return artist;
		}catch (JAXBException e) {
			System.out.println("JAXB");
			throw e;
		}catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}


	public List<Setlist> getSetlistFromSetlistFM(String MBID, String date) throws JAXBException, IOException{
		Setlists results = null;
		try{

			URL url = new URL(Constants.SETLISTBASEURL + "/0.1/search/setlists?artistMbid=" + MBID + "&date=" + date);
			JAXBContext ctxt = JAXBContext.newInstance(Setlists.class);
			URLConnection conn = url.openConnection();
			Unmarshaller unmarshaller = ctxt.createUnmarshaller();

			results = (Setlists) unmarshaller.unmarshal(conn.getInputStream());

			List<Setlist> setlists = results.getList();
			return setlists;
		}catch (JAXBException e) {
			throw e;
		}catch (IOException e) {
			throw e;
		}
	}


	public List<Setlist> getArtistSetlistsFromSetlistFM(String MBID) throws JAXBException, IOException{
		Setlists results = null;
		try{

			URL url = new URL(Constants.SETLISTBASEURL + "/0.1/search/setlists?artistMbid=" + MBID);
			JAXBContext ctxt = JAXBContext.newInstance(Setlists.class);
			URLConnection conn = url.openConnection();
			Unmarshaller unmarshaller = ctxt.createUnmarshaller();

			results = (Setlists) unmarshaller.unmarshal(conn.getInputStream());

			int total = results.getTotal();
			int pageSize = results.getItemsPerPage();

			int pages = (int) Math.ceil((double)total / (double)pageSize);

			System.out.println("total: " + total + ", pageSize: " + pageSize + ", pages: " + pages);

			List<Setlist> setlists = new ArrayList<Setlist>();

			for (int i = 1 ; i<=pages; i++){
				System.out.print(i + ", ");
				url = new URL(Constants.SETLISTBASEURL + "/0.1/search/setlists?artistMbid=" + MBID + "&p=" + i);
				ctxt = JAXBContext.newInstance(Setlists.class);
				conn = url.openConnection();
				unmarshaller = ctxt.createUnmarshaller();
				results = (Setlists) unmarshaller.unmarshal(conn.getInputStream());
				List<Setlist> pageSetlists = results.getList();

				setlists.addAll(pageSetlists);
			}
			return setlists;
		}catch (JAXBException e) {
			throw e;
		}catch (IOException e) {
			throw e;
		}
	}




	public ResultSet getArtistSetlistsFromLMA(String lmaArtistID){
		try{			
			String queryString = Constants.ALL_PREFIXES+
					"SELECT ?foafName ?skosPrefLabel ?lmaID ?MBID WHERE {\n"+
					"  ?lmaID rdf:type mo:MusicArtist .\n"+
					"  ?lmaID skos:prefLabel ?skosPrefLabel .\n"+
					"  ?lmaID foaf:name ?foafName .\n"+
					"  ?lmaID sim:subjectOf ?mbSIM .\n"+
					"  ?mbSIM sim:object ?MBID .\n"+
					"}\n"+
					"ORDER BY ASC(?foafName)\n"+
					"LIMIT 10\n";

			Query query = QueryFactory.create(queryString); //s2 = the query above
			QueryExecution qExe = QueryExecutionFactory.sparqlService( Constants.LMA_SERVICE, query );
			ResultSet results = qExe.execSelect(); 
			return results;

		} finally {
			try {
				//conn.close();
			} catch (Exception e) {}
		}
	}

	public LMAArtist getLmaArtist(String lmaArtistURI){
		LMAArtist artist = new LMAArtist();
		try{			
			String queryString = Constants.ALL_PREFIXES+
					"SELECT *\n"+
					"WHERE{\n"+
					"<" + lmaArtistURI + "> skos:prefLabel ?skosPrefLabel .\n"+
					"<" + lmaArtistURI + "> foaf:name ?foafName .\n"+
					"<" + lmaArtistURI + "> sim:subjectOf ?mbSIM .\n"+
					"?mbSIM sim:object ?MBID .\n"+
					"}\n";

			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService( Constants.LMA_SERVICE, query );
			ResultSet results = qExe.execSelect();

			while ( results.hasNext() ) {
				QuerySolution qs = results.next();

				artist.setFoafArtistName(qs.get("foafName").asLiteral().getString());
				artist.setLmaArtistURI(lmaArtistURI);
				artist.setMbArtistURI(qs.getResource("MBID").asResource().toString());
				artist.setSkosPrefLabel(qs.get("skosPrefLabel").asLiteral().getString());
				artist.setPerformances(getLmaPerformancesByLmaArtistURI(lmaArtistURI));
			}

			return artist;

		} finally {
			try {
				//conn.close();
			} catch (Exception e) {}
		}
	}







	public List<LMAPerformance> getLmaPerformancesByLmaArtistURI(String lmaArtistURI){
		List<LMAPerformance> lmaPerformances = new ArrayList<LMAPerformance>();
		try{			
			String queryString = Constants.ALL_PREFIXES+

					"SELECT ?performed ?prefPerformanceLabel ?date ?location ?name ?prefPlaceLabel ?lmaVenueID ?locLFMID ?locGEOID\n"+ 
					"WHERE {\n"+ 
					"<" + lmaArtistURI + "> mo:performed ?performed.\n"+ 

							"?performed etree:date ?date .\n"+ 
							"OPTIONAL{?performed  skos:prefLabel ?prefPerformanceLabel .\n"+ 
							"?performed event:place ?lmaVenueID .\n"+ 
							"?lmaVenueID etree:location ?location .\n"+ 
							"?lmaVenueID etree:name ?name .\n"+ 
							"?lmaVenueID skos:prefLabel ?prefPlaceLabel .}\n"+ 

							"OPTIONAL{?lmaVenueID sim:subjectOf ?subjectOf_LastFM .\n"+ 
							"?subjectOf_LastFM sim:method etree:simpleLastfmMatch .\n"+ 
							"?subjectOf_LastFM sim:object ?locLFMID .}\n"+ 

							"OPTIONAL{?lmaVenueID sim:subjectOf ?subjectOf_GN .\n"+ 
							"?subjectOf_GN sim:method etree:simpleGeoAndLastfmMatch .\n"+ 
							"?subjectOf_GN sim:object ?locGEOID .}\n"+ 
							"}\n"; 

			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService( Constants.LMA_SERVICE, query );
			ResultSet results = qExe.execSelect();

			while ( results.hasNext() ) {
				QuerySolution qs = results.next();
				LMAPerformance performance = new LMAPerformance();

				if(qs.get("date")!=null){
					performance.setDate(qs.get("date").asLiteral().getString());
				}

				if(qs.getResource("locGEOID")!=null){
					performance.setGeonamesLocationURI(qs.getResource("locGEOID").asResource().toString());
				}
				if(qs.getResource("locLFMID")!=null){
					performance.setLastfmLocationURI(qs.getResource("locLFMID").asResource().toString());
				}
				if(qs.getResource("performed")!=null){
					performance.setLmaPerformanceURI(qs.getResource("performed").asResource().toString());
				}
				if(qs.getResource("lmaVenueID")!=null){
					performance.setLmaVenueURI(qs.getResource("lmaVenueID").asResource().toString());
				}
				if(qs.get("location")!=null){
					performance.setLocation(qs.get("location").asLiteral().getString());
				}
				if(qs.get("name")!=null){
					performance.setPlaceName(qs.get("name").asLiteral().getString());
				}
				if(qs.get("prefPerformanceLabel")!=null){
					performance.setPrefPerformanceLabel(qs.get("prefPerformanceLabel").asLiteral().getString());
				}
				if(qs.get("prefPlaceLabel")!=null){
					performance.setPrefPlaceLabel(qs.get("prefPlaceLabel").asLiteral().getString());
				}


				performance.setSongs(getSongsByLmaPerformanceURI(qs.getResource("performed").asResource().toString()));

				lmaPerformances.add(performance);
			}

			return lmaPerformances;

		} finally {
			try {
				//conn.close();
			} catch (Exception e) {}
		}
	}


	public List<LMAPerformance> getLmaPerformancesByLmaArtistURI2(String lmaArtistURI){
		List<LMAPerformance> lmaPerformances = new ArrayList<LMAPerformance>();
		try{			
			String queryString = Constants.ALL_PREFIXES+

					"SELECT ?performed ?prefPerformanceLabel ?date ?location ?name ?prefPlaceLabel ?lmaVenueID ?locLFMID ?locGEOID\n"+ 
					"WHERE {\n"+ 
					"<" + lmaArtistURI + "> mo:performed ?performed.\n"+ 

							"?performed etree:date ?date .\n"+ 
							"OPTIONAL{?performed  skos:prefLabel ?prefPerformanceLabel .\n"+ 
							"?performed event:place ?lmaVenueID .\n"+ 
							"?lmaVenueID etree:location ?location .\n"+ 
							"?lmaVenueID etree:name ?name .\n"+ 
							"?lmaVenueID skos:prefLabel ?prefPlaceLabel .}\n"+ 

							"OPTIONAL{?lmaVenueID sim:subjectOf ?subjectOf_LastFM .\n"+ 
							"?subjectOf_LastFM sim:method etree:simpleLastfmMatch .\n"+ 
							"?subjectOf_LastFM sim:object ?locLFMID .}\n"+ 

							"OPTIONAL{?lmaVenueID sim:subjectOf ?subjectOf_GN .\n"+ 
							"?subjectOf_GN sim:method etree:simpleGeoAndLastfmMatch .\n"+ 
							"?subjectOf_GN sim:object ?locGEOID .}\n"+ 
							"}\n"; 

			Query query = QueryFactory.create(queryString);
			QueryExecution qExe = QueryExecutionFactory.sparqlService( Constants.LMA_SERVICE, query );
			ResultSet results = qExe.execSelect();

			while ( results.hasNext() ) {
				QuerySolution qs = results.next();
				LMAPerformance performance = new LMAPerformance();

				if(qs.get("date")!=null){
					performance.setDate(qs.get("date").asLiteral().getString());
				}

				if(qs.getResource("locGEOID")!=null){
					performance.setGeonamesLocationURI(qs.getResource("locGEOID").asResource().toString());
				}
				if(qs.getResource("locLFMID")!=null){
					performance.setLastfmLocationURI(qs.getResource("locLFMID").asResource().toString());
				}
				if(qs.getResource("performed")!=null){
					performance.setLmaPerformanceURI(qs.getResource("performed").asResource().toString());
				}
				if(qs.getResource("lmaVenueID")!=null){
					performance.setLmaVenueURI(qs.getResource("lmaVenueID").asResource().toString());
				}
				if(qs.get("location")!=null){
					performance.setLocation(qs.get("location").asLiteral().getString());
				}
				if(qs.get("name")!=null){
					performance.setPlaceName(qs.get("name").asLiteral().getString());
				}
				if(qs.get("prefPerformanceLabel")!=null){
					performance.setPrefPerformanceLabel(qs.get("prefPerformanceLabel").asLiteral().getString());
				}
				if(qs.get("prefPlaceLabel")!=null){
					performance.setPrefPlaceLabel(qs.get("prefPlaceLabel").asLiteral().getString());
				}

				lmaPerformances.add(performance);
			}

			return lmaPerformances;

		} finally {
			try {
				//conn.close();
			} catch (Exception e) {}
		}
	}



	public List<LMASong> getSongsByLmaPerformanceURI(String lmaPerformanceURI){
		List<LMASong> lmaSongs = new ArrayList<LMASong>();
		try{			
			String queryString = Constants.ALL_PREFIXES+

					"SELECT ?songID ?trackNumber ?prefTrackLabel\n"+
					"WHERE {\n"+
					"<" + lmaPerformanceURI + "> event:hasSubEvent ?songID .\n"+
					"OPTIONAL {?songID etree:number ?trackNumber .}\n"+
					"OPTIONAL {?songID skos:prefLabel ?prefTrackLabel .}\n"+
					"}\n"+
					"ORDER BY ASC(?trackNumber)\n";

			Query query = QueryFactory.create(queryString); 
			QueryExecution qExe = QueryExecutionFactory.sparqlService( Constants.LMA_SERVICE, query );
			ResultSet results = qExe.execSelect();

			while ( results.hasNext() ) {
				QuerySolution qs = results.next();
				LMASong song = new LMASong();

				if(qs.getResource("songID")!=null){
					song.setLmaSongURI(qs.getResource( "songID" ).asResource().toString());
				}
				if(qs.get("prefTrackLabel")!=null){
					song.setPrefTrackLabel(qs.get("prefTrackLabel").asLiteral().getString());
				}
				if(qs.get("trackNumber")!=null){
					song.setTrackNumber(qs.get("trackNumber").asLiteral().getString());
				}
				lmaSongs.add(song);
			}

			return lmaSongs;

		} finally {
			try {
				//conn.close();
			} catch (Exception e) {}
		}
	}




	public void updateSourceFile(String filePath, Document doc) throws JAXBException, IOException{

		try{
			doc.getDocumentElement().normalize();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filePath));
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(source, result);
		}catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	//-----------------------------------------------------------------------------------------------------------------------------------------------

	public void insertLMAArtist(String lmaArtistUri, String mbArtistUri, String mbid, String foafName, String artistSkosPrefLabel) throws JAXBException, IOException{
		PreparedStatement ps = null;
		Connection conn = getConnection();
		try{
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			ps = conn.prepareStatement("INSERT INTO LMA_ARTIST(lmaArtistUri, mbArtistUri, mbid, foafName, artistSkosPrefLabel) values (?,?,?,?,?)");
			ps.setString(1, lmaArtistUri);
			ps.setString(2, mbArtistUri);
			ps.setString(3, mbid);
			ps.setString(4, foafName);
			ps.setString(5, artistSkosPrefLabel);
			ps.executeUpdate();

			conn.commit();
			conn.setAutoCommit(autoCommit);

		} catch(SQLException e){
			logger.error(e);
			throw new RuntimeException(e);
		} finally {
			try {
				conn.close();
			} catch (Exception e) {}
			ps=null;
		}
	}


	public void insertLMAPerformance(String lmaPerformanceUri, String lmaArtistUri, String date, String prefPerformanceLabel, String lmaVenueURI, String location, String placeName, String prefPlaceLabel, String lastfmLocURI, String gnameLocURI) throws JAXBException, IOException{
		PreparedStatement ps = null;
		Connection conn = getConnection();
		try{
			boolean autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			ps = conn.prepareStatement("INSERT INTO LMA_PERFORMANCE(lmaPerformanceUri, lmaArtistUri, date, prefPerformanceLabel, lmaVenueURI, location, placeName, prefPlaceLabel, lastfmLocURI, geonameLocURI) values (?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, lmaPerformanceUri);
			ps.setString(2, lmaArtistUri);
			ps.setString(3, date);
			ps.setString(4, prefPerformanceLabel);
			ps.setString(5, lmaVenueURI);
			ps.setString(6, location);
			ps.setString(7, placeName);
			ps.setString(8, prefPlaceLabel);
			ps.setString(9, lastfmLocURI);
			ps.setString(10, gnameLocURI);
			ps.executeUpdate();

			conn.commit();
			conn.setAutoCommit(autoCommit);

		} catch(SQLException e){
			logger.error(e);
			throw new RuntimeException(e);
		} finally {
			try {
				conn.close();
			} catch (Exception e) {}
			ps=null;
		}
	}



	protected static Connection getConnection(){
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager.getConnection(Constants.CONN_STRING);
			return connection;
		} catch (Exception e) {
			logger.error("DB Connection error: ", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}



}
