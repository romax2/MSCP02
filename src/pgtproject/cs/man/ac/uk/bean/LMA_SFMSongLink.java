package pgtproject.cs.man.ac.uk.bean;

public class LMA_SFMSongLink {

	private String lmaSongURI = "";
	private String lmaTrackNumber = "";
	private String lmaSongName = "";
	private String sfmSongName = "";
	private int sfmSongNumber = -1;
	private int distance = -1;
	private String method = "";
	
	public LMA_SFMSongLink(){
		
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
	 * @return the lmatrackNumber
	 */
	public String getLmaTrackNumber() {
		return lmaTrackNumber;
	}

	/**
	 * @param lmatrackNumber the lmatrackNumber to set
	 */
	public void setLmaTrackNumber(String lmaTrackNumber) {
		this.lmaTrackNumber = lmaTrackNumber;
	}

	/**
	 * @return the lmaSongName
	 */
	public String getLmaSongName() {
		return lmaSongName;
	}

	/**
	 * @param lmaSongName the lmaSongName to set
	 */
	public void setLmaSongName(String lmaSongName) {
		this.lmaSongName = lmaSongName;
	}

	/**
	 * @return the sfmSongName
	 */
	public String getSfmSongName() {
		return sfmSongName;
	}

	/**
	 * @param sfmSongName the sfmSongName to set
	 */
	public void setSfmSongName(String sfmSongName) {
		this.sfmSongName = sfmSongName;
	}

	/**
	 * @return the sfmSongNumber
	 */
	public int getSfmSongNumber() {
		return sfmSongNumber;
	}

	/**
	 * @param sfmSongNumber the sfmSongNumber to set
	 */
	public void setSfmSongNumber(int sfmSongNumber) {
		this.sfmSongNumber = sfmSongNumber;
	}

	/**
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	
}
