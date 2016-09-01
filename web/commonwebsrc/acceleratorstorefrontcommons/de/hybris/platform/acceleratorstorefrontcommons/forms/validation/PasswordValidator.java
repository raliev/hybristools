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

import de.hybris.platform.acceleratorstorefrontcommons.forms.UpdatePasswordForm;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Validator for password forms.
 */
@Component("passwordValidator")
public class PasswordValidator implements Validator
{
	private static final String UPDATE_PWD_INVALID = "updatePwd.pwd.invalid";

	@Override
	public boolean supports(final Class<?> aClass)
	{
		return UpdatePasswordForm.class.equals(aClass);
	}

	@Override
	public void validate(final Object object, final Errors errors)
	{
		final UpdatePasswordForm passwordForm = (UpdatePasswordForm) object;
		final String currPasswd = passwordForm.getCurrentPassword();
		final String newPasswd = passwordForm.getNewPassword();
		final String checkPasswd = passwordForm.getCheckNewPassword();

		if (StringUtils.isEmpty(currPasswd))
		{
			errors.rejectValue("currentPassword", "profile.currentPassword.invalid");
		}

		if (StringUtils.isEmpty(newPasswd))
		{
			errors.rejectValue("newPassword", UPDATE_PWD_INVALID);
		}
		else if (StringUtils.length(newPasswd) < 6 || StringUtils.length(newPasswd) > 255)
		{
			errors.rejectValue("newPassword", UPDATE_PWD_INVALID);
		}

		if (StringUtils.isEmpty(checkPasswd))
		{
			errors.rejectValue("checkNewPassword", UPDATE_PWD_INVALID);
		}
		else if (StringUtils.length(checkPasswd) < 6 || StringUtils.length(checkPasswd) > 255)
		{
			errors.rejectValue("checkNewPassword", UPDATE_PWD_INVALID);
		}
	}

}
