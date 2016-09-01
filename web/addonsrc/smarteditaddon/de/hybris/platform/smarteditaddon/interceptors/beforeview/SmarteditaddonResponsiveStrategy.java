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
package de.hybris.platform.smarteditaddon.interceptors.beforeview;

/**
 * Strategy to determine if the storefront is responsive or non responsive.
 */
public interface SmarteditaddonResponsiveStrategy
{
	/**
	 * Indicate if the storefront is responsive (true) or not (false).
	 *
	 * @return Boolean representing the responsiveness of the storefront
	 */
	Boolean isResponsive();
}
