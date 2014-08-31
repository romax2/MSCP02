package pgtproject.cs.man.ac.uk.rdf;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import pgtproject.cs.man.ac.uk.Constants;

public class LMA_LFMArtistRDFGenerator {
	private static Logger logger = Logger.getLogger(LMA_LFMArtistRDFGenerator.class);

	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement sql = null;
		try {
			long startTime = System.currentTimeMillis();
			
			
			Class.forName("org.sqlite.JDBC");
	        conn = DriverManager.getConnection(Constants.CONN_STRING);
	        conn.setAutoCommit(false);

	        System.out.println("Executing query...");
	        String query = "SELECT lma.lmaArtistUri, lfm.lfmUrl FROM lma_artist AS lma JOIN lfm_artist AS lfm ON lma.mbid = lfm.mbid";
			sql = conn.prepareStatement(query);

			ResultSet res = sql.executeQuery();
			
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("rdf-results/LastFMArtistLinks.rdf")), "UTF-8"));
			int i = 1;
			while(res.next()){
				pw.println("");
				pw.println("<" + res.getString("lmaArtistUri") + "/slfm-sim> a sim:Similarity ;");
				pw.println("sim:method etree:simpleLastFMArtistMatch ;");
				pw.println("sim:object <" + res.getString("lfmUrl") + "> ;");
				pw.println("sim:subject <" + res.getString("lmaArtistUri") + "> ;");
				pw.println("sim:weight \"1.0\"^^xsd:double ;");
				pw.println("prov:wasAttributedTo <http://etree.linkedmusic.org/person/mario-ramirez> .");
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
