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

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Form for validating product quantity on pickup in store page.
 */
public class PickupInStoreForm
{
	private long hiddenPickupQty;

	@NotNull(message = "{basket.error.quantity.notNull}")
	@Min(value = 0, message = "{basket.error.quantity.invalid}")
	@Digits(fraction = 0, integer = 10, message = "{basket.error.quantity.invalid}")
	public long getHiddenPickupQty()
	{
		return hiddenPickupQty;
	}

	public void setHiddenPickupQty(final long hiddenPickupQty)
	{
		this.hiddenPickupQty = hiddenPickupQty;
	}
}
