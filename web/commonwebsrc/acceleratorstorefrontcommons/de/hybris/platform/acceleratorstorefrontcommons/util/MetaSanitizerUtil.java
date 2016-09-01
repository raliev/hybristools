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

import de.hybris.platform.catalog.model.KeywordModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;


/**
 *
 * Utility class for sanitizing up content that will appear in HTML meta tags.
 *
 */
public final class MetaSanitizerUtil
{

	private MetaSanitizerUtil()
	{
		//Utility classes, which are a collection of static members, are not meant to be instantiated
	}

	/**
	 * Takes a List of KeywordModels and returns a comma separated list of keywords as String.
	 *
	 * @param keywords
	 *           List of KeywordModel objects
	 * @deprecated use {@link MetaSanitizerUtil#sanitizeKeywords(Collection)} instead
	 * @return String of comma separated keywords
	 */
	@Deprecated
	public static String sanitizeKeywords(final List<KeywordModel> keywords)
	{
		if (keywords != null && !keywords.isEmpty())
		{
			// Remove duplicates
			final Set<String> keywordSet = new HashSet<String>(keywords.size());
			for (final KeywordModel keyword : keywords)
			{
				keywordSet.add(keyword.getKeyword());
			}

			return sanitizeKeywords(keywordSet);
		}
		return "";
	}

	/**
	 * Takes a List of keyword Strings and returns a comma separated list of keywords as String.
	 *
	 * @param keywords
	 *           List of KeywordModel objects
	 * @return String of comma separated keywords
	 */
	public static String sanitizeKeywords(final Collection<String> keywords)
	{
		if (keywords != null && !keywords.isEmpty())
		{
			// Remove duplicates
			final Set<String> keywordSet = new HashSet<String>(keywords);

			// Format keywords, join with comma
			final StringBuilder stringBuilder = new StringBuilder();
			for (final String keyword : keywordSet)
			{
				stringBuilder.append(keyword).append(',');
			}
			if (stringBuilder.length() > 0)
			{
				// Remove last comma
				return stringBuilder.substring(0, stringBuilder.length() - 1);
			}
		}
		return "";
	}

	/**
	 * Takes a string of words, removes duplicates and returns a comma separated list of keywords as a String
	 *
	 * @param keywords
	 *           Keywords to be sanitized
	 * @return String of comma separated keywords
	 */
	public static String sanitizeKeywords(final String keywords)
	{
		final String clean = StringUtils.isNotEmpty(keywords) ? Jsoup.parse(keywords).text() : ""; // Clean html
		final String[] cleaned = StringUtils.split(clean.replace("\"", "")); // Clean quotes

		// Remove duplicates
		String noDupes = "";
		for (final String word : cleaned)
		{
			if (!noDupes.contains(word))
			{
				noDupes += word + ",";
			}
		}
		if (!noDupes.isEmpty())
		{
			noDupes = noDupes.substring(0, noDupes.length() - 1);
		}
		return noDupes;
	}

	/**
	 * Removes all HTML tags and double quotes and returns a String
	 *
	 * @param description
	 *           Description to be sanitized
	 * @return String object
	 */
	public static String sanitizeDescription(final String description)
	{
		if (StringUtils.isNotEmpty(description))
		{
			final String clean = Jsoup.parse(description).text();
			return clean.replace("\"", "");
		}
		else
		{
			return "";
		}
	}
}
