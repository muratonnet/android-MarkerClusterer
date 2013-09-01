package com.muratonnet.map;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.muratonnet.map.markerclusterer.MarkerCluster;
import com.muratonnet.map.markerclusterer.MarkerClusterer;

public class MainActivity extends FragmentActivity {

	GoogleMap mMap;
	float mCurrentZoom;
	ArrayList<Marker> mMarkers;
	ArrayList<Marker> mClusterMarkers;
	boolean mIsClustered = false;
	boolean mMapNeedsSetup = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// initialize ui
		initializeUI();
		
		// initialize marker list
		mMarkers = new ArrayList<Marker>();
		mClusterMarkers = new ArrayList<Marker>();

	}

	@Override
	protected void onResume() {
		super.onResume();

		// check google play services
		int isAvailable = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (isAvailable != ConnectionResult.SUCCESS) {
			GooglePlayServicesUtil.getErrorDialog(isAvailable, this, 1).show();
		} else {
			setupMap();
		}

	}

	private void initializeUI() {
		// get ui items
		Button create = (Button) findViewById(R.id.create);
		final Button cluster = (Button) findViewById(R.id.cluster);

		// set listeners
		create.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createRandomMarkers(100);
				cluster.setText("Cluster");
				mIsClustered = false;
				redrawMap();
			}
		});

		cluster.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsClustered) {
					cluster.setText("Cluster");
					mIsClustered = false;
					redrawMap();
				} else {
					cluster.setText("Uncluster");
					createClusterMarkers();
					mIsClustered = true;
					redrawMap();
				}
			}
		});
	}

	private void setupMap() {
		if (mMapNeedsSetup) {
			if (getGoogleMap() != null) {
				mCurrentZoom = getGoogleMap().getCameraPosition().zoom;
				getGoogleMap().setOnCameraChangeListener(
						new OnCameraChangeListener() {
							@Override
							public void onCameraChange(
									CameraPosition newPosition) {
								// is clustered?
								if (mIsClustered
										&& mCurrentZoom != newPosition.zoom) {
									// create cluster markers for new position
									recreateClusterMarkers();
									// redraw map
									redrawMap();
								}
								mCurrentZoom = newPosition.zoom;
							}
						});
			}
			mMapNeedsSetup = false;
		}
	}

	private void createRandomMarkers(int numberOfMarkers) {
		// clear map
		getGoogleMap().clear();
		// clear marker lists
		mMarkers.clear();
		mClusterMarkers.clear();

		// get projection area
		Projection projection = getGoogleMap().getProjection();
		LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
		// set min/max for lat/lng
		double minLat = bounds.southwest.latitude;
		double maxLat = bounds.northeast.latitude;
		double minLng = bounds.southwest.longitude;
		double maxLng = bounds.northeast.longitude;

		// create random markers
		for (int i = 0; i < numberOfMarkers; i++) {
			// create random position
			LatLng markerPos = new LatLng(minLat
					+ (Math.random() * (maxLat - minLat)), minLng
					+ (Math.random() * (maxLng - minLng)));

			// create marker as non-visible
			MarkerOptions markerOptions = new MarkerOptions().position(
					markerPos).visible(false);
			// create marker
			Marker marker = getGoogleMap().addMarker(markerOptions);
			// add to list
			mMarkers.add(marker);
		}

	}

	private void createClusterMarkers() {

		if (mClusterMarkers.size() == 0) {
			// set cluster parameters
			int gridSize = 100;
			boolean averageCenter = false;
			// create clusters
			Marker[] markers = mMarkers.toArray(new Marker[mMarkers.size()]);
			ArrayList<MarkerCluster> markerClusters = new MarkerClusterer(
					getGoogleMap(), markers, gridSize, averageCenter)
					.createMarkerClusters();
			// create cluster markers
			for (MarkerCluster cluster : markerClusters) {
				int markerCount = cluster.markers.size();
				if (markerCount == 1) {
					mClusterMarkers.add(cluster.markers.get(0));
				} else {
					// get marker view and set text
					View markerView = getLayoutInflater().inflate(
							R.layout.cluster_marker_view, null);
					((TextView) markerView.findViewById(R.id.marker_count))
							.setText(String.valueOf(markerCount));

					// create cluster marker
					MarkerOptions markerOptions = new MarkerOptions()
							.position(cluster.center)
							.icon(BitmapDescriptorFactory
									.fromBitmap(createDrawableFromView(markerView)))
							.visible(false);
					Marker clusterMarker = getGoogleMap().addMarker(
							markerOptions);
					// add to list
					mClusterMarkers.add(clusterMarker);
				}
			}
		}
	}

	private void recreateClusterMarkers() {
		// remove cluster markers from map
		for (Marker marker : mClusterMarkers) {
			marker.remove();
		}
		// clear cluster markers list
		mClusterMarkers.clear();
		// create mew cluster markers
		createClusterMarkers();
	}

	private void redrawMap() {

		// hide all markers
		for (Marker marker : mMarkers) {
			marker.setVisible(false);
		}
		for (Marker marker : mClusterMarkers) {
			marker.setVisible(false);
		}
		// show markers
		if (mIsClustered) {
			for (Marker marker : mClusterMarkers) {
				marker.setVisible(true);
			}
		} else {
			for (Marker marker : mMarkers) {
				marker.setVisible(true);
			}
		}
	}

	private GoogleMap getGoogleMap() {
		if (mMap == null) {
			SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map));
			mMap = mapFragment.getMap();
		}
		return mMap;
	}

	public static Bitmap createDrawableFromView(View view) {

		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache(true);
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);

		return bitmap;
	}
}
