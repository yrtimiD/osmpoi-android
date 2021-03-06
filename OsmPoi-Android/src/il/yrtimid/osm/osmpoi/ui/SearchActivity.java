package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.LocationChangeManager.LocationChangeListener;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.categories.CategoriesLoader;
import il.yrtimid.osm.osmpoi.categories.Category;
import il.yrtimid.osm.osmpoi.dal.DbStarred;
import il.yrtimid.osm.osmpoi.formatters.EntityFormattersLoader;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;

import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Main view with category list and other search options (starred items, custom search)
 * @author yrtimid
 *
 */
public class SearchActivity extends Activity implements LocationChangeListener, OnItemClickListener {

	private static final String EXTRA_CATEGORY = "CATEGORY";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		setupCategories();
		setupFormatters();
	}

	private void setupCategories() {
		if (OsmPoiApplication.mainCategory == null) 		
			OsmPoiApplication.mainCategory = CategoriesLoader.load(this);
				
		ListView list = (ListView)findViewById(R.id.listCategories);
		
		Bundle extras = getIntent().getExtras();
		Category cat;
		if (extras!=null && extras.containsKey(EXTRA_CATEGORY)){
			cat = (Category) extras.getParcelable(EXTRA_CATEGORY);
		}else {
			cat = OsmPoiApplication.mainCategory;
		}
		
		CategoriesListAdapter adapter = new CategoriesListAdapter(this, cat);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	private void setupFormatters(){
		if (OsmPoiApplication.formatters == null) 		
			OsmPoiApplication.formatters = EntityFormattersLoader.load(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		OsmPoiApplication.Config.reloadConfig(this);
		checkSearchSource();

		OsmPoiApplication.locationManager.setLocationChangeListener(this);
		
		//CharSequence lastSearch = Preferences.getLastSearch(this);
		//EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
		//txtSearch.setText(lastSearch);

	}

	@Override
	protected void onPause() {
		super.onPause();
		
		OsmPoiApplication.locationManager.setLocationChangeListener(null);


		//EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
		//Preferences.setLastSearch(this, txtSearch.getText());

		OsmPoiApplication.locationManager.setLocationChangeListener(null);
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		super.onStop();
		//if (OsmPoiApplication.searchSource != null)
			//OsmPoiApplication.searchSource.close();
		// The activity is no longer visible (it is now "stopped")
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mnu_settings:
			Intent pref = new Intent(this, Preferences.class);
			startActivity(pref);
			return true;
			
		case R.id.mnu_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void checkSearchSource() {
		View mainLayout = findViewById(R.id.layoutMain);
		View warning = findViewById(R.id.layoutNoSearchSource_ref);

		if (OsmPoiApplication.searchSource == null) {
			mainLayout.setVisibility(View.GONE);
			warning.setVisibility(View.VISIBLE);
		} else {
			mainLayout.setVisibility(View.VISIBLE);
			warning.setVisibility(View.GONE);
		}
	}

	private void search(BaseSearchParameter search) {
		Intent intent = new Intent(this, ResultsActivity.class);
		
		//intent.putExtra(ResultsActivity.SEARCH_TYPE, searchType);
		if (search instanceof SearchByKeyValue){
			intent.putExtra(ResultsActivity.SEARCH_PARAMETER, (SearchByKeyValue)search);
		}else if (search instanceof SearchById){
			intent.putExtra(ResultsActivity.SEARCH_PARAMETER, (SearchById)search);
		} 
		
		startActivity(intent);
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Category cat = (Category)parent.getItemAtPosition(position);
		switch (cat.getType()){
		case NONE:
			showCategory(cat);
			break;
		case STARRED:
			DbStarred dbStarredHelper = OsmPoiApplication.databases.getStarredDb();
			Collection<Category> starred = dbStarredHelper.getAllStarred();
			cat.getSubCategories().clear();
			cat.getSubCategories().addAll(starred);
			showCategory(cat);
			break;
		case CUSTOM:
			LayoutInflater inflater = LayoutInflater.from(this);
	        final EditText textEntryView = (EditText)inflater.inflate(R.layout.custom_search, null);
	        new AlertDialog.Builder(this)
	        	.setTitle(R.string.custom_search)
            	.setView(textEntryView)
            	.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
        			search(new SearchByKeyValue(textEntryView.getText().toString()));
                }
            })
            .create()
            .show();
			break;
		case SEARCH:
			search(cat.getSearchParameter());
			break;
		case INLINE_SEARCH:
			if (cat.isSubCategoriesFetched()){
				showCategory(cat);
			}else {
				AsyncTask<Category, Void, Category> asyncLoad = new AsyncTask<Category, Void, Category>(){
	
					@Override
					protected Category doInBackground(Category... params) {
						CategoriesLoader.loadInlineCategories(SearchActivity.this, params[0]);		
						return params[0];
					}
					@Override
					protected void onPostExecute(Category result) {
						super.onPostExecute(result);
						Util.dismissWaiting();
						SearchActivity.this.showCategory(result);
					}
				};
				Util.showWaiting(this, getString(R.string.category), null);
				asyncLoad.execute(cat);
			}
			break;
		}
	}

	private void showCategory(Category cat) {
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra(EXTRA_CATEGORY, cat);
		startActivity(intent);
	}

	/* (non-Javadoc)
	 * @see il.yrtimid.osm.osmpoi.LocationManager.LocationChangeListener#OnLocationChanged(android.location.Location)
	 */
	@Override
	public void OnLocationChanged(Location loc) {
		if (OsmPoiApplication.getCurrentLocation() != null) {
			TextView txtAccuracy = (TextView) findViewById(R.id.textAccuracy);
			Util.updateAccuracyText(txtAccuracy, OsmPoiApplication.getCurrentLocation());
		}
	}
	


}