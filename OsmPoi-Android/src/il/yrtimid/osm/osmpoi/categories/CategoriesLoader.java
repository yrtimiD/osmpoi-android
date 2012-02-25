/**
 * 
 */
package il.yrtimid.osm.osmpoi.categories;

import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.categories.Category.Type;
import il.yrtimid.osm.osmpoi.dal.DbAnalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import android.content.Context;

/**
 * @author yrtimid
 *
 */
public class CategoriesLoader {
	public static Category load(Context context){
		Category cat = null;
		InputStream xmlStream = null;
		try {
			xmlStream = context.getAssets().open("categories.xml");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlStream);
			doc.getDocumentElement().normalize();
			
			cat = createSubCategories(context, doc.getDocumentElement()); 
		}catch(SAXException e){
			Log.wtf("can't parse categories.xml", e);
		}catch (ParserConfigurationException e){
			Log.wtf("can't create parser", e);
		} catch (IOException e) {
			Log.wtf("can't open categories.xml", e);
		}finally{
			try{
				if (xmlStream != null) 
					xmlStream.close();
			}catch (IOException e) {
				Log.wtf("Closing categories.xml stream", e);
			}
		}

		return cat;
	}

	/**
	 * @param documentElement
	 * @param cat
	 */
	private static Category createSubCategories(Context context, Element root) {
		Category cat = null;
		
		String elementName = root.getNodeName();
		String name = root.getAttribute("name");
		String icon = root.getAttribute("icon");
		String query = root.getAttribute("query");
		String select = root.getAttribute("select");

		if (elementName.equals("category")){
			cat = new Category(Type.NONE);
		}else if (elementName.equals("search")){
			cat = new Category(Type.SEARCH);
		}else if (elementName.equals("inline")){
			cat = new Category(Type.INLINE_SEARCH);
		}else if (elementName.equals("starred")){
			cat = new Category(Type.STARRED);
		}else if (elementName.equals("custom")){
			cat = new Category(Type.CUSTOM);
		}else if (elementName.equals("categories")){
			cat = new Category(Type.NONE);
		}else {
			Log.d("Unknown category element: "+elementName);
			return null;
		}
		
		cat.setLocalizable(true);
		cat.setName(name);
		cat.setIcon(icon);
		cat.setQuery(query);
		cat.setSelect(select);
		
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element){
				Category subCat = createSubCategories(context, (Element)node);
				if (subCat != null)
					cat.getSubCategories().add(subCat);
			}
		}
		return cat;
	}

	public static void loadInlineCategories(Context context, Category cat){
		if (cat.getSubCategoriesCount()>0) return;
		if (cat.getType() != Category.Type.INLINE_SEARCH) return;
		DbAnalyzer dbHelper = null;
		try{
			dbHelper = OsmPoiApplication.databases.getPoiAnalizerDb();
			Long id = dbHelper.getInlineResultsId(cat.getQuery(), cat.getSelect());
			if (id == 0L){
				id = dbHelper.createInlineResults(cat.getQuery(), cat.getSelect());
			}
			Collection<String> subs = dbHelper.getInlineResults(id);
			for (String inlineCat:subs){
				Category subCat = new Category(Type.SEARCH);
				subCat.setName(inlineCat);
				subCat.setIcon(inlineCat);
				subCat.setQuery(String.format("%s=%s", cat.getSelect(), inlineCat));
				cat.getSubCategories().add(subCat);
			}
		}catch(Exception e){
			Log.wtf("loadInlineCategories", e);
		}finally{
			//if (dbHelper != null) dbHelper.close();
		}
		return;
	}
}
