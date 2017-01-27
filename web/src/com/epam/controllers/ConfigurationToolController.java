package com.epam.controllers;


import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.helpers.CSVPrint;
import com.epam.services.FlexibleSearchToolService;
import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.hmc.model.SavedValueEntryModel;
import de.hybris.platform.hmc.model.SavedValuesModel;
import de.hybris.platform.licence.sap.HybrisAdminTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.util.Utilities;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Rauf_Aliev on 8/17/2016.
 */
@Controller
@RequestMapping(value = "/configuration")
public class ConfigurationToolController
{
    private static final Logger LOG = Logger.getLogger(ConfigurationToolController.class);

    @Resource
    FlexibleSearchToolService flexibleSearchToolService;

    @Resource
    ConfigurationService configurationService;

    @Resource
    FlexibleSearchService flexibleSearchService;


    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    public String checkConfigurationAgainstPropertyFiles(
            @RequestParam(value = "extension", required = true) String extension) {
        List<String> propertyFiles = parsePropertyFileForExtenstion(extension, false);
        return String.join("\n", propertyFiles);
    }

    @RequestMapping(value = "/sync", method = RequestMethod.GET)
    @ResponseBody
    public String syncConfigurationAgainstPropertyFiles(
            @RequestParam(value = "extension", required = true) String extension) {
        List<String> propertyFiles = parsePropertyFileForExtenstion(extension, true);
        return String.join("\n", propertyFiles);
    }

    @RequestMapping(value = "/set", method = RequestMethod.GET)
    @ResponseBody
    public String syncConfigurationAgainstPropertyFiles(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "value", required = true) String value
            ) {
        configurationService.getConfiguration().setProperty(name, value);
        return "New value of '"+name+"' has been set.";
    }


    private List<String> parsePropertyFileForExtenstion(String extension, boolean sync) {



        String pathToFile1 = Utilities.getPlatformConfig().getExtensionInfo(extension).getExtensionDirectory().getAbsolutePath()+"/project.properties";
        Map<String, String> extensionConf = loadConfigurationFromPropertyFile(pathToFile1);

        String pathToFile2 = Utilities.getPlatformConfig().getSystemConfig().getConfigDir().getAbsolutePath()+"/local.properties";
        Map<String, String> localPropConf = loadConfigurationFromPropertyFile(pathToFile2);

        String pathToFile3 = Utilities.getPlatformConfig().getSystemConfig().getPlatformHome().getAbsolutePath()+"/project.properties";
        Map<String, String> platformConf = loadConfigurationFromPropertyFile(pathToFile3);

        Map<String, String> resulting = new HashMap<>();
        resulting.putAll(platformConf);
        resulting.putAll(extensionConf);
        resulting.putAll(localPropConf);

        ArrayList<String> output = new ArrayList<>();
        output.add("Loaded platform/project.properties, local.properties and "+extension+"/project.properties");

        output.add("Loaded "+resulting.keySet().size()+" items from *.properties ("+extension+", platform, config)");

        Iterator<String> keys = configurationService.getConfiguration().getKeys();

        String exceptionsFile = ConfigUtil.getPlatformConfig(ConfigurationToolController.class).getExtensionInfo("hybristoolsserver").getExtensionDirectory().getAbsolutePath()+"/resources/configuration-exceptions.txt";

        String exceptions = "";
        try {
            exceptions = Files.lines(Paths.get(exceptionsFile)).collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> exceptionList= Arrays.asList(exceptions.split("\n"));
        output.add("Loaded "+exceptionList.size()+" exceptions");
        Set<String> memoryKeys = new HashSet<>();
        boolean foundMemoryNew = false;
        boolean foundMemoryUpdated = false;
        while (keys.hasNext())
        {
            String key = keys.next();
            if (resulting.get(key) != null) {
                if (key.contains("#")) { continue;}
                if (exceptionList.contains(key)) { continue; }
                String inMemory = configurationService.getConfiguration().getString(key).trim();
                if (resulting.get(key).contains("$")) {
                    resulting.put(key, resulting.get(key).replace("${HYBRIS_TEMP_DIR}",
                            ConfigUtil.getPlatformConfig(ConfigurationToolController.class).getSystemConfig().getTempDir().getAbsolutePath()
                    ));
                    resulting.put(key, resulting.get(key).replace("${HYBRIS_CONFIG_DIR}",
                            ConfigUtil.getPlatformConfig(ConfigurationToolController.class).getSystemConfig().getConfigDir().getAbsolutePath()
                    ));
                    resulting.put(key, resulting.get(key).replace("${platformhome}",
                            ConfigUtil.getPlatformConfig(ConfigurationToolController.class).getSystemConfig().getPlatformHome().getAbsolutePath()
                    ));
                }
                if (!resulting.get(key).trim().equals(inMemory)) {
                    output.add("updated: "+key+"="+resulting.get(key)+" ("+configurationService.getConfiguration().getString(key)+")");
                    if (sync) {
                        output.add("[!] updated");
                        configurationService.getConfiguration().setProperty(key, resulting.get(key));
                    }
                    foundMemoryUpdated = true;

                }
                if (inMemory != null && resulting.get(key)==null) {
                    output.add("new in memory: "+key+"="+resulting.get(key)+" ("+configurationService.getConfiguration().getString(key)+")");
                    foundMemoryNew = true;
                }

                memoryKeys.add(key);
            }
        }
        if (!foundMemoryNew) { output.add("No new items are found in memory ");}
        if (!foundMemoryUpdated) { output.add("No updated items are found in memory");}

        boolean foundNew = false;

        for (String key : resulting.keySet())
        {
            if (!memoryKeys.contains(key)) {
                if (key.contains("#")) { continue;}
                if (exceptionList.contains(key)) { continue; }
                if (key.trim().startsWith("log4j")) { continue; }
                if (key.trim().startsWith("standalone.javaoptions")) { continue; }
                if (key.trim().startsWith("test.")) { continue; }
                output.add("new: "+key+"="+resulting.get(key)+" ("+configurationService.getConfiguration().getString(key)+")");
                if (sync) {
                    configurationService.getConfiguration().setProperty(key, resulting.get(key));
                    output.add("[!] created");
                }
            }
        }
        if (!foundNew) { output.add("No new items are found in *.properties ("+extension+", platform, config)");}
        return output;
    }

    private Map<String,String> loadConfigurationFromPropertyFile(String pathToFile1) {

        Map<String, String> result = new HashMap<>();
        try {
            String fileContents = Files.lines(Paths.get(pathToFile1)).collect(Collectors.joining("\n"));
            List<String> lines = Arrays.asList(fileContents.split("\n"));
            for (String line : lines)
            {
                if (line.indexOf("=")>0) {
                    String key = line.substring(0, line.indexOf("="));
                    String value = line.substring(line.indexOf("=") + 1, line.length());
                    result.put(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public String allConfiguration()
    {

        List<String> result = new ArrayList<>();
        Iterator<String> keys = configurationService.getConfiguration().getKeys();

        while (keys.hasNext())
        {
            String key = keys.next();
            result.add(key+" = "+configurationService.getConfiguration().getString(key));
        }

        return String.join("\n", result);
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