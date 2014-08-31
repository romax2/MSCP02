package pgtproject.cs.man.ac.uk.rdf;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import pgtproject.cs.man.ac.uk.Constants;

public class LMA_SFMSongRDFGenerator {
	private static Logger logger = Logger.getLogger(LMA_SFMSongRDFGenerator.class);
	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement sql = null;
		try {
			long startTime = System.currentTimeMillis();
			
			
			Class.forName("org.sqlite.JDBC");
	        conn = DriverManager.getConnection(Constants.CONN_STRING);
	        conn.setAutoCommit(false);

	        System.out.println("Executing query...");
	        String query = "SELECT m.lmaSongUri,m.lmaSongName,a.url,m.sfmSongName,m.method FROM ls_song_map m JOIN sfm_setlist s ON m.sfmSetlistUrl = s.setlistUrl JOIN sfm_artist a ON s.mbid = a.mbid";
	        
			sql = conn.prepareStatement(query);
			
			ResultSet res = sql.executeQuery();
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("rdf-results/SongLinks.rdf")), "UTF-8"));
			int i = 1;
			while(res.next()){
				
				String method = "";
				if(res.getString("method").equalsIgnoreCase("LevenZero")){
					method = "etree:simpleSongSFMMatch";
				}else if(res.getString("method").equalsIgnoreCase("LevenDyn")){
					method = "etree:simpleDynLevSongSFMMatch";
				}else if(res.getString("method").equalsIgnoreCase("SFMInsideLMA")){
					method = "etree:simpleSFMinLMASongMatch";
				}else if(res.getString("method").equalsIgnoreCase("LMAInsideSFM")){
					method = "etree:simpleLMAinSFMSongMatch";
				}/*else if(res.getString("method").equalsIgnoreCase("LevenHalf")){
					method = "simpleHalfLevSongSFMMatch";
				}*/
				
				
				String sfmSongUrl = res.getString("url").replace("setlists", "stats/songs");
				sfmSongUrl = sfmSongUrl + "?song=" + URLEncoder.encode(res.getString("sfmSongName"), "UTF-8");
				
				pw.println("");
				pw.println("<" + res.getString("lmaSongUri") + "/slfm-sim> a sim:Similarity ;");
				pw.println("sim:method " + method + " ;");
				pw.println("sim:object <" + sfmSongUrl + "> ;");
				pw.println("sim:subject <" + res.getString("lmaSongUri") + "> ;");
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
