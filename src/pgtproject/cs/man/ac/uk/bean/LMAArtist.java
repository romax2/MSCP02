package pgtproject.cs.man.ac.uk.bean;

import java.util.List;

public class LMAArtist {
	
	private String lmaArtistURI = "";
	private String mbArtistURI = "";
	private String foafArtistName = "";
	private String skosPrefLabel = "";
	private List<LMAPerformance> performances;
	
	public LMAArtist(){
		
	}
	/**
	 * @return the lmaArtistURI
	 */
	public String getLmaArtistURI() {
		return lmaArtistURI;
	}
	/**
	 * @param lmaArtistURI the lmaArtistURI to set
	 */
	public void setLmaArtistURI(String lmaArtistURI) {
		this.lmaArtistURI = lmaArtistURI;
	}
	/**
	 * @return the mbArtistURI
	 */
	public String getMbArtistURI() {
		return mbArtistURI;
	}
	/**
	 * @param mbArtistURI the mbArtistURI to set
	 */
	public void setMbArtistURI(String mbArtistURI) {
		this.mbArtistURI = mbArtistURI;
	}
	/**
	 * @return the foafArtistName
	 */
	public String getFoafArtistName() {
		return foafArtistName;
	}
	/**
	 * @param foafArtistName the foafArtistName to set
	 */
	public void setFoafArtistName(String foafArtistName) {
		this.foafArtistName = foafArtistName;
	}
	/**
	 * @return the skosPrefLabel
	 */
	public String getSkosPrefLabel() {
		return skosPrefLabel;
	}
	/**
	 * @param skosPrefLabel the skosPrefLabel to set
	 */
	public void setSkosPrefLabel(String skosPrefLabel) {
		this.skosPrefLabel = skosPrefLabel;
	}
	/**
	 * @return the performances
	 */
	public List<LMAPerformance> getPerformances() {
		return performances;
	}
	/**
	 * @param performances the performances to set
	 */
	public void setPerformances(List<LMAPerformance> performances) {
		this.performances = performances;
	}
	
	

}
