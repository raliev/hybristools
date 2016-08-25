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
 *
 */
package com.epam.controllers;

import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.exception.EValidationError;
import com.epam.services.FlexibleSearchToolService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import org.apache.log4j.Logger;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Sample Controller
 */
@Controller
@RequestMapping(value = "/flexiblesearch")
public class FlexibleSearchToolController
{
	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource (name = "flexibleSearchToolService")
	private FlexibleSearchToolService flexibleSearchToolService;


	private static final Logger LOG = Logger.getLogger(FlexibleSearchToolController.class);

	private String currentLanguage;
	private String currentCatalog;
	private String currentCatalogVersion;
	private String currentUserId;
	private int queryMaxResults;
	private String queryOutputFormat;




	@RequestMapping(value = "/execute", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public String executeFlexibleSearch(
				@RequestParam(value="query", required = false) String query,
				@RequestParam(value="itemtype", required = false) final String itemtype,
				@RequestParam(value="fields", required = false) final String fields,
				@RequestParam(value="language", required = false, defaultValue = "en") final String language,
				@RequestParam(value="catalogName", required = false, defaultValue = "")  String catalogName,
				@RequestParam(value="catalogVersion", required = false, defaultValue = "")  String catalogVersion,
				@RequestParam(value="outputFormat", required = false, defaultValue = "TSV") final String outputFormat,
				@RequestParam(value="user", required = false) final String userId,
				@RequestParam(value="debug", required = false, defaultValue = "false") final boolean debug,
				@RequestParam(value="maxResults", required = false, defaultValue = "100000") final int maxResults,
				@RequestParam(value="ref", required = false) final String ref,
				@RequestParam(value="beautify", required = false) final boolean beautify,
				@RequestParam(value="resultTypes", required = false) final String resultTypes,
				@RequestParam(value="pk", required = false) final String pk
				) throws EValidationError {

		if (beautify) { return flexibleSearchToolService.doBeautify(query);}

		FlexibleSearchToolConfiguration flexibleSearchToolConfiguration = new FlexibleSearchToolConfiguration();
		flexibleSearchToolConfiguration.setQuery(query);
		flexibleSearchToolConfiguration.setItemtype(itemtype);
		flexibleSearchToolConfiguration.setFields(fields);
		flexibleSearchToolConfiguration.setLanguage(language);
		flexibleSearchToolConfiguration.setCatalogName(catalogName);
		flexibleSearchToolConfiguration.setCatalogVersion(catalogVersion);
		flexibleSearchToolConfiguration.setOutputFormat(outputFormat);
		flexibleSearchToolConfiguration.setUser(userId);
		flexibleSearchToolConfiguration.setDebug(debug);
		flexibleSearchToolConfiguration.setMaxResults(maxResults);
		flexibleSearchToolConfiguration.setRef(ref);
		flexibleSearchToolConfiguration.setBeautify(beautify);
		flexibleSearchToolConfiguration.setPk(pk);
		flexibleSearchToolConfiguration.mergeWithDefaults(configurationService.getConfiguration());
		flexibleSearchToolConfiguration.processParams();
		flexibleSearchToolConfiguration.setConfigurableResultClassListFromStr(resultTypes);
		flexibleSearchToolConfiguration.validation();
		String result = "";
		try {
			result = flexibleSearchToolService.execute(flexibleSearchToolConfiguration);
		} catch (Exception e)
		{
			return e.getMessage();
		}
		return result;
	}




}