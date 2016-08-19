package com.epam.controllers;

import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.impex.ImpExResource;
import de.hybris.platform.servicelayer.impex.ImportConfig;
import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rx.Observable;
import rx.functions.Action1;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rauf_Aliev on 8/17/2016.
 */
@Controller
@RequestMapping(value = "/impex")
public class ImpexToolController
{
    private static final Logger LOG = Logger.getLogger(ImpexToolController.class);

    @Resource
    SessionService sessionService;

    @Resource
    private ImportService importService;

    @Resource
    private MediaService mediaService;


    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public String importImpex(

            @RequestParam(value = "filename", required = false) final String filename,
            /* encoding is not used */
            @RequestParam(value = "encoding", required = false) final String encoding,
            @RequestParam(value = "legacyMode", required = false) final String legacyMode,
            @RequestParam(value = "mode", required = false) final String mode,
            @RequestParam(value = "codeExecutionEnabled", required = false) final String codeExecutionEnabled,
            @RequestParam(value = "impex", required = false) final String impex,
            @RequestParam(value = "maxThreads", required = false, defaultValue = "16") final String maxThreads,
            @RequestParam(value = "mediaCode", required = false, defaultValue = "") final String mediaCode
            )
    {

        List<String> results = new ArrayList<>();

        ImportConfig importConfig = new ImportConfig();
        importConfig.setEnableCodeExecution(Boolean.parseBoolean(codeExecutionEnabled));
        importConfig.setLegacyMode(Boolean.parseBoolean(legacyMode));
        importConfig.setValidationMode(mode.equals("strict") ? ImportConfig.ValidationMode.STRICT : ImportConfig.ValidationMode.RELAXED);
        importConfig.setMaxThreads(Integer.parseInt(maxThreads));
        importConfig.setSynchronous(true);
        importConfig.setDistributedImpexEnabled(false);

        if (mediaCode!=null && !mediaCode.equals("") ) {
            ImpExResource impexResource = new ImpExResource() {
                @Override
                public ImpExMediaModel getMedia() {
                    return (ImpExMediaModel) mediaService.getMedia(mediaCode);
                }
            };
            importConfig.setScript(impexResource);
        } else
        {
            importConfig.setScript(impex);
        }
        final ImportResult importResult = importService.importData(importConfig);
        boolean isSuccessful = importResult.isSuccessful();
        boolean unresolvedLines = importResult.hasUnresolvedLines();
        String resultString = "";
        if (importResult.hasUnresolvedLines())
        {
            resultString += new String(mediaService.getDataFromMedia(importResult.getUnresolvedLines())) + "\n\n";
        }

        if (importResult.getCronJob() != null)
        {
            resultString += new String(importResult.getCronJob().getLogText());
        }
        results.add(resultString);
        results.add("Status: "+ (isSuccessful? "SUCCESSFUL" : "ERROR"));
        return String.join("\n", results);
    }

}
