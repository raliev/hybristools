<%--
  [y] hybris Platform

  Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
  All rights reserved.

  This software is the confidential and proprietary information of SAP
  ("Confidential Information"). You shall not disclose such Confidential
  Information and shall use it only in accordance with the terms of the
  license agreement you entered into with SAP.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/addons/textfieldconfiguratortemplateaddon/responsive/product" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/responsive/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/responsive/nav/breadcrumb" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url value="/cart/${entryNumber}/configuration/TEXTFIELD" var="cartPageUrl"/>
<template:page pageTitle="${pageTitle}">
    <jsp:body>
        <form:form method="post" id="textFieldConfigurationForm" action="${cartPageUrl}">
            <input id="quantity" name="quantity" type="hidden" value="1" hidden="hidden">
            <product:productConfiguratorTab configurations="${configurations}"/>
            <div class="config-action">
                <div class="row">
                    <div class="col-sm-12 col-md-6">
                        <button id="update" type="submit" class="btn btn-primary btn-block">
                            <spring:theme code="configuration.page.update"/>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </jsp:body>
</template:page>
