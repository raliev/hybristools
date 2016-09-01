package com.epam.controllers;

import com.epam.helpers.CSVPrint;
import de.hybris.bootstrap.config.ConfigUtil;
import de.hybris.bootstrap.config.ExtensionInfo;
import de.hybris.platform.core.Registry;
import de.hybris.platform.spring.ctx.TenantIgnoreXmlWebApplicationContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.mutable.Mutable;
import org.apache.log4j.Logger;
import org.springframework.beans.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created by Rauf_Aliev on 8/17/2016.
 */

@Controller
@RequestMapping(value = "/beans")
public class SpringBeansToolController
{
    private static final Logger LOG = Logger.getLogger(SpringBeansToolController.class);
    /*
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public String getAllBeans(
        @RequestParam(value="extension", required = false, defaultValue = "")
        String extension
    )
    {
        List<String> output = ListOfAllBeans();
        return String.join("\n", output);
    }
    */

    @RequestMapping(value = "/bean/{bean}", method = RequestMethod.GET)
    @ResponseBody
    public String getBean(
            @PathVariable final String bean,
            @RequestParam (value="propertyName", required = false) String propertyName,
            @RequestParam (value="propertyValue", required = false) String propertyValue
    )
    {
        if (bean == null) { return "ERROR: specify a bean /tools/beans/bean/<beanName>\n"; }
        List<String> output = BeanOperations(bean, propertyName, propertyValue);
        return String.join("\n", output);
    }

    private List<String> BeanOperations(String bean, String propertyName, String propertyValue)  {
        List<String> result = new ArrayList<>();



       /* Object beanObject = Registry.getApplicationContext().getBean(bean);
        ApplicationContext context = Registry.getApplicationContext();*/
       /*
        if (beanObject == null)
        {
            beanObject = Registry.getCoreApplicationContext().getBean(bean);
            context = Registry.getCoreApplicationContext();
            if (bean == null)
            {
                beanObject = Registry.getSingletonGlobalApplicationContext().getBean(bean);
                context = Registry.getSingletonGlobalApplicationContext();
            }
        }
        */

            BeanDef  beanDef = getBeanForContext(bean);
        ApplicationContext context = beanDef.getAppContext();
        Object beanObject = beanDef.getBean();
        result.add(createPair("context", context.getId()+", "+context.getDisplayName()+", "+context.getApplicationName()));

        BeanDefinition beanDefinition = null;
        if (bean.equals("webToolController")) {

            beanDefinition = ((GenericApplicationContext) ((((WebApplicationContext) Registry.getServletContextIfExists().getContext("/trainingstorefront").getAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.DispatcherServlet")).getParent()))).getBeanFactory().getBeanDefinition(bean);
        } else {
            beanDefinition = (((GenericApplicationContext)
                    (context.getParent())).getBeanFactory().getBeanDefinition(bean));
        }


        if (propertyName != null && propertyValue!= null && !propertyName.equals("") && (!propertyValue.equals("")))
        {
            result.add("...changing the property of "+bean);
            result.add(changeBeanProperty(context, bean, beanObject, beanDefinition, propertyName, propertyValue));
            result.add("");
        }


        result.add(createPair("Class name: ", beanDefinition.getBeanClassName()));
        result.add(createPair("Description: ", beanDefinition.getDescription()));
        result.add(createPair("Bean scope: ",        IfNotNull(beanDefinition.getScope()).toString()));
        result.add(createPair("Factory bean Name: ", IfNotNull(beanDefinition.getFactoryBeanName()).toString()));
        if (beanDefinition.getDependsOn() != null) {
            result.add(createPair("Depends on: ", IfNotNull(String.join(", ", Arrays.asList(beanDefinition.getDependsOn()))).toString()));
        }
        result.add(createPair("Parent bean: ", IfNotNull(beanDefinition.getParentName()).toString()));
        result.add(createPair("* abstract bean flag: ", IfNotNull(beanDefinition.isAbstract()).toString()));
        result.add(createPair("* autowire candidate flag: ", IfNotNull(beanDefinition.isAutowireCandidate()).toString()));
        result.add(createPair("* lazy init flag: ", IfNotNull(beanDefinition.isLazyInit()).toString()));
        result.add(createPair("* primary flag: ", IfNotNull(beanDefinition.isPrimary()).toString()));
        result.add(createPair("* prototype flag: ", IfNotNull(beanDefinition.isPrototype())));

        MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
        List<PropertyValue> propertyValues = mutablePropertyValues.getPropertyValueList();
        DirectFieldAccessor wrapper = new DirectFieldAccessor(beanObject);
        result.add("Properties:");
                for (PropertyValue apropertyValue : propertyValues)
                {
                    //apropertyValue.getValue().toString()
                    result.add("* "+createPair(apropertyValue.getName(), wrapper.getPropertyValue(apropertyValue.getName()).toString())+" ("+apropertyValue.getValue().toString()+")");
                }
        result.add(createPair("* singleton flag: ", IfNotNull(beanDefinition.isSingleton())));
        result.add("");
        result.add("methods:");
        try {
            System.out.println(beanDefinition.toString());
            if (beanDefinition == null) { throw new Exception("no bean!"); }
            System.out.println(beanDefinition.toString());
            Class<?> c = Class.forName(beanDefinition.getBeanClassName());
            Object t = c.newInstance();
            Method[] allMethods = c.getDeclaredMethods();
            for (Method m : allMethods) {
                List<Parameter> params = Arrays.asList(m.getParameters());
                String paramStr = "";
                for (Parameter param : params)
                {
                    if (!paramStr.equals("")) { paramStr = paramStr + ", "; }
                    String typeS = param.getType().toString();
                    typeS = typeS.substring(typeS.lastIndexOf(".")+1,typeS.length());
                    paramStr = paramStr + typeS;
                }
                String modifStr = "";
                int modifInt = m.getModifiers();
                if ((modifInt & Modifier.PUBLIC) == Modifier.PUBLIC) { modifStr = modifStr + "public "; }
                if ((modifInt & Modifier.PROTECTED) == Modifier.PROTECTED) { modifStr = modifStr + "protected "; }
                if ((modifInt & Modifier.PRIVATE) == Modifier.PRIVATE) { modifStr = modifStr + "private "; }
                String typeS = m.getReturnType().toString();
                typeS = typeS.substring(typeS.lastIndexOf(".")+1,typeS.length());
                result.add(createPair(modifStr+" method: ", typeS +" "+m.getName()+" ("+paramStr+")"));
            }
        } catch (ClassNotFoundException x) {
               x.printStackTrace();
        } catch (InstantiationException x) {
               x.printStackTrace();
        } catch (IllegalAccessException x) {
               x.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) (((GenericApplicationContext) (Registry.getApplicationContext().getParent())).getBeanFactory());


        /*ConfigurableListableBeanFactory configurableListableBeanFactory = context
                .getBeanFactory();
*/
        return result;
    }

    private String changeBeanProperty(ApplicationContext context, String origBeanName, Object origBean, BeanDefinition beanDefinition, String propertyName, String propertyValue) {
        if (propertyValue.substring(0,1).equals("<"))
        {
            // bean
            String beanName = propertyValue.substring(1,propertyValue.length()-1);
            Object beanObject = Registry.getApplicationContext().getBean(beanName);

            /*if (beanObject == null)
            {
                beanObject = Registry.getCoreApplicationContext().getBean(beanName);
                if (beanObject == null)
                {
                    beanObject = Registry.getSingletonGlobalApplicationContext().getBean(beanName);
                }
            }
            if (beanObject == null) { return "bean "+beanName+" is not found"; }
           */

            beanObject = getBeanForContext(beanName);

            //beanDefinition.setAttribute(propertyName, beanObject);
            try {
                BeanUtils.setProperty(origBean, propertyName, beanObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else
        {
            //TypedStringValue typedString = new TypedStringValue(propertyValue);
                //BeanUtils.setProperty(origBean, propertyName, typedString);
                System.out.println("changing "+origBeanName+": "+origBean.toString());
                //BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(origBean);
                DirectFieldAccessor wrapper = new DirectFieldAccessor(origBean);
                System.out.println("before: "+wrapper.getPropertyValue(propertyName));
                wrapper.setPropertyValue(propertyName, propertyValue);
                System.out.println("after: "+wrapper.getPropertyValue(propertyName));
        }
        return "bean property changed.";
        //return "bean property "+propertyName+" of "+origBeanName+" has been successfully changed to "+propertyValue;
    }

    private String createPair(String name, String value) {
        return String.join("\t", Arrays.asList(name, value));
    }
    private String IfNotNull(Object value) {
        if (value != null) { return value.toString(); } else { return "<NULL>"; }
    }


    private Set<String> ListOfAllBeans() {
        Set<String> result = new HashSet<>();

        List<String> beanDefinitionNames = Arrays.asList(Registry.getApplicationContext().getBeanDefinitionNames());

        List<String> coreAppBeans = Arrays.asList(Registry.getCoreApplicationContext().getBeanDefinitionNames());

        List<String> globalBeans = Arrays.asList(Registry.getSingletonGlobalApplicationContext().getBeanDefinitionNames());

        result.addAll(beanDefinitionNames);
        result.addAll(coreAppBeans);
        result.addAll(globalBeans);

        return result;
    }

    @RequestMapping(value = "/all-core", method = RequestMethod.GET)
    @ResponseBody
    public String allBeans()
    {
        Set<String> output = ListOfAllBeans();
        return String.join("\n", output);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public String allBeans(@RequestParam (value="extension", required = false, defaultValue = "") String extension)
    {
        final List<ExtensionInfo> allExtensions = ConfigUtil.getPlatformConfig(Registry.class).getExtensionInfosInBuildOrder();
        if (!extension.equals("")) {
            for (ExtensionInfo e : allExtensions) {
                if (e.getName().equals(extension))
                {
                    ServletContext webroot = Registry.getServletContextIfExists().getContext(e.getWebModule().getWebRoot());
                    return String.join("\n", getAllBeans(webroot.getContextPath()));
                }
            }

        }
        else {
            List<List<String>> output = new ArrayList<List<String>>();
            for (ExtensionInfo e : allExtensions) {
                if (Registry.getServletContextIfExists() == null) { continue; }
                if (e.getWebModule() == null) { continue; }
                if (e.getWebModule().getWebRoot() == null) { continue; }
                if (Registry.getServletContextIfExists().getContext(e.getWebModule().getWebRoot()) == null) { continue; }
                ServletContext webroot = Registry.getServletContextIfExists().getContext(e.getWebModule().getWebRoot());
                List<String> beanList = getAllBeans(webroot.getContextPath());
                for (String bean : beanList)
                {
                    List<String> line = new ArrayList<>();
                    line.add(e.getName());
                    line.add(bean);
                    output.add(line);
                }
            }
            return CSVPrint.writeCSV(output, false);

        }
        return "";
    }

        private BeanDef getBeanForContext(String bean) {

            BeanDef beanDef = new BeanDef();
            Object beanObj = null;
            try {
                 beanObj = Registry.getApplicationContext().getBean(bean);
            } catch (Exception e) {}
            if (beanObj != null) {
                beanDef.setAppContext(Registry.getCoreApplicationContext());
                beanDef.setBean(beanObj);
                return beanDef; }
            else {
                final List<ExtensionInfo> allExtensions = ConfigUtil.getPlatformConfig(Registry.class).getExtensionInfosInBuildOrder();
                for (ExtensionInfo e : allExtensions) {
                    if (e.getWebModule() == null) { continue; }
                    if (e.getWebModule().getWebRoot() == null) { continue; }
                    if (Registry.getServletContextIfExists() == null) { continue; }
                    ServletContext webroot = Registry.getServletContextIfExists().getContext(e.getWebModule().getWebRoot());
                    if (webroot == null) { continue; }
                    BeanDef beanObj2 = getBeanForContext(bean, webroot.getContextPath());
                    if (beanObj2.getBean() != null) {
                        beanDef.setAppContext(beanObj2.getAppContext());
                 ;       beanDef.setBean(beanObj2.getBean());
                        return beanDef;
                    }
                }
            }
            return beanDef;
        }


        private BeanDef getBeanForContext(String bean, String context) {
            BeanDef beanDef = new BeanDef();
            ServletContext sc = Registry.getServletContextIfExists();
            if (sc == null) { return new BeanDef(); }
            ServletContext sc2 = sc.getContext(context);
            if (sc2  == null) { return new BeanDef(); }
            Object r = sc2.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
            if (r == null) { return new BeanDef(); }
            try {
                beanDef.setBean(((WebApplicationContext) Registry.getServletContextIfExists().getContext(context).getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).getBean(bean));
            } catch (Exception e) { }
            beanDef.setAppContext(WebApplicationContextUtils.getWebApplicationContext(sc2));
            return beanDef;
    }

    private List<String> getAllBeans(String context)
    {
        ServletContext sc = Registry.getServletContextIfExists();
        if (sc == null) { return new ArrayList<>(); }
        ServletContext sc2 = sc.getContext(context);
        if (sc2  == null) { return new ArrayList<>(); }
        Object r = sc2.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT");
        if (r == null) { return new ArrayList<>(); }
        return Arrays.asList(((WebApplicationContext) Registry.getServletContextIfExists().getContext(context).getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).getBeanDefinitionNames());
    }

    @RequestMapping(value = "/all1", method = RequestMethod.GET)
    @ResponseBody
    public String allBeans1() {
        final List<ExtensionInfo> allExtensions = ConfigUtil.getPlatformConfig(Registry.class).getExtensionInfosInBuildOrder();
        Set<String> output = new HashSet<>();
        for (ExtensionInfo extension : allExtensions) {
            if (extension.getWebModule()!=null && extension.getWebModule().getWebRoot()!=null)
            {
                if (Registry.getServletContextIfExists().getContext(extension.getWebModule().getWebRoot())!=null)
                {
                    ServletContext webroot = Registry.getServletContextIfExists().getContext(extension.getWebModule().getWebRoot());
                    if (WebApplicationContextUtils.getWebApplicationContext(webroot)!=null &&
                        WebApplicationContextUtils.getWebApplicationContext(webroot).getParent() !=null) {
                        output.addAll(Arrays.asList(WebApplicationContextUtils.getWebApplicationContext(webroot).getParent().getBeanDefinitionNames()));
                    }


                }
            }
        }
        return String.join("\n", output);
    }

class BeanDef {
    ApplicationContext appContext;
    Object bean;

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}

}
