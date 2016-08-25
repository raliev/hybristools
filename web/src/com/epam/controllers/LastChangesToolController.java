package com.epam.controllers;


import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.services.FlexibleSearchToolService;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.hmc.model.SavedValueEntryModel;
import de.hybris.platform.hmc.model.SavedValuesModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by Rauf_Aliev on 8/17/2016.
 */
@Controller
@RequestMapping(value = "/lastchanges")
public class LastChangesToolController
{
    private static final Logger LOG = Logger.getLogger(LastChangesToolController.class);

    @Resource
    FlexibleSearchToolService flexibleSearchToolService;

    @Resource
    ConfigurationService configurationService;

    @Resource
    FlexibleSearchService flexibleSearchService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public String lastChanges()
    {


        FlexibleSearchToolConfiguration flexibleSearchToolConfiguration = new FlexibleSearchToolConfiguration();
        flexibleSearchToolConfiguration.mergeWithDefaults(configurationService.getConfiguration());
        flexibleSearchToolService.prepareSession(flexibleSearchToolConfiguration);
        String query = "select {pk} from {SavedValues} order by {modifiedTime}";
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(query);
        SearchResult<SavedValuesModel> searchResult = flexibleSearchService.search(flexibleSearchQuery);
        LinkedList<HistoryRecord> historyLog =  new LinkedList<HistoryRecord>();
        for (SavedValuesModel savedValuesModel : searchResult.getResult())
        {
            ItemModel modifiedItem = savedValuesModel.getModifiedItem();
            String modifiedItemCode = modifiedItem == null ? "" : modifiedItem.getItemtype();
            Set<SavedValueEntryModel> entries = savedValuesModel.getSavedValuesEntries();
            for (SavedValueEntryModel entry : entries)
            {
                String modifiedAttribute = entry.getModifiedAttribute();
                Object newValue = entry.getNewValue();
                Date   modifiedTime = entry.getModifiedtime();
                HistoryRecord historyRecord = new HistoryRecord(modifiedTime, modifiedItemCode, modifiedAttribute, newValue );
                historyLog.add(historyRecord);
            }
        }

        String result = "";
        for (HistoryRecord hr : historyLog)
        {
            String newValueStr = "";
            if (hr.getNewValue() == null) { newValueStr = "<null>"; } else  {newValueStr = hr.getNewValue().toString(); }
            result = result +
                    hr.getModifiedTime() + "\t"+
                    hr.getModifiedItemCode() + "\t"+
                    hr.getModifiedAttribute() + "\t" +
                    newValueStr + "\n";
        }

        return result;
    }


    private class HistoryRecord {
        Date modifiedTime;
        String modifiedItemCode;
        String modifiedAttribute;
        Object newValue;

        public HistoryRecord(Date modifiedTime, String modifiedItemCode, String modifiedAttribute, Object newValue) {
            this.modifiedTime = modifiedTime;
            this.modifiedItemCode = modifiedItemCode;
            this.modifiedAttribute = modifiedAttribute;
            this.newValue = newValue;
        }

        public Date getModifiedTime() {
            return modifiedTime;
        }

        public String getModifiedItemCode() {
            return modifiedItemCode;
        }

        public String getModifiedAttribute() {
            return modifiedAttribute;
        }

        public Object getNewValue() {
            return newValue;
        }
    }
}