package com.epam.controllers;

import com.epam.helpers.CSVPrint;
import de.hybris.platform.util.Utilities;
import de.hybris.platform.util.logging.log4j2.HybrisLoggerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Rauf_Aliev on 8/24/2016.
 */
@Controller
@RequestMapping(value = "/logging")
public class LoggingToolController {


    @RequestMapping(value = "/loggers", method = RequestMethod.GET)
    @ResponseBody
    public String allLoggers() {
        final HybrisLoggerContext loggerCtx = (HybrisLoggerContext) LogManager.getContext(false);
        final Configuration loggerCfg = loggerCtx.getConfiguration();
        final LoggerConfig rootLoggerCfg = loggerCfg.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        Map<String, LoggerConfig> loggers = loggerCfg.getLoggers();
        List<List<String>> result = new ArrayList<List<String>>();
        for (String key : loggers.keySet()) {
            LoggerConfig loggerConfig = loggers.get(key);
            ArrayList<String> line = new ArrayList<>();
            line.add(key);
            line.add(loggerConfig.getLevel().toString());
            result.add(line);
        }
        return CSVPrint.writeCSV(result, false);
    }

    @RequestMapping(value = "/change", method = RequestMethod.GET)
    @ResponseBody
    public String ChangeLog4J2Settings(
            @RequestParam(value = "class", defaultValue = "", required = true) String logClass,
            @RequestParam(value = "logLevel", defaultValue = "", required = true) String logLevel

    ) {

        final HybrisLoggerContext loggerCtx = (HybrisLoggerContext) LogManager.getContext(false);
        final Configuration loggerCfg = loggerCtx.getConfiguration();
        final LoggerConfig loggerConfig = loggerCfg.getLoggerConfig(logClass);
        loggerConfig .setLevel(Level.getLevel(logLevel));
        loggerCtx.updateLoggers();
        return "";
    }


    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ResponseBody
    public String addLogger(
            @RequestParam(value = "class", defaultValue = "", required = true) String logClass,
            @RequestParam(value = "logLevel", defaultValue = "", required = true) String logLevel

    ) {

        final HybrisLoggerContext loggerCtx = (HybrisLoggerContext) LogManager.getContext(false);
        final Configuration loggerCfg = loggerCtx.getConfiguration();
        LoggerConfig loggerConfig = loggerCfg.getLoggers().get(logClass);
        if (loggerConfig == null) {
            // create
            String additivity = "true";
            String includeLocation = "true";
            Property[] properties = null;
            AppenderRef[] refs = {};
            Filter filter = null;
            LoggerConfig createdLoggerConfig = LoggerConfig.createLogger(
                    additivity,
                    Level.getLevel(logLevel),
                    logClass,
                    includeLocation,
                    refs,
                    properties,
                    loggerCfg,
                    filter
            );
            loggerCfg.addLogger(logClass, createdLoggerConfig);
        } else {
            loggerCfg.getLoggers().get(logClass).setLevel(Level.getLevel(logLevel));
        }
        loggerCtx.updateLoggers();
        return "";
    }


}
