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
package de.hybris.platform.acceleratorstorefrontcommons.controllers.pages;

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorservices.checkout.flow.CheckoutFlowStrategy;
import de.hybris.platform.commercefacades.address.AddressVerificationFacade;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Base controller for all page controllers. Provides common functionality for all page controllers.
 */
public abstract class AbstractCheckoutController extends AbstractPageController
{
	protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/orderConfirmation/";

	/**
	 * @deprecated Since 6.0. use
	 *             {@link de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController#LOGGER}
	 *             instead.
	 */
	@Deprecated
	protected static final Logger LOG = Logger.getLogger(AbstractCheckoutController.class);
	private static final Logger LOGGER = Logger.getLogger(AbstractCheckoutController.class);

	@Resource(name = "userFacade")
	private UserFacade userFacade;

	@Resource(name = "checkoutFlowFacade")
	private CheckoutFlowFacade checkoutFlowFacade;

	@Resource(name = "addressVerificationFacade")
	private AddressVerificationFacade addressVerificationFacade;

	@Resource(name = "checkoutFlowStrategy")
	private CheckoutFlowStrategy checkoutFlowStrategy;

	@Resource(name = "i18NFacade")
	private I18NFacade i18NFacade;

	@Resource(name = "acceleratorCheckoutFacade")
	private AcceleratorCheckoutFacade checkoutFacade;

	@Resource(name = "checkoutCustomerStrategy")
	private CheckoutCustomerStrategy checkoutCustomerStrategy;

	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	protected UserFacade getUserFacade()
	{
		return userFacade;
	}

	protected CheckoutFlowFacade getCheckoutFlowFacade()
	{
		return checkoutFlowFacade;
	}

	protected AddressVerificationFacade getAddressVerificationFacade()
	{
		return addressVerificationFacade;
	}

	protected CheckoutFlowStrategy getCheckoutFlowStrategy()
	{
		return checkoutFlowStrategy;
	}

	protected I18NFacade getI18NFacade()
	{
		return i18NFacade;
	}

	protected AcceleratorCheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

	protected CheckoutCustomerStrategy getCheckoutCustomerStrategy()
	{
		return checkoutCustomerStrategy;
	}

	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	protected boolean isAddressIdChanged(final AddressData cartCheckoutDeliveryAddress, final AddressData selectedAddressData)
	{
		return cartCheckoutDeliveryAddress == null || !selectedAddressData.getId().equals(cartCheckoutDeliveryAddress.getId());
	}

	// It is possible for this class to be extended or for methods to be used in other extensions - so no sonar added
	protected List<? extends AddressData> getDeliveryAddresses(final AddressData selectedAddressData) // NOSONAR
	{
		List<AddressData> deliveryAddresses = null;
		if (selectedAddressData != null)
		{
			deliveryAddresses = (List<AddressData>) getCheckoutFacade().getSupportedDeliveryAddresses(true);

			if (deliveryAddresses == null || deliveryAddresses.isEmpty())
			{
				deliveryAddresses = Collections.singletonList(selectedAddressData);
			}
			else if (!isAddressOnList(deliveryAddresses, selectedAddressData))
			{
				deliveryAddresses.add(selectedAddressData);
			}
		}

		return deliveryAddresses == null ? Collections.<AddressData> emptyList() : deliveryAddresses;
	}

	protected boolean isAddressOnList(final List<AddressData> deliveryAddresses, final AddressData selectedAddressData)
	{
		if (deliveryAddresses == null || selectedAddressData == null)
		{
			return false;
		}

		for (final AddressData address : deliveryAddresses)
		{
			if (address.getId().equals(selectedAddressData.getId()))
			{
				return true;
			}
		}

		return false;
	}


	protected String redirectToOrderConfirmationPage(final OrderData orderData)
	{
		return REDIRECT_URL_ORDER_CONFIRMATION
				+ (getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode());
	}


	/**
	 * Data class used to hold a drop down select option value. Holds the code identifier as well as the display name.
	 */
	public static class SelectOption
	{
		private final String code;
		private final String name;

		public SelectOption(final String code, final String name)
		{
			this.code = code;
			this.name = name;
		}

		public String getCode()
		{
			return code;
		}

		public String getName()
		{
			return name;
		}
	}


	protected boolean validateCart(final RedirectAttributes redirectModel)
	{
		//Validate the cart
		List<CartModificationData> modifications = new ArrayList<>();
		try
		{
			modifications = cartFacade.validateCartData();
		}
		catch (final CommerceCartModificationException e)
		{
			LOGGER.error("Failed to validate cart", e);
		}
		if (!modifications.isEmpty())
		{
			redirectModel.addFlashAttribute("validationData", modifications);

			// Invalid cart. Bounce back to the cart page.
			return true;
		}
		return false;
	}
}
