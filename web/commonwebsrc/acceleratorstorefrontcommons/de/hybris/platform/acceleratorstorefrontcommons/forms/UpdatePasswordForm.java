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
 * Form object for updating the password.
 */
public class UpdatePasswordForm
{
	private String currentPassword;
	private String newPassword;
	private String checkNewPassword;

	public String getCurrentPassword()
	{
		return currentPassword;
	}

	public void setCurrentPassword(final String currentPassword)
	{
		this.currentPassword = currentPassword;
	}

	public String getNewPassword()
	{
		return newPassword;
	}

	public void setNewPassword(final String newPassword)
	{
		this.newPassword = newPassword;
	}

	public String getCheckNewPassword()
	{
		return checkNewPassword;
	}

	public void setCheckNewPassword(final String checkNewPassword)
	{
		this.checkNewPassword = checkNewPassword;
	}
}
