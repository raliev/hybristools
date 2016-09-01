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
package de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation;


import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface CheckoutStepValidator
{
	/**
	 * This method should be implemented to validate whether all the required details are present before entering the checkout step.
	 * @param redirectAttributes
	 * @return ValidationResults
	 */
	ValidationResults validateOnEnter(final RedirectAttributes redirectAttributes);

	/**
	 * Does validation while transitioning from one CheckoutStep to another.
	 * Usually used when we want to redirect to a completely different checkout step and override the default behaviour.
	 * @return ValidationResults
	 */
	ValidationResults validateOnExit();
}
