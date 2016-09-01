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

import de.hybris.platform.acceleratorstorefrontcommons.forms.RestoreSaveCartForm;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Validator for restore save cart.
 */
@Component("restoreSaveCartFormValidator")
public class RestoreSaveCartFormValidator implements Validator
{

	@Override
	public boolean supports(final Class<?> aClass)
	{
		return RestoreSaveCartForm.class.equals(aClass);
	}

	@Override
	public void validate(final Object object, final Errors errors)
	{
		final RestoreSaveCartForm restoreSaveCartForm = (RestoreSaveCartForm) object;
		if (restoreSaveCartForm.getCartName() != null && !restoreSaveCartForm.isPreventSaveActiveCart())
		{
			final String cartName = restoreSaveCartForm.getCartName();

			if (StringUtils.isBlank(cartName))
			{
				errors.rejectValue("cartName", "basket.save.cart.validation.name.notBlank");
				return;
			}

			if (!StringUtils.isAlphanumericSpace(cartName))
			{
				errors.rejectValue("cartName", "basket.save.cart.validation.name.charset");
				return;
			}

			if (StringUtils.length(cartName) > 255)
			{
				errors.rejectValue("cartName", "basket.save.cart.validation.name.size");
				return;
			}
		}
	}
}
