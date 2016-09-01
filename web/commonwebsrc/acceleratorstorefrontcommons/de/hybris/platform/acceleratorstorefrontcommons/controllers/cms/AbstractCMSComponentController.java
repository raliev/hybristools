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
package de.hybris.platform.acceleratorstorefrontcommons.controllers.cms;

import de.hybris.platform.acceleratorcms.services.CMSPageContextService;
import de.hybris.platform.acceleratorservices.data.RequestContextData;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.contents.components.AbstractCMSComponentModel;
import de.hybris.platform.cms2.servicelayer.services.CMSComponentService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * Abstract Controller for CMS Components
 */
public abstract class AbstractCMSComponentController<T extends AbstractCMSComponentModel> extends AbstractController
{
	protected static final String COMPONENT_UID = "componentUid";
	protected static final String COMPONENT = "component";
	protected static final String SANITIZE_REGEX = "[^a-zA-Z0-9_]";

	/**
	 * @deprecated Since 6.0. use
	 *             {@link de.hybris.platform.acceleratorstorefrontcommons.controllers.cms.AbstractCMSComponentController#LOGGER}
	 *             instead.
	 */
	@Deprecated
	protected static final Logger LOG = Logger.getLogger(AbstractCMSComponentController.class);
	private static final Logger LOGGER = Logger.getLogger(AbstractCMSComponentController.class);

	@Resource(name = "cmsComponentService")
	private CMSComponentService cmsComponentService;

	@Resource(name = "cmsPageContextService")
	private CMSPageContextService cmsPageContextService;

	// Setter required for UnitTests
	public void setCmsComponentService(final CMSComponentService cmsComponentService)
	{
		this.cmsComponentService = cmsComponentService;
	}

	@RequestMapping
	public String handleGet(final HttpServletRequest request, final HttpServletResponse response, final Model model)
	{

		T component = (T) request.getAttribute(COMPONENT);
		if (component != null)
		{
			// Add the component to the model
			model.addAttribute(COMPONENT, component);

			// Allow subclasses to handle the component
			return handleComponent(request, response, model, component);
		}

		String componentUid = (String) request.getAttribute(COMPONENT_UID);
		if (StringUtils.isEmpty(componentUid))
		{
			componentUid = request.getParameter(COMPONENT_UID);
		}

		checkIfComponentNotEmpty(componentUid);

		try
		{
			component = getCmsComponentService().getAbstractCMSComponent(componentUid);
			if (component == null)
			{
				LOGGER.error("Component with UID [" + componentUid.replaceAll(SANITIZE_REGEX, "") + "] is null");
				throw new AbstractPageController.HttpNotFoundException();
			}
			else
			{
				// Add the component to the model
				model.addAttribute(COMPONENT, component);

				// Allow subclasses to handle the component
				return handleComponent(request, response, model, component);
			}
		}
		catch (final CMSItemNotFoundException e)
		{
			LOGGER.error("Could not find component with UID [" + componentUid.replaceAll(SANITIZE_REGEX, "") + "]");
			throw new AbstractPageController.HttpNotFoundException(e);
		}
	}

	protected void checkIfComponentNotEmpty(final String componentUid)
	{
		if (StringUtils.isEmpty(componentUid))
		{
			LOGGER.error("No component specified in [" + COMPONENT_UID + "]");
			throw new HttpNotFoundException();
		}
	}

	protected String handleComponent(final HttpServletRequest request, final HttpServletResponse response, final Model model,
			final T component)
	{
		fillModel(request, model, component);
		return getView(component);
	}

	protected abstract void fillModel(final HttpServletRequest request, final Model model, final T component);

	protected abstract String getView(final T component);

	protected String getTypeCode(final T component)
	{
		return component.getItemtype();
	}

	protected CMSComponentService getCmsComponentService()
	{
		return cmsComponentService;
	}

	protected CMSPageContextService getCmsPageContextService()
	{
		return cmsPageContextService;
	}

	protected RequestContextData getRequestContextData(final HttpServletRequest request)
	{
		return getBean(request, "requestContextData", RequestContextData.class);
	}
}
