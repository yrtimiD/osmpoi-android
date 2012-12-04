// This software is released into the Public Domain.  See copying.txt for details.
package il.yrtimid.osm.osmpoi.xml;

import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.osmosis.core.task.v0_6.Sink;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.xml.v0_6.impl.MemberTypeParser;
import org.openstreetmap.osmosis.xml.v0_6.impl.XmlConstants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * Reads the contents of an osm file using a Stax parser.
 * 
 * @author Jiri Klement
 * @author Brett Henderson
 */
public class FastXmlParser {
	
	private static final String ELEMENT_NAME_BOUND = "bound";
	private static final String ELEMENT_NAME_NODE = "node";
	private static final String ELEMENT_NAME_WAY = "way";
	private static final String ELEMENT_NAME_RELATION = "relation";
	private static final String ELEMENT_NAME_TAG = "tag";
	private static final String ELEMENT_NAME_NODE_REFERENCE = "nd";
	private static final String ELEMENT_NAME_MEMBER = "member";
	private static final String ATTRIBUTE_NAME_ID = "id";
	private static final String ATTRIBUTE_NAME_VERSION = "version";
//	private static final String ATTRIBUTE_NAME_TIMESTAMP = "timestamp";
//	private static final String ATTRIBUTE_NAME_USER_ID = "uid";
//	private static final String ATTRIBUTE_NAME_USER = "user";
//	private static final String ATTRIBUTE_NAME_CHANGESET_ID = "changeset";
	private static final String ATTRIBUTE_NAME_LATITUDE = "lat";
	private static final String ATTRIBUTE_NAME_LONGITUDE = "lon";
	private static final String ATTRIBUTE_NAME_KEY = "k";
	private static final String ATTRIBUTE_NAME_VALUE = "v";
	private static final String ATTRIBUTE_NAME_REF = "ref";
	private static final String ATTRIBUTE_NAME_TYPE = "type";
	private static final String ATTRIBUTE_NAME_ROLE = "role";
//	private static final String ATTRIBUTE_NAME_BOX = "box";
//	private static final String ATTRIBUTE_NAME_ORIGIN = "origin";
	
	private static final Logger LOG = Logger.getLogger(FastXmlParser.class.getName());
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param sink
	 *            The sink receiving all output data.
	 * @throws XmlPullParserException 
	 */
	public FastXmlParser(Sink sink) throws XmlPullParserException {
		this.sink = sink;
		
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        reader = factory.newPullParser();

		memberTypeParser = new MemberTypeParser();
	}
	
	private final XmlPullParser reader;
	private final Sink sink;
	private final MemberTypeParser memberTypeParser;
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void parseStream(InputStream inputStream) {
		
		try {
			
	        reader.setInput(inputStream, null);
			readOsm();
			
			sink.complete();
			
		} catch (Exception e) {
			throw new OsmosisRuntimeException("Unable to read XML stream.", e);
		} finally {
			sink.release();
			
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Unable to close input stream.", e);
				}
				inputStream = null;
			}
		}
	}
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void parse(String xml) {
		
		try {
			
	        reader.setInput(new StringReader(xml));
			readOsm();
			
			sink.complete();
			
		} catch (Exception e) {
			throw new OsmosisRuntimeException("Unable to read XML stream.", e);
		} finally {
			sink.release();
		}
	}
	
	private void readUnknownElement() throws XmlPullParserException, IOException {
		int level = 0;
		
		do {
			if (reader.getEventType() == XmlPullParser.START_TAG) {
				level++;
			} else if (reader.getEventType() == XmlPullParser.END_TAG) {
				level--;
			}
			reader.next();
		} while (level > 0);
	}
	
	private Tag readTag() throws Exception {
		Tag tag = new Tag(reader.getAttributeValue(null, ATTRIBUTE_NAME_KEY),
				reader.getAttributeValue(null, ATTRIBUTE_NAME_VALUE));
		reader.nextTag();
		reader.nextTag();
		return tag;
	}
	
	private Node readNode() throws Exception {
		long id;
		double latitude;
		double longitude;
		Node node;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		latitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LATITUDE));
		longitude = Double.parseDouble(reader.getAttributeValue(null, ATTRIBUTE_NAME_LONGITUDE));
		
		node = new Node(new CommonEntityData(id), latitude, longitude);
		
		reader.nextTag();
		while (reader.getEventType() == XmlPullParser.START_TAG) {
			if (reader.getName().equals(ELEMENT_NAME_TAG)) {
				node.getTags().add(readTag());
			} else {
				readUnknownElement();
			}
		}
		
		reader.nextTag();
		
		return node;
	}
	
	private WayNode readWayNode() throws Exception {
		WayNode node = new WayNode(
				Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_REF)));
		reader.nextTag();
		reader.nextTag();
		return node;
	}
	
	private Way readWay() throws Exception {
		long id;
		Way way;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		
		way = new Way(new CommonEntityData(id));
		
		reader.nextTag();
		while (reader.getEventType() == XmlPullParser.START_TAG) {
			if (reader.getName().equals(ELEMENT_NAME_TAG)) {
				way.getTags().add(readTag());
			} else if (reader.getName().equals(ELEMENT_NAME_NODE_REFERENCE)) {
				way.getWayNodes().add(readWayNode());
			} else {
				readUnknownElement();
			}
		}
		reader.nextTag();

		return way;
	}
	
	private RelationMember readRelationMember() throws Exception {
		long id;
		EntityType type;
		String role;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_REF));
		type = memberTypeParser.parse(reader.getAttributeValue(null, ATTRIBUTE_NAME_TYPE));
		role = reader.getAttributeValue(null, ATTRIBUTE_NAME_ROLE);
		
		RelationMember relationMember = new RelationMember(id, type, role);
		
		reader.nextTag();
		reader.nextTag();
		
		return relationMember;
	}
	
	private Relation readRelation() throws Exception {
		long id;
		Relation relation;
		
		id = Long.parseLong(reader.getAttributeValue(null, ATTRIBUTE_NAME_ID));
		
		relation = new Relation(new CommonEntityData(id));
		
		reader.nextTag();
		while (reader.getEventType() == XmlPullParser.START_TAG) {
			if (reader.getName().equals(ELEMENT_NAME_TAG)) {
				relation.getTags().add(readTag());
			} else if (reader.getName().equals(ELEMENT_NAME_MEMBER)) {
				relation.getMembers().add(readRelationMember());
			} else {
				readUnknownElement();
			}
		}
		reader.nextTag();
		
		return relation;
	}

	
	/**
	 * Parses the xml and sends all data to the sink.
	 */
	public void readOsm() {
		
		try {
		
			if (reader.nextTag() == XmlPullParser.START_TAG && reader.getName().equals("osm")) {

				String fileVersion;

				fileVersion = reader.getAttributeValue(null, ATTRIBUTE_NAME_VERSION);

				if (!XmlConstants.OSM_VERSION.equals(fileVersion)) {
					LOG.warning(
							"Expected version " + XmlConstants.OSM_VERSION
							+ " but received " + fileVersion + "."
					);
				}

				reader.nextTag();
				

				if (reader.getEventType() == XmlPullParser.START_TAG
						&& reader.getName().equals(ELEMENT_NAME_BOUND)) {
					//sink.process(new BoundContainer(readBound()));
				}

				while (reader.getEventType() == XmlPullParser.START_TAG) {			
					// Node, way, relation
					if (reader.getName().equals(ELEMENT_NAME_NODE)) {
						sink.process(readNode());
					} else if (reader.getName().equals(ELEMENT_NAME_WAY)) {
						sink.process(readWay());
					} else if (reader.getName().equals(ELEMENT_NAME_RELATION)) {
						sink.process(readRelation());
					} else {
						readUnknownElement();
					}
					if (reader.getEventType() == XmlPullParser.TEXT)
						reader.nextTag();
				}

			} else {
				throw new RuntimeException("No osm tag found");
			}
		} catch (Exception e) {
			throw new OsmosisRuntimeException(e);
		}
	}
}
