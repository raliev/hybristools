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
package de.hybris.platform.acceleratorstorefrontcommons.util;

import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;


public class AddressDataUtil
{
	@Resource(name = "userFacade")
	private UserFacade userFacade;

	@Resource(name = "i18NFacade")
	private I18NFacade i18NFacade;

	public void convertBasic(final AddressData source, final AddressForm target)
	{
		target.setAddressId(source.getId());
		target.setTitleCode(source.getTitleCode());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setLine1(source.getLine1());
		target.setLine2(source.getLine2());
		target.setTownCity(source.getTown());
		target.setPostcode(source.getPostalCode());
		target.setCountryIso(source.getCountry().getIsocode());
	}

	public void convert(final AddressData source, final AddressForm target)
	{
		convertBasic(source, target);
		target.setSaveInAddressBook(Boolean.valueOf(source.isVisibleInAddressBook()));
		target.setShippingAddress(Boolean.valueOf(source.isShippingAddress()));
		target.setBillingAddress(Boolean.valueOf(source.isBillingAddress()));
		target.setPhone(source.getPhone());

		if (source.getRegion() != null && !StringUtils.isEmpty(source.getRegion().getIsocode()))
		{
			target.setRegionIso(source.getRegion().getIsocode());
		}
	}

	public AddressData convertToVisibleAddressData(final AddressForm addressForm)
	{
		final AddressData addressData = convertToAddressData(addressForm);
		addressData.setVisibleInAddressBook(true);
		return addressData;
	}

	public AddressData convertToAddressData(final AddressForm addressForm)
	{
		final AddressData addressData = new AddressData();
		addressData.setId(addressForm.getAddressId());
		addressData.setTitleCode(addressForm.getTitleCode());
		addressData.setFirstName(addressForm.getFirstName());
		addressData.setLastName(addressForm.getLastName());
		addressData.setLine1(addressForm.getLine1());
		addressData.setLine2(addressForm.getLine2());
		addressData.setTown(addressForm.getTownCity());
		addressData.setPostalCode(addressForm.getPostcode());
		addressData.setBillingAddress(false);
		addressData.setShippingAddress(true);
		addressData.setPhone(addressForm.getPhone());

		if (addressForm.getCountryIso() != null)
		{
			final CountryData countryData = getI18NFacade().getCountryForIsocode(addressForm.getCountryIso());
			addressData.setCountry(countryData);
		}
		if (addressForm.getRegionIso() != null && !StringUtils.isEmpty(addressForm.getRegionIso()))
		{
			final RegionData regionData = getI18NFacade().getRegion(addressForm.getCountryIso(), addressForm.getRegionIso());
			addressData.setRegion(regionData);
		}

		return addressData;
	}

	public UserFacade getUserFacade()
	{
		return userFacade;
	}

	public void setUserFacade(final UserFacade userFacade)
	{
		this.userFacade = userFacade;
	}

	public I18NFacade getI18NFacade()
	{
		return i18NFacade;
	}

	public void setI18NFacade(final I18NFacade i18nFacade)
	{
		i18NFacade = i18nFacade;
	}


}
