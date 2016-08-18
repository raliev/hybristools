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
import com.epam.controllers.helpers.CSVPrint;
import com.epam.controllers.helpers.FlexibleSearchFormatter;
import com.epam.exception.EValidationError;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.HybrisEnumValue;
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
	private int queryMaxResults;
	private String queryOutputFormat;

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
	private CharSequence delimiter;

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
				@RequestParam(value="maxResults", required = false, defaultValue = "") final int maxResults,
				@RequestParam(value="ref", required = false) final String ref,
				@RequestParam(value="beautify", required = false) final boolean beautify
	) throws EValidationError {

		LOG.setLevel(Level.INFO);
		if (debug) { LOG.setLevel(Level.DEBUG); }

		if (beautify) {
			 return doBeautify(query);
		}

		if (catalogName.equals("")) { catalogName = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.name");
			LOG.debug("setting up catalogName="+catalogName+", from the conf file");
		}
		if (catalogVersion.equals("")) { catalogVersion = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.version");
			LOG.debug("setting up catalogVersion="+catalogVersion+", from the conf file");
		}

		if (query!=null && query.equals("") && (itemtype==null || itemtype.equals(""))) { throw new EValidationError("neither query or itemtype is specified"); }
		if (query == null || query.equals("") && (itemtype != null && !itemtype.equals(""))) { query = "select {pk} from {"+itemtype+"}"; }

		/*
		* ref="Category:code,name" means that all CategoryModel references will be replaced to the (category.code, category name)
		* */
		Map<String, String> modelCodePair = createModelCodePairs(ref);

		currentCatalog = catalogName;
		currentCatalogVersion = catalogVersion;
		currentLanguage = language;
		currentUserId = userId;
		queryMaxResults = maxResults;
		queryOutputFormat = outputFormat;

		List<String> resultStr = flexibleSearchInternal(query, fields, modelCodePair, true);

		String toOut = String.join("\n", resultStr);
		if (queryOutputFormat.equals("CON")) {
				List<List<String>> dataToOut = new ArrayList<>();
			for (String line : resultStr)
				{
					List<String> columns = Arrays.asList(line.split("\t"));
					dataToOut.add(columns);
				}
				toOut = CSVPrint.writeCSV(dataToOut);
		}
		return toOut;
	}

	private String doBeautify(String query) {
		return new FlexibleSearchFormatter().format(query);
	}

	private Map<String, String> createModelCodePairs(@RequestParam(value = "ref", required = false) String ref) throws EValidationError {
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
		return modelCodePair;
	}

	private List<String> flexibleSearchInternal(String query,
												String fields,
												Map<String, String> modelCodePair,
												boolean rootHandler
	) throws EValidationError {
		String typeName = extractTypeFromFS(query);
		LOG.debug("typeName was extracted from query, " + typeName);
		List<String> attributes = getAllAttributes(getComposedTypeModel(typeName));
		removeFromAttributes(attributes, Arrays.asList("allDocuments", "assignedCockpitItemTemplates", "savedValues", "synchronizationSources", "synchronizedCopies", "valueHistory", "classificationIndexString"));

		//List<String> fieldList = verifyFieldsAndReturnTheListOfThem(fields, attributes);
		List<String> fieldList = new ArrayList<>();
		List<String> fieldList2 = new ArrayList();
		if (fields != null && !fields.equals("*")) {
			fieldList2.addAll(Arrays.asList(fields.split(",")));
		}

		if (fields != null && fields.equals("*")) {
			fieldList2.addAll(attributes);
		}
		if (fieldList2.size() == 0) {
			fieldList2.addAll(typeService.getUniqueAttributes(typeName));
		}


		LOG.debug("setting up the session (" + currentLanguage + ", " + currentCatalog + ", " + currentCatalogVersion);
		prepareSession(
				currentLanguage,
				currentCatalog,
				currentCatalogVersion
		);

		if (currentUserId != null && !currentUserId.equals("")) {
			LOG.debug("looking up for the user " + currentUserId + "...");
			UserModel userModel = userService.getUserForUID(currentUserId);
			LOG.debug("setting up the current user, " + currentUserId + "...");
			userService.setCurrentUser(userModel);
		}
		LOG.debug("query = " + query);
		FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
		SearchResult<ItemModel> searchResult = flexibleSearchService.search(flexibleSearchQuery);
		List<ItemModel> resultList = searchResult.getResult();
		Iterator<ItemModel> iter = resultList.iterator();
		List<String> resultStr = new ArrayList();
		if (rootHandler ) {
			if (!queryOutputFormat.equals("BRD"))
				resultStr.add(getStart_delimiter("", true) + String.join(getDelimiter("", true), fieldList2) + getEnd_delimiter(true));
		}
		int counter = 0;

		for (ItemModel data : resultList) {
			ArrayList<String> values = buildValues(fieldList2, data);
			String valuesOfFields = "";
			for (int i = 1; i < fieldList2.size(); i++) {
				valuesOfFields = valuesOfFields + getDelimiter(fieldList2.get(i), rootHandler) + values.get(i);
			}
			resultStr.add(getStart_delimiter(fieldList2.get(0), rootHandler) + values.get(0) + valuesOfFields + getEnd_delimiter(rootHandler));
			counter++;
			if (counter >= queryMaxResults) {
				break;
			}
		}
		List<String> processedResultStr = processModelCodePair(resultStr, modelCodePair);
		return processedResultStr;
	}

	private void removeFromAttributes(List<String> attributes, List<String> attrToDelete) {
		for (String attr : attrToDelete)
		{
			int index = attributes.indexOf(attr);
			if (index >= 0) { attributes.remove(index);}
		}
	}

	private List<String> processModelCodePair(List<String> resultStr, Map<String, String> modelCodePair) throws EValidationError {
		List<String> processedLines = new ArrayList<>();
		for (String line : resultStr)
		{
			while (line.indexOf("Model (") != -1) {
					line = line.replace("()", "");
					int posit = line.indexOf("Model (");
					//int tabindex = line.substring(0, posit).lastIndexOf("\t");
					//int skindex = line.substring(0, posit).lastIndexOf("(");
					//int commaindex = line.substring(0, posit).lastIndexOf(",");
					int max = posit;
					String a;
					while (max >= 0 && Character.isLetter(line.substring(max, max+1).charAt(0)))
					{
						max --;
					}
					max++;
					/*if ((tabindex == -1) && (commaindex == -1) && (skindex  == -1)) {
						max = -1;
					}
					max = (tabindex > commaindex) ? tabindex  : commaindex ;
					max = (skindex > max) ? skindex  : max ;
				    max ++;
					*/
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

	/*
	 * external references that looks like "CategoryModel (1237352321543@12)" are replaced with attributes
	 * */
	private String ResolvePK(String line, String objectName, String pk, Map<String, String> modelCodePair
							 ) throws EValidationError {
		List<String> res = flexibleSearchInternal("select {pk} from {"+objectName+"} where {pk} = \""+pk+"\"",
								modelCodePair.get(objectName),
								modelCodePair,
								false);
		if (res.size()<1) { throw new EValidationError("can't find PK "+pk+" for "+objectName); }
		String textToReplace = objectName+"Model ("+pk+"@";
		int indexToReplace = line.indexOf(textToReplace);
		int lastPositionToReplace = line.indexOf(")", indexToReplace);
		String resolved = res.get(0).toString();
		if (resolved.indexOf("\t") != -1) {
			resolved = resolved.replace("\t", ",");
			resolved = "{" + resolved + "}";
		}
		line = line.substring(0,indexToReplace) + PreprocessForOutputFormat(resolved) + line.substring(lastPositionToReplace+1, line.length());
		return line;
	}

	private String PreprocessForOutputFormat(String resolved) {
		return resolved.replaceAll("(\r\n|\n)", "").replace("\"", "\"\"");
	}

	private ArrayList<String> buildValues(List<String> fieldList, ItemModel data) {
		ArrayList<String> values = new ArrayList<>();
		for (String field : fieldList) {
			Object v = modelService.getAttributeValue(data, field);
			if (v == null) {
				values.add ( "<NULL> ");
			} else
			if (v instanceof ItemModel)
			{
				values.add(  PreprocessForOutputFormat(((ItemModel) v).toString()));
			} else
			if (v instanceof HybrisEnumValue) {
				values.add(  PreprocessForOutputFormat(((HybrisEnumValue) v).toString()));
			} else
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
				values.add("("+PreprocessForOutputFormat(String.join(",", collectionList))+")");
			} else
			{
				values.add(PreprocessForOutputFormat(v.toString()));
			}
        }
		return values;
	}

	private List<String> verifyFieldsAndReturnTheListOfThem(String fields, List<String> attributes) throws EValidationError {
		if (fields == null) { return new ArrayList<>(); }
		if (fields.equals("*")) { return new ArrayList<>(); }
		List<String> fieldList = Arrays.asList(fields.split(","));
		List<String> resultingSetOfFields = new ArrayList<>();
		for (String field : fieldList)
		{
			if (attributes.indexOf(field) == -1 )
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

	public CharSequence getDelimiter(String field, boolean rootHandler) {
		if (!rootHandler) { return ":"; }
		if (queryOutputFormat.equals("TSV")) { return "\t"; }
		if (queryOutputFormat.equals("CSV")) { return "\", \""; }
		if (queryOutputFormat.equals("CON")) { return "\t"; }
		if (queryOutputFormat.equals("BRD")) { return "\n"+ field + (field.equals("")? "" : ": "); }
		return "";
	}


	public String getStart_delimiter(String field, boolean rootHandler) {
		if (!rootHandler) { return ""; }
		if (queryOutputFormat.equals("TSV")) { return ""; }
		if (queryOutputFormat.equals("CSV")) { return "\""; }
		if (queryOutputFormat.equals("CON")) { return ""; }
		if (queryOutputFormat.equals("BRD")) { return "\n\n" +field+ (field.equals("")? "" : ": "); }
		return "";
	}

	public String getEnd_delimiter(boolean rootHandler) {
		if (!rootHandler) { return ""; }
		if (queryOutputFormat.equals("TSV")) { return ""; }
		if (queryOutputFormat.equals("CSV")) { return "\""; }
		if (queryOutputFormat.equals("CON")) { return ""; }
		if (queryOutputFormat.equals("BRD")) { return "\n"; }
		return "";
	}
}