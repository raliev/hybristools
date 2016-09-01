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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;


/**
 * Validator for equal attributes
 */
public class EqualAttributesValidator implements ConstraintValidator<EqualAttributes, Object>
{
	private static final Logger LOG = Logger.getLogger(EqualAttributesValidator.class);

	private String firstAttribute;
	private String secondAttribute;

	@Override
	public void initialize(final EqualAttributes constraintAnnotation)
	{
		Assert.notEmpty(constraintAnnotation.value());
		Assert.isTrue(constraintAnnotation.value().length == 2);
		firstAttribute = constraintAnnotation.value()[0];
		secondAttribute = constraintAnnotation.value()[1];
		Assert.hasText(firstAttribute);
		Assert.hasText(secondAttribute);
		Assert.isTrue(!firstAttribute.equals(secondAttribute));
	}

	@Override
	public boolean isValid(final Object object, final ConstraintValidatorContext constraintContext)
	{
		if (object == null)
		{
			return true;
		}
		try
		{
			final Object first = PropertyUtils.getProperty(object, firstAttribute);
			final Object second = PropertyUtils.getProperty(object, secondAttribute);
			return new EqualsBuilder().append(first, second).isEquals();
		}
		catch (final Exception e)
		{
			LOG.error("Could not validate", e);
			throw new IllegalArgumentException(e);
		}
	}
}
