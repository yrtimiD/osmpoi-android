package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.R;
import android.app.AlertDialog;
import android.content.Context;
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
}
