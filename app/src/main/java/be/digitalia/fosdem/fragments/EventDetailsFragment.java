package me.osorio.eurobsd.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.activities.PersonInfoActivity;
import me.osorio.eurobsd.db.DatabaseManager;
import me.osorio.eurobsd.loaders.BookmarkStatusLoader;
import me.osorio.eurobsd.loaders.LocalCacheLoader;
import me.osorio.eurobsd.model.Building;
import me.osorio.eurobsd.model.Event;
import me.osorio.eurobsd.model.Link;
import me.osorio.eurobsd.model.Person;
import me.osorio.eurobsd.utils.DateUtils;
import me.osorio.eurobsd.utils.StringUtils;

public class EventDetailsFragment extends Fragment {

	/**
	 * Interface implemented by container activities
	 */
	public interface FloatingActionButtonProvider {
		// May return null
		ImageView getActionButton();
	}

	static class EventDetails {
		List<Person> persons;
		List<Link> links;
	}

	static class ViewHolder {
		LayoutInflater inflater;
		TextView personsTextView;
		View linksHeader;
		ViewGroup linksContainer;
	}

	private static final int BOOKMARK_STATUS_LOADER_ID = 1;
	private static final int EVENT_DETAILS_LOADER_ID = 2;

	private static final String ARG_EVENT = "event";

	Event event;
	int personsCount = 1;
	Boolean isBookmarked;
	ViewHolder holder;

	private MenuItem bookmarkMenuItem;
	private ImageView actionButton;

	public static EventDetailsFragment newInstance(Event event) {
		EventDetailsFragment f = new EventDetailsFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_EVENT, event);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		event = getArguments().getParcelable(ARG_EVENT);
	}

	public Event getEvent() {
		return event;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_event_details, container, false);

		holder = new ViewHolder();
		holder.inflater = inflater;

		((TextView) view.findViewById(R.id.title)).setText(event.getTitle());
		TextView textView = (TextView) view.findViewById(R.id.subtitle);
		String text = event.getSubTitle();
		if (TextUtils.isEmpty(text)) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setText(text);
		}

		MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();

		// Set the persons summary text first; replace it with the clickable text when the loader completes
		holder.personsTextView = (TextView) view.findViewById(R.id.persons);
		String personsSummary = event.getPersonsSummary();
		if (TextUtils.isEmpty(personsSummary)) {
			holder.personsTextView.setVisibility(View.GONE);
		} else {
			holder.personsTextView.setText(personsSummary);
			holder.personsTextView.setMovementMethod(linkMovementMethod);
			holder.personsTextView.setVisibility(View.VISIBLE);
		}


		textView = ((TextView) view.findViewById(R.id.track));
		text = event.getTrack().getName();
		textView.setText(text);
		textView.setContentDescription(getString(R.string.track_content_description, text));

		textView = ((TextView) view.findViewById(R.id.time));
		Date startTime = event.getStartTime();
		Date endTime = event.getEndTime();
		DateFormat timeDateFormat = DateUtils.getTimeDateFormat(getActivity());
		text = String.format("%1$s, %2$s ― %3$s",
				event.getDay().toString(),
				(startTime != null) ? timeDateFormat.format(startTime) : "?",
				(endTime != null) ? timeDateFormat.format(endTime) : "?");
		textView.setText(text);
		textView.setContentDescription(getString(R.string.time_content_description, text));

		textView = (TextView) view.findViewById(R.id.room);
		final String roomName = event.getRoomName();
		Spannable roomText = new SpannableString(String.format("%1$s (Floor %2$s)", roomName, Building.fromRoomName(roomName)));
		final int roomImageResId = getResources().getIdentifier(StringUtils.roomNameToResourceName(roomName), "drawable", getActivity().getPackageName());
		// If the room image exists, make the room text clickable to display it
		if (roomImageResId != 0) {
			roomText.setSpan(new ClickableSpan() {
				@Override
				public void onClick(View view) {
					RoomImageDialogFragment.newInstance(roomName, roomImageResId).show(getFragmentManager());
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(false);
				}
			}, 0, roomText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setMovementMethod(linkMovementMethod);
		}
		textView.setText(roomText);
		textView.setContentDescription(getString(R.string.room_content_description, roomText));


		textView = (TextView) view.findViewById(R.id.abstract_text);
		text = event.getAbstractText();
		if (TextUtils.isEmpty(text)) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setText(StringUtils.parseHtml(text, getResources()));
			textView.setMovementMethod(linkMovementMethod);
		}
		textView = (TextView) view.findViewById(R.id.description);
		text = event.getDescription();
		if (TextUtils.isEmpty(text)) {
			textView.setVisibility(View.GONE);
		} else {
			textView.setText(StringUtils.parseHtml(text, getResources()));
			textView.setMovementMethod(linkMovementMethod);
		}

		holder.linksHeader = view.findViewById(R.id.links_header);
		holder.linksContainer = (ViewGroup) view.findViewById(R.id.links_container);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = getActivity();
		if (activity instanceof FloatingActionButtonProvider) {
			actionButton = ((FloatingActionButtonProvider) activity).getActionButton();
			if (actionButton != null) {
				actionButton.setOnClickListener(actionButtonClickListener);
			}
		}

		// Ensure the actionButton is initialized before creating the options menu
		setHasOptionsMenu(true);

		LoaderManager loaderManager = getLoaderManager();
		loaderManager.initLoader(BOOKMARK_STATUS_LOADER_ID, null, bookmarkStatusLoaderCallbacks);
		loaderManager.initLoader(EVENT_DETAILS_LOADER_ID, null, eventDetailsLoaderCallbacks);
	}

	private final View.OnClickListener actionButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			if (isBookmarked != null) {
				new UpdateBookmarkAsyncTask(event).execute(isBookmarked);
			}
		}
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		holder = null;
		if (actionButton != null) {
			// Clear the reference to this fragment
			actionButton.setOnClickListener(null);
			actionButton = null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.event, menu);
		menu.findItem(R.id.share).setIntent(getShareChooserIntent());
		bookmarkMenuItem = menu.findItem(R.id.bookmark);
		if (actionButton != null) {
			bookmarkMenuItem.setEnabled(false).setVisible(false);
		}
		updateBookmarkMenuItem(false);
	}

	private Intent getShareChooserIntent() {
		return ShareCompat.IntentBuilder.from(getActivity())
				.setSubject(String.format("%1$s (FOSDEM)", event.getTitle()))
				.setType("text/plain")
				.setText(String.format("%1$s %2$s #FOSDEM", event.getTitle(), event.getUrl()))
				.setChooserTitle(R.string.share)
				.createChooserIntent();
	}

	void updateBookmarkMenuItem(boolean animate) {
		if (actionButton != null) {
			// Action Button is used as bookmark button

			if (isBookmarked == null) {
				actionButton.setEnabled(false);
			} else {
				// Only animate if the button was showing a previous value
				animate = animate && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
						&& actionButton.isEnabled();
				actionButton.setEnabled(true);

				if (isBookmarked) {
					actionButton.setContentDescription(getString(R.string.remove_bookmark));
					actionButton.setImageResource(animate ? R.drawable.avd_bookmark_add_24dp : R.drawable.ic_bookmark_white_24dp);
				} else {
					actionButton.setContentDescription(getString(R.string.add_bookmark));
					actionButton.setImageResource(animate ? R.drawable.avd_bookmark_remove_24dp : R.drawable.ic_bookmark_outline_white_24dp);
				}
				if (animate) {
					((Animatable) actionButton.getDrawable()).start();
				}
			}
		} else {
			// Standard menu item is used as bookmark button

			if (bookmarkMenuItem != null) {
				if (isBookmarked == null) {
					bookmarkMenuItem.setEnabled(false);
				} else {
					// Only animate if the menu item was showing a previous value
					animate = animate && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
							&& bookmarkMenuItem.isEnabled();
					bookmarkMenuItem.setEnabled(true);

					if (isBookmarked) {
						bookmarkMenuItem.setTitle(R.string.remove_bookmark);
						bookmarkMenuItem.setIcon(animate ? R.drawable.avd_bookmark_add_24dp : R.drawable.ic_bookmark_white_24dp);
					} else {
						bookmarkMenuItem.setTitle(R.string.add_bookmark);
						bookmarkMenuItem.setIcon(animate ? R.drawable.avd_bookmark_remove_24dp : R.drawable.ic_bookmark_outline_white_24dp);
					}
					if (animate) {
						((Animatable) bookmarkMenuItem.getIcon()).stop();
						((Animatable) bookmarkMenuItem.getIcon()).start();
					}
				}
			}
		}
	}

	@Override
	public void onDestroyOptionsMenu() {
		super.onDestroyOptionsMenu();
		bookmarkMenuItem = null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.bookmark:
				if (isBookmarked != null) {
					new UpdateBookmarkAsyncTask(event).execute(isBookmarked);
				}
				return true;
			case R.id.add_to_agenda:
				addToAgenda();
				return true;
		}
		return false;
	}

	private static class UpdateBookmarkAsyncTask extends AsyncTask<Boolean, Void, Void> {

		private final Event event;

		public UpdateBookmarkAsyncTask(Event event) {
			this.event = event;
		}

		@Override
		protected Void doInBackground(Boolean... remove) {
			if (remove[0]) {
				DatabaseManager.getInstance().removeBookmark(event);
			} else {
				DatabaseManager.getInstance().addBookmark(event);
			}
			return null;
		}
	}

	@SuppressLint("InlinedApi")
	private void addToAgenda() {
		Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
		intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "ULB - " + event.getRoomName());
		String description = event.getAbstractText();
		if (TextUtils.isEmpty(description)) {
			description = event.getDescription();
		}
		description = StringUtils.stripHtml(description);
		// Add speaker info if available
		if (personsCount > 0) {
			description = String.format("%1$s: %2$s\n\n%3$s", getResources().getQuantityString(R.plurals.speakers, personsCount), event.getPersonsSummary(),
					description);
		}
		intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
		Date time = event.getStartTime();
		if (time != null) {
			intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time.getTime());
		}
		time = event.getEndTime();
		if (time != null) {
			intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time.getTime());
		}
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getActivity(), R.string.calendar_not_found, Toast.LENGTH_LONG).show();
		}
	}

	private final LoaderCallbacks<Boolean> bookmarkStatusLoaderCallbacks = new LoaderCallbacks<Boolean>() {

		@Override
		public Loader<Boolean> onCreateLoader(int id, Bundle args) {
			return new BookmarkStatusLoader(getActivity(), event);
		}

		@Override
		public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
			if (isBookmarked != data) {
				isBookmarked = data;
				updateBookmarkMenuItem(true);
			}
		}

		@Override
		public void onLoaderReset(Loader<Boolean> loader) {
		}
	};

	private static class EventDetailsLoader extends LocalCacheLoader<EventDetails> {

		private final Event event;

		public EventDetailsLoader(Context context, Event event) {
			super(context);
			this.event = event;
		}

		@Override
		public EventDetails loadInBackground() {
			EventDetails result = new EventDetails();
			DatabaseManager dbm = DatabaseManager.getInstance();
			result.persons = dbm.getPersons(event);
			result.links = dbm.getLinks(event);
			return result;
		}
	}

	private final LoaderCallbacks<EventDetails> eventDetailsLoaderCallbacks = new LoaderCallbacks<EventDetails>() {

		@Override
		public Loader<EventDetails> onCreateLoader(int id, Bundle args) {
			return new EventDetailsLoader(getActivity(), event);
		}

		@Override
		public void onLoadFinished(Loader<EventDetails> loader, EventDetails data) {
			// 1. Persons
			if (data.persons != null) {
				personsCount = data.persons.size();
				if (personsCount > 0) {
					// Build a list of clickable persons
					SpannableStringBuilder sb = new SpannableStringBuilder();
					int length = 0;
					for (Person person : data.persons) {
						if (length != 0) {
							sb.append(", ");
						}
						String name = person.getName();
						sb.append(name);
						length = sb.length();
						sb.setSpan(new PersonClickableSpan(person), length - name.length(), length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					holder.personsTextView.setText(sb);
					holder.personsTextView.setVisibility(View.VISIBLE);
				}
			}

			// 2. Links
			holder.linksContainer.removeAllViews();
			if ((data.links != null) && (data.links.size() > 0)) {
				holder.linksHeader.setVisibility(View.VISIBLE);
				holder.linksContainer.setVisibility(View.VISIBLE);
				for (Link link : data.links) {
					View view = holder.inflater.inflate(R.layout.item_link, holder.linksContainer, false);
					TextView tv = (TextView) view.findViewById(R.id.description);
					tv.setText(link.getDescription());
					view.setOnClickListener(new LinkClickListener(link));
					holder.linksContainer.addView(view);
				}
			} else {
				holder.linksHeader.setVisibility(View.GONE);
				holder.linksContainer.setVisibility(View.GONE);
			}
		}

		@Override
		public void onLoaderReset(Loader<EventDetails> loader) {
		}
	};

	private static class PersonClickableSpan extends ClickableSpan {

		private final Person person;

		public PersonClickableSpan(Person person) {
			this.person = person;
		}

		@Override
		public void onClick(View v) {
			Context context = v.getContext();
			Intent intent = new Intent(context, PersonInfoActivity.class).putExtra(PersonInfoActivity.EXTRA_PERSON, person);
			context.startActivity(intent);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			ds.setUnderlineText(false);
		}
	}

	private class LinkClickListener implements View.OnClickListener {

		private final Link link;

		public LinkClickListener(Link link) {
			this.link = link;
		}

		@Override
		public void onClick(View v) {
			String url = link.getUrl();
			if (url != null) {
				try {
					Activity context = getActivity();
					new CustomTabsIntent.Builder()
							.setToolbarColor(ContextCompat.getColor(context, event.getTrack().getType().getColorResId()))
							.setShowTitle(true)
							.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
							.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
							.build()
							.launchUrl(context, Uri.parse(url));
				} catch (ActivityNotFoundException ignore) {
				}
			}
		}
	}
}