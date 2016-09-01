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
package de.hybris.platform.acceleratorstorefrontcommons.forms;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SopPaymentDetailsForm
{
	private String amount;
	private String billTo_city; // NOSONAR
	private String billTo_country; // NOSONAR
	private String billTo_customerID; // NOSONAR
	private String billTo_email; // NOSONAR
	private String billTo_firstName; // NOSONAR
	private String billTo_lastName; // NOSONAR
	private String billTo_phoneNumber; // NOSONAR
	private String billTo_postalCode; // NOSONAR
	private String billTo_titleCode; // NOSONAR
	private String billTo_state; // NOSONAR
	private String billTo_street1; // NOSONAR
	private String billTo_street2; // NOSONAR
	private String card_accountNumber; // NOSONAR
	private String card_cardType; // NOSONAR
	private String card_startMonth; // NOSONAR
	private String card_startYear; // NOSONAR
	private String card_issueNumber; // NOSONAR
	private String card_cvNumber; // NOSONAR
	private String card_expirationMonth; // NOSONAR
	private String card_expirationYear; // NOSONAR
	private String card_nameOnCard; // NOSONAR
	private String comments;
	private String currency;
	private String shipTo_city; // NOSONAR
	private String shipTo_country; // NOSONAR
	private String shipTo_firstName; // NOSONAR
	private String shipTo_lastName; // NOSONAR
	private String shipTo_phoneNumber; // NOSONAR
	private String shipTo_postalCode; // NOSONAR
	private String shipTo_shippingMethod; // NOSONAR
	private String shipTo_state; // NOSONAR
	private String shipTo_street1; // NOSONAR
	private String shipTo_street2; // NOSONAR
	private String taxAmount;
	private boolean savePaymentInfo;
	private boolean useDeliveryAddress;

	private Map<String, String> parameters;


	/**
	 * @return the card_nameOnCard
	 */
	public String getCard_nameOnCard() // NOSONAR  NOPMD
	{
		return card_nameOnCard;
	}

	/**
	 * @param card_nameOnCard
	 *           the card_nameOnCard to set
	 */
	public void setCard_nameOnCard(final String card_nameOnCard) // NOSONAR NOPMD
	{
		this.card_nameOnCard = card_nameOnCard;
	}

	public String getAmount()
	{
		return amount;
	}

	public void setAmount(final String amount)
	{
		this.amount = amount;
	}

	public String getBillTo_city() // NOSONAR NOPMD
	{
		return billTo_city;
	}

	public void setBillTo_city(final String billTo_city) // NOSONAR NOPMD
	{
		this.billTo_city = billTo_city;
	}

	public String getBillTo_country() // NOSONAR NOPMD
	{
		if (billTo_country != null)
		{
			return billTo_country.toUpperCase(Locale.US);
		}
		return billTo_country;
	}

	public void setBillTo_country(final String billTo_country) // NOSONAR NOPMD
	{
		this.billTo_country = billTo_country;
	}

	public String getBillTo_customerID() // NOSONAR NOPMD
	{
		return billTo_customerID;
	}

	public void setBillTo_customerID(final String billTo_customerID) // NOSONAR NOPMD
	{
		this.billTo_customerID = billTo_customerID;
	}

	public String getBillTo_email() // NOSONAR NOPMD
	{
		return billTo_email;
	}

	public void setBillTo_email(final String billTo_email) // NOSONAR NOPMD
	{
		this.billTo_email = billTo_email;
	}

	public String getBillTo_firstName() // NOSONAR NOPMD
	{
		return billTo_firstName;
	}

	public void setBillTo_firstName(final String billTo_firstName) // NOSONAR NOPMD
	{
		this.billTo_firstName = billTo_firstName;
	}

	public String getBillTo_lastName() // NOSONAR NOPMD
	{
		return billTo_lastName;
	}

	public void setBillTo_lastName(final String billTo_lastName) // NOSONAR NOPMD
	{
		this.billTo_lastName = billTo_lastName;
	}

	public String getBillTo_phoneNumber() // NOSONAR NOPMD
	{
		return billTo_phoneNumber;
	}

	public void setBillTo_phoneNumber(final String billTo_phoneNumber) // NOSONAR NOPMD
	{
		this.billTo_phoneNumber = billTo_phoneNumber;
	}

	public String getBillTo_postalCode() // NOSONAR NOPMD
	{
		return billTo_postalCode;
	}

	public void setBillTo_postalCode(final String billTo_postalCode) // NOSONAR NOPMD
	{
		this.billTo_postalCode = billTo_postalCode;
	}

	public String getBillTo_titleCode() // NOSONAR NOPMD
	{
		return billTo_titleCode;
	}

	public void setBillTo_titleCode(final String billTo_titleCode) // NOSONAR NOPMD
	{
		this.billTo_titleCode = billTo_titleCode;
	}

	public String getBillTo_state() // NOSONAR NOPMD
	{
		return billTo_state;
	}

	public void setBillTo_state(final String billTo_state) // NOSONAR NOPMD
	{
		this.billTo_state = billTo_state;
	}

	public String getBillTo_street1() // NOSONAR NOPMD
	{
		return billTo_street1;
	}

	public void setBillTo_street1(final String billTo_street1) // NOSONAR NOPMD
	{
		this.billTo_street1 = billTo_street1;
	}

	public String getBillTo_street2() // NOSONAR NOPMD
	{
		return billTo_street2;
	}

	public void setBillTo_street2(final String billTo_street2) // NOSONAR NOPMD
	{
		this.billTo_street2 = billTo_street2;
	}

	public String getCard_accountNumber() // NOSONAR NOPMD
	{
		return card_accountNumber;
	}

	public void setCard_accountNumber(final String card_accountNumber) // NOSONAR NOPMD
	{
		this.card_accountNumber = card_accountNumber;
	}

	public String getCard_startMonth() // NOSONAR NOPMD
	{
		return card_startMonth;
	}

	public void setCard_startMonth(final String card_startMonth) // NOSONAR NOPMD
	{
		this.card_startMonth = card_startMonth;
	}

	public String getCard_startYear() // NOSONAR NOPMD
	{
		return card_startYear;
	}

	public void setCard_startYear(final String card_startYear) // NOSONAR NOPMD
	{
		this.card_startYear = card_startYear;
	}

	public String getCard_issueNumber() // NOSONAR NOPMD
	{
		return card_issueNumber;
	}

	public void setCard_issueNumber(final String card_issueNumber) // NOSONAR NOPMD
	{
		this.card_issueNumber = card_issueNumber;
	}

	public String getCard_cardType() // NOSONAR NOPMD
	{
		return card_cardType;
	}

	public void setCard_cardType(final String card_cardType) // NOSONAR NOPMD
	{
		this.card_cardType = card_cardType;
	}

	public String getCard_cvNumber() // NOSONAR NOPMD
	{
		return card_cvNumber;
	}

	public void setCard_cvNumber(final String card_cvNumber) // NOSONAR NOPMD
	{
		this.card_cvNumber = card_cvNumber;
	}

	public String getCard_expirationMonth() // NOSONAR NOPMD
	{
		return card_expirationMonth;
	}

	public void setCard_expirationMonth(final String card_expirationMonth) // NOSONAR NOPMD
	{
		this.card_expirationMonth = card_expirationMonth;
	}

	public String getCard_expirationYear() // NOSONAR NOPMD
	{
		return card_expirationYear;
	}

	public void setCard_expirationYear(final String card_expirationYear) // NOSONAR NOPMD
	{
		this.card_expirationYear = card_expirationYear;
	}

	public String getComments()
	{
		return comments;
	}

	public void setComments(final String comments)
	{
		this.comments = comments;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(final String currency)
	{
		this.currency = currency;
	}

	public String getShipTo_city() // NOSONAR NOPMD
	{
		return shipTo_city;
	}

	public void setShipTo_city(final String shipTo_city) // NOSONAR NOPMD
	{
		this.shipTo_city = shipTo_city;
	}

	public String getShipTo_country() // NOSONAR NOPMD
	{
		if (shipTo_country != null)
		{
			return shipTo_country.toUpperCase(Locale.US);
		}
		return shipTo_country;
	}

	public void setShipTo_country(final String shipTo_country) // NOSONAR NOPMD
	{
		this.shipTo_country = shipTo_country;
	}

	public String getShipTo_firstName() // NOSONAR NOPMD
	{
		return shipTo_firstName;
	}

	public void setShipTo_firstName(final String shipTo_firstName) // NOSONAR NOPMD
	{
		this.shipTo_firstName = shipTo_firstName;
	}

	public String getShipTo_lastName() // NOSONAR NOPMD
	{
		return shipTo_lastName;
	}

	public void setShipTo_lastName(final String shipTo_lastName) // NOSONAR NOPMD
	{
		this.shipTo_lastName = shipTo_lastName;
	}

	public String getShipTo_phoneNumber() // NOSONAR NOPMD
	{
		return shipTo_phoneNumber;
	}

	public void setShipTo_phoneNumber(final String shipTo_phoneNumber) // NOSONAR NOPMD
	{
		this.shipTo_phoneNumber = shipTo_phoneNumber;
	}

	public String getShipTo_postalCode() // NOSONAR NOPMD
	{
		return shipTo_postalCode;
	}

	public void setShipTo_postalCode(final String shipTo_postalCode) // NOSONAR NOPMD
	{
		this.shipTo_postalCode = shipTo_postalCode;
	}

	public String getShipTo_shippingMethod() // NOSONAR NOPMD
	{
		return shipTo_shippingMethod;
	}

	public void setShipTo_shippingMethod(final String shipTo_shippingMethod) // NOSONAR NOPMD
	{
		this.shipTo_shippingMethod = shipTo_shippingMethod;
	}

	public String getShipTo_state() // NOSONAR NOPMD
	{
		return shipTo_state;
	}

	public void setShipTo_state(final String shipTo_state) // NOSONAR NOPMD
	{
		this.shipTo_state = shipTo_state;
	}

	public String getShipTo_street1() // NOSONAR NOPMD
	{
		return shipTo_street1;
	}

	public void setShipTo_street1(final String shipTo_street1) // NOSONAR NOPMD
	{
		this.shipTo_street1 = shipTo_street1;
	}

	public String getShipTo_street2() // NOSONAR NOPMD
	{
		return shipTo_street2;
	}

	public void setShipTo_street2(final String shipTo_street2) // NOSONAR NOPMD
	{
		this.shipTo_street2 = shipTo_street2;
	}

	public String getTaxAmount()
	{
		return taxAmount;
	}

	public void setTaxAmount(final String taxAmount)
	{
		this.taxAmount = taxAmount;
	}

	public boolean isSavePaymentInfo()
	{
		return savePaymentInfo;
	}

	public void setSavePaymentInfo(final boolean savePaymentInfo)
	{
		this.savePaymentInfo = savePaymentInfo;
	}

	public boolean isUseDeliveryAddress()
	{
		return useDeliveryAddress;
	}

	public void setUseDeliveryAddress(final boolean useDeliveryAddress)
	{
		this.useDeliveryAddress = useDeliveryAddress;
	}

	public Map<String, String> getParameters()
	{
		return parameters;
	}

	public void setParameters(final Map<String, String> parameters)
	{
		this.parameters = parameters;
	}

	public Map<String, String> getSubscriptionSignatureParams()
	{
		if (parameters != null)
		{
			final Map<String, String> subscriptionSignatureParams = new HashMap<>();
			subscriptionSignatureParams.put("recurringSubscriptionInfo_amount", parameters.get("recurringSubscriptionInfo_amount"));
			subscriptionSignatureParams.put("recurringSubscriptionInfo_numberOfPayments",
					parameters.get("recurringSubscriptionInfo_numberOfPayments"));
			subscriptionSignatureParams.put("recurringSubscriptionInfo_frequency",
					parameters.get("recurringSubscriptionInfo_frequency"));
			subscriptionSignatureParams.put("recurringSubscriptionInfo_automaticRenew",
					parameters.get("recurringSubscriptionInfo_automaticRenew"));
			subscriptionSignatureParams.put("recurringSubscriptionInfo_startDate",
					parameters.get("recurringSubscriptionInfo_startDate"));
			subscriptionSignatureParams.put("recurringSubscriptionInfo_signaturePublic",
					parameters.get("recurringSubscriptionInfo_signaturePublic"));
			return subscriptionSignatureParams;
		}
		return null;
	}

	public Map<String, String> getSignatureParams()
	{
		if (parameters != null)
		{
			final Map<String, String> signatureParams = new HashMap<>();
			signatureParams.put("orderPage_signaturePublic", parameters.get("orderPage_signaturePublic"));
			signatureParams.put("merchantID", parameters.get("merchantID"));
			signatureParams.put("amount", parameters.get("amount"));
			signatureParams.put("currency", parameters.get("currency"));
			signatureParams.put("orderPage_timestamp", parameters.get("orderPage_timestamp"));
			signatureParams.put("orderPage_transactionType", parameters.get("orderPage_transactionType"));
			signatureParams.put("orderPage_version", parameters.get("orderPage_version"));
			signatureParams.put("orderPage_serialNumber", parameters.get("orderPage_serialNumber"));
			return signatureParams;
		}
		return null;
	}
}
