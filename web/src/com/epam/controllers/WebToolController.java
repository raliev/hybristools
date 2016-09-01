package com.epam.controllers;

import com.epam.helpers.CSVPrint;
import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.platform.core.Registry;
import de.hybris.bootstrap.config.ExtensionInfo;

import de.hybris.platform.util.Utilities;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fest.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletRegistration;
import java.util.*;

/**
 * Created by Rauf_Aliev on 8/26/2016.
 */
@Controller
@RequestMapping(value = "/web")

public class WebToolController {

    private final  List<RequestMappingHandlerMapping> handlerMapping;

    @RequestMapping(value = "/extensions", method = RequestMethod.GET)
    @ResponseBody
    public String allExtensions() {
        final List<ExtensionInfo> allExtensions = ConfigUtil.getPlatformConfig(Registry.class).getExtensionInfosInBuildOrder();
        List<List<String>> result = new ArrayList<List<String>>();
        for (ExtensionInfo eInfo : allExtensions)
        {
            ArrayList<String> line = new ArrayList<>();
            line.add(eInfo.getName());
            if (eInfo != null && eInfo.getWebModule()!=null ) {
                String webroot = eInfo.getWebModule().getWebRoot();
                if (StringUtils.isBlank(webroot)) {
                    webroot = "/";
                }
                line.add(webroot);
            } else
            {
                line.add("---");
            }
            result.add(line);
        }
        return CSVPrint.writeCSV(result, false);
    }

    @Autowired
    public WebToolController( List<RequestMappingHandlerMapping>  handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @RequestMapping(value = "/contexts", method = RequestMethod.GET)
    @ResponseBody
    public String allContexts() {
        return "";
    }

    @RequestMapping(value = "/servlets", method = RequestMethod.GET)
    @ResponseBody
    public String getServlets()
    {
        List<List<String>> result = new ArrayList<List<String>>();
        final List<ExtensionInfo> allExtensions = ConfigUtil.getPlatformConfig(Registry.class).getExtensionInfosInBuildOrder();
        for (ExtensionInfo extension : allExtensions) {
            if (extension!=null && extension.getWebModule()!=null) {
                String contextPath = extension.getWebModule().getWebRoot();
                if (!StringUtils.isBlank(contextPath)) {
                    getContextDetails(result, contextPath);
                }
            }
        }
        return CSVPrint.writeCSV(result, false);

    }

    private void getContextDetails(List<List<String>> result, String contextPath) {
        if (Registry.getServletContextIfExists().getContext(contextPath) == null) { return; }
        Map<String, ? extends ServletRegistration> servlets =  Registry.getServletContextIfExists().getContext(contextPath).getServletRegistrations();
        if (servlets == null) { return; }
        for (String group : servlets.keySet()) {
            ArrayList<String> line = new ArrayList<>();

            ServletRegistration svrreg = servlets.get(group);

            Collection<String> mapping = svrreg.getMappings();

            for (String item : mapping) {
                ArrayList<String> line2 = new ArrayList<>();
                line2.add(contextPath);
                line2.add(group);
                line2.add(svrreg.getName());
                line2.add(item.toString());
                result.add(line2);
            }
        }
    }

    @RequestMapping(value = "/urls", method = RequestMethod.GET)
    @ResponseBody
    public String allUrls() {
        List<List<String>> result = new ArrayList<List<String>>();

           for (RequestMappingHandlerMapping rmhm : this.handlerMapping) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = rmhm.getHandlerMethods();
            for (RequestMappingInfo rmi : handlerMethodMap.keySet()) {
                ArrayList<String> line = new ArrayList<>();
                for (String pattern : rmi.getPatternsCondition().getPatterns()) {
                    line.add(rmhm.getApplicationContext().getParent().getApplicationName());
                    line.add(pattern);
                    Set<RequestMethod> methods = rmi.getMethodsCondition().getMethods();
                    String methodStr = "";
                    for (RequestMethod rm : methods)
                    {
                        if (!methods.equals("")) { methodStr = methodStr + ", "; }
                        methodStr += rm.toString();
                    }
                    line.add(methodStr);
                    Set<NameValueExpression<String>> expressions = rmi.getParamsCondition().getExpressions();
                    List<String> expList = new ArrayList<>();
                    for (NameValueExpression<String> nve : expressions)
                    {
                        String name = nve.getName();
                        String value = nve.getValue();
                        expList.add(name+"="+value);
                    }
                    line.add(StringUtils.join(expList, ", "));
                }
                result.add(line);
            }
        }
        return CSVPrint.writeCSV(result, false);

    }


}
