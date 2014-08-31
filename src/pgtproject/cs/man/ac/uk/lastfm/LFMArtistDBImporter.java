package pgtproject.cs.man.ac.uk.lastfm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.umass.lastfm.Artist;
import pgtproject.cs.man.ac.uk.Constants;



public class LFMArtistDBImporter {
	private static Logger logger = Logger.getLogger(LFMArtistDBImporter.class);

	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement sql = null;
		try {
			long startTime = System.currentTimeMillis();

			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(Constants.CONN_STRING);
			conn.setAutoCommit(false);

			String query = "select distinct mbid from lma_artist";
			sql = conn.prepareStatement(query);
			ResultSet rs = sql.executeQuery();
			List<String> mbArtistList = new ArrayList<String>();
			while(rs.next()){
				String mbid = rs.getString("mbid");
				mbArtistList.add(mbid);
			}

			System.out.println("Artists collected:" + mbArtistList.size());
			logger.info("Artists collected:" + mbArtistList.size());

			PreparedStatement ps = null;
			for (int i = 0; i < mbArtistList.size(); i++){
				String mbid = mbArtistList.get(i);

				try{
					Artist artist = Artist.getInfo(mbid, Constants.LASTFM_KEY);


					if(artist!=null){
						String name = artist.getName();
						String url = artist.getUrl();
						String lfmId = artist.getId();

						ps = conn.prepareStatement("INSERT INTO LFM_ARTIST(mbid, name, lfmUrl, lfmId) values (?,?,?,?)");

						ps.setString(1, mbid);
						if(name == null){ps.setNull(2,Types.NULL);} else {ps.setString(2, name);}
						if(url == null){ps.setNull(3,Types.NULL);} else {ps.setString(3, url);}
						if(lfmId == null){ps.setNull(4,Types.NULL);} else {ps.setString(4, lfmId);}
						ps.executeUpdate();
						conn.commit();
						System.out.println("Inserted " + i);
					}else{
						logger.error("NOT FOUND in LastFM: " + mbid);
					}
				}catch(Exception e){
					logger.error("Not found in LastFM: " + mbid);
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

}
