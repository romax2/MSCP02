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
import fm.setlist.api.model.City;
import fm.setlist.api.model.Coords;
import fm.setlist.api.model.Country;
import fm.setlist.api.model.Set;
import fm.setlist.api.model.Setlist;
import fm.setlist.api.model.Song;
import fm.setlist.api.model.Venue;
import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.database.DatabaseManager;

public class SFMSongDataDBImporter {
	private static Logger logger = Logger.getLogger(SFMSongDataDBImporter.class);

	public static void main(String[] args) {
		Connection conn0 = null;
		PreparedStatement sql = null;
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();
			
			DatabaseManager dbman = new DatabaseManager();
			
			Class.forName("org.sqlite.JDBC");
	        conn0 = DriverManager.getConnection(Constants.CONN_STRING);
	        conn0.setAutoCommit(false);
			
	        String query = "select distinct mbid from sfm_artist where populated = \'false\'";
			sql = conn0.prepareStatement(query);
			ResultSet rs = sql.executeQuery();
			List<String> mbArtistList = new ArrayList<String>();
			while(rs.next()){
				String mbid = rs.getString("mbid");
				mbArtistList.add(mbid);
			}
	        
			conn0.close();
			System.out.println("Info collected:" + mbArtistList.size());
			
			PreparedStatement ps = null;
			PreparedStatement ps2 = null;
			for (int i = 0; i < mbArtistList.size(); i++){
				String mbid = mbArtistList.get(i);
				boolean hasSongs = false;
				Connection conn = null;
				try{
					
					List<Setlist> setlists = dbman.getArtistSetlistsFromSetlistFM(mbid);
					System.out.println("");
					Class.forName("org.sqlite.JDBC");
			        conn = DriverManager.getConnection(Constants.CONN_STRING);
			        conn.setAutoCommit(false);
					ps = conn.prepareStatement("INSERT INTO SFM_SETLIST(setlistId, mbid, date, setlistUrl,lfmEventId,info,tour,venueId,venueName,venueUrl,cityId,cityName,cityState,cityStateCode,cityLongitude,cityLatitude,countryName,countryCode) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
					ps2 = conn.prepareStatement("INSERT INTO SFM_SONG(setlistId, setlistUrl, songNumber, songName, songInfo, cover, with, setName, setEncore) values (?,?,?,?,?,?,?,?,?)");
			        
					if(setlists.size()>0){
						for (int j = 0; j < setlists.size(); j++){
							int lfmEventId;
							Double cityLatitude;
							Double cityLongitude;
							
							Setlist setlist = setlists.get(j);
							String date = setlist.getEventDate();
							String setlistId = setlist.getId();
							String info = setlist.getInfo();
							
							try{lfmEventId = setlist.getLastFmEventId();}catch(Exception e){lfmEventId=-1;}
							String tour = setlist.getTour();
							String setlistUrl = setlist.getUrl();
							
							Venue venue = setlist.getVenue();
							String venueId = venue.getId();
							String venueName = venue.getName();
							String venueUrl = venue.getUrl();
							
							City city = venue.getCity();
							String cityId = city.getId();
							String cityName = city.getName();
							String cityState = city.getState();
							String cityStateCode = city.getStateCode();
							
							Coords coords = city.getCoords();
							try{cityLatitude = coords.getLatitude(); }catch(Exception e){cityLatitude=-1D; logger.info("no cityLatitude");}
							try{cityLongitude = coords.getLongitude(); }catch(Exception e){cityLongitude=-1D; logger.info("no cityLongitude");}
							
							Country cityCountry = city.getCountry();
							String countryName = cityCountry.getName();
							String countryCode = cityCountry.getCode();
							
							ps.setString(1, setlistId);
							ps.setString(2, mbid);
							ps.setString(3, date);
							if(setlistUrl == null){ps.setNull(4,Types.NULL);} else {ps.setString(4, setlistUrl);}
							if(lfmEventId == -1){ps.setNull(5,Types.NULL);} else {ps.setInt(5, lfmEventId);}
							if(info == null){ps.setNull(6,Types.NULL);} else {ps.setString(6, info);}
							if(tour == null){ps.setNull(7,Types.NULL);} else {ps.setString(7, tour);}
							if(venueId == null){ps.setNull(8,Types.NULL);} else {ps.setString(8, venueId);}
							if(venueName == null){ps.setNull(9,Types.NULL);} else {ps.setString(9, venueName);}
							if(venueUrl == null){ps.setNull(10,Types.NULL);} else {ps.setString(10, venueUrl);}
							if(cityId == null){ps.setNull(11,Types.NULL);} else {ps.setString(11, cityId);}
							if(cityName == null){ps.setNull(12,Types.NULL);} else {ps.setString(12, cityName);}
							if(cityState == null){ps.setNull(13,Types.NULL);} else {ps.setString(13, cityState);}
							if(cityStateCode == null){ps.setNull(14,Types.NULL);} else {ps.setString(14, cityStateCode);}
							if(cityLongitude == -1D || cityLongitude == null){ps.setNull(15,Types.NULL);} else {ps.setDouble(15, cityLongitude);}
							if(cityLatitude == -1D || cityLatitude == null){ps.setNull(16,Types.NULL);} else {ps.setDouble(16, cityLatitude);}
							if(countryName == null){ps.setNull(17,Types.NULL);} else {ps.setString(17, countryName);}
							if(countryCode == null){ps.setNull(18,Types.NULL);} else {ps.setString(18, countryCode);}
							
							ps.addBatch();
							
							List<Set> sets = setlist.getSets();
							
							int songNumber = 1;
							for (int k = 0; k < sets.size(); k++){
								Set set = sets.get(k);
								String setName = set.getName();
								int setEncore;
								try{setEncore = set.getEncore();}catch(Exception e){setEncore=-1;}

								List<Song> songs = set.getSongs();
								for (int l = 0; l < songs.size(); l++){
									Song song = songs.get(l);
									String songName = song.getName();
									String songInfo = song.getInfo();
									String cover = null;
									String with = null;
									try{cover = song.getCover().getMbid();}catch(Exception e){}
									try{with = song.getWith().getMbid();}catch(Exception e){}
									
									ps2.setString(1, setlistId);
									if(setlistUrl == null){ps2.setNull(2,Types.NULL);} else {ps2.setString(2, setlistUrl);}
									ps2.setInt(3, songNumber);
									if(songName == null){ps2.setNull(4,Types.NULL);} else {ps2.setString(4, songName);}
									if(songInfo == null){ps2.setNull(5,Types.NULL);} else {ps2.setString(5, songInfo);}
									if(cover == null){ps2.setNull(6,Types.NULL);} else {ps2.setString(6, cover);}
									if(with == null){ps2.setNull(7,Types.NULL);} else {ps2.setString(7, with);}
									if(setName == null){ps2.setNull(8,Types.NULL);} else {ps2.setString(8, setName);}
									if(setEncore == -1){ps2.setNull(9,Types.NULL);} else {ps2.setInt(9, setEncore);}
									ps2.addBatch();
									songNumber++;
									hasSongs = true;
								}
							}
							
							System.out.print(j + ",");
						}	
						
						int[] updateCounts = ps.executeBatch();
						if(hasSongs){int[] updateCounts2 = ps2.executeBatch();}else{logger.error("No songs: " + mbid);};
						
						PreparedStatement ps3 = null;
						ps3 = conn.prepareStatement("UPDATE SFM_ARTIST SET populated = \'true\' where mbid = ?");
						ps3.setString(1,mbid);
						ps3.executeUpdate();
						
						conn.commit();
						System.out.println("Processed " + i);
						
						logger.info("PROCESSED Artist " + i + ": "+ mbid + ", Setlists: " + setlists.size());
						conn.close();
					}else{
						logger.error("NO SETLISTS: " + mbid);
						}
					
					
				}catch(FileNotFoundException e){
					logger.error("Not found in SFM: " + mbid);
					
				}catch (Exception e) {
					logger.error("FAILED " + i + ": " + mbid,e);
					System.out.println("FAILED " + i + ": " + mbid);
					try {
						conn.rollback();
					}catch(Exception ex){
						logger.error("ERROR",e);
					}
					throw new RuntimeException(e);
				}finally {
					try {
						conn.close();
					} catch (Exception e) {}
					conn = null;
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
