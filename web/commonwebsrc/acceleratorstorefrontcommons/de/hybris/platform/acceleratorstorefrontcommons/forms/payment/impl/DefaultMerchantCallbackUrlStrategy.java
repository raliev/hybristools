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
package de.hybris.platform.acceleratorstorefrontcommons.forms.payment.impl;

import de.hybris.platform.acceleratorstorefrontcommons.forms.payment.MerchantCallbackUrlStrategy;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;


@Component("merchantCallbackUrlStrategy")
public class DefaultMerchantCallbackUrlStrategy implements MerchantCallbackUrlStrategy
{
	@Resource
	private ConfigurationService configurationService;

	@Override
	public String getUrl()
	{
		return configurationService.getConfiguration().getString("merchant.callback.url", "");
	}
}
