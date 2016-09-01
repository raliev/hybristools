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
package de.hybris.platform.acceleratorstorefrontcommons.variants.impl;

import java.util.Comparator;

import org.apache.commons.lang.math.NumberUtils;


/**
 * Default comparator for variant values
 */
public class DefaultVariantComparator implements Comparator<Object>
{
	@Override
	public int compare(final Object variant1, final Object variant2)
	{
		if (variant1 instanceof Number)
		{
			final double number1 = ((Number) variant1).doubleValue();
			final double number2 = ((Number) variant2).doubleValue();
			return NumberUtils.compare(number1, number2);
		}
		else if (variant1 instanceof String)
		{
			final String string1 = (String) variant1;
			final String string2 = (String) variant2;
			return string1.compareTo(string2);
		}
		else
		{
			return getResult(variant1, variant2);
		}
	}

	protected int getResult(final Object variant1, final Object variant2)
	{
		if (variant1 == null && variant2 == null)
		{
            return 0;
        }
		else if (variant1 == null)
		{
            return -1;
        }
		else if (variant2 == null)
		{
            return 1;
        }
		return variant1.toString().compareTo(variant2.toString());
	}
}
