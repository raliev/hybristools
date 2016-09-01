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
import de.hybris.platform.commercefacades.storelocator.data.PointOfServiceData;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;


/**
 * StorefinderBreadcrumbBuilder implementation for store finder related pages
 */
public class StoreBreadcrumbBuilder
{
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

	public List<Breadcrumb> getBreadcrumbs(final PointOfServiceData pointOfServiceData)
	{
		return getBreadcrumbs(pointOfServiceData, null);
	}

	public List<Breadcrumb> getBreadcrumbs(final PointOfServiceData pointOfServiceData, final String storeFinderUrl)
	{
		final List<Breadcrumb> breadcrumbs = new ArrayList<>();

		final String storeFinderLinkName = getMessageSource().getMessage("storeFinder.link", null, getI18nService().getCurrentLocale());
		final String resolvedStoreFinderUrl = storeFinderUrl == null ? "/store-finder" : storeFinderUrl;
		breadcrumbs.add(new Breadcrumb(resolvedStoreFinderUrl, storeFinderLinkName, null));

		breadcrumbs.add(new Breadcrumb("#", pointOfServiceData.getName(), null));

		if (!breadcrumbs.isEmpty())
		{
			breadcrumbs.get(breadcrumbs.size() - 1).setLinkClass(LAST_LINK_CLASS);
		}

		return breadcrumbs;
	}

	public List<Breadcrumb> getBreadcrumbsForSubPage(final PointOfServiceData pointOfServiceData, final String pageResourceKey)
	{
		final List<Breadcrumb> breadcrumbs = new ArrayList<>();

		final String storeFinderLinkName = getMessageSource().getMessage("storeFinder.link", null, getI18nService().getCurrentLocale());
		breadcrumbs.add(new Breadcrumb("/store-finder", storeFinderLinkName, null));

		final String storePageUrl = "/store/" + pointOfServiceData.getName();
		breadcrumbs.add(new Breadcrumb(storePageUrl, pointOfServiceData.getName(), null));

		final String pageLinkName = getMessageSource().getMessage(pageResourceKey, null, getI18nService().getCurrentLocale());
		breadcrumbs.add(new Breadcrumb("#", pageLinkName, null));

		if (!breadcrumbs.isEmpty())
		{
			breadcrumbs.get(breadcrumbs.size() - 1).setLinkClass(LAST_LINK_CLASS);
		}

		return breadcrumbs;
	}
}
