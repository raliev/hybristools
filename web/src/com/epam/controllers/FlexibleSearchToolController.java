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
import de.hybris.platform.core.model.media.MediaModel;
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

	private String currentLanguage;
	private String currentCatalog;
	private String currentCatalogVersion;
	private String currentUserId;

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
				@RequestParam(value="debug", required = false, defaultValue = "false") final boolean debug,
				@RequestParam(value="ref", required = false) final String ref
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

		Map<String, String> modelCodePair = new HashMap<String, String>();
		if (ref != null) {
			List<String> refArray = Arrays.asList(ref.split(" "));
			for (String el : refArray) {
				List<String> modelCodePairEl = Arrays.asList(el.split(":"));
				if (modelCodePairEl.size() != 2) {
					throw new EValidationError("bad syntax of ref: " + el);
				}
				LOG.debug("ref: "+modelCodePairEl.get(0) + "=>" + modelCodePairEl.get(1));
				modelCodePair.put(modelCodePairEl.get(0), modelCodePairEl.get(1));
			}
		}

		currentCatalog = catalogName;
		currentCatalogVersion = catalogVersion;
		currentLanguage = language;
		currentUserId = userId;

		List<String> resultStr = flexibleSearchInternal(query, fields, modelCodePair);

		return String.join("\n", resultStr);
	}

	private List<String> flexibleSearchInternal(String query,
												String fields,
												Map<String, String> modelCodePair
	) throws EValidationError {
		String typeName = extractTypeFromFS(query);
		LOG.debug("typeName was extracted from query, "+typeName);
		List<String> attributes = getAllAttributes(getComposedTypeModel(typeName));
		List<String> fieldList = verifyFieldsAndReturnTheListOfThem(fields, attributes);
		LOG.debug("setting up the session ("+currentLanguage+", "+currentCatalog+", "+currentCatalogVersion);
		prepareSession(
				currentLanguage,
				currentCatalog,
				currentCatalogVersion
		);

		if (currentUserId!=null && !currentUserId.equals("")) {
			LOG.debug("looking up for the user "+currentUserId+"...");
			UserModel userModel = userService.getUserForUID(currentUserId);
			LOG.debug("setting up the current user, "+currentUserId+"...");
			userService.setCurrentUser(userModel);
		}
		LOG.debug("query = "+query);
		FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
		SearchResult<ItemModel> searchResult = flexibleSearchService.search(flexibleSearchQuery);
		List<ItemModel> resultList = searchResult.getResult();
		Iterator<ItemModel> iter = resultList.iterator();
		List<String> resultStr = new ArrayList();
		resultStr.add(String.join("\t", fieldList));
		for (ItemModel data : resultList) {
			ArrayList<String> values = buildValues(fieldList, data);
			resultStr.add(String.join("\t",  values));
		}
		List<String> processedResultStr = processModelCodePair(resultStr, modelCodePair);
		return processedResultStr;
	}

	private List<String> processModelCodePair(List<String> resultStr, Map<String, String> modelCodePair) throws EValidationError {
		List<String> processedLines = new ArrayList<>();
		for (String line : resultStr)
		{
			while (line.indexOf("Model (") != -1) {
					int posit = line.indexOf("Model (");
					int tabindex = line.substring(0, posit).lastIndexOf("(");
					int commaindex = line.substring(0, posit).lastIndexOf(",");
					if ((tabindex == -1) && (commaindex == 1)) {
						throw new EValidationError("something strange with the data. neither '(' or ',' not found before 'Model ('");
					}
					int max = (tabindex > commaindex) ? tabindex + 1 : commaindex + 1;
					String objectNameModel = line.substring(max, posit + "Model (".length() - 2);
					String objectName = line.substring(max, posit);
					String PK = line.substring(posit + "Model (".length(), line.length()).substring(0, 13);
					line = ResolvePK(line, objectName, PK, modelCodePair);
					System.out.println("[" + objectName + "]PK:[" + PK + "]");
			}
			processedLines.add(line);
		}
		return processedLines;
	}

	private String ResolvePK(String line, String objectName, String pk, Map<String, String> modelCodePair
							 ) throws EValidationError {
		List<String> res = flexibleSearchInternal("select {pk} from {"+objectName+"} where {pk} = \""+pk+"\"",
								modelCodePair.get(objectName),
								modelCodePair);
		if (res.size()<2) { throw new EValidationError("can't find PK "+pk+" for "+objectName); }
		String textToReplace = objectName+"Model ("+pk+"@";
		int indexToReplace = line.indexOf(textToReplace);
		int lastPositionToReplace = line.indexOf(")", indexToReplace);
		String resolved = res.get(1).toString();
		if (resolved.indexOf("\t") != -1) {
			resolved = resolved.replace("\t", ",");
			resolved = "{" + resolved + "}";
		}
		line = line.substring(0,indexToReplace) + resolved + line.substring(lastPositionToReplace+1, line.length());
		return line;
	}

	private ArrayList<String> buildValues(List<String> fieldList, ItemModel data) {
		ArrayList<String> values = new ArrayList<>();
		for (String field : fieldList) {
			Object v = modelService.getAttributeValue(data, field);
			if (v instanceof ItemModel)
			{
				values.add(((ItemModel) v).toString());
			}
			if (v instanceof Collection)
			{
				List<String> collectionList = new ArrayList<>();
				for (Object el : (Collection) v)
				{
					if (el instanceof ItemModel)
					{
						collectionList.add(el.toString());
					}
					if (el instanceof java.lang.String) {
						collectionList.add(el.toString());
					}
				}
				values.add("("+String.join(",", collectionList)+")");
			}
			if (v instanceof java.lang.String) {
				values.add(v.toString());
			}
        }
		return values;
	}

	private List<String> verifyFieldsAndReturnTheListOfThem(String fields, List<String> attributes) throws EValidationError {
		if (fields == null) { return new ArrayList<>(); }
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
