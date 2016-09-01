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

import de.hybris.platform.acceleratorstorefrontcommons.forms.UpdateEmailForm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Validator for Emails
 */
@Component("emailValidator")
public class EmailValidator implements Validator
{

	public static final Pattern EMAIL_REGEX = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b");

	@Override
	public boolean supports(final Class<?> aClass)
	{
		return UpdateEmailForm.class.equals(aClass);
	}

	@Override
	public void validate(final Object object, final Errors errors)
	{
		final UpdateEmailForm updateEmailForm = (UpdateEmailForm) object;
		final String email = updateEmailForm.getEmail();
		final String chkEmail = updateEmailForm.getChkEmail();
		final String password = updateEmailForm.getPassword();

		if (StringUtils.isEmpty(email))
		{
			errors.rejectValue("email", "profile.email.invalid");
		}
		else if (StringUtils.length(email) > 255 || !EMAIL_REGEX.matcher(email).matches())
		{
			errors.rejectValue("email", "profile.email.invalid");
		}

		if (StringUtils.isEmpty(chkEmail))
		{
			errors.rejectValue("chkEmail", "profile.checkEmail.invalid");
		}

		if (StringUtils.isEmpty(password))
		{
			errors.rejectValue("password", "profile.pwd.invalid");
		}
	}
}
