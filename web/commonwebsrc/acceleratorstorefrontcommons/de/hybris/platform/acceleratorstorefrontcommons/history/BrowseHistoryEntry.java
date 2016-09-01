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
package de.hybris.platform.acceleratorstorefrontcommons.history;

import java.io.Serializable;


/**
 * Browse history entry data object
 */
public class BrowseHistoryEntry implements Serializable
{
	private String url;
	private String pageTitle;


	public BrowseHistoryEntry(final String url, final String pageTitle)
	{
		this.url = url;
		this.pageTitle = pageTitle;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(final String url)
	{
		this.url = url;
	}

	public String getPageTitle()
	{
		return pageTitle;
	}

	public void setPageTitle(final String pageTitle)
	{
		this.pageTitle = pageTitle;
	}
}
