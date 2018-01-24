package me.osorio.eurobsd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.fragments.EventDetailsFragment;
import me.osorio.eurobsd.fragments.RoomImageDialogFragment;
import me.osorio.eurobsd.fragments.TrackScheduleListFragment;
import me.osorio.eurobsd.model.Day;
import me.osorio.eurobsd.model.Event;
import me.osorio.eurobsd.model.Track;
import me.osorio.eurobsd.utils.NfcUtils;
import me.osorio.eurobsd.utils.NfcUtils.CreateNfcAppDataCallback;
import me.osorio.eurobsd.utils.ThemeUtils;

/**
 * Track Schedule container, works in both single pane and dual pane modes.
 *
 * @author Christophe Beyls
 */
public class TrackScheduleActivity extends AppCompatActivity
		implements TrackScheduleListFragment.Callbacks,
		EventDetailsFragment.FloatingActionButtonProvider,
		CreateNfcAppDataCallback {

	public static final String EXTRA_DAY = "day";
	public static final String EXTRA_TRACK = "track";
	// Optional extra used as a hint for up navigation from an event
	public static final String EXTRA_FROM_EVENT_ID = "from_event_id";

	private Day day;
	private Track track;
	private boolean isTabletLandscape;
	private Event lastSelectedEvent;

	private ImageView floatingActionButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_schedule);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		floatingActionButton = (ImageView) findViewById(R.id.fab);

		Bundle extras = getIntent().getExtras();
		day = extras.getParcelable(EXTRA_DAY);
		track = extras.getParcelable(EXTRA_TRACK);

		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle(track.toString());
		bar.setSubtitle(day.toString());
		setTitle(String.format("%1$s, %2$s", track.toString(), day.toString()));
		ThemeUtils.setActionBarTrackColor(this, track.getType());

		isTabletLandscape = getResources().getBoolean(R.bool.tablet_landscape);

		TrackScheduleListFragment trackScheduleListFragment;
		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			long fromEventId = extras.getLong(EXTRA_FROM_EVENT_ID, -1L);
			if (fromEventId != -1L) {
				trackScheduleListFragment = TrackScheduleListFragment.newInstance(day, track, fromEventId);
			} else {
				trackScheduleListFragment = TrackScheduleListFragment.newInstance(day, track);
			}
			fm.beginTransaction().add(R.id.schedule, trackScheduleListFragment).commit();
		} else {
			trackScheduleListFragment = (TrackScheduleListFragment) fm.findFragmentById(R.id.schedule);

			// Cleanup after switching from dual pane to single pane mode
			if (!isTabletLandscape) {
				FragmentTransaction ft = null;

				Fragment eventDetailsFragment = fm.findFragmentById(R.id.event);
				if (eventDetailsFragment != null) {
					ft = fm.beginTransaction();
					ft.remove(eventDetailsFragment);
				}

				Fragment roomImageDialogFragment = fm.findFragmentByTag(RoomImageDialogFragment.TAG);
				if (roomImageDialogFragment != null) {
					if (ft == null) {
						ft = fm.beginTransaction();
					}
					ft.remove(roomImageDialogFragment);
				}

				if (ft != null) {
					ft.commit();
				}
			}
		}
		trackScheduleListFragment.setSelectionEnabled(isTabletLandscape);

		if (isTabletLandscape) {
			// Enable Android Beam
			NfcUtils.setAppDataPushMessageCallbackIfAvailable(this, this);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return false;
	}

	// TrackScheduleListFragment.Callbacks

	@Override
	public void onEventSelected(int position, Event event) {
		if (isTabletLandscape) {
			// Tablet mode: Show event details in the right pane fragment
			lastSelectedEvent = event;

			FragmentManager fm = getSupportFragmentManager();
			EventDetailsFragment currentFragment = (EventDetailsFragment) fm.findFragmentById(R.id.event);
			if (event != null) {
				// Only replace the fragment if the event is different
				if ((currentFragment == null) || !currentFragment.getEvent().equals(event)) {
					Fragment f = EventDetailsFragment.newInstance(event);
					// Allow state loss since the event fragment will be synchronized with the list selection after activity re-creation
					fm.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).replace(R.id.event, f).commitAllowingStateLoss();
				}
			} else {
				// Nothing is selected because the list is empty
				if (currentFragment != null) {
					fm.beginTransaction().remove(currentFragment).commitAllowingStateLoss();
				}
			}
		} else {
			// Classic mode: Show event details in a new activity
			Intent intent = new Intent(this, TrackScheduleEventActivity.class);
			intent.putExtra(TrackScheduleEventActivity.EXTRA_DAY, day);
			intent.putExtra(TrackScheduleEventActivity.EXTRA_TRACK, track);
			intent.putExtra(TrackScheduleEventActivity.EXTRA_POSITION, position);
			startActivity(intent);
		}
	}

	// EventDetailsFragment.FloatingActionButtonProvider

	@Override
	public ImageView getActionButton() {
		return floatingActionButton;
	}

	// CreateNfcAppDataCallback

	@Override
	public byte[] createNfcAppData() {
		if (lastSelectedEvent == null) {
			return null;
		}
		return String.valueOf(lastSelectedEvent.getId()).getBytes();
	}
}
