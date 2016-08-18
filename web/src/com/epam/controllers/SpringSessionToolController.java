package com.epam.controllers;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rauf_Aliev on 8/17/2016.
 */
@Controller
@RequestMapping(value = "/sessions")
public class SpringSessionToolController
{
    private static final Logger LOG = Logger.getLogger(SpringBeansToolController.class);

    @Resource
    SessionService sessionService;

    @RequestMapping(value = "/{sessionid}/contents", method = RequestMethod.GET)
    @ResponseBody
    public String getBean(
            @PathVariable final String jsessionId
    )
    {
        List<String> results = new ArrayList<>();
        Session session = sessionService.getSession(jsessionId);
        results.add(session.getAllAttributes().toString());
        return String.join("\n", results);
    }

}
