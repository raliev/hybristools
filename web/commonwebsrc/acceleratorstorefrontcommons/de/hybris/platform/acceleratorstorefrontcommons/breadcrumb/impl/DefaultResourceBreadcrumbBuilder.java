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
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.ui.context.Theme;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;


/**
 * BasicBreadcrumbBuilder implementation
 */
public class DefaultResourceBreadcrumbBuilder implements ResourceBreadcrumbBuilder
{
	private static final String LAST_LINK_CLASS = "active";

	private I18NService i18nService;

	private String parentBreadcrumbResourceKey;
	private String parentBreadcrumbLinkPath;

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
		final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null)
		{
			final HttpServletRequest request = requestAttributes.getRequest();
			final Theme theme = RequestContextUtils.getTheme(request);
			if (theme != null)
			{
				return theme.getMessageSource();
			}
		}

		return null;
	}

	protected String getParentBreadcrumbResourceKey()
	{
		return parentBreadcrumbResourceKey;
	}

	// Optional
	public void setParentBreadcrumbResourceKey(final String parentBreadcrumbResourceKey)
	{
		this.parentBreadcrumbResourceKey = parentBreadcrumbResourceKey;
	}

	protected String getParentBreadcrumbLinkPath()
	{
		return parentBreadcrumbLinkPath;
	}

	// Optional
	public void setParentBreadcrumbLinkPath(final String parentBreadcrumbLinkPath)
	{
		this.parentBreadcrumbLinkPath = parentBreadcrumbLinkPath;
	}

	@Override
	public List<Breadcrumb> getBreadcrumbs(final String resourceKey)
	{
		final List<Breadcrumb> breadcrumbs = new ArrayList<>();
		final MessageSource messageSource = getMessageSource();


		if (getParentBreadcrumbResourceKey() != null && !getParentBreadcrumbResourceKey().isEmpty())
		{
			final String name = messageSource
					.getMessage(getParentBreadcrumbResourceKey(), null, getI18nService().getCurrentLocale());
			final String breadcrumbLinkPath = getParentBreadcrumbLinkPath();
			final String link = breadcrumbLinkPath != null && !breadcrumbLinkPath.isEmpty() ? breadcrumbLinkPath : "#";
			breadcrumbs.add(new Breadcrumb(link, name, null));
		}

		if (StringUtils.isNotBlank(resourceKey))
		{
			final String name = messageSource.getMessage(resourceKey, null, getI18nService().getCurrentLocale());
			breadcrumbs.add(new Breadcrumb("#", name, null));
		}

		if (!breadcrumbs.isEmpty())
		{
			breadcrumbs.get(breadcrumbs.size() - 1).setLinkClass(LAST_LINK_CLASS);
		}

		return breadcrumbs;
	}
}
