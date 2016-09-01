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
package de.hybris.platform.acceleratorstorefrontcommons.forms;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;

import java.util.List;


public class AddToCartOrderForm
{

	private List<OrderEntryData> cartEntries;

	/**
	 * @return Return the cartEntries.
	 */
	public List<OrderEntryData> getCartEntries()
	{
		return cartEntries;
	}

	/**
	 * @param cartEntries
	 *           The cartEntries to set.
	 */
	public void setCartEntries(final List<OrderEntryData> cartEntries)
	{
		this.cartEntries = cartEntries;
	}
}
