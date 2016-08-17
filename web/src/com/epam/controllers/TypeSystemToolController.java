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
    public String executeFlexibleSearch(@PathVariable final String typeName) throws EValidationError {
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
        return String.join("\n", output);
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
