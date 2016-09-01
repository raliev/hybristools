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
package de.hybris.platform.acceleratorstorefrontcommons.breadcrumb;

import java.util.List;


/**
 * ResourceBreadcrumbBuilder builds a list of breadcrumbs based on a resource key
 */
public interface ResourceBreadcrumbBuilder
{
	List<Breadcrumb> getBreadcrumbs(String resourceKey);
}
