package me.osorio.eurobsd.utils;

import android.content.Context;

import java.text.DateFormat;
import java.util.TimeZone;

public class DateUtils {

	private static final TimeZone ROMANIAN_TIME_ZONE = TimeZone.getTimeZone("GMT+2");

	public static TimeZone getRomanianTimeZone() {
		return ROMANIAN_TIME_ZONE;
	}

	public static DateFormat withRomanianTimeZone(DateFormat format) {
		format.setTimeZone(ROMANIAN_TIME_ZONE);
		return format;
	}

	public static DateFormat getTimeDateFormat(Context context) {
		return withRomanianTimeZone(android.text.format.DateFormat.getTimeFormat(context));
	}
}
