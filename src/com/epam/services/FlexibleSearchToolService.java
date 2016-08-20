package com.epam.services;


import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.exception.EValidationError;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import com.epam.helpers.FlexibleSearchFormatter;
import com.epam.helpers.CSVPrint;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by Rauf_Aliev on 8/19/2016.
 */

public class FlexibleSearchToolService {

    private static final Logger LOG = Logger.getLogger(FlexibleSearchToolService.class);


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

    @Resource (name = "flexibleSearchService")
    private FlexibleSearchService flexibleSearchService;
    private CharSequence delimiter;

    @Resource
    private ConfigurationService configurationService;

    private final boolean ROOT_HANDLER = true;

    public String execute(FlexibleSearchToolConfiguration flexibleSearchToolConfiguration) throws EValidationError {

        prepareSession(flexibleSearchToolConfiguration);

        //LOG.setLevel(Level.INFO);
        //if (debug) { LOG.setLevel(Level.DEBUG); }

        if (flexibleSearchToolConfiguration.getBeautify()) return doBeautify(flexibleSearchToolConfiguration.getQuery());

        List<String> resultStr = flexibleSearchInternal(
                flexibleSearchToolConfiguration,
                ROOT_HANDLER);

        String toOut = String.join("\n", resultStr);
        if (flexibleSearchToolConfiguration.getOutputFormat().equals("CON")) {
            List<List<String>> dataToOut = new ArrayList<>();
            for (String line : resultStr)
            {
                List<String> columns = Arrays.asList(line.split("\t"));
                dataToOut.add(columns);
            }
            toOut = CSVPrint.writeCSV(dataToOut, false);
        }

        return toOut;
    }


    private List<String> flexibleSearchInternal(FlexibleSearchToolConfiguration flexibleSearchToolConfiguration,
                                                boolean rootHandler
    ) throws EValidationError {

        String typeName = extractTypeFromFS(flexibleSearchToolConfiguration.getQuery());
        LOG.debug("typeName was extracted from query, " + typeName);
        List<String> attributes = getAllAttributes(getComposedTypeModel(typeName));

        removeFromAttributes(attributes,
                Arrays.asList(
                            "allDocuments",
                            "assignedCockpitItemTemplates",
                            "savedValues",
                            "synchronizationSources",
                            "synchronizedCopies",
                            "valueHistory",
                            "classificationIndexString"));

        //List<String> fieldList = verifyFieldsAndReturnTheListOfThem(fields, attributes);
        List<String> fieldList = new ArrayList<>();

        List<String> fieldList2 = buildFieldListArray(flexibleSearchToolConfiguration, typeName, attributes);

        if (!StringUtils.isEmpty(flexibleSearchToolConfiguration.getUser())) {
            UserModel userModel = userService.getUserForUID(flexibleSearchToolConfiguration.getUser());
            userService.setCurrentUser(userModel);
        }

        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(flexibleSearchToolConfiguration.getQuery());
        SearchResult<ItemModel> searchResult = flexibleSearchService.search(flexibleSearchQuery);

        List<ItemModel> resultList = searchResult.getResult();
        Iterator<ItemModel> iter = resultList.iterator();
        List<String> resultStr = new ArrayList();
        if (rootHandler ) {
            if (!flexibleSearchToolConfiguration.getOutputFormat().equals("BRD"))
                resultStr.add(  flexibleSearchToolConfiguration.getStart_delimiter("", true) +
                        String.join(
                                flexibleSearchToolConfiguration.getDelimiter("", true), fieldList2) +
                                flexibleSearchToolConfiguration.getEnd_delimiter(true));
        }
        int counter = 0;

        for (ItemModel data : resultList) {
            ArrayList<String> values = buildValues(fieldList2, data);
            String valuesOfFields = "";
            for (int i = 1; i < fieldList2.size(); i++) {
                valuesOfFields = valuesOfFields +
                            flexibleSearchToolConfiguration.getDelimiter(fieldList2.get(i), rootHandler) + values.get(i);
            }
            resultStr.add(
                            flexibleSearchToolConfiguration.getStart_delimiter(fieldList2.get(0), rootHandler) + values.get(0) +
                            valuesOfFields +
                            flexibleSearchToolConfiguration.getEnd_delimiter(rootHandler));
            counter++;
            if (counter >= flexibleSearchToolConfiguration.getMaxResults()) {
                break;
            }
        }
        List<String> processedResultStr = processModelCodePair(flexibleSearchToolConfiguration, resultStr);
        return processedResultStr;
    }

    private List<String> buildFieldListArray(FlexibleSearchToolConfiguration flexibleSearchToolConfiguration, String typeName, List<String> attributes) {
        List<String> fieldList2 = new ArrayList();
        if ( flexibleSearchToolConfiguration.getFields() != null &&
            !flexibleSearchToolConfiguration.getFields().equals("*")) {
            fieldList2.addAll(Arrays.asList(flexibleSearchToolConfiguration.getFields().split(",")));
        }

        if (flexibleSearchToolConfiguration.getFields() != null
                &&
            flexibleSearchToolConfiguration.getFields().equals("*")) {
            fieldList2.addAll(attributes);
        }

        if (fieldList2.size() == 0) {
            fieldList2.addAll(typeService.getUniqueAttributes(typeName));
        }
        return fieldList2;
    }

    private void removeFromAttributes(List<String> attributes, List<String> attrToDelete) {
        for (String attr : attrToDelete)
        {
            int index = attributes.indexOf(attr);
            if (index >= 0) { attributes.remove(index);}
        }
    }

    private List<String> processModelCodePair(FlexibleSearchToolConfiguration flexibleSearchToolConfiguration, List<String> resultStr) throws EValidationError {
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
                line = ResolvePK(flexibleSearchToolConfiguration, line, objectName, PK);
                //System.out.println("[" + objectName + "]PK:[" + PK + "]");
            }
            processedLines.add(line);
        }
        return processedLines;
    }

    /*
     * external references that looks like "CategoryModel (1237352321543@12)" are replaced with attributes
     * */
    private String ResolvePK(
            FlexibleSearchToolConfiguration flexibleSearchToolConfiguration,
            String line, String objectName, String pk
    ) throws EValidationError {


        FlexibleSearchToolConfiguration newFSC = flexibleSearchToolConfiguration.createAClone();
        newFSC.setQuery("select {pk} from {"+objectName+"} where {pk} = \""+pk+"\"");
        newFSC.setItemtype("");
        newFSC.setPk("");
        newFSC.setModelCodePair(flexibleSearchToolConfiguration.getModelCodePair());
        newFSC.setFields(flexibleSearchToolConfiguration.getModelCodePair().get(objectName));
        newFSC.mergeWithDefaults(configurationService.getConfiguration());
        newFSC.processParams();
        newFSC.validation();
        newFSC.setQuery("select {pk} from {"+objectName+"} where {pk} = \""+pk+"\"");

        List<String> res = flexibleSearchInternal(newFSC, !ROOT_HANDLER);
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
        String s = query.substring(query.toLowerCase().indexOf("from")+"from".length());
        s = s.substring(s.indexOf('{')+1);
        s = s.substring(0, s.indexOf('}'));
        return s;
    }






    private String doBeautify(String query) {
        return new FlexibleSearchFormatter().format(query);

    }


    private void prepareSession(FlexibleSearchToolConfiguration configuration) {
        CatalogVersionModel catalogVersionModel = catalogVersionService.getCatalogVersion(configuration.getCatalogName(), configuration.getCatalogVersion());
        CatalogModel catalogModel = catalogService.getCatalogForId(configuration.getCatalogName());
        LanguageModel languageModel = i18NService.getLanguage(configuration.getLanguage());
        Collection<CatalogVersionModel> catalogVersions = new ArrayList<CatalogVersionModel>();
        catalogVersions.add(catalogVersionModel);

        sessionService.setAttribute("currentCatalogVersion", catalogVersionModel);
        sessionService.setAttribute("catalogversions", catalogVersions);
        sessionService.setAttribute("language", languageModel);
        sessionService.setAttribute("locale", new Locale(configuration.getLanguage()));

    }

}
