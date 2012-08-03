package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.CancelFlag;
import il.yrtimid.osm.osmpoi.Log;
import il.yrtimid.osm.osmpoi.OsmPoiApplication;
import il.yrtimid.osm.osmpoi.R;
import il.yrtimid.osm.osmpoi.ResultItem;
import il.yrtimid.osm.osmpoi.SearchPipe;
import il.yrtimid.osm.osmpoi.domain.*;
import il.yrtimid.osm.osmpoi.searchparameters.BaseSearchParameter;
import il.yrtimid.osm.osmpoi.searchparameters.SearchAround;
import il.yrtimid.osm.osmpoi.searchparameters.SearchById;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByKeyValue;
import il.yrtimid.osm.osmpoi.searchparameters.SearchByParentId;
import il.yrtimid.osm.osmpoi.tagmatchers.TagMatcher;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;


public class SearchAsyncTask extends AsyncTask<BaseSearchParameter, ResultItem, Boolean> {
	
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
	
	SearchPipe<Entity> newItemNotifier;
	Action onFinish;
	Action onCancel;
	CancelFlag cancelFlag;
	Context context;
	
	public SearchAsyncTask(Context context, SearchPipe<Entity> newItemNotifier, Action onFinish, Action onCancel) {
		this.context = context;
		this.newItemNotifier = newItemNotifier;
		this.onFinish = onFinish;
		this.onCancel = onCancel;
	}

	@Override
	protected Boolean doInBackground(BaseSearchParameter... task) {
		this.cancelFlag = new AsyncTaskCancelFlag(this);
		if (OsmPoiApplication.searchSource == null) return false;
		try{
			SearchPipe<Entity> notifier = new SearchPipe<Entity>() {
				@Override
				public void pushItem(Entity item) {
					SearchAsyncTask.this.publishProgress(new ResultItem(item, null));
				}

				@Override
				public void pushRadius(int radius) {
					SearchAsyncTask.this.publishProgress(new ResultItem(null, radius));
					
				}
				
			};
	
			if (this.isCancelled()) return false;
			Log.d("Search task started");
			OsmPoiApplication.searchSource.search(task[0], notifier, this.cancelFlag);

			return true;
		}catch(Exception e){
			Log.wtf("doInBackground", e);
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(ResultItem... values) {
		super.onProgressUpdate(values);
		for (ResultItem ri: values){
			if (ri.entity != null)
				this.newItemNotifier.pushItem(ri.entity);
			
			if (ri.radius != null)
				this.newItemNotifier.pushRadius(ri.radius);
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
