/**
 * 
 */
package il.yrtimid.osm.osmpoi.formatters;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;

/**
 * @author yrtimid
 *
 */
public class EntityFormattersLoader {
	public static List<EntityFormatter> load(Context context){
		List<EntityFormatter> formatters = new ArrayList<EntityFormatter>();
		InputStream xmlStream = null;
		try {
			xmlStream = context.getAssets().open("entity_formatters.xml");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlStream);
			doc.getDocumentElement().normalize();
			
			NodeList nodes = doc.getDocumentElement().getElementsByTagName("formatter");
			for (int i = 0; i < nodes.getLength(); i++) {
				Element ele = (Element)nodes.item(i);
				String match = ele.getAttribute("match");
				String selectPattern = ele.getAttribute("select_pattern");
				
				TagMatcher matcher = TagMatcher.parse(match);
				TagSelector selector = new TagSelector(selectPattern);
				formatters.add(new EntityFormatter(matcher, selector));
			}
		}catch(SAXException e){
			Log.wtf("can't parse entity_formatters.xml", e);
		}catch (ParserConfigurationException e){
			Log.wtf("can't create parser", e);
		} catch (IOException e) {
			Log.wtf("can't open entity_formatters.xml", e);
		}finally{
			try {
				if (xmlStream != null)
					xmlStream.close();
			} catch (IOException e) {
				Log.wtf("Closing entity_formatters.xml stream", e);
			}
		}

		return formatters;
	}
}
