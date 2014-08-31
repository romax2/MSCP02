package pgtproject.cs.man.ac.uk.lastfm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Event;
import de.umass.lastfm.PaginatedResult;
import de.umass.lastfm.Venue;
import pgtproject.cs.man.ac.uk.Constants;



public class LFMEventDBImporter {
	private static Logger logger = Logger.getLogger(LFMEventDBImporter.class);

	public static void main(String[] args) {
		Connection conn0 = null;
		PreparedStatement sql = null;
		try {
			long startTime = System.currentTimeMillis();

			Class.forName("org.sqlite.JDBC");
			conn0 = DriverManager.getConnection(Constants.CONN_STRING);
			conn0.setAutoCommit(false);

			String query = "select distinct mbid from lfm_artist where populated = \'false\'  order by name";
			sql = conn0.prepareStatement(query);
			ResultSet rs = sql.executeQuery();
			List<String> mbArtistList = new ArrayList<String>();
			while(rs.next()){
				String mbid = rs.getString("mbid");
				mbArtistList.add(mbid);
			}

			System.out.println("Artists collected:" + mbArtistList.size());
			logger.info("Artists collected:" + mbArtistList.size());

			conn0.close();

			PreparedStatement ps = null;

			for (int i = 0; i < mbArtistList.size(); i++){
				String mbid = mbArtistList.get(i);
				boolean hasEvents = false;
				Connection conn = null;
				System.out.println("Processing " + mbid);
				System.out.println("");
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:C:\\DissertationDB\\MScProjectDB.s3db");
				conn.setAutoCommit(false);
				try{

					PaginatedResult<Event> pastEvents = Artist.getPastEvents(mbid,false,1,2000,Constants.LASTFM_KEY);	
					Collection<Event> e = pastEvents.getPageResults();
					System.out.println(" (" + e.size() + ")");

					ps = conn.prepareStatement("INSERT INTO LFM_EVENT(eventId, mbid, eventUrl, eventDate, webSite, headliner, title, description, venueId, city, country, venueLatitude, venueLongitude, venueName, venuePhone, venuePostal, venueStreet, venueTimezone, venueUrl, venueWebsite) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

					if(e.size()>0){
						for (Iterator<Event> iterator = e.iterator(); iterator.hasNext();) {
							Event event = iterator.next();
							int eventId = -1;

							System.out.print(".");
							try{eventId = event.getId();}catch(Exception ex){eventId=-1; /*logger.info("no lfmEventId");*/}

							String eventUrl = event.getUrl();//
							Date date = event.getStartDate();
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String eventDate = df.format(date);//

							String webSite = event.getWebsite();//
							String headLiner = event.getHeadliner();//
							String title = event.getTitle();//
							String description = event.getDescription();//

							Venue venue = event.getVenue();

							String venueId = null;
							String city = null;
							String country = null;
							float venueLatitude = -1000;
							float venueLongitude = -1000;
							String venueName = null;
							String venuePhone = null;
							String venuePostal = null;
							String venueStreet = null;
							String venueTimezone = null;
							String venueUrl = null;
							String venueWebsite = null;

							if(venue!=null){
								venueId = venue.getId();
								city = venue.getCity();
								country = venue.getCountry();

								try{venueLatitude = venue.getLatitude();}catch(Exception ex){venueLatitude=-1000;}
								try{venueLongitude = venue.getLongitude();}catch(Exception ex){venueLongitude=-1000;}
								venueName = venue.getName();
								venuePhone = venue.getPhonenumber();
								venuePostal = venue.getPostal();
								venueStreet = venue.getStreet();
								venueTimezone = venue.getTimezone();
								venueUrl = venue.getUrl();
								venueWebsite = venue.getWebsite();

							}

							if(eventId == -1){ps.setNull(1,Types.NULL);} else {ps.setInt(1, eventId);}
							ps.setString(2, mbid);
							ps.setString(3, eventUrl);
							if(eventDate == null){ps.setNull(4,Types.NULL);} else {ps.setString(4, eventDate);}
							if(webSite == null){ps.setNull(5,Types.NULL);} else {ps.setString(5, webSite);}
							if(headLiner == null){ps.setNull(6,Types.NULL);} else {ps.setString(6, headLiner);}
							if(title == null){ps.setNull(7,Types.NULL);} else {ps.setString(7, title);}
							if(description == null){ps.setNull(8,Types.NULL);} else {ps.setString(8, description);}
							if(venueId == null){ps.setNull(9,Types.NULL);} else {ps.setString(9, venueId);}
							if(city == null){ps.setNull(10,Types.NULL);} else {ps.setString(10, city);}
							if(country == null){ps.setNull(11,Types.NULL);} else {ps.setString(11, country);}
							if(venueLatitude == -1000){ps.setNull(12,Types.NULL);} else {ps.setFloat(12, venueLatitude);}
							if(venueLongitude == -1000){ps.setNull(13,Types.NULL);} else {ps.setFloat(13, venueLongitude);}
							if(venueName == null){ps.setNull(14,Types.NULL);} else {ps.setString(14, venueName);}
							if(venuePhone == null){ps.setNull(15,Types.NULL);} else {ps.setString(15, venuePhone);}
							if(venuePostal == null){ps.setNull(16,Types.NULL);} else {ps.setString(16, venuePostal);}
							if(venueStreet == null){ps.setNull(17,Types.NULL);} else {ps.setString(17, venueStreet);}
							if(venueTimezone == null){ps.setNull(18,Types.NULL);} else {ps.setString(18, venueTimezone);}
							if(venueUrl == null){ps.setNull(19,Types.NULL);} else {ps.setString(19, venueUrl);}
							if(venueWebsite == null){ps.setNull(20,Types.NULL);} else {ps.setString(20, venueWebsite);}

							ps.addBatch();
							hasEvents = true;

						}

						if(hasEvents){int[] updateCounts = ps.executeBatch();}else{logger.error("No events: " + mbid);};

						PreparedStatement ps3 = null;
						ps3 = conn.prepareStatement("UPDATE LFM_ARTIST SET populated = \'true\' where mbid = ?");
						ps3.setString(1,mbid);
						ps3.executeUpdate();
						conn.commit();
						System.out.println("Processed " + i);
						logger.info("PROCESSED Artist " + i + ": "+ mbid + ", Events: " + e.size());

					}else{logger.error("No events: " + mbid);};
					conn.close();
					Thread.sleep(340);

				}catch (Exception e) {

					PreparedStatement ps3 = null;
					ps3 = conn.prepareStatement("UPDATE LFM_ARTIST SET populated = \'error\' where mbid = ?");
					ps3.setString(1,mbid);
					ps3.executeUpdate();

					conn.commit();
					conn.close();
					logger.error("FAILED " + i + ": " + mbid,e);
					System.out.println("FAILED " + i + ": " + mbid);
					try {
						conn.rollback();
					}catch(Exception ex){
						logger.error("ERROR",e);
					}
				}finally {
					try {
						conn.close();
					} catch (Exception e) {}
					conn = null;
				}

			}
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
