package pgtproject.cs.man.ac.uk.rdf;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pgtproject.cs.man.ac.uk.Constants;

public class Test {
	private static Logger logger = Logger.getLogger(Test.class);

	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement sql = null;
		try {
			long startTime = System.currentTimeMillis();
			
			
			Class.forName("org.sqlite.JDBC");
	        conn = DriverManager.getConnection(Constants.CONN_STRING);
	        conn.setAutoCommit(false);

	        System.out.println("Executing query...");
	        String query = "SELECT lma.lmaArtistUri, sfm.url,lma.foafName,sfm.name FROM lma_artist AS lma JOIN sfm_artist AS sfm ON lma.mbid = sfm.mbid";
			sql = conn.prepareStatement(query);

			ResultSet res = sql.executeQuery();
			
			int i = 1;
			while(res.next()){
				
				String lmaName = res.getString("foafName");
				String sfmfName = res.getString("name");
				
				int distance = StringUtils.getLevenshteinDistance(nameCleaner(lmaName), nameCleaner(sfmfName));
				
				if (distance>0){
					//System.out.println("\""+ res.getString("lmaArtistUri") + "\"  d: " + distance);
					System.out.println("\""+ res.getString("lmaArtistUri") + "\",");
				}
			}
	        

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
	
	
	private static String nameCleaner(String rawName){

		String cleanName = rawName.toLowerCase();
		cleanName = cleanName.replaceAll("&amp;","and");
		cleanName = cleanName.replaceAll("&lt;"," ");
		cleanName = cleanName.replaceAll("&gt;"," ");
		cleanName = cleanName.replaceAll("%"," ");

		cleanName = cleanName.replaceAll("&","and");
		cleanName = cleanName.replaceAll(">"," ");
		cleanName = cleanName.replaceAll("<"," ");

		cleanName = cleanName.replaceAll("\\*"," ");
		cleanName = cleanName.replaceAll("#"," ");
		cleanName = cleanName.replaceAll("\\[", " ").replaceAll("\\]"," ");
		cleanName = cleanName.replaceAll("\\(", " ").replaceAll("\\)"," ");
		cleanName = cleanName.trim();
		cleanName = cleanName.trim().replaceAll("\\s+", " ");
		cleanName = Normalizer.normalize(cleanName, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		return cleanName;
	}
}
