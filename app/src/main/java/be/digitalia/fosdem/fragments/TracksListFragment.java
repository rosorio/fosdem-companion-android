package me.osorio.eurobsd.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.activities.TrackScheduleActivity;
import me.osorio.eurobsd.adapters.RecyclerViewCursorAdapter;
import me.osorio.eurobsd.db.DatabaseManager;
import me.osorio.eurobsd.loaders.SimpleCursorLoader;
import me.osorio.eurobsd.model.Day;
import me.osorio.eurobsd.model.Track;

public class TracksListFragment extends RecyclerViewFragment implements LoaderCallbacks<Cursor> {

	private static final int TRACKS_LOADER_ID = 1;
	private static final String ARG_DAY = "day";

	Day day;
	private TracksAdapter adapter;

	public static TracksListFragment newInstance(Day day) {
		TracksListFragment f = new TracksListFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_DAY, day);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new TracksAdapter();
		day = getArguments().getParcelable(ARG_DAY);
	}

	@Override
	protected void onRecyclerViewCreated(RecyclerView recyclerView, Bundle savedInstanceState) {
		Fragment parentFragment = getParentFragment();
		if (parentFragment instanceof RecycledViewPoolProvider) {
			recyclerView.setRecycledViewPool(((RecycledViewPoolProvider) parentFragment).getRecycledViewPool());
		}

		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText(getString(R.string.no_data));
		setProgressBarVisible(true);

		getLoaderManager().initLoader(TRACKS_LOADER_ID, null, this);
	}

	private static class TracksLoader extends SimpleCursorLoader {

		private final Day day;

		public TracksLoader(Context context, Day day) {
			super(context);
			this.day = day;
		}

		@Override
		protected Cursor getCursor() {
			return DatabaseManager.getInstance().getTracks(day);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new TracksLoader(getActivity(), day);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null) {
			adapter.swapCursor(data);
		}

		setProgressBarVisible(false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	private class TracksAdapter extends RecyclerViewCursorAdapter<TrackViewHolder> {

		private final LayoutInflater inflater;

		public TracksAdapter() {
			inflater = LayoutInflater.from(getContext());
		}

		@Override
		public Track getItem(int position) {
			return DatabaseManager.toTrack((Cursor) super.getItem(position));
		}

		@Override
		public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = inflater.inflate(R.layout.simple_list_item_2_material, parent, false);
			return new TrackViewHolder(view);
		}

		@Override
		public void onBindViewHolder(TrackViewHolder holder, Cursor cursor) {
			holder.day = day;
			holder.track = DatabaseManager.toTrack(cursor, holder.track);
			holder.name.setText(holder.track.getName());
			holder.type.setText(holder.track.getType().getNameResId());
			holder.type.setTextColor(ContextCompat.getColor(holder.type.getContext(), holder.track.getType().getColorResId()));
		}
	}

	static class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView name;
		TextView type;

		Day day;
		Track track;

		TrackViewHolder(View itemView) {
			super(itemView);
			name = (TextView) itemView.findViewById(android.R.id.text1);
			type = (TextView) itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			Context context = view.getContext();
			Intent intent = new Intent(context, TrackScheduleActivity.class)
					.putExtra(TrackScheduleActivity.EXTRA_DAY, day)
					.putExtra(TrackScheduleActivity.EXTRA_TRACK, track);
			context.startActivity(intent);
		}
	}
}
