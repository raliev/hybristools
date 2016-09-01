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

import java.util.List;


public class FutureStockForm
{

	private List<String> skus;
	private String productCode;

	public List<String> getSkus()
	{
		return skus;
	}

	public void setSkus(final List<String> skus)
	{
		this.skus = skus;
	}

	public String getProductCode()
	{
		return productCode;
	}

	public void setProductCode(final String productCode)
	{
		this.productCode = productCode;
	}


}
