<%--
  [y] hybris Platform

  Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
  All rights reserved.

  This software is the confidential and proprietary information of SAP
  ("Confidential Information"). You shall not disclose such Confidential
  Information and shall use it only in accordance with the terms of the
  license agreement you entered into with SAP.
--%>
<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="configurations" required="true" type="java.util.ArrayList" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="product__config">
    <c:forEach var="configuration" items="${configurations}">
        <div class="product__config-row">
            <div class="row">
                <div class="col-md-6">
                    <div class="form-group">
                        <label for="configurationsKeyValueMap[${configuration.configuratorType}][${configuration.configurationLabel}]">
                                ${configuration.configurationLabel}
                        </label>
                        <input id="configurationsKeyValueMap[${configuration.configuratorType}][${configuration.configurationLabel}]"
                               name="configurationsKeyValueMap[${configuration.configuratorType}][${configuration.configurationLabel}]"
                               class="form-control" type="text" value="${configuration.configurationValue}">
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>
</div>