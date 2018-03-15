<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.shoppingcartpages" />

<fmt:message key="shoppingcart.checkout" var="msg_checkout"/>
<fmt:message key="shoppingcart.continueshopping" var="msg_continueshopping"/>

<c:url value="/product/list/" var="productlistURL"/>
<c:url value="/customer/" var="checkoutURL"/>


<div class="shoppingCart">
 
	<div class="continueShopping">
		<a href="${productlistURL}">${msg_continueshopping}</a>
	</div>

	<jsp:include flush="true" page="/jsp-shoppingcart/shoppingCartContent.jsp"></jsp:include>

	<div class="checkout">
		<c:choose>
			<c:when test="${(fn:length(shoppingCart.items) != 0) }"><a href="${checkoutURL}">${msg_checkout}</a></c:when>
			<c:otherwise></c:otherwise>
		</c:choose>
	</div>

</div>