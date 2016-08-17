package com.epam.dto;

import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.TypeModel;

/**
 * Created by Rauf_Aliev on 8/16/2016.
 */
public class DescriptorRecord {

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getAttributeType() {
        return attributeType;
    }

    String qualifier;
    String databaseColumn;
    String defaultValue;
    String description;
    String persistanceType;
    String persistanceClass;
    String modifiers;
    boolean localized;
    boolean optional;
    boolean partof;
    boolean unique;
    boolean inherited;
    String flags;
    private String attributeType;

    public String getDatabaseColumn() {
        return databaseColumn;
    }

    public void setDatabaseColumn(String databaseColumn) {
        this.databaseColumn = databaseColumn;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersistanceType() {
        return persistanceType;
    }

    public void setPersistanceType(String persistanceType) {
        this.persistanceType = persistanceType;
    }

    public String getPersistanceClass() {
        return persistanceClass;
    }

    public void setPersistanceClass(String persistanceClass) {
        this.persistanceClass = persistanceClass;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public boolean isLocalized() {
        return localized;
    }

    public void setLocalized(boolean localized) {
        this.localized = localized;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isPartof() {
        return partof;
    }

    public void setPartof(boolean partof) {
        this.partof = partof;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }
}
