package com.andrew.component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.andrew.common.InputType;

@FacesComponent("app.CustomInput")
public class CustomInput extends HtmlInputText {

	protected enum PropertyKeys {
		type, _wired
	}

	private static final int MIN_WORKING_AGE = 18;
	private static final int MAX_WORKING_AGE = 60;

	private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}]+([\\s\\-'][\\p{L}]+)*$");

	public String getType() {
		Object v = getStateHelper().eval(PropertyKeys.type, InputType.TEXT.name());
		return v == null ? InputType.TEXT.name() : v.toString();
	}

	public void setType(String type) {
		getStateHelper().put(PropertyKeys.type, type);
		getStateHelper().remove(PropertyKeys._wired);
	}

	@Override
	public void processValidators(FacesContext context) {
		ensureWired(context);
		super.processValidators(context);
	}

	@Override
	protected void validateValue(FacesContext context, Object newValue) {
		super.validateValue(context, newValue);

		if (!isValid()) {
			return;
		}

		if (newValue == null) {
			return;
		}
		InputType t = InputType.from(getType());

		switch (t) {
		case NAME:
			validateFullName(context, (String) newValue);
			break;

		case DOB:
			validateDobRange(context, (Date) newValue);
			break;

		default:
			break;
		}
	}

	private void ensureWired(FacesContext context) {
		if (Boolean.TRUE.equals(getStateHelper().eval(PropertyKeys._wired))) {
			return;
		}

		Application app = context.getApplication();
		InputType t = InputType.from(getType());

		if (getConverter() == null) {
			Converter<?> c = resolveConverter(app, t);
			if (c != null) {
				setConverter(c);
			}
		}

		getStateHelper().put(PropertyKeys._wired, Boolean.TRUE);
	}

	private Converter<?> resolveConverter(Application app, InputType type) {
		switch (type) {
		case NAME:
			return app.createConverter("titleCaseNameConverter");
		case DOB:
			return app.createConverter("multiDateConverter");
		default:
			return null;
		}
	}

	private void validateFullName(FacesContext context, String value) {
		if (value == null)
			return;

		String trimmed = value.trim();
		if (trimmed.isEmpty())
			return;

		if (!NAME_PATTERN.matcher(trimmed).matches()) {
			fail(context, "Invalid FullName", "Only letters, hyphens, apostrophes, and spaces are allowed.", true);
		}
	}

	private void validateDobRange(FacesContext context, Date value) {
		if (value == null)
			return;

		LocalDate dob = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int birthYear = dob.getYear();
		int currentYear = LocalDate.now().getYear();

		int minYear = currentYear - MAX_WORKING_AGE;
		int maxYear = currentYear - MIN_WORKING_AGE;

		if (birthYear < minYear || birthYear > maxYear) {
			fail(context, "Invalid Date of Birth",
					String.format("Year of birth must be between %d and %d.", minYear, maxYear), true);
		}
	}

	private void fail(FacesContext context, String summary, String detail, boolean clearInput) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);

		context.addMessage(getClientId(context), msg);

		setValid(false);
		context.validationFailed();

		if (clearInput) {
			setSubmittedValue(null);
			setValue(null);
			setLocalValueSet(false);
		}
	}

}
