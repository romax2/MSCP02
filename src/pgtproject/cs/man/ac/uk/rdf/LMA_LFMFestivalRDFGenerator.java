package pgtproject.cs.man.ac.uk.rdf;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.apache.log4j.Logger;

import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.RandomGUID;

public class LMA_LFMFestivalRDFGenerator {
	private static Logger logger = Logger.getLogger(LMA_LFMFestivalRDFGenerator.class);

	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement sql = null;
		try {
			long startTime = System.currentTimeMillis();
			
			
			Class.forName("org.sqlite.JDBC");
	        conn = DriverManager.getConnection(Constants.CONN_STRING);
	        conn.setAutoCommit(false);

	        System.out.println("Executing query...");
	        String query = "select distinct mp.lmaPerformanceUri,mp.lfmEventUrl, mp.mbid, mp.lmaDate, le.title, le.description, headliner, venueName " +   
	        				"from lma_lfm_perf_mapping mp join lfm_event le on mp.lfmEventUrl = le.eventUrl " +
	        				"where lfmEventUrl like '%/festival/%'"; 
	        
	        
	        
 
			sql = conn.prepareStatement(query);

			ResultSet res = sql.executeQuery();
			
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("rdf-results/LastFMFestivalLinks.rdf")), "UTF-8"));
			int i = 1;
			while(res.next()){
				
				
				String query2 = "select mp.lmaPerformanceUri,mp.lfmEventUrl, mp.mbid, mp.lmaDate, le.title, le.description, headliner, venueName " +   
								"from lma_lfm_perf_mapping mp join lfm_event le on mp.lfmEventUrl = le.eventUrl " +
								"where lfmEventUrl= ?"; 
				
				
				String lfmFestivalUrl = res.getString("lfmEventUrl");
				String subEventList = "";

				sql = conn.prepareStatement(query2);
				sql.setString(1,lfmFestivalUrl);
				ResultSet rs = sql.executeQuery();
				while(rs.next()){
					subEventList = subEventList + "<" + rs.getString("lmaPerformanceUri") + ">,";
				}
				subEventList = subEventList.substring(0, subEventList.length()-1);
				
				RandomGUID guid = new RandomGUID();
				String guidstr = guid.toString().toLowerCase();
				pw.println("");
				//pw.println("<" + res.getString("lmaPerformanceUri") + "/slfm-sim> a mo:Festival ;" + guid.toString());
				pw.println("<http://etree.linkedmusic.org/festival/" + guidstr.substring(1,guidstr.length()-1) + "> a mo:Festival ;");
				pw.println("rdfs:seeAlso <" + lfmFestivalUrl + "> ;");
				pw.println("etree:date " + res.getString("lmaDate") + " ;");
				pw.println("event:hasSubEvent " + subEventList + " ;");
				pw.println("skos:prefLabel \"" + res.getString("title") + "\" ;");
				pw.println("etree:description \"\"\"" + res.getString("description") + "\"\"\" ;");
				pw.println("mo:headliner \"" + res.getString("headliner") + "\" ;");
				pw.println("event:place \"" + res.getString("venueName") + "\" ;");
				

				System.out.println(i);
				i++;
			}
	        
			pw.close();
			conn.close();

			long endTime = System.currentTimeMillis();
			
			long timeTaken = endTime - startTime;
			
			int seconds = (int) (timeTaken / 1000) % 60 ;
			int minutes = (int) ((timeTaken / (1000*60)) % 60);
			int hours   = (int) ((timeTaken / (1000*60*60)) % 24);
			
			System.out.println("That took " + hours+":"+minutes+":"+seconds);
			logger.info("That took " + hours+":"+minutes+":"+seconds);

			System.out.println("END.");
			
		} catch (Exception e) {
			logger.error("Unexpected error: ", e);
			e.printStackTrace();
		}

	}

}
