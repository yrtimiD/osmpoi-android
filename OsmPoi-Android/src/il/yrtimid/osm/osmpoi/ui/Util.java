package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.widget.TextView;

public class Util {

	/*public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (resolveInfo.size() > 0) {
			return true;
		}
		return false;
	}*/
	
	public static void showAlert(Context context, String message) {
		new AlertDialog.Builder(context).setMessage(message).show();
	}

	public static void showDebugAlert(Context context, String message) {
		new AlertDialog.Builder(context).setTitle("Debug").setMessage(message).show();
	}

	public static void updateAccuracyText(TextView txtAccuracy, Location location) {
		if (location != null && location.hasAccuracy()){
			txtAccuracy.setText(String.format("%,dm", (int) location.getAccuracy()));
		}else
			txtAccuracy.setText(R.string.NA);
	}
	
	private static ProgressDialog progressDialog = null;
	public static void showWaiting(Context context, String title, String message){
		showWaiting(context, title, message, null);
	}
	public static void showWaiting(Context context, String title, String message, OnCancelListener cancelListener){
		if (message == null) message = context.getString(R.string.loading);
		progressDialog = ProgressDialog.show(context, title, message, true, true, cancelListener);
	}
	
	public static void dismissWaiting(){
		if (progressDialog != null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	private static char[] directionChars = new char[]{'↑','↗','→','↘','↓','↙','←','↖'};
	public static char getDirectionChar(int degree){
		if (degree>=360) degree = degree % 360;
		if (degree<0) degree+=360;
		degree+=45/2;
		int section = (int)(degree/45);
		if (section == 8) section = 0;
		return directionChars[section];
	}
	
	public static int normalizeBearing(int bearing){
		bearing = bearing % 360;
		if (bearing < -180) bearing = bearing + 360;
		if (bearing > 180) bearing = bearing - 360;
		return bearing;
	}
	
}