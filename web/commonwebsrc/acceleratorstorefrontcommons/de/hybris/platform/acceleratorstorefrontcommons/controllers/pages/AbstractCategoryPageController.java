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
package de.hybris.platform.acceleratorstorefrontcommons.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorservices.customer.CustomerLocationService;
import de.hybris.platform.acceleratorservices.data.RequestContextData;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.impl.SearchBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.util.MetaSanitizerUtil;
import de.hybris.platform.acceleratorstorefrontcommons.util.XSSFilterUtil;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.CategoryPageModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.commercefacades.product.data.CategoryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.ProductSearchFacade;
import de.hybris.platform.commercefacades.search.data.SearchQueryData;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commerceservices.category.CommerceCategoryService;
import de.hybris.platform.commerceservices.search.facetdata.BreadcrumbData;
import de.hybris.platform.commerceservices.search.facetdata.FacetData;
import de.hybris.platform.commerceservices.search.facetdata.FacetRefinement;
import de.hybris.platform.commerceservices.search.facetdata.ProductCategorySearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;


public class AbstractCategoryPageController extends AbstractSearchPageController
{
	/**
	 * We use this suffix pattern because of an issue with Spring 3.1 where a Uri value is incorrectly extracted if it
	 * contains on or more '.' characters. Please see https://jira.springsource.org/browse/SPR-6164 for a discussion on
	 * the issue and future resolution.
	 */
	protected static final String CATEGORY_CODE_PATH_VARIABLE_PATTERN = "/{categoryCode:.*}";
	protected static final String PRODUCT_GRID_PAGE = "category/productGridPage";

	@Resource(name = "cmsPageService")
	private CMSPageService cmsPageService;

	@Resource(name = "productSearchFacade")
	private ProductSearchFacade<ProductData> productSearchFacade;

	@Resource(name = "commerceCategoryService")
	private CommerceCategoryService commerceCategoryService;

	@Resource(name = "searchBreadcrumbBuilder")
	private SearchBreadcrumbBuilder searchBreadcrumbBuilder;

	@Resource(name = "categoryModelUrlResolver")
	private UrlResolver<CategoryModel> categoryModelUrlResolver;

	@Resource(name = "customerLocationService")
	private CustomerLocationService customerLocationService;

	@ExceptionHandler(UnknownIdentifierException.class)
	public String handleUnknownIdentifierException(final UnknownIdentifierException exception, final HttpServletRequest request)
	{
		request.setAttribute("message", exception.getMessage());
		return FORWARD_PREFIX + "/404";
	}


	protected String performSearchAndGetResultsPage(final String categoryCode, final String searchQuery, final int page, // NOSONAR
			final ShowMode showMode, final String sortCode, final Model model, final HttpServletRequest request,
			final HttpServletResponse response) throws UnsupportedEncodingException
	{
		final CategoryModel category = getCommerceCategoryService().getCategoryForCode(categoryCode);

		final String redirection = checkRequestUrl(request, response, getCategoryModelUrlResolver().resolve(category));
		if (StringUtils.isNotEmpty(redirection))
		{
			return redirection;
		}

		final CategoryPageModel categoryPage = getCategoryPage(category);

		final CategorySearchEvaluator categorySearch = new CategorySearchEvaluator(categoryCode, XSSFilterUtil.filter(searchQuery),
				page, showMode, sortCode, categoryPage);
		
		ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData = null;
		try
		{
			categorySearch.doSearch();
			searchPageData = categorySearch.getSearchPageData();
		}
		catch (final ConversionException e) // NOSONAR
		{
			searchPageData = createEmptySearchResult(categoryCode);
		}
		 
		final boolean showCategoriesOnly = categorySearch.isShowCategoriesOnly();

		storeCmsPageInModel(model, categorySearch.getCategoryPage());
		storeContinueUrl(request);

		populateModel(model, searchPageData, showMode);
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, getSearchBreadcrumbBuilder().getBreadcrumbs(categoryCode, searchPageData));
		model.addAttribute("showCategoriesOnly", Boolean.valueOf(showCategoriesOnly));
		model.addAttribute("categoryName", category.getName());
		model.addAttribute("pageType", PageType.CATEGORY.name());
		model.addAttribute("userLocation", getCustomerLocationService().getUserLocation());

		updatePageTitle(category, searchPageData.getBreadcrumbs(), model);

		final RequestContextData requestContextData = getRequestContextData(request);
		requestContextData.setCategory(category);
		requestContextData.setSearch(searchPageData);

		if (searchQuery != null)
		{
			model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_FOLLOW);
		}

		final String metaKeywords = MetaSanitizerUtil.sanitizeKeywords(category.getKeywords());
		final String metaDescription = MetaSanitizerUtil.sanitizeDescription(category.getDescription());
		setUpMetaData(model, metaKeywords, metaDescription);

		return getViewPage(categorySearch.getCategoryPage());

	}
	
	/**
	 * Creates empty search results in case {@code doSearch} throws an exception in order to avoid stacktrace on storefront.
	 * 
	 * @param categoryCode category code
	 * @return created {@link ProductCategorySearchPageData}
	 */
	protected ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> createEmptySearchResult(final String categoryCode)
	{
		ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData = new ProductCategorySearchPageData<>();
		searchPageData.setResults(Collections.<ProductData> emptyList());
		searchPageData.setPagination(createEmptyPagination());
		searchPageData.setCategoryCode(categoryCode);
		return searchPageData;
	}

	protected FacetRefinement<SearchStateData> performSearchAndGetFacets(final String categoryCode, final String searchQuery,
			final int page, final ShowMode showMode, final String sortCode)
	{
		final ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData = populateSearchPageData(
				categoryCode, searchQuery, page, showMode, sortCode);
		final List<FacetData<SearchStateData>> facets = refineFacets(searchPageData.getFacets(),
				convertBreadcrumbsToFacets(searchPageData.getBreadcrumbs()));
		final FacetRefinement<SearchStateData> refinement = new FacetRefinement<>();
		refinement.setFacets(facets);
		refinement.setCount(searchPageData.getPagination().getTotalNumberOfResults());
		refinement.setBreadcrumbs(searchPageData.getBreadcrumbs());

		return refinement;
	}

	protected SearchResultsData<ProductData> performSearchAndGetResultsData(final String categoryCode, final String searchQuery,
			final int page, final ShowMode showMode, final String sortCode)
	{
		final ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData = populateSearchPageData(
				categoryCode, searchQuery, page, showMode, sortCode);
		final SearchResultsData<ProductData> searchResultsData = new SearchResultsData<>();
		searchResultsData.setResults(searchPageData.getResults());
		searchResultsData.setPagination(searchPageData.getPagination());

		return searchResultsData;
	}


	protected ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> populateSearchPageData(
			final String categoryCode, final String searchQuery, final int page, final ShowMode showMode, final String sortCode)
	{
		final CategoryModel category = getCommerceCategoryService().getCategoryForCode(categoryCode);
		final CategoryPageModel categoryPage = getCategoryPage(category);
		final CategorySearchEvaluator categorySearch = new CategorySearchEvaluator(categoryCode, searchQuery, page, showMode,
				sortCode, categoryPage);
		categorySearch.doSearch();

		return categorySearch.getSearchPageData();
	}


	protected CategoryPageModel getDefaultCategoryPage()
	{
		try
		{
			return cmsPageService.getPageForCategory(null);
		}
		catch (final CMSItemNotFoundException ignore) // NOSONAR
		{
			// Ignore
		}
		return null;
	}

	protected boolean categoryHasDefaultPage(final CategoryPageModel categoryPage)
	{
		return Boolean.TRUE.equals(categoryPage.getDefaultPage());
	}

	protected CategoryPageModel getCategoryPage(final CategoryModel category)
	{
		try
		{
			return getCmsPageService().getPageForCategory(category);
		}
		catch (final CMSItemNotFoundException ignore) // NOSONAR
		{
			// Ignore
		}
		return null;
	}

	protected <QUERY> void updatePageTitle(final CategoryModel category, final List<BreadcrumbData<QUERY>> appliedFacets,
			final Model model)
	{
		storeContentPageTitleInModel(model, getPageTitleResolver().resolveCategoryPageTitle(category));
	}

	protected String getViewPage(final CategoryPageModel categoryPage)
	{
		if (categoryPage != null)
		{
			final String targetPage = getViewForPage(categoryPage);
			if (targetPage != null && !targetPage.isEmpty())
			{
				return targetPage;
			}
		}
		return PAGE_ROOT + PRODUCT_GRID_PAGE;
	}


	protected class CategorySearchEvaluator
	{
		private final String categoryCode;
		private final SearchQueryData searchQueryData = new SearchQueryData();
		private final int page;
		private final ShowMode showMode;
		private final String sortCode;
		private CategoryPageModel categoryPage;
		private boolean showCategoriesOnly;
		private ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> searchPageData;

		public CategorySearchEvaluator(final String categoryCode, final String searchQuery, final int page,
				final ShowMode showMode, final String sortCode, final CategoryPageModel categoryPage)
		{
			this.categoryCode = categoryCode;
			this.searchQueryData.setValue(searchQuery);
			this.page = page;
			this.showMode = showMode;
			this.sortCode = sortCode;
			this.categoryPage = categoryPage;
		}

		public void doSearch()
		{
			showCategoriesOnly = false;
			if (searchQueryData.getValue() == null)
			{
				// Direct category link without filtering
				searchPageData = getProductSearchFacade().categorySearch(categoryCode);
				if (categoryPage != null)
				{
					showCategoriesOnly = !categoryHasDefaultPage(categoryPage)
							&& CollectionUtils.isNotEmpty(searchPageData.getSubCategories());
				}
			}
			else
			{
				// We have some search filtering
				if (categoryPage == null || !categoryHasDefaultPage(categoryPage))
				{
					// Load the default category page
					categoryPage = getDefaultCategoryPage();
				}

				final SearchStateData searchState = new SearchStateData();
				searchState.setQuery(searchQueryData);

				final PageableData pageableData = createPageableData(page, getSearchPageSize(), sortCode, showMode);
				searchPageData = getProductSearchFacade().categorySearch(categoryCode, searchState, pageableData);
			}
		}

		public int getPage()
		{
			return page;
		}

		public CategoryPageModel getCategoryPage()
		{
			return categoryPage;
		}

		public boolean isShowCategoriesOnly()
		{
			return showCategoriesOnly;
		}

		public ProductCategorySearchPageData<SearchStateData, ProductData, CategoryData> getSearchPageData()
		{
			return searchPageData;
		}
	}

	/**
	 * @return the productSearchFacade
	 */
	public ProductSearchFacade<ProductData> getProductSearchFacade()
	{
		return productSearchFacade;
	}


	/**
	 * @return the commerceCategoryService
	 */
	public CommerceCategoryService getCommerceCategoryService()
	{
		return commerceCategoryService;
	}


	/**
	 * @return the searchBreadcrumbBuilder
	 */
	public SearchBreadcrumbBuilder getSearchBreadcrumbBuilder()
	{
		return searchBreadcrumbBuilder;
	}


	/**
	 * @return the categoryModelUrlResolver
	 */
	public UrlResolver<CategoryModel> getCategoryModelUrlResolver()
	{
		return categoryModelUrlResolver;
	}


	/**
	 * @return the customerLocationService
	 */
	public CustomerLocationService getCustomerLocationService()
	{
		return customerLocationService;
	}


}
