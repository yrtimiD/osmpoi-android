package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.LocationChangeManager.LocationChangeListener;
import il.yrtimid.osm.osmpoi.OrientationChangeManager.OrientationChangeListener;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.dal.DbStarred;
import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.formatters.EntityFormatter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultItemActivity extends Activity implements OnCheckedChangeListener, LocationChangeListener, OrientationChangeListener {
	public static final String ENTITY = "ENTITY";
	private DbStarred dbHelper;
	private Entity entity;
	private Location location;
	private float azimuth = 0.0f;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.result_item_full_view);

		Bundle extras = getIntent().getExtras();
		this.entity = (Entity) extras.getParcelable(ENTITY);
		
		setIcon();

		dbHelper = OsmPoiApplication.databases.getStarredDb();
		
		((TextView)findViewById(R.id.itemViewID)).setText(getString(R.string.ID)+": "+entity.getId());
		CheckBox star = ((CheckBox)findViewById(R.id.star));
		star.setChecked(dbHelper.isStarred(entity));
		star.setOnCheckedChangeListener(this);
		
		Node node = il.yrtimid.osm.osmpoi.Util.getFirstNode(entity);
		if (node != null){
			TextView textLat = (TextView) findViewById(R.id.textLat);
			TextView textLon = (TextView) findViewById(R.id.textLon);

			textLat.setText(String.format("Lat: %f", node.getLatitude()));
			textLon.setText(String.format("Lon: %f", node.getLongitude()));
		}		
		
		LayoutInflater inflater = LayoutInflater.from(this);

		ViewGroup tags = (ViewGroup) findViewById(R.id.tagsLayout);
		for(Tag tag: entity.getTags().getSorted()){
			
			View v = inflater.inflate(R.layout.tag_row, null);
			((TextView)v.findViewById(R.id.textKey)).setText(tag.getKey());
			((TextView)v.findViewById(R.id.textValue)).setText(tag.getValue());
			tags.addView(v);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		OsmPoiApplication.locationManager.setLocationChangeListener(this);
		OsmPoiApplication.orientationManager.setOrientationChangeListener(this);
		
		updateDistanceAndDirection();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		OsmPoiApplication.locationManager.setLocationChangeListener(null);
		OsmPoiApplication.orientationManager.setOrientationChangeListener(null);
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
	
	private void updateDistanceAndDirection(){
		TextView tv = (TextView)findViewById(R.id.textDistanceAndDirection);
		Node node = il.yrtimid.osm.osmpoi.Util.getFirstNode(entity);

		if (node != null && this.location != null){
			
			Location nl = new Location(this.location);
			nl.setLatitude(node.getLatitude());
			nl.setLongitude(node.getLongitude());
			int bearing = Util.normalizeBearing(((int) location.bearingTo(nl)-(int)azimuth));
			
			tv.setText(String.format("%,dm %c (%d˚)", (int) location.distanceTo(nl), Util.getDirectionChar(bearing), bearing));
		}
		else {	
			tv.setText("");
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

	/* (non-Javadoc)
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
		
		setProgressBarIndeterminateVisibility(true);
		if (buttonView.isChecked()){
			String title = EntityFormatter.format(OsmPoiApplication.formatters, entity, OsmPoiApplication.Config.getResultLanguage());
			LayoutInflater inflater = LayoutInflater.from(this);
			final EditText textEntryView = (EditText)inflater.inflate(R.layout.starred_title, null);
			textEntryView.setText(title);
			new AlertDialog.Builder(this)
				.setTitle(R.string.title)
			.setView(textEntryView)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			     public void onClick(DialogInterface dialog, int whichButton) {
					dbHelper.addStarred(entity, textEntryView.getText().toString());	     			
			     }
			 })
			 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					buttonView.setChecked(false);
				}
			 })
	         .create()
	         .show();
		}else {
			dbHelper.removeStarred(entity);
		}
		setProgressBarIndeterminateVisibility(false);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.LocationChangeManager.LocationChangeListener#OnLocationChanged(android.location.Location)
	 */
	@Override
	public void OnLocationChanged(Location loc) {
		this.location = loc;
		updateDistanceAndDirection();
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.OrientationChangeManager.OrientationChangeListener#OnOrientationChanged(float)
	 */
	@Override
	public void OnOrientationChanged(float azimuth) {
		this.azimuth = azimuth;
		updateDistanceAndDirection();
	}

}
