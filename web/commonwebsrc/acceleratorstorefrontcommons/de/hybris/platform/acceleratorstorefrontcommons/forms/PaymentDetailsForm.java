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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


/**
 */
public class PaymentDetailsForm
{
	private String paymentId;
	private String cardTypeCode;
	private String nameOnCard;
	private String cardNumber;
	private String startMonth;
	private String startYear;
	private String expiryMonth;
	private String expiryYear;
	private String issueNumber;

	private Boolean saveInAccount;

	private Boolean newBillingAddress;

	private AddressForm billingAddress;


	public String getPaymentId()
	{
		return paymentId;
	}

	public void setPaymentId(final String paymentId)
	{
		this.paymentId = paymentId;
	}

	@NotNull(message = "{payment.cardType.invalid}")
	@Size(min = 1, max = 255, message = "{payment.cardType.invalid}")
	public String getCardTypeCode()
	{
		return cardTypeCode;
	}

	public void setCardTypeCode(final String cardTypeCode)
	{
		this.cardTypeCode = cardTypeCode;
	}

	@NotNull(message = "{payment.nameOnCard.invalid}")
	@Size(min = 1, max = 255, message = "{payment.nameOnCard.invalid}")
	public String getNameOnCard()
	{
		return nameOnCard;
	}

	public void setNameOnCard(final String nameOnCard)
	{
		this.nameOnCard = nameOnCard;
	}

	@NotNull(message = "{payment.cardNumber.invalid}")
	@Size(min = 16, max = 16, message = "{payment.cardNumber.invalid}")
	@Pattern(regexp = "(^$|^?\\d*$)", message = "{payment.cardNumber.invalid}")
	public String getCardNumber()
	{
		return cardNumber;
	}

	public void setCardNumber(final String cardNumber)
	{
		this.cardNumber = cardNumber;
	}

	public String getStartMonth()
	{
		return startMonth;
	}

	public void setStartMonth(final String startMonth)
	{
		this.startMonth = startMonth;
	}

	public String getStartYear()
	{
		return startYear;
	}

	public void setStartYear(final String startYear)
	{
		this.startYear = startYear;
	}

	@NotNull(message = "{payment.expiryMonth.invalid}")
	@Size(min = 1, max = 2, message = "{payment.expiryMonth.invalid}")
	public String getExpiryMonth()
	{
		return expiryMonth;
	}

	public void setExpiryMonth(final String expiryMonth)
	{
		this.expiryMonth = expiryMonth;
	}

	@NotNull(message = "{payment.expiryYear.invalid}")
	@Size(min = 2, max = 4, message = "{payment.expiryYear.invalid}")
	public String getExpiryYear()
	{
		return expiryYear;
	}

	public void setExpiryYear(final String expiryYear)
	{
		this.expiryYear = expiryYear;
	}

	@Pattern(regexp = "(^$|^?\\d*$)", message = "{payment.issueNumber.invalid}")
	@Size(min = 0, max = 16, message = "{payment.issueNumber.toolong}")
	public String getIssueNumber()
	{
		return issueNumber;
	}

	public void setIssueNumber(final String issueNumber)
	{
		this.issueNumber = issueNumber;
	}

	public Boolean getSaveInAccount()
	{
		return saveInAccount;
	}

	public void setSaveInAccount(final Boolean saveInAccount)
	{
		this.saveInAccount = saveInAccount;
	}

	public Boolean getNewBillingAddress()
	{
		return newBillingAddress;
	}

	public void setNewBillingAddress(final Boolean newBillingAddress)
	{
		this.newBillingAddress = newBillingAddress;
	}

	//	@Valid
	public AddressForm getBillingAddress()
	{
		return billingAddress;
	}

	public void setBillingAddress(final AddressForm billingAddress)
	{
		this.billingAddress = billingAddress;
	}

}
