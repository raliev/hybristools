package com.epam.services;


import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.exception.EValidationError;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.AbstractLazyLoadMultiColumnList;
import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.jalo.Item;
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
import java.io.Serializable;
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

        List<String> resultStr = flexibleSearchInternal(
                flexibleSearchToolConfiguration,
                ROOT_HANDLER);

        String toOut = String.join("\n", resultStr);
        if (flexibleSearchToolConfiguration.getOutputFormat().equals("CON")) {
            List<List<String>> dataToOut = new ArrayList<>();
            for (String line : resultStr)
            {
                List<String> columns = new ArrayList(Arrays.asList(line.split("\t")));
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

        //List<String> fieldList = verifyFieldsAndReturnTheListOfThem(fields, attributes);
        List<String> fieldList = new ArrayList<>();

        List<String> fieldList2 = buildFieldListArray(flexibleSearchToolConfiguration, typeName, attributes);

        if (!StringUtils.isEmpty(flexibleSearchToolConfiguration.getUser())) {
            UserModel userModel = userService.getUserForUID(flexibleSearchToolConfiguration.getUser());
            userService.setCurrentUser(userModel);
        }

        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(flexibleSearchToolConfiguration.getQuery());
  //      if (rootHandler) {
            flexibleSearchQuery.setResultClassList(
                    flexibleSearchToolConfiguration.getConfigurableResultClassList()
            );
            /*
        } else
        {
            flexibleSearchQuery.setResultClassList(Collections.singletonList(Item.class));
        }
*/
        SearchResult<List<Object>> searchResult = flexibleSearchService .search(flexibleSearchQuery);

        //List<Object> resultList = searchResult.getResult();
        //Iterator<Object> iter = resultList.iterator();
        List<String> resultStr = new ArrayList();
        if (rootHandler ) {
            if (!flexibleSearchToolConfiguration.getOutputFormat().equals("BRD"))
                resultStr.add(  flexibleSearchToolConfiguration.getStart_delimiter("", true) +
                        String.join(
                                flexibleSearchToolConfiguration.getDelimiter("", true), fieldList2) +
                                flexibleSearchToolConfiguration.getEnd_delimiter(true));
        }
        int counter = 0;

        for (List<Object> data : searchResult.getResult()) {

                Object obj;
                int size = 0;
                 { obj = (ItemModel) ((List<Object>) data).get(0); size = ((List<Object>) data).size(); }
                //{ obj = (ItemModel) data; }

            ArrayList<String> values = buildValues(fieldList2, (ItemModel) obj);
                    //ArrayList<String> values = buildValues(fieldList2, (ItemModel) obj);



            List<String> extra_fields = new ArrayList<>();
            String valuesOfFields = "";
            for (int i = 1; i < fieldList2.size(); i++) {
                valuesOfFields = valuesOfFields +
                        flexibleSearchToolConfiguration.getDelimiter(fieldList2.get(i), rootHandler) + values.get(i);
            }
            if (rootHandler) {
                    for (int i = 1; i < size; i++) {
                        extra_fields.add(((List<Object>) data).get(i).toString());
                    }
                }

            String ext_f = String.join(
                    flexibleSearchToolConfiguration.getDelimiter("extra", rootHandler),
                    extra_fields);
            resultStr.add(
                        flexibleSearchToolConfiguration.getStart_delimiter(fieldList2.get(0), rootHandler) + values.get(0) +
                                valuesOfFields +
                                (ext_f.equals("") ? "" : flexibleSearchToolConfiguration.getDelimiter("extra", rootHandler)) +
                                ext_f +
                                flexibleSearchToolConfiguration.getEnd_delimiter(rootHandler));


            counter++;
            if (counter >= flexibleSearchToolConfiguration.getMaxResults()) {
                break;
            }
        }
        List<String> processedResultStr = processModelCodePair(flexibleSearchToolConfiguration, resultStr);
        return processedResultStr;
        //return resultStr;
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
            String pairs = flexibleSearchToolConfiguration.getModelCodePair().get(typeName);
            if (pairs != null) {
                List<String> pairList = Arrays.asList(pairs.split(","));
                fieldList2.addAll(pairList);
            } else {
                fieldList2.addAll(typeService.getUniqueAttributes(typeName));
            }

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

                String objectName = line.substring(max, posit);
                String PK = line.substring(posit + "Model (".length(), line.length()).substring(0, 13);
                line = ResolvePK(flexibleSearchToolConfiguration, line, objectName, PK);
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
        newFSC.setQuery("select {pk},1 from {"+objectName+"} where {pk} = '" + pk + "'");
        newFSC.setItemtype("");
        newFSC.setPk("");
        newFSC.setModelCodePair(flexibleSearchToolConfiguration.getModelCodePair());
        newFSC.setFields(flexibleSearchToolConfiguration.getModelCodePair().get(objectName));
        newFSC.mergeWithDefaults(configurationService.getConfiguration());
        newFSC.setConfigurableResultClassList(Arrays.asList(Item.class,String.class));
        newFSC.processParams();
        newFSC.validation();
        newFSC.setQuery("select {pk},1 from {"+objectName+"} where {pk} = '" + pk + "'");

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
        removeFromAttributes(result,
                Arrays.asList(
                        "allDocuments",
                        "assignedCockpitItemTemplates",
                        "savedValues",
                        "synchronizationSources",
                        "synchronizedCopies",
                        "valueHistory",
                        "classificationIndexString"));
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

    public String doBeautify(String query) {
        return new FlexibleSearchFormatter().format(query);

    }

    public void prepareSession(FlexibleSearchToolConfiguration configuration) {
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
