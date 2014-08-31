package pgtproject.cs.man.ac.uk.ld;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import pgtproject.cs.man.ac.uk.Constants;

public class LMA2LFMPerformanceLinker {
	private static Logger logger = Logger.getLogger(LMA2LFMPerformanceLinker.class);
	public static void main(String[] args) {
		Connection conn0 = null;
		PreparedStatement sql = null;
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();

			Class.forName("org.sqlite.JDBC");
			conn0 = DriverManager.getConnection(Constants.CONN_STRING);
			conn0.setAutoCommit(false);

			System.out.println("Getting Artist List...");

			String query = "SELECT distinct mbid FROM lma_performance";
			sql = conn0.prepareStatement(query);
			ResultSet res = sql.executeQuery();

			System.out.println("Query executed...");
			List<String> mbArtistList = new ArrayList<String>();
			while(res.next()){
				String mbid = res.getString("mbid");
				mbArtistList.add(mbid);
				System.out.print(".");
			}

			conn0.close();
			System.out.println("Artists with common concerts:" + mbArtistList.size());

			for (int i = 0; i < mbArtistList.size(); i++){
				String mbid = mbArtistList.get(i);
				Connection conn = null;
				System.out.println("LMA Artist" + i + ": " + mbid);
				try {
					Class.forName("org.sqlite.JDBC");
					conn = DriverManager.getConnection(Constants.CONN_STRING);
					conn.setAutoCommit(false);

					String qry = "INSERT INTO LMA_LFM_PERF_MAPPING(mbid,lmaPerformanceUri,lmaDate,lmaSongCheck,lfmEventUrl,lfmDate) " + 
							"SELECT lma.mbid, lma.lmaPerformanceUri, lma.date, lma.songCheck, lfm.eventUrl, lfm.eventDate " +
							"FROM lma_performance AS lma " +
							"JOIN lfm_event AS lfm " +
							"ON lma.mbid = lfm.mbid " +
							"WHERE lma.mbid = ? " +
							"AND substr(lma.date,1,4)||substr(lma.date,6,2)||substr(lma.date,9,2) = substr(lfm.eventDate,1,4)||substr(lfm.eventDate,6,2)||substr(lfm.eventDate,9,2)";			        

					sql = conn.prepareStatement(qry);
					sql.setString(1,mbid);
					sql.executeUpdate();

					sql.close();

					System.out.println();
					System.out.println("Processed " + i + ": " + mbid);
					//conn.close();
					conn.commit();

				} catch (Exception e) {
					logger.error("FAILED " + i + ": " + mbid,e);
					System.out.println("FAILED " + i + ": " + mbid);
					try {
						conn.rollback();
					}catch(Exception ex){logger.error("FAILED ROLLBACK" + i + ": " + mbid,e);}
					throw new RuntimeException(e);
				}

				conn.close();
				logger.info("Processed " + i + ": " + mbid);
			}

			long endTime1 = System.nanoTime();
			long endTime2 = System.currentTimeMillis();

			System.out.println("That took " + (endTime1 - startTime1));
			System.out.println("That took " + (endTime2 - startTime2) + " milliseconds");
			logger.info("That took " + (endTime2 - startTime2) + " milliseconds");

			System.out.println("XML file updated successfully");
			System.out.println("END.");

		} catch (Exception e) {
			logger.error("Unexpected error: ", e);
			e.printStackTrace();
		}


	}

}
