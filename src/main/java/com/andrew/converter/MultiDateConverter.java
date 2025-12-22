package com.andrew.converter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("multiDateConverter")
public class MultiDateConverter implements Converter<Date> {

	private static final String DISPLAY_PATTERN = "dd/MM/yyyy";

	private static final int YY_PIVOT = 40;

	@Override
	public Date getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null)
			return null;

		String s = value.trim();
		if (s.isEmpty())
			return null;

		s = s.replaceAll("\\s+", "");
		if (!s.matches("^[0-9/\\-\\s]+$")) {
			throw new ConverterException(new FacesMessage("Date of birth only accept numbers"));
		}
		Date parsed = parseFlexible(s);

		if (parsed == null) {
			throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Date of Birth",
					"Accepted formats : dd/MM/yyyy, dd-MM-yyyy, ddMMyyyy, dd/MM/yy, dd-MM-yy, ddMMyy. "
							+ "with yy: 00-40 => 20yy, 41-99 => 19yy."));
		}

		return parsed;

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Date value) {
		if (value == null)
			return "";

		SimpleDateFormat out = new SimpleDateFormat(DISPLAY_PATTERN);
		out.setLenient(false);
		return out.format(value);

	}

	private Date parseFlexible(String s) {
		if (s.contains("/") || s.contains("-")) {
			String sep = s.contains("/") ? "/" : "-";
			String[] parts = s.split(java.util.regex.Pattern.quote(sep));
			if (parts.length != 3)
				return null;

			Integer dd = toInt(parts[0]);
			Integer mm = toInt(parts[1]);
			Integer yyOrYyyy = toInt(parts[2]);
			if (dd == null || mm == null || yyOrYyyy == null)
				return null;

			int year;
			if (parts[2].length() == 2) {
				year = resolveTwoDigitYear(yyOrYyyy);
			} else if (parts[2].length() == 4) {
				year = yyOrYyyy;
			} else {
				return null;
			}

			return buildStrictDate(dd, mm, year);
		}

		if (s.matches("^\\d{6}$")) {
			int dd = Integer.parseInt(s.substring(0, 2));
			int mm = Integer.parseInt(s.substring(2, 4));
			int yy = Integer.parseInt(s.substring(4, 6));
			int year = resolveTwoDigitYear(yy);
			return buildStrictDate(dd, mm, year);
		}

		if (s.matches("^\\d{8}$")) {
			int dd = Integer.parseInt(s.substring(0, 2));
			int mm = Integer.parseInt(s.substring(2, 4));
			int year = Integer.parseInt(s.substring(4, 8));
			return buildStrictDate(dd, mm, year);
		}

		return null;
	}

	private int resolveTwoDigitYear(int yy) {
		if (yy < 0 || yy > 99)
			throw new ConverterException(new FacesMessage("yy out of range"));
		return (yy <= YY_PIVOT) ? (2000 + yy) : (1900 + yy);
	}

	private Date buildStrictDate(int day, int month, int year) {
		final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		if (year > currentYear)
			throw new ConverterException(new FacesMessage("Year of DoB not exitsed"));
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);
		cal.clear();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, day);

		try {
			return cal.getTime();
		} catch (Exception e) {
			return null;
		}
	}

	private Integer toInt(String s) {
		if (s == null || s.isEmpty() || !s.matches("^\\d+$"))
			return null;
		return Integer.parseInt(s);
	}

}
