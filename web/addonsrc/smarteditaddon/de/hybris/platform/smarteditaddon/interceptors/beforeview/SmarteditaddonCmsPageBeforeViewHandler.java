package de.hybris.platform.smarteditaddon.interceptors.beforeview;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.interceptors.BeforeViewHandler;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This handler adds an almost unaltered page uid to a class of the jsp body with the format smartedit-page-uid-<pageUID>
 */
public class SmarteditaddonCmsPageBeforeViewHandler implements BeforeViewHandler
{

	private static final String CSS_CODE_PREFIX = "smartedit-page-uid-";
	private static final String PAGE_BODY_CSS_CLASSES = "pageBodyCssClasses";
	private static final String PAGEUID_CHARACTER_EXCLUSION_REGEXP = "[^a-zA-Z0-9-_]";

	@Override
	public void beforeView(final HttpServletRequest request, final HttpServletResponse response, final ModelAndView modelAndView)
	{

		final AbstractPageModel page = (AbstractPageModel) modelAndView.getModel().get(AbstractPageController.CMS_PAGE_MODEL);
		if (page!=null && page.getUid()!=null)
		{
			String presetCssClasses = (String) modelAndView.getModelMap().get(PAGE_BODY_CSS_CLASSES);

			final StringBuilder cssClasses = new StringBuilder();

			if (isNotBlank(presetCssClasses))
			{
				cssClasses.append(presetCssClasses);
				cssClasses.append(' ');
			}
			cssClasses.append(CSS_CODE_PREFIX).append(page.getUid().replaceAll(PAGEUID_CHARACTER_EXCLUSION_REGEXP, "-"));
			cssClasses.append(' ');

			modelAndView.addObject(PAGE_BODY_CSS_CLASSES, cssClasses.toString());
		}
	}

}
