package com.muratonnet.map.markerclusterer;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapUtils {

	public Location getLocation(Context context) {
		Location location = null;
		LocationManager locationManager = ((LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE));
		if (location == null
				&& locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		if (location == null
				&& locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		return location;

	}

	public LatLngBounds getExtendedBounds(GoogleMap googleMap, int gridSize,
			LatLngBounds bounds) {
		Projection projection = googleMap.getProjection();

		// Turn the bounds into latlng.
		LatLng tr = new LatLng(bounds.northeast.latitude,
				bounds.northeast.longitude);
		LatLng bl = new LatLng(bounds.southwest.latitude,
				bounds.southwest.longitude);

		// Convert the points to pixels and the extend out by the grid size.
		Point trPoint = projection.toScreenLocation(tr);
		trPoint.x += gridSize;
		trPoint.y -= gridSize;

		Point blPoint = projection.toScreenLocation(bl);
		blPoint.x -= gridSize;
		blPoint.y += gridSize;

		// Convert the pixel points back to LatLng
		LatLng ne = projection.fromScreenLocation(trPoint);
		LatLng sw = projection.fromScreenLocation(blPoint);

		// Extend the bounds to contain the new bounds.
		LatLngBounds newBounds = new LatLngBounds(sw, ne);

		return newBounds;
	}

	public static double distanceBetweenPoints(LatLng p1, LatLng p2) {
		double R = 6371; // Radius of the Earth in km
		double lat = (p2.latitude - p1.latitude) * Math.PI / 180;
		double lon = (p2.longitude - p1.longitude) * Math.PI / 180;
		double a = Math.sin(lat / 2) * Math.sin(lat / 2)
				+ Math.cos(p1.latitude * Math.PI / 180)
				* Math.cos(p2.latitude * Math.PI / 180) * Math.sin(lon / 2)
				* Math.sin(lon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;
		return d;
	}
}
