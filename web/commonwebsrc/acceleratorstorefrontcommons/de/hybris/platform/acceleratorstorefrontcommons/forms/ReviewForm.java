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

/**
 * Form object for writing reviews
 */
public class ReviewForm
{
	private String headline;
	private String comment;
	private Double rating;
	private String alias;

	/**
	 * @return the headline
	 */
	public String getHeadline()
	{
		return headline;
	}

	/**
	 * @param headline
	 *           the headline to set
	 */
	public void setHeadline(final String headline)
	{
		this.headline = headline;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment
	 *           the comment to set
	 */
	public void setComment(final String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the rating
	 */
	public Double getRating()
	{
		return rating;
	}

	/**
	 * @param rating
	 *           the rating to set
	 */
	public void setRating(final Double rating)
	{
		this.rating = rating;
	}

	public String getAlias()
	{
		return alias;
	}

	public void setAlias(final String alias)
	{
		this.alias = alias;
	}
}
