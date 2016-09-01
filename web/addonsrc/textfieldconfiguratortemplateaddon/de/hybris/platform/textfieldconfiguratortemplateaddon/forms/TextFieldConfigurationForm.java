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
 *
*/
package de.hybris.platform.textfieldconfiguratortemplateaddon.forms;

import de.hybris.platform.catalog.enums.ConfiguratorType;

import java.util.Map;

public class TextFieldConfigurationForm
{
	private Long quantity;
	private Map<ConfiguratorType, Map<String, String>> configurationsKeyValueMap;

	public Long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(final Long quantity)
	{
		this.quantity = quantity;
	}

	public Map<ConfiguratorType, Map<String, String>> getConfigurationsKeyValueMap()
	{
		return configurationsKeyValueMap;
	}

	public void setConfigurationsKeyValueMap(final Map<ConfiguratorType, Map<String, String>> configurationsKeyValueMap)
	{
		this.configurationsKeyValueMap = configurationsKeyValueMap;
	}
}
