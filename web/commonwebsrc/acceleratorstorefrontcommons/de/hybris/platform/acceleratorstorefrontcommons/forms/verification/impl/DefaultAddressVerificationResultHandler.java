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
package de.hybris.platform.acceleratorstorefrontcommons.forms.verification.impl;

import de.hybris.platform.commercefacades.address.data.AddressVerificationErrorField;
import de.hybris.platform.commercefacades.address.data.AddressVerificationResult;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.address.AddressFieldType;
import de.hybris.platform.commerceservices.address.AddressVerificationDecision;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.verification.AddressVerificationResultHandler;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Default implementation of {@link AddressVerificationResultHandler}.
 */
@Component("addressVerificationResultHandler")
public class DefaultAddressVerificationResultHandler implements AddressVerificationResultHandler
{

	@Override
	public boolean handleResult(final AddressVerificationResult verificationResult, final AddressData newAddress,
			final Model model, final RedirectAttributes redirectModel, final Errors bindingResult, final boolean customerBypass,
			final String successMsg)
	{
		if (verificationResult != null)
		{
			if (isResultUnknown(verificationResult))
			{
				// Unknown error. Services are likely down.
				return false;
			}

			if (resultHasSuggestedAddresses(verificationResult))
			{
				model.addAttribute("inputAddress", newAddress);
				model.addAttribute("customerBypass", Boolean.valueOf(customerBypass));
				model.addAttribute("suggestedAddresses", verificationResult.getSuggestedAddresses());
				model.addAttribute("saveInAddressBook", Boolean.valueOf(newAddress.isVisibleInAddressBook()));
				return true;
			}
			else if (resultHasErrors(verificationResult))
			{
				addFieldErrorsToBindingResult(verificationResult.getErrors(), bindingResult);
				GlobalMessages.addErrorMessage(model, "address.error.formentry.invalid");
				return true;
			}
			else
			{
				// The address was successfully verified by Cybersource. Inform the user and proceed.
				return false;
			}
		}

		return false;
	}

	protected boolean resultHasErrors(final AddressVerificationResult verificationResult)
	{
		return verificationResult.getErrors() != null && !verificationResult.getErrors().isEmpty();
	}

	protected boolean resultHasSuggestedAddresses(final AddressVerificationResult verificationResult)
	{
		return verificationResult.getSuggestedAddresses() != null && !verificationResult.getSuggestedAddresses().isEmpty();
	}

	protected boolean isResultUnknown(final AddressVerificationResult verificationResult)
	{
		return verificationResult.getDecision() != null
				&& verificationResult.getDecision().equals(AddressVerificationDecision.UNKNOWN);
	}

	protected void addFieldErrorsToBindingResult(final Map<String, AddressVerificationErrorField> fieldErrors,
			final Errors bindingResult)
	{
		for (final String errorKey : fieldErrors.keySet())
		{
			processFields(fieldErrors, bindingResult, errorKey);
		}
	}

	protected void processFields(final Map<String, AddressVerificationErrorField> fieldErrors,
							   final Errors bindingResult, final String errorKey)
	{
		switch (AddressFieldType.lookup(fieldErrors.get(errorKey).getName()))
		{
            case TITLE_CODE:
                bindingResult.rejectValue("titleCode", "address.title.invalid");
                break;
            case FIRST_NAME:
                bindingResult.rejectValue("firstName", "address.firstName.invalid");
                break;
            case LAST_NAME:
                bindingResult.rejectValue("lastName", "address.lastName.invalid");
                break;
            case ADDRESS_LINE1:
                bindingResult.rejectValue("line1", "address.line1.invalid");
                break;
            case ADDRESS_LINE2:
                bindingResult.rejectValue("line2", "address.line2.invalid");
                break;
            case CITY:
                bindingResult.rejectValue("townCity", "address.townCity.invalid");
                break;
            case REGION:
                bindingResult.rejectValue("regionIso", "address.regionIso.invalid");
                break;
            case ZIP_CODE:
                bindingResult.rejectValue("postcode", "address.postcode.invalid");
                break;
            case COUNTRY:
                bindingResult.rejectValue("countryIso", "address.country.invalid");
                break;
            default:
                break;
        }
	}
}
