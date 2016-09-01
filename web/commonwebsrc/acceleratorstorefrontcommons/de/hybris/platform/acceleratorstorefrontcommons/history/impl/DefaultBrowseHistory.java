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
package de.hybris.platform.acceleratorstorefrontcommons.history.impl;

import de.hybris.platform.acceleratorstorefrontcommons.history.BrowseHistory;
import de.hybris.platform.acceleratorstorefrontcommons.history.BrowseHistoryEntry;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Deque;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Required;


/**
 */
public class DefaultBrowseHistory implements BrowseHistory
{
	private static final String SESSION_USER_BROWSE_HISTORY_KEY = "sessionUserBrowseHistory";

	private SessionService sessionService;
	private CMSSiteService cmsSiteService;
	private int capacity = 10;


	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	@Required
	public void setCmsSiteService(final CMSSiteService cmsSiteService)
	{
		this.cmsSiteService = cmsSiteService;
	}

	protected int getCapacity()
	{
		return capacity;
	}

	@Required
	public void setCapacity(final int capacity)
	{
		this.capacity = capacity;
	}


	@Override
	public void addBrowseHistoryEntry(final BrowseHistoryEntry browseHistoryEntry)
	{
		// Get the actual history entry list stored in the session
		final Deque<BrowseHistoryEntry> browseHistoryEntries = getBrowseHistoryEntries();

		if (browseHistoryEntries != null)
		{
			// Lock on the entries to ensure that we modify it atomically
			synchronized (browseHistoryEntries)
			{
				// Add the entry
				browseHistoryEntries.addFirst(browseHistoryEntry);

				// Remove any entries that are over capacity
				while (browseHistoryEntries.size() > getCapacity())
				{
					browseHistoryEntries.removeLast();
				}
			}
		}
	}

	protected Deque<BrowseHistoryEntry> getBrowseHistoryEntries()
	{
		final CMSSiteModel currentSite = getCmsSiteService().getCurrentSite();

		if (currentSite != null)
		{
			final String sessionKey = SESSION_USER_BROWSE_HISTORY_KEY + "-" + currentSite.getUid();

			// Get the queue of BrowseHistoryEntries from the session
			// We need to use the InstanceWrapper to protect the collection from the session service
			// which will wrap it in a java.util.Collections$UnmodifiableRandomAccessList
			return getSessionService().getOrLoadAttribute(sessionKey,
					new SessionService.SessionAttributeLoader<InstanceWrapper<Deque<BrowseHistoryEntry>>>()
					{
						@Override
						public InstanceWrapper<Deque<BrowseHistoryEntry>> load()
						{
							return new InstanceWrapper<Deque<BrowseHistoryEntry>>(new LinkedList<BrowseHistoryEntry>());
						}
					}).get();
		}
		// Null is returned as a result of current site not available which should not happen - caller methods
		// may treat null differently than an empty collection thus fix could break existing functionality
		return null; // NOSONAR
	}

	@Override
	public BrowseHistoryEntry findEntryMatchUrlEndsWith(final String match)
	{
		final Deque<BrowseHistoryEntry> browseHistoryEntries = getBrowseHistoryEntries();

		if (browseHistoryEntries != null)
		{
			// Lock on the entries to ensure that we don't modify it while iterating
			synchronized (browseHistoryEntries)
			{
				for (final BrowseHistoryEntry entry : browseHistoryEntries)
				{
					if (entry.getUrl().endsWith("/" + match))
					{
						return entry;
					}
				}
			}
		}
		return null;
	}

	public static class InstanceWrapper<T>
	{
		private final T instance;

		public InstanceWrapper(final T instance)
		{
			this.instance = instance;
		}

		public T get()
		{
			return instance;
		}
	}
}
