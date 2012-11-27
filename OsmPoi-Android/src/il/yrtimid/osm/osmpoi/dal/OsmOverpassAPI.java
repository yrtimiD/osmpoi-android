package il.yrtimid.osm.osmpoi.dal;


import il.yrtimid.osm.osmpoi.domain.CommonEntityData;
import il.yrtimid.osm.osmpoi.domain.TagCollection;
import il.yrtimid.osm.osmpoi.tagmatchers.KeyValueMatcher;

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class OsmOverpassAPI {

	private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter";
	private static final String BBOX = "(%f,%f,%f,%f)";//lower latitude, lower longitude, upper latitude, upper longitude
	private static final String KV_QUERY = "'%s'='%s'";
	private static final String NODES_AND_WAYS_BY_KV = "node[%s]%s->.n;way[%s]%s->.w;(.n;.w;.w>;);out body;";//query,bbox,query,bbox
	
	/**
	 * 
	 * @param lat center
	 * @param lon center
	 * @param radius 0.01+
	 * @param k
	 * @param v
	 * @return
	 */
	private static String getQueryForKV(Double lat, Double lon, Double radius, String k, String v){
		String bbox = String.format(BBOX, lat-radius, lon-radius, lat+radius, lon+radius);
		String kv = String.format(KV_QUERY, k, v);
		return String.format(NODES_AND_WAYS_BY_KV, kv, bbox, kv, bbox);
	}
	
	/**
	 * 
	 * @param xmlDocument 
	 * @return a list of OSM entities extracted from xml
	 */
	private static List<il.yrtimid.osm.osmpoi.domain.Entity> getEntities(Document xmlDocument) {
		List<il.yrtimid.osm.osmpoi.domain.Entity> osmEntities = new ArrayList<il.yrtimid.osm.osmpoi.domain.Entity>();

		Node osmRoot = xmlDocument.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			Node item = osmXMLNodes.item(i);
			if (item.getNodeName().equals("node")) {
				
				NamedNodeMap attributes = item.getAttributes();
				
				Node namedItemID = attributes.getNamedItem("id");
				Node namedItemLat = attributes.getNamedItem("lat");
				Node namedItemLon = attributes.getNamedItem("lon");

				Long id = Long.valueOf(namedItemID.getNodeValue());
				Double latitude = Double.valueOf(namedItemLat.getNodeValue());
				Double longitude = Double.valueOf(namedItemLon.getNodeValue());

				il.yrtimid.osm.osmpoi.domain.CommonEntityData entityData = new CommonEntityData(id, 0);
				il.yrtimid.osm.osmpoi.domain.Node node = new il.yrtimid.osm.osmpoi.domain.Node(entityData, latitude, longitude);
				TagCollection tags = node.getTags();

				NodeList tagXMLNodes = item.getChildNodes();
				for (int j = 1; j < tagXMLNodes.getLength(); j++) {
					Node tagItem = tagXMLNodes.item(j);
					NamedNodeMap tagAttributes = tagItem.getAttributes();
					if (tagAttributes != null) {
						String k = tagAttributes.getNamedItem("k").getNodeValue();
						String v = tagAttributes.getNamedItem("v").getNodeValue();
						tags.add(new il.yrtimid.osm.osmpoi.domain.Tag(k, v));
					}
				}
				
				osmEntities.add(node);
			}

		}
		return osmEntities;
	}
//
//	public static List<il.yrtimid.osm.osmpoi.domain.Node> getOSMNodesInVicinity(double lat, double lon, double vicinityRange) throws IOException, SAXException, ParserConfigurationException {
//		return OSMWrapperAPI.getNodes(getXML(lon, lat, vicinityRange));
//	}

	/**
	 * 
	 * @param query the overpass query
	 * @return the nodes in the formulated query
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private static Document getDataViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
		String hostname = OVERPASS_API;

		URL osm = new URL(hostname);
		HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setReadTimeout(2*60*1000);//2 minute timeout
		
		DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
		printout.writeBytes("data=" + URLEncoder.encode(query, "utf-8"));
		printout.flush();
		printout.close();

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		return docBuilder.parse(connection.getInputStream());
	}
	
	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param radius
	 * @param kvQuery only k=v supported
	 * @return
	 */
	public static List<il.yrtimid.osm.osmpoi.domain.Entity> Search(Double lat, Double lon, Double radius, KeyValueMatcher matcher){
		List<il.yrtimid.osm.osmpoi.domain.Entity> result = new ArrayList<il.yrtimid.osm.osmpoi.domain.Entity>();
		try {
		String query = getQueryForKV(lat, lon, radius, matcher.getKey(), matcher.getValue());
		Document doc;
		
			doc = getDataViaOverpass(query);
			result = getEntities(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
