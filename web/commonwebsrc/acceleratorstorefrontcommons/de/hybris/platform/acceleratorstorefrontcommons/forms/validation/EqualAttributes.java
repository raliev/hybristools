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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Retention(RUNTIME)
@Constraint(validatedBy = EqualAttributesValidator.class)
@Documented
public @interface EqualAttributes
{
	String message();

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String[] value();
}
