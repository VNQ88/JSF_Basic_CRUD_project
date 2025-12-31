package com.andrew.validator;

import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("fullNameValidator")
public class FullNameValidator implements Validator<String> {

	private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}]+([\\s\\-'][\\p{L}]+)*$");

	@Override
	public void validate(FacesContext context, UIComponent component, String value) {
		if (value == null)
			return;

		String trimmed = value.trim();
		if (trimmed.isEmpty())
			return;

		if (!NAME_PATTERN.matcher(trimmed).matches()) {
			if (component instanceof EditableValueHolder) {
				((EditableValueHolder) component).setSubmittedValue(null);
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid FullName",
					"Only letters, hyphens, apostrophes, and spaces are allowed."));
		}
	}
}
