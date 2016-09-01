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
package de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.impl;

import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.history.BrowseHistory;
import de.hybris.platform.catalog.model.classification.ClassificationClassModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

/**
 * ProductBreadcrumbBuilder implementation for {@link ProductData}
 */
public class ProductBreadcrumbBuilder
{
	private static final String LAST_LINK_CLASS = "active";

	private UrlResolver<ProductModel> productModelUrlResolver;
	private UrlResolver<CategoryModel> categoryModelUrlResolver;
	private BrowseHistory browseHistory;

	private ProductService productService;

	/**
	 * Returns a list of breadcrumbs for the given product.
	 *
	 * @param productCode
	 * @return breadcrumbs for the given product
	 */
	public List<Breadcrumb> getBreadcrumbs(final String productCode)
	{
		final ProductModel productModel = getProductService().getProductForCode(productCode);
		final List<Breadcrumb> breadcrumbs = new ArrayList<>();

		final Collection<CategoryModel> categoryModels = new ArrayList<>();
		final Breadcrumb last;

		final ProductModel baseProductModel = getBaseProduct(productModel);
		last = getProductBreadcrumb(baseProductModel);
		categoryModels.addAll(baseProductModel.getSupercategories());
		last.setLinkClass(LAST_LINK_CLASS);

		breadcrumbs.add(last);

		while (!categoryModels.isEmpty())
		{
			CategoryModel toDisplay = null;
			toDisplay = processCategoryModels(categoryModels, toDisplay);
			categoryModels.clear();
			if (toDisplay != null)
			{
				breadcrumbs.add(getCategoryBreadcrumb(toDisplay));
				categoryModels.addAll(toDisplay.getSupercategories());
			}
		}
		Collections.reverse(breadcrumbs);
		return breadcrumbs;
	}

	/**
	 * Returns a list of breadcrumbs for the given product.
	 *
	 * @param productModel
	 * @return breadcrumbs for the given product
	 * @throws IllegalArgumentException
	 * @deprecated since 6.0, use {@link #getBreadcrumbs(String)} instead
	 */
	@Deprecated
	public List<Breadcrumb> getBreadcrumbs(final ProductModel productModel) throws IllegalArgumentException
	{
		return getBreadcrumbs(productModel.getCode());
	}

	protected CategoryModel processCategoryModels(final Collection<CategoryModel> categoryModels,
			final CategoryModel toDisplay)
	{
		CategoryModel categoryToDisplay = toDisplay;

		for (final CategoryModel categoryModel : categoryModels)
		{
			if (!(categoryModel instanceof ClassificationClassModel))
			{
				if (categoryToDisplay == null)
				{
					categoryToDisplay = categoryModel;
				}
				if (getBrowseHistory().findEntryMatchUrlEndsWith(categoryModel.getCode()) != null)
				{
					break;
				}
			}
		}
		return categoryToDisplay;
	}

	protected ProductModel getBaseProduct(final ProductModel product)
	{
		if (product instanceof VariantProductModel)
		{
			return getBaseProduct(((VariantProductModel) product).getBaseProduct());
		}
		return product;
	}

	protected Breadcrumb getProductBreadcrumb(final ProductModel product)
	{
		final String productUrl = getProductModelUrlResolver().resolve(product);
		return new Breadcrumb(productUrl, product.getName(), null);
	}

	protected Breadcrumb getCategoryBreadcrumb(final CategoryModel category)
	{
		final String categoryUrl = getCategoryModelUrlResolver().resolve(category);
		return new Breadcrumb(categoryUrl, category.getName(), null, category.getCode());
	}

	protected UrlResolver<ProductModel> getProductModelUrlResolver()
	{
		return productModelUrlResolver;
	}

	@Required
	public void setProductModelUrlResolver(final UrlResolver<ProductModel> productModelUrlResolver)
	{
		this.productModelUrlResolver = productModelUrlResolver;
	}

	protected UrlResolver<CategoryModel> getCategoryModelUrlResolver()
	{
		return categoryModelUrlResolver;
	}

	@Required
	public void setCategoryModelUrlResolver(final UrlResolver<CategoryModel> categoryModelUrlResolver)
	{
		this.categoryModelUrlResolver = categoryModelUrlResolver;
	}

	protected BrowseHistory getBrowseHistory()
	{
		return browseHistory;
	}

	@Required
	public void setBrowseHistory(final BrowseHistory browseHistory)
	{
		this.browseHistory = browseHistory;
	}
	
	protected ProductService getProductService()
	{
		return productService;
	}

	@Required
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}
}
