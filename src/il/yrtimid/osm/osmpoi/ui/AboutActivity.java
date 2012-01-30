/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;



import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.dal.DbAnalyzer;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * @author yrtimid
 * 
 */
public class AboutActivity extends Activity {
	
	private interface ItemsCounter{ public Long getCount();}
	
	DbAnalyzer db = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		db = new DbAnalyzer(this);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
		if (db != null) db.close();
	}
	
	/* (non-Javadoc)
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
		final TextView countText = (TextView)findViewById(textId);
		final View countProgress = findViewById(progressId);
		countText.setVisibility(View.GONE);
		countProgress.setVisibility(View.VISIBLE);
		
		AsyncTask<Void, Void, Long> taskCount = new AsyncTask<Void, Void, Long>(){
			Long startTime;
			Long finishTime;
			
			@Override
			protected Long doInBackground(Void... params) {
				startTime = System.currentTimeMillis();
				Long result = counter.getCount();
				finishTime = System.currentTimeMillis();
				return result;
			}
			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Long result) {
				super.onPostExecute(result);
				countProgress.setVisibility(View.GONE);
				countText.setVisibility(View.VISIBLE);
				countText.setText(result.toString()+" ("+(finishTime-startTime)/1000+"sec)");
			}
		};
		
		taskCount.execute();
	}
}
