package com.andrew.converter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("multiDateConverter")
public class MultiDateConverter implements Converter<Date> {

	private static final String DISPLAY_PATTERN = "dd/MM/yyyy";
	private static final ArrayList<DateTimeFormatter> DATE_FORMATTERS = new ArrayList<>(
			Arrays.asList(DateTimeFormatter.ofPattern("dd-MM-yyyy"), DateTimeFormatter.ofPattern("ddMMyyyy"),
					new DateTimeFormatterBuilder().appendValue(ChronoField.DAY_OF_MONTH, 2)
							.appendValue(ChronoField.MONTH_OF_YEAR, 2).appendValueReduced(ChronoField.YEAR, 2, 2, 1930)
							.toFormatter().withResolverStyle(ResolverStyle.STRICT),
					new DateTimeFormatterBuilder().appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('-')
							.appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-')
							.appendValueReduced(ChronoField.YEAR, 2, 2, 1930).toFormatter()
							.withResolverStyle(ResolverStyle.STRICT)));

	@Override
	public Date getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		String s = value.trim().replaceAll("[\\s/.]+", "-");

		for (DateTimeFormatter formatter : DATE_FORMATTERS) {
			try {
				LocalDate localDate = LocalDate.parse(s, formatter);

				return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

			} catch (DateTimeParseException ignored) {
			}
		}
		if (component instanceof EditableValueHolder) {
			((EditableValueHolder) component).setSubmittedValue(null);
		}
		throw new ConverterException(new FacesMessage("Invalid date format."));
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Date value) {
		if (value == null)
			return "";

		SimpleDateFormat out = new SimpleDateFormat(DISPLAY_PATTERN);
		return out.format(value);
	}
}
