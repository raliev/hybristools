package com.epam.configuration;

import com.epam.exception.EValidationError;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.hybris.platform.core.PK;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.util.Utilities;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Rauf_Aliev on 8/19/2016.
 */
public class FlexibleSearchToolConfiguration {

    private String query;
    private String itemtype;
    private String fields;
    private String language;
    private String catalogName;
    private String catalogVersion;
    private String outputFormat;
    private String user;
    private boolean debug;
    private int maxResults;
    private String ref;
    private Boolean beautify;
    private String pk;

    private Map<String, String> modelCodePair;
    List<Class> configurableResultClassList = new ArrayList(Arrays.asList(Item.class, String.class));

    public Map<String, String> getModelCodePair() {
        return modelCodePair;
    }

    public void setModelCodePair(Map<String, String> modelCodePair) {
        this.modelCodePair = modelCodePair;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getItemtype() {
        return itemtype;
    }

    public void setItemtype(String itemtype) {
        this.itemtype = itemtype;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getRef() {

        return ref;

    }

    public void setRef(String ref) throws EValidationError {
        /*
		* ref="Category:code,name" means that all CategoryModel references will be replaced to the (category.code, category name)
		* */
        modelCodePair = createModelCodePairs(ref);
        this.ref = ref;
    }

    public Boolean getBeautify() {
        return beautify;
    }

    public void setBeautify(Boolean beautify) {
        this.beautify = beautify;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void mergeWithDefaults( org.apache.commons.configuration.Configuration configuration) {
        if (StringUtils.isEmpty(getCatalogName())) {
            setCatalogName(configuration.getString("flexiblesearch.default.catalog.name"));
        }
        if (StringUtils.isEmpty(getCatalogVersion()))
        setCatalogVersion(configuration.getString("flexiblesearch.default.catalog.version"));
        setLanguage(configuration.getString("flexiblesearch.default.language"));
        }

    public void validation() throws EValidationError {
        if (StringUtils.isEmpty(getCatalogName())) throw new EValidationError("Catalog Name is empty. flexiblesearch.default.catalog.name in project.properties or 'catalogName' param is required");
        if (StringUtils.isEmpty(getCatalogVersion())) throw new EValidationError("Catalog Version is empty. flexiblesearch.default.catalog.version in project.properties or 'catalogName' param is required");
        if (StringUtils.isEmpty(getQuery()) && StringUtils.isEmpty(getItemtype()) && StringUtils.isEmpty(getPk())) { throw new EValidationError("neither query or itemtype or Pk is specified"); }
    }

    public void processParams() throws EValidationError {

        if (!StringUtils.isEmpty(getPk())) {
            ifPKparamCreateACustomQuery();
        }
        if (!StringUtils.isEmpty(itemtype)) {
            setQuery("select {pk} from {"+itemtype+"}");
        }
        if (getQuery().toLowerCase().indexOf("from") == -1 ) { throw new EValidationError("incorrect flexible search query"); }

        String st = getQuery().substring(0,getQuery().toLowerCase().indexOf("from"));
        int skCount = Arrays.asList(st.split(",")).size();
        if (skCount == 1) {
            setQuery("select {pk}, {pk} " + getQuery().substring(getQuery().toLowerCase().indexOf("from"), getQuery().length()));
            skCount ++;
        }
        while (getConfigurableResultClassList().size()<skCount)
        {
            getConfigurableResultClassList().add(String.class);
        }

    }

    private Map<String, String> createModelCodePairs(String ref) throws EValidationError {
        Map<String, String> modelCodePair = new HashMap<String, String>();

        String filename = Utilities.getPlatformConfig().getExtensionInfo("hybristoolsserver").getExtensionDirectory()+"/resources/types-without-uniq-fields.txt";
        try {
            String listOfExceptions = Files.lines(Paths.get(filename)).collect(Collectors.joining("\n"));
            for (String item : listOfExceptions.split("\n"))
            {
                List<String> pair = Arrays.asList(item.split(":"));
                if (pair.size() != 2) {
                    throw new EValidationError("incorrect values in /resource/types-without-uniq-fields.txt: "+item);
                }
                modelCodePair.put(pair.get(0), pair.get(1));
            }
        } catch (IOException e) {
            //
        }

        if (ref != null) {
            List<String> refArray = Arrays.asList(ref.split(" "));
            for (String el : refArray) {
                List<String> modelCodePairEl = Arrays.asList(el.split(":"));
                if (modelCodePairEl.size() != 2) {
                    throw new EValidationError("bad syntax of ref: " + el);
                }
                //LOG.debug("ref: "+modelCodePairEl.get(0) + "=>" + modelCodePairEl.get(1));
                modelCodePair.put(modelCodePairEl.get(0), modelCodePairEl.get(1));
            }
        }



        return modelCodePair;
    }

    private void ifPKparamCreateACustomQuery() {
        PK pkObj = PK.fromLong(Long.parseLong(pk));
        String typeCode = pkObj.getTypeCodeAsString();
        List<String> typesOfThisTypeCode = Arrays.asList(JaloSession.getCurrentSession().getTypeManager().getRootComposedType(Integer.parseInt(typeCode)).getCode().toString());;
        List<String> subQueries = new ArrayList<>();
        for (String type : typesOfThisTypeCode) {
            subQueries.add("select {pk} from {"+type+"} where {pk}="+pk);
        }
        query = String.join ("UNION ", subQueries);
    }

    public FlexibleSearchToolConfiguration createAClone()  {

        XStream x = new XStream(new StaxDriver());
        FlexibleSearchToolConfiguration flexibleSearchToolConfiguration = (FlexibleSearchToolConfiguration) x.fromXML(x.toXML(this));

        return flexibleSearchToolConfiguration;
    }

    public CharSequence getDelimiter(String field, boolean rootHandler) {
        if (!rootHandler) { return ":"; }
        if (getOutputFormat().equals("TSV")) { return "\t"; }
        if (getOutputFormat().equals("CSV")) { return "\", \""; }
        if (getOutputFormat().equals("CON")) { return "\t"; }
        if (getOutputFormat().equals("BRD")) { return "\n"+ field + (field.equals("")? "" : ": "); }
        return "";
    }


    public String getStart_delimiter(String field, boolean rootHandler) {
        if (!rootHandler) { return ""; }
        if (getOutputFormat().equals("TSV")) { return ""; }
        if (getOutputFormat().equals("CSV")) { return "\""; }
        if (getOutputFormat().equals("CON")) { return ""; }
        if (getOutputFormat().equals("BRD")) { return "\n\n" +field+ (field.equals("")? "" : ": "); }
        return "";
    }

    public String getEnd_delimiter(boolean rootHandler) {
        if (!rootHandler) { return ""; }
        if (getOutputFormat().equals("TSV")) { return ""; }
        if (getOutputFormat().equals("CSV")) { return "\""; }
        if (getOutputFormat().equals("CON")) { return ""; }
        if (getOutputFormat().equals("BRD")) { return "\n"; }
        return "";
    }

    public List<Class> getConfigurableResultClassList() {
        return configurableResultClassList;
    }

    public void setConfigurableResultClassList(List<Class> configurableResultClassList) {
        this.configurableResultClassList = configurableResultClassList;
    }

    public void setConfigurableResultClassListFromStr(String resultTypes)
    {
        if (resultTypes != null && !resultTypes.equals("")) {
            try {
                List<String> resultTypesList = Arrays.asList(resultTypes.split(","));
                List<Class> classes = new ArrayList<>();
                for (String classItem : resultTypesList) {

                    Class classOfResultType = lookupClassByItsName(classItem);
                    if (classOfResultType == null ) {
                        System.out.println("class for " + classOfResultType + " is not found...");
                        return;
                    }
                    classes.add(classOfResultType);
                }
                setConfigurableResultClassList(classes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private Class lookupClassByItsName(String classItem) throws IOException {
        List<String> packages = new ArrayList<>();
        String filename = Utilities.getPlatformConfig().getExtensionInfo("hybristoolsserver").getExtensionDirectory()+"/resources/fs-packages-to-scan.txt";
        String packagesStr = Files.lines(Paths.get(filename)).collect(Collectors.joining("\n"));
        packages.addAll(Arrays.asList(packagesStr.split("\n")));
        for (String pkg : packages) {

            String fullyQualified = pkg + "." + classItem;
            try {
                Class attempt1 = null;
                try {
                    attempt1 = Class.forName(fullyQualified);
                } catch (Exception e ) { }
                if (attempt1 == null) {
                    Class attempt2 = null;
                    try {
                        attempt2 = Class.forName(fullyQualified + "Model");
                    } catch (Exception e) { }
                    if ( attempt2 != null)
                    {
                        return attempt2;
                    }
                }
                return Class.forName(fullyQualified);
            } catch (ClassNotFoundException e) {
                // Oops. Try again with the next package
            }
        }
        return null;
    }
}

