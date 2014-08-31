package pgtproject.cs.man.ac.uk.ld;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import pgtproject.cs.man.ac.uk.Constants;
import pgtproject.cs.man.ac.uk.bean.LMASongItem;
import pgtproject.cs.man.ac.uk.bean.LMA_SFMSongLink;
import pgtproject.cs.man.ac.uk.bean.SFMSongItem;

public class LMA2SFMSongLinkerFinal {
	private static Logger logger = Logger.getLogger(LMA2SFMSongLinkerFinal.class);

	public static void main(String[] args) {
		Connection conn0 = null;
		PreparedStatement sql = null;
		try {
			long startTime1 = System.nanoTime();
			long startTime2 = System.currentTimeMillis();

			Class.forName("org.sqlite.JDBC");
			conn0 = DriverManager.getConnection(Constants.CONN_STRING);
			conn0.setAutoCommit(false);

			System.out.println("Executing query...");

			String query = "SELECT lmaPerformanceUri, sfmSetlistUrl " + 
					"FROM lma_sfm_perf_mapping  " +
					"WHERE lmaSongCheck =\'true\' " +
					"AND sfmSetlistUrl IN (SELECT distinct setlistUrl FROM sfm_song) " +
					"AND songLinkedFinal = \'false\'" +
					"and lmaPerformanceUri = \'http://etree.linkedmusic.org/performance/eits2011-09-01.flac16\'";

			sql = conn0.prepareStatement(query);

			ResultSet res = sql.executeQuery();
			HashMap<String, String>performancesMap=new HashMap<String, String>();
			while(res.next()){
				String lmaPerformanceUri = res.getString("lmaPerformanceUri");
				String sfmSetlistUrl = res.getString("sfmSetlistUrl");
				performancesMap.put(lmaPerformanceUri, sfmSetlistUrl);
			}

			conn0.close();
			System.out.println("Performances with songs:" + performancesMap.size());
			logger.info("Performances with songs:" + performancesMap.size());
			int i = 1;
			for (Entry<String, String> entry : performancesMap.entrySet()) { 

				System.out.println("Processing " + i);

				String lmaPerf = entry.getKey();
				String sfmConcert = entry.getValue();

				Connection conn = null;

				try {
					Class.forName("org.sqlite.JDBC");
					conn = DriverManager.getConnection(Constants.CONN_STRING);
					conn.setAutoCommit(false);

					String qry = "SELECT songNumber, songName FROM sfm_song WHERE setlistURL = ? order by songNumber";
					sql = conn.prepareStatement(qry);
					sql.setString(1,sfmConcert);
					ResultSet rs = sql.executeQuery();
					Map<String, SFMSongItem>sfmSongs=new LinkedHashMap<String, SFMSongItem>(); 
					while(rs.next()){

						SFMSongItem sfmSongItem = new SFMSongItem();
						String songName = rs.getString("songName");
						sfmSongItem.setSfmSongNumber(rs.getInt("songNumber"));
						sfmSongItem.setSfmSongName(songName);

						sfmSongs.put(rs.getString("songNumber")+songName,sfmSongItem);
					}

					rs.close();
					sql.close();


					qry = "SELECT trackNumber, prefTrackLabel, lmaSongUri FROM lma_song WHERE lmaPerformanceUri = ? order by cast(trackNumber as integer)";
					sql = conn.prepareStatement(qry);
					sql.setString(1,lmaPerf);
					rs = sql.executeQuery();
					Map<String, LMASongItem>lmaSongs=new LinkedHashMap<String, LMASongItem>(); 

					while(rs.next()){
						String lmaSongUri = rs.getString("lmaSongUri");
						LMASongItem lmaSongItem = new LMASongItem();
						lmaSongItem.setLmaSongName(rs.getString("prefTrackLabel"));
						lmaSongItem.setLmaSongURI(lmaSongUri);
						lmaSongItem.setLmaTrackNumber(rs.getString("trackNumber"));

						lmaSongs.put(lmaSongUri,lmaSongItem);
					}

					System.out.println("Processing " + lmaPerf + " and " + sfmConcert + "["+ sfmSongs.size() + "]");
					logger.info("Processing: " + lmaPerf + " :and: " + sfmConcert + " ::["+ sfmSongs.size() + "]");

					List<LMA_SFMSongLink> songLinks = new ArrayList<LMA_SFMSongLink>();

					try {
						//------------------------------------------------------01 CHECK PERFECT MATCH---------------------------------------------------------

						for(Iterator<Map.Entry<String, LMASongItem>> lmaIter = lmaSongs.entrySet().iterator(); lmaIter.hasNext(); ) {
							Map.Entry<String, LMASongItem> lmaItem= lmaIter.next();
							String lmaSongUri = lmaItem.getKey();
							LMASongItem lmaSongItem= lmaItem.getValue();
							String lmaCleanSongName = nameCleaner(lmaSongItem.getLmaSongName());

							checkAnotherSong:
								for(Iterator<Map.Entry<String, SFMSongItem>> sfmIter = sfmSongs.entrySet().iterator(); sfmIter.hasNext(); ) {

									Map.Entry<String, SFMSongItem> sfmItem= sfmIter.next();
									SFMSongItem sfmSongItem = sfmItem.getValue();

									String sfmCleanSongName = nameCleaner(sfmSongItem.getSfmSongName());

									int distance = StringUtils.getLevenshteinDistance(lmaCleanSongName, sfmCleanSongName);

									if(distance == 0){
										LMA_SFMSongLink songLink = new LMA_SFMSongLink();
										songLink.setLmaSongURI(lmaSongUri);
										songLink.setLmaTrackNumber(lmaSongItem.getLmaTrackNumber());
										songLink.setLmaSongName(lmaSongItem.getLmaSongName());
										songLink.setSfmSongName(sfmSongItem.getSfmSongName());
										songLink.setSfmSongNumber(sfmSongItem.getSfmSongNumber());
										songLink.setDistance(distance);
										songLink.setMethod("LevenZero");

										songLinks.add(songLink);

										sfmIter.remove();
										lmaIter.remove();
										break checkAnotherSong;
									}
								}
						}

						//------------------------------------------------------02 CHECK DYN LEVENSHTEIN---------------------------------------------------------

						for(Iterator<Map.Entry<String, LMASongItem>> lmaIter = lmaSongs.entrySet().iterator(); lmaIter.hasNext(); ) {
							Map.Entry<String, LMASongItem> lmaItem= lmaIter.next();
							String lmaSongUri = lmaItem.getKey();
							LMASongItem lmaSongItem= lmaItem.getValue();
							String lmaCleanSongName = nameCleaner(lmaSongItem.getLmaSongName());

							int threshold;
							if(lmaCleanSongName.length()<=5){threshold = 2;} else if(lmaCleanSongName.length()>5&&lmaCleanSongName.length()<=10){threshold = 3;} else {threshold = 5;}
							HashMap<String, Integer>weightMap=new HashMap<String, Integer>();


							for(Iterator<Map.Entry<String, SFMSongItem>> sfmIter = sfmSongs.entrySet().iterator(); sfmIter.hasNext(); ) {

								Map.Entry<String, SFMSongItem> sfmItem= sfmIter.next();
								String sfmSongKey = sfmItem.getKey();
								SFMSongItem sfmSongItem = sfmItem.getValue();

								String sfmCleanSongName = nameCleaner(sfmSongItem.getSfmSongName());


								int distance = StringUtils.getLevenshteinDistance(lmaCleanSongName, sfmCleanSongName);


								if(distance<=threshold){
									weightMap.put(sfmSongKey,distance);	
								}
							}

							if(weightMap.size()>0){
								int minValueInMap=(Collections.min(weightMap.values()));  // This will return min value in the Hashmap

										List<String> SuggestedSFMName = new ArrayList<String>();

										for (Entry<String, Integer> entry1 : weightMap.entrySet()) {  // Iterate through hashmap
											if (entry1.getValue()==minValueInMap) {
												SuggestedSFMName.add(entry1.getKey());
											}
										}

										if(SuggestedSFMName.size()==1){

											SFMSongItem sfmSongItem = sfmSongs.get(SuggestedSFMName.get(0));

											LMA_SFMSongLink songLink = new LMA_SFMSongLink();
											songLink.setLmaSongURI(lmaSongUri);
											songLink.setLmaTrackNumber(lmaSongItem.getLmaTrackNumber());
											songLink.setLmaSongName(lmaSongItem.getLmaSongName());
											songLink.setSfmSongName(sfmSongItem.getSfmSongName());
											songLink.setSfmSongNumber(sfmSongItem.getSfmSongNumber());
											songLink.setDistance(minValueInMap);
											songLink.setMethod("LevenDyn");

											songLinks.add(songLink);

											sfmSongs.remove(SuggestedSFMName.get(0));
											lmaIter.remove();
										}								
							}
						}


						//------------------------------------------------------03 CHECK IF ONE OR SEVERAL SFM SONGS ARE CONTAINED IN ONE LMA SONG---------------------------------------------

						for(Iterator<Map.Entry<String, LMASongItem>> lmaIter = lmaSongs.entrySet().iterator(); lmaIter.hasNext(); ) {
							Map.Entry<String, LMASongItem> lmaItem= lmaIter.next();
							LMASongItem lmaSongItem= lmaItem.getValue();
							String lmaCleanSongName = nameCleaner(lmaSongItem.getLmaSongName());

							HashMap<String, Integer>sfmCorrespondences=new HashMap<String, Integer>();

							for(Iterator<Map.Entry<String, SFMSongItem>> sfmIter = sfmSongs.entrySet().iterator(); sfmIter.hasNext(); ) {

								Map.Entry<String, SFMSongItem> sfmItem= sfmIter.next();
								String sfmSongKey = sfmItem.getKey();
								SFMSongItem sfmSongItem = sfmItem.getValue();

								String sfmCleanSongName = nameCleaner(sfmSongItem.getSfmSongName());


								int distance = StringUtils.getLevenshteinDistance(lmaCleanSongName, sfmCleanSongName);


								if(lmaCleanSongName.contains(sfmCleanSongName)){
									sfmCorrespondences.put(sfmSongKey, distance);
								}

							}

							if(sfmCorrespondences.size()>0){

								for (Entry<String, Integer> entry1 : sfmCorrespondences.entrySet()) {  // Iterate through hashmap

									SFMSongItem sfmSongItem = sfmSongs.get(entry1.getKey());

									LMA_SFMSongLink songLink = new LMA_SFMSongLink();
									songLink.setLmaSongURI(lmaSongItem.getLmaSongURI());
									songLink.setLmaTrackNumber(lmaSongItem.getLmaTrackNumber());
									songLink.setLmaSongName(lmaSongItem.getLmaSongName());
									songLink.setSfmSongName(sfmSongItem.getSfmSongName());
									songLink.setSfmSongNumber(sfmSongItem.getSfmSongNumber());
									songLink.setDistance(entry1.getValue());
									songLink.setMethod("SFMInsideLMA");
									songLinks.add(songLink);
									sfmSongs.remove(entry1.getKey());
								}
								lmaIter.remove();

							}
						}

						//------------------------------------------------------04 CHECK IF ONE OR SEVERAL LMA SONGS ARE CONTAINED IN ONE SFM SONG---------------------------------------------

						for(Iterator<Map.Entry<String, SFMSongItem>> sfmIter = sfmSongs.entrySet().iterator(); sfmIter.hasNext(); ) {
							Map.Entry<String, SFMSongItem> sfmItem= sfmIter.next();
							SFMSongItem sfmSongItem= sfmItem.getValue();
							String sfmCleanSongName = nameCleaner(sfmSongItem.getSfmSongName());

							HashMap<String, Integer>lmaCorrespondences=new HashMap<String, Integer>();


							for(Iterator<Map.Entry<String, LMASongItem>> lmaIter = lmaSongs.entrySet().iterator(); lmaIter.hasNext(); ) {

								Map.Entry<String, LMASongItem> lmaItem= lmaIter.next();
								String lmaSongUri = lmaItem.getKey();
								LMASongItem lmaSongItem = lmaItem.getValue();

								String lmaCleanSongName = nameCleaner(lmaSongItem.getLmaSongName());


								int distance = StringUtils.getLevenshteinDistance(lmaCleanSongName, sfmCleanSongName);


								if(sfmCleanSongName.contains(lmaCleanSongName)){
									lmaCorrespondences.put(lmaSongUri, distance);
								}

							}

							if(lmaCorrespondences.size()>0){

								for (Entry<String, Integer> entry1 : lmaCorrespondences.entrySet()) {  // Iterate through hashmap

									LMASongItem lmaSongItem = lmaSongs.get(entry1.getKey());

									LMA_SFMSongLink songLink = new LMA_SFMSongLink();
									songLink.setLmaSongURI(lmaSongItem.getLmaSongURI());
									songLink.setLmaTrackNumber(lmaSongItem.getLmaTrackNumber());
									songLink.setLmaSongName(lmaSongItem.getLmaSongName());
									songLink.setSfmSongName(sfmSongItem.getSfmSongName());
									songLink.setSfmSongNumber(sfmSongItem.getSfmSongNumber());
									songLink.setDistance(entry1.getValue());
									songLink.setMethod("LMAInsideSFM");
									songLinks.add(songLink);
									lmaSongs.remove(entry1.getKey());
								}
								sfmIter.remove();

							}
						}

						
						//------------------------------------------------------ CHECK HALF LEVENSHTEIN---------------------------------------------------------

						/*for(Iterator<Map.Entry<String, LMASongItem>> lmaIter = lmaSongs.entrySet().iterator(); lmaIter.hasNext(); ) {
							Map.Entry<String, LMASongItem> lmaItem= lmaIter.next();
							String lmaSongUri = lmaItem.getKey();
							LMASongItem lmaSongItem= lmaItem.getValue();
							String lmaCleanSongName = nameCleaner(lmaSongItem.getLmaSongName());

							HashMap<String, Integer>weightMap=new HashMap<String, Integer>();


							for(Iterator<Map.Entry<String, SFMSongItem>> sfmIter = sfmSongs.entrySet().iterator(); sfmIter.hasNext(); ) {

								Map.Entry<String, SFMSongItem> sfmItem= sfmIter.next();
								String sfmSongKey = sfmItem.getKey();
								SFMSongItem sfmSongItem = sfmItem.getValue();

								String sfmCleanSongName = nameCleaner(sfmSongItem.getSfmSongName());


								int distance = StringUtils.getLevenshteinDistance(lmaCleanSongName, sfmCleanSongName);
								int threshold = lmaCleanSongName.length()/2;


								if(distance<=threshold){
									weightMap.put(sfmSongKey,distance);	
								}
							}

							if(weightMap.size()>0){
								int minValueInMap=(Collections.min(weightMap.values()));  // This will return min value in the Hashmap

										List<String> SuggestedSFMName = new ArrayList<String>();

										for (Entry<String, Integer> entry1 : weightMap.entrySet()) {  // Iterate through hashmap
											if (entry1.getValue()==minValueInMap) {
												SuggestedSFMName.add(entry1.getKey());
											}
										}

										if(SuggestedSFMName.size()==1){

											SFMSongItem sfmSongItem = sfmSongs.get(SuggestedSFMName.get(0));

											LMA_SFMSongLink songLink = new LMA_SFMSongLink();
											songLink.setLmaSongURI(lmaSongUri);
											songLink.setLmaTrackNumber(lmaSongItem.getLmaTrackNumber());
											songLink.setLmaSongName(lmaSongItem.getLmaSongName());
											songLink.setSfmSongName(sfmSongItem.getSfmSongName());
											songLink.setSfmSongNumber(sfmSongItem.getSfmSongNumber());
											songLink.setDistance(minValueInMap);
											songLink.setMethod("LevenHalf");

											songLinks.add(songLink);

											sfmSongs.remove(SuggestedSFMName.get(0));
											lmaIter.remove();
										}								
							}
						}*/
						
						
						//------------------------------------------------------05 INSERT LINKS IN DATABASE--------------------------------------------
						PreparedStatement ps = null;
						ps = conn.prepareStatement("INSERT INTO LS_SONG_MAP (lmaPerformanceUri,sfmSetlistUrl,lmaSongUri,lmaTrackNumber,lmaSongName,sfmSongName,sfmSongNumber,distance,method) values (?,?,?,?,?,?,?,?,?)");

						boolean hasMatches = false;

						if (songLinks.size()>0){
							hasMatches = true;
							for (int j = 0; j < songLinks.size(); j++){

								LMA_SFMSongLink sl = songLinks.get(j); 

								ps.setString(1, lmaPerf);
								ps.setString(2, sfmConcert);
								ps.setString(3, sl.getLmaSongURI());
								ps.setString(4, sl.getLmaTrackNumber());
								ps.setString(5, sl.getLmaSongName());
								ps.setString(6, sl.getSfmSongName());
								ps.setInt(7, sl.getSfmSongNumber());
								ps.setInt(8, sl.getDistance());
								ps.setString(9, sl.getMethod());
								ps.addBatch();

							}

							int[] updateCounts = ps.executeBatch();

						}

						ps.close();
						rs.close();
						sql.close();

						//------------------------------------------------------06 INSERT GAPS IN DATABASE--------------------------------------------
						boolean hasGaps = false; 
						ps = conn.prepareStatement("INSERT INTO LS_SONG_GAP (lmaPerformanceUri,sfmSetlistUrl,type,lmaSongUri,lmaTrackNumber,lmaSongName,sfmSongName,sfmSongNumber) values (?,?,?,?,?,?,?,?)");

						if(lmaSongs.size()>0){
							hasGaps = true;
							for (Entry<String, LMASongItem> eLma : lmaSongs.entrySet()) {

								LMASongItem lmaItem = eLma.getValue();

								ps.setString(1, lmaPerf);
								ps.setString(2, sfmConcert);
								ps.setString(3, "LMA");
								ps.setString(4, lmaItem.getLmaSongURI());
								ps.setString(5, lmaItem.getLmaTrackNumber());
								ps.setString(6, lmaItem.getLmaSongName());
								ps.setNull(7,Types.NULL);
								ps.setNull(8,Types.NULL);
								ps.addBatch();
							}
						}


						if(sfmSongs.size()>0){
							hasGaps = true;
							for (Entry<String, SFMSongItem> eSfm : sfmSongs.entrySet()) {

								SFMSongItem sfmItem = eSfm.getValue();

								ps.setString(1, lmaPerf);
								ps.setString(2, sfmConcert);
								ps.setString(3, "SFM");
								ps.setNull(4,Types.NULL);
								ps.setNull(5,Types.NULL);
								ps.setNull(6,Types.NULL);
								ps.setString(7, sfmItem.getSfmSongName());
								ps.setInt(8, sfmItem.getSfmSongNumber());
								ps.addBatch();
							}
						}

						if(hasGaps){
							int[] updateCounts = ps.executeBatch();
							ps.close();
						}



						ps = conn.prepareStatement("UPDATE lma_sfm_perf_mapping SET songLinkedFinal = ? WHERE lmaPerformanceUri = ? AND sfmSetlistUrl = ?");

						if(hasMatches){
							if(hasGaps){ps.setString(1, "partial");} 
							else {ps.setString(1, "all");}
						}else{ps.setString(1, "nomatch");}

						ps.setString(2, lmaPerf);
						ps.setString(3, sfmConcert);
						ps.executeUpdate();

						conn.commit();

						ps.close();
						conn.close();

						rs.close();

						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} catch (Exception e) {

						logger.error("FAILED " + i + ": LMA: " + lmaPerf + " -- SFM: " + sfmConcert,e);
						System.out.println("FAILED " + i + ": LMA: " + lmaPerf + " -- SFM: " + sfmConcert);
						try {
							conn.rollback();
						}catch(Exception ex){logger.error("FAILED ROLLBACK" + i + ": LMA: " + lmaPerf + " -- SFM: " + sfmConcert,e);}
						throw new RuntimeException(e);
					}

				} catch (Exception e) {
					// TODO: handle exception
				}

				i++;
			}


			long endTime1 = System.nanoTime();
			long endTime2 = System.currentTimeMillis();

			System.out.println("That took " + (endTime1 - startTime1));
			System.out.println("That took " + (endTime2 - startTime2) + " milliseconds");
			logger.info("That took " + (endTime2 - startTime2) + " milliseconds");

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
