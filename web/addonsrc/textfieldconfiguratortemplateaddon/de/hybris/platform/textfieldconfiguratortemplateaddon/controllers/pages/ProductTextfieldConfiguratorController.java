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
 *
*/
package de.hybris.platform.textfieldconfiguratortemplateaddon.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.impl.ProductBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.catalog.enums.ConfiguratorType;
import de.hybris.platform.catalog.enums.ProductInfoStatus;
import de.hybris.platform.textfieldconfiguratortemplateaddon.forms.TextFieldConfigurationForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.ConfigurationInfoData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.textfieldconfiguratortemplateaddon.controllers.TextFieldConfigurationValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


@Controller
public class ProductTextfieldConfiguratorController extends AbstractPageController
{
	private static final String TYPE_MISMATCH_ERROR_CODE = "typeMismatch";
	private static final String ERROR_MSG_TYPE = "errorMsg";
	private static final String QUANTITY_INVALID_BINDING_MESSAGE_KEY = "basket.error.quantity.invalid.binding";
	public static final String PRODUCT_CONFIGURATOR_PAGE = "addon:/textfieldconfiguratortemplateaddon/pages/productConfiguratorPage";
	public static final String ENTRY_CONFIGURATOR_PAGE = "addon:/textfieldconfiguratortemplateaddon/pages/cartEntryConfiguratorPage";
	public static final String TEXTFIELDCONFIGURATOR_TYPE = "TEXTFIELD";
	public static final String PAGE_LABEL = "configure/" + TEXTFIELDCONFIGURATOR_TYPE;


	@Resource
	private ProductFacade productFacade;

	@Resource
	private CartFacade cartFacade;

	@Resource(name = "productBreadcrumbBuilder")
	private ProductBreadcrumbBuilder productBreadcrumbBuilder;

	@Resource
	private TextFieldConfigurationValidator textFieldConfigurationValidator;

	@InitBinder
	protected void initBinder(final WebDataBinder binder)
	{
		if (binder.getTarget() != null && TextFieldConfigurationForm.class.isAssignableFrom(binder.getTarget().getClass()))
		{
			binder.setValidator(getTextFieldConfigurationValidator());
		}
	}

	@RequestMapping(value = "/**/p/{productCode}/configure/" + TEXTFIELDCONFIGURATOR_TYPE)
	public String productConfigurator(@PathVariable("productCode") final String productCode, final Model model)
			throws CMSItemNotFoundException
	{
		storePageData(productCode, getProductFacade().getConfiguratorSettingsForCode(productCode), model);
		return PRODUCT_CONFIGURATOR_PAGE;
	}

	@RequestMapping(value = "/**/p/{productCode}/configure/" + TEXTFIELDCONFIGURATOR_TYPE, method = RequestMethod.POST)
	public String addToCart(@PathVariable("productCode") final String productCode, final Model model,
			@ModelAttribute("foo") @Valid final TextFieldConfigurationForm form, final BindingResult bindingErrors,
			final HttpServletRequest request, final RedirectAttributes redirectModel)
	{
		boolean err = false;
		if (bindingErrors.hasErrors())
		{
			bindingErrors.getAllErrors().forEach(error -> GlobalMessages.addFlashMessage(
					redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, error.getCode()));
			err = true;
		}
		else
		{
			final long qty = form.getQuantity();
			try
			{
				final CartModificationData cartModification = cartFacade.addToCart(productCode, qty);
				if (cartModification == null)
				{
					throw new CommerceCartModificationException("Null cart modification");
				}
				if (cartModification.getQuantityAdded() > 0)
				{
					cartFacade.updateCartEntry(enrichOrderEntryWithConfigurationData(form, cartModification.getEntry()));
					model.addAttribute("quantity", cartModification.getQuantityAdded());
					model.addAttribute("entry", cartModification.getEntry());
				}

				if (cartModification.getQuantityAdded() == 0L)
				{
					err = true;
					GlobalMessages.addFlashMessage(
							redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
							"basket.information.quantity.noItemsAdded." + cartModification.getStatusCode());
				}
				else if (cartModification.getQuantityAdded() < qty)
				{
					err = true;
					GlobalMessages.addFlashMessage(
							redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
							"basket.information.quantity.reducedNumberOfItemsAdded." + cartModification.getStatusCode());
				}
			}
			catch (final CommerceCartModificationException ex)
			{
				err = true;
				GlobalMessages.addFlashMessage(
						redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "basket.error.occurred");
			}
		}

		if (err)
		{
			return REDIRECT_PREFIX + request.getServletPath();
		}
		model.addAttribute("product",
				productFacade.getProductForCodeAndOptions(productCode, Collections.singletonList(ProductOption.BASIC)));
		return REDIRECT_PREFIX + "/cart";
	}

	@RequestMapping(value = "/cart/{entryNumber}/configuration/" + TEXTFIELDCONFIGURATOR_TYPE)
	public String editConfigurationInEntry(@PathVariable("entryNumber") final int entryNumber, final Model model) throws
			CMSItemNotFoundException, CommerceCartModificationException
	{
		final CartData cart = cartFacade.getSessionCart();
		final OrderEntryData entry = getOrderEntry(entryNumber, cart);
		model.addAttribute("entryNumber", entryNumber);
		storePageData(entry.getProduct().getCode(), entry.getConfigurationInfos(), model);
		return ENTRY_CONFIGURATOR_PAGE;
	}

	@RequestMapping(value = "/cart/{entryNumber}/configuration/" + TEXTFIELDCONFIGURATOR_TYPE, method = RequestMethod.POST)
	public String updateConfigurationInEntry(@PathVariable("entryNumber") final int entryNumber, final Model model,
			@ModelAttribute("foo") @Valid final TextFieldConfigurationForm form,
			final BindingResult bindingErrors,
			final HttpServletRequest request, final RedirectAttributes redirectModel) throws CommerceCartModificationException
	{
		if (bindingErrors.hasErrors())
		{
			bindingErrors.getAllErrors().forEach(error -> GlobalMessages.addFlashMessage(
					redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, error.getCode()));
			return REDIRECT_PREFIX + request.getServletPath();
		}
		final CartData cart = cartFacade.getSessionCart();
		final OrderEntryData entry = getOrderEntry(entryNumber, cart);
		cartFacade.updateCartEntry(enrichOrderEntryWithConfigurationData(form, entry));
		model.addAttribute("product",
				productFacade
						.getProductForCodeAndOptions(entry.getProduct().getCode(), Collections.singletonList(ProductOption.BASIC)));
		model.addAttribute("quantity", entry.getQuantity());
		model.addAttribute("entry", entry);
		return REDIRECT_PREFIX + "/cart";
	}

	protected void storePageData(final String productCode, final List<ConfigurationInfoData> configuration, final Model model)
			throws CMSItemNotFoundException
	{
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, productBreadcrumbBuilder.getBreadcrumbs(productCode));
		final Set<ProductOption> options = new HashSet<>(Arrays.asList(ProductOption.VARIANT_FIRST_VARIANT, ProductOption.BASIC,
				ProductOption.URL, ProductOption.PRICE, ProductOption.SUMMARY, ProductOption.DESCRIPTION, ProductOption.GALLERY,
				ProductOption.CATEGORIES, ProductOption.REVIEW, ProductOption.PROMOTIONS, ProductOption.CLASSIFICATION,
				ProductOption.VARIANT_FULL, ProductOption.STOCK, ProductOption.VOLUME_PRICES, ProductOption.PRICE_RANGE,
				ProductOption.DELIVERY_MODE_AVAILABILITY));

		final ProductData productData = getProductFacade().getProductForCodeAndOptions(productCode, options);
		model.addAttribute("product", productData);
		model.addAttribute("pageType", PageType.PRODUCT.name());
		final ContentPageModel pageModel = getContentPageForLabelOrId(PAGE_LABEL);
		storeCmsPageInModel(model, pageModel);
		model.addAttribute("configurations", configuration);
	}

	protected OrderEntryData getOrderEntry(final int entryNumber, final CartData cart) throws CommerceCartModificationException
	{
		final List<OrderEntryData> entries = cart.getEntries();
		if (entries == null)
		{
			throw new CommerceCartModificationException("Cart is empty");
		}
		try
		{
			return entries.stream()
					.filter(e -> e != null)
					.filter(e -> e.getEntryNumber() == entryNumber)
					.findAny().get();
		}
		catch (final NoSuchElementException e)
		{
			throw new CommerceCartModificationException("Cart entry #" + entryNumber + " does not exist");
		}
	}

	protected OrderEntryData enrichOrderEntryWithConfigurationData(final TextFieldConfigurationForm form,
			final OrderEntryData orderEntryData)
	{
		final List<ConfigurationInfoData> configurationInfoDataList = new ArrayList<>();
		if (form != null && form.getConfigurationsKeyValueMap() != null)
		{
			for (final Map.Entry<ConfiguratorType, Map<String, String>> item : form.getConfigurationsKeyValueMap().entrySet())
			{
				item.getValue().entrySet().stream().map(formEntry ->
				{
					ConfigurationInfoData configurationInfoData = new ConfigurationInfoData();
					configurationInfoData.setConfigurationLabel(formEntry.getKey());
					configurationInfoData.setConfigurationValue(formEntry.getValue());
					configurationInfoData.setConfiguratorType(item.getKey());
					configurationInfoData.setStatus(ProductInfoStatus.SUCCESS);
					return configurationInfoData;
				}).forEach(configurationInfoDataList::add);
			}
		}
		orderEntryData.setConfigurationInfos(configurationInfoDataList);
		return orderEntryData;
	}

	protected boolean isTypeMismatchError(final ObjectError error)
	{
		return error.getCode().equals(TYPE_MISMATCH_ERROR_CODE);
	}

	protected ProductFacade getProductFacade()
	{
		return productFacade;
	}

	protected TextFieldConfigurationValidator getTextFieldConfigurationValidator()
	{
		return textFieldConfigurationValidator;
	}
}
