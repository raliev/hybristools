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
package de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.impl;

import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;


/**
 * StorefinderBreadcrumbBuilder implementation for store finder related pages
 */
public class StorefinderBreadcrumbBuilder
{
	private static final String STORE_FINDER_LINK_MSG_KEY = "storeFinder.link";
	private static final String STORE_FINDER_URL = "/store-finder";
	private static final String LAST_LINK_CLASS = "active";

	private MessageSource messageSource;
	private I18NService i18nService;

	protected I18NService getI18nService()
	{
		return i18nService;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
	}

	protected MessageSource getMessageSource()
	{
		return messageSource;
	}

	@Required
	public void setMessageSource(final MessageSource messageSource)
	{
		this.messageSource = messageSource;
	}

	public List<Breadcrumb> getBreadcrumbs()
	{
		final List<Breadcrumb> breadcrumbs = addStoreFinderLinkBreadcrumb();

		markLastBreadcrumbAsLastLink(breadcrumbs);

		return breadcrumbs;
	}

	public List<Breadcrumb> getBreadcrumbsForLocationSearch(final String locationSearch)
	{
		final List<Breadcrumb> breadcrumbs = addStoreFinderLinkBreadcrumb();

		if (locationSearch != null && !locationSearch.isEmpty())
		{
			breadcrumbs.add(new Breadcrumb("#", locationSearch, null));
		}

		markLastBreadcrumbAsLastLink(breadcrumbs);

		return breadcrumbs;
	}

	public List<Breadcrumb> getBreadcrumbsForCurrentPositionSearch()
	{
		final List<Breadcrumb> breadcrumbs = addStoreFinderLinkBreadcrumb();

		final String currentPositionLinkName = getMessageSource().getMessage("storeFinder.currentPosition", null, getI18nService().getCurrentLocale());
		breadcrumbs.add(new Breadcrumb("#", currentPositionLinkName, null));

		markLastBreadcrumbAsLastLink(breadcrumbs);

		return breadcrumbs;
	}

	/**
	 * Creates the list of {@link Breadcrumb} and adds store finder breadcrumb to it.
	 * 
	 * @return list of {@link Breadcrumb}
	 */
	protected final List<Breadcrumb> addStoreFinderLinkBreadcrumb()
	{
		final List<Breadcrumb> breadcrumbs = new ArrayList<>();
		final String storeFinderLinkName = getMessageSource().getMessage(STORE_FINDER_LINK_MSG_KEY, null, getI18nService().getCurrentLocale());
		breadcrumbs.add(new Breadcrumb(STORE_FINDER_URL, storeFinderLinkName, null));
		return breadcrumbs;
	}

	/**
	 * Marks the last breadcrumb from the given list with the special link class.
	 * 
	 * @param breadcrumbs list of {@link Breadcrumb}
	 */
	protected void markLastBreadcrumbAsLastLink(final List<Breadcrumb> breadcrumbs)
	{
		if (!breadcrumbs.isEmpty())
		{
			breadcrumbs.get(breadcrumbs.size() - 1).setLinkClass(LAST_LINK_CLASS);
		}
	}
}
