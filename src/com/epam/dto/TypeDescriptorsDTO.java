package com.epam.dto;

import java.util.List;

/**
 * Created by Rauf_Aliev on 8/16/2016.
 */
public class TypeDescriptorsDTO {
    List<DescriptorRecord> descriptorRecordList;

    public List<DescriptorRecord> getDescriptorRecordList() {
        return descriptorRecordList;
    }

    public void setDescriptorRecordList(List<DescriptorRecord> descriptorRecordList) {
        this.descriptorRecordList = descriptorRecordList;
    }
}
