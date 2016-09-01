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
package de.hybris.platform.acceleratorstorefrontcommons.strategy.impl;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.strategy.CartRestorationStrategy;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.servicelayer.session.SessionService;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Strategy for cart restoration without merging to improve performance.
 */
public class DefaultCartRestorationStrategy implements CartRestorationStrategy
{
	private static final Logger LOG = Logger.getLogger(DefaultCartRestorationStrategy.class);

	private SessionService sessionService;
	private CartFacade cartFacade;

	@Override
	public void restoreCart(final HttpServletRequest request)
	{
		if (!getCartFacade().hasEntries())
		{
			getSessionService().setAttribute(WebConstants.CART_RESTORATION_SHOW_MESSAGE, Boolean.TRUE);
			try
			{
				getSessionService().setAttribute(WebConstants.CART_RESTORATION, getCartFacade().restoreSavedCart(null));
			}
			catch (final CommerceCartRestorationException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug(e);
				}
				getSessionService().setAttribute(WebConstants.CART_RESTORATION_ERROR_STATUS,
						WebConstants.CART_RESTORATION_ERROR_STATUS);
			}
		}
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	@Required
	public void setCartFacade(final CartFacade cartFacade)
	{
		this.cartFacade = cartFacade;
	}

}
