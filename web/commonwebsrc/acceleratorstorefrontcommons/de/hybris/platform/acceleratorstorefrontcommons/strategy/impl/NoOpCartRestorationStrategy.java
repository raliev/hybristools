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

import de.hybris.platform.acceleratorstorefrontcommons.strategy.CartRestorationStrategy;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


/**
 * Strategy for skipping cart restoration.
 */
public class NoOpCartRestorationStrategy implements CartRestorationStrategy
{
	private static final Logger LOG = Logger.getLogger(NoOpCartRestorationStrategy.class);

	@Override
	public void restoreCart(final HttpServletRequest request)
	{
		LOG.info("Skipping cart restoration");
	}
}
