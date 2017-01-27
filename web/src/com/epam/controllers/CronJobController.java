
package com.epam.controllers;

import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.services.FlexibleSearchToolService;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Rauf_Aliev on 8/26/2016.
 */
@Controller
@RequestMapping(value = "/cronjobs")
public class CronJobController {

    @Resource
    CronJobService cronJobService;

    @Resource
    ModelService modelService;

    @Resource (name = "flexibleSearchToolService")
    private FlexibleSearchToolService flexibleSearchToolService;

    @Resource
    ConfigurationService configurationService;

    private static final Logger LOG = Logger.getLogger(CronJobController.class);


    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    public String execute(
            @RequestParam (value="cronJobName", defaultValue = "", required = true) String cronJobName)
    {

        CronJobModel cronJobModel = cronJobService.getCronJob(cronJobName.trim());
        cronJobService.performCronJob(cronJobModel);
        return "";
    }

    @RequestMapping(value = "/change", method = RequestMethod.GET)
    @ResponseBody
    public String change(
            @RequestParam (value="cronJobName", defaultValue = "", required = true) String cronJobName,
            @RequestParam (value="active", defaultValue = "", required = false) String active)
    {
        CronJobModel cronJobModel = cronJobService.getCronJob(cronJobName);
        if (!StringUtils.isBlank(active)) {
            cronJobModel.setActive(Boolean.parseBoolean(active));
            modelService.save(cronJobModel);
            return "status changed";
        }
        return "";
    }


    @RequestMapping(value = "/cronjobs", method = RequestMethod.GET)
    @ResponseBody
    public String getCronJobs(
            @RequestParam (value = "code", required = false, defaultValue = "") final String code   ,
            @RequestParam (value = "fields", required = false, defaultValue = "itemtype,code,active,status") final String fields,
            @RequestParam (value = "outputFormat", required = false, defaultValue = "CON") final String outputFormat,
            HttpServletResponse response

    ) {
        return showCronJobs(code, fields, outputFormat);
    }


    private String showCronJobs(
            String s, String fields, String outputFormat
    )  {
        String result;
        try {
            FlexibleSearchToolConfiguration flexibleSearchToolConfiguration = new FlexibleSearchToolConfiguration();
            flexibleSearchToolConfiguration.setQuery("select {pk} from {CronJob} " + (s.equals("") ? "" : "where {code} = \""+s+"\""));
            flexibleSearchToolConfiguration.setItemtype("");
            flexibleSearchToolConfiguration.setFields(fields);
            flexibleSearchToolConfiguration.setLanguage("en");
            flexibleSearchToolConfiguration.setCatalogName("");
            flexibleSearchToolConfiguration.setCatalogVersion("");
            flexibleSearchToolConfiguration.setOutputFormat(outputFormat);
            flexibleSearchToolConfiguration.setUser("");
            flexibleSearchToolConfiguration.setDebug(false);
            flexibleSearchToolConfiguration.setMaxResults(1000000);
            flexibleSearchToolConfiguration.setRef(null);
            flexibleSearchToolConfiguration.setBeautify(false);
            flexibleSearchToolConfiguration.setPk("");
            flexibleSearchToolConfiguration.mergeWithDefaults(configurationService.getConfiguration());
            flexibleSearchToolConfiguration.processParams();
            flexibleSearchToolConfiguration.setConfigurableResultClassListFromStr("");
            flexibleSearchToolConfiguration.validation();
            result = flexibleSearchToolService.execute(flexibleSearchToolConfiguration);

        } catch (Exception e)
        {
            return e.getMessage();
        }
        return result;
    }


}
