package il.yrtimid.osm.osmpoi.ui;

import java.security.InvalidParameterException;

import il.yrtimid.osm.osmpoi.LocationChangeManager.LocationChangeListener;
import il.yrtimid.osm.osmpoi.OrientationChangeManager.OrientationChangeListener;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.SearchPipe;
import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import android.app.Activity;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Actual search running and results view activity 
 * @author yrtimid
 *
 */
public class ResultsActivity extends Activity implements OnItemClickListener, LocationChangeListener, OrientationChangeListener, OnClickListener {
	//public static final String SEARCH_TYPE = "SEARCH_TYPE";
	public static final String SEARCH_PARAMETER = "SEARCH_PARAMETER";
	
	private static final int START_RESULTS = 20;
	private static final int RESULTS_INCREMENT = 20;

	private ResultsAdapter adapter;
	private ListView resultsList;
	private Boolean followingGPS = true;
	private SearchAsyncTask searchTask;

	private Button btnMoreResults;

	private Boolean waitingForLocation = false;
	private final Object waitingForLocationLocker = new Object();
	private BaseSearchParameter currentSearch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSearchParameter();
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.results_view);

		adapter = new ResultsAdapter(this, OsmPoiApplication.getCurrentLocation(), OsmPoiApplication.formatters);
		adapter.setLocale(OsmPoiApplication.Config.getResultLanguage());
		resultsList = (ListView) findViewById(R.id.listResults);
		View footer = getLayoutInflater().inflate(R.layout.results_view_footer, null);

		//button may become cancel/more only when will be possible to know currant state searching/idle
		btnMoreResults = ((Button) footer.findViewById(R.id.btnMoreResults));
		btnMoreResults.setOnClickListener(this);

		resultsList.addFooterView(footer);
		resultsList.setAdapter(adapter);

		resultsList.setOnItemClickListener(this);

		Entity[] savedData = (Entity[]) getLastNonConfigurationInstance();
		if (savedData != null)
			adapter.addItems(savedData);
		else{
			search();
		}
	}

	private void getSearchParameter() {
		Bundle extras = getIntent().getExtras();
		currentSearch = (BaseSearchParameter)extras.getParcelable(SEARCH_PARAMETER);
		currentSearch.setMaxResults(START_RESULTS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		return adapter.getAllItems();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		updateCountView();
		if (followingGPS)
			OsmPoiApplication.locationManager.setLocationChangeListener(this);
		
		OsmPoiApplication.orientationManager.setOrientationChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		cancelCurrentTask();
		OsmPoiApplication.locationManager.setLocationChangeListener(null);
		OsmPoiApplication.orientationManager.setOrientationChangeListener(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.results_menu, menu);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.mnu_follow).setVisible(!followingGPS);
		menu.findItem(R.id.mnu_stop_follow).setVisible(followingGPS);
		menu.findItem(R.id.mnu_cancel).setVisible(searchTask != null && searchTask.getStatus() == Status.RUNNING && !searchTask.isCancelled());
		menu.findItem(R.id.mnu_refresh).setVisible(searchTask == null || searchTask.getStatus() == Status.FINISHED || searchTask.isCancelled());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnu_follow:
			OsmPoiApplication.locationManager.setLocationChangeListener(this);
			followingGPS = true;
			return true;
		case R.id.mnu_stop_follow:
			OsmPoiApplication.locationManager.setLocationChangeListener(null);
			followingGPS = false;
			return true;
		case R.id.mnu_cancel:
			cancelCurrentTask();
			return true;
		case R.id.mnu_refresh:
			adapter.clear();
			search();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateCountView() {
		TextView txt = (TextView) findViewById(R.id.txtCount);
		int count = adapter.getCount();
		int dist = adapter.getMaximumDistance();

		txt.setText(getResources().getQuantityString(R.plurals.results_within, count, count, Util.formatDistance(dist)));
	}

	private void updateAccuracyView() {
		Location curLocation = OsmPoiApplication.getCurrentLocation();
		TextView txtAccuracy = (TextView) findViewById(R.id.textAccuracy);
		Util.updateAccuracyText(txtAccuracy, curLocation);
	}

	private void search() {
		if (waitingForLocation) return;
		if (false == OsmPoiApplication.hasLocation()) {
			waitingForLocation = true;
			Util.showWaiting(this, null, getString(R.string.waiting_for_location), new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					ResultsActivity.this.waitingForLocation = false;
				}
			});
			return;
		}
		
		if (currentSearch instanceof SearchByKeyValue) {
			try {
				((SearchByKeyValue)currentSearch).getMatcher();
			} catch (InvalidParameterException e) {
				Toast.makeText(this, getText(R.string.cant_parse) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			((SearchByKeyValue)currentSearch).setCenter(OsmPoiApplication.getCurrentLocationPoint());
		}else if (currentSearch instanceof SearchAround){
			((SearchAround)currentSearch).setCenter(OsmPoiApplication.getCurrentLocationPoint());
		}
		
		cancelCurrentTask();
		updateCountView();

		searchTask = new SearchAsyncTask(this, new SearchPipe<Entity>() {
			@Override
			public void pushItem(Entity item) {
				adapter.addItem(item);
				updateCountView();
			}

			@Override
			public void pushRadius(int radius) {
				adapter.setRadius(radius);
				updateCountView();
			}

		}, new Action() {
			@Override
			public void onAction() {
				setProgressBarIndeterminateVisibility(false);
				Toast.makeText(ResultsActivity.this, getText(R.string.search_finished), Toast.LENGTH_SHORT).show();
				searchTask = null;
				btnMoreResults.setEnabled(true);
			}
		}, new Action() {
			@Override
			public void onAction() {
				if (searchTask == null) {
					setProgressBarIndeterminateVisibility(false);
					Toast.makeText(ResultsActivity.this, getText(R.string.search_cancelled), Toast.LENGTH_SHORT).show();
				}
				btnMoreResults.setEnabled(true);
			}
		});

		setProgressBarIndeterminateVisibility(true);
		btnMoreResults.setEnabled(false);
		searchTask.execute(currentSearch);
		Toast.makeText(this, OsmPoiApplication.searchSource.getName(), Toast.LENGTH_SHORT).show();
		return;
	}

	public void cancelCurrentTask() {
		if (searchTask != null && searchTask.getStatus() != Status.FINISHED) {
			SearchAsyncTask t = searchTask;
			searchTask = null;
			t.cancel(true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		cancelCurrentTask();
		
		Entity entity = (Entity) resultsList.getItemAtPosition(position);

		Intent intent = new Intent(this, ResultItemActivity.class);
		if (entity instanceof Node)
			intent.putExtra(ResultItemActivity.ENTITY, new ParcelableNode((Node) entity));
		else if (entity instanceof Way)
			intent.putExtra(ResultItemActivity.ENTITY, new ParcelableWay((Way) entity));
		else if (entity instanceof Relation)
			intent.putExtra(ResultItemActivity.ENTITY, new ParcelableRelation((Relation) entity));

		startActivity(intent);

		// ResultItemDialog dialog = new ResultItemDialog(this, entity);
		// dialog.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnMoreResults) {
			int max = currentSearch.getMaxResults();
			currentSearch.setMaxResults(max + RESULTS_INCREMENT);
			search();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see il.yrtimid.osm.osmpoi.LocationChangeManager.LocationChangeListener#
	 * OnLocationChanged(android.location.Location)
	 */
	@Override
	public void OnLocationChanged(Location loc) {
		adapter.setLocation(loc);
		updateAccuracyView();
		updateCountView();
		synchronized (waitingForLocationLocker) {
			if (waitingForLocation && OsmPoiApplication.hasLocation()) {
				waitingForLocation = false;
				Util.dismissWaiting();
				search();
			}
		}
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.OrientationChangeManager.OrientationChangeListener#OnOrientationChanged(float)
	 */
	@Override
	public void OnOrientationChanged(float azimuth) {
		adapter.setAzimuth(azimuth);
	}

}
