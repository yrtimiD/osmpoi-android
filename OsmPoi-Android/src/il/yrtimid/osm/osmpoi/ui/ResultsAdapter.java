package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.formatters.EntityFormatter;

import java.util.ArrayList;
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
	float azimuth = 0.0f;
	List<Entity> items;
	Comparator<Entity> comparator;
	List<EntityFormatter> formatters;
	
	public ResultsAdapter(Context context, Location location, List<EntityFormatter> formatters) {
		this.items = new ArrayList<Entity>();
		this.location = location;
		inflater = LayoutInflater.from(context);
		this.comparator = new DistanceComparator(this.location);
		this.formatters = formatters;
	}

	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	public void addItem(Entity entity) {
		if (!items.contains(entity)) {
			items.add(entity);
			updateAndSort();
		}
	}

	public void addItems(Entity[] entities) {
		for (Entity e : entities) {
			if (!items.contains(e)) {
				items.add(e);
			}
		}
		updateAndSort();
	}

	public Entity[] getAllItems(){
		return items.toArray(new Entity[items.size()]);
	}
	
	public void setLocation(Location newLoc) {
		this.location = newLoc;
		this.comparator = new DistanceComparator(this.location);
		updateAndSort();
	}

	public void setAzimuth(float azimuth){
		this.azimuth = azimuth;
		notifyDataSetChanged();
	}
	
	public void updateAndSort() {
		Collections.sort(items, this.comparator);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entity item = items.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.results_view_row, parent, false);
		}

		TwoLineListItem listItem = (TwoLineListItem) convertView;

		String name = getEntityText(item);
		listItem.getText1().setText(name);

		if (this.location != null){
			Node node = il.yrtimid.osm.osmpoi.Util.getFirstNode(item);
			
			if (node != null){	
				Location nl = new Location(this.location);
				nl.setLatitude(node.getLatitude());
				nl.setLongitude(node.getLongitude());
				int bearing = Util.normalizeBearing ((int) location.bearingTo(nl)-(int)azimuth);
				listItem.getText2().setText(String.format("%,dm %c (%d˚)", (int) location.distanceTo(nl), Util.getDirectionChar(bearing), bearing));
			}
		}

		return convertView;
	}


	private CharSequence localPostfix = null;
	public void setLocale(CharSequence locale) {
		if (locale != null)
			localPostfix = locale;
		else
			locale = null;
	}
	
	public String getEntityText(Entity entity) {
		return EntityFormatter.format(this.formatters, entity, localPostfix);
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
		if (items.size()>0){
			n = il.yrtimid.osm.osmpoi.Util.getFirstNode(items.get(items.size()-1));
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
