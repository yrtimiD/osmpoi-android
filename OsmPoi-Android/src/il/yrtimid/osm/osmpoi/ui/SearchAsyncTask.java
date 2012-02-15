package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.CancelFlag;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.ItemPipe;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.parcelables.SearchParameters;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


public class SearchAsyncTask extends AsyncTask<SearchParameters, Entity, Boolean> {
	
	public class AsyncTaskCancelFlag extends CancelFlag{
		AsyncTask<?,?,?> task;
		/**
		 * 
		 */
		public AsyncTaskCancelFlag(AsyncTask<?, ?, ?> task) {
			this.task = task;
		}
		
		/* (non-Javadoc)
		 * @see il.yrtimid.osm.osmpoi.CancelFlag#isCancelled()
		 */
		@Override
		public boolean isCancelled() {
			return task.isCancelled();
		}
		/* (non-Javadoc)
		 * @see il.yrtimid.osm.osmpoi.CancelFlag#isNotCancelled()
		 */
		@Override
		public boolean isNotCancelled() {
			return !task.isCancelled();
		}
		
		/* (non-Javadoc)
		 * @see il.yrtimid.osm.osmpoi.CancelFlag#cancel()
		 */
		@Override
		public void cancel() {
			task.cancel(true);
		}
	}
	
	ItemPipe<Entity> newItemNotifier;
	Action onFinish;
	Action onCancel;
	CancelFlag cancelFlag;
	Context context;
	
	public SearchAsyncTask(Context context, ItemPipe<Entity> newItemNotifier, Action onFinish, Action onCancel) {
		this.context = context;
		this.newItemNotifier = newItemNotifier;
		this.onFinish = onFinish;
		this.onCancel = onCancel;
	}

	@Override
	protected Boolean doInBackground(SearchParameters... task) {
		this.cancelFlag = new AsyncTaskCancelFlag(this);
		if (OsmPoiApplication.searchSource == null) return false;
		try{
			ItemPipe<Entity> notifier = new ItemPipe<Entity>() {
				@Override
				public void pushItem(Entity item) {
					SearchAsyncTask.this.publishProgress(item);
				}
			};
	
			if (this.isCancelled()) return false;
			Log.d("Search task started");
			if (task[0].hasExpression()){
				TagMatcher matcher = TagMatcher.parse(task[0].getExpression());
				OsmPoiApplication.searchSource.getByDistanceAndKeyValue(task[0], matcher, notifier, this.cancelFlag);
			}else {
				OsmPoiApplication.searchSource.getByDistance(task[0], notifier, this.cancelFlag);
			}
			return true;
		}catch(Exception e){
			Log.wtf("doInBackground", e);
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(Entity... values) {
		super.onProgressUpdate(values);
		for (Entity e: values){
			this.newItemNotifier.pushItem(e);
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result == false){
			Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
		}
		if (onFinish != null)
			onFinish.onAction();
		Log.d("Search task finished");
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onCancelled()
	 */
	@Override
	protected void onCancelled() {
		super.onCancelled();
		if (onCancel != null)
			onCancel.onAction();
		Log.d("Search task canceled");
	}
}
