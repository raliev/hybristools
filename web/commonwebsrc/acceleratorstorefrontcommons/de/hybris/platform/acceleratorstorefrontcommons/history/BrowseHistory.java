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

/**
 */
public interface BrowseHistory
{
	/**
	 * Adds the url to the browsing history.
	 *
	 * @param browseHistoryEntry the {@link BrowseHistoryEntry} that will be added to the history
	 */
	void addBrowseHistoryEntry(BrowseHistoryEntry browseHistoryEntry);
	
	BrowseHistoryEntry findEntryMatchUrlEndsWith(String match);
}
