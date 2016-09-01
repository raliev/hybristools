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
package de.hybris.platform.acceleratorstorefrontcommons.forms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;


/**
 * Form for forgotten password
 */
public class ForgottenPwdForm
{
	private String email;

	/**
	 * @return the email
	 */
	@NotNull(message = "{forgottenPwd.email.invalid}")
	@Size(min = 1, max = 255, message = "{forgottenPwd.email.invalid}")
	@Email(message = "{forgottenPwd.email.invalid}")
	public String getEmail()
	{
		return email;
	}

	/**
	 * @param email
	 *           the email to set
	 */
	public void setEmail(final String email)
	{
		this.email = email;
	}


}
