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

import com.epam.FlexibleSearchDTO;
import com.epam.exception.EValidationError;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.internal.model.impl.AbstractModelService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.TypeService;

import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.Resource;

import de.hybris.platform.servicelayer.user.UserService;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Sample Controller
 */
@Controller
@RequestMapping(value = "/flexiblesearch")
public class FlexibleSearchToolController
{

	private static final Logger LOG = Logger.getLogger(FlexibleSearchToolController.class);

	@Resource(name = "modelService")
	private ModelService modelService;

	@Resource(name = "typeService")
	private TypeService typeService;

	@Resource(name = "catalogService")
	private CatalogService catalogService;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "i18NService")
	private I18NService i18NService;

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "catalogVersionService")
	private CatalogVersionService catalogVersionService;

	@Resource
	private ConfigurationService configurationService;

	@Resource (name = "flexibleSearchService")
	private FlexibleSearchService flexibleSearchService;

	@RequestMapping(value = "/execute", method = RequestMethod.GET)
	@ResponseBody
	public String executeFlexibleSearch(
				@RequestParam(value="query", required = false) String query,
				@RequestParam(value="itemtype", required = false) final String itemtype,
				@RequestParam(value="fields", required = true) final String fields,
				@RequestParam(value="language", required = false, defaultValue = "en") final String language,
				@RequestParam(value="catalogName", required = false, defaultValue = "")  String catalogName,
				@RequestParam(value="catalogVersion", required = false, defaultValue = "")  String catalogVersion,
				@RequestParam(value="outputFormat", required = false, defaultValue = "TSV") final String outputFormat,
				@RequestParam(value="user", required = false) final String userId,
				@RequestParam(value="debug", required = false, defaultValue = "false") final boolean debug

				) throws EValidationError {

		LOG.setLevel(Level.INFO);
		if (debug) { LOG.setLevel(Level.DEBUG); }

		if (catalogName.equals("")) { catalogName = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.name");
			LOG.debug("setting up catalogName="+catalogName+", from the conf file");
		}
		if (catalogVersion.equals("")) { catalogVersion = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.version");
			LOG.debug("setting up catalogVersion="+catalogVersion+", from the conf file");
		}

		if (query!=null && query.equals("") && (itemtype==null || itemtype.equals(""))) { throw new EValidationError("neither query or itemtype is specified"); }
		if (query == null || query.equals("") && (itemtype != null && !itemtype.equals(""))) { query = "select {pk} from {"+itemtype+"}"; }


		String typeName = extractTypeFromFS(query);
		LOG.debug("typeName was extracted from query, "+typeName);
		List<String> attributes = getAllAttributes(getComposedTypeModel(typeName));
		List<String> fieldList = verifyFieldsAndReturnTheListOfThem(fields, attributes);
		LOG.debug("setting up the session ("+language+", "+catalogName+", "+catalogVersion);
		prepareSession(
				language,
				catalogName,
				catalogVersion
		);

		if (userId!=null && !userId.equals("")) {
			LOG.debug("looking up for the user "+userId+"...");
			UserModel userModel = userService.getUserForUID(userId);
			LOG.debug("setting up the current user, "+userId+"...");
			userService.setCurrentUser(userModel);
		}
		LOG.debug("query = "+query);
		FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
		SearchResult<ItemModel> searchResult = flexibleSearchService.search(flexibleSearchQuery);
		List<ItemModel> resultList = searchResult.getResult();
		Iterator<ItemModel> iter = resultList.iterator();
		StringBuilder resultStr = new StringBuilder();
		resultStr.append(String.join("\t", fieldList));
		resultStr.append("\n");
		while (iter.hasNext()) {
			ItemModel data = iter.next();
			ArrayList<String> values = buildValues(fieldList, data);
			resultStr.append(String.join("\t", values));
			resultStr.append("\n");
		}
		return resultStr.toString();
	}

	private ArrayList<String> buildValues(List<String> fieldList, ItemModel data) {
		ArrayList<String> values = new ArrayList<>();
		for (String field : fieldList) {
			String v = modelService.getAttributeValue(data, field);
            values.add(v);
        }
		return values;
	}

	private List<String> verifyFieldsAndReturnTheListOfThem(String fields, List<String> attributes) throws EValidationError {

		List<String> fieldList = Arrays.asList(fields.split(","));
		List<String> resultingSetOfFields = new ArrayList<>();
		for (String field : fieldList)
		{
			if (attributes.indexOf(field) == -1)
				{  throw new EValidationError("Field list has an wrong item (not present in the item type attributes)"); }
			else
			{	resultingSetOfFields.add(field); }
		}
		return resultingSetOfFields;
	}

	private List<String> getAllAttributes(ComposedTypeModel composedTypeModel) {
		final Set<AttributeDescriptorModel> attributesDescriptors = typeService.getAttributeDescriptorsForType(composedTypeModel);
		List<String> result = new ArrayList<>();
		for (AttributeDescriptorModel adm : attributesDescriptors)
		{
			result.add(adm.getQualifier());
		}
		return result;
	}

	private Class getModelClass(String typeName) {
		ComposedTypeModel type = getComposedTypeModel(typeName);
		Class classOfType = typeService.getModelClass(type);
		return classOfType;
	}

	private ComposedTypeModel getComposedTypeModel(String typeName) {
		final ComposedTypeModel type = typeService.getComposedTypeForCode(typeName);
		return type;
	}

	private String extractTypeFromFS(final String query) {
		String s = query.substring(query.indexOf("from")+"from".length());
		s = s.substring(s.indexOf('{')+1);
		s = s.substring(0, s.indexOf('}'));
		return s;
	}

	private void prepareSession(String lang, String catalogName, String catalogVersion) {
		CatalogVersionModel catalogVersionModel = catalogVersionService.getCatalogVersion(catalogName, catalogVersion);
		CatalogModel catalogModel = catalogService.getCatalogForId(catalogName);
		LanguageModel languageModel = i18NService.getLanguage(lang);
		Collection<CatalogVersionModel> catalogVersions = new ArrayList<CatalogVersionModel>();
		catalogVersions.add(catalogVersionModel);

		sessionService.setAttribute("currentCatalogVersion", catalogVersionModel);
		sessionService.setAttribute("catalogversions", catalogVersions);
		sessionService.setAttribute("language", languageModel);
		sessionService.setAttribute("locale", new Locale(lang));

	}

}
