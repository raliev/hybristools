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

import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.LoginForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.RegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.security.AutoLoginStrategy;
import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


public abstract class AbstractRegisterPageController extends AbstractPageController
{
	protected static final String CHECKOUT_ORDER_CONFIRMATION_CMS_PAGE_LABEL = "orderConfirmation";

	/**
	 * @deprecated Since 6.0. use
	 *             {@link de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractRegisterPageController#LOGGER}
	 *             instead.
	 */
	@Deprecated
	protected static final Logger LOG = Logger.getLogger(AbstractRegisterPageController.class);
	private static final Logger LOGGER = Logger.getLogger(AbstractRegisterPageController.class);

	private static final String FORM_GLOBAL_ERROR = "form.global.error";

	@Resource(name = "autoLoginStrategy")
	private AutoLoginStrategy autoLoginStrategy;

	@Resource(name = "guidCookieStrategy")
	private GUIDCookieStrategy guidCookieStrategy;

	@Resource(name = "userFacade")
	private UserFacade userFacade;

	@Resource(name = "registrationValidator")
	private Validator registrationValidator;

	protected abstract AbstractPageModel getCmsPage() throws CMSItemNotFoundException;

	protected abstract String getSuccessRedirect(final HttpServletRequest request, final HttpServletResponse response);

	protected abstract String getView();

	/**
	 * @return the registrationValidator
	 */
	protected Validator getRegistrationValidator()
	{
		return registrationValidator;
	}

	/**
	 * @return the autoLoginStrategy
	 */
	protected AutoLoginStrategy getAutoLoginStrategy()
	{
		return autoLoginStrategy;
	}

	/**
	 * @return the userFacade
	 */
	protected UserFacade getUserFacade()
	{
		return userFacade;
	}

	/**
	 *
	 * @return GUIDCookieStrategy
	 */
	protected GUIDCookieStrategy getGuidCookieStrategy()
	{
		return guidCookieStrategy;
	}

	@ModelAttribute("titles")
	public Collection<TitleData> getTitles()
	{
		return userFacade.getTitles();
	}

	protected String getDefaultRegistrationPage(final Model model) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getCmsPage());
		setUpMetaDataForContentPage(model, (ContentPageModel) getCmsPage());
		final Breadcrumb loginBreadcrumbEntry = new Breadcrumb("#", getMessageSource().getMessage("header.link.login", null,
				getI18nService().getCurrentLocale()), null);
		model.addAttribute("breadcrumbs", Collections.singletonList(loginBreadcrumbEntry));
		model.addAttribute(new RegisterForm());
		return getView();
	}

	/**
	 * This method takes data from the registration form and create a new customer account and attempts to log in using
	 * the credentials of this new user.
	 *
	 * @return true if there are no binding errors or the account does not already exists.
	 * @throws CMSItemNotFoundException
	 */
	protected String processRegisterUserRequest(final String referer, final RegisterForm form, final BindingResult bindingResult,
			final Model model, final HttpServletRequest request, final HttpServletResponse response,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			model.addAttribute(form);
			model.addAttribute(new LoginForm());
			model.addAttribute(new GuestForm());
			GlobalMessages.addErrorMessage(model, FORM_GLOBAL_ERROR);
			return handleRegistrationError(model);
		}

		final RegisterData data = new RegisterData();
		data.setFirstName(form.getFirstName());
		data.setLastName(form.getLastName());
		data.setLogin(form.getEmail());
		data.setPassword(form.getPwd());
		data.setTitleCode(form.getTitleCode());
		try
		{
			getCustomerFacade().register(data);
			getAutoLoginStrategy().login(form.getEmail().toLowerCase(), form.getPwd(), request, response);

			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
					"registration.confirmation.message.title");
		}
		catch (final DuplicateUidException e)
		{
			LOGGER.warn("registration failed: " + e);
			model.addAttribute(form);
			model.addAttribute(new LoginForm());
			model.addAttribute(new GuestForm());
			bindingResult.rejectValue("email", "registration.error.account.exists.title");
			GlobalMessages.addErrorMessage(model, FORM_GLOBAL_ERROR);
			return handleRegistrationError(model);
		}

		return REDIRECT_PREFIX + getSuccessRedirect(request, response);
	}

	/**
	 * Anonymous checkout process.
	 *
	 * Creates a new guest customer and updates the session cart with this user. The session user will be anonymous and
	 * it's never updated with this guest user.
	 *
	 * If email is required, grab the email from the form and set it as uid with "guid|email" format.
	 *
	 * @throws de.hybris.platform.cms2.exceptions.CMSItemNotFoundException
	 */
	protected String processAnonymousCheckoutUserRequest(final GuestForm form, final BindingResult bindingResult,
			final Model model, final HttpServletRequest request, final HttpServletResponse response) throws CMSItemNotFoundException
	{
		try
		{
			if (bindingResult.hasErrors())
			{
				model.addAttribute(form);
				model.addAttribute(new LoginForm());
				model.addAttribute(new RegisterForm());
				GlobalMessages.addErrorMessage(model, FORM_GLOBAL_ERROR);
				return handleRegistrationError(model);
			}

			getCustomerFacade().createGuestUserForAnonymousCheckout(form.getEmail(),
					getMessageSource().getMessage("text.guest.customer", null, getI18nService().getCurrentLocale()));
			getGuidCookieStrategy().setCookie(request, response);
			getSessionService().setAttribute(WebConstants.ANONYMOUS_CHECKOUT, Boolean.TRUE);
		}
		catch (final DuplicateUidException e)
		{
			LOGGER.warn("guest registration failed: " + e);
			GlobalMessages.addErrorMessage(model, FORM_GLOBAL_ERROR);
			return handleRegistrationError(model);
		}

		return REDIRECT_PREFIX + getSuccessRedirect(request, response);
	}


	protected String handleRegistrationError(final Model model) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getCmsPage());
		setUpMetaDataForContentPage(model, (ContentPageModel) getCmsPage());
		return getView();
	}
}
