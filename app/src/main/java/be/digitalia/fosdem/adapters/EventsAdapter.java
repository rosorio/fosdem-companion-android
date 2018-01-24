package me.osorio.eurobsd.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import me.osorio.eurobsd.R;
import me.osorio.eurobsd.activities.EventDetailsActivity;
import me.osorio.eurobsd.db.DatabaseManager;
import me.osorio.eurobsd.model.Event;
import me.osorio.eurobsd.model.Track;
import me.osorio.eurobsd.utils.DateUtils;
import me.osorio.eurobsd.widgets.MultiChoiceHelper;

public class EventsAdapter extends RecyclerViewCursorAdapter<EventsAdapter.ViewHolder> {

	protected final LayoutInflater inflater;
	protected final DateFormat timeDateFormat;
	private final boolean showDay;

	public EventsAdapter(Context context) {
		this(context, true);
	}

	public EventsAdapter(Context context, boolean showDay) {
		inflater = LayoutInflater.from(context);
		timeDateFormat = DateUtils.getTimeDateFormat(context);
		this.showDay = showDay;
	}

	@Override
	public Event getItem(int position) {
		return DatabaseManager.toEvent((Cursor) super.getItem(position));
	}

	@Override
	public int getItemViewType(int position) {
		return R.layout.item_event;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = inflater.inflate(R.layout.item_event, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
		Context context = holder.itemView.getContext();
		Event event = DatabaseManager.toEvent(cursor, holder.event);
		holder.event = event;

		holder.title.setText(event.getTitle());
		boolean isBookmarked = DatabaseManager.toBookmarkStatus(cursor);
		Drawable bookmarkDrawable = isBookmarked
				? AppCompatDrawableManager.get().getDrawable(context, R.drawable.ic_bookmark_grey600_24dp)
				: null;
		TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(holder.title, null, null, bookmarkDrawable, null);
		holder.title.setContentDescription(isBookmarked
				? context.getString(R.string.in_bookmarks_content_description, event.getTitle())
				: null
		);
		String personsSummary = event.getPersonsSummary();
		holder.persons.setText(personsSummary);
		holder.persons.setVisibility(TextUtils.isEmpty(personsSummary) ? View.GONE : View.VISIBLE);
		Track track = event.getTrack();
		holder.trackName.setText(track.getName());
		holder.trackName.setTextColor(ContextCompat.getColor(holder.trackName.getContext(), track.getType().getColorResId()));
		holder.trackName.setContentDescription(context.getString(R.string.track_content_description, track.getName()));

		Date startTime = event.getStartTime();
		Date endTime = event.getEndTime();
		String startTimeString = (startTime != null) ? timeDateFormat.format(startTime) : "?";
		String endTimeString = (endTime != null) ? timeDateFormat.format(endTime) : "?";
		String details;
		if (showDay) {
			details = String.format("%1$s, %2$s ― %3$s  |  %4$s", event.getDay().getShortName(), startTimeString, endTimeString, event.getRoomName());
		} else {
			details = String.format("%1$s ― %2$s  |  %3$s", startTimeString, endTimeString, event.getRoomName());
		}
		holder.details.setText(details);
		holder.details.setContentDescription(context.getString(R.string.details_content_description, details));
	}

	static class ViewHolder extends MultiChoiceHelper.ViewHolder implements View.OnClickListener {
		TextView title;
		TextView persons;
		TextView trackName;
		TextView details;

		Event event;

		public ViewHolder(View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);
			persons = (TextView) itemView.findViewById(R.id.persons);
			trackName = (TextView) itemView.findViewById(R.id.track_name);
			details = (TextView) itemView.findViewById(R.id.details);
			setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			Context context = view.getContext();
			Intent intent = new Intent(context, EventDetailsActivity.class)
					.putExtra(EventDetailsActivity.EXTRA_EVENT, event);
			context.startActivity(intent);
		}
	}
}
