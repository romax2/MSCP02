package pgtproject.cs.man.ac.uk.lma;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import pgtproject.cs.man.ac.uk.bean.LMASong;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;
import fm.setlist.api.model.City;
import fm.setlist.api.model.Coords;
import fm.setlist.api.model.Country;
import fm.setlist.api.model.Set;
import fm.setlist.api.model.Setlist;
import fm.setlist.api.model.Song;
import fm.setlist.api.model.Venue;

public class LMASongDBInserter {
	private static Logger logger = Logger.getLogger(LMASongDBInserter.class);

	public static void main(String[] args) {
		Connection conn0 = null;
        //Statement statement = null;
		PreparedStatement sql = null;
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			
			Class.forName("org.sqlite.JDBC");
	        conn0 = DriverManager.getConnection("jdbc:sqlite:C:\\DissertationDB\\MScProjectDB.s3db");
	        conn0.setAutoCommit(false);
			
			//String query = "select distinct mbArtistId from mb_artist where hasWork = \'true\'";
	        String query = "select distinct mbid from lma_artist where songCheck = \'false\' order by foafName";
			sql = conn0.prepareStatement(query);
			//sql.setString(1,this.activityUuid);
			ResultSet res = sql.executeQuery();
			List<String> mbArtistList = new ArrayList<String>();
			while(res.next()){
				String mbid = res.getString("mbid");
				mbArtistList.add(mbid);
			}
	        
			conn0.close();
			System.out.println("Info collected:" + mbArtistList.size());
			
			for (int i = 0; i < mbArtistList.size(); i++){
				String mbid = mbArtistList.get(i);
				boolean hasSongs = false;
				Connection conn = null;
				
				Class.forName("org.sqlite.JDBC");
		        conn = DriverManager.getConnection("jdbc:sqlite:C:\\DissertationDB\\MScProjectDB.s3db");
		        conn.setAutoCommit(false);
				
				//String query = "select distinct mbArtistId from mb_artist where hasWork = \'true\'";
		        String qry = "select distinct lmaPerformanceUri, songCheck from lma_Performance where mbid = ?";
				sql = conn.prepareStatement(qry);
				sql.setString(1,mbid);
				ResultSet rs = sql.executeQuery();
				//List<String> lmaPerformanceList = new ArrayList<String>();
				Map<String, String> lmaPerformanceMap = new HashMap<String, String>();
				while(rs.next()){
					String lmaPerformanceUri = rs.getString("lmaPerformanceUri");
					String songCheck = rs.getString("songCheck");
					
					lmaPerformanceMap.put(lmaPerformanceUri, songCheck);
					
				}
				System.out.println("Artist: " + mbid + ", PERFS: " + lmaPerformanceMap.size());
				logger.info("Artist: " + mbid + ", PERFS: " + lmaPerformanceMap.size());
				if(lmaPerformanceMap.size()>0){
					
					System.out.println("progress: ");
					int j = 1;
					for (Map.Entry<String, String> entry : lmaPerformanceMap.entrySet())
					{
						
						String lmaPerformanceUri = entry.getKey();
						String songCheck = entry.getValue();
						
						if(!songCheck.equals("true")){
							
							try {
								List<LMASong>songs = dbman.getSongsByLmaPerformanceURI(lmaPerformanceUri);
								
								if(songs.size()>0){
									PreparedStatement ps = null;
									ps = conn.prepareStatement("INSERT INTO LMA_SONG(lmaSongUri, lmaPerformanceUri, trackNumber, prefTrackLabel) values (?,?,?,?)");
									
									for (LMASong sng : songs) {
										
										String lmaSongUri = sng.getLmaSongURI();
										String trackNumber = sng.getTrackNumber();
										String prefTrackLabel = sng.getPrefTrackLabel();
										
										if(lmaSongUri == null){ps.setNull(1,Types.NULL);} else {ps.setString(1, lmaSongUri);}
										ps.setString(2, lmaPerformanceUri);
										if(trackNumber == null){ps.setNull(3,Types.NULL);} else {ps.setString(3, trackNumber);}
										if(prefTrackLabel == null){ps.setNull(4,Types.NULL);} else {ps.setString(4, prefTrackLabel);}
										ps.addBatch();
									}
									
									int[] updateCounts = ps.executeBatch();
									//if(hasSongs){int[] updateCounts2 = ps2.executeBatch();}else{logger.error("No songs: " + mbid);};
									
									PreparedStatement ps3 = null;
									ps3 = conn.prepareStatement("UPDATE LMA_PERFORMANCE SET songCheck = \'true\' where lmaPerformanceUri = ?");
									ps3.setString(1,lmaPerformanceUri);
									ps3.executeUpdate();
									
									conn.commit();
									ps.close();
								}else{
									PreparedStatement ps = null;
									ps = conn.prepareStatement("UPDATE LMA_PERFORMANCE SET songCheck = \'nosongs\' where lmaPerformanceUri = ?");
									ps.setString(1,lmaPerformanceUri);
									ps.executeUpdate();
									
									conn.commit();
									
									logger.error(i + ": NO SONGS FOR PERFORMANCE " + lmaPerformanceUri);
									//conn.close();
									ps.close();
								}
							} catch (Exception e) {
								logger.error(i + ": ERROR FOR PERFORMANCE " + lmaPerformanceUri, e);
								System.out.println(i + ": ERROR FOR PERFORMANCE " + lmaPerformanceUri);
								try {
									conn.rollback();
									PreparedStatement ps = null;
									ps = conn.prepareStatement("UPDATE LMA_PERFORMANCE SET songCheck = \'error\' where lmaPerformanceUri = ?");
									ps.setString(1,lmaPerformanceUri);
									ps.executeUpdate();
									conn.commit();
									ps.close();
								}catch(Exception ex){
									logger.error("ERROR",e);
								}
							}
						}	
						
						System.out.print(j + ",");
						j++;
					}
					
					PreparedStatement ps = null;
					ps = conn.prepareStatement("UPDATE LMA_ARTIST SET songCheck = \'true\' where mbid = ?");
					ps.setString(1,mbid);
					ps.executeUpdate();
					
					conn.commit();
					System.out.println("Processed " + i + ": " + mbid);
					logger.info("Processed " + i + ": " + mbid);
					//conn.close();
					ps.close();
					
				}else{
					PreparedStatement ps = null;
					ps = conn.prepareStatement("UPDATE LMA_ARTIST SET songCheck = \'noperf\' where mbid = ?");
					ps.setString(1,mbid);
					ps.executeUpdate();
					conn.commit();
					logger.error("NO PERFORMANCES FOR ARTIST " + i + ": "+ mbid);
					//conn.close();
					ps.close();
				}
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
