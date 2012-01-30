package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.*;
import il.yrtimid.osm.osmpoi.R.id;
import il.yrtimid.osm.osmpoi.domain.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TwoLineListItem;

public class ResultsAdapter extends BaseAdapter {
	LayoutInflater inflater;
	Location location;
	List<Entity> items;
	Comparator<Entity> comparator;

	public ResultsAdapter(Context context, Location location) {
		this.items = new ArrayList<Entity>();
		this.location = location;
		inflater = LayoutInflater.from(context);
		this.comparator = new DistanceComparator(this.location);
	}

	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	public void addItem(Entity entity) {
		if (!items.contains(entity)) {
			items.add(entity);
			update();
		}
	}

	public void addItems(Entity[] entities) {
		for (Entity e : entities) {
			if (!items.contains(e)) {
				items.add(e);
			}
		}
		update();
	}

	public Entity[] getAllItems(){
		return items.toArray(new Entity[items.size()]);
	}
	
	public void setLocation(Location newLoc) {
		this.location = newLoc;
		this.comparator = new DistanceComparator(this.location);
		update();
	}

	public void update() {
		Collections.sort(items, this.comparator);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entity item = items.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.result_item, parent, false);
		}

		TwoLineListItem listItem = (TwoLineListItem) convertView;

		String name = getEntityName(item);
		if (name == null) {
			name = getEntityType(item);
		}
		if (name == null) {
			name = "ID:" + item.getId();
		}

		listItem.getText1().setText(name);

		if (this.location != null){
			Node node = il.yrtimid.osm.osmpoi.Util.getFirstNode(item);
			
			if (node != null){	
				Location nl = new Location(this.location);
				nl.setLatitude(node.getLatitude());
				nl.setLongitude(node.getLongitude());
				int bearing = (int) location.bearingTo(nl);
				listItem.getText2().setText(String.format("%,dm %c (%d˚)", (int) location.distanceTo(nl), getDirectionChar(bearing), bearing));
			}
		}

		return convertView;
	}


	private final String engPostfix = "en";
	private CharSequence localPostfix = null;

	public void setLocale(CharSequence locale) {
		if (locale != null)
			localPostfix = locale;
		else
			locale = null;
	}

	public String getEntityName(Entity entity) {
		String enName = null;
		String locName = null;
		String name = null;

		Collection<Tag> tags = entity.getTags();
		for (Tag tag : tags) {
			if (tag.getKey().equals("name")) {
				name = tag.getValue();
			} else if (tag.getKey().equals("name" + ((localPostfix != null) ? ":" + localPostfix : "")))
			/* local name will have value of default name */{
				locName = tag.getValue();
			} else if (tag.getKey().equals("name" + ":" + engPostfix)) {
				enName = tag.getValue();
			}
		}

		if (locName != null)
			return locName;
		else if (enName != null)
			return enName;
		else if (name != null)
			return name;
		else
			return null;
	}

	private String getEntityType(Entity entity) {
		Collection<Tag> tags = entity.getTags();
		for (Tag tag : tags) {
			if (tag.getKey().equals("amenity")) {
				return tag.getValue();
			} else if (tag.getKey().equals("shop")) {
				return String.format("%s shop", tag.getValue());
			} else if (tag.getKey().equals("building") && tag.getValue().equals("yes")) {
				return "Building";
			}
		}
		return null;
	}

	private char[] directionChars = new char[]{'↑','↗','→','↘','↓','↙','←','↖'};
	private char getDirectionChar(int degree){
		if (degree<0) degree+=360;
		degree+=45/2;
		int section = (int)(degree/45);
		if (section == 8) section = 0;
		return directionChars[section];
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).getId();
	}
	
	public int getMaximumDistance(){
		Node n = null;
		for(int i=items.size()-1; i>0; i--){
			if (items.get(i).getType() == EntityType.Node){
				n = (Node)items.get(i);
				break;
			}
		}
		if (n!=null){
			Location nl = new Location(this.location);
			nl.setLatitude(n.getLatitude());
			nl.setLongitude(n.getLongitude());
			return (int) location.distanceTo(nl);
		}else {
			return 0;
		}
		
	}
}
