package com.epam.controllers;

import com.epam.dto.DescriptorRecord;
import com.epam.dto.TypeDescriptorsDTO;
import com.epam.exception.EValidationError;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.type.TypeService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Rauf_Aliev on 8/16/2016.
 */

@Controller
@RequestMapping(value = "/typesystem")
public class TypeSystemToolController
{
    private static final Logger LOG = Logger.getLogger(FlexibleSearchToolController.class);
    @Resource(name = "modelService")
    private ModelService modelService;

    @Resource(name = "typeService")
    private TypeService typeService;

    @Resource
    private ConfigurationService configurationService;

    @Resource (name = "flexibleSearchService")
    private FlexibleSearchService flexibleSearchService;

    @RequestMapping(value = "/type/{typeName}/attributes", method = RequestMethod.GET)
    @ResponseBody
    public String getAllAttributes(
            @PathVariable final String typeName )
    {
        List<String> output = ListOfAttributesForTheType(typeName);
        return String.join("\n", output);
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

    private List<String> ListOfAttributesForTheType(@PathVariable String typeName) {
        final ComposedTypeModel type = typeService.getComposedTypeForCode(typeName);
        Collection<AttributeDescriptorModel> inheritedDescriptors = type.getInheritedattributedescriptors();
        Collection<AttributeDescriptorModel> declaredDescriptors = type.getDeclaredattributedescriptors();
        String superType = type.getSuperType().getCode().toString();
        List<String> subtypes = new ArrayList<>();
        for (ComposedTypeModel ctm : type.getSubtypes())
        {
            subtypes.add(ctm.getCode());
        }
        String subTypes = String.join(", ", subtypes);
        TypeDescriptorsDTO typeDescriptorsDTO = new TypeDescriptorsDTO();
        TypeDescriptorsDTO typeDescriptorsDTO2= new TypeDescriptorsDTO();
        processDescriptors(inheritedDescriptors, typeDescriptorsDTO, true);
        processDescriptors(declaredDescriptors, typeDescriptorsDTO2, false);
        typeDescriptorsDTO.getDescriptorRecordList().addAll(typeDescriptorsDTO2.getDescriptorRecordList());

        List<String> output = new ArrayList();
        output.add("Type: "+typeName);
        output.add("Supertype:"+superType);
        output.add("Subtypes: "+subTypes);
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
        descriptorRecord.setDescription(attributeDescriptorModel.getDescription());
        descriptorRecord.setLocalized(attributeDescriptorModel.getLocalized());
        descriptorRecord.setOptional(attributeDescriptorModel.getOptional());
        descriptorRecord.setPartof(attributeDescriptorModel.getPartOf());
        descriptorRecord.setModifiers(attributeDescriptorModel.getModifiers().toString());
        descriptorRecord.setUnique(attributeDescriptorModel.getUnique());
        descriptorRecord.setAttributeType(attributeDescriptorModel.getAttributeType().getCode());
        List<String> flags = new ArrayList<>();
        if (descriptorRecord.isUnique()) { flags.add("unique"); }
        if (descriptorRecord.isOptional()) { flags.add("optional"); } else {flags.add("mandatory!"); }
        descriptorRecord.setFlags(String.join(", ", flags));
        return descriptorRecord;
    }

}
