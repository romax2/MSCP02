package pgtproject.cs.man.ac.uk.bean;

public class LMASong {

	private String lmaSongURI = "";
	private String trackNumber = "";
	private String prefTrackLabel = "";
	
	public LMASong(){
		
	}
	/**
	 * @return the lmaSongURI
	 */
	public String getLmaSongURI() {
		return lmaSongURI;
	}
	/**
	 * @param lmaSongURI the lmaSongURI to set
	 */
	public void setLmaSongURI(String lmaSongURI) {
		this.lmaSongURI = lmaSongURI;
	}
	/**
	 * @return the trackNumber
	 */
	public String getTrackNumber() {
		return trackNumber;
	}
	/**
	 * @param trackNumber the trackNumber to set
	 */
	public void setTrackNumber(String trackNumber) {
		this.trackNumber = trackNumber;
	}
	/**
	 * @return the prefTrackLabel
	 */
	public String getPrefTrackLabel() {
		return prefTrackLabel;
	}
	/**
	 * @param prefTrackLabel the prefTrackLabel to set
	 */
	public void setPrefTrackLabel(String prefTrackLabel) {
		this.prefTrackLabel = prefTrackLabel;
	}
}
