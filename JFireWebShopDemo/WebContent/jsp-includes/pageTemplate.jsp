<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url value="/" var="url_home"/>
<c:url value="/images/JFire.png" var="img_jfirelogo"/>
<c:url value="/images/Chez_Francois_Header.png" var="img_header"/>
<c:url value="/includes/styles.css" var="styleURL"/>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.errormessages" />

<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<html>
	<c:url value="/customer/" var="loginAction" >
		<c:param name="action" value="login" />
	</c:url>
	<c:url value="/customer/" var="logoutAction" >
		<c:param name="action" value="logout" />
	</c:url>
	<c:url value="/customer/" var="myDataAction" >
		<c:param name="action" value="" />
	</c:url>
	<head>
		<meta http-equiv="content-type" content="text/html;charset=UTF-8">
		<title>ChezFrancoisStore</title>
		<link rel="stylesheet" type="text/css" href="${styleURL}"/>
	</head>
	<body>
		<div class="main">
			<div class="logo" >
				<a href="${url_home}"><img src="${img_jfirelogo}" alt="JFire" title="JFire"></a>
			</div>
			<div class="menu">
				<img src="${img_header}" align="right">
			</div>
			<div class="login">
				<%
				response.setHeader("Cache-Control", "no-cache");
				String name = "";
				String mydata = "";
				String logout = "";
				String loginOrRegister = "";
				
				    if(session.getAttribute("user") == null) {
				   		name = "Guest";
				   		loginOrRegister = "Login Or Register";
				   		mydata = "";
				   		logout = "";
				    }else{
				    	Map umap = (HashMap)session.getAttribute("user");
				    	name = (String)umap.get("displayname");
				    	loginOrRegister = "";
				    	mydata = "My Data";
				    	logout = "Log Out";
				    }
				%>
				
				Hello! <b><%out.println(name);%></b><a href="${loginAction}"><%out.println(loginOrRegister);%></a><a href="${myDataAction}"><%out.println(mydata);%></a>&nbsp;<a href="${logoutAction}"><%out.println(logout);%></a>
				
				
			</div>
			<c:if test="${fn:length(request_errors) > 0}">
				<div class="errors">
					<div class="message"><fmt:message key="error.errorsoccured" /></div>
					<ul>
					<c:forEach items="${request_errors}" var="error">
						<!-- check if the Error Message has a messageKey -->
						<c:choose>
							<c:when test="${error.messageKey == null}">
								<li>${error.localizedMessage}</li>
							</c:when>
							<c:otherwise>
								<fmt:message key="${error.messageKey}" var="localizedErrorMessage"/>
								<c:choose>
									<c:when test="${fn:startsWith(localizedErrorMessage,'???')}"><li><fmt:message key="error.noresourcefound" />${error.localizedMessage}</li></c:when>
									<c:otherwise>
									<li>${localizedErrorMessage}</li></c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>
					</c:forEach>
	<!-- 			<a href="" onClick="window.history.back()"><fmt:message key="history.back" /></a> -->
					</ul>
				</div>
			</c:if>
			<div class="content">
			<c:choose>
			<c:when test="${pageTemplate_contentJSP == null}">
				<jsp:include flush="true" page="<%=request.getParameter("pageTemplate_contentJSP")%>"></jsp:include>
			</c:when>
			<c:otherwise>
				<jsp:include flush="true" page="${pageTemplate_contentJSP}"></jsp:include>
			</c:otherwise>
			</c:choose>	
			</div>
		</div>
		
	</body>
</html>

	