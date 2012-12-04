/**
 * 
 */
package il.yrtimid.osm.osmpoi;

import il.yrtimid.osm.osmpoi.logging.Log;

import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * @author yrtimid
 * 
 */
public class LocationChangeManager implements LocationListener {

	public interface LocationChangeListener{
		public void OnLocationChanged(Location loc);
	}
	
	private LocationManager locationManager;
	private LocationChangeListener listener;
	
	/**
	 * 
	 */
	public LocationChangeManager(Context context) {
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void setLocationChangeListener(LocationChangeListener listener){
		this.listener = listener;
		if (this.listener == null){
			locationManager.removeUpdates(this);
		}else {
			Location lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastLoc != null && (new Date().getTime() - lastLoc.getTime()) < 1000 * 60)
				onLocationChanged(lastLoc);

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
		}
	}
	
	@Override
	public void onLocationChanged(Location newLoc) {
		Log.d(String.format("Got new location: %f %f %f", newLoc.getLatitude(), newLoc.getLongitude(), newLoc.getAccuracy()));
		if (OsmPoiApplication.setCurrentLocation(newLoc)) {
			if (listener != null){
				listener.OnLocationChanged(newLoc);
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onStatusChanged(java.lang.String, int, android.os.Bundle)
	 */
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onProviderEnabled(java.lang.String)
	 */
	@Override
	public void onProviderEnabled(String provider) {
	}

	/* (non-Javadoc)
	 * @see android.location.LocationListener#onProviderDisabled(java.lang.String)
	 */
	@Override
	public void onProviderDisabled(String provider) {
	}


}
