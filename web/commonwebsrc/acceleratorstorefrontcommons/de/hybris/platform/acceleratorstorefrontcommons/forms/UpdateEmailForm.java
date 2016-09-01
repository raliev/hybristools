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

/**
 * Form object for updating email
 */
public class UpdateEmailForm
{

	private String email;
	private String chkEmail;
	private String password;


	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}


	/**
	 * @param password
	 *           the password to set
	 */
	public void setPassword(final String password)
	{
		this.password = password;
	}

	/**
	 * @return the email
	 */
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


	/**
	 * @return the chkEmail
	 */
	public String getChkEmail()
	{
		return chkEmail;
	}


	/**
	 * @param chkEmail
	 *           the chkEmail to set
	 */
	public void setChkEmail(final String chkEmail)
	{
		this.chkEmail = chkEmail;
	}

}
