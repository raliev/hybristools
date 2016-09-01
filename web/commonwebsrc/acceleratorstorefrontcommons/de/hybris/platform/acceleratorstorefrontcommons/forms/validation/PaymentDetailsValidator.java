/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import de.hybris.platform.acceleratorservices.util.CalendarHelper;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PaymentDetailsForm;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


@Component("paymentDetailsValidator")
public class PaymentDetailsValidator implements Validator
{
	@Override
	public boolean supports(final Class<?> aClass)
	{
		return PaymentDetailsForm.class.equals(aClass);
	}

	@Override
	public void validate(final Object object, final Errors errors)
	{
		final PaymentDetailsForm form = (PaymentDetailsForm) object;

		final Calendar start = CalendarHelper.parseDate(form.getStartMonth(), form.getStartYear());
		final Calendar expiration = CalendarHelper.parseDate(form.getExpiryMonth(), form.getExpiryYear());

		if (start != null && expiration != null && start.after(expiration))
		{
			errors.rejectValue("startMonth", "payment.startDate.invalid");
		}

		final boolean editMode = StringUtils.isNotBlank(form.getPaymentId());
		if (editMode || Boolean.TRUE.equals(form.getNewBillingAddress()))
		{
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.titleCode", "address.title.invalid");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.firstName", "address.firstName.invalid");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.lastName", "address.lastName.invalid");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.line1", "address.line1.invalid");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.townCity", "address.townCity.invalid");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.postcode", "address.postcode.invalid");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.countryIso", "address.country.invalid");
			// ValidationUtils.rejectIfEmptyOrWhitespace(errors, "billingAddress.line2", "address.line2.invalid"); // for some addresses this field is required by cybersource
		}
	}
}
