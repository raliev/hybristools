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

import de.hybris.platform.acceleratorservices.config.HostConfigService;
import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorservices.data.RequestContextData;
import de.hybris.platform.acceleratorservices.storefront.data.MetaElementData;
import de.hybris.platform.acceleratorservices.storefront.util.PageTitleResolver;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.tags.Functions;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.cms2.model.pages.PageTemplateModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commercefacades.storesession.data.CurrencyData;
import de.hybris.platform.commercefacades.storesession.data.LanguageData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;


/**
 * Base controller for all page controllers. Provides common functionality for all page controllers.
 */
public abstract class AbstractPageController extends AbstractController
{
	public static final String PAGE_ROOT = "pages/";
	public static final String CMS_PAGE_MODEL = "cmsPage";
	public static final String CMS_PAGE_TITLE = "pageTitle";

	/**
	 * @deprecated Since 6.0. use
	 *             {@link de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController#LOGGER}
	 *             instead.
	 */
	@Deprecated
	protected static final Logger LOG = Logger.getLogger(AbstractPageController.class);
	private static final Logger LOGGER = Logger.getLogger(AbstractPageController.class);
	private static final String ERROR_CMS_PAGE = "notFound";

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "cmsPageService")
	private CMSPageService cmsPageService;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	@Resource(name = "customerFacade")
	private CustomerFacade customerFacade;

	@Resource(name = "pageTitleResolver")
	private PageTitleResolver pageTitleResolver;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "hostConfigService")
	private HostConfigService hostConfigService;

	@Resource(name = "messageSource")
	private MessageSource messageSource;

	@Resource(name = "i18nService")
	private I18NService i18nService;

	@Resource(name = "siteConfigService")
	private SiteConfigService siteConfigService;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	protected SiteConfigService getSiteConfigService()
	{
		return siteConfigService;
	}

	protected CMSSiteService getCmsSiteService()
	{
		return cmsSiteService;
	}

	protected CMSPageService getCmsPageService()
	{
		return cmsPageService;
	}

	protected StoreSessionFacade getStoreSessionFacade()
	{
		return storeSessionFacade;
	}

	protected CustomerFacade getCustomerFacade()
	{
		return customerFacade;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	protected HostConfigService getHostConfigService()
	{
		return hostConfigService;
	}

	protected MessageSource getMessageSource()
	{
		return messageSource;
	}

	protected I18NService getI18nService()
	{
		return i18nService;
	}

	@ModelAttribute("languages")
	public Collection<LanguageData> getLanguages()
	{
		return storeSessionFacade.getAllLanguages();
	}

	@ModelAttribute("currencies")
	public Collection<CurrencyData> getCurrencies()
	{
		return storeSessionFacade.getAllCurrencies();
	}

	@ModelAttribute("currentLanguage")
	public LanguageData getCurrentLanguage()
	{
		return storeSessionFacade.getCurrentLanguage();
	}

	@ModelAttribute("currentCurrency")
	public CurrencyData getCurrentCurrency()
	{
		return storeSessionFacade.getCurrentCurrency();
	}

	@ModelAttribute("siteName")
	public String getSiteName()
	{
		final CMSSiteModel site = cmsSiteService.getCurrentSite();
		return site != null ? site.getName() : "";
	}

	@ModelAttribute("user")
	public CustomerData getUser()
	{
		return customerFacade.getCurrentCustomer();
	}

	protected void storeCmsPageInModel(final Model model, final AbstractPageModel cmsPage)
	{
		if (model != null && cmsPage != null)
		{
			model.addAttribute(CMS_PAGE_MODEL, cmsPage);
			if (cmsPage instanceof ContentPageModel)
			{
				storeContentPageTitleInModel(model, getPageTitleResolver().resolveContentPageTitle(cmsPage.getTitle()));
			}
		}
	}

	protected void storeContentPageTitleInModel(final Model model, final String title)
	{
		model.addAttribute(CMS_PAGE_TITLE, title);
	}

	protected String getViewForPage(final Model model)
	{
		if (model.containsAttribute(CMS_PAGE_MODEL))
		{
			final AbstractPageModel page = (AbstractPageModel) model.asMap().get(CMS_PAGE_MODEL);
			if (page != null)
			{
				return getViewForPage(page);
			}
		}
		return null;
	}

	protected String getViewForPage(final AbstractPageModel page)
	{
		if (page != null)
		{
			final PageTemplateModel masterTemplate = page.getMasterTemplate();
			if (masterTemplate != null)
			{
				final String targetPage = cmsPageService.getFrontendTemplateName(masterTemplate);
				if (targetPage != null && !targetPage.isEmpty())
				{
					return PAGE_ROOT + targetPage;
				}
			}
		}
		return null;
	}

	/**
	 * Checks request URL against properly resolved URL and returns null if url is proper or redirection string if not.
	 *
	 * @param request
	 *           - request that contains current URL
	 * @param response
	 *           - response to write "301 Moved Permanently" status to if redirected
	 * @param resolvedUrlPath
	 *           - properly resolved URL
	 * @return null if url is properly resolved or redirection string if not
	 * @throws UnsupportedEncodingException
	 */
	protected String checkRequestUrl(final HttpServletRequest request, final HttpServletResponse response,
			final String resolvedUrlPath) throws UnsupportedEncodingException
	{
		try
		{
			final String resolvedUrl = response.encodeURL(request.getContextPath() + resolvedUrlPath);
			final String requestURI = URIUtil.decode(request.getRequestURI(), "utf-8");
			final String decoded = URIUtil.decode(resolvedUrl, "utf-8");
			if (StringUtils.isNotEmpty(requestURI) && requestURI.endsWith(decoded))
			{
				return null;
			}
			else
			{
				//  org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE = "org.springframework.web.servlet.View.responseStatus"
				request.setAttribute("org.springframework.web.servlet.View.responseStatus", HttpStatus.MOVED_PERMANENTLY);
				final String queryString = request.getQueryString();
				if (queryString != null && !queryString.isEmpty())
				{
					return "redirect:" + resolvedUrlPath + "?" + queryString;
				}
				return "redirect:" + resolvedUrlPath;
			}
		}
		catch (final URIException e)
		{
			LOGGER.error("URIException:" + e.getMessage(), e);
			throw new UnsupportedEncodingException(e.getMessage());
		}
	}

	protected ContentPageModel getContentPageForLabelOrId(final String labelOrId) throws CMSItemNotFoundException
	{
		String key = labelOrId;
		if (StringUtils.isEmpty(labelOrId))
		{
			// Fallback to site home page
			final ContentPageModel homePage = cmsPageService.getHomepage();
			if (homePage != null)
			{
				key = cmsPageService.getLabelOrId(homePage);
			}
			else
			{
				// Fallback to site start page label
				final CMSSiteModel site = cmsSiteService.getCurrentSite();
				if (site != null)
				{
					key = cmsSiteService.getStartPageLabelOrId(site);
				}
			}
		}

		// Actually resolve the label or id - running cms restrictions
		return cmsPageService.getPageForLabelOrId(key);
	}

	protected PageTitleResolver getPageTitleResolver()
	{
		return pageTitleResolver;
	}

	protected void storeContinueUrl(final HttpServletRequest request)
	{
		final StringBuilder url = new StringBuilder();
		url.append(request.getServletPath());
		final String queryString = request.getQueryString();
		if (queryString != null && !queryString.isEmpty())
		{
			url.append('?').append(queryString);
		}
		getSessionService().setAttribute(WebConstants.CONTINUE_URL, url.toString());
	}

	protected void setUpMetaData(final Model model, final String metaKeywords, final String metaDescription)
	{
		final List<MetaElementData> metadata = new LinkedList<>();
		metadata.add(createMetaElement("keywords", metaKeywords));
		metadata.add(createMetaElement("description", metaDescription));
		model.addAttribute("metatags", metadata);
	}

	protected MetaElementData createMetaElement(final String name, final String content)
	{
		final MetaElementData element = new MetaElementData();
		element.setName(name);
		element.setContent(content);
		return element;
	}

	protected void setUpMetaDataForContentPage(final Model model, final ContentPageModel contentPage)
	{
		setUpMetaData(model, contentPage.getKeywords(), contentPage.getDescription());
	}

	protected RequestContextData getRequestContextData(final HttpServletRequest request)
	{
		return getBean(request, "requestContextData", RequestContextData.class);
	}

	/**
	 * Url encode.
	 *
	 * @param url
	 *           the url
	 * @return the encoded string
	 * @throws IllegalArgumentException
	 *            if url is null
	 */
	protected String urlEncode(final String url)
	{
		Assert.notNull(url, "Parameter [url] cannot be null");
		return Functions.encodeUrl(url);
	}

	/**
	 * Prepares Not Found (404) Page
	 *
	 * @param model
	 *           The model to pass with the page
	 * @param response
	 *           object represents http response needed for setting not found status.
	 * @throws CMSItemNotFoundException
	 *            when cannot find ContentPage with {@value #ERROR_CMS_PAGE} id
	 *
	 */
	protected void prepareNotFoundPage(final Model model, final HttpServletResponse response) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getContentPageForLabelOrId(ERROR_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(ERROR_CMS_PAGE));
		model.addAttribute(WebConstants.MODEL_KEY_ADDITIONAL_BREADCRUMB,
				resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.not.found"));
		GlobalMessages.addErrorMessage(model, "system.error.page.not.found");
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);

	}
}
