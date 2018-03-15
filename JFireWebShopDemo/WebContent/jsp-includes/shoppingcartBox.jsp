<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:url value="/product/list/" var="productlistURL"/>
<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.shoppingcartpages" />

<div style="width:800px;height:25px;position:absolute;top:55px;">
 	<a href="<c:url value="/shoppingcart/"/>" class ="shoppingcartBox">
 	<img src="<c:url value="/images/cart.png"/>" border="0" style="position:relative;">	
	<span style="position:relative; top:-8px;">
	<c:if test="${(fn:length(shoppingCart.items) == 0) && (fn:length(voucherShoppingCart.items) == 0)}">
		<fmt:message key="shoppingcart.noitem"/>
	</c:if>
	<c:if test="${fn:length(shoppingCart.items) != 0 || (fn:length(voucherShoppingCart.items) != 0)}">
		${(fn:length(shoppingCart.items)) + (fn:length(voucherShoppingCart.items))}&nbsp;<fmt:message key="shoppingcart.itemcount"/>
	</c:if>
	</span>
	</a>
</div>


