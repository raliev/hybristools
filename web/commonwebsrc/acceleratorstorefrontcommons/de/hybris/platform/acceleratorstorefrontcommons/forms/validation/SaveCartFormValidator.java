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

import de.hybris.platform.acceleratorstorefrontcommons.forms.SaveCartForm;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component("saveCartFormValidator")
public class SaveCartFormValidator implements Validator
{
	@Override
	public boolean supports(final Class<?> aClass)
	{
		return SaveCartForm.class.equals(aClass);
	}

	@Override
	public void validate(final Object object, final Errors errors)
	{
		final SaveCartForm saveCartForm = (SaveCartForm) object;
		final String name = saveCartForm.getName();
		final String description = saveCartForm.getDescription();

		if (StringUtils.isBlank(name))
		{
			errors.rejectValue("name", "basket.save.cart.validation.name.notBlank");
			return;
		}

		if (!StringUtils.isAlphanumericSpace(name))
		{
			errors.rejectValue("name", "basket.save.cart.validation.name.charset");
			return;
		}

		if (StringUtils.length(name) > 255)
		{
			errors.rejectValue("name", "basket.save.cart.validation.name.size");
			return;
		}

		if (StringUtils.length(description) > 255)
		{
			errors.rejectValue("description", "basket.save.cart.validation.description.size");
			return;
		}
	}
}
