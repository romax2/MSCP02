package pgtproject.cs.man.ac.uk.bean;

import java.util.List;

public class LMAPerformance {
	
	private String lmaPerformanceURI = "";
	private String prefPerformanceLabel = "";
	private String date = "";
	private String location = "";
	private String placeName = "";
	private String prefPlaceLabel = "";
	private String lmaVenueURI = "";
	private String geonamesLocationURI = "";
	private String lastfmLocationURI = "";
	private List<LMASong> songs;
	
	public LMAPerformance(){
		
	}
	/**
	 * @return the lmaPerformanceURI
	 */
	public String getLmaPerformanceURI() {
		return lmaPerformanceURI;
	}
	/**
	 * @param lmaPerformanceURI the lmaPerformanceURI to set
	 */
	public void setLmaPerformanceURI(String lmaPerformanceURI) {
		this.lmaPerformanceURI = lmaPerformanceURI;
	}
	/**
	 * @return the prefPerformanceLabel
	 */
	public String getPrefPerformanceLabel() {
		return prefPerformanceLabel;
	}
	/**
	 * @param prefPerformanceLabel the prefPerformanceLabel to set
	 */
	public void setPrefPerformanceLabel(String prefPerformanceLabel) {
		this.prefPerformanceLabel = prefPerformanceLabel;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return the placeName
	 */
	public String getPlaceName() {
		return placeName;
	}
	/**
	 * @param placeName the placeName to set
	 */
	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}
	/**
	 * @return the prefPlaceLabel
	 */
	public String getPrefPlaceLabel() {
		return prefPlaceLabel;
	}
	/**
	 * @param prefPlaceLabel the prefPlaceLabel to set
	 */
	public void setPrefPlaceLabel(String prefPlaceLabel) {
		this.prefPlaceLabel = prefPlaceLabel;
	}

	/**
	 * @return the lmaVenueURI
	 */
	public String getLmaVenueURI() {
		return lmaVenueURI;
	}
	/**
	 * @param lmaVenueURI the lmaVenueURI to set
	 */
	public void setLmaVenueURI(String lmaVenueURI) {
		this.lmaVenueURI = lmaVenueURI;
	}
	/**
	 * @return the geonamesLocationURI
	 */
	public String getGeonamesLocationURI() {
		return geonamesLocationURI;
	}
	/**
	 * @param geonamesLocationURI the geonamesLocationURI to set
	 */
	public void setGeonamesLocationURI(String geonamesLocationURI) {
		this.geonamesLocationURI = geonamesLocationURI;
	}
	/**
	 * @return the lastfmLocationURI
	 */
	public String getLastfmLocationURI() {
		return lastfmLocationURI;
	}
	/**
	 * @param lastfmLocationURI the lastfmLocationURI to set
	 */
	public void setLastfmLocationURI(String lastfmLocationURI) {
		this.lastfmLocationURI = lastfmLocationURI;
	}
	/**
	 * @return the songs
	 */
	public List<LMASong> getSongs() {
		return songs;
	}
	/**
	 * @param songs the songs to set
	 */
	public void setSongs(List<LMASong> songs) {
		this.songs = songs;
	}
}
