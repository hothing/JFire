<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.nightlabs.jfire.web.demoshop.resource.shoppingcartpages" />
<fmt:message key="shoppingcart.shoppingcart" var="msg_shoppingcart"/>
<fmt:message key="shoppingcart.itemcount" var="msg_itemcount"/>
<fmt:message key="shoppingcart.noitem" var="msg_noitem"/>
<fmt:message key="shoppingcart.price" var="msg_price"/>
<fmt:message key="shoppingcart.ordertotal" var="msg_ordertotal"/>
<fmt:message key="shoppingcart.amount" var="msg_amount"/>
<fmt:message key="shoppingcart.product" var="msg_productdesc"/>
<fmt:message key="shoppingcart.remove" var="msg_remove"/>
<fmt:message key="shoppingcart.total" var="msg_total"/>
<fmt:message key="shoppingcart.refreshamount" var="msg_refreshamount"/>

<c:url value="/images/" var="imagesURL"/>
<c:url value="/images/refresh.gif" var="refreshImage"/>

<div class="content">

<div class="title">${msg_shoppingcart}</div>
<div class="numberOfProducts">
	<c:choose>
		<c:when test="${(fn:length(shoppingCart.items) == 0) }">${msg_noitem}</c:when>
		<c:otherwise>${(fn:length(shoppingCart.items)) } ${msg_itemcount}</c:otherwise>
	</c:choose>
</div>

<c:if test="${(fn:length(shoppingCart.items) != 0) }">
	<table cellpadding="0" cellspacing="0">
		<tr>
			<th>&nbsp;</th>
			<th>${msg_productdesc}</th>
			<th>${msg_price }</th>
			<th>${msg_amount}</th>
			<th>${msg_total}</th>
			<th>${msg_remove}</th>
		</tr>
		
		<c:forEach items="${simpleProductGroupItem}" var="simpleProductGroupItem">
			<tr>
				<c:url value="/product/imageproperty/" var="propertyURL">
					<c:param name="productTypeID" value="${simpleProductGroupItem.key.productTypeID}" />
					<c:param name="structFieldID" value="${structFieldIDSmallImage}" />
				</c:url>
				<td><img src="${propertyURL}" style="width:100px"></td>
				<td>${simpleProductGroupItem.key.simpleProductName}</td>
				<td style="text-align: center;">
					<fmt:formatNumber
							currencyCode="${simpleProductGroupItem.key.price.currency.currencyID}" 
							minFractionDigits="${simpleProductGroupItem.key.price.currency.decimalDigitCount}" 
							type="currency" 
							value="${simpleProductGroupItem.key.price.amountAsDouble}"/>
					&nbsp;&nbsp;		
				</td>
				<c:url value="/shoppingcart/" var="setURL">
					<c:param name="action" value="set" />
					<c:param name="productTypeID" value="${simpleProductGroupItem.key.productTypeID}" />
					<c:param name="tariffID" value="${simpleProductGroupItem.key.tariffID}" />
				</c:url>
				<td style="width:90px">
					<form action="${setURL}" method="post">
						<input type="text" name="quantity" size="2" value="${simpleProductGroupItem.value}" />
						<input type="image" src="${refreshImage}" style="position:relative;top:10px" title="${msg_refreshamount}" />
					</form>
					&nbsp;&nbsp;
				</td>
				<td style="text-align: center;">
				<b>
					<fmt:formatNumber
							currencyCode="${simpleProductGroupItem.key.price.currency.currencyID}" 
							minFractionDigits="${simpleProductGroupItem.key.price.currency.decimalDigitCount}" 
							type="currency" 
							value="${(simpleProductGroupItem.key.price.amountAsDouble)*(simpleProductGroupItem.value)}"/>
					</b>
				</td>
				<td style="width=20px" align="center">
					<c:url value="/shoppingcart/" var="removeURL">
						<c:param name="action" value="remove" />
						<c:param name="productTypeID" value="${simpleProductGroupItem.key.productTypeID}" />
						<c:param name="tariffID" value="${simpleProductGroupItem.key.tariffID}" />
						<c:param name="quantity" value="${simpleProductGroupItem.value}" />
					</c:url>
					<a href="${removeURL}"><img src="<c:url value="/images/remove.png"/>" border="0" title="${msg_remove}"></a>
				</td>
			</tr>
		</c:forEach>
		
		<c:forEach items="${voucherGroupItem}" var="voucherGroupItem">
			<tr>
				<c:url value="/product/imageproperty/" var="propertyURL">
					<c:param name="productTypeID" value="${voucherGroupItem.key.productTypeID}" />
					<c:param name="structFieldID" value="${structFieldIDSmallImage}" />
				</c:url>
				<td></td>
				<td>${voucherGroupItem.key.voucherName}</td>
				<td style="text-align: center;">
					<fmt:formatNumber
							currencyCode="${voucherGroupItem.key.price.currency.currencyID}" 
							minFractionDigits="${voucherGroupItem.key.price.currency.decimalDigitCount}" 
							type="currency" 
							value="${voucherGroupItem.key.price.amountAsDouble}"/>
					&nbsp;&nbsp;		
				</td>
				<c:url value="/shoppingcart/" var="setURL">
					<c:param name="action" value="setvoucher" />
					<c:param name="productTypeID" value="${voucherGroupItem.key.productTypeID}" />
				</c:url>
				<td style="width:90px">
					<form action="${setURL}" method="post">
						<input type="text" name="quantity" size="2" value="${voucherGroupItem.value}" />
						<input type="image" src="${refreshImage}" style="position:relative;top:10px" title="${msg_refreshamount}" />
					</form>
					&nbsp;&nbsp;
				</td>
				<td style="text-align: center;">
				<b>
					<fmt:formatNumber
							currencyCode="${voucherGroupItem.key.price.currency.currencyID}" 
							minFractionDigits="${voucherGroupItem.key.price.currency.decimalDigitCount}" 
							type="currency" 
							value="${(voucherGroupItem.key.price.amountAsDouble)*(voucherGroupItem.value)}"/>
					</b>
				</td>
				<td style="width=20px" align="center">
					<c:url value="/shoppingcart/" var="removeURL">
						<c:param name="action" value="removevoucher" />
						<c:param name="productTypeID" value="${voucherGroupItem.key.productTypeID}" />
						<c:param name="quantity" value="${voucherGroupItem.value}" />
					</c:url>
					<a href="${removeURL}"><img src="<c:url value="/images/remove.png"/>" border="0" title="${msg_remove}"></a>
				</td>
			</tr>
		</c:forEach>
		
		<tr>
			<td colspan="4"></td>
			<td style="text-align: right;">________</td>
		</tr>
		<tr>
			<td colspan="5" style="text-align: right;">
				<b>${msg_ordertotal}:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<fmt:formatNumber currencyCode="EUR" minFractionDigits="2" type="currency" value="${(shoppingCart.totalAsDouble)}"/></b>
			</td>
		</tr>
		<tr>
		<!--  save the cart in the session scope TODO: WHY? -->
		<c:set var="cachedShoppincart" value="${shoppingCart.groupedItems}" scope="session"/>
	</table>
</c:if>

</div>
