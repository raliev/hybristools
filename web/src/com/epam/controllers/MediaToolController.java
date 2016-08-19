package com.epam.controllers;

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
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

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String createFile(
            @RequestParam(value = "name", required = false) final String name,
            @RequestParam(value = "filename",  required = false) final String filename,
            @RequestParam(value = "type", required = false) final String type,
            @RequestParam("file") MultipartFile file
            )
    {
        ArrayList<String> result = new ArrayList<String>();
        result.add("name="+name);
        result.add("filename="+filename);
        result.add("type="+type);
        result.add("filesize="+file.getSize());

        DataInputStream dis = null;
        try
        {
            ImpExMediaModel mediaModel = modelService.create(ImpExMediaModel.class);
            mediaModel.setCode(name);
            mediaModel.setRealFileName(filename);
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
