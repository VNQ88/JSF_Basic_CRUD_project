package com.andrew.converter;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("titleCaseNameConverter")
public class TitleCaseNameConverter implements Converter<String> {

	@Override
	public String getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null)
			return null;

		String s = value.trim().replaceAll("\\s+", " ");
		return s.isEmpty() ? null : s;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, String value) {
		if (value == null)
			return "";

		Locale vi = new Locale("vi", "VN");

		String s = value.trim().replaceAll("\\s+", " ");
		return Arrays.stream(s.split(" ")).filter(w -> !w.isEmpty())
				.map(w -> w.substring(0, 1).toUpperCase(vi) + w.substring(1).toLowerCase(vi))
				.collect(Collectors.joining(" "));
	}
}
