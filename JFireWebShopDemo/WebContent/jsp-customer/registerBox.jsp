<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.customerregistration" />

<c:url value="/customer/" var="registerAction" >
	<c:param name="action" value="register" />
</c:url>

<div class="register">
<div class="title"><fmt:message key="customer.registerasnew" />:</div>

<form action="${registerAction}" method="post" name="registerForm">
<table id="registerFormular">
	<tr>
		<td colspan="2" class="smallTitle"><fmt:message
			key="customer.personaldata" /></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.salutation" />:</td>
		<td class="formInput"><select size="1" name="">
			<option></option>
			<%
				String isSelected = "";
				if (request.getParameter("PERSONALDATA_SALUTATION_MR") != null)
					isSelected = "selected";
			%>
			<option value="PERSONALDATA_SALUTATION_MR" <%=isSelected %>><fmt:message
				key="customer.salutation_mr" /></option>
			<%
				isSelected = "";
				if (request.getParameter("PERSONALDATA_SALUTATION_MRS") != null)
					isSelected = "selected";
			%>
			<option value="PERSONALDATA_SALUTATION_MRS" <%=isSelected %>><fmt:message key="customer.salutation_mrs" /></option>
		</select></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.firstname" />:*</td>
		<td class="formInput"><input type="text" name="PERSONALDATA_FIRSTNAME" value="${param.PERSONALDATA_FIRSTNAME}"></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.name" />:*</td>
		<td class="formInput"><input type="text" name="PERSONALDATA_NAME" value="${param.PERSONALDATA_NAME}"></td>
	</tr>

	<tr>
		<td colspan="2" class="smallTitle"><fmt:message key="customer.address" /></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.address" />:*</td>
		<td class="formInput"><input type="text" name="POSTADDRESS_ADDRESS" value="${param.POSTADDRESS_ADDRESS}"></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.postcode" />:*</td>
		<td class="formInput"><input type="text" name="POSTADDRESS_POSTCODE" value="${param.POSTADDRESS_POSTCODE}"></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.city" />:*</td>
		<td class="formInput"><input type="text" name="POSTADDRESS_CITY" value="${param.POSTADDRESS_CITY}"></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.country" />:*</td>
		<td class="formInput"><input type="text" name="POSTADDRESS_COUNTRY" value="${param.POSTADDRESS_COUNTRY}"></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.phone" />:</td>
		<td class="formInput"><input type="text" name="PHONE_PRIMARY" value="${param.PHONE_PRIMARY}"></td>
	</tr>
	<tr>
		<td colspan="2" class="smallTitle"><fmt:message key="customer.userdata" /></td>
	</tr>
	<!-- USERNAME & PASSWORD  are WebCustomer datas and have no dependencies to JFire Core  -->
	<tr>
		<td class="formLabel"><fmt:message key="customer.username" />:*</td>
		<td class="formInput"><input type="text" name="customerId" value="${param.customerId}"></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.password" />:*</td>
		<td class="formInput"><input type="password" name="customerPassword" value=""></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.confirmpassword" />:*</td>
		<td class="formInput"><input type="password" name="customerPasswordConfirm" value=""></td>
	</tr>
	<tr>
		<td class="formLabel"><fmt:message key="customer.email" />:*</td>
		<td class="formInput"><input type="text" name="INTERNET_EMAIL" value="${param.INTERNET_EMAIL}"></td>
	</tr>
	<tr>
		<td colspan="2" class="formSubmit"><input type="submit"
			value="<fmt:message key="customer.register" />"></td>
	</tr>

	<c:forEach items="${request_errors}" var="error">
		<c:if test="${error.messageKey != null}">
			<fmt:message key="${error.messageKey}" var="localizedErrorMessage" />
			<div onLoad="setRedBorder(${error.localizedMessage})"></div>

			<script type="text/javascript">
					setRedBorder("${error.localizedMessage}");
					</script>
		</c:if>
	</c:forEach>




</table>
</form>
</div>
