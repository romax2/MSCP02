package pgtproject.cs.man.ac.uk;

public class Constants {
	
	public static final String LMA_SERVICE = "http://etree.linkedmusic.org/sparql";
	public static final String SETLISTBASEURL = "http://api.setlist.fm/rest";
	public static final String LASTFM_KEY = "7913fd3d006cd8aff76e25c934f16504";
	//public static final String CONN_STRING = "jdbc:sqlite:C:\\DissertationDB\\MScProjectDB.s3db";
	public static final String CONN_STRING = "jdbc:sqlite:C:\\DissertationDB\\cleanDB.s3db";
	
	public static final String RDF_PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
	public static final String RDFS_PREFIX = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
	public static final String OWL_PREFIX = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
	public static final String DC_PREFIX = "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n";
	public static final String DCTERMS_PREFIX = "PREFIX dcterms: <http://purl.org/dc/terms/>\n";
	public static final String FOAF_PREFIX = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n";
	public static final String SIM_PREFIX = "PREFIX sim: <http://purl.org/ontology/similarity/>\n";
	public static final String MO_PREFIX = "PREFIX mo: <http://purl.org/ontology/mo/>\n";
	public static final String OV_PREFIX = "PREFIX ov: <http://open.vocab.org/terms/>\n";
	public static final String XSD_PREFIX = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
	public static final String ETREE_PREFIX = "PREFIX etree: <http://etree.linkedmusic.org/vocab/>\n";
	public static final String MB_PREFIX = "PREFIX mb: <http://musicbrainz.org/>\n";
	public static final String GEO_PREFIX = "PREFIX geo: <http://www.geonames.org/ontology>\n";
	public static final String PROV_PREFIX = "PREFIX prov: <http://www.w3.org/ns/prov#>\n";
	public static final String TIMELINE_PREFIX = "PREFIX timeline: <http://purl.org/NET/c4dm/timeline.owl#>\n";
	public static final String EVENT_PREFIX = "PREFIX event: <http://purl.org/NET/c4dm/event.owl#>\n";
	public static final String TIME_PREFIX = "PREFIX time: <http://www.w3.org/2006/time#>\n";
	public static final String SKOS_PREFIX = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
	
	
	
	
	public static final String ALL_PREFIXES =   RDF_PREFIX +
												RDFS_PREFIX +
												OWL_PREFIX +
												DC_PREFIX +
												DCTERMS_PREFIX +
												FOAF_PREFIX +
												SIM_PREFIX +
												MO_PREFIX +
												OV_PREFIX +
												XSD_PREFIX +
												ETREE_PREFIX +
												MB_PREFIX +
												GEO_PREFIX +
												PROV_PREFIX +
												TIMELINE_PREFIX +
												EVENT_PREFIX +
												TIME_PREFIX +
												SKOS_PREFIX;
	

}
