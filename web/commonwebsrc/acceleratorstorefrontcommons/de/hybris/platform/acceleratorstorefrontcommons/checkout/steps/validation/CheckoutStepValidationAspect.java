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


import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutGroup;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;


public class CheckoutStepValidationAspect
{
	private AcceleratorCheckoutFacade checkoutFacade;
	private Map<String, CheckoutGroup> checkoutFlowGroupMap;

	public Object validateCheckoutStep(final ProceedingJoinPoint pjp) throws Throwable // NOSONAR
	{
		final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
		final PreValidateCheckoutStep preValidateCheckoutStep = methodSignature.getMethod().getAnnotation(
				PreValidateCheckoutStep.class);
		final CheckoutGroup checkoutGroup = getCheckoutFlowGroupMap().get(getCheckoutFacade().getCheckoutFlowGroupForCheckout());
		final CheckoutStep checkoutStepBean = checkoutGroup.getCheckoutStepMap().get(preValidateCheckoutStep.checkoutStep());

		final ValidationResults validationResults = checkoutStepBean.validate((RedirectAttributesModelMap) pjp.getArgs()[1]);

		if (checkoutStepBean.checkIfValidationErrors(validationResults))
		{
			return checkoutStepBean.onValidation(validationResults);
		}
		return pjp.proceed();
	}

	public AcceleratorCheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

	@Required
	public void setCheckoutFacade(final AcceleratorCheckoutFacade checkoutFacade)
	{
		this.checkoutFacade = checkoutFacade;
	}

	public Map<String, CheckoutGroup> getCheckoutFlowGroupMap()
	{
		return checkoutFlowGroupMap;
	}

	@Required
	public void setCheckoutFlowGroupMap(final Map<String, CheckoutGroup> checkoutFlowGroupMap)
	{
		this.checkoutFlowGroupMap = checkoutFlowGroupMap;
	}
}
