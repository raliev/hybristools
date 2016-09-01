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

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


public abstract class AbstractCheckoutStepValidator implements CheckoutStepValidator
{
	private CheckoutFacade checkoutFacade;
	private CheckoutFlowFacade checkoutFlowFacade;

	@Deprecated
	protected static final Logger LOG = Logger.getLogger(AbstractCheckoutStepValidator.class);

	@Override
	public abstract ValidationResults validateOnEnter(final RedirectAttributes redirectAttributes);

	@Override
	public ValidationResults validateOnExit()
	{
		return ValidationResults.SUCCESS;
	}

	public CheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

	@Required
	public void setCheckoutFacade(final CheckoutFacade checkoutFacade)
	{
		this.checkoutFacade = checkoutFacade;
	}

	public CheckoutFlowFacade getCheckoutFlowFacade()
	{
		return checkoutFlowFacade;
	}

	@Required
	public void setCheckoutFlowFacade(final CheckoutFlowFacade checkoutFlowFacade)
	{
		this.checkoutFlowFacade = checkoutFlowFacade;
	}

}
