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

import de.hybris.platform.acceleratorstorefrontcommons.variants.VariantSortStrategy;
import de.hybris.platform.commercefacades.product.data.VariantOptionData;
import de.hybris.platform.commercefacades.product.data.VariantOptionQualifierData;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default strategy for comparing variants. <br>
 * Use {@link #sortingFieldsOrder} property to set up attributes to sort by. E.g give A,B attributes and sorting will be
 * performed first against A attribute and if two values are considered equal (by A) then values of B attribute are
 * compared.<br>
 * Use {@link #comparators} to provide comparators for particular attributes
 */
public class DefaultVariantSortStrategy implements VariantSortStrategy
{
	private List<String> sortingFieldsOrder;
	private Map<String, Comparator<String>> comparators;
	private Comparator<String> defaultComparator;

	protected List<String> getSortingFieldsOrder()
	{
		return sortingFieldsOrder;
	}

	@Override
	@Required
	public void setSortingFieldsOrder(final List<String> sortingFieldsOrder)
	{
		this.sortingFieldsOrder = sortingFieldsOrder;
	}

	protected Map<String, Comparator<String>> getComparators()
	{
		return comparators;
	}

	@Override
	@Required
	public void setComparators(final Map<String, Comparator<String>> comparators)
	{
		this.comparators = comparators;
	}

	protected Comparator<String> getDefaultComparator()
	{
		return defaultComparator;
	}

	@Override
	@Required
	public void setDefaultComparator(final Comparator<String> defaultComparator)
	{
		this.defaultComparator = defaultComparator;
	}

	@Override
	public int compare(final VariantOptionData product1, final VariantOptionData product2)
	{
		final List<String> fields = getSortingFieldsOrder();
		if (fields != null)
		{
			for (final String field : fields)
			{
				final int result = getComparator(field).compare(getVariantValue(field, product1), getVariantValue(field, product2));
				if (result != 0)
				{
					return result;
				}
			}
		}
		return -1;
	}

	protected String getVariantValue(final String field, final VariantOptionData variant)
	{
		for (final VariantOptionQualifierData variantOptionQualifier : variant.getVariantOptionQualifiers())
		{
			if (field.equals(variantOptionQualifier.getQualifier()))
			{
				return variantOptionQualifier.getValue();
			}
		}
		return null;
	}

	protected Comparator<String> getComparator(final String field)
	{
		final Comparator<String> comparator = getComparators().get(field);
		if (comparator == null)
		{
			return getDefaultComparator();
		}
		return comparator;
	}
}
