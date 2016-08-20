package com.epam.controllers;

import com.epam.exception.EValidationError;
import de.hybris.platform.cms2.servicelayer.daos.impl.DefaultCMSMediaFormatDao;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.media.MediaFormatModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Rauf_Aliev on 8/18/2016.
 */
@Controller
@RequestMapping(value = "/media")
public class MediaToolController {
    private static final Logger LOG = Logger.getLogger(MediaToolController.class);

    @Resource
    private MediaService mediaService;

    @Resource
    private ModelService modelService;

    @Resource
    DefaultCMSMediaFormatDao defaultCMSMediaFormatDao;

    @RequestMapping(value = "/mediaformats", method = RequestMethod.GET)
    @ResponseBody
    public String getAllMediaFormats() {
        List<String> results = new ArrayList<>();
        Collection<MediaFormatModel> mediaFormats = defaultCMSMediaFormatDao.getAllMediaFormats();
        results.add("qualifier\tname\n");
        for (MediaFormatModel mediaFormatModel : mediaFormats)
        {
            results.add(mediaFormatModel.getQualifier() + "\t" + mediaFormatModel.getName());
        }
        return String.join("\n", results);
    }

    @RequestMapping(value = "/medias", method = RequestMethod.GET)
    @ResponseBody
    public String getAllMedias() throws EValidationError {
        List<String> results = new ArrayList<>();

        FlexibleSearchToolController flexibleSearchToolController = Registry.getApplicationContext().getBean(FlexibleSearchToolController.class);
        return flexibleSearchToolController.executeFlexibleSearch(
            "select {pk} from {Media}",
            "", // itemtype
            "code,mediaContainer,mediaFormat", // fields
            "en", // lang
            "", // catalog
            "", // catalogVersion
            "CON", // output format
            "", // userId
            false, // debug
            1000000, // max Results
            null, // ref
            false, // beatify
            "" ); // pk

    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String createFile(
            @RequestParam(value = "code", required = false) final String code,
            @RequestParam(value = "mediaType", required = false) final String mediaType,
            @RequestParam(value = "name", required = false) final String name,
            @RequestParam(value = "filename",  required = false) final String filename,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam(value = "mediaFormatStr", required = false) final String mediaFormatStr,
            @RequestParam("file") MultipartFile file
            )
    {
        ArrayList<String> result = new ArrayList<String>();

        DataInputStream dis = null;
        try
        {
            Class classOfMediaModel1 = null;
            Class classOfMediaModel2 = null;
            try {
                classOfMediaModel1 = Class.forName("de.hybris.platform.core.model.media."+mediaType+"Model");
            }
            catch (Exception e) { }

            try {
                classOfMediaModel2 = Class.forName("de.hybris.platform.impex.model." + mediaType + "Model");
            } catch (Exception e) { }

            if (classOfMediaModel1 == null) { classOfMediaModel1 = classOfMediaModel2; }
            if (classOfMediaModel1 == null) {
                String message = mediaType+" is not found. See section 'Subtypes' of ./hybrisTypeSystem -t MediaModel for variants.";
                message = message + "hybris OOTB subtypes:  \n" +
                        " * Media (default)                    \n" +
                        " * BarcodeMedia                    \n" +
                        " * CatalogUnawareMedia             \n" +
                        " * CatalogVersionSyncScheduleMedia \n" +
                        " * ConfigurationMedia              \n" +
                        " * Document                        \n" +
                        " * EmailAttachment                 \n" +
                        " * Formatter                       \n" +
                        " * ImpExMedia                      \n" +
                        " * JasperMedia                     \n" +
                        " * JobMedia                        \n" +
                        " * LDIFMedia                       \n" +
                        " * LogFile                         \n" +
                        " * ScriptMedia   \n";
                return message;
            }
            MediaModel mediaModel = modelService.create(classOfMediaModel1);
            mediaModel.setCode(code);
            mediaModel.setRealFileName(filename);
            if (!StringUtils.isEmpty(mediaFormatStr)) {
                MediaFormatModel mediaFormatModel;
                try {
                    mediaFormatModel = defaultCMSMediaFormatDao.getMediaFormatByQualifier(mediaFormatStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: mediaFormat");
                    return "ERROR: mediaFormat";
                } catch (UnknownIdentifierException e) {
                    System.out.println("Unknown media format. Check -all-media-formats");
                    return "Unknown media format. Check -all-media-formats";
                } catch (AmbiguousIdentifierException e) {
                    System.out.println("Ambuguous Identifier");
                    return "Ambuguous Identifier";
                }
                mediaModel.setMediaFormat(mediaFormatModel);
            }
            modelService.save(mediaModel);
            dis = new DataInputStream(file.getInputStream());
            mediaService.setStreamForMedia(mediaModel, dis);
            modelService.save(mediaModel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return String.join("\n", result);
    }


}
