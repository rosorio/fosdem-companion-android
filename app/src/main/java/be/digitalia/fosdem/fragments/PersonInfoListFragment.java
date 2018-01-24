package me.osorio.eurobsd.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.ConcatAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.adapters.EventsAdapter;
import me.osorio.eurobsd.db.DatabaseManager;
import me.osorio.eurobsd.loaders.SimpleCursorLoader;
import me.osorio.eurobsd.model.Person;

public class PersonInfoListFragment extends RecyclerViewFragment implements LoaderCallbacks<Cursor> {

	private static final int PERSON_EVENTS_LOADER_ID = 1;
	private static final String ARG_PERSON = "person";

	private Person person;
	private EventsAdapter adapter;

	public static PersonInfoListFragment newInstance(Person person) {
		PersonInfoListFragment f = new PersonInfoListFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PERSON, person);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new EventsAdapter(getActivity());
		person = getArguments().getParcelable(ARG_PERSON);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.person, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	@Override
	protected void onRecyclerViewCreated(RecyclerView recyclerView, Bundle savedInstanceState) {
		final int contentMargin = getResources().getDimensionPixelSize(R.dimen.content_margin);
		recyclerView.setPadding(contentMargin, contentMargin, contentMargin, contentMargin);
		recyclerView.setClipToPadding(false);
		recyclerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		recyclerView.setAdapter(new ConcatAdapter(new HeaderAdapter(), adapter));
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText(getString(R.string.no_data));
		setProgressBarVisible(true);

		getLoaderManager().initLoader(PERSON_EVENTS_LOADER_ID, null, this);
	}

	private static class PersonEventsLoader extends SimpleCursorLoader {

		private final Person person;

		public PersonEventsLoader(Context context, Person person) {
			super(context);
			this.person = person;
		}

		@Override
		protected Cursor getCursor() {
			return DatabaseManager.getInstance().getEvents(person);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new PersonEventsLoader(getActivity(), person);
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

	static class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.ViewHolder> {

		@Override
		public int getItemCount() {
			return 1;
		}

		@Override
		public int getItemViewType(int position) {
			return R.layout.header_person_info;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_person_info, null);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			// Nothing to bind
		}

		static class ViewHolder extends RecyclerView.ViewHolder {

			public ViewHolder(View itemView) {
				super(itemView);
			}
		}
	}
}
