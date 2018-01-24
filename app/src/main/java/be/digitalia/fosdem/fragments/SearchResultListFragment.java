package me.osorio.eurobsd.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.adapters.EventsAdapter;
import me.osorio.eurobsd.db.DatabaseManager;
import me.osorio.eurobsd.loaders.SimpleCursorLoader;

public class SearchResultListFragment extends RecyclerViewFragment implements LoaderCallbacks<Cursor> {

	private static final int EVENTS_LOADER_ID = 1;
	private static final String ARG_QUERY = "query";

	private EventsAdapter adapter;

	public static SearchResultListFragment newInstance(String query) {
		SearchResultListFragment f = new SearchResultListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_QUERY, query);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EventsAdapter(getActivity());
	}

	@Override
	protected void onRecyclerViewCreated(RecyclerView recyclerView, Bundle savedInstanceState) {
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setEmptyText(getString(R.string.no_search_result));
		setProgressBarVisible(true);

		getLoaderManager().initLoader(EVENTS_LOADER_ID, null, this);
	}

	private static class TextSearchLoader extends SimpleCursorLoader {

		private final String query;

		public TextSearchLoader(Context context, String query) {
			super(context);
			this.query = query;
		}

		@Override
		protected Cursor getCursor() {
			return DatabaseManager.getInstance().getSearchResults(query);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String query = getArguments().getString(ARG_QUERY);
		return new TextSearchLoader(getActivity(), query);
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
}
