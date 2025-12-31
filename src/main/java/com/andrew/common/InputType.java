package com.andrew.common;

public enum InputType {
	TEXT, NAME, DOB;

	public static InputType from(String s) {
		if (s == null || s.isEmpty())
			return TEXT;
		try {
			return InputType.valueOf(s.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return TEXT;
		}
	}
}
