package com.andrew.validator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("dobRangeValidator")
public class DobRangeValidator implements Validator<Date> {

	private static final int MIN_WORKING_AGE = 18;
	private static final int MAX_WORKING_AGE = 60;

	@Override
	public void validate(FacesContext context, UIComponent component, Date value) {
		if (value == null)
			return;
		LocalDate dob = value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int birthYear = dob.getYear();
		int currentYear = LocalDate.now().getYear();

		int minYear = currentYear - MAX_WORKING_AGE;
		int maxYear = currentYear - MIN_WORKING_AGE;

		if (birthYear < minYear || birthYear > maxYear) {
			if (component instanceof EditableValueHolder) {
				((EditableValueHolder) component).setSubmittedValue(null);
			}
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Date of Birth",
					String.format("Year of birth must be between %d and %d.", minYear, maxYear)));
		}
	}
}
