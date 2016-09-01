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
package de.hybris.platform.acceleratorstorefrontcommons.security;

import de.hybris.platform.acceleratorservices.uiexperience.UiExperienceService;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.strategy.CartRestorationStrategy;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commerceservices.enums.UiExperienceLevel;
import de.hybris.platform.core.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;


/**
 * Success handler initializing user settings, restoring or merging the cart and ensuring the cart is handled correctly.
 * Cart restoration is stored in the session since the request coming in is that to j_spring_security_check and will be
 * redirected.
 */
public class StorefrontAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
	private CustomerFacade customerFacade;
	private UiExperienceService uiExperienceService;
	private CartFacade cartFacade;
	private BruteForceAttackCounter bruteForceAttackCounter;
	private CartRestorationStrategy cartRestorationStrategy;
	private Map<UiExperienceLevel, Boolean> forceDefaultTargetForUiExperienceLevel;
	private List<String> restrictedPages;
	private List<String> listRedirectUrlsForceDefaultTarget;
	private GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_" + Constants.USER.ADMIN_USERGROUP.toUpperCase());

	private static final String CHECKOUT_URL = "/checkout";
	private static final String CART_URL = "/cart";

	private static final Logger LOG = Logger.getLogger(StorefrontAuthenticationSuccessHandler.class);

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException, ServletException
	{
		//if redirected from some specific url, need to remove the cachedRequest to force use defaultTargetUrl
		final RequestCache requestCache = new HttpSessionRequestCache();
		final SavedRequest savedRequest = requestCache.getRequest(request, response);

		if (savedRequest != null)
		{
			for (final String redirectUrlForceDefaultTarget : getListRedirectUrlsForceDefaultTarget())
			{
				if (savedRequest.getRedirectUrl().contains(redirectUrlForceDefaultTarget))
				{
					requestCache.removeRequest(request, response);
					break;
				}
			}
		}
		getCustomerFacade().loginSuccess();
		request.setAttribute(WebConstants.CART_MERGED, Boolean.FALSE);

		// Check if the user is in role admingroup
		if (!isAdminAuthority(authentication))
		{
			getCartRestorationStrategy().restoreCart(request);
			getBruteForceAttackCounter().resetUserCounter(getCustomerFacade().getCurrentCustomerUid());
			super.onAuthenticationSuccess(request, response, authentication);
		}
		else
		{
			LOG.warn("Invalidating session for user in the " + Constants.USER.ADMIN_USERGROUP + " group");
			invalidateSession(request, response);
		}
	}

	protected void invalidateSession(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		SecurityContextHolder.getContext().setAuthentication(null);
		request.getSession().invalidate();
		response.sendRedirect(request.getContextPath());
	}

	protected boolean isAdminAuthority(final Authentication authentication)
	{
		return CollectionUtils.isNotEmpty(authentication.getAuthorities())
				&& authentication.getAuthorities().contains(adminAuthority);
	}

	protected List<String> getRestrictedPages()
	{
		return restrictedPages;
	}

	public void setRestrictedPages(final List<String> restrictedPages)
	{
		this.restrictedPages = restrictedPages;
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

	protected CustomerFacade getCustomerFacade()
	{
		return customerFacade;
	}

	@Required
	public void setCustomerFacade(final CustomerFacade customerFacade)
	{
		this.customerFacade = customerFacade;
	}

	/*
	 * @see org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler#
	 * isAlwaysUseDefaultTargetUrl()
	 */
	@Override
	protected boolean isAlwaysUseDefaultTargetUrl()
	{
		final UiExperienceLevel uiExperienceLevel = getUiExperienceService().getUiExperienceLevel();
		if (getForceDefaultTargetForUiExperienceLevel().containsKey(uiExperienceLevel))
		{
			return Boolean.TRUE.equals(getForceDefaultTargetForUiExperienceLevel().get(uiExperienceLevel));
		}
		else
		{
			return false;
		}
	}

	@Override
	protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response)
	{
		String targetUrl = super.determineTargetUrl(request, response);
		if (CollectionUtils.isNotEmpty(getRestrictedPages()))
		{
			for (final String restrictedPage : getRestrictedPages())
			{
				// When logging in from a restricted page, return user to default target url.
				if (targetUrl.contains(restrictedPage))
				{
					targetUrl = super.getDefaultTargetUrl();
				}
			}
		}
		/*
		 * If the cart has been merged and the user logging in through checkout, redirect to the cart page to display the
		 * new cart
		 */
		if (StringUtils.equals(targetUrl, CHECKOUT_URL)
				&& BooleanUtils.toBoolean((Boolean) request.getAttribute(WebConstants.CART_MERGED)))
		{
			targetUrl = CART_URL;
		}

		return targetUrl;
	}

	protected Map<UiExperienceLevel, Boolean> getForceDefaultTargetForUiExperienceLevel()
	{
		return forceDefaultTargetForUiExperienceLevel;
	}

	@Required
	public void setForceDefaultTargetForUiExperienceLevel(
			final Map<UiExperienceLevel, Boolean> forceDefaultTargetForUiExperienceLevel)
	{
		this.forceDefaultTargetForUiExperienceLevel = forceDefaultTargetForUiExperienceLevel;
	}

	protected BruteForceAttackCounter getBruteForceAttackCounter()
	{
		return bruteForceAttackCounter;
	}

	@Required
	public void setBruteForceAttackCounter(final BruteForceAttackCounter bruteForceAttackCounter)
	{
		this.bruteForceAttackCounter = bruteForceAttackCounter;
	}

	protected UiExperienceService getUiExperienceService()
	{
		return uiExperienceService;
	}

	@Required
	public void setUiExperienceService(final UiExperienceService uiExperienceService)
	{
		this.uiExperienceService = uiExperienceService;
	}

	protected List<String> getListRedirectUrlsForceDefaultTarget()
	{
		return listRedirectUrlsForceDefaultTarget;
	}

	@Required
	public void setListRedirectUrlsForceDefaultTarget(final List<String> listRedirectUrlsForceDefaultTarget)
	{
		this.listRedirectUrlsForceDefaultTarget = listRedirectUrlsForceDefaultTarget;
	}

	/**
	 * @param adminGroup
	 *           the adminGroup to set
	 */
	public void setAdminGroup(final String adminGroup)
	{
		if (StringUtils.isBlank(adminGroup))
		{
			adminAuthority = null;
		}
		else
		{
			adminAuthority = new SimpleGrantedAuthority(adminGroup);
		}
	}

	protected GrantedAuthority getAdminAuthority()
	{
		return adminAuthority;
	}

	protected CartRestorationStrategy getCartRestorationStrategy()
	{
		return cartRestorationStrategy;
	}

	@Required
	public void setCartRestorationStrategy(final CartRestorationStrategy cartRestorationStrategy)
	{
		this.cartRestorationStrategy = cartRestorationStrategy;
	}

}
