/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ImportCSVSavedCartForm;


@Component("importCSVSavedCartFormValidator")
public class ImportCSVSavedCartFormValidator implements Validator
{
	public static final String IMPORT_CSV_FILE_MAX_SIZE_BYTES_KEY = "import.csv.file.max.size.bytes";
	public static final String CSV_FILE_FIELD = "csvFile";
	public static final String TEXT_CSV_CONTENT_TYPE = "text/csv";
	public static final String APP_EXCEL_CONTENT_TYPE = "application/vnd.ms-excel";
	public static final String CSV_FILE_EXTENSION = ".csv";

	@Resource(name = "siteConfigService")
	private SiteConfigService siteConfigService;

	@Override
	public boolean supports(Class<?> aClass)
	{
		return ImportCSVSavedCartForm.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors)
	{
		final ImportCSVSavedCartForm importCSVSavedCartForm = (ImportCSVSavedCartForm) target;
		final MultipartFile csvFile = importCSVSavedCartForm.getCsvFile();

		if (csvFile == null || csvFile.isEmpty())
		{
			errors.rejectValue(CSV_FILE_FIELD, "import.csv.savedCart.fileRequired");
			return;
		}

		final String fileContentType = csvFile.getContentType();
		final String fileName = csvFile.getOriginalFilename();

		if (!(TEXT_CSV_CONTENT_TYPE.equalsIgnoreCase(fileContentType) || APP_EXCEL_CONTENT_TYPE.equalsIgnoreCase(fileContentType))
				|| fileName == null || !fileName.toLowerCase().endsWith(CSV_FILE_EXTENSION))
		{
			errors.rejectValue(CSV_FILE_FIELD, "import.csv.savedCart.fileCSVRequired");
			return;
		}

		if (csvFile.getSize() > getFileMaxSize())
		{
			errors.rejectValue(CSV_FILE_FIELD, "import.csv.savedCart.fileMaxSizeExceeded");
		}
	}

	protected long getFileMaxSize()
	{
		return getSiteConfigService().getLong(IMPORT_CSV_FILE_MAX_SIZE_BYTES_KEY, 0);
	}

	protected SiteConfigService getSiteConfigService()
	{
		return siteConfigService;
	}
}
