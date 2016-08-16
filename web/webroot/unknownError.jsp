<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
  ~ [y] hybris Platform
  ~
  ~ Copyright (c) 2000-2016 SAP SE or an SAP affiliate company.
  ~ All rights reserved.
  ~
  ~ This software is the confidential and proprietary information of SAP
  ~ ("Confidential Information"). You shall not disclose such Confidential
  ~ Information and shall use it only in accordance with the terms of the
  ~ license agreement you entered into with SAP.
  --%>
<c:choose>
    <c:when test="${header.accept=='application/xml'}">
<% response.setContentType("application/xml"); %>
<?xml version='1.0' encoding='UTF-8'?>
<errors>
   <error>
      <message>Unknown server error</message>
      <type>UnknownError</type>
   </error>
</errors>
</c:when>
    <c:otherwise><% response.setContentType("application/json"); %>{
   "errors" : [ {
      "message": "Unknown server error",
      "type": "UnknownError"
   } ]
}
</c:otherwise>
</c:choose>
