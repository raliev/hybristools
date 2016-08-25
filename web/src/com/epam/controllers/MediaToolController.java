package com.epam.controllers;

import org.apache.commons.configuration.Configuration;
import com.epam.configuration.FlexibleSearchToolConfiguration;
import com.epam.exception.EValidationError;
import com.epam.services.FlexibleSearchToolService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.servicelayer.daos.impl.DefaultCMSMediaFormatDao;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.media.MediaFormatModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.MediaUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
    private ConfigurationService configurationService;

    @Resource (name = "flexibleSearchToolService")
    private FlexibleSearchToolService flexibleSearchToolService;

    @Resource
    DefaultCMSMediaFormatDao defaultCMSMediaFormatDao;

    @Resource
    CatalogVersionService catalogVersionService;

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
    public String getAllMedias(
            @RequestParam (value = "code", required = false, defaultValue = "") final String code   ,
            @RequestParam (value = "fields", required = false, defaultValue = "code,mediaContainer,mediaFormat") final String fields,
            @RequestParam (value = "download", required = false, defaultValue = "false") final boolean download,
            @RequestParam (value = "catalogVersion", required = false, defaultValue = "")  String catalogVersionId,
            @RequestParam (value = "catalog", required = false, defaultValue = "")  String catalogId,
            @RequestParam (value = "outputFormat", required = false, defaultValue = "CON") final String outputFormat,
            HttpServletResponse response
    ) throws EValidationError, IOException {
        List<String> results = new ArrayList<>();

        if (download) {
            CatalogVersionModel catalogVersionModel = configureCatalogVersion(catalogVersionId, catalogId);
            MediaModel media = mediaService.getMedia(catalogVersionModel, code);
            String filename = MediaUtil.getLocalStorageDataDir()+"/"+media.getLocation();

            File outputFile = new File(filename);

            response.reset();
            response.setHeader("Content-Disposition", "attachment; filename=\"" + "test" + "\"");

            //response.setContentType(doc.getContentType());
            response.setContentLength((int)outputFile.length());

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(outputFile));

            FileCopyUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
            return "";
        } else {
            return getAllMediasInternal(code, fields, outputFormat);
        }
    }

    private CatalogVersionModel configureCatalogVersion(@RequestParam(value = "catalogVersion", required = false, defaultValue = "") String catalogVersionId, @RequestParam(value = "catalog", required = false, defaultValue = "") String catalogId) {
        Configuration configuration = configurationService.getConfiguration();
        if (StringUtils.isEmpty(catalogId)) {
              catalogId = configuration.getString("flexiblesearch.default.catalog.name");
         }
        if (StringUtils.isEmpty(catalogVersionId)) {
            catalogVersionId = configuration.getString("flexiblesearch.default.catalog.version");
        }

        return catalogVersionService.getCatalogVersion(catalogId, catalogVersionId);
    }


    private String getAllMediasInternal(String s, String fields, String outputFormat) {
        String result = "";
        try {
            FlexibleSearchToolConfiguration flexibleSearchToolConfiguration = new FlexibleSearchToolConfiguration();
            flexibleSearchToolConfiguration.setQuery("select {pk} from {Media} " + (s.equals("") ? "" : "where {code} = \""+s+"\""));
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

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String createFile(
            @RequestParam(value = "code", required = false) final String code,
            @RequestParam(value = "mediaType", required = false) final String mediaType,
            @RequestParam(value = "name", required = false) final String name,
            @RequestParam(value = "filename",  required = false) final String filename,
            @RequestParam (value = "catalogVersion", required = false, defaultValue = "")  String catalogVersionId,
            @RequestParam (value = "catalog", required = false, defaultValue = "")  String catalogId,
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
            CatalogVersionModel catalogVersionModel = configureCatalogVersion(catalogVersionId, catalogId);
            mediaModel.setCatalogVersion(catalogVersionModel);
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
