package com.andrew.converter;

import java.util.Locale;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("titleCaseNameConverter")
public class TitleCaseNameConverter implements Converter<String> {

	@Override
	public String getAsObject(FacesContext context, UIComponent component, String value) {
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, String value) {
		if (value == null)
			return "";

		String s = value.toString().trim().replaceAll("\\s+", " ");
		if (s.isEmpty())
			return s;

		Locale vi = new Locale("vi", "VN");
		String[] parts = s.split(" ");
		StringBuilder out = new StringBuilder();

		for (String w : parts) {
			if (w.isEmpty())
				continue;
			String first = w.substring(0, 1).toUpperCase(vi);
			String rest = (w.length() > 1) ? w.substring(1).toLowerCase(vi) : "";
			if (out.length() > 0)
				out.append(" ");
			out.append(first).append(rest);
		}
		return out.toString();
	}
}
