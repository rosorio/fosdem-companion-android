package me.osorio.eurobsd.api;

import java.util.Locale;

/**
 * This class contains all FOSDEM Urls
 *
 * @author Christophe Beyls
 */
public class FosdemUrls {

	private static final String SCHEDULE_URL = "http://osorio.in/xml";
	private static final String EVENT_URL_FORMAT = "https://fosdem.org/%1$d/schedule/event/%2$s/";
	private static final String PERSON_URL_FORMAT = "https://fosdem.org/%1$d/schedule/speaker/%2$s/";
	private static final String LOCAL_NAVIGATION_URL = "http://nav.fosdem.org/";
	private static final String LOCAL_NAVIGATION_TO_ROOM_URL_FORMAT = "http://nav.fosdem.org/d/%1$s/";

	public static String getSchedule() {
		return SCHEDULE_URL;
	}

	public static String getEvent(String slug, int year) {
		return String.format(Locale.US, EVENT_URL_FORMAT, year, slug);
	}

	public static String getPerson(String slug, int year) {
		return String.format(Locale.US, PERSON_URL_FORMAT, year, slug);
	}

	public static String getLocalNavigation() {
		return LOCAL_NAVIGATION_URL;
	}

	public static String getLocalNavigationToLocation(String locationSlug) {
		return String.format(Locale.US, LOCAL_NAVIGATION_TO_ROOM_URL_FORMAT, locationSlug);
	}
}
