package me.osorio.eurobsd.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.viewpagerindicator.UnderlinePageIndicator;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.db.DatabaseManager;
import me.osorio.eurobsd.fragments.EventDetailsFragment;
import me.osorio.eurobsd.loaders.TrackScheduleLoader;
import me.osorio.eurobsd.model.Day;
import me.osorio.eurobsd.model.Track;
import me.osorio.eurobsd.utils.NfcUtils;
import me.osorio.eurobsd.utils.NfcUtils.CreateNfcAppDataCallback;
import me.osorio.eurobsd.utils.ThemeUtils;
import me.osorio.eurobsd.widgets.ContentLoadingProgressBar;

/**
 * Event view of the track schedule; allows to slide between events of the same track using a ViewPager.
 *
 * @author Christophe Beyls
 */
public class TrackScheduleEventActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, CreateNfcAppDataCallback {

	public static final String EXTRA_DAY = "day";
	public static final String EXTRA_TRACK = "track";
	public static final String EXTRA_POSITION = "position";

	private static final int EVENTS_LOADER_ID = 1;

	private Day day;
	private Track track;
	private int initialPosition = -1;
	private ContentLoadingProgressBar progress;
	private ViewPager pager;
	private UnderlinePageIndicator pageIndicator;
	private TrackScheduleEventAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.track_schedule_event);

		Bundle extras = getIntent().getExtras();
		day = extras.getParcelable(EXTRA_DAY);
		track = extras.getParcelable(EXTRA_TRACK);

		progress = (ContentLoadingProgressBar) findViewById(R.id.progress);
		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new TrackScheduleEventAdapter(getSupportFragmentManager());
		pageIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
		pageIndicator.setSelectedColor(ContextCompat.getColor(this, track.getType().getColorResId()));

		if (savedInstanceState == null) {
			initialPosition = extras.getInt(EXTRA_POSITION, -1);
			pager.setAdapter(adapter);
			pageIndicator.setViewPager(pager);
		}

		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle(track.toString());
		bar.setSubtitle(day.toString());
		ThemeUtils.setActionBarTrackColor(this, track.getType());

		// Enable Android Beam
		NfcUtils.setAppDataPushMessageCallbackIfAvailable(this, this);

		setCustomProgressVisibility(true);
		getSupportLoaderManager().initLoader(EVENTS_LOADER_ID, null, this);
	}

	private void setCustomProgressVisibility(boolean isVisible) {
		if (isVisible) {
			progress.show();
		} else {
			progress.hide();
		}
	}

	@Override
	public byte[] createNfcAppData() {
		if (adapter.getCount() == 0) {
			return null;
		}
		long eventId = adapter.getItemId(pager.getCurrentItem());
		if (eventId == -1L) {
			return null;
		}
		return String.valueOf(eventId).getBytes();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new TrackScheduleLoader(this, day, track);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		setCustomProgressVisibility(false);

		if (data != null) {
			adapter.setCursor(data);

			// Delay setting the adapter when the instance state is restored
			// to ensure the current position is restored properly
			if (pager.getAdapter() == null) {
				pager.setAdapter(adapter);
				pageIndicator.setViewPager(pager);
			}

			if (initialPosition != -1) {
				pager.setCurrentItem(initialPosition, false);
				initialPosition = -1;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.setCursor(null);
	}

	public static class TrackScheduleEventAdapter extends FragmentStatePagerAdapter {

		private Cursor cursor;

		public TrackScheduleEventAdapter(FragmentManager fm) {
			super(fm);
		}

		public Cursor getCursor() {
			return cursor;
		}

		public void setCursor(Cursor cursor) {
			this.cursor = cursor;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return (cursor == null) ? 0 : cursor.getCount();
		}

		@Override
		public Fragment getItem(int position) {
			cursor.moveToPosition(position);
			return EventDetailsFragment.newInstance(DatabaseManager.toEvent(cursor));
		}

		public long getItemId(int position) {
			if (!cursor.moveToPosition(position)) {
				return -1L;
			}
			return cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
		}
	}
}
