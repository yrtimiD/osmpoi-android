/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.dal.DbAnalyzer;
import android.app.TabActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
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

	DbAnalyzer db = null;
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

		
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (info != null){
			TextView appNameText = (TextView)findViewById(R.id.text_about_app_name);
			appNameText.setText(getString(R.string.app_name) +" " +info.versionName);
		}
		db = new DbAnalyzer(this);
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
		if (tag.equals(APP)) {
			return inflater.inflate(R.layout.about_app, null);
		} else if (tag.equals(HELP)) {
			return inflater.inflate(R.layout.about_help, null);
		} else if (tag.equals(COPYRIGHT)) {
			return inflater.inflate(R.layout.about_copyright, null);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();

		if (db != null)
			db.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		populateCount(R.id.textNodesCount, R.id.progressNodesCount, new ItemsCounter() {
			@Override
			public Long getCount() {
				return db.getNodesCount();
			}
		});

		populateCount(R.id.textWaysCount, R.id.progressWaysCount, new ItemsCounter() {
			@Override
			public Long getCount() {
				return db.getWaysCount();
			}
		});

		populateCount(R.id.textRelationsCount, R.id.progressRelationsCount, new ItemsCounter() {
			@Override
			public Long getCount() {
				return db.getRelationsCount();
			}
		});
	}

	public void populateCount(int textId, int progressId, final ItemsCounter counter) {
		final TextView countText = (TextView) findViewById(textId);
		final View countProgress = findViewById(progressId);
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
}
