/**
 * 
 */
package il.yrtimid.osm.osmpoi.ui;

import il.yrtimid.osm.osmpoi.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @author yrtimid
 *
 */
public class ConfirmDialog {
	public interface Action{
		public void PositiveAction();
	}
	public static void Confirm(Context context, String message, final Action action){
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					action.PositiveAction();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				default:
					break;
				}
			}
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
			.setNegativeButton(context.getText(R.string.cancel), listener)
			.setPositiveButton(context.getText(R.string.Continue), listener)
			.show();
	}
}
