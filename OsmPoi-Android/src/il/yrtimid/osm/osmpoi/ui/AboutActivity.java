/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import org.w3c.dom.Text;

import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.dal.DbAnalyzer;
import android.app.TabActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * @author yrtimid
 * 
 */
public class AboutActivity extends TabActivity implements TabHost.TabContentFactory {

	private interface ItemsCounter {
		public Long getCount();
	}

	DbAnalyzer poiDb = null;
	DbAnalyzer addrDb = null;
	LayoutInflater inflater;

	static final String APP = "app";
	static final String HELP = "help";
	static final String COPYRIGHT = "copyright";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflater = LayoutInflater.from(this);
		setContentView(R.layout.about);

		poiDb = new DbAnalyzer(this, OsmPoiApplication.Config.getPoiDbLocation());
		addrDb = new DbAnalyzer(this, OsmPoiApplication.Config.getAddressDbLocation());
		
		TabHost host = getTabHost();
		TabSpec spec;

		spec = host.newTabSpec(APP);
		spec.setIndicator(getString(R.string.about));
		spec.setContent(this);
		host.addTab(spec);

		spec = host.newTabSpec(HELP);
		spec.setIndicator(getString(R.string.help));
		spec.setContent(this);
		host.addTab(spec);

		spec = host.newTabSpec(COPYRIGHT);
		spec.setIndicator(getString(R.string.copyrights));
		spec.setContent(this);
		host.addTab(spec);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String
	 * )
	 */
	@Override
	public View createTabContent(String tag) {
		View v = null;
		if (tag.equals(APP)) {
			v = inflater.inflate(R.layout.about_app, null);
			populateVersion(v);
			populateDbStats(v);
		} else if (tag.equals(HELP)) {
			v = inflater.inflate(R.layout.about_help, null);
		} else if (tag.equals(COPYRIGHT)) {
			v = inflater.inflate(R.layout.about_copyright, null);
			populateCopyrightTexts(v);
		}

		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();

		if (poiDb != null)
			poiDb.close();
		if (addrDb != null)
			addrDb.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	public void populateCount(View v, int dbStatsElementId, String name, final ItemsCounter counter) {
		View dbStatsElement = v.findViewById(dbStatsElementId);
		TextView nameText = (TextView) dbStatsElement.findViewById(R.id.textName);
		nameText.setText(name);
		final TextView countText = (TextView) dbStatsElement.findViewById(R.id.textCount);
		final View countProgress = dbStatsElement.findViewById(R.id.progressCount);
		countText.setVisibility(View.GONE);
		countProgress.setVisibility(View.VISIBLE);

		AsyncTask<Void, Void, Long> taskCount = new AsyncTask<Void, Void, Long>() {
			Long startTime;
			Long finishTime;

			@Override
			protected Long doInBackground(Void... params) {
				startTime = System.currentTimeMillis();
				Long result = counter.getCount();
				finishTime = System.currentTimeMillis();
				return result;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Long result) {
				super.onPostExecute(result);
				countProgress.setVisibility(View.GONE);
				countText.setVisibility(View.VISIBLE);
				countText.setText(result.toString() + " (" + (finishTime - startTime) / 1000 + "sec)");
			}
		};

		taskCount.execute();
	}

	private void populateVersion(View view){
		TextView appNameText = (TextView)view.findViewById(R.id.text_about_app_name);
		appNameText.setText(getString(R.string.app_name));
		
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info != null){
			appNameText.setText(getString(R.string.app_name) +" "+ info.versionName);
		}
	}
	
	private void populateCopyrightTexts(View view){
		TextView aboutMapIconsText = (TextView)view.findViewById(R.id.about_map_icons);
		aboutMapIconsText.setMovementMethod(LinkMovementMethod.getInstance());
		aboutMapIconsText.setText(Html.fromHtml(getString(R.string.about_map_icons)));
	}
	
	private void populateDbStats(View v){
		populateCount(v, R.id.poi_nodes, "Nodes", new ItemsCounter() {
			@Override
			public Long getCount() {
				return poiDb.getNodesCount();
			}
		});

		populateCount(v, R.id.poi_ways, "Ways", new ItemsCounter() {
			@Override
			public Long getCount() {
				return poiDb.getWaysCount();
			}
		});

		populateCount(v, R.id.poi_relations, "Relations", new ItemsCounter() {
			@Override
			public Long getCount() {
				return poiDb.getRelationsCount();
			}
		});

		populateCount(v, R.id.poi_cells, "Cells", new ItemsCounter() {
			@Override
			public Long getCount() {
				return poiDb.getCellsCount();
			}
		});

		
		populateCount(v, R.id.addr_nodes, "Nodes", new ItemsCounter() {
			@Override
			public Long getCount() {
				return addrDb.getNodesCount();
			}
		});

		populateCount(v, R.id.addr_ways, "Ways", new ItemsCounter() {
			@Override
			public Long getCount() {
				return addrDb.getWaysCount();
			}
		});

		populateCount(v, R.id.addr_relations, "Relations", new ItemsCounter() {
			@Override
			public Long getCount() {
				return addrDb.getRelationsCount();
			}
		});
	}
}
