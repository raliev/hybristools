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
package de.hybris.platform.smarteditaddon.interceptors.beforeview;

import de.hybris.platform.acceleratorstorefrontcommons.interceptors.BeforeViewHandler;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;


/**
 * Filter to set the appropriate view of smarteditaddon cms components into the model
 */
public class SmarteditaddonCMSComponentBeforeViewHandler implements BeforeViewHandler
{
	public static final String ADDON_PREFIX = "addon:/smarteditaddon/";

	private List<String> smarteditaddonComponents;
	private SmarteditaddonResponsiveStrategy smarteditaddonResponsiveStrategy;

	@Override
	public void beforeView(final HttpServletRequest request,
			final HttpServletResponse response, final ModelAndView modelAndView)
			throws Exception
	{
		if (getSmarteditaddonResponsiveStrategy().isResponsive())
		{
			final boolean isAddonComponent = getSmarteditaddonComponents().contains(
					modelAndView.getViewName().substring(4));
			if (isAddonComponent)
			{
				modelAndView.setViewName(ADDON_PREFIX + modelAndView.getViewName());
			}
		}
	}

	protected SmarteditaddonResponsiveStrategy getSmarteditaddonResponsiveStrategy()
	{
		return smarteditaddonResponsiveStrategy;
	}

	@Required
	public void setSmarteditaddonResponsiveStrategy(
			final SmarteditaddonResponsiveStrategy smarteditaddonResponsiveStrategy)
	{
		this.smarteditaddonResponsiveStrategy = smarteditaddonResponsiveStrategy;
	}

	protected List<String> getSmarteditaddonComponents()
	{
		return smarteditaddonComponents;
	}

	@Required
	public void setSmarteditaddonComponents(final List<String> smarteditaddonComponents)
	{
		this.smarteditaddonComponents = smarteditaddonComponents;
	}

}
