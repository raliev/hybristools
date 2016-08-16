<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isErrorPage="true" trimDirectiveWhitespaces="true" %>
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

<%
    if (exception.getClass().getSimpleName().equals("InvalidResourceException")) {
        response.setStatus(400);
    }
    if (exception.getClass().getSimpleName().equals("UnknownIdentifierException")) {
        response.setStatus(400);
    }
%>
<c:choose>
    <c:when test="${header.accept=='application/xml'}">
<% response.setContentType("application/xml"); %>
<?xml version='1.0' encoding='UTF-8'?>
<errors>
   <error>
      <message><%=exception.getMessage()%></message>
      <type><%=exception.getClass().getSimpleName().replace("Exception", "Error")%></type>
   </error>
</errors>
</c:when>
    <c:otherwise><% response.setContentType("application/json"); %>{
   "errors": [ {
      "message": <%=exception.getMessage()%>,
      "type": <%=exception.getClass().getSimpleName().replace("Exception", "Error")%>
   } ]
}
</c:otherwise>
</c:choose>
