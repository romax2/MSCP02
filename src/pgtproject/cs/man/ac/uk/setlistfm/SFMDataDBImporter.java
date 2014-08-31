package pgtproject.cs.man.ac.uk.setlistfm;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import fm.setlist.api.model.Artist;
import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class SFMDataDBImporter {
	private static Logger logger = Logger.getLogger(SFMDataDBImporter.class);

	public static void main(String[] args) {
		Connection conn = null;
		PreparedStatement sql = null;
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();

			DatabaseManager dbman = new DatabaseManager();

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

			System.out.println("Info collected:" + mbArtistList.size());

			PreparedStatement ps = null;
			for (int i = 0; i < mbArtistList.size(); i++){
				String mbid = mbArtistList.get(i);

				try{
					Artist artist = dbman.getArtistFromSetlistFM(mbid);

					int tmid;
					String name = artist.getName();
					String sortName = artist.getSortName();
					String url = artist.getUrl();
					try{tmid = artist.getTmid();}catch(Exception e){tmid=-1; logger.equals("no tmid");}
					String disambiguation = artist.getDisambiguation();

					ps = conn.prepareStatement("INSERT INTO SFM_ARTIST(mbid, name, sortName, url, tmid, disambiguation) values (?,?,?,?,?,?)");

					ps.setString(1, mbid);
					if(name == null){ps.setNull(2,Types.NULL);} else {ps.setString(2, name);}
					if(sortName == null){ps.setNull(3,Types.NULL);} else {ps.setString(3, sortName);}
					if(url == null){ps.setNull(4,Types.NULL);} else {ps.setString(4, url);}
					if(tmid == -1){ps.setNull(5,Types.NULL);} else {ps.setInt(5, tmid);}
					if(disambiguation == null){ps.setNull(6,Types.NULL);} else {ps.setString(6, disambiguation);}
					ps.executeUpdate();
					conn.commit();
					System.out.println("Inserted " + i);

				}catch(FileNotFoundException e){
					logger.error("Not found in SFM: " + mbid);

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
