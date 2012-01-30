package il.yrtimid.osm.osmpoi.ui;

import java.util.List;

import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.domain.*;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

public class ResultItemActivity extends Activity {
	public static final String ENTITY = "ENTITY";

	private Entity entity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_item_full_view);

		Bundle extras = getIntent().getExtras();
		this.entity = (Entity) extras.getParcelable(ENTITY);
		
		TableLayout table = (TableLayout) findViewById(R.id.tableLayout1);
		
		setIcon();
		
		((TextView)findViewById(R.id.itemViewID)).setText(getString(R.string.ID)+": "+entity.getId());
		
		
		
		Node node = il.yrtimid.osm.osmpoi.Util.getFirstNode(entity);
		if (node != null){
			TextView textLat = (TextView) findViewById(R.id.textLat);
			TextView textLon = (TextView) findViewById(R.id.textLon);

			textLat.setText(String.format("Lat: %f", node.getLatitude()));
			textLon.setText(String.format("Lon: %f", node.getLongitude()));
		}		
		
		LayoutInflater inflater = LayoutInflater.from(this);
		for(Tag tag: entity.getTags()){
			
			View v = inflater.inflate(R.layout.tag_row, null);
			((TextView)v.findViewById(R.id.textKey)).setText(tag.getKey());
			((TextView)v.findViewById(R.id.textValue)).setText(tag.getValue());
			table.addView(v);
		}
	}

	private void setIcon() {
		ImageView imageType = ((ImageView)findViewById(R.id.imageType));
		switch (entity.getType()) {
		case Node:
			imageType.setImageResource(R.drawable.ic_node);
			break;
		case Way:
			imageType.setImageResource(R.drawable.ic_way);
			break;
		case Relation:
			imageType.setImageResource(R.drawable.ic_relation);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.item_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnu_open:
			Node node = il.yrtimid.osm.osmpoi.Util.getFirstNode(entity);
			if (node != null){
				String uri = String.format("geo:%f,%f?z=23", node.getLatitude(), node.getLongitude());
				Intent pref = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(pref);
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
