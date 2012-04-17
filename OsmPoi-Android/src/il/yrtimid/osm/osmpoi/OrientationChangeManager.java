/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author yrtimid
 * 
 */
public class OrientationChangeManager implements SensorEventListener {

	public interface OrientationChangeListener{
		public void OnOrientationChanged(float azimuth);
	}
	
	private OrientationChangeListener listener;

	private SensorManager sensorManager;
	private Sensor orientation;

	
	/**
	 * 
	 */
	public OrientationChangeManager(Context context) {
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

	}
	
	public void setOrientationChangeListener(OrientationChangeListener listener){
		this.listener = listener;
		if (this.listener == null){
			sensorManager.unregisterListener(this);
		}else {
			sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_UI);
		}
	}

	/* (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		StringBuilder b = new StringBuilder();
		for(float f : event.values){
			b.append(f).append(" ");
		}
		//Log.d(b.toString());
		
		if (listener != null){
			listener.OnOrientationChanged(event.values[0]);
		}
	}

	/* (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
