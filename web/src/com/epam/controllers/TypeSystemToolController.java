package com.epam.controllers;

import com.epam.dto.DescriptorRecord;
import com.epam.dto.TypeDescriptorsDTO;
import com.epam.exception.EValidationError;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.*;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.type.CollectionType;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by Rauf_Aliev on 8/16/2016.
 */

@Controller
@RequestMapping(value = "/typesystem")
public class TypeSystemToolController
{
    private static final Logger LOG = Logger.getLogger(TypeSystemToolController.class);


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



    @RequestMapping(value = "/types", method = RequestMethod.GET)
    @ResponseBody
    public String getAllTypes(
            @RequestParam (value="extension", required =  false, defaultValue = "") final String extension
    )
    {
        List<String> output;
        if (extension.equals("")) {
            output = ListOfAllTypes();
        } else
        {
            output = ListOfAllTypes(extension);
        }
        return String.join("\n", output);
    }



    private List<String> ListOfAllTypes() {
        String currentCatalog = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.name");
        String currentCatalogVersion = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.version");;
        prepareSession(
                "en",
                currentCatalog,
                currentCatalogVersion
        );
        List<String> result = new ArrayList<>();
        Set<String> extensions = getAllExtensions();
        for (String extension : extensions) {
            List<ComposedTypeModel> composedTypes = getAllComposedTypes(extension);
            for (ComposedTypeModel composedType : composedTypes) {
                result.add(extension + "\t" + composedType.getCode());
            }
        }
        return result;
    }

    private List<String> ListOfAllTypes(String extension) {
        String currentCatalog = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.name");
        String currentCatalogVersion = configurationService.getConfiguration().getString("flexiblesearch.default.catalog.version");;
        prepareSession(
                "en",
                currentCatalog,
                currentCatalogVersion
        );
        List<String> result = new ArrayList<>();
            List<ComposedTypeModel> composedTypes = getAllComposedTypes(extension);
            for (ComposedTypeModel composedType : composedTypes) {
                result.add(extension + "\t" + composedType.getCode());
        }
        Set<RelationDescriptorModel> relations = getAllRelationNames(extension);
        for (RelationDescriptorModel relation : relations) {
            result.add(extension + "\t" + relation.getQualifier()+"\t"+relation.getRelationName());

        }
        return result;
    }



    @RequestMapping(value = "/type/{typeName}/attributes", method = RequestMethod.GET)
    @ResponseBody
    public String getAllAttributes(
            @PathVariable final String typeName ) {
        List<String> output = null;
        try {
            TypeModel type = typeService.getType(typeName);
            output = new ArrayList<>();
            if (type instanceof ComposedTypeModel) {
                output = ListOfAttributesForTheComposedType(typeName);
            } else if (type instanceof CollectionTypeModel) {
                output = DetailsAboutCollectionTypeModel(typeName);
            } else {
                output.add("Type " + type.getCode() + " is not supported");
            }
        } catch (Exception e)
        {
            return e.getMessage();
        }
        return String.join("\n", output);
    }

    private List<String> DetailsAboutCollectionTypeModel (String typeName) {
        List<String> result = new ArrayList<>();
        CollectionTypeModel type = (CollectionTypeModel) typeService.getTypeForCode(typeName);
        String xmlDefinition = type.getXmldefinition();
        if (xmlDefinition.contains("elementtype=")) {
            String element = xmlDefinition.substring(xmlDefinition.toLowerCase().indexOf("elementtype=")+"elementtype=".length()+1, xmlDefinition.length());
            element = element.substring(0, element.indexOf("\""));
            result.add("The Element of collection (type):\t"+element);
        }
        result.add(createPair("Code:", type.getCode()));
        result.add(createPair("Extension name:", type.getExtensionName()));
        result.add(createPair("Description:", type.getDescription()));
        result.add(createPair("XML definition:", type.getXmldefinition()));
        return result;
    }


    @RequestMapping(value = "/pk/{pk}", method = RequestMethod.GET)
    @ResponseBody
    public String getTypesByPk(
            @PathVariable final String pk )
    {
        List<String> output =  ListAllTypesByPk(pk);
        return String.join("\n", output);
    }

    private List<String> ListAllTypesByPk(String pk) {
        List<String> result = new ArrayList<>();
        if (pk != null && !pk.equals("")) {
//            System.out.println(pk);
            PK pkObj = PK.fromLong(Long.parseLong(pk));
            String typeCode = pkObj.getTypeCodeAsString();
            List<String> typesOfThisTypeCode = getTypesByTypeCode(typeCode);
            List<String> subQueries = new ArrayList<>();
            for (String type : typesOfThisTypeCode) {
                result.add(type);
            }
        }

        return result;
    }

    private List<String> getTypesByTypeCode(String typeCode) {
        return Arrays.asList(JaloSession.getCurrentSession().getTypeManager().getRootComposedType(Integer.parseInt(typeCode)).getCode().toString());
    }

    @RequestMapping(value = "/type/{typeName}/attribute/{attr}", method = RequestMethod.GET)
    @ResponseBody
    public String getAttribute(
            @PathVariable final String typeName,
            @PathVariable final String attr
    ) throws EValidationError {
        List<String> output = DetailedInfoAboutTheAttribute(typeName, attr);
        return String.join("\n", output);
    }

    private List<String> DetailedInfoAboutTheAttribute(String typeName, String attr) {
        List<String> result = new ArrayList<>();
        final ComposedTypeModel type = typeService.getComposedTypeForCode(typeName);
        AttributeDescriptorModel attrModel = typeService.getAttributeDescriptor(type, attr);
        result.add(createPair("Qualifier:", attrModel.getQualifier()));
        result.add(createPair("Name:", attrModel.getName()));
        result.add(createPair("Database column:", attrModel.getDatabaseColumn()));
        result.add(createPair("Description:", attrModel.getDescription()));
        result.add(createPair("Extension name:", attrModel.getExtensionName()));
        result.add(createPair("Type:", attrModel.getAttributeType().getCode()));
        result.add(createPair("Default Value:", IfNotNull(attrModel.getDefaultValue())));
        result.add(createPair("Attribute handler:", IfNotNull(attrModel.getAttributeHandler())));
        result.add(createPair("Flags",""));
        result.add(createPair("* Optional flag:", attrModel.getOptional().toString()));
        result.add(createPair("* Localized flag:", attrModel.getLocalized().toString()));
        result.add(createPair("* Unique flag:", attrModel.getUnique().toString()));
        result.add(createPair("",""));
        result.add(createPair("* Don't copy flag:", IfNotNull(attrModel.getDontCopy()).toString()));
        result.add(createPair("* Encrypted flag:", IfNotNull(attrModel.getEncrypted()).toString()));
        result.add(createPair("* Hidden for UI flag:", IfNotNull(attrModel.getHiddenForUI()).toString()));
        result.add(createPair("* Readonly for UI flag:", IfNotNull(attrModel.getReadOnlyForUI()).toString()));
        result.add(createPair("* Initial flag:", IfNotNull(attrModel.getInitial()).toString()));
        result.add(createPair("* Primitive flag:", IfNotNull(attrModel.getPrimitive()).toString()));
        result.add(createPair("* PartOf flag:", IfNotNull(attrModel.getPartOf()).toString()));
        result.add(createPair("* Property flag:", IfNotNull(attrModel.getProperty()).toString()));
        result.add(createPair("* Search flag:", IfNotNull(attrModel.getSearch()).toString()));
        result.add(createPair("* Writable flag:", IfNotNull(attrModel.getWritable()).toString()));
        result.add(createPair("* Removable flag:", IfNotNull(attrModel.getRemovable()).toString()));
        result.add(createPair("* Readable flag:", IfNotNull(attrModel.getReadable()).toString()));
        result.add(createPair("* Private flag:", IfNotNull(attrModel.getPrivate()).toString()));
        return result;
    }

    private String IfNotNull(Object value) {
        if (value != null) { return value.toString(); } else { return "<NULL>"; }
    }

    private String createPair(String name, String value) {
        return String.join("\t", Arrays.asList(name, value));
    }

    private List<String> ListOfAttributesForTheComposedType(String typeName) {
        final ComposedTypeModel type = typeService.getComposedTypeForCode(typeName);
        Collection<AttributeDescriptorModel> inheritedDescriptors = type.getInheritedattributedescriptors();
        Collection<AttributeDescriptorModel> declaredDescriptors = type.getDeclaredattributedescriptors();
        String superType = type.getSuperType().getCode().toString();
        List<String> subtypes = new ArrayList<>();
        for (ComposedTypeModel ctm : type.getSubtypes())
        {
            subtypes.add(ctm.getCode());
        }
        String subTypesStr = String.join("\n * ", subtypes);
        TypeDescriptorsDTO typeDescriptorsDTO = new TypeDescriptorsDTO();
        TypeDescriptorsDTO typeDescriptorsDTO2= new TypeDescriptorsDTO();
        processDescriptors(inheritedDescriptors, typeDescriptorsDTO, true);
        processDescriptors(declaredDescriptors, typeDescriptorsDTO2, false);
        typeDescriptorsDTO.getDescriptorRecordList().addAll(typeDescriptorsDTO2.getDescriptorRecordList());

        List<String> output = new ArrayList();
        output.add("Type: "+typeName);
        output.add("Supertype:"+superType);
        output.add("Subtypes: ");
        output.add(" * "+subTypesStr);
        for (DescriptorRecord descriptorRecord : typeDescriptorsDTO.getDescriptorRecordList())
        {
            List<String> columns = new ArrayList<>();
            columns.add(descriptorRecord.getQualifier());
            columns.add(descriptorRecord.getAttributeType());
            //columns.add(descriptorRecord.getDatabaseColumn());
            columns.add(descriptorRecord.getDescription());
            columns.add(descriptorRecord.getFlags());
            String outline = String.join("\t", columns);

            output.add(outline);
        }
        return output;
    }

    private void processDescriptors(Collection<AttributeDescriptorModel> descriptors, TypeDescriptorsDTO descriptorsDTO, boolean  inherited) {
        List<DescriptorRecord> descriptorRecords = new ArrayList<>();
        for (AttributeDescriptorModel attributeDescriptorModel : descriptors)
        {
            DescriptorRecord descriptorRecord = createDTO(attributeDescriptorModel);
            descriptorRecord.setInherited(inherited);
            descriptorRecords.add(descriptorRecord);
        }
        descriptorsDTO.setDescriptorRecordList(descriptorRecords);
    }

    private DescriptorRecord createDTO(AttributeDescriptorModel attributeDescriptorModel) {

        DescriptorRecord descriptorRecord = new DescriptorRecord();

        descriptorRecord.setQualifier(attributeDescriptorModel.getQualifier());
        descriptorRecord.setDatabaseColumn(attributeDescriptorModel.getDatabaseColumn());
        descriptorRecord.setDescription(attributeDescriptorModel.getDescription() == null ? "" : attributeDescriptorModel.getDescription());
        descriptorRecord.setLocalized(attributeDescriptorModel.getLocalized());
        descriptorRecord.setOptional(attributeDescriptorModel.getOptional());
        descriptorRecord.setPartof(attributeDescriptorModel.getPartOf());
        descriptorRecord.setModifiers(attributeDescriptorModel.getModifiers().toString());
        descriptorRecord.setUnique(attributeDescriptorModel.getUnique());
        descriptorRecord.setAttributeType(attributeDescriptorModel.getAttributeType().getCode());
        List<String> flags = new ArrayList<>();
        if (descriptorRecord.isUnique()) { flags.add("[UNIQ]"); }
        if (descriptorRecord.isOptional()) { flags.add("[o]"); } else {flags.add("[!]"); }
        descriptorRecord.setFlags(String.join(", ", flags));
        return descriptorRecord;
    }

    public Set<String> getAllExtensions() {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery("select {pk} from {AttributeDescriptor}");
        SearchResult<AttributeDescriptorModel> result = flexibleSearchService.search(flexibleSearchQuery);
        Iterator<AttributeDescriptorModel> admResult = result.getResult().iterator();
        Set<String> extensions = new HashSet<>();
        while (admResult.hasNext())
        {
            AttributeDescriptorModel adm = admResult.next();
            extensions.add(adm.getExtensionName());
        }
        return extensions;
    }


    public List<RelationDescriptorModel> getRelationData (RelationDescriptorModel relationDescriptorModel)
    {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery("select {pk} from {RelationDescriptor} where {RelationName} = ?rel ");
        flexibleSearchQuery.addQueryParameter("rel", relationDescriptorModel);
        SearchResult<RelationDescriptorModel> result = flexibleSearchService.search(flexibleSearchQuery);
        return result.getResult();
    }

    public Set<RelationDescriptorModel> getAllRelationNames (String extensionName)
    {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery("select {pk} from {RelationDescriptor} where {extensionname} = ?ext  and {RelationName} <> \"\" group by {RelationName}");
        flexibleSearchQuery.addQueryParameter("ext", extensionName);
        SearchResult<RelationDescriptorModel> result = flexibleSearchService.search(flexibleSearchQuery);
        Set<RelationDescriptorModel> set = new HashSet<>();
        set.addAll(result.getResult());
        return set;
    }

    public List<ComposedTypeModel> getAllComposedTypes (String extensionName) {

        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery("select {pk} from {ComposedType} where {extensionname} = ?ext");
        flexibleSearchQuery.addQueryParameter("ext", extensionName);
        SearchResult<ComposedTypeModel> result = flexibleSearchService.search(flexibleSearchQuery);
        return result.getResult();
    }
    public List<ComposedTypeModel> getAllComposedTypes (String extensionName, Set<String> filter) {

        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery("select {pk} from {ComposedType} where {extensionname} = ?ext and {code} in (?types) ");
        flexibleSearchQuery.addQueryParameter("ext", extensionName);
        flexibleSearchQuery.addQueryParameter("types", filter);
        SearchResult<ComposedTypeModel> result = flexibleSearchService.search(flexibleSearchQuery);
        return result.getResult();
    }

    public Set<ComposedTypeModel> getAllComposedTypes (Set<String> extensions)
    {
        Iterator<String> iter = extensions.iterator();
        Set<ComposedTypeModel> list = new HashSet<ComposedTypeModel>();
        while (iter.hasNext())
        {
            String extensionName = iter.next();

            List <ComposedTypeModel> types;
            types = getAllComposedTypes(extensionName);
            list.addAll(types);
        }
        return list;
    }

    public Set<ComposedTypeModel> getFilteredComposedTypes (Set<String> extensions, Set<String> particularTypesList)
    {

        Set<ComposedTypeModel> setOfTypes = new HashSet<ComposedTypeModel>();
        setOfTypes.addAll(extractTypesFromExtensions(extensions, particularTypesList));
        setOfTypes.addAll(extractTypesFromStringTypeList(particularTypesList));
        return setOfTypes;
    }

    private Collection<? extends ComposedTypeModel> extractTypesFromStringTypeList(Set<String> particularTypesList) {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery("select {pk} from {ComposedType} where {code} in (?types)");
        flexibleSearchQuery.addQueryParameter("types", particularTypesList);
        SearchResult<ComposedTypeModel> result = flexibleSearchService.search(flexibleSearchQuery);
        return result.getResult();
    }

    private Set<ComposedTypeModel> extractTypesFromExtensions(Set<String> extensions, Set<String> particularTypesList) {
        Set<ComposedTypeModel> set = new HashSet<>();
        Iterator<String> iter = extensions.iterator();
        while (iter.hasNext())
        {
            String extensionName = iter.next();

            List <ComposedTypeModel> types;
            if (!particularTypesList.isEmpty()) {
                types = getAllComposedTypes(extensionName, particularTypesList);
            } else {
                types = getAllComposedTypes(extensionName);
            }
            set.addAll(types);
        }
        return set;
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
