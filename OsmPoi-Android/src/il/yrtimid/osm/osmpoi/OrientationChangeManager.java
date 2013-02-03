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
		public void OnOrientationChanged(int azimuth);
	}
	
	private OrientationChangeListener listener;

	private SensorManager sensorManager;
	private Sensor orientation;

	private int lastValue = 0;
	
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
			sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/* (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		/*
		StringBuilder b = new StringBuilder();
		for(float f : event.values){
			b.append(f).append(" ");
		}
		Log.d(b.toString());
		*/
		
		int newValue = Math.round( event.values[0]);

		if (newValue != lastValue && listener != null){
			lastValue = newValue;
			listener.OnOrientationChanged(newValue);
		}
	}

	/* (non-Javadoc)
	 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
