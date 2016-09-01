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
package de.hybris.platform.acceleratorstorefrontcommons.controllers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Displays "confirmation, information, error" messages
 */
public final class GlobalMessages
{
	public static final String CONF_MESSAGES_HOLDER = "accConfMsgs";
	public static final String INFO_MESSAGES_HOLDER = "accInfoMsgs";
	public static final String ERROR_MESSAGES_HOLDER = "accErrorMsgs";

	private GlobalMessages()
	{
		//Utility classes, which are a collection of static members, are not meant to be instantiated
	}

	public static void addConfMessage(final Model model, final String messageKey)
	{
		addMessage(model, CONF_MESSAGES_HOLDER, messageKey, null);
	}

	public static void addInfoMessage(final Model model, final String messageKey)
	{
		addMessage(model, INFO_MESSAGES_HOLDER, messageKey, null);
	}

	public static void addErrorMessage(final Model model, final String messageKey)
	{
		addMessage(model, ERROR_MESSAGES_HOLDER, messageKey, null);
	}

	public static void addMessage(final Model model, final String messageHolder, final String messageKey,
			final Object[] attributes)
	{
		final GlobalMessage message = new GlobalMessage();
		message.setCode(messageKey);
		message.setAttributes(attributes != null ? Arrays.asList(attributes) : Collections.emptyList());

		final Map<String, Object> modelMap = model.asMap();
		if (modelMap.containsKey(messageHolder))
		{
			final List<GlobalMessage> messages = new ArrayList<>((List<GlobalMessage>)
					modelMap.get(messageHolder));
			messages.add(message);
			model.addAttribute(messageHolder, messages);
		}
		else
		{
			model.addAttribute(messageHolder, Collections.singletonList(message));
		}
	}

	public static void addFlashMessage(final RedirectAttributes model, final String messageHolder, final String messageKey)
	{
		addFlashMessage(model, messageHolder, messageKey, null);
	}

	public static void addFlashMessage(final RedirectAttributes model, final String messageHolder, final String messageKey,
			final Object[] attributes)
	{
		final GlobalMessage message = new GlobalMessage();
		message.setCode(messageKey);
		message.setAttributes(attributes != null ? Arrays.asList(attributes) : Collections.emptyList());

		final Map<String, ?> flashModelMap = model.getFlashAttributes();
		if (flashModelMap.containsKey(messageHolder))
		{
			final List<GlobalMessage> messages = new ArrayList<>((List<GlobalMessage>)
					flashModelMap.get(messageHolder));
			messages.add(message);
			model.addFlashAttribute(messageHolder, messages);
		}
		else
		{
			model.addFlashAttribute(messageHolder, Collections.singletonList(message));
		}
	}
}
