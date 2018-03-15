<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.customerregistration" />

<div class="login">
	<div class="title"><fmt:message key="customer.pleaselogin" />:</div>
	<c:url value="/customer/" var="loginAction" >
		<c:param name="action" value="login" />
	</c:url>
	<form action="${loginAction}" method="post" name="loginForm">
		<table id="loginFormular">
			<tr>
	   			<td colspan="2" class="smallTitle"><fmt:message key="customer.login" /></td>
	   		</tr>
				<% String returnedValue = "";
		     		if(request.getParameter("loginCustomerId") != null) 
		    	 	returnedValue = request.getParameter("loginCustomerId");
		   		%>
			<tr>
				<td class="formLabel"><fmt:message key="customer.username" />:*</td>
				<td class="formInput"><input type="text" name="loginCustomerId" value="<%=returnedValue%>"> </td>
			</tr>
			<tr> 
				<td class="formLabel"><fmt:message key="customer.password" />:*</td>
				<td class="formInput"><input type="password" name="loginCustomerPassword"> </td>
			</tr>
		 	<tr> 
		 		<td colspan="2" class="formSubmit">
					<c:url value="/customer/" var="lostAction" >
						<c:param name="action" value="lostPassword" />
					</c:url>
			 		<a href="${lostAction}"><fmt:message key="customer.lostpassword" /></a>
			 		<input type="submit" value="<fmt:message key="customer.login" />">
		 		</td>
			</tr>
		</table>
	</form>
</div>
