package com.muratonnet.map.markerclusterer;

import java.util.ArrayList;
import java.util.UUID;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class MarkerCluster {

	// Id of cluster
	public String id;
	// The Google map to attach to.
	public GoogleMap map;
	// The grid size of a cluster in pixels.
	public int gridSize;
	// Wether the center of each cluster should be the average of all markers in
	// the cluster.
	public boolean averageCenter;

	// center of cluster
	public LatLng center;
	// bounds of cluster
	public LatLngBounds bounds;
	
	// Markers of this cluster.
	public ArrayList<Marker> markers;

	public MarkerCluster(GoogleMap map, int gridSize, boolean averageCenter) {
		this.id = UUID.randomUUID().toString();
		this.map = map;
		this.gridSize = gridSize;
		this.averageCenter = averageCenter;
		this.markers = new ArrayList<Marker>();
	}

	public void addMarker(Marker marker) {
		if (center == null) {
			center = marker.getPosition();
			calculateBounds();
		} else {
			if (averageCenter) {
				int l = markers.size() + 1;
				double lat = (center.latitude * (l - 1) + marker.getPosition().latitude)
						/ l;
				double lng = (center.longitude * (l - 1) + marker.getPosition().longitude)
						/ l;
				center = new LatLng(lat, lng);
				calculateBounds();
			}
		}

		markers.add(marker);
	}

	public boolean markerInBounds(Marker marker) {
		return bounds.contains(marker.getPosition());
	}

	private void calculateBounds() {
		LatLngBounds centerBounds = new LatLngBounds(center, center);
		bounds = new MapUtils().getExtendedBounds(map, gridSize, centerBounds);
	};

}