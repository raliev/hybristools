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
package de.hybris.platform.acceleratorstorefrontcommons.checkout.steps;

import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults;


public interface StepTransition
{
	/**
	 * Used for determining how to transition from the current step
	 * 
	 * @param transition
	 *           the transition key
	 * @return {@link String} containing the name of the next step
	 */
	String go(final String transition);

	/**
	 * 
	 * Determines the URL from Spring config based on
	 * {@link de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults}.
	 * 
	 * @param validationResult
	 * @return {@link String} containing the redirect url from the spring configuration.
	 */
	String onValidation(final ValidationResults validationResult);

}
