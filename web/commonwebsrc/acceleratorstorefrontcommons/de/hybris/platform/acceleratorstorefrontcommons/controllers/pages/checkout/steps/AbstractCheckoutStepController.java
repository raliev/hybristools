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
package de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps;

import de.hybris.platform.acceleratorfacades.payment.PaymentFacade;
import de.hybris.platform.acceleratorservices.customer.CustomerLocationService;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.impl.ContentPageBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutGroup;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.AddressValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.PaymentDetailsValidator;
import de.hybris.platform.acceleratorstorefrontcommons.forms.verification.AddressVerificationResultHandler;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;


public abstract class AbstractCheckoutStepController extends AbstractCheckoutController implements CheckoutStepController
{
	protected static final String MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL = "multiStepCheckoutSummary";
	protected static final String REDIRECT_URL_ADD_DELIVERY_ADDRESS = REDIRECT_PREFIX + "/checkout/multi/delivery-address/add";
	protected static final String REDIRECT_URL_CHOOSE_DELIVERY_METHOD = REDIRECT_PREFIX + "/checkout/multi/delivery-method/choose";
	protected static final String REDIRECT_URL_ADD_PAYMENT_METHOD = REDIRECT_PREFIX + "/checkout/multi/payment-method/add";
	protected static final String REDIRECT_URL_SUMMARY = REDIRECT_PREFIX + "/checkout/multi/summary/view";
	protected static final String REDIRECT_URL_CART = REDIRECT_PREFIX + "/cart";
	protected static final String REDIRECT_URL_ERROR = REDIRECT_PREFIX + "/checkout/multi/hop/error";

	private static final Logger LOGGER = Logger.getLogger(AbstractCheckoutStepController.class);

	@Resource(name = "paymentDetailsValidator")
	private PaymentDetailsValidator paymentDetailsValidator;

	@Resource(name = "accProductFacade")
	private ProductFacade productFacade;

	@Resource(name = "multiStepCheckoutBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "paymentFacade")
	private PaymentFacade paymentFacade;

	@Resource(name = "addressValidator")
	private AddressValidator addressValidator;

	@Resource(name = "customerLocationService")
	private CustomerLocationService customerLocationService;

	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "addressVerificationResultHandler")
	private AddressVerificationResultHandler addressVerificationResultHandler;

	@Resource(name = "contentPageBreadcrumbBuilder")
	private ContentPageBreadcrumbBuilder contentPageBreadcrumbBuilder;

	@Resource(name = "checkoutFlowGroupMap")
	private Map<String, CheckoutGroup> checkoutFlowGroupMap;

	@ExceptionHandler(UnknownIdentifierException.class)
	public String handleUnknownIdentifierException(final UnknownIdentifierException exception, final HttpServletRequest request)
	{
		LOGGER.error(exception.getMessage());
		request.setAttribute("message", exception.getMessage());
		return FORWARD_PREFIX + "/404";
	}


	@ModelAttribute("titles")
	public Collection<TitleData> getTitles()
	{
		return getUserFacade().getTitles();
	}

	@ModelAttribute("countries")
	public Collection<CountryData> getCountries()
	{
		return getCheckoutFacade().getDeliveryCountries();
	}

	@ModelAttribute("countryDataMap")
	public Map<String, CountryData> getCountryDataMap()
	{
		final Map<String, CountryData> countryDataMap = new HashMap<String, CountryData>();
		for (final CountryData countryData : getCountries())
		{
			countryDataMap.put(countryData.getIsocode(), countryData);
		}
		return countryDataMap;
	}

	@ModelAttribute("checkoutSteps")
	public List<CheckoutSteps> addCheckoutStepsToModel()
	{
		final CheckoutGroup checkoutGroup = getCheckoutGroup();
		final Map<String, CheckoutStep> progressBarMap = checkoutGroup.getCheckoutProgressBar();
		final List<CheckoutSteps> checkoutSteps = new ArrayList<CheckoutSteps>(progressBarMap.size());

		for (final Map.Entry<String, CheckoutStep> entry : progressBarMap.entrySet())
		{
			final CheckoutStep checkoutStep = entry.getValue();
			if (checkoutStep.isEnabled())
			{
				checkoutSteps.add(new CheckoutSteps(checkoutStep.getProgressBarId(), StringUtils.remove(checkoutStep.currentStep(),
						REDIRECT_PREFIX), Integer.valueOf(entry.getKey())));
			}
		}

		return checkoutSteps;
	}

	protected CheckoutGroup getCheckoutGroup() throws UnknownIdentifierException
	{
		final CheckoutGroup checkoutGroup = getCheckoutFlowGroupMap().get(getCheckoutFacade().getCheckoutFlowGroupForCheckout());
		if (checkoutGroup != null)
		{
			return checkoutGroup;
		}
		else
		{
			throw new UnknownIdentifierException(String.format("Cannot find checkout group '%s'", getCheckoutFacade()
					.getCheckoutFlowGroupForCheckout()));
		}
	}

	protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute("isOmsEnabled", Boolean.valueOf(getSiteConfigService().getBoolean("oms.enabled", false)));
		model.addAttribute("supportedCountries", getCartFacade().getDeliveryCountries());
		model.addAttribute("expressCheckoutAllowed", Boolean.valueOf(getCheckoutFacade().isExpressCheckoutAllowedForCart()));
		model.addAttribute("taxEstimationEnabled", Boolean.valueOf(getCheckoutFacade().isTaxEstimationEnabledForCart()));
	}

	protected CheckoutStep getCheckoutStep(final String currentController)
	{
		final CheckoutGroup checkoutGroup = getCheckoutGroup();
		return checkoutGroup.getCheckoutStepMap().get(currentController);
	}

	protected void setCheckoutStepLinksForModel(final Model model, final CheckoutStep checkoutStep)
	{
		model.addAttribute("previousStepUrl", StringUtils.remove(checkoutStep.previousStep(), REDIRECT_PREFIX));
		model.addAttribute("nextStepUrl", StringUtils.remove(checkoutStep.nextStep(), REDIRECT_PREFIX));
		model.addAttribute("currentStepUrl", StringUtils.remove(checkoutStep.currentStep(), REDIRECT_PREFIX));
		model.addAttribute("progressBarId", checkoutStep.getProgressBarId());
	}

	protected Map<String, String> getRequestParameterMap(final HttpServletRequest request)
	{
		final Map<String, String> map = new HashMap<String, String>();

		final Enumeration myEnum = request.getParameterNames();
		while (myEnum.hasMoreElements())
		{
			final String paramName = (String) myEnum.nextElement();
			final String paramValue = request.getParameter(paramName);
			map.put(paramName, paramValue);
		}

		return map;
	}

	public static class CheckoutSteps
	{
		private final String progressBarId;
		private final String url;
		private final Integer stepNumber;

		public CheckoutSteps(final String progressBarId, final String url, final Integer stepNumber)
		{
			this.progressBarId = progressBarId;
			this.url = url;
			this.stepNumber = stepNumber;
		}

		public String getProgressBarId()
		{
			return progressBarId;
		}

		public String getUrl()
		{
			return url;
		}

		public Integer getStepNumber()
		{
			return stepNumber;
		}
	}

	@Override
	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	protected ProductFacade getProductFacade()
	{
		return productFacade;
	}

	protected PaymentDetailsValidator getPaymentDetailsValidator()
	{
		return paymentDetailsValidator;
	}

	protected ResourceBreadcrumbBuilder getResourceBreadcrumbBuilder()
	{
		return resourceBreadcrumbBuilder;
	}

	protected PaymentFacade getPaymentFacade()
	{
		return paymentFacade;
	}

	protected AddressValidator getAddressValidator()
	{
		return addressValidator;
	}

	protected CustomerLocationService getCustomerLocationService()
	{
		return customerLocationService;
	}

	protected AddressVerificationResultHandler getAddressVerificationResultHandler()
	{
		return addressVerificationResultHandler;
	}

	public ContentPageBreadcrumbBuilder getContentPageBreadcrumbBuilder()
	{
		return contentPageBreadcrumbBuilder;
	}

	public Map<String, CheckoutGroup> getCheckoutFlowGroupMap()
	{
		return checkoutFlowGroupMap;
	}

}
