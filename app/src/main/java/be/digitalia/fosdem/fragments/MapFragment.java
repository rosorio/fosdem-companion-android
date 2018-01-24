package me.osorio.eurobsd.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.api.FosdemUrls;

public class MapFragment extends Fragment {

	private static final double DESTINATION_LATITUDE = 50.812375;
	private static final double DESTINATION_LONGITUDE = 4.380734;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_map, container, false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.navigation:
				launchLocalNavigation();
				return true;
		}
		return false;
	}

	private void launchDirections() {
		// Build intent to start Google Maps directions
		String uri = String.format(Locale.US,
				"https://maps.google.com/maps?f=d&daddr=%1$f,%2$f&dirflg=r",
				DESTINATION_LATITUDE, DESTINATION_LONGITUDE);

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

		try {
			startActivity(intent);
		} catch (ActivityNotFoundException ignore) {
		}
	}

	private void launchLocalNavigation() {
		try {
			Activity context = getActivity();
			new CustomTabsIntent.Builder()
					.setToolbarColor(ContextCompat.getColor(context, R.color.color_primary))
					.setShowTitle(true)
					.build()
					.launchUrl(context, Uri.parse(FosdemUrls.getLocalNavigation()));
		} catch (ActivityNotFoundException ignore) {
		}
	}
}
