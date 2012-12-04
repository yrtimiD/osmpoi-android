package il.yrtimid.osm.osmpoi.xml;

import il.yrtimid.osm.osmpoi.domain.Entity;

import java.io.InputStream;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

public class XmlReader {

	public static List<Entity> parseEntitiesFromXMLStream(InputStream inputStream) throws XmlPullParserException {

		EntityListSink sink = new EntityListSink();
		FastXmlParser parser;
		parser = new FastXmlParser(sink);
		parser.parseStream(inputStream);
		
		return sink.getEntities();
	}

	public static List<Entity> parseEntitiesFromXML(String xml) throws XmlPullParserException {

		EntityListSink sink = new EntityListSink();
		FastXmlParser parser;
		parser = new FastXmlParser(sink);
		parser.parse(xml);
		
		return sink.getEntities();
	}

}
