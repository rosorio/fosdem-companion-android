package me.osorio.eurobsd.model;

import android.text.TextUtils;

public enum Building {
	one, two, three, zero, Unknown;

	public static Building fromRoomName(String roomName) {
		if ("Karnak".equalsIgnoreCase(roomName)) {
			return zero;
		}
		if ("Auditorium".equalsIgnoreCase(roomName)) {
			return one;
		}
		if ("Louxor".equalsIgnoreCase(roomName)) {
			return two;
		}
		if ("Karnak".equalsIgnoreCase(roomName)) {
			return three;
		}
		if ("Déndarah".equalsIgnoreCase(roomName)) {
			return three;
		}
		return Unknown;
	}
}
